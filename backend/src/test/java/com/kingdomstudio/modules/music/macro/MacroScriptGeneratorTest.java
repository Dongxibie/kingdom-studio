package com.kingdomstudio.modules.music.macro;

import com.kingdomstudio.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 宏脚本生成器的单元测试。
 *
 * <p>这些断言盯的是三件事：脚本真的能执行（按下/等待/松开成对）、
 * 安全约定真的写进去了（不自动开始、急停松开所有键）、
 * 三种格式出自同一份事件流（时间戳一致）。
 */
class MacroScriptGeneratorTest {

	private final MacroScriptGenerator generator = new MacroScriptGenerator();

	private MacroScriptGenerator.Plan plan(MacroScriptGenerator.Event... events) {
		MacroScriptGenerator.Plan plan = new MacroScriptGenerator.Plan();
		plan.setId(9L);
		plan.setTaskId(1L);
		plan.setProfileId(2L);
		plan.setTaskName("小星星");
		plan.setProfileName("光遇式 15 键");
		plan.setStrategy("SINGLE_OCTAVE");
		plan.setStrokeCount(3);
		plan.setDuration(1000);
		plan.setKeys(List.of("z", "x", "c"));
		plan.setEvents(List.of(events));
		plan.setCreatedTime(LocalDateTime.of(2026, 9, 27, 14, 5));
		return plan;
	}

	private MacroScriptGenerator.Event down(String key, int at) {
		return new MacroScriptGenerator.Event(key, "DOWN", at, 1);
	}

	private MacroScriptGenerator.Event up(String key, int at) {
		return new MacroScriptGenerator.Event(key, "UP", at, 1);
	}

	@Test
	@DisplayName("AHK：按下 → 等待持续时间 → 松开，三段式与示例一致")
	void autoHotkeyFollowsTheSampleShape() {
		String script = generator.toAutoHotkey(plan(down("z", 0), up("z", 500), down("x", 1000), up("x", 1500)));
		assertTrue(script.contains("Send, {z down}"), script);
		assertTrue(script.contains("Wait(500)"), "0→500ms 之间应有一个 500ms 的等待");
		assertTrue(script.contains("Send, {z up}"), script);
		assertTrue(script.contains("Send, {x down}"), script);
		assertTrue(script.contains("Send, {x up}"), script);
	}

	@Test
	@DisplayName("AHK：事件之间的间隔也算出来，不是把所有等待都当按住时长")
	void autoHotkeyEmitsGapsBetweenEvents() {
		// 0 按下、200 松开、加 800ms 间隔后 1000 处按下
		String script = generator.toAutoHotkey(plan(down("z", 0), up("z", 200), down("x", 1000), up("x", 1200)));
		assertTrue(script.contains("Wait(200)"), "按住时长：" + script);
		assertTrue(script.contains("Wait(800)"), "两次按键之间的间隔：" + script);
	}

	@Test
	@DisplayName("AHK：和弦（同一时刻多个键）连续按下，中间不插入等待")
	void autoHotkeyKeepsChordsTogether() {
		MacroScriptGenerator.Event a = new MacroScriptGenerator.Event("a", "DOWN", 0, 1);
		MacroScriptGenerator.Event b = new MacroScriptGenerator.Event("b", "DOWN", 0, 1);
		String script = generator.toAutoHotkey(plan(a, b, up("a", 400), up("b", 400)));
		int firstDown = script.indexOf("Send, {a down}");
		int secondDown = script.indexOf("Send, {b down}");
		assertTrue(firstDown > 0 && secondDown > firstDown);
		assertFalse(script.substring(firstDown, secondDown).contains("Wait("),
				"同一个和弦的按下之间不该有等待");
	}

	@Test
	@DisplayName("AHK：安全约定齐全 —— 不自动开始、F8 暂停、ESC 急停并松开所有键")
	void autoHotkeyCarriesSafetyRules() {
		String script = generator.toAutoHotkey(plan(down("z", 0), up("z", 500)));
		assertTrue(script.contains("F9::"), "必须有一个显式的开始热键");
		assertTrue(script.contains("F8::paused := !paused"), "暂停热键");
		assertTrue(script.contains("Esc::"), "急停热键");
		assertTrue(script.contains("Send, {%key% up}"), "急停要松开所有按下的键");
		assertTrue(script.contains("ExitApp"), "急停后退出脚本");
		assertTrue(script.contains("keys := [\"z\", \"x\", \"c\"]"), "键清单用来做急停释放：" + script);
		// 主流程必须写在 F9 标签下面，脚本本身不自动开火
		assertTrue(script.indexOf("F9::") < script.indexOf("Send, {z down}"));
	}

	@Test
	@DisplayName("AHK：把校验提示与目标窗口约束写进脚本注释")
	void autoHotkeyDocumentsWarningsAndWindowScope() {
		MacroScriptGenerator.Plan plan = plan(down("z", 0), up("z", 500));
		plan.setWarnings(List.of("第 2 次按键只有 30ms，已按最短时长发送。"));
		String script = generator.toAutoHotkey(plan);
		assertTrue(script.contains("校验提示：第 2 次按键只有 30ms"));
		assertTrue(script.contains("#IfWinActive"), "给出只对某个窗口生效的做法");
	}

	@Test
	@DisplayName("TXT：给人看的时间线，秒数保留两位、按下/松开逐条列出")
	void timedTextIsHumanReadable() {
		String text = generator.toTimedText(plan(down("z", 0), up("z", 500), down("x", 1500), up("x", 2000)));
		assertTrue(text.contains("曲目：小星星"));
		assertTrue(text.contains("按下 z"));
		assertTrue(text.contains("松开 z"));
		assertTrue(text.contains("0.00s"), text);
		assertTrue(text.contains("1.50s"), text);
		assertTrue(text.contains("总时长：1.0 秒"));
	}

	@Test
	@DisplayName("JSON：notes 是 {key, action, timestamp} 三件套，并带上安全说明")
	void jsonPlanMatchesTheSpecShape() {
		String json = generator.toJsonPlan(plan(down("z", 0), up("z", 500)));
		assertTrue(json.contains("\"notes\""));
		assertTrue(json.contains("{ \"key\": \"z\", \"action\": \"DOWN\", \"timestamp\": 0"));
		assertTrue(json.contains("{ \"key\": \"z\", \"action\": \"UP\", \"timestamp\": 500"));
		assertTrue(json.contains("\"duration\": 1000"));
		assertTrue(json.contains("\"taskId\": 1"));
		assertTrue(json.contains("\"profileId\": 2"));
		assertTrue(json.contains("\"safety\""));
	}

	@Test
	@DisplayName("文件名：去掉非法字符，按格式给后缀")
	void filenamesAreSafeAndFormatSpecific() {
		assertEquals("小星星_演奏脚本.ahk",
				generator.filename("小星星", MacroScriptGenerator.FORMAT_AHK));
		assertEquals("My_Song_按键时间线.txt",
				generator.filename("My Song", MacroScriptGenerator.FORMAT_TXT));
		assertEquals("a_b_123_performance_plan.json",
				generator.filename("a/b:123", MacroScriptGenerator.FORMAT_JSON));
		assertTrue(generator.filename(null, MacroScriptGenerator.FORMAT_TXT).startsWith("performance"));
	}

	@Test
	@DisplayName("空计划不生成脚本，明确报错而不是给一份空文件")
	void emptyPlanIsRejected() {
		MacroScriptGenerator.Plan empty = new MacroScriptGenerator.Plan();
		assertThrows(BusinessException.class, () -> generator.toAutoHotkey(empty));
		assertThrows(BusinessException.class, () -> generator.toTimedText(empty));
		assertThrows(BusinessException.class, () -> generator.toJsonPlan(empty));
	}
}
