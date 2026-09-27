package com.kingdomstudio.modules.music.strategy;

import com.kingdomstudio.modules.music.parser.ParsedSong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 演奏策略的单元测试。
 *
 * <p>最要紧的两条：<b>标准与展示模式绝不改动旋律</b>（音高序列与音符数量必须一模一样），
 * 以及<b>每条改动都要能解释</b>（title 与 detail 都不能为空，界面直接拿它当 AI 解释）。
 */
class StrategyApplierTest {

	private final StrategyApplier applier = new StrategyApplier();

	private ParsedSong song(int tempo, int durationMs, ParsedSong.Note... notes) {
		return new ParsedSong("小星星", "MIDI", "scale.mid", tempo, "4/4", durationMs, List.of(notes));
	}

	private ParsedSong.Note note(int pitch, int startMs, int durationMs) {
		return new ParsedSong.Note(pitch, 90, startMs, durationMs, 0);
	}

	@Test
	@DisplayName("标准模式：一个音都不动")
	void normalKeepsEverything() {
		ParsedSong source = song(120, 2000, note(60, 0, 500), note(62, 500, 500), note(64, 1000, 1000));
		StrategyApplier.Result result = applier.apply(source, PerformanceStrategy.NORMAL);
		assertEquals(source.notes(), result.song().notes());
		assertEquals(120, result.song().tempoBpm());
		assertTrue(result.adjustments().isEmpty());
		assertTrue(result.summary().contains("保持原曲"));
	}

	@Test
	@DisplayName("初学模式：原速偏快才降速，慢曲子不降")
	void beginnerLowersFastTempoOnly() {
		ParsedSong fast = song(120, 4000, note(60, 0, 500), note(62, 1000, 500));
		StrategyApplier.Result lowered = applier.apply(fast, PerformanceStrategy.BEGINNER);
		assertEquals(96, lowered.song().tempoBpm(), "120 BPM 应降到 96");
		assertTrue(lowered.adjustments().stream().anyMatch(item -> "TEMPO".equals(item.type())));
		assertEquals("120 BPM", lowered.adjustments().stream()
				.filter(item -> "TEMPO".equals(item.type())).findFirst().orElseThrow().before());

		ParsedSong medium = song(96, 4000, note(60, 0, 800), note(62, 1600, 800));
		StrategyApplier.Result adjusted = applier.apply(medium, PerformanceStrategy.BEGINNER);
		assertEquals(77, adjusted.song().tempoBpm(), "96 BPM 应降到 77，初学模式总要给出更从容的速度");
		assertTrue(adjusted.adjustments().stream().anyMatch(item -> "TEMPO".equals(item.type())));

		ParsedSong slow = song(80, 4000, note(60, 0, 800), note(62, 1600, 800));
		StrategyApplier.Result kept = applier.apply(slow, PerformanceStrategy.BEGINNER);
		assertEquals(80, kept.song().tempoBpm(), "本来就慢的曲子不该再降速");
		assertFalse(kept.adjustments().stream().anyMatch(item -> "TEMPO".equals(item.type())));
	}

	@Test
	@DisplayName("初学模式：同一个时刻挤在一起的音只留一个，并说明原因")
	void beginnerTrimsClusters() {
		ParsedSong source = song(60, 2000, note(60, 0, 300), note(64, 30, 300), note(67, 60, 300));
		StrategyApplier.Result result = applier.apply(source, PerformanceStrategy.BEGINNER);
		assertEquals(1, result.song().notes().size(), "30/60ms 的密集音应被精简");
		StrategyApplier.Adjustment adjustment = result.adjustments().stream()
				.filter(item -> "SIMPLIFY".equals(item.type())).findFirst().orElseThrow();
		assertTrue(adjustment.detail().contains("同时按键"), adjustment.detail());
		assertEquals("3 个音", adjustment.before());
	}

	@Test
	@DisplayName("初学模式：过短的经过音合并进前一个音")
	void beginnerMergesShortNotes() {
		ParsedSong source = song(60, 2000, note(60, 0, 400), note(62, 200, 50), note(64, 1000, 400));
		StrategyApplier.Result result = applier.apply(source, PerformanceStrategy.BEGINNER);
		assertEquals(2, result.song().notes().size());
		assertTrue(result.adjustments().stream().anyMatch(item -> item.detail().contains("经过句")));
	}

	@Test
	@DisplayName("初学模式：离中心音太远的音移八度收窄，音名不变")
	void beginnerNarrowsRange() {
		// 中心音约 60，最高 88（超出 7 个半音的半径）→ 应被移八度靠近
		ParsedSong source = song(60, 3000, note(60, 0, 500), note(88, 600, 500), note(59, 1200, 500));
		StrategyApplier.Result result = applier.apply(source, PerformanceStrategy.BEGINNER);
		int max = result.song().notes().stream().mapToInt(ParsedSong.Note::pitch).max().orElse(0);
		int min = result.song().notes().stream().mapToInt(ParsedSong.Note::pitch).min().orElse(0);
		assertTrue(max - 60 <= 7, "上限应被收进 7 个半音内：" + max);
		assertTrue(60 - min <= 7, "下限应被收进 7 个半音内：" + min);
		assertEquals(64, result.song().notes().get(1).pitch(),
				"88 移八度后应落在中心音附近（88-12-12=64）：" + result.song().notes().get(1).pitch());
		assertTrue(result.adjustments().stream().anyMatch(item -> "OCTAVE".equals(item.type())));
	}

	@Test
	@DisplayName("初学模式：每条改动都带标题与原因，可直接当解释展示")
	void beginnerEveryChangeIsExplained() {
		ParsedSong source = song(140, 4000, note(60, 0, 200), note(64, 20, 200), note(90, 500, 60), note(62, 1200, 600));
		StrategyApplier.Result result = applier.apply(source, PerformanceStrategy.BEGINNER);
		assertFalse(result.adjustments().isEmpty());
		result.adjustments().forEach(item -> {
			assertFalse(item.title().isBlank(), "调整要有标题");
			assertFalse(item.detail().isBlank(), "调整要有原因");
		});
		assertTrue(result.summary().startsWith("初学模式："));
	}

	@Test
	@DisplayName("展示模式：音符数量与音高序列完全不变（不生成音乐）")
	void showcaseNeverChangesMelody() {
		ParsedSong source = song(120, 4000, note(60, 0, 500), note(64, 500, 500), note(67, 1000, 1500));
		StrategyApplier.Result result = applier.apply(source, PerformanceStrategy.SHOWCASE);
		assertEquals(source.notes().size(), result.song().notes().size());
		List<Integer> before = source.notes().stream().map(ParsedSong.Note::pitch).toList();
		List<Integer> after = result.song().notes().stream().map(ParsedSong.Note::pitch).toList();
		assertEquals(before, after, "展示模式必须保留原旋律");
		assertEquals(source.tempoBpm(), result.song().tempoBpm());
	}

	@Test
	@DisplayName("展示模式：重拍加力、长音留呼吸，各自给出去向")
	void showcaseAccentsAndBreathes() {
		ParsedSong source = song(120, 4000, note(60, 0, 500), note(64, 500, 200), note(67, 1000, 600));
		StrategyApplier.Result result = applier.apply(source, PerformanceStrategy.SHOWCASE);
		List<ParsedSong.Note> notes = result.song().notes();
		assertTrue(notes.get(0).velocity() > source.notes().get(0).velocity(), "小节首音应被加强");
		assertEquals(source.notes().get(1).velocity(), notes.get(1).velocity(), "非重拍不动力度");
		assertTrue(notes.get(2).durationMs() > source.notes().get(2).durationMs(), "长音应延长一点呼吸");
		assertTrue(result.adjustments().stream().anyMatch(item -> "ACCENT".equals(item.type())));
		assertTrue(result.adjustments().stream().anyMatch(item -> "BREATH".equals(item.type())));
		assertTrue(result.summary().contains("旋律与音符数量保持原样"));
	}

	@Test
	@DisplayName("空曲子：不报错、不产生改动，说明为什么没得改")
	void emptySongIsHandled() {
		ParsedSong empty = new ParsedSong("空", "JIANPU", "", 120, "4/4", 0, new ArrayList<>());
		StrategyApplier.Result result = applier.apply(empty, PerformanceStrategy.BEGINNER);
		assertTrue(result.adjustments().isEmpty());
		assertTrue(result.summary().contains("没有音符"));
	}

	@Test
	@DisplayName("策略解析：只认白名单，认不出来一律回落到标准模式")
	void strategyParsingIsStrict() {
		assertEquals(PerformanceStrategy.BEGINNER, PerformanceStrategy.of("easy"));
		assertEquals(PerformanceStrategy.BEGINNER, PerformanceStrategy.of("初学模式"));
		assertEquals(PerformanceStrategy.SHOWCASE, PerformanceStrategy.of("show"));
		assertEquals(PerformanceStrategy.NORMAL, PerformanceStrategy.of("随便来点"));
		assertEquals(PerformanceStrategy.NORMAL, PerformanceStrategy.of(null));
		assertTrue(PerformanceStrategy.recognizes("BEGINNER"));
		assertFalse(PerformanceStrategy.recognizes("SUPER_HARD"));
		assertNotEquals(PerformanceStrategy.SHOWCASE, PerformanceStrategy.of("SUPER_HARD"));
	}
}
