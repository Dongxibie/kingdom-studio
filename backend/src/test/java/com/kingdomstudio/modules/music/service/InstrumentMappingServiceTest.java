package com.kingdomstudio.modules.music.service;

import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.music.entity.InstrumentProfile;
import com.kingdomstudio.modules.music.parser.ParsedSong;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 映射引擎测试。
 *
 * <p>重点是三条容易出错、又直接决定「能不能照着弹出来」的规则：
 * 音阶排列的键位展开、超出音域时三种策略的行为、同时按下的音归组。
 */
class InstrumentMappingServiceTest {

	private final InstrumentMappingService service = new InstrumentMappingService();

	private InstrumentProfile profile(String mode, String scale, List<String> keys, int basePitch, String strategy) {
		InstrumentProfile profile = new InstrumentProfile();
		profile.setId(1L);
		profile.setName("测试档案");
		profile.setMappingMode(mode);
		profile.setScale(scale);
		profile.setKeyLayout(toJson(keys));
		profile.setBasePitch(basePitch);
		profile.setTranspose(0);
		profile.setOctaveShift(0);
		profile.setUnmappedStrategy(strategy);
		return profile;
	}

	private String toJson(List<String> keys) {
		return keys.stream().map(key -> "\"" + key + "\"").reduce((a, b) -> a + "," + b)
				.map(joined -> "[" + joined + "]").orElse("[]");
	}

	private ParsedSong song(ParsedSong.Note... notes) {
		List<ParsedSong.Note> list = ParsedSong.sort(List.of(notes));
		int duration = list.stream().mapToInt(ParsedSong.Note::endMs).max().orElse(0);
		return new ParsedSong("测试曲", "JIANPU", "1 2 3", 120, "4/4", duration, list);
	}

	private InstrumentProfile diatonic15() {
		return profile("DIATONIC", "MAJOR",
				List.of("Z", "X", "C", "V", "B", "N", "M", "A", "S", "D", "F", "G", "H", "J", "K"), 60, "NEAREST");
	}

	@Test
	@DisplayName("音阶排列：十五个键展开成两段 C 大调自然音，而不是半音递增")
	void shouldExpandDiatonicLayout() {
		List<Integer> pitches = service.expandKeyPitches(diatonic15());

		assertEquals(15, pitches.size());
		assertEquals(List.of(60, 62, 64, 65, 67, 69, 71, 72, 74, 76, 77, 79, 81, 83, 84), pitches);
	}

	@Test
	@DisplayName("半音排列：相邻键差一个半音")
	void shouldExpandChromaticLayout() {
		InstrumentProfile chromatic = profile("CHROMATIC", "CHROMATIC",
				List.of("Z", "S", "X", "D", "C", "V", "G", "B", "H", "N", "J", "M"), 60, "SKIP");

		List<Integer> pitches = service.expandKeyPitches(chromatic);

		assertEquals(12, pitches.size());
		assertEquals(60, pitches.get(0));
		assertEquals(71, pitches.get(11));
	}

	@Test
	@DisplayName("C 大调旋律落在音阶键上：不需要任何调整")
	void shouldMapMelodyWithoutAdjustment() {
		ParsedSong song = song(
				new ParsedSong.Note(60, 90, 0, 500, 0),
				new ParsedSong.Note(64, 90, 500, 500, 0),
				new ParsedSong.Note(67, 90, 1000, 500, 0));

		KeySequenceVO sequence = service.map(song, diatonic15(), null);

		assertEquals(3, sequence.getMappedCount());
		assertEquals(0, sequence.getAdjustedCount());
		assertEquals(0, sequence.getUnmappedCount());
		assertEquals(List.of("Z", "C", "B"), sequence.getStrokes().stream().map(stroke -> stroke.getKeys().get(0)).toList());
		assertFalse(sequence.getStrokes().get(0).getChord());
	}

	@Test
	@DisplayName("超出音域 + 跳过策略：如实记进未映射清单，并说明原因")
	void shouldReportUnmappedWithReason() {
		InstrumentProfile profile = diatonic15();
		profile.setUnmappedStrategy("SKIP");
		ParsedSong song = song(new ParsedSong.Note(90, 90, 0, 500, 0)); // 远高于十五键上限 C6

		KeySequenceVO sequence = service.map(song, profile, null);

		assertEquals(0, sequence.getMappedCount());
		assertEquals(1, sequence.getUnmappedCount());
		assertEquals("F#6", sequence.getUnmapped().get(0).getNoteName(), "音名的写法是字母在前：F#6");
		assertTrue(sequence.getUnmapped().get(0).getReason().contains("高于最高键"), sequence.getUnmapped().get(0).getReason());
		assertTrue(sequence.getExportText().contains("未能落键的音"));
	}

	@Test
	@DisplayName("就近策略：落下并标记为已调整；距离相同时取更低的键")
	void shouldSnapToNearestKey() {
		ParsedSong song = song(new ParsedSong.Note(61, 90, 0, 500, 0)); // C#4，不在自然音阶上（60 与 62 等距）

		KeySequenceVO sequence = service.map(song, diatonic15(), "NEAREST");

		assertEquals(1, sequence.getMappedCount());
		assertEquals(1, sequence.getAdjustedCount());
		assertEquals("Z", sequence.getStrokes().get(0).getKeys().get(0), "等距时取更低那个键：C4 而不是 D4");
		assertTrue(sequence.getStrokes().get(0).getAdjusted());
		assertEquals("C#4→C4", sequence.getStrokes().get(0).getNoteNames().get(0));
	}

	@Test
	@DisplayName("移八度策略：高一个八度的音挪回音域内")
	void shouldShiftOctaveIntoRange() {
		InstrumentProfile profile = diatonic15();
		profile.setUnmappedStrategy("SHIFT_OCTAVE");
		ParsedSong song = song(new ParsedSong.Note(86, 90, 0, 500, 0)); // D6，比音域上限 D6 高一个八度

		KeySequenceVO sequence = service.map(song, profile, null);

		assertEquals(1, sequence.getMappedCount());
		assertEquals(1, sequence.getAdjustedCount());
		assertEquals(0, sequence.getUnmappedCount());
		assertTrue(sequence.getStrokes().get(0).getNoteNames().get(0).startsWith("D6→"), sequence.getStrokes().get(0).getNoteNames().toString());
	}

	@Test
	@DisplayName("同时按下的音归成一组和弦，只算一次按键动作")
	void shouldGroupSimultaneousNotesAsChord() {
		ParsedSong song = song(
				new ParsedSong.Note(60, 90, 0, 500, 0),
				new ParsedSong.Note(64, 90, 0, 500, 0),
				new ParsedSong.Note(67, 90, 500, 500, 0));

		KeySequenceVO sequence = service.map(song, diatonic15(), null);

		assertEquals(2, sequence.getStrokes().size());
		KeySequenceVO.Stroke chord = sequence.getStrokes().get(0);
		assertTrue(chord.getChord());
		assertEquals(List.of("Z", "C"), chord.getKeys(), "同时按下的两个音在同一组里");
		assertEquals(List.of("B"), sequence.getStrokes().get(1).getKeys(), "第三个音单独一次按键");
		assertEquals(3, sequence.getMappedCount(), "三个音都落了键");
	}

	@Test
	@DisplayName("移调与升降八度都会作用到落键上")
	void shouldApplyTranspose() {
		InstrumentProfile profile = diatonic15();
		profile.setTranspose(2); // 整体升高两个半音：原本的 C4 变成 D4

		KeySequenceVO sequence = service.map(song(new ParsedSong.Note(60, 90, 0, 500, 0)), profile, null);

		assertEquals("X", sequence.getStrokes().get(0).getKeys().get(0), "C4 移调 +2 后落在 D4 那个键上");
	}

	@Test
	@DisplayName("导出文本：含速度、键位图例、按键行与未映射说明")
	void shouldBuildExportText() {
		InstrumentProfile profile = diatonic15();
		profile.setUnmappedStrategy("SKIP");
		ParsedSong song = song(
				new ParsedSong.Note(60, 90, 0, 500, 0),
				new ParsedSong.Note(100, 90, 500, 500, 0));

		KeySequenceVO sequence = service.map(song, profile, null);
		String text = sequence.getExportText();

		assertTrue(text.contains("测试曲 · 测试档案"), text);
		assertTrue(text.contains("120 BPM"), text);
		assertTrue(text.contains("Z=C4"), "键位图例要能看懂每个键弹什么音");
		assertTrue(text.contains("0:00.000  Z"), text);
		assertTrue(text.contains("E7"), "未映射的音名要写出来");
	}

	@Test
	@DisplayName("坏配置与空配置都明确报错，不静默当成空档案")
	void shouldRejectBadLayout() {
		InstrumentProfile broken = profile("DIATONIC", "MAJOR", List.of("Z"), 60, "SKIP");
		broken.setKeyLayout("{不是数组}");
		assertThrows(BusinessException.class, () -> service.expandKeyPitches(broken));

		InstrumentProfile empty = profile("DIATONIC", "MAJOR", List.of(), 60, "SKIP");
		assertThrows(BusinessException.class, () -> service.map(song(new ParsedSong.Note(60, 90, 0, 100, 0)), empty, null));
	}

	@Test
	@DisplayName("请求里的策略可以临时覆盖档案里的设置")
	void shouldAllowStrategyOverride() {
		ParsedSong song = song(new ParsedSong.Note(61, 90, 0, 500, 0));

		KeySequenceVO skipped = service.map(song, diatonic15(), "SKIP");
		KeySequenceVO snapped = service.map(song, diatonic15(), "NEAREST");

		assertEquals(1, skipped.getUnmappedCount());
		assertEquals(1, snapped.getMappedCount());
		assertEquals("NEAREST", snapped.getStrategy());
	}
}
