package com.kingdomstudio.modules.music.service;

import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.music.dto.LocalRunRequestDTO;
import com.kingdomstudio.modules.music.entity.PerformancePlan;
import com.kingdomstudio.modules.music.execution.ExecutionStatus;
import com.kingdomstudio.modules.music.vo.ExecutionStatusVO;
import com.kingdomstudio.modules.music.vo.RuntimePreflightVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 本机演奏会话管理器的单元测试。
 *
 * <p>这一层是安全闸门所在，所以重点测「什么时候该拒绝」：
 * 开关没开、没确认、目标窗口不对、计划是空的 —— 四种都必须明确拒绝，且拒绝理由说人话。
 * 记录模式（injector=record）+ 不做窗口判断（guard=none）让测试完全不碰真实按键。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExecutionServiceTest {

	@Mock
	private PerformanceMacroService macroService;

	private ExecutionService service;

	@BeforeEach
	void setUp() {
		service = new ExecutionService(macroService);
		ReflectionTestUtils.setField(service, "enabled", true);
		ReflectionTestUtils.setField(service, "injectorMode", "record");
		ReflectionTestUtils.setField(service, "guardMode", "none");
		ReflectionTestUtils.setField(service, "guardScript", "");
	}

	private PerformancePlan plan(String notesJson) {
		PerformancePlan plan = new PerformancePlan();
		plan.setId(5L);
		plan.setTaskId(1L);
		plan.setTaskName("小星星");
		plan.setProfileName("光遇式 15 键");
		plan.setDuration(80);
		plan.setNotes(notesJson);
		return plan;
	}

	private String shortNotes() {
		return "[{\"key\":\"Z\",\"action\":\"DOWN\",\"timestamp\":0,\"strokeSeq\":1},"
				+ "{\"key\":\"Z\",\"action\":\"UP\",\"timestamp\":40,\"strokeSeq\":1}]";
	}

	private LocalRunRequestDTO confirmed() {
		LocalRunRequestDTO request = new LocalRunRequestDTO();
		request.setConfirm(true);
		return request;
	}

	@Test
	@DisplayName("环境自检：记录模式且开关打开时可以开始，并列出必须先确认的条件")
	void preflightReportsReadyInRecordMode() {
		RuntimePreflightVO preflight = service.preflight();
		assertTrue(preflight.getEnabled());
		assertTrue(preflight.getReady(), preflight.getReason());
		assertTrue(preflight.getInjector().contains("记录模式"));
		assertTrue(preflight.getRequirements().size() >= 4);
		assertTrue(preflight.getRequirements().stream().anyMatch(item -> item.contains("ESC")));
		assertFalse(preflight.getRunning());
	}

	@Test
	@DisplayName("开关关着：自检说清原因，启动一律拒绝")
	void disabledSwitchBlocksStart() {
		ReflectionTestUtils.setField(service, "enabled", false);
		RuntimePreflightVO preflight = service.preflight();
		assertFalse(preflight.getReady());
		assertTrue(preflight.getReason().contains("开关是关的"), preflight.getReason());
		BusinessException error = assertThrows(BusinessException.class,
				() -> service.start(1L, confirmed()));
		assertTrue(error.getMessage().contains("开关是关的"));
	}

	@Test
	@DisplayName("没写 confirm：不接受默认开启")
	void confirmIsMandatory() {
		LocalRunRequestDTO request = new LocalRunRequestDTO();
		BusinessException missing = assertThrows(BusinessException.class, () -> service.start(1L, request));
		assertTrue(missing.getMessage().contains("显式确认"));

		request.setConfirm(false);
		BusinessException refused = assertThrows(BusinessException.class, () -> service.start(1L, request));
		assertTrue(refused.getMessage().contains("显式确认"));
	}

	@Test
	@DisplayName("空计划：明确拒绝，不开始一个什么都不会做的会话")
	void emptyPlanIsRejected() {
		when(macroService.requireLatest(1L)).thenReturn(plan("[]"));
		BusinessException error = assertThrows(BusinessException.class,
				() -> service.start(1L, confirmed()));
		assertTrue(error.getMessage().contains("没有任何命令"), error.getMessage());
	}

	@Test
	@DisplayName("记录模式：开始 → 状态可见 → 停止，全程不发真实按键")
	void startStatusStopInRecordMode() throws Exception {
		when(macroService.requireLatest(1L)).thenReturn(plan(shortNotes()));

		ExecutionStatusVO started = service.start(1L, confirmed());
		assertEquals("RUNNING", started.getStatus());
		assertEquals(2, started.getCommandCount());
		assertTrue(started.getInjector().contains("记录模式"));

		Thread.sleep(200);
		ExecutionStatusVO finished = service.status();
		assertTrue("RUNNING".equals(finished.getStatus()) || "FINISHED".equals(finished.getStatus()),
				finished.getStatus());
		assertTrue(finished.getProgress() > 0);

		ExecutionStatusVO stopped = service.stop();
		assertTrue(stopped.getStatus().equals("STOPPED") || stopped.getStatus().equals("FINISHED"));
		assertTrue(stopped.getHeldKeys().isEmpty());
		// 会话停下后状态应是终态，不会再回到「演奏中」
		ExecutionStatus finalStatus = service.currentStatus();
		assertTrue(finalStatus == ExecutionStatus.STOPPED || finalStatus == ExecutionStatus.FINISHED,
				String.valueOf(finalStatus));
	}

	@Test
	@DisplayName("暂停 / 继续：状态在 PAUSED 与 RUNNING 之间切换")
	void pauseAndResume() throws Exception {
		when(macroService.requireLatest(1L)).thenReturn(plan(
				"[{\"key\":\"Z\",\"action\":\"DOWN\",\"timestamp\":0,\"strokeSeq\":1},"
						+ "{\"key\":\"Z\",\"action\":\"UP\",\"timestamp\":600,\"strokeSeq\":1}]"));
		service.start(1L, confirmed());
		Thread.sleep(60);

		ExecutionStatusVO paused = service.pause();
		assertEquals("PAUSED", paused.getStatus());
		assertTrue(paused.getHeldKeys().isEmpty(), "暂停要松开按住的键");

		ExecutionStatusVO resumed = service.resume();
		assertEquals("RUNNING", resumed.getStatus());

		service.stop();
	}

	@Test
	@DisplayName("没有会话时暂停/继续直接说清楚，不静默失败")
	void pauseWithoutSessionIsRejected() {
		BusinessException error = assertThrows(BusinessException.class, () -> service.pause());
		assertTrue(error.getMessage().contains("没有正在进行的演奏"));
	}

	@Test
	@DisplayName("模式清单：本机演奏的可用性跟着开关走")
	void localModeStateFollowsSwitch() {
		assertEquals(true, service.localModeState().get("available"));
		ReflectionTestUtils.setField(service, "enabled", false);
		assertEquals(false, service.localModeState().get("available"));
		assertTrue(String.valueOf(service.localModeState().get("reason")).contains("开关是关的"));
	}
}
