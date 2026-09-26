package com.kingdomstudio.modules.motion.template.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.motion.template.entity.MotionRecipe;
import com.kingdomstudio.modules.motion.template.entity.MotionRating;
import com.kingdomstudio.modules.motion.template.entity.MotionTemplate;
import com.kingdomstudio.modules.motion.template.mapper.MotionRecipeMapper;
import com.kingdomstudio.modules.motion.template.mapper.MotionTemplateMapper;
import com.kingdomstudio.modules.motion.template.vo.MotionRecipeVO;
import com.kingdomstudio.modules.motion.template.vo.MotionTemplateItemVO;
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
		List<String> memberKeys = templateService.parseKeys(recipe.getTemplateKeys());
		Map<String, MotionTemplate> byKey = new LinkedHashMap<>();
		if (!memberKeys.isEmpty()) {
			List<MotionTemplate> members = templateMapper.selectList(new LambdaQueryWrapper<MotionTemplate>()
					.in(MotionTemplate::getTemplateKey, memberKeys));
			members.forEach(template -> byKey.put(template.getTemplateKey(), template));
		}

		List<MotionTemplateItemVO> members = new ArrayList<>();
		// 按方案里声明的顺序排列，而不是按库里的顺序：叠加顺序本身就是方案的一部分
		for (String key : memberKeys) {
			MotionTemplate template = byKey.get(key);
			if (template != null) {
				members.add(templateService.toItem(template));
			} else {
				log.warn("组合方案 {} 引用了不存在的模板 {}", recipe.getRecipeKey(), key);
			}
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
				.prompt(recipe.getPrompt())
				.manualScore(rating == null ? null : rating.getScore())
				.manualReason(rating == null ? null : rating.getReason())
				.build();
	}
}
