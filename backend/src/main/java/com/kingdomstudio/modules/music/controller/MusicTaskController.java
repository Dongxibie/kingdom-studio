package com.kingdomstudio.modules.music.controller;

import com.kingdomstudio.common.PageVO;
import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.music.dto.JianpuParseDTO;
import com.kingdomstudio.modules.music.dto.MappingRequestDTO;
import com.kingdomstudio.modules.music.service.MusicTaskService;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import com.kingdomstudio.modules.music.vo.MusicTaskDetailVO;
import com.kingdomstudio.modules.music.vo.MusicTaskListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 音乐解析任务接口（Phase 4）。
 *
 * <p>与 Phase 1 的 {@code /music/ping} 分开两个 Controller：自检是常驻入口，
 * 任务相关接口会随 Phase 5/6 继续长大，混在一个类里很快就装不下。
 */
@Tag(name = "11.1 音乐解析任务", description = "MIDI 上传 / 简谱粘贴 → 音符时间线 → 按键序列")
@RestController
@RequestMapping("/music/tasks")
@RequiredArgsConstructor
public class MusicTaskController {

	private final MusicTaskService musicTaskService;

	@Operation(summary = "上传 MIDI", description = "解析 .mid 文件并入库；解析失败会明确报错，不留下半条任务")
	@PostMapping("/midi")
	public Result<MusicTaskDetailVO> uploadMidi(@RequestPart("file") MultipartFile file) {
		return Result.success(musicTaskService.createFromMidi(file));
	}

	@Operation(summary = "粘贴简谱", description = "支持 1=C 4/4 BPM=96 头行、八度记号 ' 与 ,、延长线 - 、附点 . 、减时线 _")
	@PostMapping("/jianpu")
	public Result<MusicTaskDetailVO> parseJianpu(@Valid @RequestBody JianpuParseDTO request) {
		return Result.success(musicTaskService.createFromJianpu(request));
	}

	@Operation(summary = "任务分页", description = "关键词匹配曲子名与来源信息")
	@GetMapping
	public Result<PageVO<MusicTaskListItemVO>> page(
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "1") long page,
			@RequestParam(defaultValue = "10") long size) {
		return Result.success(musicTaskService.page(keyword, page, size));
	}

	@Operation(summary = "任务详情", description = "含全部音符，前端时间线直接用")
	@GetMapping("/{id}")
	public Result<MusicTaskDetailVO> detail(@PathVariable Long id) {
		return Result.success(musicTaskService.detail(id));
	}

	@Operation(summary = "按键映射", description = "把音符翻译成按键序列，返回落键统计、未落键清单与可复制的导出文本")
	@PostMapping("/{id}/keys")
	public Result<KeySequenceVO> mapKeys(@PathVariable Long id, @Valid @RequestBody MappingRequestDTO request) {
		return Result.success(musicTaskService.mapKeys(id, request));
	}

	@Operation(summary = "删除任务", description = "连同音符一起删除")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable Long id) {
		musicTaskService.delete(id);
		return Result.success();
	}
}
