package com.kingdomstudio.modules.music.service;

import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.desktop.service.DesktopAgentService;
import com.kingdomstudio.modules.desktop.vo.DispatchPlanVO;
import com.kingdomstudio.modules.desktop.vo.KeyCommandVO;
import com.kingdomstudio.modules.music.dto.ExecutionRequestDTO;
import com.kingdomstudio.modules.music.dto.MappingRequestDTO;
import com.kingdomstudio.modules.music.entity.PerformancePlan;
import com.kingdomstudio.modules.music.execution.ExecutionAdapter;
import com.kingdomstudio.modules.music.execution.LocalExecutionAdapter;
import com.kingdomstudio.modules.music.execution.ManualExecutionAdapter;
import com.kingdomstudio.modules.music.execution.PreviewExecutionAdapter;
import com.kingdomstudio.modules.music.macro.MacroScriptGenerator;
import com.kingdomstudio.modules.music.mapper.PerformancePlanMapper;
import com.kingdomstudio.modules.music.vo.ExecutionResultVO;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import com.kingdomstudio.modules.music.vo.MacroExportVO;
import com.kingdomstudio.modules.music.vo.PerformancePlanVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 演奏宏导出服务的单元测试。
 *
 * <p>守三条线：事件流由命令流派生（不另写时序）、计划落库后才谈导出、
 * 执行模式默认最保守（只有仅模拟会 accepted，另外两种如实拒绝）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PerformanceMacroServiceTest {

	@Mock
	private MusicTaskService musicTaskService;
	@Mock
	private DesktopAgentService desktopAgentService;
	@Mock
	private PerformancePlanMapper planMapper;

	private PerformanceMacroService service;

	@BeforeEach
	void setUp() {
		service = new PerformanceMacroService(musicTaskService, desktopAgentService, planMapper,
				new MacroScriptGenerator(),
				List.of(new PreviewExecutionAdapter(), new ManualExecutionAdapter(), new LocalExecutionAdapter()));
	}

	private KeySequenceVO sequence() {
		return KeySequenceVO.builder()
				.taskId(1L).taskName("小星星").profileId(2L).profileName("光遇式 15 键")
				.strategy("SINGLE_OCTAVE")
				.strokes(List.of(KeySequenceVO.Stroke.builder().seq(1).startMs(0).durationMs(500)
						.keys(List.of("z")).noteNames(List.of("C4")).chord(false).adjusted(false).build()))
				.build();
	}

	private DispatchPlanVO dispatch() {
		return DispatchPlanVO.builder()
				.taskId(1L).taskName("小星星").profileId(2L).profileName("光遇式 15 键")
				.durationMs(1000)
				.accepted(4)
				.warnings(List.of("第 1 次按键只有 30ms，已按最短时长发送。"))
				.commands(List.of(
						command(1, "z", "PRESS", 0, 500, 1),
						command(2, "z", "RELEASE", 500, null, 1),
						command(3, "x", "PRESS", 1000, 400, 2),
						command(4, "x", "RELEASE", 1400, null, 2)))
				.build();
	}

	private KeyCommandVO command(int seq, String key, String action, int atMs, Integer hold, int strokeSeq) {
		return KeyCommandVO.builder().seq(seq).key(key).action(action).atMs(atMs).holdMs(hold)
				.strokeSeq(strokeSeq).adjusted(false).build();
	}

	@Test
	@DisplayName("命令流 → 事件流：PRESS 变 DOWN、RELEASE 变 UP，并按时间排序")
	void eventsAreDerivedFromCommands() {
		List<MacroScriptGenerator.Event> events = service.toEvents(dispatch().getCommands());
		assertEquals(4, events.size());
		assertEquals("DOWN", events.get(0).getAction());
		assertEquals("z", events.get(0).getKey());
		assertEquals("UP", events.get(1).getAction());
		assertEquals(500, events.get(1).getTimestamp());
		assertEquals("PRESS".equals(events.get(0).getAction()) ? "DOWN" : "DOWN", events.get(0).getAction());
	}

	@Test
	@DisplayName("同一毫秒内先松后按：不允许出现「同一个键还没松又按下」")
	void releaseComesBeforePressAtTheSameMoment() {
		List<MacroScriptGenerator.Event> events = service.toEvents(List.of(
				command(1, "z", "PRESS", 500, 300, 2),
				command(2, "z", "RELEASE", 500, null, 1)));
		assertEquals("UP", events.get(0).getAction());
		assertEquals("DOWN", events.get(1).getAction());
	}

	@Test
	@DisplayName("生成计划：把命令流变成事件流并落库，计数与警告都带上")
	void generatePersistsPlan() {
		when(musicTaskService.mapKeys(any(), any())).thenReturn(sequence());
		when(desktopAgentService.plan(any(), any())).thenReturn(dispatch());

		PerformancePlanVO vo = service.generate(1L, new MappingRequestDTO());

		ArgumentCaptor<PerformancePlan> saved = ArgumentCaptor.forClass(PerformancePlan.class);
		verify(planMapper).insert(saved.capture());
		PerformancePlan entity = saved.getValue();
		assertEquals(1L, entity.getTaskId());
		assertEquals(2L, entity.getProfileId());
		assertEquals(1000, entity.getDuration());
		assertEquals(4, entity.getNoteCount());
		assertEquals(2, entity.getKeyCount());
		assertEquals(1, entity.getStrokeCount());
		assertTrue(entity.getWarnings().contains("最短时长"));
		assertTrue(entity.getNotes().contains("\"action\":\"DOWN\""));
		assertEquals(4, vo.getNotes().size());
		assertFalse(vo.getTruncated());
		assertEquals(List.of("z", "x"), vo.getKeys());
		assertEquals(List.of("TXT", "AHK", "JSON"), vo.getFormats());
	}

	@Test
	@DisplayName("导出：三种格式都能出内容，文件名按格式区分")
	void exportSupportsThreeFormats() {
		when(planMapper.selectList(any())).thenReturn(List.of(persisted()));

		MacroExportVO ahk = service.export(1L, "ahk");
		assertEquals("AHK", ahk.getFormat());
		assertTrue(ahk.getFilename().endsWith(".ahk"));
		assertTrue(ahk.getContent().contains("Send, {z down}"));

		MacroExportVO json = service.export(1L, "JSON");
		assertTrue(json.getFilename().endsWith(".json"));
		assertTrue(json.getContent().contains("\"notes\""));
		assertTrue(json.getContentType().contains("application/json"));

		MacroExportVO txt = service.export(1L, "TXT");
		assertTrue(txt.getFilename().endsWith(".txt"));
		assertTrue(txt.getContent().contains("按下 z"));
	}

	@Test
	@DisplayName("导出：不认识的格式直接报错，列出可选值")
	void exportRejectsUnknownFormat() {
		when(planMapper.selectList(any())).thenReturn(List.of(persisted()));
		BusinessException error = assertThrows(BusinessException.class, () -> service.export(1L, "PDF"));
		assertTrue(error.getMessage().contains("TXT / AHK / JSON"), error.getMessage());
	}

	@Test
	@DisplayName("没有计划时先提示生成，而不是抛一个看不懂的空指针")
	void requireLatestExplainsWhenMissing() {
		when(planMapper.selectList(any())).thenReturn(List.of());
		BusinessException error = assertThrows(BusinessException.class, () -> service.latest(1L));
		assertTrue(error.getMessage().contains("还没有生成演奏计划"), error.getMessage());
	}

	@Test
	@DisplayName("执行：默认仅模拟，会把命令流列出来但不产生真实输入")
	void previewIsTheDefaultMode() {
		when(planMapper.selectList(any())).thenReturn(List.of(persisted()));
		ExecutionResultVO result = service.execute(1L, null);
		assertEquals("PREVIEW", result.getMode());
		assertTrue(result.getAccepted());
		assertEquals(4, result.getCommandCount());
		assertTrue(result.getMessage().contains("不会产生任何真实按键输入"));
		assertEquals(4, result.getCommands().size());
		assertEquals("DOWN", result.getCommands().get(0).getAction());
		assertEquals(500, result.getCommands().get(1).getHoldMs(), "松开时应带上按住时长");
		assertTrue(result.getSafety().size() >= 3);
	}

	@Test
	@DisplayName("执行：LOCAL 引导去演奏控制面板，MANUAL 明确未开启，两者都不给出命令流")
	void localAndManualAreRefusedHonestly() {
		when(planMapper.selectList(any())).thenReturn(List.of(persisted()));

		ExecutionRequestDTO local = new ExecutionRequestDTO();
		local.setMode("LOCAL");
		ExecutionResultVO localResult = service.execute(1L, local);
		assertFalse(localResult.getAccepted(), "一次性派发接口不负责启动有状态的会话");
		assertTrue(localResult.getMessage().contains("演奏控制面板"), localResult.getMessage());
		assertTrue(localResult.getCommands().isEmpty());

		ExecutionRequestDTO manual = new ExecutionRequestDTO();
		manual.setMode("MANUAL");
		ExecutionResultVO manualResult = service.execute(1L, manual);
		assertFalse(manualResult.getAccepted());
		assertTrue(manualResult.getMessage().contains("还没做"), manualResult.getMessage());
	}

	@Test
	@DisplayName("执行：模式清单会说明每个模式可不可用")
	void modesDescribeAvailability() {
		List<java.util.Map<String, Object>> modes = service.modes();
		assertEquals(List.of("LOCAL", "MANUAL", "PREVIEW"),
				modes.stream().map(item -> String.valueOf(item.get("mode"))).toList());
		assertEquals(true, modes.stream().filter(item -> "PREVIEW".equals(item.get("mode")))
				.findFirst().orElseThrow().get("available"));
		// LOCAL 走的是有状态会话，是否真能开由 ExecutionService 按配置判断
		assertEquals(true, modes.stream().filter(item -> "LOCAL".equals(item.get("mode")))
				.findFirst().orElseThrow().get("available"));
		assertEquals(false, modes.stream().filter(item -> "MANUAL".equals(item.get("mode")))
				.findFirst().orElseThrow().get("available"));
	}

	private PerformancePlan persisted() {
		PerformancePlan entity = new PerformancePlan();
		entity.setId(9L);
		entity.setTaskId(1L);
		entity.setProfileId(2L);
		entity.setTaskName("小星星");
		entity.setProfileName("光遇式 15 键");
		entity.setStrategy("SINGLE_OCTAVE");
		entity.setDuration(1400);
		entity.setNoteCount(4);
		entity.setStrokeCount(2);
		entity.setKeyCount(2);
		entity.setWarnings("");
		entity.setNotes("[{\"key\":\"z\",\"action\":\"DOWN\",\"timestamp\":0,\"strokeSeq\":1},"
				+ "{\"key\":\"z\",\"action\":\"UP\",\"timestamp\":500,\"strokeSeq\":1},"
				+ "{\"key\":\"x\",\"action\":\"DOWN\",\"timestamp\":1000,\"strokeSeq\":2},"
				+ "{\"key\":\"x\",\"action\":\"UP\",\"timestamp\":1400,\"strokeSeq\":2}]");
		return entity;
	}
}
