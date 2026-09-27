package com.kingdomstudio.modules.music.macro;

import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * 宏脚本生成器：把一份演奏计划（按键事件流）转成三种可导出的文本。
 *
 * <p>三种格式解决三件事，不是「同一份东西导出三遍」：
 * <ul>
 *   <li><b>TXT</b>：给人看的按键时间线，照着练或打印；</li>
 *   <li><b>AutoHotkey</b>：给机器执行的按键脚本（本机自动演奏测试用）；</li>
 *   <li><b>JSON</b>：给程序读的结构化计划，未来接真实执行通道时直接消费。</li>
 * </ul>
 *
 * <p>生成脚本时的三条安全约定（与桌面代理协议保持一致）：
 * 脚本<b>不自动开始</b>（必须按 F9）、<b>急停随时可用</b>（ESC 立刻松开所有键并退出）、
 * <b>暂停可感知</b>（F8，且暂停期间 ESC 依然有效）。
 */
@Component
public class MacroScriptGenerator {

	/** 导出的格式 */
	public static final String FORMAT_TXT = "TXT";
	public static final String FORMAT_AHK = "AHK";
	public static final String FORMAT_JSON = "JSON";

	private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/** 生成 AutoHotkey v1 脚本 */
	public String toAutoHotkey(Plan plan) {
		requireNotes(plan);
		return buildAutoHotkey(plan, plan.getEvents());
	}

	/** 把事件流写进 AHK 主流程（按下 / 等待 / 松开） */
	private String buildAutoHotkey(Plan plan, List<Event> events) {
		StringBuilder out = new StringBuilder(4096);
		appendAhkHeader(out, plan);
		int last = 0;
		for (Event event : events) {
			int gap = event.timestamp - last;
			if (gap > 0) {
				out.append("    Wait(").append(gap).append(")\n");
			}
			out.append("    Send, {").append(event.key)
					.append("DOWN".equals(event.action) ? " down}" : " up}")
					.append(event.action.equals("UP") ? "   ; 按住 " + lastHold(events, event) + "ms" : "")
					.append('\n');
			last = event.timestamp;
		}
		appendAhkFooter(out, plan);
		return out.toString();
	}

	private String lastHold(List<Event> events, Event up) {
		for (int index = 0; index < events.size(); index++) {
			Event event = events.get(index);
			if (event == up) {
				for (int back = index - 1; back >= 0; back--) {
					Event previous = events.get(back);
					if (previous.key.equals(up.key) && "DOWN".equals(previous.action)) {
						return String.valueOf(up.timestamp - previous.timestamp);
					}
				}
			}
		}
		return "0";
	}

	private void appendAhkHeader(StringBuilder out, Plan plan) {
		out.append("; ============================================================\n");
		out.append("; Kingdom Studio · 演奏脚本（AutoHotkey v1.1）\n");
		out.append("; 曲目：").append(blankTo(plan.getTaskName(), "未命名")).append('\n');
		out.append("; 乐器：").append(blankTo(plan.getProfileName(), "未指定"))
				.append("　按键：").append(plan.getKeyCount()).append(" 个")
				.append("　时长：").append(seconds(plan.getDuration())).append(" 秒\n");
		out.append("; 事件：").append(plan.getEvents().size()).append(" 条（按下 + 松开）")
				.append("　生成时间：").append(stamp(plan.getCreatedTime())).append('\n');
		out.append("; ------------------------------------------------------------\n");
		out.append("; 怎么用：\n");
		out.append(";   1) 安装 AutoHotkey v1.1（别装 v2，语法不兼容）\n");
		out.append(";   2) 双击运行：脚本只待命，不会立刻按键\n");
		out.append(";   3) 切到要练的窗口，按 F9 开始\n");
		out.append(";   4) F8 暂停 / 继续，ESC 立即中止并松开所有键\n");
		out.append("; 只对某个窗口生效：把下面这行取消注释并改成你的窗口或进程名\n");
		out.append("; #IfWinActive ahk_exe 你的程序.exe\n");
		out.append("; ------------------------------------------------------------\n");
		out.append("; 安全约定（与 Kingdom Studio 桌面代理协议一致）：\n");
		out.append(";   · 不自动开始：必须按 F9；\n");
		out.append(";   · 急停优先：ESC 立刻松开所有已按下的键并退出，暂停时同样有效；\n");
		out.append(";   · 只发按键：不移动鼠标、不点击、不读写文件。\n");
		for (String warning : plan.getWarnings()) {
			out.append("; 校验提示：").append(warning).append('\n');
		}
		out.append("; ============================================================\n\n");
		out.append("#SingleInstance Force\n");
		out.append("#NoEnv\n");
		out.append("SendMode Event\n");
		out.append("SetKeyDelay, 30, 20   ; 按下与抬起之间留一点时间，节奏更稳\n\n");
		out.append("running := false\n");
		out.append("paused := false\n");
		out.append("keys := ").append(keyList(plan)).append("\n\n");
		out.append("; 与 Sleep 的区别：每 20ms 检查暂停与急停，ESC 在暂停时也有效\n");
		out.append("Wait(ms) {\n");
		out.append("    global running, paused\n");
		out.append("    elapsed := 0\n");
		out.append("    while (elapsed < ms && running) {\n");
		out.append("        while (paused && running)\n");
		out.append("            Sleep, 20\n");
		out.append("        step := (ms - elapsed > 20) ? 20 : (ms - elapsed)\n");
		out.append("        Sleep, %step%\n");
		out.append("        elapsed += step\n");
		out.append("    }\n");
		out.append("}\n\n");
		out.append("; ---------- 主流程：按 F9 开始 ----------\n");
		out.append("F9::\n");
		out.append("    global running, paused, keys\n");
		out.append("    if (running)\n");
		out.append("        return\n");
		out.append("    running := true\n");
		out.append("    paused := false\n");
	}

	private void appendAhkFooter(StringBuilder out, Plan plan) {
		out.append("    running := false\n");
		out.append("return\n\n");
		out.append("; ---------- 暂停 / 继续 ----------\n");
		out.append("F8::paused := !paused\n\n");
		out.append("; ---------- 急停：松开所有键并退出 ----------\n");
		out.append("Esc::\n");
		out.append("    global running, keys\n");
		out.append("    running := false\n");
		out.append("    Sleep, 40\n");
		out.append("    for index, key in keys\n");
		out.append("        Send, {%key% up}\n");
		out.append("    ExitApp\n");
		out.append("return\n");
	}

	/** 生成给人看的按键时间线 */
	public String toTimedText(Plan plan) {
		requireNotes(plan);
		StringBuilder out = new StringBuilder(2048);
		out.append("Kingdom Studio · 演奏脚本（按键时间线）\n");
		out.append("曲目：").append(blankTo(plan.getTaskName(), "未命名")).append('\n');
		out.append("乐器：").append(blankTo(plan.getProfileName(), "未指定"))
				.append("　按键：").append(plan.getKeyCount()).append(" 个")
				.append("　策略：").append(blankTo(plan.getStrategy(), "未指定")).append('\n');
		out.append("总时长：").append(seconds(plan.getDuration())).append(" 秒")
				.append("　按键次数：").append(plan.getStrokeCount())
				.append("　事件：").append(plan.getEvents().size()).append(" 条\n");
		out.append("生成时间：").append(stamp(plan.getCreatedTime())).append("\n\n");
		out.append("[按时间顺序]\n");
		for (Event event : plan.getEvents()) {
			out.append(String.format(Locale.ROOT, "%7.2fs  ", event.timestamp / 1000.0))
					.append("DOWN".equals(event.action) ? "按下 " : "松开 ")
					.append(event.key).append('\n');
		}
		if (!plan.getWarnings().isEmpty()) {
			out.append("\n[校验提示]\n");
			for (String warning : plan.getWarnings()) {
				out.append("- ").append(warning).append('\n');
			}
		}
		return out.toString();
	}

	/** 生成结构化计划（JSON） */
	public String toJsonPlan(Plan plan) {
		requireNotes(plan);
		StringBuilder out = new StringBuilder(4096);
		out.append("{\n");
		out.append("  \"id\": ").append(plan.getId() == null ? "null" : plan.getId()).append(",\n");
		out.append("  \"taskId\": ").append(plan.getTaskId() == null ? "null" : plan.getTaskId()).append(",\n");
		out.append("  \"profileId\": ").append(plan.getProfileId() == null ? "null" : plan.getProfileId()).append(",\n");
		out.append("  \"taskName\": ").append(quote(plan.getTaskName())).append(",\n");
		out.append("  \"profileName\": ").append(quote(plan.getProfileName())).append(",\n");
		out.append("  \"strategy\": ").append(quote(plan.getStrategy())).append(",\n");
		out.append("  \"duration\": ").append(plan.getDuration()).append(",\n");
		out.append("  \"strokeCount\": ").append(plan.getStrokeCount()).append(",\n");
		out.append("  \"noteCount\": ").append(plan.getEvents().size()).append(",\n");
		out.append("  \"planVersion\": \"1.0\",\n");
		out.append("  \"createdTime\": ").append(quote(stamp(plan.getCreatedTime()))).append(",\n");
		out.append("  \"notes\": [\n");
		for (int index = 0; index < plan.getEvents().size(); index++) {
			Event event = plan.getEvents().get(index);
			out.append("    { \"key\": ").append(quote(event.key))
					.append(", \"action\": ").append(quote(event.action))
					.append(", \"timestamp\": ").append(event.timestamp)
					.append(", \"strokeSeq\": ").append(event.strokeSeq == null ? "null" : event.strokeSeq)
					.append(" }");
			out.append(index == plan.getEvents().size() - 1 ? "\n" : ",\n");
		}
		out.append("  ],\n");
		out.append("  \"warnings\": [");
		for (int index = 0; index < plan.getWarnings().size(); index++) {
			out.append(index == 0 ? "\n    " : ",\n    ").append(quote(plan.getWarnings().get(index)));
		}
		out.append(plan.getWarnings().isEmpty() ? "],\n" : "\n  ],\n");
		out.append("  \"safety\": [\n");
		List<String> safety = SAFETY;
		for (int index = 0; index < safety.size(); index++) {
			out.append("    ").append(quote(safety.get(index))).append(index == safety.size() - 1 ? "\n" : ",\n");
		}
		out.append("  ]\n");
		out.append("}\n");
		return out.toString();
	}

	/** 三种格式共用的安全说明，导出里都会带上 */
	public static final List<String> SAFETY = List.of(
			"脚本不会自动开始：必须在目标窗口按 F9 才会发送按键。",
			"急停随时可用：按 ESC 立即松开所有已按下的键并退出（暂停状态下同样有效）。",
			"只发送按键，不移动鼠标、不点击、不读写文件。",
			"本机执行通道默认未开启；这是给单人练习用的脚本，请在你有权限的窗口里使用。");

	/** 建议的文件名 */
	public String filename(String taskName, String format) {
		String base = (taskName == null || taskName.isBlank() ? "performance" : taskName.trim())
				.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
		if (base.length() > 60) {
			base = base.substring(0, 60);
		}
		return switch (format) {
			case FORMAT_AHK -> base + "_演奏脚本.ahk";
			case FORMAT_JSON -> base + "_performance_plan.json";
			default -> base + "_按键时间线.txt";
		};
	}

	private String keyList(Plan plan) {
		StringJoiner joiner = new StringJoiner("\", \"", "[\"", "\"]");
		plan.getKeys().forEach(joiner::add);
		return joiner.toString();
	}

	private String seconds(Integer duration) {
		return String.format(Locale.ROOT, "%.1f", (duration == null ? 0 : duration) / 1000.0);
	}

	private String stamp(LocalDateTime time) {
		return STAMP.format(time == null ? LocalDateTime.now() : time);
	}

	private String blankTo(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}

	private String quote(String value) {
		if (value == null) {
			return "null";
		}
		return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}

	private void requireNotes(Plan plan) {
		if (plan == null || plan.getEvents() == null || plan.getEvents().isEmpty()) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "这份演奏计划里没有任何按键事件，无法生成脚本");
		}
	}

	/** 生成器的输入：计划元信息 + 事件流 */
	@Data
	public static class Plan {
		private Long id;
		private Long taskId;
		private Long profileId;
		private String taskName;
		private String profileName;
		private String strategy;
		private Integer duration;
		private Integer strokeCount;
		private List<String> keys = new ArrayList<>();
		private List<String> warnings = new ArrayList<>();
		private List<Event> events = new ArrayList<>();
		private LocalDateTime createdTime;

		public int getKeyCount() {
			return keys.size();
		}
	}

	/** 一个按键事件 */
	@Data
	public static class Event {
		private String key;
		/** DOWN 按下 / UP 松开 */
		private String action;
		private Integer timestamp;
		private Integer strokeSeq;

		public Event() {
		}

		public Event(String key, String action, Integer timestamp, Integer strokeSeq) {
			this.key = key;
			this.action = action;
			this.timestamp = timestamp;
			this.strokeSeq = strokeSeq;
		}
	}
}
