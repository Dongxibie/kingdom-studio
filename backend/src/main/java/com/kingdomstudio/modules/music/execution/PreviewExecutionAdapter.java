package com.kingdomstudio.modules.music.execution;

import com.kingdomstudio.modules.music.vo.ExecutionResultVO;
import org.springframework.stereotype.Component;

import java.util.List;

/** 仅模拟：把命令流按时间列出来供逐条核对，不产生任何真实输入 */
@Component
public class PreviewExecutionAdapter implements ExecutionAdapter {

	/** 三种模式共用的安全说明 */
	public static final List<String> SAFETY = List.of(
			"不会自动开始：必须由使用者显式触发。",
			"急停随时可用：ESC 立刻停止并松开所有已按下的键。",
			"只对确认过的目标窗口生效：窗口一换就自动暂停。",
			"只发送按键，不移动鼠标、不点击、不读写文件。");

	@Override
	public String mode() {
		return "PREVIEW";
	}

	@Override
	public String label() {
		return "仅模拟";
	}

	@Override
	public boolean available() {
		return true;
	}

	@Override
	public String reason() {
		return "";
	}

	@Override
	public ExecutionResultVO execute(PlanContext context) {
		return ExecutionResultVO.builder()
				.mode(mode())
				.modeLabel(label())
				.accepted(true)
				.message("仅模拟：已按时间列出全部 " + context.commands().size()
						+ " 条命令，本次不会产生任何真实按键输入。")
				.commandCount(context.commands().size())
				.duration(context.duration())
				.commands(context.commands())
				.safety(SAFETY)
				.build();
	}
}
