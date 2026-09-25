package com.kingdomstudio.modules.motion.crawler;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 动效分类器：按关键词、README 关键片段、技术栈三路打分，取最高分分类。
 *
 * <p>刻意不做模型调用：第一版用可解释的关键词权重，命中哪一条、为什么这么分，
 * 都能在日志里说清楚；等采集量上来再考虑接模型。
 *
 * <p>纯函数逻辑（不依赖 Spring 容器行为），便于单元测试。
 */
@Component
public class MotionClassifier {

	/** 分类 → 关键词与权重：越能代表该分类的词权重越高 */
	private static final Map<String, Map<String, Integer>> RULES = new LinkedHashMap<>();

	/**
	 * 交替接收 关键词, 权重 构造权重表。
	 *
	 * <p>不用 Map.of：它最多只支持 10 组键值对，这里的分类关键词普遍更多；
	 * Map.ofEntries 又要为每一组套一层 Map.entry，读起来反而更吵。
	 */
	private static Map<String, Integer> kw(Object... pairs) {
		Map<String, Integer> map = new LinkedHashMap<>();
		for (int i = 0; i + 1 < pairs.length; i += 2) {
			map.put(String.valueOf(pairs[i]), (Integer) pairs[i + 1]);
		}
		return map;
	}

	static {
		RULES.put("Entrance", kw("enter", 3, "entrance", 3, "reveal", 3, "appear", 2, "stagger", 3,
				"fade-in", 3, "fadein", 3, "slide-in", 2, "onload", 1, "入场", 3, "出现", 2));
		RULES.put("Hover", kw("hover", 3, "mouseenter", 2, "mouse-over", 2, "shine", 2, "sweep", 3,
				"glow-on-hover", 2, "悬停", 3, "划过", 2));
		RULES.put("Scroll", kw("scroll", 3, "intersectionobserver", 3, "parallax", 3, "sticky", 2,
				"scrolltrigger", 3, "reveal-on-scroll", 2, "滚动", 3, "视差", 3));
		RULES.put("Text", kw("typing", 3, "typewriter", 3, "text", 2, "split-text", 3, "marquee", 2,
				"counter", 1, "打字", 3, "文字", 2));
		RULES.put("Particle", kw("particle", 3, "particles", 3, "constellation", 3, "starfield", 3,
				"canvas", 2, "three.points", 3, "粒子", 3));
		RULES.put("3D", kw("three.js", 3, "threejs", 3, "webgl", 3, "3d", 3, "perspective", 2,
				"rotatex", 2, "rotatey", 2, "tilt", 2, "三维", 3, "倾斜", 2));
		RULES.put("Glass", kw("glassmorphism", 3, "glass", 2, "backdrop-filter", 3, "frosted", 3,
				"blur", 2, "玻璃", 3, "磨砂", 2));
		RULES.put("Cursor", kw("cursor", 3, "mousemove", 2, "follow", 2, "magnetic", 3, "trailing", 3,
				"pointer", 2, "光标", 3, "跟随", 2));
		RULES.put("Background", kw("background", 2, "gradient", 2, "noise", 2, "mesh", 2, "aurora", 3,
				"grid", 2, "背景", 2, "光斑", 2));
		RULES.put("Loading", kw("loading", 3, "skeleton", 3, "shimmer", 3, "spinner", 3, "progress", 2,
				"loader", 3, "加载", 3, "骨架", 3));
	}

	/** 分类结果：分类 + 命中的关键词（便于排查），未命中时降级为 DRAFT */
	public record Result(String category, int score, List<String> matched) {
		public boolean confident() {
			return score > 0;
		}
	}

	/**
	 * 打分分类。
	 *
	 * @param text 参与匹配的文本（建议：名称 + 描述 + README 摘要 + topics）
	 */
	public Result classify(String text) {
		String haystack = text == null ? "" : text.toLowerCase(Locale.ROOT);
		String best = "Entrance";
		int bestScore = 0;
		List<String> bestMatched = List.of();
		for (Map.Entry<String, Map<String, Integer>> entry : RULES.entrySet()) {
			int score = 0;
			List<String> matched = new java.util.ArrayList<>();
			for (Map.Entry<String, Integer> keyword : entry.getValue().entrySet()) {
				if (haystack.contains(keyword.getKey())) {
					score += keyword.getValue();
					matched.add(keyword.getKey());
				}
			}
			if (score > bestScore) {
				bestScore = score;
				best = entry.getKey();
				bestMatched = matched;
			}
		}
		return new Result(best, bestScore, bestMatched);
	}

	/** 供前端 / 文档展示用 */
	public List<String> categories() {
		return List.copyOf(RULES.keySet());
	}
}
