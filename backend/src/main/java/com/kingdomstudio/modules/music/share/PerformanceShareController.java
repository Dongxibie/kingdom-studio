package com.kingdomstudio.modules.music.share;

import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.music.share.dto.PerformanceShareDTO;
import com.kingdomstudio.modules.music.share.vo.PerformanceShareVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 曲谱分享。
 *
 * <p>三个动作：生成（把自己的方案变成演奏码）、查看（拿到码的人先看摘要）、导入（还原成自己的曲目）。
 * 导入是「复制一份」，不动原作者的任何数据。
 */
@Tag(name = "11.6 曲谱分享", description = "演奏码生成、查看与导入")
@RestController
@RequestMapping("/music/shares")
@RequiredArgsConstructor
public class PerformanceShareController {

	private final PerformanceShareService shareService;

	@Operation(summary = "生成演奏码", description = "把曲目 + 演奏方案 + 最近一次计划打包成自包含快照，返回 KS-MUSIC-… 演奏码")
	@PostMapping
	public Result<PerformanceShareVO> create(@Valid @RequestBody PerformanceShareDTO request,
			@org.springframework.web.bind.annotation.RequestParam Long taskId) {
		return Result.success(shareService.create(taskId, request));
	}

	@Operation(summary = "我的分享", description = "按时间倒序，最多 50 条")
	@GetMapping
	public Result<List<PerformanceShareVO>> list() {
		return Result.success(shareService.list());
	}

	@Operation(summary = "查看演奏码", description = "不导入也能先看到曲名、乐器、难度、时长与按键动作数")
	@GetMapping("/{shareCode}")
	public Result<PerformanceShareVO> describe(@PathVariable String shareCode) {
		return Result.success(shareService.describe(shareCode));
	}

	@Operation(summary = "导入演奏码", description = "在本库里还原成一首新曲目（复制一份，不影响原作者）")
	@PostMapping("/{shareCode}/import")
	public Result<PerformanceShareVO> importShare(@PathVariable String shareCode) {
		return Result.success(shareService.importShare(shareCode));
	}
}
