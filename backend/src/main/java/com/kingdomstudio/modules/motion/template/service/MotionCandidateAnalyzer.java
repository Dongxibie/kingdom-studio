package com.kingdomstudio.modules.motion.template.service;

import com.kingdomstudio.modules.motion.template.entity.MotionCandidate;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 候选分析器：把「一堆仓库元数据」变成「能筛选的动效线索」。
 *
 * <p><b>为什么是规则而不是模型</b>：这一步要的是**可复现、可解释、不花钱**。
 * 同一个仓库任何时候分析出来的分类都一样，而且能说出「因为命中了 text / typewriter」；
 * 换成模型就可能同样的输入两次不同结果，反而不好解释给使用者听。
 * 需要模型的地方在动效助手那边：那里要的是理解自然语言，不是归类。
 *
 * <p>分析结果落到候选表上，人工筛选只看结论与理由；规则本身全是纯函数，单测直接覆盖。
 */
@Component
public class MotionCandidateAnalyzer {

	/** 七分类 + 各自的关键词表（命中越多越像哪一类） */
	private static final Map<String, List<String>> CATEGORY_HINTS = new LinkedHashMap<>();

	static {
		CATEGORY_HINTS.put("文字动画", List.of("text", "typewriter", "typing", "split text", "headline", "letter", "font"));
		CATEGORY_HINTS.put("卡片交互", List.of("card", "tilt", "glass", "hover", "flip", "spotlight"));
		CATEGORY_HINTS.put("按钮交互", List.of("button", "btn", "magnetic", "ripple", "cursor", "pointer"));
		CATEGORY_HINTS.put("滚动动画", List.of("scroll", "parallax", "reveal", "sticky", "scrollytelling"));
		CATEGORY_HINTS.put("首屏动画", List.of("hero", "landing", "header", "intro", "entrance"));
		CATEGORY_HINTS.put("背景效果", List.of("background", "gradient", "aurora", "noise", "mesh", "particle", "stars", "blob"));
		CATEGORY_HINTS.put("三维 WebGL", List.of("three", "threejs", "webgl", "glsl", "shader", "rain", "vulkan"));
	}

	private static final Map<String, List<String>> TRIGGER_HINTS = new LinkedHashMap<>();

	static {
		TRIGGER_HINTS.put("hover", List.of("hover", "mouse", "pointer", "cursor"));
		TRIGGER_HINTS.put("scroll", List.of("scroll", "parallax", "sticky"));
		TRIGGER_HINTS.put("click", List.of("click", "tap", "toggle", "button"));
		TRIGGER_HINTS.put("load", List.of("load", "entrance", "intro", "appear", "fade-in", "reveal"));
	}

	/** 不是前端动效实现语言的仓库：看一眼就能判掉，不必让人再过一遍 */
	private static final List<String> OFF_LANGUAGE = List.of(
			"swift", "java", "kotlin", "c++", "c", "objective-c", "dart", "go", "ruby", "php", "c#", "rust");

	/** 清单 / 合集类仓库：没有单一动效模式可提炼 */
	private static final List<String> LIST_HINTS = List.of(
			"awesome", "curated list", "collection of", "checklist", "cheatsheet", "resources for");

	/** 星数门槛：低于它连参考价值都谈不上 */
	private static final int MIN_STARS = 120;

	/** 分析结论 */
	@Data
	@AllArgsConstructor
	public static class Analysis {
		private String category;
		private String technology;
		private String trigger;
		private int difficulty;
		private String performanceLevel;
		private int visualScore;
		private List<String> hints;
	}

	/** 自动初审结论 */
	@Data
	@AllArgsConstructor
	public static class Verdict {
		private String status;
		private String note;
	}

	public Analysis analyze(MotionCandidate candidate) {
		String text = text(candidate);
		String category = null;
		List<String> hits = new ArrayList<>();
		int best = 0;
		for (Map.Entry<String, List<String>> entry : CATEGORY_HINTS.entrySet()) {
			int score = 0;
			List<String> matched = new ArrayList<>();
			for (String word : entry.getValue()) {
				if (text.contains(word)) {
					score += 1;
					matched.add(word);
				}
			}
			if (score > best) {
				best = score;
				category = entry.getKey();
				hits = matched;
			}
		}
		String technology = technologyOf(text);
		int difficulty = difficultyOf(text);
		return new Analysis(category, technology, triggerOf(text), difficulty,
				performanceOf(technology, difficulty), visualScore(candidate.getStars()), hits);
	}

	/** 自动初审：能被规则明确判掉的判掉并留下理由，判不掉的交给人工 */
	public Verdict autoReview(MotionCandidate candidate) {
		String language = candidate.getLanguage() == null ? "" : candidate.getLanguage().toLowerCase(Locale.ROOT);
		if (OFF_LANGUAGE.contains(language)) {
			return new Verdict("REJECTED", "主要语言是 " + candidate.getLanguage() + "，不是前端动效可直接借鉴的实现");
		}
		String text = text(candidate);
		for (String hint : LIST_HINTS) {
			if (text.contains(hint)) {
				return new Verdict("REJECTED", "是清单 / 合集类仓库，没有可提炼的单一动效模式");
			}
		}
		if (analyze(candidate).getCategory() == null) {
			return new Verdict("REJECTED", "关键词分析没命中任何一类，无法归档");
		}
		int stars = candidate.getStars() == null ? 0 : candidate.getStars();
		if (stars < MIN_STARS) {
			return new Verdict("REJECTED", "星数偏低（" + stars + "），案例参考价值不足");
		}
		return new Verdict("ANALYZED", "规则分析已完成，等待人工筛选");
	}

	private String text(MotionCandidate candidate) {
		return String.join(" ",
				candidate.getName() == null ? "" : candidate.getName(),
				candidate.getFullName() == null ? "" : candidate.getFullName(),
				candidate.getDescription() == null ? "" : candidate.getDescription(),
				candidate.getTopics() == null ? "" : candidate.getTopics()).toLowerCase(Locale.ROOT);
	}

	String triggerOf(String text) {
		for (Map.Entry<String, List<String>> entry : TRIGGER_HINTS.entrySet()) {
			for (String word : entry.getValue()) {
				if (text.contains(word)) {
					return entry.getKey();
				}
			}
		}
		return "load";
	}

	String technologyOf(String text) {
		if (text.contains("three")) {
			return "Three.js";
		}
		if (text.contains("webgl") || text.contains("glsl") || text.contains("shader")) {
			return "Canvas";
		}
		return "CSS";
	}

	int difficultyOf(String text) {
		if (text.contains("glsl") || text.contains("webgl") || text.contains("shader")) {
			return 3;
		}
		if (text.contains("javascript") || text.contains("typescript") || text.contains("canvas")) {
			return 2;
		}
		return 1;
	}

	/** 与模板侧同一条规则：GPU / 逐像素计算一律 GPU_ENHANCED，其余按难度落档 */
	String performanceOf(String technology, int difficulty) {
		if ("Canvas".equals(technology) || "Three.js".equals(technology)) {
			return "GPU_ENHANCED";
		}
		return difficulty <= 1 ? "LIGHTWEIGHT" : "BALANCED";
	}

	/** 视觉分只用于排序：星数取 0.18 次幂后映射到 78-97，避免「十万星」把分数顶死 */
	int visualScore(Integer stars) {
		int value = stars == null ? 0 : stars;
		double bonus = value <= 0 ? 0 : Math.min(19.0, 6 * Math.pow(value, 0.18));
		return (int) Math.min(97, Math.round(78 + bonus));
	}
}
