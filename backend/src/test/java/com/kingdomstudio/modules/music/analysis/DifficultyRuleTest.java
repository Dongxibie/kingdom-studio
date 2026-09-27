package com.kingdomstudio.modules.music.analysis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 难度规则的单元测试。
 *
 * <p>规则只有三条（跨度 / 速度 / 密度），但它是曲库列表和详情卡共用的，
 * 所以每一条边界都要钉住 —— 改错了会让所有曲子的星级一起漂。
 */
class DifficultyRuleTest {

	private DifficultyRule.Result evaluate(int notes, int durationMs, int bpm, int low, int high) {
		return DifficultyRule.evaluate(new DifficultyRule.Input(notes, durationMs, bpm, low, high));
	}

	@Test
	@DisplayName("入门：单音、慢速、一个八度以内 → 1 星")
	void shouldRateBeginner() {
		// 14 个音 / 10 秒 = 1.4 个每秒，96 BPM，音域 C4-A4（9 个半音）
		DifficultyRule.Result result = evaluate(14, 10_000, 96, 60, 69);

		assertEquals(1, result.stars());
		assertEquals("入门", result.label());
		assertTrue(result.reasons().stream().noneMatch(DifficultyRule.Reason::hit), "三条都没命中");
	}

	@Test
	@DisplayName("跨度与速度各命中一条 → 2 星")
	void shouldRateSimple() {
		// 音域 15 个半音（>12）、速度 130（>120），密度 2.0 不算密
		DifficultyRule.Result result = evaluate(20, 10_000, 130, 60, 75);

		assertEquals(2, result.stars());
		assertEquals("简单", result.label());
		assertEquals(2, result.reasons().stream().filter(DifficultyRule.Reason::hit).count());
	}

	@Test
	@DisplayName("密集的快速曲子：三条全中且有两项超双倍 → 5 星")
	void shouldRateChallenge() {
		// 60 个音 / 8 秒 = 7.5 个每秒（>4）、180 BPM（>160）、跨度 24（>19）
		DifficultyRule.Result result = evaluate(60, 8_000, 180, 52, 76);

		assertEquals(5, result.stars());
		assertEquals("挑战", result.label());
		// 三条规则各命中一次、各贡献 2 分，共 6 分 → 5 星
		assertEquals(3, result.reasons().stream().filter(DifficultyRule.Reason::hit).count());
		assertEquals(4, DifficultyRule.starsOf(5), "5 分是 4 星");
		assertEquals(5, DifficultyRule.starsOf(6), "6 分才是 5 星");
	}

	@Test
	@DisplayName("计分依据逐条给出，未命中的也列出来（说明「为什么不是更难」）")
	void shouldExplainEveryReason() {
		DifficultyRule.Result result = evaluate(14, 10_000, 96, 60, 69);

		assertEquals(3, result.reasons().size());
		List<String> labels = result.reasons().stream().map(DifficultyRule.Reason::label).toList();
		assertTrue(labels.get(0).contains("音域跨度"), labels.toString());
		assertTrue(labels.get(1).contains("96 BPM"), labels.toString());
		assertTrue(labels.get(2).contains("音符密度"), labels.toString());
		assertTrue(labels.get(2).contains("1.4"), labels.toString());
	}

	@Test
	@DisplayName("边界值：正好 120 BPM / 正好 12 个半音算命中，119 / 11 不算")
	void shouldHandleBoundaries() {
		assertEquals(2, evaluate(10, 10_000, 120, 60, 72).stars(), "速度 120 与跨度 12 各加一分 → 2 星");
		assertEquals(1, evaluate(10, 10_000, 119, 60, 71).stars(), "差一点就是不命中");
	}

	@Test
	@DisplayName("空曲子不炸：0 个音 0 毫秒也给 1 星")
	void shouldHandleEmpty() {
		DifficultyRule.Result result = evaluate(0, 0, 0, 0, 0);

		assertEquals(1, result.stars());
		assertTrue(result.reasons().stream().noneMatch(DifficultyRule.Reason::hit));
	}

	@Test
	@DisplayName("三档分层：1-2 星是简单、3 星普通、4-5 星高级")
	void shouldMapTier() {
		assertEquals("EASY", DifficultyRule.tierOf(1));
		assertEquals("EASY", DifficultyRule.tierOf(2));
		assertEquals("NORMAL", DifficultyRule.tierOf(3));
		assertEquals("ADVANCED", DifficultyRule.tierOf(4));
		assertEquals("ADVANCED", DifficultyRule.tierOf(5));
		assertEquals("简单", DifficultyRule.tierLabelOf(1));
		assertEquals("普通", DifficultyRule.tierLabelOf(3));
		assertEquals("高级", DifficultyRule.tierLabelOf(5));
	}

	@Test
	@DisplayName("适合谁：简单→新手、普通→有基础、高级→熟练")
	void shouldMapAudience() {
		assertEquals("新手", DifficultyRule.audienceOf(1));
		assertEquals("新手", DifficultyRule.audienceOf(2));
		assertEquals("有基础", DifficultyRule.audienceOf(3));
		assertEquals("熟练", DifficultyRule.audienceOf(4));
		assertEquals("熟练", DifficultyRule.audienceOf(5));
	}

	@Test
	@DisplayName("结果里带着分层与适合谁，界面不用自己再推一遍")
	void shouldCarryTierInResult() {
		DifficultyRule.Result beginner = evaluate(14, 10_000, 96, 60, 69);
		assertEquals("EASY", beginner.tier());
		assertEquals("新手", beginner.audience());

		DifficultyRule.Result hard = evaluate(60, 8_000, 180, 52, 76);
		assertEquals("ADVANCED", hard.tier());
		assertEquals("熟练", hard.audience());
	}

	@Test
	@DisplayName("星级与提示文案一一对应，界面直接可用")
	void shouldProvideHints() {
		assertEquals("入门", DifficultyRule.labelOf(1));
		assertEquals("挑战", DifficultyRule.labelOf(5));
		assertTrue(DifficultyRule.hintOf(1).contains("第一次上手"));
		assertTrue(DifficultyRule.hintOf(4).contains("简单版"));
		assertFalse(DifficultyRule.hintOf(3).isBlank());
	}
}
