package com.kingdomstudio.modules.motion.template.recommend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 意图解析的单元测试。
 *
 * <p>spec 要求至少 10 个场景，这里用参数化把「一句话 ↔ 五轴」逐条钉住：
 * 解析结果必须完全可复现（同一句话任何时候都一样），所以这些断言写死在测试里。
 */
class MotionIntentParserTest {

	private final MotionIntentParser parser = new MotionIntentParser();

	@ParameterizedTest(name = "[{index}] {0} → 场景={1} 风格={2} 情绪={3}")
	@CsvSource({
			"科技感首页,               Landing Page, Cyber,     tech",
			"苹果官网风格,             Landing Page, Minimal,   premium",
			"高级酒店官网,             Landing Page, Luxury,    premium",
			"后台数据面板,             Dashboard,    ,          calm",
			"游戏登录页面,             Login,        ,          tech",
			"做一个个人作品集,         Portfolio,    ,          ",
			"AI 产品的营销页,          AI SaaS,      ,          ",
			"游戏加载界面,             Game UI,      ,          tech",
			"想要极简风格的官网,       Landing Page, Minimal,   ",
			"赛博朋克风的登录页,       Login,        Cyber,     tech",
			"温暖治愈的手作感页面,     ,             Organic,   warm",
			"奢华精品酒店主页,         Landing Page, Luxury,    premium",
	})
	@DisplayName("12 个场景：一句话解析成五个轴")
	void parsesScenarios(String query, String scene, String style, String emotion) {
		MotionIntent intent = parser.parse(query);
		assertEquals(blankToNull(scene), intent.getScene(), query);
		assertEquals(blankToNull(style), intent.getStyle(), query);
		assertEquals(blankToNull(emotion), intent.getEmotion(), query);
		assertNotNull(intent.getSummary());
	}

	@Test
	@DisplayName("触发方式：悬停 / 滚动 / 点击 / 入场 各自认得出")
	void parsesInteraction() {
		assertEquals("hover", parser.parse("鼠标悬停在卡片上有反应").getInteraction());
		assertEquals("scroll", parser.parse("滚动时触发视差").getInteraction());
		assertEquals("click", parser.parse("点击按钮后的反馈").getInteraction());
		assertEquals("load", parser.parse("进入页面时的入场动画").getInteraction());
		assertNull(parser.parse("随便看看").getInteraction(), "说不清就不猜");
	}

	@Test
	@DisplayName("性能预算：轻量优先 / 效果优先 / 均衡")
	void parsesPerformance() {
		assertEquals("LOW", parser.parse("移动端优先，轻量一点的动效").getPerformance());
		assertEquals("HIGH", parser.parse("越炫越好，3D 也上").getPerformance());
		assertEquals("MEDIUM", parser.parse("效果均衡就行").getPerformance());
		assertNull(parser.parse("科技感首页").getPerformance(), "没说性能就不加这一轴");
	}

	@Test
	@DisplayName("每一轴都留下命中痕迹，能解释「为什么这么理解」")
	void keepsHitsForExplanation() {
		MotionIntent intent = parser.parse("高级酒店官网，滚动时慢慢出现");
		assertTrue(intent.getHits().get("scene").contains("官网"));
		assertTrue(intent.getHits().get("style").contains("高级") || intent.getHits().get("style").contains("酒店"));
		assertTrue(intent.getHits().get("interaction").contains("滚动"));
		assertEquals("网站首页", intent.getLabels().get("scene"));
		assertEquals("高级", intent.getLabels().get("style"));
		assertTrue(intent.getSummary().contains("场景=网站首页"), intent.getSummary());
	}

	@Test
	@DisplayName("完全看不懂的输入：五轴全空，但仍然给一句话说明")
	void handlesUnclearInput() {
		MotionIntent intent = parser.parse("帮我看看这个");
		assertNull(intent.getScene());
		assertNull(intent.getStyle());
		assertNull(intent.getEmotion());
		assertNull(intent.getPerformance());
		assertNull(intent.getInteraction());
		assertTrue(intent.getSummary().contains("没太看出"), intent.getSummary());
	}

	@Test
	@DisplayName("空输入不报错")
	void handlesBlankInput() {
		MotionIntent intent = parser.parse("   ");
		assertNull(intent.getScene());
		assertNotNull(intent.getSummary());
	}

	@Test
	@DisplayName("同一句话解析结果稳定：两次调用完全一致")
	void parsingIsDeterministic() {
		MotionIntent first = parser.parse("科技感首页，滚动视差");
		MotionIntent second = parser.parse("科技感首页，滚动视差");
		assertEquals(first.getScene(), second.getScene());
		assertEquals(first.getStyle(), second.getStyle());
		assertEquals(first.getEmotion(), second.getEmotion());
		assertEquals(first.getInteraction(), second.getInteraction());
		assertEquals(first.getHits(), second.getHits());
	}

	private String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
