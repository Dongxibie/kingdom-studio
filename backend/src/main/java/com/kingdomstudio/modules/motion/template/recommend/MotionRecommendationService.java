package com.kingdomstudio.modules.motion.template.recommend;

import com.kingdomstudio.modules.motion.template.entity.MotionTemplate;
import com.kingdomstudio.modules.motion.template.service.MotionTemplateService;
import com.kingdomstudio.modules.motion.template.vo.MotionRecommendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 智能推荐：意图 + 权重 → Top N，并逐条说明为什么。
 *
 * <p>评分公式（spec 要求的第一版规则 + 权重，不接模型）：
 * <pre>
 *   推荐分 = 场景匹配 40 + 风格匹配 25 + 触发匹配 18 + 情绪匹配 15 + 性能匹配 12
 *          + 推荐指数加成（0-10，用模板自己的质量分做同分时的次序）
 * </pre>
 * 每一轴命中才加分，没命中的轴不加也不扣 —— 说不清的轴不该把合适的东西压下去。
 * 性能轴例外：高预算看到一个轻量模板不扣分（能用是好事），低预算看到 GPU 档要扣一点，
 * 因为「轻量优先」的人被推一个吃显卡的效果，是实打实的踩坑。
 *
 * <p>输出里每一项都带 {@code reasons}：命中了哪些轴、具体是什么值，
 * 以及性能等级 A/B/C（由运行档位与性能子分算出）。界面右侧直接展示这些理由。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotionRecommendationService {

	/** 各轴权重 */
	private static final int WEIGHT_SCENE = 40;
	private static final int WEIGHT_STYLE = 25;
	private static final int WEIGHT_INTERACTION = 18;
	private static final int WEIGHT_EMOTION = 15;
	private static final int WEIGHT_PERFORMANCE = 12;

	/** 情绪词表：情绪不进库，用它去比对模板的风格 / 标签 / 说明 */
	private static final Map<String, List<String>> EMOTION_KEYWORDS = Map.of(
			"premium", List.of("luxury", "gold", "高级", "premium", "elegant", "精致", "金"),
			"tech", List.of("cyber", "neon", "glow", "科技", "未来", "赛博", "shader", "particle", "three", "mesh"),
			"calm", List.of("minimal", "soft", "极简", "柔和", "克制", "fade", "smooth"),
			"playful", List.of("organic", "bounce", "活泼", "有趣", "跳"),
			"warm", List.of("organic", "warm", "温暖", "治愈", "噪点", "noise"),
			"bold", List.of("particle", "3d", "tilt", "shader", "冲击", "强", "粒子", "三维"));

	/** 意图里的性能预算 → 期望档位 */
	private static final Map<String, String> PERFORMANCE_TIER = Map.of(
			"LOW", "LIGHTWEIGHT", "MEDIUM", "BALANCED", "HIGH", "GPU_ENHANCED");

	private final MotionTemplateService templateService;
	private final MotionIntentParser intentParser;

	/** 输入一句需求，返回意图 + Top N 推荐 */
	public MotionRecommendVO recommend(String query, Integer limit) {
		MotionIntent intent = intentParser.parse(query);
		int size = limit == null ? 5 : Math.min(Math.max(1, limit), 20);
		List<MotionTemplate> all = templateService.allTemplates();
		List<MotionRecommendVO.Item> items = new ArrayList<>();
		for (MotionTemplate template : all) {
			items.add(score(template, intent));
		}
		// 注意：.reversed() 作用于「整个比较器」，所以后两级要各自反一次，
		// 不能写成 a.reversed().thenComparing(b).reversed()——那会把分数也一起翻过来。
		items.sort(Comparator.comparingInt(MotionRecommendVO.Item::getScore).reversed()
				.thenComparing(Comparator.comparingInt(MotionRecommendVO.Item::getRecommendScore).reversed())
				.thenComparing(MotionRecommendVO.Item::getTemplateKey));
		List<MotionRecommendVO.Item> top = items.size() > size ? items.subList(0, size) : items;
		return MotionRecommendVO.builder()
				.query(query == null ? "" : query.trim())
				.intent(intent)
				.scanned(all.size())
				.recommendations(new ArrayList<>(top))
				.build();
	}

	/** 单个模板的打分：每一轴独立判断，命中就记一条理由 */
	MotionRecommendVO.Item score(MotionTemplate template, MotionIntent intent) {
		List<String> reasons = new ArrayList<>();
		List<String> axes = new ArrayList<>();
		int score = 0;

		if (intent.getScene() != null && intent.getScene().equals(template.getScene())) {
			score += WEIGHT_SCENE;
			axes.add("scene");
			reasons.add("场景匹配：" + label(intent, "scene", template.getScene()));
		}
		if (intent.getStyle() != null && intent.getStyle().equals(template.getStyle())) {
			score += WEIGHT_STYLE;
			axes.add("style");
			reasons.add("风格匹配：" + label(intent, "style", template.getStyle()));
		}
		if (intent.getInteraction() != null && intent.getInteraction().equals(template.getTriggerType())) {
			score += WEIGHT_INTERACTION;
			axes.add("interaction");
			reasons.add("触发方式匹配：" + label(intent, "interaction", template.getTriggerType()));
		}
		if (intent.getEmotion() != null) {
			List<String> hitWords = emotionHits(template, intent.getEmotion());
			if (!hitWords.isEmpty()) {
				score += WEIGHT_EMOTION;
				axes.add("emotion");
				reasons.add("情绪匹配：" + label(intent, "emotion", intent.getEmotion())
						+ "（命中 " + String.join(" / ", hitWords) + "）");
			}
		}
		String tier = templateService.runtimeTier(template);
		if (intent.getPerformance() != null) {
			String expected = PERFORMANCE_TIER.get(intent.getPerformance());
			if (expected != null) {
				if (expected.equals(tier)) {
					score += WEIGHT_PERFORMANCE;
					axes.add("performance");
					reasons.add("性能匹配：" + label(intent, "performance", intent.getPerformance())
							+ "（该模板属" + templateService.runtimeTierLabel(tier) + "档）");
				} else if ("LOW".equals(intent.getPerformance()) && "GPU_ENHANCED".equals(tier)) {
					// 明确说「轻量优先」时，把吃 GPU 的效果往后放一点
					score -= 8;
					reasons.add("性能不匹配：你要求轻量优先，但它是"
							+ templateService.runtimeTierLabel(tier) + "档，已往后放");
				}
			}
		}

		int recommendScore = templateService.recommendScore(template);
		score += Math.round(recommendScore / 10.0f);
		if (reasons.isEmpty()) {
			reasons.add("没命中任何一轴，按推荐指数 " + recommendScore + " 排序");
		}
		return MotionRecommendVO.Item.builder()
				.templateKey(template.getTemplateKey())
				.name(template.getName())
				.category(template.getCategory())
				// 场景 / 风格显示模板自己的值：这张卡片说的是「这个模板是什么」，
				// 命中了你要求的哪一轴写在 reasons 里，两者不要混。
				.sceneLabel(templateService.sceneLabel(template.getScene()))
				.styleLabel(templateService.styleLabel(template.getStyle()))
				.technology(template.getTechnology())
				.triggerLabel(templateService.triggerLabel(template.getTriggerType()))
				.runtimeTier(tier)
				.runtimeTierLabel(templateService.runtimeTierLabel(tier))
				.performanceGrade(performanceGrade(tier, template.getScorePerf()))
				.score(score)
				.recommendScore(recommendScore)
				.stars(templateService.stars(recommendScore))
				.description(template.getDescription())
				.matchedAxes(axes)
				.reasons(reasons)
				.build();
	}

	/** 情绪轴：拿情绪词表去比对模板的风格 / 分类 / 标签 / 名称与说明 */
	private List<String> emotionHits(MotionTemplate template, String emotion) {
		List<String> words = EMOTION_KEYWORDS.get(emotion);
		if (words == null) {
			return List.of();
		}
		String text = String.join(" ",
				nullSafe(template.getStyle()), nullSafe(template.getCategory()), nullSafe(template.getTags()),
				nullSafe(template.getName()), nullSafe(template.getNameEn()), nullSafe(template.getDescription()))
				.toLowerCase(Locale.ROOT);
		List<String> hits = new ArrayList<>();
		for (String word : words) {
			if (text.contains(word)) {
				hits.add(word);
			}
		}
		return hits;
	}

	/** 性能等级：算法放在模板服务里（组合方案的每一步也用它），这里只做转发 */
	String performanceGrade(String tier, Integer scorePerf) {
		return templateService.performanceGrade(tier, scorePerf);
	}

	/**
	 * 理由里的中文说法：优先用意图解析出的标签（用户自己说的话），拿不到再退回模板自己的标签。
	 *
	 * <p>标签表只有一份，放在 {@link MotionTemplateService}：推荐理由与列表、详情显示的是同一批中文名，
	 * 不会出现「列表叫网站首页、理由里叫 Landing Page」这种同一件事两种说法。
	 */
	private String label(MotionIntent intent, String axis, String fallback) {
		String value = intent.getLabels() == null ? null : intent.getLabels().get(axis);
		if (value != null) {
			return value;
		}
		return switch (axis) {
			case "scene" -> templateService.sceneLabel(fallback);
			case "style" -> templateService.styleLabel(fallback);
			default -> fallback == null ? "" : fallback;
		};
	}

	private String nullSafe(String value) {
		return value == null ? "" : value;
	}
}
