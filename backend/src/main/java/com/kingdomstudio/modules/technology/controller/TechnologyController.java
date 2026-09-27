package com.kingdomstudio.modules.technology.controller;

import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.technology.dto.TechnologyQueryDTO;
import com.kingdomstudio.modules.technology.dto.TechnologySaveDTO;
import com.kingdomstudio.modules.technology.service.TechnologyService;
import com.kingdomstudio.modules.technology.vo.TechnologyAtlasVO;
import com.kingdomstudio.modules.technology.vo.TechnologyVO;
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

/** 技术图鉴接口：技术知识资产的展示与维护 */
@Tag(name = "02. 技术图鉴", description = "技术卡片墙：分类、掌握程度、项目应用与增删改")
@RestController
@RequestMapping("/technologies")
@Validated
@RequiredArgsConstructor
public class TechnologyController {

	private final TechnologyService technologyService;

	@Operation(summary = "技术图鉴整页数据", description = "分类导航（含数量）与当前筛选下的技术卡片")
	@GetMapping
	public Result<TechnologyAtlasVO> atlas(@Valid TechnologyQueryDTO query) {
		return Result.success(technologyService.atlas(query));
	}

	@Operation(summary = "技术详情")
	@GetMapping("/{id}")
	public Result<TechnologyVO> detail(@PathVariable Long id) {
		return Result.success(technologyService.detail(id));
	}

	@Operation(summary = "新增技术")
	@PostMapping
	public Result<Long> create(@Valid @RequestBody TechnologySaveDTO request) {
		return Result.success("技术已记录", technologyService.create(request));
	}

	@Operation(summary = "修改技术")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TechnologySaveDTO request) {
		technologyService.update(id, request);
		return Result.success("技术已更新", null);
	}

	@Operation(summary = "删除技术", description = "逻辑删除：数据保留，列表不再展示")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable Long id) {
		technologyService.delete(id);
		return Result.success("技术已删除", null);
	}
}
