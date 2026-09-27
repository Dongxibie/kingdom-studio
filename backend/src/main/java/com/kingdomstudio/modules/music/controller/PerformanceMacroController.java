package com.kingdomstudio.modules.music.controller;

import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.music.dto.ExecutionRequestDTO;
import com.kingdomstudio.modules.music.dto.LocalRunRequestDTO;
import com.kingdomstudio.modules.music.dto.MappingRequestDTO;
import com.kingdomstudio.modules.music.service.ExecutionService;
import com.kingdomstudio.modules.music.service.PerformanceMacroService;
import com.kingdomstudio.modules.music.vo.ExecutionResultVO;
import com.kingdomstudio.modules.music.vo.ExecutionStatusVO;
import com.kingdomstudio.modules.music.vo.RuntimePreflightVO;
import com.kingdomstudio.modules.music.vo.MacroExportVO;
import com.kingdomstudio.modules.music.vo.PerformancePlanVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 演奏宏导出接口（Performance Macro Export）。
 *
 * <p>挂在既有的 {@code /music/tasks/{id}} 下面，是对「按键序列」的下一步加工：
 * 生成演奏计划 → 导出 TXT / AutoHotkey / JSON → （可选）交给执行层。
 * 既有的上传、解析、详情、映射四个接口一个都没动。
 */
@Tag(name = "11.3 演奏宏导出", description = "演奏计划、按键脚本导出与本机执行（仅模拟）")
@RestController
@RequestMapping("/music/tasks/{taskId}/performance-plan")
@RequiredArgsConstructor
public class PerformanceMacroController {

	private final PerformanceMacroService macroService;
	private final ExecutionService executionService;

	@Operation(summary = "生成演奏计划",
			description = "按键序列 → 校验过的命令流 → 按键事件流；同一份计划可反复导出三种格式")
	@PostMapping
	public Result<PerformancePlanVO> generate(@PathVariable Long taskId,
			@Valid @RequestBody MappingRequestDTO request) {
		return Result.success(macroService.generate(taskId, request));
	}

	@Operation(summary = "最近一次演奏计划", description = "事件流可能被截断，完整内容走导出接口")
	@GetMapping
	public Result<PerformancePlanVO> latest(@PathVariable Long taskId) {
		return Result.success(macroService.latest(taskId));
	}

	@Operation(summary = "导出脚本", description = "格式：TXT 按键时间线 / AHK AutoHotkey 脚本 / JSON 演奏计划")
	@GetMapping("/export")
	public Result<MacroExportVO> export(@PathVariable Long taskId,
			@RequestParam(defaultValue = "AHK") String format) {
		return Result.success(macroService.export(taskId, format));
	}

	@Operation(summary = "执行（默认仅模拟）",
			description = "PREVIEW 列出将要发出的命令但不产生真实输入；MANUAL / LOCAL 当前未开启，会如实说明原因")
	@PostMapping("/execute")
	public Result<ExecutionResultVO> execute(@PathVariable Long taskId,
			@RequestBody(required = false) ExecutionRequestDTO request) {
		return Result.success(macroService.execute(taskId, request));
	}

	@Operation(summary = "执行模式清单", description = "每个模式是否可用、不可用时的原因")
	@GetMapping("/modes")
	public Result<List<Map<String, Object>>> modes(@PathVariable Long taskId) {
		return Result.success(macroService.modes());
	}

	@Operation(summary = "本机演奏环境自检",
			description = "开关是否打开、此刻的前台窗口是什么、注入方式、必须先确认的条件")
	@GetMapping("/runtime/preflight")
	public Result<RuntimePreflightVO> preflight(@PathVariable Long taskId) {
		return Result.success(executionService.preflight());
	}

	@Operation(summary = "开始本机演奏",
			description = "需要 confirm=true 与已确认的目标窗口；只对该窗口生效，ESC 随时急停")
	@PostMapping("/runtime/start")
	public Result<ExecutionStatusVO> start(@PathVariable Long taskId,
			@RequestBody LocalRunRequestDTO request) {
		return Result.success(executionService.start(taskId, request));
	}

	@Operation(summary = "演奏状态", description = "状态 / 进度 / 当前按键 / 剩余时间 / 按住的键 / 窗口")
	@GetMapping("/runtime/status")
	public Result<ExecutionStatusVO> runtimeStatus(@PathVariable Long taskId) {
		return Result.success(executionService.status());
	}

	@Operation(summary = "暂停演奏", description = "暂停会立刻松开所有按下的键，时间轴停住")
	@PostMapping("/runtime/pause")
	public Result<ExecutionStatusVO> pause(@PathVariable Long taskId) {
		return Result.success(executionService.pause());
	}

	@Operation(summary = "继续演奏")
	@PostMapping("/runtime/resume")
	public Result<ExecutionStatusVO> resume(@PathVariable Long taskId) {
		return Result.success(executionService.resume());
	}

	@Operation(summary = "停止演奏（等同急停）", description = "立刻停止并松开所有按下的键")
	@PostMapping("/runtime/stop")
	public Result<ExecutionStatusVO> stop(@PathVariable Long taskId) {
		return Result.success(executionService.stop());
	}
}
