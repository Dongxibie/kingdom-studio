package com.kingdomstudio.modules.project.controller;

import com.kingdomstudio.common.PageVO;
import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.project.dto.ProjectQueryDTO;
import com.kingdomstudio.modules.project.dto.ProjectSaveDTO;
import com.kingdomstudio.modules.project.service.ProjectService;
import com.kingdomstudio.modules.project.vo.ProjectDetailVO;
import com.kingdomstudio.modules.project.vo.ProjectListItemVO;
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

/** 项目王国接口：个人项目的展示与维护 */
@Tag(name = "01. 项目王国", description = "个人项目展示与管理：列表、详情、新增、修改、删除")
@RestController
@RequestMapping("/projects")
@Validated
@RequiredArgsConstructor
public class ProjectController {

	private final ProjectService projectService;

	@Operation(summary = "项目分页列表", description = "支持关键词（名称 / 简介 / 技术栈）与状态过滤")
	@GetMapping
	public Result<PageVO<ProjectListItemVO>> page(@Valid ProjectQueryDTO query) {
		return Result.success(projectService.page(query));
	}

	@Operation(summary = "项目详情", description = "含 Markdown 项目亮点")
	@GetMapping("/{id}")
	public Result<ProjectDetailVO> detail(@PathVariable Long id) {
		return Result.success(projectService.detail(id));
	}

	@Operation(summary = "新增项目")
	@PostMapping
	public Result<Long> create(@Valid @RequestBody ProjectSaveDTO request) {
		return Result.success("项目已创建", projectService.create(request));
	}

	@Operation(summary = "修改项目")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProjectSaveDTO request) {
		projectService.update(id, request);
		return Result.success("项目已更新", null);
	}

	@Operation(summary = "删除项目", description = "逻辑删除：数据保留，列表不再展示")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable Long id) {
		projectService.delete(id);
		return Result.success("项目已删除", null);
	}
}
