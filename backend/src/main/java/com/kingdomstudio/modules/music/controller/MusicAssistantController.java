package com.kingdomstudio.modules.music.controller;

import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.music.dto.MusicPromptDTO;
import com.kingdomstudio.modules.music.dto.StrategyApplyDTO;
import com.kingdomstudio.modules.music.service.MusicAssistantService;
import com.kingdomstudio.modules.music.vo.MusicAssistantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 音乐助手接口。
 *
 * <p>它只做两件事：理解一句要求（analyze），把这套方案落成一条新曲目（apply）。
 * 模型负责理解、规则负责改写、结果一律过校验 —— 不会把模型输出直接执行。
 */
@Tag(name = "11.4 AI 音乐助手", description = "自然语言 → 演奏方案（初学 / 标准 / 展示）+ 逐条解释")
@RestController
@RequestMapping("/music/tasks/{taskId}/assistant")
@RequiredArgsConstructor
public class MusicAssistantController {

	private final MusicAssistantService assistantService;

	@Operation(summary = "理解一句要求",
			description = "返回难度档、目标乐器、建议清单与逐条改动原因；模型不可用时回退到规则判断并如实说明")
	@PostMapping("/analyze")
	public Result<MusicAssistantVO> analyze(@PathVariable Long taskId,
			@Valid @RequestBody MusicPromptDTO request) {
		return Result.success(assistantService.analyze(taskId, request));
	}

	@Operation(summary = "应用方案",
			description = "把调整后的曲子落成一条新曲目（如「小星星（初学版）」），原曲不动")
	@PostMapping("/apply")
	public Result<MusicAssistantVO> apply(@PathVariable Long taskId,
			@Valid @RequestBody StrategyApplyDTO request) {
		return Result.success(assistantService.apply(taskId, request));
	}
}
