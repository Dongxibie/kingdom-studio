package com.kingdomstudio.modules.motion.crawler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 分类器单元测试：不联网，纯关键词打分。 */
class MotionClassifierTest {

	private final MotionClassifier classifier = new MotionClassifier();

	@Test
	@DisplayName("悬停类关键词命中 Hover")
	void shouldClassifyHover() {
		var result = classifier.classify("glass-card hover shine effect with mouse-over glow");
		assertEquals("Hover", result.category());
		assertTrue(result.score() > 0);
		assertFalse(result.matched().isEmpty());
	}

	@Test
	@DisplayName("粒子和 canvas 命中 Particle，优先于 Background")
	void shouldPreferParticleOverBackground() {
		var result = classifier.classify("interactive particle constellation background canvas");
		assertEquals("Particle", result.category());
	}

	@Test
	@DisplayName("中文关键词也能命中")
	void shouldClassifyChinese() {
		assertEquals("Loading", classifier.classify("加载动画 骨架屏 微光").category());
		assertEquals("Glass", classifier.classify("玻璃拟态 磨砂面板").category());
	}

	@Test
	@DisplayName("完全无关的文本降级为 Entrance 且标记不自信")
	void shouldFallbackWhenNothingMatched() {
		var result = classifier.classify("a small utility for parsing csv files");
		assertEquals("Entrance", result.category());
		assertEquals(0, result.score());
		assertFalse(result.confident(), "没有命中时不应认为分类可信");
	}

	@Test
	@DisplayName("null 与空串不会抛异常")
	void shouldHandleBlankInput() {
		assertEquals("Entrance", classifier.classify(null).category());
		assertEquals("Entrance", classifier.classify("   ").category());
	}
}
