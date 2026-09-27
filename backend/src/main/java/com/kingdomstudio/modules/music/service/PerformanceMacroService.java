package com.kingdomstudio.modules.music.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.desktop.service.DesktopAgentService;
import com.kingdomstudio.modules.desktop.vo.DispatchPlanVO;
import com.kingdomstudio.modules.desktop.vo.KeyCommandVO;
import com.kingdomstudio.modules.music.dto.ExecutionRequestDTO;
import com.kingdomstudio.modules.music.dto.MappingRequestDTO;
import com.kingdomstudio.modules.music.entity.PerformancePlan;
import com.kingdomstudio.modules.music.execution.ExecutionAdapter;
import com.kingdomstudio.modules.music.macro.MacroScriptGenerator;
import com.kingdomstudio.modules.music.mapper.PerformancePlanMapper;
import com.kingdomstudio.modules.music.vo.ExecutionResultVO;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import com.kingdomstudio.modules.music.vo.MacroExportVO;
import com.kingdomstudio.modules.music.vo.PerformancePlanVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 演奏宏导出：把「按键序列」变成「可带走的演奏脚本」。
 *
 * <p>这一层加在既有链路的最末端，前面一步都不动：
 * 解析 → 音符时间线 → 键位映射 → 按键序列（既有） → <b>演奏计划 → 宏脚本</b>（本层）。
 *
 * <p>三个关键取舍：
 * <ol>
 *   <li><b>时序只有一处定义</b>：命令流直接来自 {@link DesktopAgentService#plan}，
 *       它已经做了最短/最长按住截断、同键重复按顺延、单键校验；本层不另写一套，
 *       所以导出脚本里的时序与桌面代理派发计划永远一致。</li>
 *   <li><b>计划落库</b>：生成一次就存一份（{@code performance_plan}），
 *       导出与执行都读同一份，避免「同一个曲子每次导出结果不一样」。</li>
 *   <li><b>导出与执行分开</b>：导出只产出文本；一次性的执行走 {@link ExecutionAdapter}（仅模拟可用），
 *       真正的本机演奏是有状态的会话，见 {@code com.kingdomstudio.modules.music.service.ExecutionService}。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceMacroService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/** 列表接口最多带回多少条事件：整首曲谱可能有上千条，前端只用来做预览 */
	private static final int PREVIEW_LIMIT = 400;

	/** 导出格式（顺序即界面上的顺序） */
	private static final List<String> FORMATS = List.of(
			MacroScriptGenerator.FORMAT_TXT, MacroScriptGenerator.FORMAT_AHK, MacroScriptGenerator.FORMAT_JSON);

	private final MusicTaskService musicTaskService;
	private final DesktopAgentService desktopAgentService;
	private final PerformancePlanMapper planMapper;
	private final PerformancePresetService presetService;
	private final MacroScriptGenerator generator;
	private final List<ExecutionAdapter> adapters;

	/** 生成（或重新生成）演奏计划：按键序列 → 校验过的命令流 → 事件流 → 落库 */
	@Transactional
	public PerformancePlanVO generate(Long taskId, MappingRequestDTO request) {
		return generate(taskId, request, null);
	}

	/**
	 * 生成演奏计划。
	 *
	 * <p>presetId 不为空时按方案执行：用方案里的乐器档案与策略做映射，再按速度倍率缩放、
	 * 给同键重按留出最小间隔。两个变换都发生在**序列**上，之后仍然走同一条
	 * {@code DesktopAgentService.plan} 校验（最短按住 40ms、重按间隔、命令数上限都不绕开）。
	 * presetId 为空时行为与之前完全一致。
	 */
	@Transactional
	public PerformancePlanVO generate(Long taskId, MappingRequestDTO request, Long presetId) {
		MappingRequestDTO effective = request;
		double speedScale = 1.0;
		int minGapMs = 0;
		if (presetId != null) {
			PerformancePresetService.Resolution resolution = presetService.resolve(taskId, presetId);
			if (resolution.mapping() != null) {
				effective = resolution.mapping();
			}
			speedScale = resolution.speedScale();
			minGapMs = resolution.minGapMs();
		}
		KeySequenceVO sequence = musicTaskService.mapKeys(taskId, effective);
		sequence = PerformanceTempo.applyMinGap(PerformanceTempo.scale(sequence, speedScale), minGapMs);
		DispatchPlanVO dispatch = desktopAgentService.plan(sequence, "macro-export");
		List<MacroScriptGenerator.Event> events = toEvents(dispatch.getCommands());
		PerformancePlan entity = new PerformancePlan();
		entity.setTaskId(sequence.getTaskId());
		entity.setProfileId(sequence.getProfileId());
		entity.setTaskName(sequence.getTaskName());
		entity.setProfileName(sequence.getProfileName());
		entity.setStrategy(sequence.getStrategy());
		entity.setDuration(dispatch.getDurationMs());
		entity.setNoteCount(events.size());
		entity.setStrokeCount(sequence.getStrokes() == null ? 0 : sequence.getStrokes().size());
		entity.setKeyCount(distinctKeys(events).size());
		entity.setWarnings(String.join("\n", dispatch.getWarnings()));
		entity.setNotes(writeJson(events));
		planMapper.insert(entity);
		log.info("生成演奏计划：task={} 事件={} 命令={} 时长={}ms 警告={}",
				taskId, events.size(), dispatch.getCommands().size(), dispatch.getDurationMs(),
				dispatch.getWarnings().size());
		return toVO(entity, PREVIEW_LIMIT);
	}

	/** 取最近一次生成的计划 */
	public PerformancePlanVO latest(Long taskId) {
		return toVO(requireLatest(taskId), PREVIEW_LIMIT);
	}

	/** 导出脚本：三种格式都由同一份事件流生成 */
	public MacroExportVO export(Long taskId, String format) {
		PerformancePlan entity = requireLatest(taskId);
		String normalized = format == null ? MacroScriptGenerator.FORMAT_TXT : format.trim().toUpperCase(Locale.ROOT);
		if (!FORMATS.contains(normalized)) {
			throw new BusinessException(ResultCode.BAD_REQUEST,
					"不支持的导出格式：" + format + "，可选 " + String.join(" / ", FORMATS));
		}
		MacroScriptGenerator.Plan plan = toGeneratorPlan(entity);
		String content = switch (normalized) {
			case MacroScriptGenerator.FORMAT_AHK -> generator.toAutoHotkey(plan);
			case MacroScriptGenerator.FORMAT_JSON -> generator.toJsonPlan(plan);
			default -> generator.toTimedText(plan);
		};
		return MacroExportVO.builder()
				.planId(entity.getId())
				.taskId(entity.getTaskId())
				.format(normalized)
				.filename(generator.filename(entity.getTaskName(), normalized))
				.contentType(MacroScriptGenerator.FORMAT_JSON.equals(normalized)
						? "application/json;charset=utf-8" : "text/plain;charset=utf-8")
				.size(content.length())
				.content(content)
				.hint(hintOf(normalized))
				.build();
	}

	/** 一次性执行（PREVIEW 仅模拟可用；LOCAL 会指向演奏控制面板，避免误触） */
	public ExecutionResultVO execute(Long taskId, ExecutionRequestDTO request) {
		PerformancePlan entity = requireLatest(taskId);
		String mode = request == null || request.getMode() == null || request.getMode().isBlank()
				? "PREVIEW" : request.getMode().trim().toUpperCase(Locale.ROOT);
		ExecutionAdapter adapter = adapters.stream()
				.filter(item -> item.mode().equals(mode))
				.findFirst()
				.orElseThrow(() -> new BusinessException(ResultCode.BAD_REQUEST,
						"不支持的模式：" + mode + "，可选 "
								+ adapters.stream().map(ExecutionAdapter::mode).collect(Collectors.joining(" / "))));
		List<ExecutionResultVO.Command> commands = toCommands(entity);
		return adapter.execute(new ExecutionAdapter.PlanContext(entity.getId(), entity.getTaskId(),
				entity.getTaskName(), entity.getProfileName(), entity.getDuration(), commands));
	}

	private String hintOf(String format) {
		return switch (format) {
			case MacroScriptGenerator.FORMAT_AHK -> "AutoHotkey v1.1 脚本：双击运行后按 F9 开始，F8 暂停，ESC 急停。"
					+ "脚本不自动开始，也不会移动鼠标。";
			case MacroScriptGenerator.FORMAT_JSON -> "结构化计划：notes 是按键事件流（按下 / 松开 + 时间），"
					+ "可以直接喂给本机执行通道或别的工具。";
			default -> "按键时间线：给人看的版本，照着练或者打印出来都行。";
		};
	}

	/** 命令流 → 事件流：PRESS 变 DOWN，RELEASE 变 UP；同一时刻先松后按，避免同键重叠 */
	List<MacroScriptGenerator.Event> toEvents(List<KeyCommandVO> commands) {
		List<MacroScriptGenerator.Event> events = new ArrayList<>();
		if (commands == null) {
			return events;
		}
		for (KeyCommandVO command : commands) {
			events.add(new MacroScriptGenerator.Event(command.getKey(),
					"RELEASE".equals(command.getAction()) ? "UP" : "DOWN",
					command.getAtMs() == null ? 0 : command.getAtMs(),
					command.getStrokeSeq()));
		}
		events.sort(Comparator
				.comparingInt((MacroScriptGenerator.Event event) -> event.getTimestamp())
				// 同一毫秒里先松开再按下：否则同一个键会出现「还没松又按下」
				.thenComparing(event -> "UP".equals(event.getAction()) ? 0 : 1));
		return events;
	}

	private List<ExecutionResultVO.Command> toCommands(PerformancePlan entity) {
		List<MacroScriptGenerator.Event> events = readEvents(entity);
		List<ExecutionResultVO.Command> commands = new ArrayList<>();
		int seq = 1;
		Map<String, Integer> downAt = new java.util.HashMap<>();
		for (MacroScriptGenerator.Event event : events) {
			Integer hold = null;
			if ("UP".equals(event.getAction())) {
				Integer start = downAt.remove(event.getKey());
				hold = start == null ? null : event.getTimestamp() - start;
			} else {
				downAt.put(event.getKey(), event.getTimestamp());
			}
			commands.add(ExecutionResultVO.Command.builder()
					.seq(seq++)
					.key(event.getKey())
					.action(event.getAction())
					.atMs(event.getTimestamp())
					.holdMs(hold)
					.strokeSeq(event.getStrokeSeq())
					.build());
		}
		return commands;
	}

	PerformancePlan requireLatest(Long taskId) {
		List<PerformancePlan> plans = planMapper.selectList(new LambdaQueryWrapper<PerformancePlan>()
				.eq(PerformancePlan::getTaskId, taskId)
				.orderByDesc(PerformancePlan::getId)
				.last("limit 1"));
		if (plans.isEmpty()) {
			throw new BusinessException(ResultCode.NOT_FOUND,
					"这个任务还没有生成演奏计划，请先点「导出演奏脚本」");
		}
		return plans.get(0);
	}

	private List<String> distinctKeys(List<MacroScriptGenerator.Event> events) {
		return new ArrayList<>(events.stream()
				.map(MacroScriptGenerator.Event::getKey)
				.collect(Collectors.toCollection(LinkedHashSet::new)));
	}

	private String writeJson(List<MacroScriptGenerator.Event> events) {
		try {
			List<Map<String, Object>> rows = events.stream()
					.map(event -> Map.<String, Object>of(
							"key", event.getKey(),
							"action", event.getAction(),
							"timestamp", event.getTimestamp(),
							"strokeSeq", event.getStrokeSeq() == null ? 0 : event.getStrokeSeq()))
					.toList();
			return OBJECT_MAPPER.writeValueAsString(rows);
		} catch (Exception error) {
			throw new BusinessException(ResultCode.INTERNAL_ERROR, "演奏计划序列化失败：" + error.getMessage());
		}
	}

	List<MacroScriptGenerator.Event> readEvents(PerformancePlan entity) {
		try {
			List<Map<String, Object>> rows = OBJECT_MAPPER.readValue(entity.getNotes(),
					new TypeReference<List<Map<String, Object>>>() {
					});
			List<MacroScriptGenerator.Event> events = new ArrayList<>();
			for (Map<String, Object> row : rows) {
				events.add(new MacroScriptGenerator.Event(
						String.valueOf(row.get("key")),
						String.valueOf(row.get("action")),
						((Number) row.getOrDefault("timestamp", 0)).intValue(),
						((Number) row.getOrDefault("strokeSeq", 0)).intValue()));
			}
			return events;
		} catch (Exception error) {
			throw new BusinessException(ResultCode.INTERNAL_ERROR, "演奏计划解析失败：" + error.getMessage());
		}
	}

	PerformancePlanVO toVO(PerformancePlan entity, int limit) {
		List<MacroScriptGenerator.Event> events = readEvents(entity);
		List<PerformancePlanVO.Note> notes = events.stream()
				.limit(limit)
				.map(event -> PerformancePlanVO.Note.builder()
						.key(event.getKey())
						.action(event.getAction())
						.timestamp(event.getTimestamp())
						.strokeSeq(event.getStrokeSeq())
						.build())
				.toList();
		return PerformancePlanVO.builder()
				.id(entity.getId())
				.taskId(entity.getTaskId())
				.profileId(entity.getProfileId())
				.taskName(entity.getTaskName())
				.profileName(entity.getProfileName())
				.strategy(entity.getStrategy())
				.duration(entity.getDuration())
				.noteCount(entity.getNoteCount())
				.strokeCount(entity.getStrokeCount())
				.keyCount(entity.getKeyCount())
				.keys(distinctKeys(events))
				.warnings(entity.getWarnings() == null || entity.getWarnings().isBlank()
						? List.of() : List.of(entity.getWarnings().split("\n")))
				.notes(notes)
				.truncated(events.size() > limit)
				.formats(FORMATS)
				.createTime(entity.getCreateTime())
				.build();
	}

	MacroScriptGenerator.Plan toGeneratorPlan(PerformancePlan entity) {
		List<MacroScriptGenerator.Event> events = readEvents(entity);
		MacroScriptGenerator.Plan plan = new MacroScriptGenerator.Plan();
		plan.setId(entity.getId());
		plan.setTaskId(entity.getTaskId());
		plan.setProfileId(entity.getProfileId());
		plan.setTaskName(entity.getTaskName());
		plan.setProfileName(entity.getProfileName());
		plan.setStrategy(entity.getStrategy());
		plan.setDuration(entity.getDuration());
		plan.setStrokeCount(entity.getStrokeCount());
		plan.setKeys(distinctKeys(events));
		plan.setWarnings(entity.getWarnings() == null || entity.getWarnings().isBlank()
				? List.of() : List.of(entity.getWarnings().split("\n")));
		plan.setEvents(events);
		plan.setCreatedTime(entity.getCreateTime());
		return plan;
	}

	/** 供控制器做模式清单展示 */
	public List<Map<String, Object>> modes() {
		return adapters.stream()
				.sorted(Comparator.comparing(ExecutionAdapter::mode))
				.map(adapter -> Map.<String, Object>of(
						"mode", adapter.mode(),
						"label", adapter.label(),
						"available", adapter.available(),
						"reason", adapter.reason()))
				.collect(Collectors.toList());
	}
}
