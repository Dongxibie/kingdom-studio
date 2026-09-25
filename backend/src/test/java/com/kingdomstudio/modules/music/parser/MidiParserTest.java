package com.kingdomstudio.modules.music.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MIDI 解析测试。
 *
 * <p>刻意不依赖外部的 .mid 样本文件：测试自己用 {@code javax.sound.midi} 造一段序列，
 * 写成字节再解析回来 —— 期望值是算得出来的，样本文件一旦丢失或损坏测试也不会跟着烂掉。
 */
class MidiParserTest {

	private static final int PPQ = 480;

	private final MidiParser parser = new MidiParser();

	/** 造一个单轨序列：按 (音高, 起始 tick, 时长 tick) 铺音符 */
	private Sequence buildSequence(Object[][] notes, int[] tempoUsPerQuarter, int[] timeSignature) throws Exception {
		Sequence sequence = new Sequence(Sequence.PPQ, PPQ);
		Track track = sequence.createTrack();
		for (int[] tempo : tempoChangesOf(tempoUsPerQuarter)) {
			MetaMessage meta = new MetaMessage();
			byte[] data = {(byte) ((tempo[1] >> 16) & 0xFF), (byte) ((tempo[1] >> 8) & 0xFF), (byte) (tempo[1] & 0xFF)};
			meta.setMessage(0x51, data, data.length);
			track.add(new MidiEvent(meta, tempo[0]));
		}
		if (timeSignature != null) {
			MetaMessage meta = new MetaMessage();
			byte[] data = {(byte) timeSignature[0], (byte) timeSignature[1], 24, 8};
			meta.setMessage(0x58, data, data.length);
			track.add(new MidiEvent(meta, 0));
		}
		for (Object[] note : notes) {
			int pitch = (int) note[0];
			long start = (long) (int) note[1];
			long length = (long) (int) note[2];
			int velocity = note.length > 3 ? (int) note[3] : 90;
			track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, pitch, velocity), start));
			track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, pitch, 0), start + length));
		}
		return sequence;
	}

	/** 把 [tick, tempo, tick, tempo...] 成对解释成变更表 */
	private int[][] tempoChangesOf(int[] flat) {
		if (flat == null || flat.length == 0) {
			return new int[0][];
		}
		int[][] result = new int[flat.length / 2][2];
		for (int i = 0; i + 1 < flat.length; i += 2) {
			result[i / 2] = new int[]{flat[i], flat[i + 1]};
		}
		return result;
	}

	@Test
	@DisplayName("120 BPM 下四个四分音符：tick 换算成毫秒，音名与音域正确")
	void shouldConvertTicksToMillis() throws Exception {
		Sequence sequence = buildSequence(new Object[][]{
				{60, 0, PPQ}, {62, PPQ, PPQ}, {64, PPQ * 2, PPQ}, {65, PPQ * 3, PPQ},
		}, new int[]{0, 500_000}, new int[]{4, 2});

		ParsedSong song = parser.parseSequence(sequence, "音阶.mid");

		assertEquals(4, song.noteCount());
		assertEquals(120, song.tempoBpm(), "500000 微秒/四分音符 = 120 BPM");
		assertEquals("4/4", song.timeSignature());
		assertEquals("C4", PitchNames.name(song.notes().get(0).pitch()));
		assertEquals(0, song.notes().get(0).startMs());
		assertEquals(500, song.notes().get(1).startMs(), "每拍 500ms");
		assertEquals(2000, song.durationMs(), "四个四分音符共两秒");
		assertEquals(60, song.pitchLow());
		assertEquals(65, song.pitchHigh());
	}

	@Test
	@DisplayName("中途变速：第二段按新速度换算，而不是一路用开头 tempo 平推")
	void shouldRespectTempoChanges() throws Exception {
		// 前两拍 120 BPM（各 500ms），第 2 拍（tick 960）起变 60 BPM（每拍 1000ms）
		Sequence sequence = buildSequence(new Object[][]{
				{60, 0, PPQ}, {62, PPQ, PPQ}, {64, PPQ * 2, PPQ}, {65, PPQ * 3, PPQ},
		}, new int[]{0, 500_000, PPQ * 2, 1_000_000}, new int[]{4, 2});

		ParsedSong song = parser.parseSequence(sequence, "变速.mid");

		assertEquals(0, song.notes().get(0).startMs());
		assertEquals(500, song.notes().get(1).startMs());
		assertEquals(1000, song.notes().get(2).startMs(), "tick 960 之前的时长仍按 120 BPM 算");
		assertEquals(2000, song.notes().get(3).startMs(), "变速之后的一拍变成 1000ms");
	}

	@Test
	@DisplayName("力度 0 的 Note On 当作松开：否则整首歌会没有一个音符有时长")
	void shouldTreatZeroVelocityNoteOnAsNoteOff() throws Exception {
		Sequence sequence = new Sequence(Sequence.PPQ, PPQ);
		Track track = sequence.createTrack();
		track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100), 0));
		// 很多导出工具用「力度 0 的按下」表示松开
		track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 0), PPQ));

		ParsedSong song = parser.parseSequence(sequence, "力度0.mid");

		assertEquals(1, song.noteCount());
		assertEquals(500, song.notes().get(0).durationMs());
	}

	@Test
	@DisplayName("同音重叠：松开事件按后进先出配对（栈），时长不会串成一条")
	void shouldPairOverlappingSameNotes() throws Exception {
		Sequence sequence = new Sequence(Sequence.PPQ, PPQ);
		Track track = sequence.createTrack();
		track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 90), 0));
		track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 90), PPQ / 2));
		track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, 0), PPQ));
		track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, 0), PPQ * 2));

		ParsedSong song = parser.parseSequence(sequence, "重叠.mid");

		// 同一音高本来就只可能有一个音在响：第一次松开停掉的是最后一次按下，
		// 所以「后按下的那次」时长 250ms，「先按下的那次」一直响到最后一个松开点，共 1000ms。
		// 列表按起始时间排序，先按下的是第 0 个。
		assertEquals(2, song.noteCount());
		assertEquals(1000, song.notes().get(0).durationMs(), "先按下的那次响到最后一个松开点");
		assertEquals(250, song.notes().get(1).durationMs(), "后按下的那次在第一个松开点结束");
	}

	@Test
	@DisplayName("有按下没松开：按最后一个事件收尾，不丢音符")
	void shouldCloseUnfinishedNotes() throws Exception {
		Sequence sequence = new Sequence(Sequence.PPQ, PPQ);
		Track track = sequence.createTrack();
		track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 67, 90), 0));
		track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, 67, 0), PPQ));

		ParsedSong song = parser.parseSequence(sequence, "截断.mid");

		assertEquals(1, song.noteCount());
		assertTrue(song.notes().get(0).durationMs() > 0);
	}

	@Test
	@DisplayName("解析字节流：真的写出一份 MIDI 文件再读回来")
	void shouldParseFromBytes() throws Exception {
		Sequence sequence = buildSequence(new Object[][]{{60, 0, PPQ}}, new int[]{0, 500_000}, new int[]{4, 2});
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		MidiSystem.write(sequence, 1, buffer);

		ParsedSong song = parser.parse(buffer.toByteArray(), "落盘.mid");

		assertEquals("MIDI", song.sourceType());
		assertEquals(1, song.noteCount());
		assertFalse(song.notes().isEmpty());
	}
}
