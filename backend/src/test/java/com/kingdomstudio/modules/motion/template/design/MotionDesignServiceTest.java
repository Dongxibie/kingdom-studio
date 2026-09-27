package com.kingdomstudio.modules.motion.template.design;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * AI Motion Designer 的单元测试。
 *
 * <p>这一层的纪律是「模型只能选与调，不能造」：不存在的方案 key 被忽略、越界参数被夹紧、
 * 编造的参数名被丢弃；没有模型时走完全相同的输出结构，而且**同一句输入结果稳定**。
 */
class MotionDesignServiceTest {

	private MotionDesignService service;

	@BeforeEach
	void setUp() {
		// 模型未配置（三项配置都为空）→ design() 直接返回 null，走规则路径
		MotionModelClient modelClient = new MotionModelClient(mock(RestClient.Builder.class));
		service = new MotionDesignService(mock(MotionTemplateService.class), mock(MotionRecipeService.class),
				new MotionIntentParser(), modelClient);
	}

	private TemplateParamVO param(String key, String label, String unit, double min, double max, double def) {
		return TemplateParamVO.builder().key(key).label(label).unit(unit).min(min).max(max).defaultValue(def).build();
	}

	private RecipeStepVO step(int order, String key, String name, String stage, String role,
			String tier, String grade, List<TemplateParamVO> params) {
		return RecipeStepVO.builder()
				.order(order).templateKey(key).name(name).stage(stage).role(role)
				.technology("CSS").triggerLabel("加载时")
				.runtimeTier(tier).runtimeTierLabel("GPU_ENHANCED".equals(tier) ? "依赖 GPU 加速" : "轻量")
				.performanceGrade(grade).score(90).stars(4.5).params(params).previewReady(true)
				.build();
	}

	private MotionRecipeVO recipe(String key, String name, String scene, String style, String grade,
			String worstTier, List<RecipeStepVO> steps) {
		return MotionRecipeVO.builder()
				.recipeKey(key).name(name).description(name + " 的说明")
				.scene(scene).sceneLabel("网站首页").style(style).styleLabel("赛博")
				.bestFor(List.of("官网首页")).score(90).stars(4.5).grade("S")
				.steps(steps).memberKeys(steps.stream().map(RecipeStepVO::getTemplateKey).toList())
				.members(List.of())
				.performance(RecipePerformanceVO.builder()
						.worstTier(worstTier).worstTierLabel("依赖 GPU 加速".equals(worstTier) ? "依赖 GPU 加速" : "轻量")
						.grade(grade).lightweight(2).balanced(0).gpuEnhanced(1)
						.note("2 步轻量 + 0 步均衡 + 1 步依赖 GPU 加速：桌面端无压力。")
						.build())
				.build();
	}

	private void givenRecipes(MotionRecipeVO... recipes) {
		MotionRecipeService recipeService = mock(MotionRecipeService.class);
		org.mockito.Mockito.when(recipeService.list(null)).thenReturn(List.of(recipes));
		MotionModelClient modelClient = new MotionModelClient(mock(RestClient.Builder.class));
		service = new MotionDesignService(mock(MotionTemplateService.class), recipeService,
				new MotionIntentParser(), modelClient);
	}

	@Test
	@DisplayName("规则路径：按场景与风格从组合里挑一套，输出完整方案（动画 / 作用 / 参数 / 说明）")
	void shouldDesignByRules() {
		givenRecipes(
				recipe("cyber-launch", "Cyber Launch", "Landing Page", "Cyber", "B", "BALANCED", List.of(
						step(1, "galaxy-background", "星系背景", "背景", "星系渐变打底", "LIGHTWEIGHT", "A",
								List.of(param("--m-duration", "时长", "s", 0.3, 3, 1.2))),
						step(2, "particle-network", "粒子星网", "背景", "粒子给纵深", "GPU_ENHANCED", "C",
								List.of(param("--m-count", "粒子数", "个", 10, 120, 60))),
						step(3, "magnetic-button", "磁吸按钮", "收尾", "主按钮磁吸", "LIGHTWEIGHT", "A",
								List.of(param("--m-strength", "吸附强度", "", 0.1, 1, 0.35))))),
				recipe("luxury-hotel", "Luxury Hotel", "Landing Page", "Luxury", "B", "BALANCED", List.of(
						step(1, "grain-overlay-bg", "颗粒噪点叠加", "背景", "压住廉价感", "LIGHTWEIGHT", "A",
								List.of(param("--m-duration", "时长", "s", 0.3, 3, 1.2))))));

		MotionDesignVO vo = service.design("帮我设计一个科技公司首页", null, null);

		assertEquals("cyber-launch", vo.getRecipe().getRecipeKey(), "场景 + 风格都命中 Cyber Launch");
		assertEquals("RULE", vo.getSource());
		assertNotNull(vo.getFallbackReason());
		assertTrue(vo.getFallbackReason().contains("未配置模型"));
		assertEquals(3, vo.getAnimations().size());
		assertEquals("星系背景", vo.getAnimations().get(0).getName());
		assertEquals("背景", vo.getAnimations().get(0).getStage());
		assertEquals("星系渐变打底", vo.getAnimations().get(0).getRole());
		assertEquals(1, vo.getAnimations().get(0).getParams().size());
		assertEquals(1.2, vo.getAnimations().get(0).getParams().get(0).getValue());
		assertEquals(false, vo.getAnimations().get(0).getParams().get(0).getAdjusted());
		assertTrue(vo.getExplanation().contains("Cyber Launch"), vo.getExplanation());
		assertNotNull(vo.getPerformance());
	}

	@Test
	@DisplayName("同一句输入两次设计，结果完全一致（规则路径没有随机）")
	void shouldBeStable() {
		givenRecipes(
				recipe("a", "方案甲", "Landing Page", "Cyber", "B", "BALANCED", List.of(
						step(1, "t1", "模板一", "背景", "铺底", "LIGHTWEIGHT", "A", List.of()))),
				recipe("b", "方案乙", "Landing Page", "Minimal", "A", "LIGHTWEIGHT", List.of(
						step(1, "t2", "模板二", "背景", "铺底", "LIGHTWEIGHT", "A", List.of()))));

		MotionDesignVO first = service.design("科技感首页", null, null);
		MotionDesignVO second = service.design("科技感首页", null, null);

		assertEquals(first.getRecipe().getRecipeKey(), second.getRecipe().getRecipeKey());
		assertEquals(first.getExplanation(), second.getExplanation());
		assertEquals(first.getAnimations().size(), second.getAnimations().size());
	}

	@Test
	@DisplayName("说「轻量优先」：数量类参数减半、时长收到 1.2s 以内，并写明原因")
	void shouldTuneForLightBudget() {
		givenRecipes(recipe("cyber-launch", "Cyber Launch", "Landing Page", "Cyber", "C", "GPU_ENHANCED", List.of(
				step(1, "particle-network", "粒子星网", "背景", "粒子给纵深", "GPU_ENHANCED", "C",
						List.of(param("--m-count", "粒子数", "个", 10, 120, 60),
								param("--m-duration", "时长", "s", 0.3, 3, 2.4),
								param("--m-blur", "模糊", "px", 0, 40, 20))))));

		MotionDesignVO vo = service.design("移动端优先，首页要轻量一点", null, null);

		List<MotionDesignVO.Param> params = vo.getAnimations().get(0).getParams();
		assertEquals(30.0, params.get(0).getValue(), "粒子数 60 → 30");
		assertEquals(true, params.get(0).getAdjusted());
		assertTrue(params.get(0).getReason().contains("轻量预算"), params.get(0).getReason());
		assertEquals(1.2, params.get(1).getValue(), "时长 2.4 → 1.2");
		assertEquals(12.0, params.get(2).getValue(), "模糊 20 → 12");
		assertTrue(vo.getNotes().stream().anyMatch(note -> note.contains("第 1 步")), vo.getNotes().toString());
	}

	@Test
	@DisplayName("数量类参数按整段匹配：--m-timeline 是时长，不当成 count 减半")
	void shouldNotTreatTimelineAsCount() {
		assertEquals(true, service.isCountParam("--m-particle-count"));
		assertEquals(true, service.isCountParam("--m-dot-density"));
		assertEquals(true, service.isCountParam("--m-粒子数"));
		assertEquals(false, service.isCountParam("--m-timeline"), "timeline 是时长，不是行数");
		assertEquals(false, service.isCountParam("--m-duration"));
		assertEquals(false, service.isCountParam("--m-outline-offset"), "outline 不是 line");
	}

	@Test
	@DisplayName("时长只在参数允许收短时收短：18 秒的背景循环周期不动")
	void shouldNotShortenLongLoops() {
		TemplateParamVO entrance = param("--m-duration", "入场时长", "s", 0.3, 3, 2.4);
		TemplateParamVO loop = param("--m-duration", "穿越周期", "s", 6, 40, 18);

		assertEquals(1.2, service.tuneForBudget(2.4, entrance, "LOW"), "入场时长收到 1.2s 以内");
		assertEquals(18.0, service.tuneForBudget(18.0, loop, "LOW"), "下限是 6 秒的循环周期不动它");
		assertEquals(2.4, service.tuneForBudget(2.4, entrance, "HIGH"), "非轻量预算不做任何调整");
	}

	@Test
	@DisplayName("参数夹紧：超出模板区间的建议值被夹到边界，而不是原样落地")
	void shouldClampParams() {
		TemplateParamVO count = param("--m-count", "粒子数", "个", 10, 120, 60);
		assertEquals(120.0, service.clamp(999.0, count));
		assertEquals(10.0, service.clamp(1.0, count));
		assertEquals(60.0, service.clamp(60.0, count));
		assertNull(service.clamp(null, count));
	}

	@Test
	@DisplayName("预算覆盖：显式指定轻量优先时，即使需求里没提也会按轻量调整")
	void shouldRespectExplicitBudget() {
		MotionIntent intent = new MotionIntentParser().parse("科技感首页");
		assertNull(intent.getPerformance());
		assertEquals("LOW", service.normalizeBudget("low"));
		assertEquals("HIGH", service.normalizeBudget("HIGH"));
		assertNull(service.normalizeBudget("随便"));
	}

	@Test
	@DisplayName("组合选取：场景命中 40 分压过风格命中 25 分")
	void shouldPickRecipeByAxes() {
		// 「极简风格的官网」→ 场景=网站首页、风格=极简、情绪为空（这句话没表达出情绪）
		MotionRecipeVO sceneHit = recipe("z-landing", "首页方案", "Landing Page", "Organic", "B", "BALANCED", List.of());
		MotionRecipeVO styleHit = recipe("a-portfolio", "风格方案", "Portfolio", "Minimal", "B", "BALANCED", List.of());
		givenRecipes(styleHit, sceneHit);

		MotionIntent intent = new MotionIntentParser().parse("极简风格的官网");
		assertEquals("Landing Page", intent.getScene());
		assertEquals("Minimal", intent.getStyle());
		assertNull(intent.getEmotion(), "这句话里没有情绪词，这一轴不参与打分");

		assertEquals(40 * 100 + 90, service.recipeScore(sceneHit, intent, null));
		assertEquals(25 * 100 + 90, service.recipeScore(styleHit, intent, null));

		MotionDesignVO vo = service.design("极简风格的官网", null, null);

		assertEquals("z-landing", vo.getRecipe().getRecipeKey(), "场景命中（40）> 风格命中（25）");
	}

	@Test
	@DisplayName("低预算不会选中依赖 GPU 的方案（同分时被扣 8 分压下去）")
	void shouldPushGpuRecipeDownForLowBudget() {
		MotionRecipeVO gpu = recipe("gpu-one", "重方案", "Landing Page", "Cyber", "C", "GPU_ENHANCED", List.of());
		MotionRecipeVO light = recipe("light-one", "轻方案", "Landing Page", "Cyber", "A", "LIGHTWEIGHT", List.of());
		givenRecipes(light, gpu);

		MotionDesignVO vo = service.design("科技感首页，移动端优先，要轻量", null, null);

		assertEquals("light-one", vo.getRecipe().getRecipeKey());
	}

	@Test
	@DisplayName("模型给的参数：只认这套方案里的模板与模板自己的参数名，其余丢弃并说明")
	void shouldOnlyAcceptValidModelParams() {
		MotionRecipeVO chosen = recipe("cyber-launch", "Cyber Launch", "Landing Page", "Cyber", "B", "BALANCED", List.of(
				step(1, "particle-network", "粒子星网", "背景", "粒子给纵深", "GPU_ENHANCED", "C",
						List.of(param("--m-count", "粒子数", "个", 10, 120, 60)))));

		ObjectMapper mapper = new ObjectMapper();
		MotionDesignService.ParamPlan plan;
		try {
			plan = service.readModelParams(mapper.readTree("""
					{
					  "particle-network": { "--m-count": 999, "--m-fake": 3 },
					  "not-in-recipe": { "--m-count": 20 }
					}
					"""), chosen);
		} catch (Exception e) {
			throw new AssertionError(e);
		}

		assertEquals(1, plan.values().size());
		assertEquals(999.0, plan.values().get("particle-network").get("--m-count"), "夹紧发生在落地那一步");
		assertNull(plan.values().get("particle-network").get("--m-fake"), "编造的参数名被丢弃");
		assertNull(plan.values().get("not-in-recipe"), "不在方案里的模板被丢弃");
		assertTrue(plan.notes().stream().anyMatch(note -> note.contains("2 处")), plan.notes().toString());
	}

	@Test
	@DisplayName("模型的场景 / 风格只在规则没解析出来时补空，不覆盖规则结果")
	void shouldNotOverrideRuleIntent() {
		MotionIntent parsed = new MotionIntentParser().parse("科技感首页");
		MotionIntent filled = new MotionIntentParser().parse("帮我看看这个");
		ObjectMapper mapper = new ObjectMapper();
		try {
			service.applyModelAxes(parsed, mapper.readTree("{\"scene\":\"Dashboard\",\"style\":\"Luxury\"}"), new java.util.ArrayList<>());
			service.applyModelAxes(filled, mapper.readTree("{\"scene\":\"Dashboard\",\"style\":\"Luxury\"}"), new java.util.ArrayList<>());
		} catch (Exception e) {
			throw new AssertionError(e);
		}

		assertEquals("Landing Page", parsed.getScene(), "规则已经认出来的场景不被模型改掉");
		assertEquals("Cyber", parsed.getStyle());
		assertEquals("Dashboard", filled.getScene(), "规则没认出来的场景由模型补上");
		assertEquals("Luxury", filled.getStyle());
	}

	@Test
	@DisplayName("方案数量限制：maxSteps 决定用几步，且不只做「前 N 步」截断以外的改动")
	void shouldRespectMaxSteps() {
		givenRecipes(recipe("cyber-launch", "Cyber Launch", "Landing Page", "Cyber", "B", "BALANCED", List.of(
				step(1, "a", "甲", "背景", "铺底", "LIGHTWEIGHT", "A", List.of()),
				step(2, "b", "乙", "内容", "给内容", "LIGHTWEIGHT", "A", List.of()),
				step(3, "c", "丙", "收尾", "收住", "LIGHTWEIGHT", "A", List.of()))));

		assertEquals(3, service.design("科技感首页", null, null).getAnimations().size());
		assertEquals(2, service.design("科技感首页", null, 2).getAnimations().size());
		assertEquals(1, service.design("科技感首页", null, 0).getAnimations().size(), "下限收敛到 1");
		assertEquals(3, service.design("科技感首页", null, 99).getAnimations().size(), "上限收敛到 8，也不会超过方案步数");
	}

	@Test
	@DisplayName("库里没有组合方案：给一句可执行的提示，而不是抛异常")
	void shouldHandleEmptyRecipePool() {
		givenRecipes();

		MotionDesignVO vo = service.design("科技感首页", null, null);

		assertNull(vo.getRecipe());
		assertTrue(vo.getAnimations().isEmpty());
		assertTrue(vo.getNotes().get(0).contains("extensions_motion_recipe_seed.sql"), vo.getNotes().get(0));
	}

	@Test
	@DisplayName("空需求：不报错，按推荐指数给一套通用方案")
	void shouldHandleBlankQuery() {
		givenRecipes(recipe("generic", "通用方案", "Landing Page", "Minimal", "A", "LIGHTWEIGHT", List.of(
				step(1, "a", "甲", "背景", "铺底", "LIGHTWEIGHT", "A", List.of()))));

		MotionDesignVO vo = service.design("   ", null, null);

		assertEquals("generic", vo.getRecipe().getRecipeKey());
		assertEquals(1, vo.getAnimations().size());
		assertNotNull(vo.getExplanation());
	}
}
