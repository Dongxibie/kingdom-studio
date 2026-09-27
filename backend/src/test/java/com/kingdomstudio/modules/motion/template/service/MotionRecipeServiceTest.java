package com.kingdomstudio.modules.motion.template.service;

import com.kingdomstudio.modules.motion.template.entity.MotionRecipe;
import com.kingdomstudio.modules.motion.template.entity.MotionTemplate;
import com.kingdomstudio.modules.motion.template.mapper.MotionRecipeMapper;
import com.kingdomstudio.modules.motion.template.mapper.MotionRatingMapper;
import com.kingdomstudio.modules.motion.template.mapper.MotionTemplateMapper;
import com.kingdomstudio.modules.motion.template.vo.MotionRecipeVO;
import com.kingdomstudio.modules.motion.template.vo.RecipeStepVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 组合方案（Motion Recipe 2.0）的单元测试。
 *
 * <p>这一版的重点是「步骤」：一套方案不只是成员清单，而是有顺序、有分工的一串步骤。
 * 测试钉住三件事 —— 顺序按方案声明（不是库里的顺序）、不存在的成员被跳过、
 * 整体性能成本取最重的那一档（不被平均值抹掉）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MotionRecipeServiceTest {

	@Mock
	private MotionRecipeMapper recipeMapper;
	@Mock
	private MotionTemplateMapper templateMapper;
	@Mock
	private MotionRatingMapper ratingMapper;

	private MotionRecipeService recipeService;

	@BeforeEach
	void setUp() {
		MotionTemplateService templateService = new MotionTemplateService(templateMapper, recipeMapper, ratingMapper);
		recipeService = new MotionRecipeService(recipeMapper, templateMapper, templateService);
	}

	private MotionTemplate template(String key, String name, String scene, String style, String technology, int perf) {
		MotionTemplate template = new MotionTemplate();
		template.setId((long) Math.abs(key.hashCode() % 1000));
		template.setTemplateKey(key);
		template.setName(name);
		template.setNameEn(key);
		template.setDescription(name + " 的说明");
		template.setCategory("首屏动画");
		template.setScene(scene);
		template.setStyle(style);
		template.setTechnology(technology);
		template.setTriggerType("load");
		template.setDifficulty(1);
		template.setBestFor("官网首页");
		template.setTags("");
		template.setScoreVisual(90);
		template.setScoreCode(90);
		template.setScoreReuse(90);
		template.setScorePerf(perf);
		template.setScore(1);
		template.setParams("[{\"key\":\"--m-duration\",\"label\":\"时长\",\"unit\":\"s\",\"min\":0.3,\"max\":3,\"step\":0.1,\"default\":1.2}]");
		template.setPreviewHtml("<div class=\"motion-root\"></div>");
		return template;
	}

	private MotionRecipe recipe(String key, String stepsJson, String templateKeys) {
		MotionRecipe recipe = new MotionRecipe();
		recipe.setId(1L);
		recipe.setRecipeKey(key);
		recipe.setName("测试方案");
		recipe.setDescription("一句话说明");
		recipe.setScene("Landing Page");
		recipe.setStyle("Minimal");
		recipe.setBestFor("官网首页");
		recipe.setScore(1);
		recipe.setSteps(stepsJson);
		recipe.setTemplateKeys(templateKeys);
		recipe.setPrompt("做一个测试页面");
		return recipe;
	}

	@Test
	@DisplayName("步骤按方案声明的顺序装配，带作用与所在层级，并给出可调参数")
	void shouldBuildStepsInDeclaredOrder() {
		when(recipeMapper.selectOne(any())).thenReturn(recipe("apple-product-page",
				"[{\"templateKey\":\"smooth-fade\",\"stage\":\"背景\",\"role\":\"整页容器先柔和淡入\"},"
						+ "{\"templateKey\":\"slide-up\",\"stage\":\"内容\",\"role\":\"主标题上滑出现\"}]",
				"[\"slide-up\",\"smooth-fade\"]"));
		when(templateMapper.selectList(any())).thenReturn(List.of(
				template("slide-up", "向上滑入", "Landing Page", "Minimal", "CSS", 98),
				template("smooth-fade", "柔和淡入", "Landing Page", "Minimal", "CSS", 98)));

		MotionRecipeVO vo = recipeService.detail("apple-product-page");

		assertEquals(2, vo.getSteps().size());
		RecipeStepVO first = vo.getSteps().get(0);
		assertEquals(1, first.getOrder());
		assertEquals("smooth-fade", first.getTemplateKey(), "顺序按方案声明，而不是按成员清单或库里的顺序");
		assertEquals("背景", first.getStage());
		assertEquals("整页容器先柔和淡入", first.getRole());
		assertEquals("向上滑入", vo.getSteps().get(1).getName());
		assertEquals(2, vo.getSteps().get(1).getOrder());

		assertEquals(1, first.getParams().size());
		assertEquals("--m-duration", first.getParams().get(0).getKey());
		assertEquals("加载时", first.getTriggerLabel());
		assertEquals("轻量", first.getRuntimeTierLabel());
		assertEquals("A", first.getPerformanceGrade());
		assertTrue(first.getPreviewReady());
	}

	@Test
	@DisplayName("成员引用了不存在的模板：跳过这一步，其余照常，编号不留空")
	void shouldSkipUnknownStep() {
		when(recipeMapper.selectOne(any())).thenReturn(recipe("broken",
				"[{\"templateKey\":\"smooth-fade\",\"stage\":\"背景\",\"role\":\"淡入\"},"
						+ "{\"templateKey\":\"not-exists\",\"stage\":\"内容\",\"role\":\"不存在\"},"
						+ "{\"templateKey\":\"slide-up\",\"stage\":\"内容\",\"role\":\"上滑\"}]",
				"[\"smooth-fade\",\"not-exists\",\"slide-up\"]"));
		when(templateMapper.selectList(any())).thenReturn(List.of(
				template("smooth-fade", "柔和淡入", "Landing Page", "Minimal", "CSS", 98),
				template("slide-up", "向上滑入", "Landing Page", "Minimal", "CSS", 98)));

		MotionRecipeVO vo = recipeService.detail("broken");

		assertEquals(2, vo.getSteps().size());
		assertEquals(List.of("smooth-fade", "slide-up"), vo.getMemberKeys(), "memberKeys 与 steps 一一对应，不含找不到的成员");
		assertEquals(1, vo.getSteps().get(0).getOrder());
		assertEquals(2, vo.getSteps().get(1).getOrder(), "跳过一步之后编号仍然连续");
		assertEquals("slide-up", vo.getSteps().get(1).getTemplateKey());
	}

	@Test
	@DisplayName("老数据没有 steps 时回退到成员清单：仍能列出成员，只是没有作用说明")
	void shouldFallBackToTemplateKeys() {
		when(recipeMapper.selectOne(any())).thenReturn(recipe("legacy", null, "[\"smooth-fade\",\"slide-up\"]"));
		when(templateMapper.selectList(any())).thenReturn(List.of(
				template("smooth-fade", "柔和淡入", "Landing Page", "Minimal", "CSS", 98),
				template("slide-up", "向上滑入", "Landing Page", "Minimal", "CSS", 98)));

		MotionRecipeVO vo = recipeService.detail("legacy");

		assertEquals(2, vo.getSteps().size());
		assertEquals("smooth-fade", vo.getSteps().get(0).getTemplateKey());
		assertEquals("", vo.getSteps().get(0).getRole());
		assertEquals("", vo.getSteps().get(0).getStage());
	}

	@Test
	@DisplayName("steps 是坏 JSON：不炸，按成员清单处理")
	void shouldSurviveBrokenStepsJson() {
		when(recipeMapper.selectOne(any())).thenReturn(recipe("broken-json", "[不是 JSON", "[\"smooth-fade\"]"));
		when(templateMapper.selectList(any())).thenReturn(List.of(
				template("smooth-fade", "柔和淡入", "Landing Page", "Minimal", "CSS", 98)));

		MotionRecipeVO vo = recipeService.detail("broken-json");

		assertEquals(1, vo.getSteps().size());
		assertTrue(recipeService.parseSteps("[不是 JSON").isEmpty());
		assertTrue(recipeService.parseSteps(null).isEmpty());
	}

	@Test
	@DisplayName("整体性能成本：最重的一档说了算，各档步数摊开，降级建议点名到具体那一步")
	void shouldSummarizePerformanceByWorstStep() {
		when(recipeMapper.selectOne(any())).thenReturn(recipe("cyber-launch",
				"[{\"templateKey\":\"galaxy-background\",\"stage\":\"背景\",\"role\":\"打底\"},"
						+ "{\"templateKey\":\"starfield-drift-bg\",\"stage\":\"背景\",\"role\":\"漂移\"},"
						+ "{\"templateKey\":\"three-scene\",\"stage\":\"内容\",\"role\":\"三维开场\"}]",
				"[\"galaxy-background\",\"starfield-drift-bg\",\"three-scene\"]"));
		when(templateMapper.selectList(any())).thenReturn(List.of(
				template("galaxy-background", "星系背景", "Landing Page", "Cyber", "CSS", 88),
				template("starfield-drift-bg", "星空漂移", "Game UI", "Cyber", "CSS", 95),
				template("three-scene", "三维场景", "Game UI", "Cyber", "Three.js", 96)));

		MotionRecipeVO vo = recipeService.detail("cyber-launch");

		assertNotNull(vo.getPerformance());
		assertEquals("GPU_ENHANCED", vo.getPerformance().getWorstTier());
		assertEquals("依赖 GPU 加速", vo.getPerformance().getWorstTierLabel());
		assertEquals("C", vo.getPerformance().getGrade());
		assertEquals(1, vo.getPerformance().getGpuEnhanced());
		assertEquals(1, vo.getPerformance().getLightweight(), "星空漂移性能子分 95 → 轻量");
		assertEquals(1, vo.getPerformance().getBalanced(), "星系背景性能子分 88 → 均衡");
		assertTrue(vo.getPerformance().getNote().contains("三维场景"), vo.getPerformance().getNote());
	}

	@Test
	@DisplayName("整套都轻量：性能等级是 A，说明里明确「可放心整页使用」")
	void shouldReportAllLightweightRecipe() {
		when(recipeMapper.selectOne(any())).thenReturn(recipe("startup-one-pager",
				"[{\"templateKey\":\"smooth-fade\",\"stage\":\"背景\",\"role\":\"淡入\"},"
						+ "{\"templateKey\":\"slide-up\",\"stage\":\"内容\",\"role\":\"上滑\"}]",
				"[\"smooth-fade\",\"slide-up\"]"));
		when(templateMapper.selectList(any())).thenReturn(List.of(
				template("smooth-fade", "柔和淡入", "Landing Page", "Minimal", "CSS", 98),
				template("slide-up", "向上滑入", "Landing Page", "Minimal", "CSS", 98)));

		MotionRecipeVO vo = recipeService.detail("startup-one-pager");

		assertEquals("LIGHTWEIGHT", vo.getPerformance().getWorstTier());
		assertEquals("A", vo.getPerformance().getGrade());
		assertEquals(2, vo.getPerformance().getLightweight());
		assertEquals(0, vo.getPerformance().getGpuEnhanced());
		assertTrue(vo.getPerformance().getNote().contains("可放心整页使用"));
	}

	@Test
	@DisplayName("方案分数 = 成员推荐指数的均值（服务端重算，不读库里缓存值）")
	void shouldAverageMemberScores() {
		when(recipeMapper.selectOne(any())).thenReturn(recipe("avg", "[\"a\",\"b\"]", "[\"a\",\"b\"]"));
		MotionTemplate high = template("a", "甲", "Landing Page", "Minimal", "CSS", 100);
		high.setScoreVisual(100);
		high.setScoreCode(100);
		high.setScoreReuse(100);
		MotionTemplate low = template("b", "乙", "Landing Page", "Minimal", "CSS", 90);
		low.setScoreVisual(60);
		low.setScoreCode(60);
		low.setScoreReuse(60);
		when(templateMapper.selectList(any())).thenReturn(List.of(low, high));

		MotionRecipeVO vo = recipeService.detail("avg");

		assertEquals(83, vo.getScore(), "甲 100 分、乙 66 分（视觉/代码/复用 60 + 性能 90），均值 83");
		assertEquals("A", vo.getGrade());
		assertEquals(1, vo.getPerformance().getLightweight());
		assertEquals(1, vo.getPerformance().getBalanced(), "steps 缺省时按成员清单算性能，两个成员都算上");
	}

	@Test
	@DisplayName("列表：按方案数量返回，每条都带步骤与性能成本")
	void shouldListRecipesWithSteps() {
		MotionRecipe first = recipe("p1", "[\"a\"]", "[\"a\"]");
		first.setId(1L);
		MotionRecipe second = recipe("p2", "[\"b\"]", "[\"b\"]");
		second.setId(2L);
		when(recipeMapper.selectList(any())).thenReturn(List.of(first, second));
		when(templateMapper.selectList(any())).thenReturn(List.of(
				template("a", "甲", "Landing Page", "Minimal", "CSS", 98),
				template("b", "乙", "Landing Page", "Cyber", "Canvas", 88)));

		List<MotionRecipeVO> list = recipeService.list(null);

		assertEquals(2, list.size());
		assertEquals(1, list.get(0).getSteps().size());
		assertFalse(list.get(0).getSteps().isEmpty());
		assertEquals("A", list.get(0).getPerformance().getGrade());
		assertEquals("C", list.get(1).getPerformance().getGrade());
	}

	@Test
	@DisplayName("方案不存在：明确报 404")
	void shouldRejectUnknownRecipe() {
		when(recipeMapper.selectOne(any())).thenReturn(null);

		var error = assertThrows(RuntimeException.class, () -> recipeService.detail("nope"));
		assertTrue(error.getMessage().contains("组合方案不存在"), error.getMessage());
	}
}
