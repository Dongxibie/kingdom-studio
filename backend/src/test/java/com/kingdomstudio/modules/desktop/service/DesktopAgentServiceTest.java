package com.kingdomstudio.modules.desktop.service;

import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.desktop.vo.AgentStatusVO;
import com.kingdomstudio.modules.desktop.vo.DispatchPlanVO;
import com.kingdomstudio.modules.desktop.vo.KeyCommandVO;
import com.kingdomstudio.modules.desktop.vo.ProtocolVO;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 桌面代理（模拟）测试。
 *
 * <p>这里验的其实是「安全规则有没有生效」：命令流是否成对、时间是否合理、
 * 不可能的操作有没有被拦下并说明原因。真实按键不在本模块范围内，测也不用测。
 */
class DesktopAgentServiceTest {

	private final DesktopAgentService service = new DesktopAgentService();

	private KeySequenceVO.Stroke stroke(int seq, int startMs, int durationMs, String... keys) {
		return KeySequenceVO.Stroke.builder()
				.seq(seq)
				.startMs(startMs)
				.durationMs(durationMs)
				.keys(List.of(keys))
				.noteNames(List.of("C4"))
				.chord(keys.length > 1)
				.adjusted(false)
				.build();
	}

	private KeySequenceVO sequence(KeySequenceVO.Stroke... strokes) {
		return KeySequenceVO.builder()
				.taskId(1L)
				.taskName("小星星")
				.profileId(1L)
				.profileName("光遇式 15 键")
				.keyCount(15)
				.keyLayout(List.of("Z", "X", "C"))
				.keyPitches(List.of(60, 62, 64))
				.strokes(List.of(strokes))
				.build();
	}

	@Test
	@DisplayName("状态：明确是模拟模式，并说明接真实执行的前提")
	void statusShouldSayMock() {
		AgentStatusVO status = service.status();

		assertFalse(status.getAvailable(), "本期没有接真实代理");
		assertEquals("MOCK", status.getMode());
		assertTrue(status.getNotes().stream().anyMatch(note -> note.contains("不会产生任何真实的键盘")),
				status.getNotes().toString());
	}

	@Test
	@DisplayName("协议：含 WS 地址、五个消息类型与安全约束")
	void protocolShouldDescribeEverything() {
		ProtocolVO protocol = service.protocol();

		assertEquals("/api/desktop/ws", protocol.getEndpoint());
		assertEquals("WebSocket", protocol.getTransport());
		assertEquals(5, protocol.getMessages().size());
		assertTrue(protocol.getMessages().stream().anyMatch(message -> "SESSION_ABORT".equals(message.getType())));
		assertTrue(protocol.getSafety().stream().anyMatch(rule -> rule.contains("急停")), protocol.getSafety().toString());
		assertTrue(protocol.getEnvelope().stream().anyMatch(field -> "atMs".equals(field.getName())));
	}

	@Test
	@DisplayName("命令流：每次按键生成按下 + 松开两条，时间正确且成对")
	void shouldBuildPairedCommands() {
		DispatchPlanVO plan = service.plan(sequence(
				stroke(1, 0, 500, "Z"),
				stroke(2, 500, 500, "X")), "test-client");

		assertEquals(4, plan.getAccepted());
		List<KeyCommandVO> commands = plan.getCommands();
		assertEquals("PRESS", commands.get(0).getAction());
		assertEquals(0, commands.get(0).getAtMs());
		assertEquals(500, commands.get(0).getHoldMs());
		assertEquals("RELEASE", commands.get(1).getAction());
		assertEquals(500, commands.get(1).getAtMs());
		assertEquals("X", commands.get(2).getKey());
		assertEquals(500, commands.get(2).getAtMs());
		assertTrue(plan.getWarnings().isEmpty());
		assertTrue(plan.getNotes().get(0).contains("test-client"));
	}

	@Test
	@DisplayName("和弦：同一时刻的多个键各自成对，时间点相同")
	void shouldKeepChordKeysAtSameMoment() {
		DispatchPlanVO plan = service.plan(sequence(stroke(1, 0, 400, "Z", "C")), null);

		assertEquals(4, plan.getAccepted());
		long presses = plan.getCommands().stream()
				.filter(command -> "PRESS".equals(command.getAction()))
				.count();
		assertEquals(2, presses);
		assertTrue(plan.getCommands().stream()
				.filter(command -> "PRESS".equals(command.getAction()))
				.allMatch(command -> command.getAtMs() == 0));
	}

	@Test
	@DisplayName("同一个键还没松开又要按下：顺延到上一次松开之后并给出说明")
	void shouldShiftImpossibleRepeat() {
		// 第一次按住到 600ms，第二次却在 200ms 就要按下同一个键
		DispatchPlanVO plan = service.plan(sequence(
				stroke(1, 0, 600, "Z"),
				stroke(2, 200, 200, "Z")), null);

		assertTrue(plan.getWarnings().stream().anyMatch(warning -> warning.contains("被重复按下")),
				plan.getWarnings().toString());
		int secondPress = plan.getCommands().stream()
				.filter(command -> "PRESS".equals(command.getAction()))
				.mapToInt(KeyCommandVO::getAtMs)
				.max()
				.orElse(-1);
		assertEquals(600 + DesktopAgentService.MIN_REPRESS_GAP_MS, secondPress);
	}

	@Test
	@DisplayName("按住过长：截断到上限并说明")
	void shouldClampLongHold() {
		DispatchPlanVO plan = service.plan(sequence(stroke(1, 0, 9_000, "Z")), null);

		assertEquals(DesktopAgentService.MAX_HOLD_MS, plan.getCommands().get(0).getHoldMs());
		assertTrue(plan.getWarnings().stream().anyMatch(warning -> warning.contains("已截断")),
				plan.getWarnings().toString());
	}

	@Test
	@DisplayName("按键过短：按最短时长发送并说明，避免「按了等于没按」")
	void shouldClampShortHold() {
		DispatchPlanVO plan = service.plan(sequence(stroke(1, 0, 5, "Z")), null);

		assertEquals(DesktopAgentService.MIN_HOLD_MS, plan.getCommands().get(0).getHoldMs());
		assertTrue(plan.getWarnings().stream().anyMatch(warning -> warning.contains("短于最短按键时长")),
				plan.getWarnings().toString());
	}

	@Test
	@DisplayName("多字符的键：协议只传单键，跳过后说明")
	void shouldSkipMultiCharacterKey() {
		DispatchPlanVO plan = service.plan(sequence(stroke(1, 0, 300, "Z", "F5")), null);

		assertEquals(2, plan.getAccepted(), "只留下 Z 的按下与松开");
		assertTrue(plan.getWarnings().stream().anyMatch(warning -> warning.contains("F5")),
				plan.getWarnings().toString());
	}

	@Test
	@DisplayName("空序列：直接拒绝，不生成空计划")
	void shouldRejectEmptySequence() {
		KeySequenceVO empty = KeySequenceVO.builder().taskName("空").strokes(List.of()).build();

		assertThrows(BusinessException.class, () -> service.plan(empty, null));
	}

	@Test
	@DisplayName("命令流按时间排序：代理可以顺序消费，不需要自己再排")
	void planShouldBeSortedByTime() {
		DispatchPlanVO plan = service.plan(sequence(
				stroke(1, 1000, 200, "C"),
				stroke(2, 0, 200, "Z")), null);

		int previous = -1;
		for (KeyCommandVO command : plan.getCommands()) {
			assertTrue(command.getAtMs() >= previous, "命令时间必须单调不减：" + command);
			previous = command.getAtMs();
		}
		assertEquals("Z", plan.getCommands().get(0).getKey());
	}
}
