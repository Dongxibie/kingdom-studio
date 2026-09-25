package com.kingdomstudio.modules.music.parser;

import java.util.Comparator;
import java.util.List;

/**
 * 解析结果：MIDI 与简谱两条入口最后都归一到这里，后面的映射与导出只认这一种结构。
 *
 * @param title         曲子名（文件名或用户填的名字）
 * @param sourceType    MIDI / JIANPU
 * @param sourceRef     来源信息：原始文件名 / 简谱首行
 * @param tempoBpm      速度
 * @param timeSignature 拍号，如 4/4
 * @param durationMs    总时长（毫秒）
 * @param notes         音符列表，已按起始时间排好序
 */
public record ParsedSong(
		String title,
		String sourceType,
		String sourceRef,
		int tempoBpm,
		String timeSignature,
		int durationMs,
		List<Note> notes) {

	/**
	 * 单个音符。
	 *
	 * @param pitch      音高号
	 * @param velocity   力度 1-127
	 * @param startMs    起始毫秒
	 * @param durationMs 持续毫秒
	 * @param trackNo    来源轨道（简谱恒为 0）
	 */
	public record Note(int pitch, int velocity, int startMs, int durationMs, int trackNo) {

		public int endMs() {
			return startMs + durationMs;
		}
	}

	public int noteCount() {
		return notes.size();
	}

	public int pitchLow() {
		return notes.stream().mapToInt(Note::pitch).min().orElse(0);
	}

	public int pitchHigh() {
		return notes.stream().mapToInt(Note::pitch).max().orElse(0);
	}

	/** 音符按起始时间排序，同一时刻的按音高从低到高，保证同样的输入永远得到同样的序列 */
	public static List<Note> sort(List<Note> notes) {
		return notes.stream()
				.sorted(Comparator.comparingInt(Note::startMs).thenComparingInt(Note::pitch))
				.toList();
	}
}
