package com.kingdomstudio.modules.music.macro;

import com.kingdomstudio.modules.music.vo.ExecutionResultVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 三种执行模式。
 *
 * <p>只有 {@code PREVIEW} 是真能跑的（而且它一个键都不按）；另外两个如实说明当前不能执行。
 * 这样界面上「执行」按钮的行为永远是确定的：要么给你一份可逐条核对的命令流，
 * 要么明确告诉你缺什么条件，不会出现「点了没反应」或者「假装执行了」。
 */
public final class ExecutionAdapters {

	private ExecutionAdapters() {
	}

	/** 安全说明：三种模式共用 */
	static final List<String> SAFETY = List.of(
			"脚本与本机执行都不会自动开始：需要你显式触发。",
			"急停必须随时可用：一旦执行，ESC 立刻松开所有已按下的键。",
			"只对前台窗口生效：不做后台静默执行。",
			"只发送按键，不移动鼠标、不点击、不读写文件。");

	/** 只模拟：把命令流按时间列出来，供逐条核对；不产生任何真实输入 */
	@Component
	public static class Preview implements ExecutionAdapter {

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

	/** 逐条确认后执行：依赖本机执行通道，本轮未开启 */
	@Component
	public static class Manual implements ExecutionAdapter {

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
			return "逐条确认模式依赖本机执行通道，当前未开启；可以先用「仅模拟」核对命令流，或导出 AutoHotkey 脚本自己控制节奏。";
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
					.safety(SAFETY)
					.build();
		}
	}

	/** 本机连续执行：本轮未开启，且开之前必须先满足三条前置条件 */
	@Component
	public static class Local implements ExecutionAdapter {

		@Override
		public String mode() {
			return "LOCAL";
		}

		@Override
		public String label() {
			return "本机执行";
		}

		@Override
		public boolean available() {
			return false;
		}

		@Override
		public String reason() {
			return "本机执行通道未开启：需要先具备「随时可用的急停」「只对前台窗口生效」「显式开启开关」三条，"
					+ "在此之前不做任何真实按键注入。";
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
					.safety(SAFETY)
					.build();
		}
	}
}
