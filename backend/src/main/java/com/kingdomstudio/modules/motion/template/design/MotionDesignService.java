package com.kingdomstudio.modules.motion.template.design;

import com.fasterxml.jackson.databind.JsonNode;
import com.kingdomstudio.modules.motion.template.recommend.MotionEmotionWords;
import com.kingdomstudio.modules.motion.template.recommend.MotionIntent;
import com.kingdomstudio.modules.motion.template.recommend.MotionIntentParser;
import com.kingdomstudio.modules.motion.template.service.MotionModelClient;
import com.kingdomstudio.modules.motion.template.service.MotionRecipeService;
import com.kingdomstudio.modules.motion.template.service.MotionTemplateService;
import com.kingdomstudio.modules.motion.template.vo.MotionDesignVO;
import com.kingdomstudio.modules.motion.template.vo.MotionRecipeVO;
import com.kingdomstudio.modules.motion.template.vo.RecipePerformanceVO;
import com.kingdomstudio.modules.motion.template.vo.RecipeStepVO;
import com.kingdomstudio.modules.motion.template.vo.TemplateParamVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AI Motion Designer：一句需求 → 一套完整设计方案。
 *
 * <p><b>模型在这里做什么、不做什么，是这一层最重要的设计。</b>
 * 它只做三件事：在**已有的 30 套组合方案**里挑一套、在**每一步允许的参数区间内**微调、写一段设计说明。
 * 它不新增步骤、不改顺序、不生成代码 —— 结构来自组合系统，代码来自模板库，
 * 所以「模型说得好听但东西跑不起来」这类问题从根上不会出现。
 *
 * <p>没有模型时走完全相同的输出结构：五轴意图（规则解析）+ 组合选取（按轴加权）+ 参数落地（默认值）。
 * 两条路径的差别只有「谁选的方案」与「参数调过没有」，这一点如实写在 source 与 notes 里。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotionDesignService {

	/** 组合方案的轴权重：与模板推荐同源，只是对象从模板换成方案 */
	private static final int WEIGHT_SCENE = 40;
	private static final int WEIGHT_STYLE = 25;
	private static final int WEIGHT_EMOTION = 15;
	private static final int WEIGHT_PERFORMANCE = 12;

	/** 预算 → 期望档位 */
	private static final Map<String, String> PERFORMANCE_TIER = Map.of(
			"LOW", "LIGHTWEIGHT", "MEDIUM", "BALANCED", "HIGH", "GPU_ENHANCED");

	/**
	 * 轻量预算下要减半的参数名（全是「越多越重」的东西）。
	 *
	 * <p>按**整段**匹配（用 - 或 _ 切开的词），不做子串包含：
	 * 子串会把 --m-timeline 当成 line（其实它是时长），把时长减半只会让动画变快，并不会更省。
	 */
	private static final List<String> COUNT_HINTS = List.of(
			"count", "particles", "particle", "dots", "dot", "points", "quantity", "lines");

	/** 中文提示词：参数名里是连续的词，走包含判断 */
	private static final List<String> COUNT_HINTS_CN = List.of("数量", "粒子", "个数", "密度");

	private final MotionTemplateService templateService;
	private final MotionRecipeService recipeService;
	private final MotionIntentParser intentParser;
	private final MotionModelClient modelClient;

	public MotionDesignVO design(String query, String performanceOverride, Integer maxSteps) {
		String text = query == null ? "" : query.trim();
		MotionIntent intent = intentParser.parse(text);
		String budget = normalizeBudget(performanceOverride) != null
				? normalizeBudget(performanceOverride)
				: intent.getPerformance();
		int limit = maxSteps == null ? 5 : Math.min(Math.max(1, maxSteps), 8);

		List<MotionRecipeVO> recipes = recipeService.list(null);
		MotionRecipeVO picked = pickRecipe(recipes, intent, budget);
		if (picked == null) {
			// 库里一套方案都没有时给出空方案，而不是抛异常：页面能显示「先导入种子」
			return MotionDesignVO.builder()
					.query(text)
					.intent(intent)
					.animations(List.of())
					.notes(List.of("模板库里还没有组合方案，先执行 db/extensions_motion_recipe_seed.sql。"))
					.explanation("暂时没有可用的组合方案。")
					.source("RULE")
					.build();
		}

		List<String> notes = new ArrayList<>();
		JsonNode raw = modelClient.design(text, catalog(recipes));
		boolean modelUsed = false;
		String fallbackReason = null;

		MotionRecipeVO chosen = picked;
		Map<String, Map<String, Double>> modelParams = Map.of();
		String modelExplanation = null;
		if (raw == null) {
			fallbackReason = modelClient.enabled()
					? "模型本次没有返回可用结果（超时、格式不合法或服务不可用），已改用内置规则设计。"
					: "未配置模型（MOTION_LLM_BASE_URL / MOTION_LLM_API_KEY / MOTION_LLM_MODEL 任一为空），使用内置规则设计。";
		} else {
			String recipeKey = textOrNull(raw.path("recipeKey"));
			MotionRecipeVO fromModel = recipes.stream()
					.filter(item -> item.getRecipeKey().equals(recipeKey)).findFirst().orElse(null);
			if (recipeKey == null) {
				notes.add("模型没有指定组合方案，本次沿用规则选出的「" + picked.getName() + "」。");
			} else if (fromModel == null) {
				notes.add("模型给的方案 key（" + recipeKey + "）不在现有 30 套组合里，已忽略，沿用「" + picked.getName() + "」。");
			} else {
				chosen = fromModel;
				modelUsed = true;
				notes.add("方案由模型从现有组合里选出，并在参数允许区间内做了微调。");
			}
			ParamPlan plan = readModelParams(raw.path("params"), chosen);
			modelParams = plan.values();
			if (!plan.notes().isEmpty()) {
				notes.addAll(plan.notes());
			}
			if (!modelParams.isEmpty()) {
				modelUsed = true;
			}
			modelExplanation = textOrNull(raw.path("explanation"));
			applyModelAxes(intent, raw, notes);
		}

		List<MotionDesignVO.PlanStep> animations = new ArrayList<>();
		int order = 0;
		for (RecipeStepVO step : chosen.getSteps()) {
			if (order >= limit) {
				break;
			}
			order++;
			animations.add(toPlanStep(order, step, budget, modelParams.get(step.getTemplateKey()), notes));
		}

		RecipePerformanceVO performance = chosen.getPerformance();
		if (performance != null && performance.getNote() != null) {
			notes.add("整体性能：" + performance.getNote());
		}

		return MotionDesignVO.builder()
				.query(text)
				.intent(intent)
				.recipe(chosen)
				.animations(animations)
				.performance(performance)
				.notes(notes)
				.explanation(modelExplanation != null ? modelExplanation : explain(intent, chosen, animations))
				.source(modelUsed ? "MODEL" : "RULE")
				.modelName(modelUsed ? modelClient.modelName() : null)
				.fallbackReason(fallbackReason)
				.build();
	}

	// ------------------------------------------------------------------ 规则路径

	/** 在 30 套组合里按五轴挑一套：命中越多分越高，同分按方案 key 排，结果稳定 */
	MotionRecipeVO pickRecipe(List<MotionRecipeVO> recipes, MotionIntent intent, String budget) {
		return recipes.stream()
				.map(recipe -> Map.entry(recipe, recipeScore(recipe, intent, budget)))
				.filter(entry -> entry.getValue() > Integer.MIN_VALUE)
				.sorted(Comparator.comparingInt((Map.Entry<MotionRecipeVO, Integer> entry) -> entry.getValue()).reversed()
						.thenComparing(entry -> entry.getKey().getRecipeKey()))
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(null);
	}

	int recipeScore(MotionRecipeVO recipe, MotionIntent intent, String budget) {
		int score = 0;
		if (intent.getScene() != null && intent.getScene().equals(recipe.getScene())) {
			score += WEIGHT_SCENE;
		}
		if (intent.getStyle() != null && intent.getStyle().equals(recipe.getStyle())) {
			score += WEIGHT_STYLE;
		}
		if (intent.getEmotion() != null && !emotionHits(recipe, intent.getEmotion()).isEmpty()) {
			score += WEIGHT_EMOTION;
		}
		if (budget != null && recipe.getPerformance() != null) {
			String expected = PERFORMANCE_TIER.get(budget);
			String worst = recipe.getPerformance().getWorstTier();
			if (expected != null && expected.equals(worst)) {
				score += WEIGHT_PERFORMANCE;
			} else if ("LOW".equals(budget) && "GPU_ENHANCED".equals(worst)) {
				score -= 8;
			}
		}
		return score * 100 + (recipe.getScore() == null ? 0 : recipe.getScore());
	}

	/** 方案的情绪命中：扫方案自己的文本 + 成员的风格与名称（组合的情绪来自成员） */
	List<String> emotionHits(MotionRecipeVO recipe, String emotion) {
		StringBuilder text = new StringBuilder()
				.append(nullSafe(recipe.getName())).append(' ')
				.append(nullSafe(recipe.getDescription())).append(' ')
				.append(nullSafe(recipe.getStyle())).append(' ')
				.append(nullSafe(recipe.getBestFor() == null ? "" : String.join(" ", recipe.getBestFor())));
		for (RecipeStepVO step : recipe.getSteps()) {
			text.append(' ').append(nullSafe(step.getName())).append(' ')
					.append(nullSafe(step.getTechnology())).append(' ')
					.append(nullSafe(step.getCategory()));
		}
		return MotionEmotionWords.hits(text.toString(), emotion);
	}

	// ------------------------------------------------------------------ 参数落地

	/** 一步的参数：默认值 → 模型建议 → 预算调整 → 区间夹紧 */
	MotionDesignVO.PlanStep toPlanStep(int order, RecipeStepVO step, String budget,
			Map<String, Double> overrides, List<String> notes) {
		List<MotionDesignVO.Param> params = new ArrayList<>();
		List<String> adjustments = new ArrayList<>();
		for (TemplateParamVO definition : step.getParams()) {
			Double base = definition.getDefaultValue();
			Double value = base;
			String reason = null;

			if (overrides != null && overrides.containsKey(definition.getKey())) {
				Double suggested = clamp(overrides.get(definition.getKey()), definition);
				if (!equalsDouble(suggested, base)) {
					reason = "模型建议：" + format(base) + " → " + format(suggested);
				}
				value = suggested;
			}
			Double tuned = tuneForBudget(value, definition, budget);
			if (!equalsDouble(tuned, value)) {
				String tuneReason = "轻量预算：" + format(value) + " → " + format(tuned);
				reason = reason == null ? tuneReason : reason + "；" + tuneReason;
				value = tuned;
			}
			params.add(MotionDesignVO.Param.builder()
					.key(definition.getKey())
					.label(definition.getLabel())
					.unit(definition.getUnit())
					.defaultValue(base)
					.value(value)
					.adjusted(!equalsDouble(value, base))
					.reason(reason)
					.build());
			if (reason != null) {
				adjustments.add(definition.getLabel() + "（" + reason + "）");
			}
		}
		if (!adjustments.isEmpty()) {
			notes.add("第 " + order + " 步「" + step.getName() + "」：" + String.join("；", adjustments));
		}
		return MotionDesignVO.PlanStep.builder()
				.order(order)
				.templateKey(step.getTemplateKey())
				.name(step.getName())
				.stage(step.getStage())
				.role(step.getRole())
				.params(params)
				.technology(step.getTechnology())
				.triggerLabel(step.getTriggerLabel())
				.runtimeTier(step.getRuntimeTier())
				.runtimeTierLabel(step.getRuntimeTierLabel())
				.performanceGrade(step.getPerformanceGrade())
				.build();
	}

	/** 轻量预算：数量类参数减半、时长收到 1.2s 以内；其余预算不动 */
	Double tuneForBudget(Double value, TemplateParamVO definition, String budget) {
		if (!"LOW".equals(budget) || value == null) {
			return value;
		}
		String key = definition.getKey() == null ? "" : definition.getKey().toLowerCase(Locale.ROOT);
		if (isCountParam(key)) {
			return clamp(value * 0.5, definition);
		}
		// 时长只在「参数自己允许收短」时收短：背景循环这类参数的下限常是 6 秒以上，
		// 把 18 秒的循环周期压到 6 秒只会让动画变快，并不会更省。
		if (key.contains("duration") && value > 1.2
				&& definition.getMin() != null && definition.getMin() <= 1.2) {
			return clamp(1.2, definition);
		}
		if (key.contains("blur") && value > 8) {
			return clamp(value * 0.6, definition);
		}
		return value;
	}

	/** 这个参数是不是「数量类」：越多越重的才减半 */
	boolean isCountParam(String key) {
		if (COUNT_HINTS_CN.stream().anyMatch(key::contains)) {
			return true;
		}
		for (String token : key.split("[-_]")) {
			if (COUNT_HINTS.contains(token)) {
				return true;
			}
		}
		return false;
	}

	/** 夹到参数自己声明的区间里：模型给的值只能在这个区间内生效 */
	Double clamp(Double value, TemplateParamVO definition) {
		if (value == null) {
			return null;
		}
		double result = value;
		if (definition.getMin() != null) {
			result = Math.max(result, definition.getMin());
		}
		if (definition.getMax() != null) {
			result = Math.min(result, definition.getMax());
		}
		return result;
	}

	// ------------------------------------------------------------------ 模型输出读取

	/** 模型给的参数覆盖：只认「这套方案里真的有这一步 + 参数名在模板里存在」的项 */
	ParamPlan readModelParams(JsonNode node, MotionRecipeVO recipe) {
		Map<String, Map<String, Double>> values = new LinkedHashMap<>();
		List<String> notes = new ArrayList<>();
		int dropped = 0;
		if (node == null || !node.isObject()) {
			return new ParamPlan(values, notes);
		}
		Map<String, RecipeStepVO> byKey = new LinkedHashMap<>();
		recipe.getSteps().forEach(step -> byKey.put(step.getTemplateKey(), step));
		var fields = node.fields();
		while (fields.hasNext()) {
			var entry = fields.next();
			RecipeStepVO step = byKey.get(entry.getKey());
			if (step == null || !entry.getValue().isObject()) {
				dropped++;
				continue;
			}
			Map<String, TemplateParamVO> definitions = new LinkedHashMap<>();
			step.getParams().forEach(param -> definitions.put(param.getKey(), param));
			Map<String, Double> accepted = new LinkedHashMap<>();
			var params = entry.getValue().fields();
			while (params.hasNext()) {
				var paramEntry = params.next();
				TemplateParamVO definition = definitions.get(paramEntry.getKey());
				if (definition == null || !paramEntry.getValue().isNumber()) {
					dropped++;
					continue;
				}
				accepted.put(paramEntry.getKey(), paramEntry.getValue().doubleValue());
			}
			if (!accepted.isEmpty()) {
				values.put(entry.getKey(), accepted);
			}
		}
		if (dropped > 0) {
			notes.add("模型给了 " + dropped + " 处不存在或不可用的参数，已忽略（只接受方案里步骤自己的参数名）。");
		}
		return new ParamPlan(values, notes);
	}

	/** 模型对场景 / 风格 / 说明的判断：只在规则没解析出来时用来补空，不覆盖规则已识别的结果 */
	void applyModelAxes(MotionIntent intent, JsonNode raw, List<String> notes) {
		String scene = sceneOf(textOrNull(raw.path("scene")));
		String style = styleOf(textOrNull(raw.path("style")));
		if (intent.getScene() == null && scene != null) {
			intent.setScene(scene);
			intent.getLabels().put("scene", templateService.sceneLabel(scene));
			notes.add("场景由模型补充判断为「" + templateService.sceneLabel(scene) + "」。");
		}
		if (intent.getStyle() == null && style != null) {
			intent.setStyle(style);
			intent.getLabels().put("style", templateService.styleLabel(style));
			notes.add("风格由模型补充判断为「" + templateService.styleLabel(style) + "」。");
		}
	}

	String sceneOf(String raw) {
		return List.of("Landing Page", "Dashboard", "Portfolio", "Login", "AI SaaS", "Game UI").contains(raw) ? raw : null;
	}

	String styleOf(String raw) {
		return List.of("Minimal", "Luxury", "Cyber", "Glass", "Organic").contains(raw) ? raw : null;
	}

	/** 给模型的清单：只给方案层的选择依据，不给模板代码 */
	String catalog(List<MotionRecipeVO> recipes) {
		StringBuilder builder = new StringBuilder();
		for (MotionRecipeVO recipe : recipes) {
			builder.append("- recipeKey=").append(recipe.getRecipeKey())
					.append(" | ").append(recipe.getName())
					.append(" | 场景=").append(recipe.getScene())
					.append(" | 风格=").append(recipe.getStyle())
					.append(" | 推荐指数=").append(recipe.getScore())
					.append(" | 性能=").append(recipe.getPerformance() == null ? "" : recipe.getPerformance().getGrade())
					.append(" | 步骤=");
			for (RecipeStepVO step : recipe.getSteps()) {
				builder.append(step.getOrder()).append('.').append(step.getName())
						.append('(').append(step.getStage()).append(')');
				if (!step.getParams().isEmpty()) {
					builder.append('[');
					for (int index = 0; index < step.getParams().size(); index++) {
						TemplateParamVO param = step.getParams().get(index);
						if (index > 0) {
							builder.append(',');
						}
						builder.append(param.getKey()).append('=')
								.append(format(param.getDefaultValue())).append('（')
								.append(format(param.getMin())).append('~').append(format(param.getMax()))
								.append(param.getUnit() == null ? "" : param.getUnit()).append('）');
					}
					builder.append(']');
				}
				builder.append(' ');
			}
			builder.append('\n');
		}
		return builder.toString();
	}

	// ------------------------------------------------------------------ 其它

	private String explain(MotionIntent intent, MotionRecipeVO recipe, List<MotionDesignVO.PlanStep> animations) {
		String scene = intent.getLabels().getOrDefault("scene", "通用页面");
		String style = intent.getLabels().getOrDefault("style", "不限风格");
		String first = animations.isEmpty() ? "" : animations.get(0).getStage();
		String last = animations.isEmpty() ? "" : animations.get(animations.size() - 1).getStage();
		String grade = recipe.getPerformance() == null ? "" : "，整体性能 " + recipe.getPerformance().getGrade() + " 级";
		return "按「" + scene + " / " + style + "」选了「" + recipe.getName() + "」这套 "
				+ animations.size() + " 步组合：从" + (first.isEmpty() ? "背景" : first) + "铺到"
				+ (last.isEmpty() ? "收尾" : last) + grade + "。";
	}

	String normalizeBudget(String raw) {
		if (raw == null) {
			return null;
		}
		String value = raw.trim().toUpperCase(Locale.ROOT);
		return PERFORMANCE_TIER.containsKey(value) ? value : null;
	}

	private String textOrNull(JsonNode node) {
		if (node == null || !node.isTextual()) {
			return null;
		}
		String value = node.asText().trim();
		if (value.isEmpty() || "null".equalsIgnoreCase(value)) {
			return null;
		}
		return value.length() > 240 ? value.substring(0, 240) : value;
	}

	String format(Double value) {
		if (value == null) {
			return "-";
		}
		if (Math.abs(value - Math.rint(value)) < 0.0001) {
			return String.valueOf((long) Math.rint(value));
		}
		return String.valueOf(Math.round(value * 100) / 100.0);
	}

	private boolean equalsDouble(Double left, Double right) {
		if (left == null || right == null) {
			return left == right;
		}
		return Math.abs(left - right) < 0.0001;
	}

	private String nullSafe(String value) {
		return value == null ? "" : value;
	}

	/** 模型参数覆盖的结果：接受的覆盖值 + 丢弃说明 */
	record ParamPlan(Map<String, Map<String, Double>> values, List<String> notes) {
	}
}
