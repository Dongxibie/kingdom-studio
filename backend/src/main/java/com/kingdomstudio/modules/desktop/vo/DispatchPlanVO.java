package com.kingdomstudio.modules.desktop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 派发计划：本次「如果执行会按下什么」的完整清单。
 *
 * <p>注意用词是「计划」而不是「已执行」：本期不做真实系统输入，
 * 返回的每条命令都只是待发送的消息体，前端也按模拟展示。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "桌面代理派发计划（模拟）")
public class DispatchPlanVO {

	@Schema(description = "本次派发的会话 id（模拟，仅用于日志串联）")
	private String sessionId;

	@Schema(description = "运行模式：MOCK / LIVE")
	private String mode;

	private Long taskId;

	private String taskName;

	private Long profileId;

	private String profileName;

	@Schema(description = "曲目总时长（毫秒）")
	private Integer durationMs;

	@Schema(description = "接受并排入计划的命令数")
	private Integer accepted;

	@Schema(description = "被规则拦下或调整的地方，逐条说明原因")
	private List<String> warnings;

	@Schema(description = "待发送的按键命令，按时间排序")
	private List<KeyCommandVO> commands;

	@Schema(description = "说明：为什么现在是模拟、真实执行需要什么前提")
	private List<String> notes;
}
