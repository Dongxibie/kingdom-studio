package com.kingdomstudio.modules.music.execution;

import com.kingdomstudio.modules.music.vo.ExecutionResultVO;

import java.util.List;

/**
 * 执行通道抽象：把「谁去发按键」隔离成一层。
 *
 * <p>三种模式各有自己的适配器，{@link #mode()} 是它们的标识：
 * <ul>
 *   <li>{@code PREVIEW} 仅模拟：列命令，不产生输入；</li>
 *   <li>{@code MANUAL} 逐条确认：依赖本机执行通道，当前未开启；</li>
 *   <li>{@code LOCAL} 本机演奏：有状态的会话（开始 / 暂停 / 停止 / 状态），
 *       由 {@link ExecutionService} 管理，见 {@link LocalExecutionAdapter}。</li>
 * </ul>
 *
 * <p>接口刻意只描述「一次派发」这种无状态动作；有状态的本机演奏另外走会话接口，
 * 两种语义不混在一个方法里。
 */
public interface ExecutionAdapter {

	/** 模式标识：PREVIEW / MANUAL / LOCAL */
	String mode();

	/** 模式中文名 */
	String label();

	/** 这个模式当前能不能真的执行 */
	boolean available();

	/** 不能执行时的原因（available 为 true 时返回空串） */
	String reason();

	/** 执行（或模拟执行）一次派发 */
	ExecutionResultVO execute(PlanContext context);

	/** 交给执行层的东西：计划头信息 + 已经校验、已按时间排好序的命令流 */
	record PlanContext(Long planId, Long taskId, String taskName, String profileName,
			Integer duration, List<ExecutionResultVO.Command> commands) {
	}
}
