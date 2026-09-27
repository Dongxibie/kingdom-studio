package com.kingdomstudio.modules.music.controller;

import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.music.analysis.PerformanceOptimizerService;
import com.kingdomstudio.modules.music.analysis.SongAnalysisService;
import com.kingdomstudio.modules.music.dto.PerformancePresetDTO;
import com.kingdomstudio.modules.music.service.PerformancePresetService;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import com.kingdomstudio.modules.music.vo.OptimizationVO;
import com.kingdomstudio.modules.music.vo.PerformancePresetVO;
import com.kingdomstudio.modules.music.vo.SongAnalysisVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 曲目洞察与演奏方案。
 *
 * <p>三组能力都挂在「某一首曲子」下面，所以路径统一是 /music/tasks/{taskId}/…
 * —— 分析、优化、方案都是围绕曲目的，放在曲目路径下最自然，也不用在前端拼参数。
 */
@Tag(name = "11.5 曲目洞察与演奏方案", description = "曲目分析卡、演奏优化器、多套演奏方案")
@RestController
@RequestMapping("/music/tasks/{taskId}")
@RequiredArgsConstructor
public class MusicInsightController {

	private final SongAnalysisService analysisService;
	private final PerformanceOptimizerService optimizerService;
	private final PerformancePresetService presetService;

	@Operation(summary = "曲目分析",
			description = "难度星级与计分依据、速度、音域、预计演奏时长、推荐键位；推荐是拿每套乐器档案真实试算出来的")
	@GetMapping("/analysis")
	public Result<SongAnalysisVO> analysis(@PathVariable Long taskId,
			@RequestParam(required = false) Long profileId) {
		return Result.success(analysisService.analyze(taskId, profileId));
	}

	@Operation(summary = "演奏优化",
			description = "检查同键重按冲突、超出音域、和弦、密度与长按，每条建议都带可直接应用的参数；带 presetId 时按该方案（速度与同键间隔）调好序列再检查")
	@GetMapping("/optimize")
	public Result<OptimizationVO> optimize(@PathVariable Long taskId,
			@RequestParam Long profileId,
			@RequestParam(required = false) String strategy,
			@RequestParam(required = false) Long presetId) {
		return Result.success(optimizerService.optimize(taskId, profileId, strategy, presetId));
	}

	@Operation(summary = "演奏方案列表", description = "首次访问会为这首曲子补齐「原版 / 简单版 / 快速版」三套内置方案")
	@GetMapping("/presets")
	public Result<List<PerformancePresetVO>> presets(@PathVariable Long taskId) {
		return Result.success(presetService.list(taskId));
	}

	@Operation(summary = "新建演奏方案", description = "同一首曲子可以存多套打法：换档案、换策略、调速、加最小间隔")
	@PostMapping("/presets")
	public Result<PerformancePresetVO> createPreset(@PathVariable Long taskId,
			@Valid @RequestBody PerformancePresetDTO request) {
		return Result.success(presetService.create(taskId, request));
	}

	@Operation(summary = "修改演奏方案")
	@PutMapping("/presets/{presetId}")
	public Result<PerformancePresetVO> updatePreset(@PathVariable Long taskId, @PathVariable Long presetId,
			@Valid @RequestBody PerformancePresetDTO request) {
		return Result.success(presetService.update(presetId, request));
	}

	@Operation(summary = "删除演奏方案")
	@DeleteMapping("/presets/{presetId}")
	public Result<Void> deletePreset(@PathVariable Long taskId, @PathVariable Long presetId) {
		presetService.delete(presetId);
		return Result.success();
	}

	@Operation(summary = "按方案映射", description = "用某套方案的档案与策略跑一遍按键映射（导出中心与编排台切换方案时用）")
	@GetMapping("/presets/{presetId}/keys")
	public Result<KeySequenceVO> mapWithPreset(@PathVariable Long taskId, @PathVariable Long presetId) {
		return Result.success(presetService.mapWithPreset(taskId, presetId));
	}
}
