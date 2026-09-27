package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 执行监视器要显示的全部信息 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "本机演奏状态")
public class ExecutionStatusVO {

	private Long taskId;
	private String taskName;
	private String profileName;

	@Schema(description = "状态：READY / RUNNING / PAUSED / STOPPED / FINISHED")
	private String status;

	@Schema(description = "状态中文名")
	private String statusLabel;

	@Schema(description = "进度百分比 0-100")
	private Integer progress;

	@Schema(description = "当前（最近一条）按键")
	private String currentKey;

	private Integer executedCount;
	private Integer commandCount;

	@Schema(description = "已用时间（ms，不含暂停时间）")
	private Integer elapsedMs;

	@Schema(description = "计划总时长（ms）")
	private Integer duration;

	@Schema(description = "剩余时间（ms）")
	private Integer remainingMs;

	@Schema(description = "当前按住的键（正常应为空）")
	private List<String> heldKeys;

	@Schema(description = "用户确认过的目标窗口")
	private String targetWindow;

	@Schema(description = "此刻的前台窗口")
	private String currentWindow;

	@Schema(description = "窗口守护是否在线")
	private Boolean guardAlive;

	@Schema(description = "注入方式说明")
	private String injector;

	@Schema(description = "过程中产生的提示：自动暂停、异常退出等")
	private List<String> warnings;

	@Schema(description = "停止原因")
	private String stopReason;

	private LocalDateTime startedAt;
}
