package com.kingdomstudio.modules.music.parser;

import org.springframework.stereotype.Component;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MIDI 解析：把 .mid 文件读成统一的音符序列。
 *
 * <p>用 JDK 自带的 {@code javax.sound.midi}，不引第三方库 —— 只需要读事件，
 * 不需要音源与合成，标准库够用且不增加依赖。
 *
 * <p>三件容易做错的事，这里都处理了：
 * <ol>
 *   <li><b>tick → 毫秒</b>：MIDI 的时间单位是 tick，换算依赖每拍分辨率（PPQ）与 tempo，
 *       而且 tempo 可以中途改变。这里先把文件里所有 tempo 变更收集成一张表，再逐段换算。</li>
 *   <li><b>Note On 力度 0 等于 Note Off</b>：很多导出工具用「力度 0 的按下」表示松开，
 *       只认 NOTE_OFF 会漏掉一半音符，让整首歌变得没有时长。</li>
 *   <li><b>同音重叠</b>：同一音高连续两次按下时，松开事件是按「后进先出」配对的，
 *       用栈（Deque）而不是单个变量，避免时长串在一起。</li>
 * </ol>
 *
 * <p>纯函数式实现（不碰数据库、不碰 Spring 容器），便于单元测试直接构造字节流验证。
 */
@Component
public class MidiParser {

	/** MIDI 默认每拍 500000 微秒 = 120 BPM */
	private static final int DEFAULT_TEMPO_US_PER_QUARTER = 500_000;

	/** 元事件类型：速度 */
	private static final int META_TEMPO = 0x51;

	/** 元事件类型：拍号 */
	private static final int META_TIME_SIGNATURE = 0x58;

	public ParsedSong parse(byte[] bytes, String filename) throws InvalidMidiDataException, IOException {
		try (InputStream in = new ByteArrayInputStream(bytes)) {
			Sequence sequence = MidiSystem.getSequence(in);
			return parseSequence(sequence, filename);
		}
	}

	/** 供测试直接用内存里的 Sequence 验证，不必先写成文件 */
	public ParsedSong parseSequence(Sequence sequence, String filename) {
		int ppq = sequence.getResolution();
		// resolution 为负表示 SMPTE 计时（帧/秒），这种文件很少见：按 1 tick = 1 帧近似，至少不崩
		boolean smpte = ppq < 0;
		int ticksPerQuarter = smpte ? Math.abs(ppq) : ppq;

		List<int[]> tempoChanges = new ArrayList<>(); // [tick, usPerQuarter]
		collectTempoChanges(sequence, tempoChanges);
		tempoChanges.sort(Comparator.comparingInt(change -> change[0]));

		String timeSignature = collectTimeSignature(sequence);
		int initialTempo = tempoChanges.isEmpty() ? DEFAULT_TEMPO_US_PER_QUARTER : tempoChanges.get(0)[1];

		List<ParsedSong.Note> notes = new ArrayList<>();
		int endTick = 0;
		Track[] tracks = sequence.getTracks();
		for (int trackIndex = 0; trackIndex < tracks.length; trackIndex++) {
			Track track = tracks[trackIndex];
			Map<Integer, Deque<long[]>> open = new HashMap<>(); // 音高 → 未松开的（tick, velocity）栈
			for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
				MidiEvent event = track.get(eventIndex);
				MidiMessage message = event.getMessage();
				long tick = event.getTick();
				endTick = (int) Math.max(endTick, tick);
				if (!(message instanceof ShortMessage shortMessage)) {
					continue;
				}
				int command = shortMessage.getCommand();
				int pitch = shortMessage.getData1();
				int velocity = shortMessage.getData2();
				boolean isNoteOn = command == ShortMessage.NOTE_ON && velocity > 0;
				boolean isNoteOff = command == ShortMessage.NOTE_OFF
						|| (command == ShortMessage.NOTE_ON && velocity == 0);
				if (isNoteOn) {
					open.computeIfAbsent(pitch, key -> new ArrayDeque<>()).push(new long[]{tick, velocity});
				} else if (isNoteOff) {
					Deque<long[]> stack = open.get(pitch);
					if (stack == null || stack.isEmpty()) {
						continue;
					}
					long[] start = stack.pop();
					int startMs = tickToMillis((int) start[0], ticksPerQuarter, tempoChanges);
					int endMs = tickToMillis((int) tick, ticksPerQuarter, tempoChanges);
					notes.add(new ParsedSong.Note(pitch, (int) start[1], startMs,
							Math.max(1, endMs - startMs), trackIndex));
				}
			}
			// 文件被截断、有按下没松开：按最后一个事件的时刻收尾，总比丢掉这些音好
			for (Map.Entry<Integer, Deque<long[]>> entry : open.entrySet()) {
				for (long[] start : entry.getValue()) {
					int startMs = tickToMillis((int) start[0], ticksPerQuarter, tempoChanges);
					int endMs = tickToMillis(endTick, ticksPerQuarter, tempoChanges);
					notes.add(new ParsedSong.Note(entry.getKey(), (int) start[1], startMs,
							Math.max(1, endMs - startMs), trackIndex));
				}
			}
		}

		List<ParsedSong.Note> sorted = ParsedSong.sort(notes);
		int durationMs = sorted.stream().mapToInt(ParsedSong.Note::endMs).max().orElse(0);
		int bpm = (int) Math.round(60_000_000d / initialTempo);

		return new ParsedSong(
				filename == null || filename.isBlank() ? "未命名 MIDI" : filename,
				"MIDI",
				filename == null ? "" : filename,
				bpm,
				timeSignature,
				durationMs,
				sorted);
	}

	private void collectTempoChanges(Sequence sequence, List<int[]> target) {
		for (Track track : sequence.getTracks()) {
			for (int i = 0; i < track.size(); i++) {
				MidiEvent event = track.get(i);
				MidiMessage message = event.getMessage();
				if (message instanceof MetaMessage meta && meta.getType() == META_TEMPO) {
					byte[] data = meta.getData();
					if (data.length >= 3) {
						int micros = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
						if (micros > 0) {
							target.add(new int[]{(int) event.getTick(), micros});
						}
					}
				}
			}
		}
	}

	private String collectTimeSignature(Sequence sequence) {
		for (Track track : sequence.getTracks()) {
			for (int i = 0; i < track.size(); i++) {
				MidiMessage message = track.get(i).getMessage();
				if (message instanceof MetaMessage meta && meta.getType() == META_TIME_SIGNATURE) {
					byte[] data = meta.getData();
					if (data.length >= 2) {
						int numerator = data[0] & 0xFF;
						int denominator = 1 << (data[1] & 0xFF);
						return numerator + "/" + denominator;
					}
				}
			}
		}
		return "4/4";
	}

	/**
	 * tick → 毫秒：按 tempo 变更分段累加。
	 *
	 * <p>不能简单用「首段 tempo × tick 数」：曲子里常见变速（渐快、变速段落），
	 * 那样算出来的时间轴从变速点开始就整体偏掉。
	 */
	int tickToMillis(int tick, int ticksPerQuarter, List<int[]> tempoChanges) {
		long micros = 0;
		int cursor = 0;
		int currentUsPerQuarter = DEFAULT_TEMPO_US_PER_QUARTER;
		if (!tempoChanges.isEmpty()) {
			currentUsPerQuarter = tempoChanges.get(0)[1];
		}
		for (int[] change : tempoChanges) {
			if (change[0] >= tick) {
				break;
			}
			if (change[0] > cursor) {
				micros += (long) (change[0] - cursor) * currentUsPerQuarter / ticksPerQuarter;
				cursor = change[0];
			}
			currentUsPerQuarter = change[1];
		}
		if (tick > cursor) {
			micros += (long) (tick - cursor) * currentUsPerQuarter / ticksPerQuarter;
		}
		return (int) Math.round(micros / 1000d);
	}
}
