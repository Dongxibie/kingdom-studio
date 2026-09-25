package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 按键序列：映射引擎的产物，也是「照着弹」的最终依据。
 *
 * <p>三种计数刻意分开：{@code mappedCount} 落键的音、{@code adjustedCount} 被挪过位置的组、
 * {@code unmappedCount} 完全落不下的音。只报一个「成功数」会掩盖「其实弹不出来」的事实。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "按键序列")
public class KeySequenceVO {

	private Long taskId;

	private String taskName;

	private Long profileId;

	private String profileName;

	/** 实际生效的超范围策略 */
	private String strategy;

	private Integer keyCount;

	/** 按键顺序 */
	private List<String> keyLayout;

	/** 每个键对应的音高号，与 keyLayout 一一对应 */
	private List<Integer> keyPitches;

	private Integer noteCount;

	private Integer mappedCount;

	private Integer adjustedCount;

	private Integer unmappedCount;

	/** 按时间排好的按键动作 */
	private List<Stroke> strokes;

	/** 落不下键的音，附原因 */
	private List<Unmapped> unmapped;

	/** 纯文本导出：可以直接复制去练 */
	private String exportText;

	/** 一次按键动作：单个音或一组和弦 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "一次按键")
	public static class Stroke {

		private Integer seq;

		private Integer startMs;

		private Integer durationMs;

		/** 需要同时按下的键 */
		private List<String> keys;

		/** 对应的音名，被调整过的会写成「原音→实际音」 */
		private List<String> noteNames;

		/** 被调整过的组才带原始音高，便于前端提示 */
		private List<Integer> originalPitches;

		private Boolean chord;

		private Boolean adjusted;
	}

	/** 未能落键的音 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "未能落键的音")
	public static class Unmapped {

		private Integer pitch;

		private String noteName;

		private Integer startMs;

		private String reason;
	}
}
