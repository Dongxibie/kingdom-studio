package com.kingdomstudio.modules.music.macro;

import com.kingdomstudio.modules.music.vo.ExecutionResultVO;

import java.util.List;

/**
 * 本机执行通道的抽象。
 *
 * <p>把「演奏计划」交给谁去发按键，这一步被刻意抽成一个接口，原因有三：
 * <ol>
 *   <li><b>测试不需要真的按键</b>：单测注入假实现就能覆盖时序、暂停、急停的判断；</li>
 *   <li><b>能如实说明"没执行"</b>：实现里 {@link #available()} 为 false 的模式，
 *       界面直接告诉你为什么没执行，而不是返回一个看着成功的空结果；</li>
 *   <li><b>为真实执行留口子</b>：将来接真实输入时，只换这一层的实现，导出与计划模型都不用动。</li>
 * </ol>
 *
 * <p>真实执行的前置条件（三条，缺一不可）：明确的单人使用场景、随时可用的急停、
 * 只对前台窗口生效。这三条没落地之前，{@code LOCAL} 模式一律拒绝执行。
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

	/** 执行（或模拟执行） */
	ExecutionResultVO execute(PlanContext context);

	/** 交给执行层的东西：计划头信息 + 已经校验、已按时间排好序的命令流 */
	record PlanContext(Long planId, Long taskId, String taskName, String profileName,
			Integer duration, List<ExecutionResultVO.Command> commands) {
	}
}
