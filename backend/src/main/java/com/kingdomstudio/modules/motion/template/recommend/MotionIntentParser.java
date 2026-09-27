package com.kingdomstudio.modules.motion.template.recommend;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 意图解析器：一句话 → 五个轴。
 *
 * <p>第一版刻意不接模型（spec 要求）：规则 + 词表足够处理「科技感首页」「苹果官网风格」这类说法，
 * 而且**完全可复现**——同一句话任何时候解析结果都一样，界面上还能指出是哪个词触发的。
 * 模型留在 Phase 4 的 AI Motion Designer 里做更自由的理解；两层的输入输出形状一致，
 * 所以将来把模型接在同一个 {@link MotionIntent} 上即可，评分与推荐不用改。
 *
 * <p>说不清的轴一律留空：宁可少一轴打分，也不猜一个错的风格把推荐带偏。
 */
@Component
public class MotionIntentParser {

	/** 场景词表：值都是库里真实存在的 scene */
	private static final Map<String, List<String>> SCENE_WORDS = new LinkedHashMap<>();

	/** 风格词表：值都是库里真实存在的 style */
	private static final Map<String, List<String>> STYLE_WORDS = new LinkedHashMap<>();

	/** 情绪词表：情绪不进库，用来做「像不像那种感觉」的加权 */
	private static final Map<String, List<String>> EMOTION_WORDS = new LinkedHashMap<>();

	/** 触发词表 */
	private static final Map<String, List<String>> INTERACTION_WORDS = new LinkedHashMap<>();

	static {
		// 顺序即优先级：越具体的场景排越前，「AI 产品的营销页」应该落在 AI 产品页，
		// 而不是被 Landing Page 的「营销页」抢走。
		SCENE_WORDS.put("AI SaaS", List.of("saas", "ai 产品", "ai产品", "订阅", "智能产品", "模型服务"));
		SCENE_WORDS.put("Dashboard", List.of("后台", "控制台", "仪表盘", "数据面板", "报表", "看板", "dashboard", "管理端"));
		SCENE_WORDS.put("Login", List.of("登录", "注册", "sign in", "login", "表单页"));
		SCENE_WORDS.put("Game UI", List.of("游戏", "电竞", "战斗", "开黑", "game", "loading 界面"));
		SCENE_WORDS.put("Portfolio", List.of("作品集", "个人主页", "个人站", "portfolio", "简历页"));
		SCENE_WORDS.put("Landing Page", List.of("首页", "落地页", "官网", "主页", "首屏", "landing", "hero", "营销页", "产品页"));

		STYLE_WORDS.put("Minimal", List.of("极简", "简约", "简洁", "性冷淡", "留白", "minimal", "苹果", "apple"));
		STYLE_WORDS.put("Luxury", List.of("高级", "奢华", "轻奢", "酒店", "精品", "高端", "luxury", "premium", "质感"));
		STYLE_WORDS.put("Cyber", List.of("科技", "赛博", "未来", "霓虹", "机能", "cyber", "tech", "科幻"));
		STYLE_WORDS.put("Glass", List.of("玻璃", "毛玻璃", "亚克力", "glass", "磨砂"));
		STYLE_WORDS.put("Organic", List.of("有机", "自然", "柔和", "手作", "治愈", "organic", "呼吸感"));

		// 情绪轴同样按「越具体越前」排：苹果 / 高级 这类说法指向的是质感，不是别的
		EMOTION_WORDS.put("premium", List.of("高级", "高端", "精致", "质感", "大气", "尊贵", "奢华", "苹果", "apple", "premium"));
		EMOTION_WORDS.put("tech", List.of("科技", "未来", "硬核", "炫酷", "赛博", "geek", "极客", "数据感", "游戏", "电竞", "科幻"));
		EMOTION_WORDS.put("calm", List.of("平静", "克制", "安静", "干净", "稳重", "专业", "舒缓", "斯文", "后台", "面板"));
		EMOTION_WORDS.put("playful", List.of("活泼", "有趣", "俏皮", "轻快", "可爱", "互动感"));
		EMOTION_WORDS.put("warm", List.of("温暖", "治愈", "亲切", "柔和", "生活感"));
		EMOTION_WORDS.put("bold", List.of("冲击", "震撼", "强烈", "醒目", "抓眼球", "炸裂"));

		INTERACTION_WORDS.put("hover", List.of("悬停", "鼠标经过", "hover", "指上去", "划过"));
		INTERACTION_WORDS.put("scroll", List.of("滚动", "视差", "下拉", "长页", "scroll", "parallax"));
		INTERACTION_WORDS.put("click", List.of("点击", "按钮反馈", "点击后", "click", "交互反馈"));
		INTERACTION_WORDS.put("load", List.of("入场", "载入", "打开时", "首屏出现", "进入页面", "load"));
	}

	private static final Map<String, List<String>> PERFORMANCE_WORDS = new LinkedHashMap<>();

	static {
		PERFORMANCE_WORDS.put("LOW", List.of("轻量", "不要太吃", "省电", "低端机", "移动端优先", "流畅优先", "性能优先", "轻一点"));
		PERFORMANCE_WORDS.put("HIGH", List.of("效果优先", "越炫越好", "3d", "三维", "粒子", "电影感", "顶配", "吃性能也行", "不差性能"));
		PERFORMANCE_WORDS.put("MEDIUM", List.of("均衡", "别太夸张", "适中", "常规"));
	}

	private static final Map<String, String> SCENE_LABELS = Map.of(
			"Landing Page", "网站首页", "Dashboard", "数据后台", "Portfolio", "个人主页",
			"Login", "登录页面", "AI SaaS", "AI 产品页", "Game UI", "游戏界面");

	private static final Map<String, String> STYLE_LABELS = Map.of(
			"Minimal", "极简", "Luxury", "高级", "Cyber", "赛博", "Glass", "玻璃", "Organic", "有机");

	private static final Map<String, String> EMOTION_LABELS = Map.of(
			"premium", "高级感", "tech", "科技感", "calm", "克制安静",
			"playful", "轻快有趣", "warm", "温暖治愈", "bold", "强烈冲击");

	private static final Map<String, String> PERFORMANCE_LABELS = Map.of(
			"LOW", "轻量优先", "MEDIUM", "均衡", "HIGH", "效果优先");

	private static final Map<String, String> INTERACTION_LABELS = Map.of(
			"load", "加载时入场", "hover", "悬停响应", "scroll", "滚动驱动", "click", "点击反馈");

	public MotionIntent parse(String query) {
		String text = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
		Map<String, List<String>> hits = new LinkedHashMap<>();

		String scene = match(SCENE_WORDS, text, hits, "scene");
		String style = match(STYLE_WORDS, text, hits, "style");
		String emotion = match(EMOTION_WORDS, text, hits, "emotion");
		String interaction = match(INTERACTION_WORDS, text, hits, "interaction");
		String performance = performance(text, hits);

		Map<String, String> labels = new LinkedHashMap<>();
		if (scene != null) {
			labels.put("scene", SCENE_LABELS.getOrDefault(scene, scene));
		}
		if (style != null) {
			labels.put("style", STYLE_LABELS.getOrDefault(style, style));
		}
		if (emotion != null) {
			labels.put("emotion", EMOTION_LABELS.getOrDefault(emotion, emotion));
		}
		if (performance != null) {
			labels.put("performance", PERFORMANCE_LABELS.getOrDefault(performance, performance));
		}
		if (interaction != null) {
			labels.put("interaction", INTERACTION_LABELS.getOrDefault(interaction, interaction));
		}
		return MotionIntent.builder()
				.scene(scene).style(style).emotion(emotion)
				.performance(performance).interaction(interaction)
				.labels(labels).hits(hits)
				.summary(summarize(labels, query))
				.build();
	}

	/** 在一个轴里找第一个命中的词：先命中的优先（词表顺序即优先级） */
	private String match(Map<String, List<String>> table, String text,
			Map<String, List<String>> hits, String axis) {
		for (Map.Entry<String, List<String>> entry : table.entrySet()) {
			List<String> matched = new ArrayList<>();
			for (String word : entry.getValue()) {
				if (text.contains(word)) {
					matched.add(word);
				}
			}
			if (!matched.isEmpty()) {
				hits.put(axis, matched);
				return entry.getKey();
			}
		}
		return null;
	}

	/** 性能预算：先看「效果优先 / 轻量优先」这类显式说法，都没说就按场景给一个合理默认 */
	private String performance(String text, Map<String, List<String>> hits) {
		for (String level : List.of("HIGH", "LOW", "MEDIUM")) {
			List<String> matched = new ArrayList<>();
			for (String word : PERFORMANCE_WORDS.get(level)) {
				if (text.contains(word)) {
					matched.add(word);
				}
			}
			if (!matched.isEmpty()) {
				hits.put("performance", matched);
				return level;
			}
		}
		return null;
	}

	private String summarize(Map<String, String> labels, String query) {
		if (labels.isEmpty()) {
			return "没太看出具体想要什么，先按推荐指数给你一版通用组合：「" + (query == null ? "" : query.trim()) + "」。";
		}
		List<String> parts = new ArrayList<>();
		labels.forEach((axis, label) -> parts.add(axisName(axis) + "=" + label));
		return "理解到：" + String.join("，", parts) + "。";
	}

	private String axisName(String axis) {
		return switch (axis) {
			case "scene" -> "场景";
			case "style" -> "风格";
			case "emotion" -> "情绪";
			case "performance" -> "性能";
			case "interaction" -> "触发";
			default -> axis;
		};
	}
}
