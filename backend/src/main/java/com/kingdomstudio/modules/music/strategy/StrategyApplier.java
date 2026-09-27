package com.kingdomstudio.modules.music.strategy;

import com.kingdomstudio.modules.music.parser.ParsedSong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 策略应用器：把一首曲子按策略改写成一份「演奏方案」，并逐条说明改了什么、为什么。
 *
 * <p>三条硬规矩：
 * <ol>
 *   <li><b>只动演奏方式，不动风格</b>：音高序列在标准与展示模式下原样保留，
 *       初学模式才允许删繁就简，且删的每一条都要写理由；</li>
 *   <li><b>改动可解释</b>：每次调整都产出一条 {@link Adjustment}，界面直接拿它当「AI 解释」展示；</li>
 *   <li><b>不新增音</b>：任何模式下都不会凭空多出音符 —— 这是「不生成音乐」的底线。</li>
 * </ol>
 */
@Slf4j
@Component
public class StrategyApplier {

	/** 初学模式：速度上限（相对原曲的倍率） */
	private static final double BEGINNER_TEMPO_FACTOR = 0.8;

	/** 初学模式：慢于这个速度就不必再降了（本来就够慢了，再降只会拖沓） */
	private static final int BEGINNER_TEMPO_FLOOR_BPM = 90;

	/** 初学模式：降速后的速度下限，太慢会失去乐句感 */
	private static final int BEGINNER_TEMPO_MIN_BPM = 72;

	/** 初学模式：同一个时刻附近的音算作装饰音，超过这个数量就精简（毫秒窗口） */
	private static final int BEGINNER_CLUSTER_WINDOW_MS = 70;

	/** 初学模式：短于这个时值的音会被合并到前一个音（毫秒） */
	private static final int BEGINNER_MIN_NOTE_MS = 120;

	/** 初学模式：旋律被压到的音域半径（半音），超出部分移八度靠近 */
	private static final int BEGINNER_RANGE_RADIUS = 7;

	/** 展示模式：重拍的力度提升比例 */
	private static final double SHOWCASE_ACCENT_FACTOR = 1.25;

	/** 展示模式：长音延长比例，让乐句有呼吸（不改音高） */
	private static final double SHOWCASE_BREATH_FACTOR = 1.08;

	/** 一份演奏方案 */
	public record Result(ParsedSong song, List<Adjustment> adjustments, String summary) {
	}

	/** 一条调整：改了什么 + 为什么 */
	public record Adjustment(String type, String title, String detail, String before, String after) {
	}

	public Result apply(ParsedSong song, PerformanceStrategy strategy) {
		if (song == null || song.notes() == null || song.notes().isEmpty()) {
			return new Result(song, List.of(), "这首曲子里没有音符，不需要调整。");
		}
		return switch (strategy) {
			case BEGINNER -> beginner(song);
			case SHOWCASE -> showcase(song);
			default -> new Result(song, List.of(), "标准模式：保持原曲，不做任何改动。");
		};
	}

	/** 初学模式：先删装饰音与过短音，收窄跨度，最后整体降速 */
	private Result beginner(ParsedSong song) {
		List<Adjustment> adjustments = new ArrayList<>();
		List<ParsedSong.Note> notes = new ArrayList<>(song.notes());

		// 1) 同一时刻挤在一起的和弦/装饰音只留最低的一个音：初学时同时按键最难
		List<ParsedSong.Note> trimmed = new ArrayList<>();
		int removedCluster = 0;
		for (ParsedSong.Note note : notes) {
			ParsedSong.Note last = trimmed.isEmpty() ? null : trimmed.get(trimmed.size() - 1);
			if (last != null && Math.abs(note.startMs() - last.startMs()) <= BEGINNER_CLUSTER_WINDOW_MS) {
				removedCluster++;
				continue;
			}
			trimmed.add(note);
		}
		if (removedCluster > 0) {
			adjustments.add(new Adjustment("SIMPLIFY", "精简同时按下的音",
					"有 " + removedCluster + " 个音与前一个音几乎同时出现（相差不超过 "
							+ BEGINNER_CLUSTER_WINDOW_MS + "ms）：初学阶段同时按键最吃力，这里只保留一个，旋律骨架不变。",
					noteCount(notes), noteCount(trimmed)));
		}

		// 2) 太短的音合并进前一个音：快速经过句先不追求
		List<ParsedSong.Note> merged = new ArrayList<>();
		int mergedShort = 0;
		for (ParsedSong.Note note : trimmed) {
			ParsedSong.Note last = merged.isEmpty() ? null : merged.get(merged.size() - 1);
			if (last != null && note.durationMs() < BEGINNER_MIN_NOTE_MS
					&& note.startMs() - last.startMs() <= BEGINNER_MIN_NOTE_MS * 2) {
				merged.set(merged.size() - 1, new ParsedSong.Note(last.pitch(), last.velocity(),
						last.startMs(), Math.max(last.durationMs(), note.endMs() - last.startMs()), last.trackNo()));
				mergedShort++;
				continue;
			}
			merged.add(note);
		}
		if (mergedShort > 0) {
			adjustments.add(new Adjustment("SIMPLIFY", "合并过短的音",
					"有 " + mergedShort + " 个音的时值短于 " + BEGINNER_MIN_NOTE_MS
							+ "ms：这类快速经过句在初学阶段最容易糊过去，先合并进前一个音，节奏更从容。",
					noteCount(trimmed), noteCount(merged)));
		}

		// 3) 跨度过大：把偏离中心音太远的音移八度靠近（音名不变，只是换八度）
		int center = medianPitch(merged);
		List<ParsedSong.Note> narrowed = new ArrayList<>();
		int shifted = 0;
		for (ParsedSong.Note note : merged) {
			int pitch = note.pitch();
			int moved = pitch;
			while (moved - center > BEGINNER_RANGE_RADIUS) {
				moved -= 12;
			}
			while (center - moved > BEGINNER_RANGE_RADIUS) {
				moved += 12;
			}
			if (moved != pitch) {
				shifted++;
			}
			narrowed.add(new ParsedSong.Note(moved, note.velocity(), note.startMs(), note.durationMs(), note.trackNo()));
		}
		if (shifted > 0) {
			adjustments.add(new Adjustment("OCTAVE", "收窄音域跨度",
					"有 " + shifted + " 个音离中心音（" + center + "）超过 " + BEGINNER_RANGE_RADIUS
							+ " 个半音：把它们移八度靠近中心，音名不变、旋律照样认得出，但手不用来回大跳。",
					"音域半径 > " + BEGINNER_RANGE_RADIUS, "音域半径 ≤ " + BEGINNER_RANGE_RADIUS));
		}

		// 4) 降速：原速偏快才降（本来就慢的曲子再降只会拖沓）
		int tempo = song.tempoBpm();
		int newTempo = tempo;
		if (tempo > BEGINNER_TEMPO_FLOOR_BPM) {
			// 降到 0.8 倍，但不再低于下限：初学模式总要给出一个比原曲更从容的速度
			newTempo = Math.max(BEGINNER_TEMPO_MIN_BPM, (int) Math.round(tempo * BEGINNER_TEMPO_FACTOR));
		}
		if (newTempo < tempo) {
			adjustments.add(new Adjustment("TEMPO", "降低速度",
					"原速 " + tempo + " BPM 对初学偏快，按 " + Math.round(BEGINNER_TEMPO_FACTOR * 100)
							+ "% 降到 " + newTempo + " BPM（不低于 " + BEGINNER_TEMPO_MIN_BPM
							+ " BPM，再慢会失去乐句感）；音符时值同比拉长，听觉上的节奏比例不变。",
					tempo + " BPM", newTempo + " BPM"));
		}
		double scale = tempo == 0 ? 1 : (double) newTempo / tempo;
		List<ParsedSong.Note> scaled = narrowed.stream()
				.map(note -> new ParsedSong.Note(note.pitch(), note.velocity(),
						(int) Math.round(note.startMs() / scale),
						(int) Math.round(note.durationMs() / scale), note.trackNo()))
				.toList();

		ParsedSong result = new ParsedSong(song.title(), song.sourceType(), song.sourceRef(),
				newTempo, song.timeSignature(), (int) Math.round(song.durationMs() / scale), scaled);
		String summary = adjustments.isEmpty()
				? "这首曲子本来就很适合初学：没有同时按下的音、没有过快经过句、跨度也在一个八度内，速度保持不变。"
				: "初学模式：" + String.join("；", adjustments.stream().map(Adjustment::title).toList()) + "。";
		return new Result(result, adjustments, summary);
	}

	/** 展示模式：音高与音符数一律不动，只调力度与呼吸 */
	private Result showcase(ParsedSong song) {
		List<Adjustment> adjustments = new ArrayList<>();
		List<ParsedSong.Note> notes = new ArrayList<>(song.notes());
		int beatMs = song.tempoBpm() == 0 ? 500 : (int) Math.round(60_000.0 / song.tempoBpm());
		int accented = 0;
		int breathed = 0;
		List<ParsedSong.Note> enhanced = new ArrayList<>(notes.size());
		for (ParsedSong.Note note : notes) {
			int velocity = note.velocity();
			if (note.startMs() % (beatMs * 4) < beatMs / 2) {
				velocity = (int) Math.min(127, Math.round(velocity * SHOWCASE_ACCENT_FACTOR));
				accented++;
			}
			int duration = note.durationMs();
			if (duration >= beatMs) {
				duration = (int) Math.round(duration * SHOWCASE_BREATH_FACTOR);
				breathed++;
			}
			enhanced.add(new ParsedSong.Note(note.pitch(), velocity, note.startMs(), duration, note.trackNo()));
		}
		if (accented > 0) {
			adjustments.add(new Adjustment("ACCENT", "强化重拍",
					"每小节第一个音（共 " + accented + " 个）力度提到 " + Math.round(SHOWCASE_ACCENT_FACTOR * 100)
							+ "%，让小节的骨架听得出来；音高与时值都没动。",
					"力度 ×1.0", "重拍力度 ×" + SHOWCASE_ACCENT_FACTOR));
		}
		if (breathed > 0) {
			adjustments.add(new Adjustment("BREATH", "拉长乐句呼吸",
					"有 " + breathed + " 个音长于或等于一拍，时值延长 " + Math.round(SHOWCASE_BREATH_FACTOR * 100)
							+ "%，句尾更收得住；不新增任何音符。",
					"时值 ×1.0", "长音时值 ×" + SHOWCASE_BREATH_FACTOR));
		}
		ParsedSong result = new ParsedSong(song.title(), song.sourceType(), song.sourceRef(),
				song.tempoBpm(), song.timeSignature(), song.durationMs(), List.copyOf(enhanced));
		String summary = adjustments.isEmpty()
				? "展示模式：这首曲子的力度与时长本来就比较饱满，没有可加强的地方。"
				: "展示模式：" + String.join("；", adjustments.stream().map(Adjustment::title).toList())
						+ "。旋律与音符数量保持原样。";
		return new Result(result, adjustments, summary);
	}

	private int medianPitch(List<ParsedSong.Note> notes) {
		List<Integer> pitches = notes.stream().map(ParsedSong.Note::pitch).sorted(Comparator.naturalOrder()).toList();
		return pitches.get(pitches.size() / 2);
	}

	private String noteCount(List<ParsedSong.Note> notes) {
		return notes.size() + " 个音";
	}
}
