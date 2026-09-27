package com.kingdomstudio.modules.motion.template.recommend;

import com.kingdomstudio.modules.motion.template.entity.MotionTemplate;
import com.kingdomstudio.modules.motion.template.mapper.MotionTemplateMapper;
import com.kingdomstudio.modules.motion.template.service.MotionTemplateService;
import com.kingdomstudio.modules.motion.template.vo.MotionRecommendVO;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 推荐规则的单元测试。
 *
 * <p>三条要钉死的事：
 * <ol>
 *   <li><b>顺序可解释</b>：命中场景 40 / 风格 25 / 触发 18 / 情绪 15 / 性能 12，谁的轴命中得多谁在前；
 *       说不清的轴不加分也不扣分。</li>
 *   <li><b>低预算不踩坑</b>：用户明说「轻量优先」时，依赖 GPU 的模板要被扣分并在理由里说明，
 *       但不能被隐藏 —— 只是排后面。</li>
 *   <li><b>同一句话结果稳定</b>：同样的输入两次调用，顺序完全一致（没有随机、没有哈希遍历）。</li>
 * </ol>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MotionRecommendationServiceTest {

	@Mock
	private MotionTemplateMapper templateMapper;

	private MotionRecommendationService recommendService;

	@BeforeEach
	void setUp() {
		// 真实的模板服务 + 真实的意图解析器：只把「从库里取数」这一步换成假数据
		MotionTemplateService templateService = new MotionTemplateService(templateMapper, null, null);
		recommendService = new MotionRecommendationService(templateService, new MotionIntentParser());
	}

	private MotionTemplate template(String key, String name, String scene, String style,
			String technology, String trigger, int... scores) {
		MotionTemplate template = new MotionTemplate();
		template.setId((long) Math.abs(key.hashCode() % 1000));
		template.setTemplateKey(key);
		template.setName(name);
		template.setNameEn(key);
		template.setDescription(name + "：把页面做得更利落。");
		template.setCategory("基础交互");
		template.setScene(scene);
		template.setStyle(style);
		template.setTechnology(technology);
		template.setTriggerType(trigger);
		template.setSource("OFFICIAL");
		template.setDifficulty(2);
		template.setBestFor("官网首页");
		template.setTags("");
		template.setScoreVisual(scores[0]);
		template.setScoreCode(scores[1]);
		template.setScoreReuse(scores[2]);
		template.setScorePerf(scores[3]);
		template.setScore(1); // 库里那列故意写错：推荐必须自己重算
		return template;
	}

	private void givenTemplates(MotionTemplate... templates) {
		when(templateMapper.selectList(any())).thenReturn(List.of(templates));
	}

	private List<String> keysOf(MotionRecommendVO result) {
		return result.getRecommendations().stream().map(MotionRecommendVO.Item::getTemplateKey).toList();
	}

	@Test
	@DisplayName("「科技感首页」：场景命中的排最前，风格与情绪次之，之后按推荐指数")
	void shouldRankBySceneThenStyleThenEmotion() {
		givenTemplates(
				template("cyber-hero", "赛博首屏", "Landing Page", "Cyber", "CSS", "load", 90, 90, 90, 90),
				template("minimal-hero", "极简首屏", "Landing Page", "Minimal", "CSS", "load", 95, 95, 95, 95),
				template("cyber-panel", "赛博面板", "Dashboard", "Cyber", "CSS", "load", 80, 80, 80, 80),
				template("organic-card", "有机卡片", "Portfolio", "Organic", "CSS", "hover", 90, 90, 90, 90));

		MotionRecommendVO result = recommendService.recommend("科技感首页", null);

		assertEquals("Landing Page", result.getIntent().getScene());
		assertEquals("Cyber", result.getIntent().getStyle());
		assertEquals("tech", result.getIntent().getEmotion());
		assertEquals(4, result.getScanned());

		// 场景+风格+情绪 = 40+25+15 = 80，再加推荐指数/10 的加成 → 89
		MotionRecommendVO.Item first = result.getRecommendations().get(0);
		assertEquals("cyber-hero", first.getTemplateKey());
		assertEquals(89, first.getScore());
		assertEquals(List.of("scene", "style", "emotion"), first.getMatchedAxes());

		// 只命中场景：40 + 10 = 50
		MotionRecommendVO.Item second = result.getRecommendations().get(1);
		assertEquals("minimal-hero", second.getTemplateKey());
		assertEquals(50, second.getScore());

		assertEquals(List.of("cyber-hero", "minimal-hero", "cyber-panel", "organic-card"), keysOf(result));
	}

	@Test
	@DisplayName("每条推荐都给得出理由，且理由与命中的轴一一对应")
	void shouldExplainEveryRecommendation() {
		givenTemplates(
				template("cyber-hero", "赛博首屏", "Landing Page", "Cyber", "CSS", "load", 90, 90, 90, 90),
				template("organic-card", "有机卡片", "Portfolio", "Organic", "CSS", "hover", 90, 90, 90, 90));

		MotionRecommendVO result = recommendService.recommend("科技感首页", null);

		MotionRecommendVO.Item hit = result.getRecommendations().get(0);
		assertEquals(hit.getMatchedAxes().size(), hit.getReasons().size(), "命中几轴就说几条理由");
		assertTrue(hit.getReasons().stream().anyMatch(reason -> reason.startsWith("场景匹配：网站首页")), hit.getReasons().toString());
		assertTrue(hit.getReasons().stream().anyMatch(reason -> reason.contains("情绪匹配")), hit.getReasons().toString());
		assertEquals("网站首页", hit.getSceneLabel());
		assertEquals("赛博", hit.getStyleLabel());
		assertEquals("加载时", hit.getTriggerLabel());

		// 一轴都没命中时也要有理由：说明按推荐指数排序，而不是留空让人猜
		MotionRecommendVO.Item miss = result.getRecommendations().get(1);
		assertTrue(miss.getMatchedAxes().isEmpty());
		assertTrue(miss.getReasons().get(0).contains("没命中任何一轴"), miss.getReasons().toString());
	}

	@Test
	@DisplayName("卡片上的场景 / 风格是模板自己的值，不是用户要求的那个")
	void shouldShowTemplateOwnLabels() {
		givenTemplates(
				template("cyber-hero", "赛博首屏", "Landing Page", "Cyber", "CSS", "load", 90, 90, 90, 90),
				template("organic-card", "有机卡片", "Portfolio", "Organic", "CSS", "hover", 90, 90, 90, 90));

		MotionRecommendVO result = recommendService.recommend("科技感首页", null);

		// 这一条只命中了情绪轴：它既不是网站首页，也不是赛博风格，卡片上必须照实写
		MotionRecommendVO.Item second = result.getRecommendations().get(1);
		assertEquals("organic-card", second.getTemplateKey());
		assertEquals("个人主页", second.getSceneLabel());
		assertEquals("有机", second.getStyleLabel());
		assertEquals("悬停", second.getTriggerLabel());
	}

	@Test
	@DisplayName("触发方式也是一轴：说「滚动」时，滚动触发的模板进一位")
	void shouldScoreInteractionAxis() {
		givenTemplates(
				template("scroll-parallax", "滚动视差", "Portfolio", "Minimal", "CSS", "scroll", 80, 80, 80, 80),
				template("hover-card", "悬停卡片", "Portfolio", "Minimal", "CSS", "hover", 80, 80, 80, 80));

		MotionRecommendVO result = recommendService.recommend("作品集，滚动时慢慢出现", null);

		assertEquals("scroll", result.getIntent().getInteraction());
		assertEquals("scroll-parallax", result.getRecommendations().get(0).getTemplateKey());
		// 两条都属于 Portfolio，场景各 +40；滚动触发的那条再多 18 → 66 对 48
		assertEquals(66, result.getRecommendations().get(0).getScore());
		assertEquals(48, result.getRecommendations().get(1).getScore());
		assertTrue(result.getRecommendations().get(0).getReasons().stream()
				.anyMatch(reason -> reason.startsWith("触发方式匹配：滚动驱动")), result.getRecommendations().get(0).getReasons().toString());
	}

	@Test
	@DisplayName("说「轻量优先」时，依赖 GPU 的模板被往后放，并说明原因（不隐藏）")
	void shouldPushGpuTemplatesDownForLowBudget() {
		givenTemplates(
				template("gpu-particles", "粒子星空", "Landing Page", "Cyber", "Canvas", "load", 80, 80, 80, 80),
				template("light-fade", "柔和淡入", "Landing Page", "Cyber", "CSS", "load", 95, 95, 95, 95));

		MotionRecommendVO result = recommendService.recommend("移动端优先，要轻量一点的动效", null);

		assertEquals("LOW", result.getIntent().getPerformance());
		assertEquals("light-fade", result.getRecommendations().get(0).getTemplateKey());
		// 轻量档命中性能轴：12 + 10 = 22
		assertEquals(22, result.getRecommendations().get(0).getScore());
		assertTrue(result.getRecommendations().get(0).getReasons().stream()
				.anyMatch(reason -> reason.contains("性能匹配：轻量优先")), result.getRecommendations().get(0).getReasons().toString());

		MotionRecommendVO.Item gpu = result.getRecommendations().get(1);
		assertEquals("gpu-particles", gpu.getTemplateKey());
		assertTrue(gpu.getReasons().stream().anyMatch(reason -> reason.contains("性能不匹配")), gpu.getReasons().toString());
		assertEquals("C", gpu.getPerformanceGrade());
	}

	@Test
	@DisplayName("性能等级：轻量且子分高是 A，均衡是 B，依赖 GPU 是 C")
	void shouldGradePerformance() {
		assertEquals("A", recommendService.performanceGrade("LIGHTWEIGHT", 95));
		assertEquals("B", recommendService.performanceGrade("LIGHTWEIGHT", 90));
		assertEquals("B", recommendService.performanceGrade("BALANCED", 88));
		assertEquals("C", recommendService.performanceGrade("GPU_ENHANCED", 99));
		assertEquals("B", recommendService.performanceGrade("LIGHTWEIGHT", null), "性能子分缺失时按 80 兜底，不给虚假的 A");
	}

	@Test
	@DisplayName("Top N：默认 5 条，可以要 3 条，要 999 条时收敛到 20")
	void shouldRespectLimit() {
		MotionTemplate[] pool = new MotionTemplate[8];
		for (int index = 0; index < pool.length; index++) {
			pool[index] = template("tpl-" + index, "模板 " + index, "Landing Page", "Minimal", "CSS", "load",
					80 + index, 80, 80, 80);
		}
		givenTemplates(pool);

		assertEquals(5, recommendService.recommend("首页", null).getRecommendations().size());
		assertEquals(3, recommendService.recommend("首页", 3).getRecommendations().size());
		assertEquals(1, recommendService.recommend("首页", 0).getRecommendations().size(), "下限收敛到 1 条");
		assertEquals(8, recommendService.recommend("首页", 999).getRecommendations().size(), "池子只有 8 个，不会凭空多出来");
	}

	@Test
	@DisplayName("听不懂的需求：不报错，按推荐指数给一版通用结果")
	void shouldFallBackForUnclearQuery() {
		givenTemplates(
				template("score-90", "高分模板", "Landing Page", "Minimal", "CSS", "load", 90, 90, 90, 90),
				template("score-70", "中分模板", "Dashboard", "Glass", "CSS", "hover", 70, 70, 70, 70));

		MotionRecommendVO result = recommendService.recommend("帮我看看这个", null);

		assertNull(result.getIntent().getScene());
		assertNull(result.getIntent().getStyle());
		assertNull(result.getIntent().getLabels().get("scene"), "说不清的轴不进标签表");
		assertEquals(2, result.getRecommendations().size());
		assertEquals("score-90", result.getRecommendations().get(0).getTemplateKey());
		assertEquals(9, result.getRecommendations().get(0).getScore(), "全部没命中时只剩推荐指数加成");
		assertTrue(result.getIntent().getSummary().contains("没太看出"));
	}

	@Test
	@DisplayName("空输入：返回空需求 + 通用排序，不抛异常")
	void shouldHandleBlankQuery() {
		givenTemplates(template("only-one", "唯一模板", "Login", "Cyber", "CSS", "load", 88, 88, 88, 88));

		MotionRecommendVO result = recommendService.recommend("   ", 5);

		assertEquals("", result.getQuery());
		assertEquals(1, result.getRecommendations().size());
		assertNotNull(result.getRecommendations().get(0).getRuntimeTierLabel());
		assertNotNull(result.getRecommendations().get(0).getStars());
	}

	@Test
	@DisplayName("同一句输入两次调用，结果顺序完全一致")
	void shouldBeStableForSameInput() {
		givenTemplates(
				template("a", "甲", "Landing Page", "Cyber", "CSS", "load", 90, 90, 90, 90),
				template("b", "乙", "Landing Page", "Cyber", "CSS", "load", 90, 90, 90, 90),
				template("c", "丙", "Landing Page", "Minimal", "CSS", "load", 90, 90, 90, 90),
				template("d", "丁", "Dashboard", "Cyber", "CSS", "scroll", 90, 90, 90, 90));

		MotionRecommendVO first = recommendService.recommend("科技感首页", 5);
		MotionRecommendVO second = recommendService.recommend("科技感首页", 5);

		assertEquals(keysOf(first), keysOf(second));
		assertFalse(keysOf(first).isEmpty());
	}

	@Test
	@DisplayName("运行档位与性能等级一起给出：轻量模板是 A 档，Canvas 模板是依赖 GPU 的 C 档")
	void shouldExposeRuntimeTier() {
		givenTemplates(
				template("light-fade", "柔和淡入", "Landing Page", "Minimal", "CSS", "load", 90, 90, 90, 95),
				template("three-hero", "三维首屏", "Landing Page", "Cyber", "Three.js", "load", 90, 90, 90, 95));

		MotionRecommendVO result = recommendService.recommend("首页入场", null);

		MotionRecommendVO.Item light = result.getRecommendations().stream()
				.filter(item -> "light-fade".equals(item.getTemplateKey())).findFirst().orElseThrow();
		assertEquals("LIGHTWEIGHT", light.getRuntimeTier());
		assertEquals("轻量", light.getRuntimeTierLabel());
		assertEquals("A", light.getPerformanceGrade());

		MotionRecommendVO.Item three = result.getRecommendations().stream()
				.filter(item -> "three-hero".equals(item.getTemplateKey())).findFirst().orElseThrow();
		assertEquals("GPU_ENHANCED", three.getRuntimeTier());
		assertEquals("C", three.getPerformanceGrade());
	}
}
