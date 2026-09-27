package com.kingdomstudio.modules.music.service;

import com.kingdomstudio.modules.music.vo.KeySequenceVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 演奏速度变换：把一份按键序列按倍率缩放，或给同键重按留出最小间隔。
 *
 * <p>为什么放在「序列」这一层而不是直接改时间戳：时序的唯一来源是
 * {@code DesktopAgentService.plan(sequence)}，那里才有按住时长、重按间隔、命令数上限等校验。
 * 所以这里只把序列调好，再交给同一条路径去校验 —— 速度 1.5 倍不会绕开最短按住 40ms 的限制。
 *
 * <p>两个变换都在**副本**上做，不改调用方传进来的对象。
 */
public final class PerformanceTempo {

	private PerformanceTempo() {
	}

	/**
	 * 按倍率缩放时间。
	 *
	 * @param scale 1.0 原速；大于 1 更快（每组的起点与按住时长同时缩短）
	 */
	public static KeySequenceVO scale(KeySequenceVO sequence, double scale) {
		if (sequence == null || Math.abs(scale - 1.0) < 0.0001) {
			return sequence;
		}
		double factor = Math.max(0.25, Math.min(4.0, scale));
		List<KeySequenceVO.Stroke> strokes = new ArrayList<>();
		for (KeySequenceVO.Stroke stroke : safeStrokes(sequence)) {
			strokes.add(copy(stroke,
					(int) Math.round(value(stroke.getStartMs()) / factor),
					Math.max(1, (int) Math.round(value(stroke.getDurationMs()) / factor))));
		}
		return rebuild(sequence, strokes, sequence.getUnmapped());
	}

	/**
	 * 给同一批键的连续重按留出最小间隔。
	 *
	 * <p>连按同一个键太快在部分乐器/输入法上会被吞掉或触发重复过滤。判定口径是
	 * **同一个键「上一次松开」到这一次「按下」之间的空档**：不足 {@code minGapMs} 就往后推，
	 * 后面的组跟着顺延。不改按键内容、不改顺序，也不会让两组重叠。
	 *
	 * @param minGapMs 同键两次按下之间的最小间隔，0 表示不处理
	 */
	public static KeySequenceVO applyMinGap(KeySequenceVO sequence, int minGapMs) {
		if (sequence == null || minGapMs <= 0) {
			return sequence;
		}
		List<KeySequenceVO.Stroke> strokes = new ArrayList<>();
		// 记录每个键上一次「松开」的时刻：间隔从上一次松开算起，
		// 所以「还没松开就又按」自然也包含在内（那时的空档是负的）
		Map<String, Integer> lastEnd = new HashMap<>();
		int shift = 0;
		for (KeySequenceVO.Stroke stroke : safeStrokes(sequence)) {
			int start = value(stroke.getStartMs()) + shift;
			int hold = value(stroke.getDurationMs());
			int push = 0;
			for (String key : safeKeys(stroke)) {
				Integer previousEnd = lastEnd.get(key);
				if (previousEnd == null) {
					continue;
				}
				int earliest = previousEnd + minGapMs;
				if (start < earliest) {
					push = Math.max(push, earliest - start);
				}
			}
			if (push > 0) {
				start += push;
				shift += push;
			}
			for (String key : safeKeys(stroke)) {
				lastEnd.put(key, start + hold);
			}
			strokes.add(copy(stroke, start, hold));
		}
		return rebuild(sequence, strokes, sequence.getUnmapped());
	}

	/** 序列的结束时刻：最后一组按键的起点 + 按住时长 */
	public static int durationOf(KeySequenceVO sequence) {
		int end = 0;
		for (KeySequenceVO.Stroke stroke : safeStrokes(sequence)) {
			end = Math.max(end, value(stroke.getStartMs()) + value(stroke.getDurationMs()));
		}
		return end;
	}

	private static KeySequenceVO.Stroke copy(KeySequenceVO.Stroke source, int startMs, int durationMs) {
		KeySequenceVO.Stroke target = new KeySequenceVO.Stroke();
		target.setSeq(source.getSeq());
		target.setStartMs(startMs);
		target.setDurationMs(durationMs);
		target.setKeys(source.getKeys());
		target.setNoteNames(source.getNoteNames());
		target.setOriginalPitches(source.getOriginalPitches());
		target.setChord(source.getChord());
		target.setAdjusted(source.getAdjusted());
		return target;
	}

	private static KeySequenceVO rebuild(KeySequenceVO sequence, List<KeySequenceVO.Stroke> strokes,
			List<KeySequenceVO.Unmapped> unmapped) {
		KeySequenceVO target = new KeySequenceVO();
		target.setTaskId(sequence.getTaskId());
		target.setTaskName(sequence.getTaskName());
		target.setProfileId(sequence.getProfileId());
		target.setProfileName(sequence.getProfileName());
		target.setStrategy(sequence.getStrategy());
		target.setKeyCount(sequence.getKeyCount());
		target.setKeyLayout(sequence.getKeyLayout());
		target.setKeyPitches(sequence.getKeyPitches());
		target.setNoteCount(sequence.getNoteCount());
		target.setMappedCount(sequence.getMappedCount());
		target.setAdjustedCount(sequence.getAdjustedCount());
		target.setUnmappedCount(sequence.getUnmappedCount());
		target.setUnmapped(unmapped);
		target.setStrokes(strokes);
		target.setExportText(sequence.getExportText());
		return target;
	}

	private static List<KeySequenceVO.Stroke> safeStrokes(KeySequenceVO sequence) {
		return sequence.getStrokes() == null ? List.of() : sequence.getStrokes();
	}

	private static List<String> safeKeys(KeySequenceVO.Stroke stroke) {
		return stroke.getKeys() == null ? List.of() : stroke.getKeys();
	}

	private static int value(Integer number) {
		return number == null ? 0 : number;
	}
}
