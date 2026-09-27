package com.kingdomstudio.modules.motion.template.controller;

import com.kingdomstudio.common.PageVO;
import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.motion.template.dto.CandidatePromoteDTO;
import com.kingdomstudio.modules.motion.template.dto.CandidateQueryDTO;
import com.kingdomstudio.modules.motion.template.dto.CandidateReviewDTO;
import com.kingdomstudio.modules.motion.template.service.MotionCandidateService;
import com.kingdomstudio.modules.motion.template.vo.CandidateStatsVO;
import com.kingdomstudio.modules.motion.template.vo.MotionCandidateVO;
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

import java.util.Map;

/**
 * 候选池接口。
 *
 * <p>四步流程在这里各有落点：发现（采集脚本写入）→ 分析（analyze）→ 人工筛选（review）→ 转换（promote）。
 */
@Tag(name = "10.3 动效候选池", description = "GitHub 发现的候选资源：分析、筛选与转成 Motion Pattern")
@RestController
@RequestMapping("/motion/candidates")
@RequiredArgsConstructor
public class MotionCandidateController {

	private final MotionCandidateService candidateService;

	@Operation(summary = "候选分页", description = "状态 / 分类 / 技术 / 关键词 / 星数下限可任意组合")
	@GetMapping
	public Result<PageVO<MotionCandidateVO>> page(CandidateQueryDTO query) {
		return Result.success(candidateService.page(query));
	}

	@Operation(summary = "候选池概览", description = "总数 / 各状态 / 各分类，以及资源库的官方与社区配比")
	@GetMapping("/stats")
	public Result<CandidateStatsVO> stats() {
		return Result.success(candidateService.stats());
	}

	@Operation(summary = "重新分析", description = "按关键词权重表重跑分类、触发方式、难度与运行档位")
	@PostMapping("/{id}/analyze")
	public Result<MotionCandidateVO> analyze(@PathVariable Long id) {
		return Result.success(candidateService.analyze(id));
	}

	@Operation(summary = "人工筛选", description = "改成 待看 / 已分析 / 已选入 / 已淘汰；淘汰必须写理由")
	@PostMapping("/{id}/review")
	public Result<MotionCandidateVO> review(@PathVariable Long id, @Valid @RequestBody CandidateReviewDTO request) {
		return Result.success(candidateService.review(id, request));
	}

	@Operation(summary = "转成模板",
			description = "指定用哪个内置 Pattern 承载：代码来自 Pattern，候选只提供名字、来源与许可；已入库的直接返回")
	@PostMapping("/{id}/promote")
	public Result<Map<String, Object>> promote(@PathVariable Long id, @Valid @RequestBody CandidatePromoteDTO request) {
		return Result.success(candidateService.promote(id, request));
	}
}
