package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 演奏计划。
 *
 * <p>{@code notes} 就是事件流本身（按下 / 松开 + 时间），导出的三种格式都由它生成。
 * 列表接口只返回前若干条，避免整首曲谱把响应撑爆；要完整内容走导出接口。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "演奏计划")
public class PerformancePlanVO {

	private Long id;
	private Long taskId;
	private Long profileId;

	private String taskName;
	private String profileName;
	private String strategy;

	@Schema(description = "整首时长（ms）")
	private Integer duration;

	@Schema(description = "事件数（按下 + 松开）")
	private Integer noteCount;

	@Schema(description = "按键次数（和弦算一次）")
	private Integer strokeCount;

	@Schema(description = "用到的不同按键数量")
	private Integer keyCount;

	@Schema(description = "用到的按键清单")
	private List<String> keys;

	@Schema(description = "命令流校验提示：截断 / 顺延 / 跳过逐条说明")
	private List<String> warnings;

	@Schema(description = "按键事件流（可能被截断，完整内容见导出接口）")
	private List<Note> notes;

	@Schema(description = "事件流是否被截断")
	private Boolean truncated;

	@Schema(description = "可导出的格式")
	private List<String> formats;

	private LocalDateTime createTime;

	/** 一个按键事件：按下或松开 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "按键事件")
	public static class Note {

		@Schema(description = "按键字符")
		private String key;

		@Schema(description = "动作：DOWN 按下 / UP 松开")
		private String action;

		@Schema(description = "相对曲首的毫秒时间")
		private Integer timestamp;

		@Schema(description = "来自第几次按键（和弦的多个键共享同一个 strokeSeq）")
		private Integer strokeSeq;
	}
}
