package com.kingdomstudio.modules.motion.template.controller;

import com.kingdomstudio.common.PageVO;
import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.motion.template.design.MotionDesignService;
import com.kingdomstudio.modules.motion.template.dto.MotionDesignDTO;
import com.kingdomstudio.modules.motion.template.dto.MotionRatingDTO;
import com.kingdomstudio.modules.motion.template.dto.MotionSearchDTO;
import com.kingdomstudio.modules.motion.template.dto.TemplateQueryDTO;
import com.kingdomstudio.modules.motion.template.recommend.MotionRecommendationService;
import com.kingdomstudio.modules.motion.template.service.MotionAssistantService;
import com.kingdomstudio.modules.motion.template.service.MotionRecipeService;
import com.kingdomstudio.modules.motion.template.service.MotionTemplateService;
import com.kingdomstudio.modules.motion.template.vo.MotionAssistantVO;
import com.kingdomstudio.modules.motion.template.vo.MotionDesignVO;
import com.kingdomstudio.modules.motion.template.vo.MotionFacetVO;
import com.kingdomstudio.modules.motion.template.vo.MotionRecommendVO;
import com.kingdomstudio.modules.motion.template.vo.MotionRecipeVO;
import com.kingdomstudio.modules.motion.template.vo.MotionTemplateDetailVO;
import com.kingdomstudio.modules.motion.template.vo.MotionTemplateItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Motion Lab 工作台接口（v1.1.0）。
 *
 * <p>与既有的 {@code /motion}（资源 CRUD + 采集）分开：那套面向「资源管理」，
 * 这套面向「开发工作台」——模板、组合方案、发现方式与检索。
 */
@Tag(name = "10.2 动效工作台", description = "官方模板、组合方案、发现方式与可解释检索")
@RestController
@RequestMapping("/motion/templates")
@RequiredArgsConstructor
public class MotionTemplateController {

	private final MotionTemplateService templateService;
	private final MotionRecipeService recipeService;
	private final MotionAssistantService assistantService;
	private final MotionRecommendationService recommendService;
	private final MotionDesignService designService;

	@Operation(summary = "模板分页", description = "分组 / 场景 / 风格 / 技术 / 难度 / 关键词可任意组合")
	@GetMapping
	public Result<PageVO<MotionTemplateItemVO>> page(TemplateQueryDTO query) {
		return Result.success(templateService.page(query));
	}

	@Operation(summary = "发现方式分面", description = "场景 / 风格 / 技术 / 分组 / 难度各自的选项与数量，供左栏使用")
	@GetMapping("/facets")
	public Result<MotionFacetVO> facets() {
		return Result.success(templateService.facets());
	}

	@Operation(summary = "搜索栏检索",
			description = "识别输入里的场景 / 风格 / 技术与动效关键词，返回带命中理由的模板与组合方案；不调用模型，结果可复现")
	@PostMapping("/search")
	public Result<MotionAssistantVO> search(@Valid @RequestBody MotionSearchDTO request) {
		return Result.success(assistantService.search(request));
	}

	@Operation(summary = "Motion Assistant（模型优先，失败回退检索）",
			description = "理解自然语言需求 → 判断风格 → 从现有模板里挑 → 组一套组合方案并给参数建议。"
					+ "不生成代码；未配置模型或模型失败时自动回退到内置检索，并在 source / fallbackReason 里说明")
	@PostMapping("/assist")
	public Result<MotionAssistantVO> assist(@Valid @RequestBody MotionSearchDTO request) {
		return Result.success(assistantService.assist(request));
	}

	@Operation(summary = "智能推荐",
			description = "一句话 → 五轴意图（场景 / 风格 / 情绪 / 性能 / 触发）→ Top N，每条带命中理由与性能等级")
	@PostMapping("/recommend")
	public Result<MotionRecommendVO> recommend(@Valid @RequestBody MotionSearchDTO request) {
		return Result.success(recommendService.recommend(request.getQuery(), request.getLimit()));
	}

	@Operation(summary = "AI 设计方案",
			description = "一句需求 → 完整设计方案：五轴意图 + 选中的组合方案 + 每一步的动画 / 作用 / 参数建议 / 性能成本 + 逐条说明。"
					+ "模型只负责在现有 30 套组合里挑一套、在参数区间内微调、写说明；未配置模型或模型失败时走同一套结构的规则设计，"
					+ "来源与回退原因在 source / fallbackReason 里如实说明")
	@PostMapping("/design")
	public Result<MotionDesignVO> design(@Valid @RequestBody MotionDesignDTO request) {
		return Result.success(designService.design(request.getQuery(), request.getPerformance(), request.getMaxSteps()));
	}

	@Operation(summary = "组合方案列表", description = "按场景筛选；推荐指数为组成模板的加权平均")
	@GetMapping("/recipes")
	public Result<List<MotionRecipeVO>> recipes(@RequestParam(required = false) String scene) {
		return Result.success(recipeService.list(scene));
	}

	@Operation(summary = "组合方案详情")
	@GetMapping("/recipes/{recipeKey}")
	public Result<MotionRecipeVO> recipe(@PathVariable String recipeKey) {
		return Result.success(recipeService.detail(recipeKey));
	}

	@Operation(summary = "模板详情", description = "含预览结构、可调参数与五类代码产物")
	@GetMapping("/{templateKey}")
	public Result<MotionTemplateDetailVO> detail(@PathVariable String templateKey) {
		return Result.success(templateService.detail(templateKey));
	}

	@Operation(summary = "人工评分", description = "对模板或组合方案打分（1-5 星）并记录理由，重复评分覆盖")
	@PostMapping("/ratings")
	public Result<Void> rate(@Valid @RequestBody MotionRatingDTO request) {
		templateService.rate(request);
		return Result.success();
	}
}
