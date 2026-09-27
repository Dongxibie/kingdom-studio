package com.kingdomstudio.modules.timeline.controller;

import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.timeline.dto.TimelineQueryDTO;
import com.kingdomstudio.modules.timeline.dto.TimelineSaveDTO;
import com.kingdomstudio.modules.timeline.service.TimelineService;
import com.kingdomstudio.modules.timeline.vo.TimelineNodeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 成长时间线接口：一条按时间倒序的成长路径 */
@Tag(name = "03. 成长时间线", description = "按年份与具体日期记录的成长节点")
@RestController
@RequestMapping("/timeline")
@Validated
@RequiredArgsConstructor
public class TimelineController {

	private final TimelineService timelineService;

	@Operation(summary = "成长节点列表", description = "按年份倒序；同一年里具体日期的节点排在年度节点之前")
	@GetMapping
	public Result<List<TimelineNodeVO>> list(@Valid TimelineQueryDTO query) {
		return Result.success(timelineService.list(query));
	}

	@Operation(summary = "新增成长节点")
	@PostMapping
	public Result<Long> create(@Valid @RequestBody TimelineSaveDTO request) {
		return Result.success("成长节点已记录", timelineService.create(request));
	}

	@Operation(summary = "修改成长节点")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TimelineSaveDTO request) {
		timelineService.update(id, request);
		return Result.success("成长节点已更新", null);
	}

	@Operation(summary = "删除成长节点", description = "逻辑删除：数据保留，时间线不再展示")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable Long id) {
		timelineService.delete(id);
		return Result.success("成长节点已删除", null);
	}
}
