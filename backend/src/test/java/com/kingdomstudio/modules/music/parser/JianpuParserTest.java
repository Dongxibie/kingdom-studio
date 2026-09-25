package com.kingdomstudio.modules.music.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 简谱解析测试：头行、八度记号、时值记号与调号。 */
class JianpuParserTest {

	private final JianpuParser parser = new JianpuParser();

	@Test
	@DisplayName("基础音级：1 到 7 对应 C 大调自然音")
	void shouldParseScaleDegrees() {
		ParsedSong song = parser.parse("1 2 3 4 5 6 7", "音阶");

		List<ParsedSong.Note> notes = song.notes();
		assertEquals(7, notes.size());
		assertEquals("C4", PitchNames.name(notes.get(0).pitch()));
		assertEquals(60, notes.get(0).pitch());
		assertEquals(62, notes.get(1).pitch());
		assertEquals(64, notes.get(2).pitch());
		assertEquals(65, notes.get(3).pitch(), "4 是半音关系：E 到 F 只差半音");
		assertEquals(71, notes.get(6).pitch(), "7 = B4");
	}

	@Test
	@DisplayName("头行：速度、拍号、调号都能读出来")
	void shouldParseHeader() {
		ParsedSong song = parser.parse("1=G 3/4 BPM=120\n5 5 5", "小曲");

		assertEquals(120, song.tempoBpm());
		assertEquals("3/4", song.timeSignature());
		assertEquals(500, song.notes().get(1).startMs(), "120 BPM 每拍 500ms");
		// 1=G 时「5」是 G 调上的第 5 级 = D
		assertEquals(74, song.notes().get(0).pitch(), "G 调的 5 应为 D5");
	}

	@Test
	@DisplayName("八度记号：' 升、, 降，可叠加")
	void shouldHandleOctaveMarks() {
		ParsedSong song = parser.parse("1 1' 1'' 1,", "八度");

		List<ParsedSong.Note> notes = song.notes();
		assertEquals(60, notes.get(0).pitch());
		assertEquals(72, notes.get(1).pitch());
		assertEquals(84, notes.get(2).pitch());
		assertEquals(48, notes.get(3).pitch());
	}

	@Test
	@DisplayName("时值记号：延长线、附点、减时线")
	void shouldHandleDurations() {
		ParsedSong song = parser.parse("BPM=60\n5 - 5. 5_ 5", "时值");

		List<ParsedSong.Note> notes = song.notes();
		assertEquals(2000, notes.get(0).durationMs(), "延长线：本来的 1 拍再加 1 拍");
		assertEquals(1500, notes.get(1).durationMs(), "附点：1 拍 × 1.5");
		assertEquals(500, notes.get(2).durationMs(), "减时线：1 拍 ÷ 2");
		assertEquals(1000, notes.get(3).durationMs());
	}

	@Test
	@DisplayName("休止符 0 不产生音符，但占用时间轴")
	void shouldHandleRests() {
		ParsedSong song = parser.parse("BPM=60\n5 0 5", "休止");

		assertEquals(2, song.noteCount());
		assertEquals(0, song.notes().get(0).startMs());
		assertEquals(2000, song.notes().get(1).startMs(), "前一个音占 1 拍 + 休止 1 拍 = 2 拍后才轮到它");
	}

	@Test
	@DisplayName("变化音 # 与 b")
	void shouldHandleAccidentals() {
		ParsedSong song = parser.parse("#4 b7", "变化音");

		assertEquals(66, song.notes().get(0).pitch(), "#4 = F#4");
		assertEquals(70, song.notes().get(1).pitch(), "b7 = A#4 下方的降 B4");
	}

	@Test
	@DisplayName("用斜杠断拍的谱面行不会被当成头行吃掉")
	void shouldNotSwallowBodyLinesWithSlash() {
		ParsedSong song = parser.parse("1=C\n1 2 / 3 4", "斜杠");

		assertEquals(4, song.noteCount(), "行内的斜杠只是断拍");
		assertEquals(60, song.notes().get(0).pitch());
	}

	@Test
	@DisplayName("多行谱面：每行从小节线开始；没写满一小节的行补到小节末")
	void shouldAlignEachLineToBar() {
		// 首行只写了 2 拍，4/4 一小节 4 拍，于是补到 4000ms；次行从下一小节的起点开始
		ParsedSong song = parser.parse("BPM=60 4/4\n1 2 |\n5 5 5 5", "换行");

		assertEquals(6, song.noteCount(), "首行两个音 + 次行四个音");
		assertEquals(4000, song.notes().get(2).startMs(), "第二行从第 2 小节的起点开始");
	}

	@Test
	@DisplayName("写满整小节的行不需要补：下一行紧接在本小节之后")
	void shouldNotPadCompleteBars() {
		ParsedSong song = parser.parse("BPM=60 4/4\n1 2 3 4\n5 5 5 5", "整小节");

		assertEquals(4000, song.notes().get(4).startMs(), "首行正好 4 拍，次行紧接着开始");
	}
}
