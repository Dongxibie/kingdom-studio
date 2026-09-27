package com.kingdomstudio.modules.music.execution;

import com.kingdomstudio.modules.music.vo.ExecutionResultVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 本机演奏适配器。
 *
 * <p>它是「一次派发」语义里的 LOCAL 入口，但**真正的执行是有状态的会话**：
 * 开始、暂停、继续、停止、看状态都由 {@link ExecutionSession} 与 ExecutionService 承载，
 * 所以这个适配器不自己发按键，而是把「本机执行要开会话」这件事说清楚——
 * 这样从宏导出的 {@code /execute} 接口误触 LOCAL 时不会静默什么都不做。
 */
@Component
public class LocalExecutionAdapter implements ExecutionAdapter {

	@Override
	public String mode() {
		return "LOCAL";
	}

	@Override
	public String label() {
		return "本机演奏";
	}

	/** 会话接口可用；真正的开关在 ExecutionService 里逐次判断 */
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
				.accepted(false)
				.message("本机演奏是有状态的会话：请用演奏控制面板开始（会先做环境自检并要求确认目标窗口），"
						+ "不要从这个一次性的派发接口启动。")
				.commandCount(context.commands().size())
				.duration(context.duration())
				.commands(List.of())
				.safety(PreviewExecutionAdapter.SAFETY)
				.build();
	}
}
