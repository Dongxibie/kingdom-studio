package com.kingdomstudio.modules.music.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.music.entity.InstrumentProfile;
import com.kingdomstudio.modules.music.parser.ParsedSong;
import com.kingdomstudio.modules.music.parser.PitchNames;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 乐器映射引擎：把音符序列翻译成「按哪些键、什么时候按」。
 *
 * <p>这是整个 Music Agent 里最要紧的一段逻辑，因为它决定了成品能不能照着弹出来。
 * 三条规则：
 *
 * <ol>
 *   <li><b>键与音的对应关系不是简单的等差数列</b>。半音排列（CHROMATIC）里相邻键差一个半音；
 *       而像虚拟乐器那种只有白键的排列（DIATONIC），相邻键差的是一个音级 ——
 *       C 后面直接是 D，没有 C#。所以这里按 mappingMode + scale 展开键位表，
 *       而不是拿音高号减基准音高直接当下标。</li>
 *   <li><b>音域不够时要明确怎么办</b>，而不是悄悄丢音符：
 *       SKIP 跳过并如实记下来；NEAREST 就近落到音阶音上（标记为已调整）；
 *       SHIFT_OCTAVE 整段挪八度，尽量让旋律完整。</li>
 *   <li><b>同时按下的音要归成一组</b>，因为最终是给一只手弹的：
 *       同一时刻的多个音在时间线上是一列，导出时也要写在同一行。</li>
 * </ol>
 */
@Service
public class InstrumentMappingService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/** 大调音阶：1 2 3 4 5 6 7 相对主音的半音数 */
	private static final int[] SCALE_MAJOR = {0, 2, 4, 5, 7, 9, 11};

	/** 小调音阶（自然小调）：1 2 b3 4 5 b6 b7 */
	private static final int[] SCALE_MINOR = {0, 2, 3, 5, 7, 8, 10};

	/** 五声音阶：1 2 3 5 6 */
	private static final int[] SCALE_PENTATONIC = {0, 2, 4, 7, 9};

	/** 半音阶：一个八度十二个音全要 */
	private static final int[] SCALE_CHROMATIC = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

	/**
	 * 展开键位表：每个按键对应一个音高号。
	 *
	 * <p>返回的列表下标就是「第几个键」，元素是该键发声的音高。
	 * 半音排列直接 base + 下标；音阶排列按音阶音程循环，跨过八度时补 12。
	 */
	public List<Integer> expandKeyPitches(InstrumentProfile profile) {
		List<String> keys = parseLayout(profile.getKeyLayout());
		int[] scale = scaleOf(profile.getScale());
		int base = profile.getBasePitch() == null ? 60 : profile.getBasePitch();
		List<Integer> pitches = new ArrayList<>(keys.size());
		for (int index = 0; index < keys.size(); index++) {
			int octave = index / scale.length;
			int step = index % scale.length;
			pitches.add(base + octave * PitchNames.SEMITONES_PER_OCTAVE + scale[step]);
		}
		return pitches;
	}

	/**
	 * 映射一首曲子。
	 *
	 * @param song     已解析的音符序列
	 * @param profile  目标乐器档案
	 * @param override 覆盖档案里的超范围策略；null 表示用档案自己的设置
	 */
	public KeySequenceVO map(ParsedSong song, InstrumentProfile profile, String override) {
		List<String> keys = parseLayout(profile.getKeyLayout());
		if (keys.isEmpty()) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "乐器档案「" + profile.getName() + "」没有配置任何按键");
		}
		List<Integer> keyPitches = expandKeyPitches(profile);
		int transpose = (profile.getTranspose() == null ? 0 : profile.getTranspose())
				+ 12 * (profile.getOctaveShift() == null ? 0 : profile.getOctaveShift());
		String strategy = override != null && !override.isBlank()
				? override.toUpperCase(java.util.Locale.ROOT)
				: (profile.getUnmappedStrategy() == null ? "SKIP" : profile.getUnmappedStrategy());

		List<KeySequenceVO.Stroke> strokes = new ArrayList<>();
		List<KeySequenceVO.Unmapped> unmapped = new ArrayList<>();
		int adjusted = 0;

		ParsedSong.Note[] notes = song.notes().toArray(new ParsedSong.Note[0]);
		int index = 0;
		int seq = 1;
		while (index < notes.length) {
			// 同一时刻起音的音符算作一组「同时按下」
			int groupEnd = index + 1;
			while (groupEnd < notes.length && notes[groupEnd].startMs() == notes[index].startMs()) {
				groupEnd++;
			}
			List<String> groupKeys = new ArrayList<>();
			List<String> groupNoteNames = new ArrayList<>();
			boolean groupAdjusted = false;
			int durationMs = notes[index].durationMs();
			for (int cursor = index; cursor < groupEnd; cursor++) {
				ParsedSong.Note note = notes[cursor];
				durationMs = Math.max(durationMs, note.durationMs());
				int target = note.pitch() + transpose;
				Placement placement = place(target, keyPitches, strategy);
				if (placement == null) {
					unmapped.add(KeySequenceVO.Unmapped.builder()
							.pitch(note.pitch())
							.noteName(PitchNames.name(note.pitch()))
							.startMs(note.startMs())
							.reason(rangeReason(target, keyPitches, strategy))
							.build());
					continue;
				}
				groupKeys.add(keys.get(placement.index()));
				groupNoteNames.add(PitchNames.name(note.pitch())
						+ (placement.adjusted() ? "→" + PitchNames.name(placement.pitch()) : ""));
				groupAdjusted = groupAdjusted || placement.adjusted();
			}
			if (!groupKeys.isEmpty()) {
				strokes.add(KeySequenceVO.Stroke.builder()
						.seq(seq++)
						.startMs(notes[index].startMs())
						.durationMs(durationMs)
						.keys(groupKeys)
						.noteNames(groupNoteNames)
						.originalPitches(groupAdjusted ? originalPitches(notes, index, groupEnd) : null)
						.chord(groupKeys.size() > 1)
						.adjusted(groupAdjusted)
						.build());
				if (groupAdjusted) {
					adjusted++;
				}
			}
			index = groupEnd;
		}

		return KeySequenceVO.builder()
				.taskId(null)
				.taskName(song.title())
				.profileId(profile.getId())
				.profileName(profile.getName())
				.strategy(strategy)
				.keyCount(keys.size())
				.keyLayout(keys)
				.keyPitches(keyPitches)
				.noteCount(song.noteCount())
				.strokes(strokes)
				.unmapped(unmapped)
				.mappedCount(strokes.stream().mapToInt(stroke -> stroke.getKeys().size()).sum())
				.unmappedCount(unmapped.size())
				.adjustedCount(adjusted)
				.exportText(buildExportText(song, keys, strokes, unmapped, profile, strategy))
				.build();
	}

	/** 一个音落在第几个键上 */
	private record Placement(int index, int pitch, boolean adjusted) {
	}

	/**
	 * 把音高放到键位上。
	 *
	 * @return null 表示这个音按当前策略无法落键（调用方需如实记进未映射清单）
	 */
	private Placement place(int pitch, List<Integer> keyPitches, String strategy) {
		for (int index = 0; index < keyPitches.size(); index++) {
			if (keyPitches.get(index) == pitch) {
				return new Placement(index, pitch, false);
			}
		}
		return switch (strategy) {
			case "NEAREST" -> {
				int bestIndex = 0;
				int bestDistance = Integer.MAX_VALUE;
				for (int index = 0; index < keyPitches.size(); index++) {
					int distance = Math.abs(keyPitches.get(index) - pitch);
					// 距离相同时取更低的那个键：旋律往下靠比往上靠更不容易突兀
					if (distance < bestDistance) {
						bestDistance = distance;
						bestIndex = index;
					}
				}
				yield new Placement(bestIndex, keyPitches.get(bestIndex), true);
			}
			case "SHIFT_OCTAVE" -> {
				int low = keyPitches.get(0);
				int high = keyPitches.get(keyPitches.size() - 1);
				int candidate = pitch;
				int guard = 0;
				while ((candidate < low || candidate > high) && guard++ < 12) {
					candidate += candidate < low ? PitchNames.SEMITONES_PER_OCTAVE : -PitchNames.SEMITONES_PER_OCTAVE;
				}
				if (candidate < low || candidate > high) {
					yield null;
				}
				int matched = keyPitches.indexOf(candidate);
				if (matched < 0) {
					// 挪到八度内但那个音不在这套键上（音阶排列常见）：再按就近处理
					int bestIndex = 0;
					int bestDistance = Integer.MAX_VALUE;
					for (int index = 0; index < keyPitches.size(); index++) {
						int distance = Math.abs(keyPitches.get(index) - candidate);
						if (distance < bestDistance) {
							bestDistance = distance;
							bestIndex = index;
						}
					}
					yield new Placement(bestIndex, keyPitches.get(bestIndex), true);
				}
				yield new Placement(matched, candidate, true);
			}
			default -> null;
		};
	}

	private String rangeReason(int pitch, List<Integer> keyPitches, String strategy) {
		int low = keyPitches.get(0);
		int high = keyPitches.get(keyPitches.size() - 1);
		String position = pitch < low ? "低于最低键" : "高于最高键";
		return position + "（本档案音域 " + PitchNames.name(low) + "–" + PitchNames.name(high)
				+ "，策略为" + strategyName(strategy) + "）";
	}

	private String strategyName(String strategy) {
		return switch (strategy) {
			case "NEAREST" -> "就近落键";
			case "SHIFT_OCTAVE" -> "移八度";
			default -> "跳过";
		};
	}

	private List<Integer> originalPitches(ParsedSong.Note[] notes, int from, int to) {
		List<Integer> pitches = new ArrayList<>();
		for (int cursor = from; cursor < to; cursor++) {
			pitches.add(notes[cursor].pitch());
		}
		return pitches;
	}

	/** 导出文本：一行一组按键，直接照着敲就行 */
	private String buildExportText(ParsedSong song, List<String> keys, List<KeySequenceVO.Stroke> strokes,
			List<KeySequenceVO.Unmapped> unmapped, InstrumentProfile profile, String strategy) {
		StringBuilder builder = new StringBuilder();
		builder.append("# ").append(song.title()).append(" · ").append(profile.getName()).append('\n');
		builder.append("# 速度 ").append(song.tempoBpm()).append(" BPM · 拍号 ").append(song.timeSignature())
				.append(" · 音符 ").append(song.noteCount()).append(" 个\n");
		builder.append("# 键位：");
		for (int index = 0; index < keys.size(); index++) {
			builder.append(keys.get(index)).append('=').append(PitchNames.name(expandKeyPitches(profile).get(index)));
			if (index < keys.size() - 1) {
				builder.append(' ');
			}
		}
		builder.append('\n');
		builder.append("# 超范围策略：").append(strategyName(strategy)).append("\n\n");
		for (KeySequenceVO.Stroke stroke : strokes) {
			builder.append(formatTime(stroke.getStartMs())).append("  ")
					.append(String.join(" + ", stroke.getKeys()));
			if (stroke.getNoteNames() != null && !stroke.getNoteNames().isEmpty()) {
				builder.append("    (").append(String.join(" ", stroke.getNoteNames())).append(')');
			}
			builder.append('\n');
		}
		if (!unmapped.isEmpty()) {
			builder.append("\n# 未能落键的音（").append(unmapped.size()).append(" 个）\n");
			for (KeySequenceVO.Unmapped item : unmapped) {
				builder.append("# ").append(formatTime(item.getStartMs())).append("  ")
						.append(item.getNoteName()).append("  —— ").append(item.getReason()).append('\n');
			}
		}
		return builder.toString();
	}

	private String formatTime(int millis) {
		int totalSeconds = millis / 1000;
		return String.format("%d:%02d.%03d", totalSeconds / 60, totalSeconds % 60, millis % 1000);
	}

	/** 解析按键 JSON；坏数据直接报错，不静默当成空档案 */
	public List<String> parseLayout(String keyLayout) {
		if (keyLayout == null || keyLayout.isBlank()) {
			return List.of();
		}
		try {
			List<String> keys = OBJECT_MAPPER.readValue(keyLayout, new TypeReference<List<String>>() {
			});
			return keys.stream().filter(key -> key != null && !key.isBlank()).map(String::trim).toList();
		} catch (Exception e) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "按键配置不是合法的 JSON 数组：" + keyLayout);
		}
	}

	private int[] scaleOf(String scale) {
		if (scale == null) {
			return SCALE_MAJOR;
		}
		return switch (scale.toUpperCase(java.util.Locale.ROOT)) {
			case "MINOR" -> SCALE_MINOR;
			case "PENTATONIC" -> SCALE_PENTATONIC;
			case "CHROMATIC" -> SCALE_CHROMATIC;
			default -> SCALE_MAJOR;
		};
	}
}
