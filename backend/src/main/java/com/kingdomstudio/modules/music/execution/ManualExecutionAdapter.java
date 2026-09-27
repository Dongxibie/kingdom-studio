package com.kingdomstudio.modules.music.execution;

import com.kingdomstudio.modules.music.vo.ExecutionResultVO;
import org.springframework.stereotype.Component;

import java.util.List;

/** 逐条确认：依赖本机执行通道，当前未开启 */
@Component
public class ManualExecutionAdapter implements ExecutionAdapter {

	@Override
	public String mode() {
		return "MANUAL";
	}

	@Override
	public String label() {
		return "逐条确认";
	}

	@Override
	public boolean available() {
		return false;
	}

	@Override
	public String reason() {
		return "逐条确认模式还没做：目前只提供「仅模拟」与「本机演奏」两种；"
				+ "需要逐条核对时，可以先用仅模拟把命令流看一遍。";
	}

	@Override
	public ExecutionResultVO execute(PlanContext context) {
		return ExecutionResultVO.builder()
				.mode(mode())
				.modeLabel(label())
				.accepted(false)
				.message(reason())
				.commandCount(context.commands().size())
				.duration(context.duration())
				.commands(List.of())
				.safety(PreviewExecutionAdapter.SAFETY)
				.build();
	}
}
