package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 执行结果。
 *
 * <p>{@code accepted=false} 表示这次请求没有执行，原因写在 {@code message} 里。
 * 本机执行通道未开启时就是这个形态：**如实说明没执行**，而不是返回一个看起来成功的空结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "执行结果")
public class ExecutionResultVO {

	@Schema(description = "模式：PREVIEW / MANUAL / LOCAL")
	private String mode;

	@Schema(description = "模式中文名")
	private String modeLabel;

	@Schema(description = "是否真的执行了")
	private Boolean accepted;

	@Schema(description = "说明：为什么执行 / 为什么不执行")
	private String message;

	@Schema(description = "将要（或已经）发出的命令条数")
	private Integer commandCount;

	@Schema(description = "计划总时长（ms）")
	private Integer duration;

	@Schema(description = "按时间排序的命令流（PREVIEW 下用于逐步核对）")
	private List<Command> commands;

	@Schema(description = "安全约束：这些条件是开真实执行的前提")
	private List<String> safety;

	/** 一条命令：按下或松开 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "一条命令")
	public static class Command {

		private Integer seq;
		private String key;
		private String action;
		private Integer atMs;
		private Integer holdMs;
		private Integer strokeSeq;
	}
}
