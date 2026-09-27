package com.kingdomstudio.modules.motion.template.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.motion.template.entity.MotionRecipe;
import com.kingdomstudio.modules.motion.template.entity.MotionRating;
import com.kingdomstudio.modules.motion.template.entity.MotionTemplate;
import com.kingdomstudio.modules.motion.template.mapper.MotionRecipeMapper;
import com.kingdomstudio.modules.motion.template.mapper.MotionTemplateMapper;
import com.kingdomstudio.modules.motion.template.vo.MotionRecipeVO;
import com.kingdomstudio.modules.motion.template.vo.MotionTemplateItemVO;
import com.kingdomstudio.modules.motion.template.vo.RecipePerformanceVO;
import com.kingdomstudio.modules.motion.template.vo.RecipeStepVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 动效组合方案（Motion Recipe）。
 *
 * <p>为什么要有这一层：单个动效是素材，真实网页里的高级感几乎都来自「几个动效按节奏叠在一起」。
 * 例如 Hero Entrance = 渐变背景 + 粒子 + 文字揭示 + 按钮，四个都很普通，合起来才有质感。
 *
 * <p>组合方案的推荐指数不单独维护：它是成员模板推荐指数的**均值**，由服务端实时算出来。
 * 成员调整了子分，方案分数立刻跟着变，不会出现「方案写着 92、成员平均只有 85」这种对不上的情况。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotionRecipeService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final MotionRecipeMapper recipeMapper;
	private final MotionTemplateMapper templateMapper;
	private final MotionTemplateService templateService;

	public List<MotionRecipeVO> list(String scene) {
		LambdaQueryWrapper<MotionRecipe> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(scene != null && !scene.isBlank(), MotionRecipe::getScene, scene)
				.orderByDesc(MotionRecipe::getScore)
				.orderByAsc(MotionRecipe::getId);
		List<MotionRecipe> recipes = recipeMapper.selectList(wrapper);
		List<MotionRecipeVO> result = new ArrayList<>(recipes.size());
		for (MotionRecipe recipe : recipes) {
			result.add(toVO(recipe));
		}
		return result;
	}

	public MotionRecipeVO detail(String recipeKey) {
		MotionRecipe recipe = recipeMapper.selectOne(new LambdaQueryWrapper<MotionRecipe>()
				.eq(MotionRecipe::getRecipeKey, recipeKey));
		if (recipe == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "组合方案不存在（key=" + recipeKey + "）");
		}
		return toVO(recipe);
	}

	MotionRecipeVO toVO(MotionRecipe recipe) {
		List<StepSpec> specs = stepSpecs(recipe);
		List<String> declaredKeys = specs.stream().map(StepSpec::templateKey).toList();

		Map<String, MotionTemplate> byKey = new LinkedHashMap<>();
		if (!declaredKeys.isEmpty()) {
			List<MotionTemplate> members = templateMapper.selectList(new LambdaQueryWrapper<MotionTemplate>()
					.in(MotionTemplate::getTemplateKey, declaredKeys));
			members.forEach(template -> byKey.put(template.getTemplateKey(), template));
		}

		List<MotionTemplateItemVO> members = new ArrayList<>();
		List<RecipeStepVO> steps = new ArrayList<>();
		List<String> memberKeys = new ArrayList<>();
		// 按方案里声明的顺序排列，而不是按库里的顺序：叠加顺序本身就是方案的一部分
		for (StepSpec spec : specs) {
			MotionTemplate template = byKey.get(spec.templateKey());
			if (template == null) {
				log.warn("组合方案 {} 引用了不存在的模板 {}", recipe.getRecipeKey(), spec.templateKey());
				continue;
			}
			members.add(templateService.toItem(template));
			steps.add(toStep(steps.size() + 1, spec, template));
			// memberKeys 只放真的有模板的：它要与 members、steps 一一对应，
			// 否则界面会显示「5 个动效」却只列出 4 步。找不到的成员已在上面记了日志。
			memberKeys.add(template.getTemplateKey());
		}
		int score = members.isEmpty()
				? (recipe.getScore() == null ? 0 : recipe.getScore())
				: (int) Math.round(members.stream().mapToInt(MotionTemplateItemVO::getScore).average().orElse(0));
		MotionRating rating = templateService.findRating("RECIPE", recipe.getRecipeKey());

		return MotionRecipeVO.builder()
				.id(recipe.getId())
				.recipeKey(recipe.getRecipeKey())
				.name(recipe.getName())
				.description(recipe.getDescription())
				.scene(recipe.getScene())
				.sceneLabel(templateService.sceneLabel(recipe.getScene()))
				.style(recipe.getStyle())
				.styleLabel(templateService.styleLabel(recipe.getStyle()))
				.bestFor(templateService.split(recipe.getBestFor()))
				.score(score)
				.stars(templateService.stars(score))
				.grade(templateService.grade(score))
				.members(members)
				.memberKeys(memberKeys)
				.steps(steps)
				.performance(performance(steps))
				.prompt(recipe.getPrompt())
				.manualScore(rating == null ? null : rating.getScore())
				.manualReason(rating == null ? null : rating.getReason())
				.build();
	}

	/** 一步的完整装配：动画是什么、负责什么、有什么可调、代价多大 */
	private RecipeStepVO toStep(int order, StepSpec spec, MotionTemplate template) {
		String tier = templateService.runtimeTier(template);
		int score = templateService.recommendScore(template);
		return RecipeStepVO.builder()
				.order(order)
				.templateKey(template.getTemplateKey())
				.name(template.getName())
				.nameEn(template.getNameEn())
				.stage(spec.stage())
				.role(spec.role())
				.description(template.getDescription())
				.category(template.getCategory())
				.technology(template.getTechnology())
				.triggerType(template.getTriggerType())
				.triggerLabel(templateService.triggerLabel(template.getTriggerType()))
				.runtimeTier(tier)
				.runtimeTierLabel(templateService.runtimeTierLabel(tier))
				.performanceGrade(templateService.performanceGrade(tier, template.getScorePerf()))
				.score(score)
				.stars(templateService.stars(score))
				.params(templateService.parseParams(template.getParams()))
				.previewReady(template.getPreviewHtml() != null && !template.getPreviewHtml().isBlank())
				.build();
	}

	/**
	 * 整体性能成本：最重的档位决定等级，三档各占几步摊开。
	 *
	 * <p>不取平均：平均值会把「有一步是 GPU 档」这件事抹掉，而用户真正要判断的是
	 * 「跑到最重那一步会不会卡」。所以最重档位单独给出，降级建议点名到具体某一步。
	 */
	RecipePerformanceVO performance(List<RecipeStepVO> steps) {
		int light = 0, balanced = 0, gpu = 0;
		for (RecipeStepVO step : steps) {
			switch (step.getRuntimeTier() == null ? "" : step.getRuntimeTier()) {
				case "LIGHTWEIGHT" -> light++;
				case "GPU_ENHANCED" -> gpu++;
				default -> balanced++;
			}
		}
		String worstTier = gpu > 0 ? "GPU_ENHANCED" : (balanced > 0 ? "BALANCED" : "LIGHTWEIGHT");
		String grade = steps.stream()
				.map(step -> step.getPerformanceGrade() == null ? "B" : step.getPerformanceGrade())
				.max(String::compareTo)
				.orElse("B");
		return RecipePerformanceVO.builder()
				.worstTier(worstTier)
				.worstTierLabel(templateService.runtimeTierLabel(worstTier))
				.grade(grade)
				.lightweight(light)
				.balanced(balanced)
				.gpuEnhanced(gpu)
				.note(performanceNote(light, balanced, gpu, steps))
				.build();
	}

	private String performanceNote(int light, int balanced, int gpu, List<RecipeStepVO> steps) {
		String head = light + " 步轻量 + " + balanced + " 步均衡 + " + gpu + " 步依赖 GPU 加速：";
		if (gpu == 0 && balanced == 0) {
			return head + "整套都是纯合成属性动画（transform / opacity），可放心整页使用。";
		}
		if (gpu == 0) {
			return head + "桌面端无压力；低端移动端建议减少同时播放的元素数量。";
		}
		String names = steps.stream()
				.filter(step -> "GPU_ENHANCED".equals(step.getRuntimeTier()))
				.map(RecipeStepVO::getName)
				.limit(2)
				.collect(java.util.stream.Collectors.joining("、"));
		return head + "最重的一步是「" + names + "」，建议在桌面设备上使用；"
				+ "移动端可以把它换成同场景的轻量模板，其余步骤照旧。";
	}

	/** 步骤声明：steps JSON 优先；为空时回退到 template_keys（老数据只有成员清单） */
	List<StepSpec> stepSpecs(MotionRecipe recipe) {
		List<StepSpec> declared = new ArrayList<>();
		for (Map<String, Object> raw : parseSteps(recipe.getSteps())) {
			Object key = raw.get("templateKey");
			if (key == null || String.valueOf(key).isBlank()) {
				continue;
			}
			declared.add(new StepSpec(String.valueOf(key),
					raw.get("stage") == null ? "" : String.valueOf(raw.get("stage")),
					raw.get("role") == null ? "" : String.valueOf(raw.get("role"))));
		}
		if (!declared.isEmpty()) {
			return declared;
		}
		List<StepSpec> fallback = new ArrayList<>();
		for (String key : templateService.parseKeys(recipe.getTemplateKeys())) {
			fallback.add(new StepSpec(key, "", ""));
		}
		return fallback;
	}

	List<Map<String, Object>> parseSteps(String json) {
		if (json == null || json.isBlank()) {
			return List.of();
		}
		try {
			return OBJECT_MAPPER.readValue(json, new TypeReference<>() {
			});
		} catch (Exception e) {
			log.warn("组合方案步骤解析失败，按成员清单处理：{}", e.getMessage());
			return List.of();
		}
	}

	/** 一步的声明（还没和模板对上的原始数据） */
	record StepSpec(String templateKey, String stage, String role) {
	}
}
