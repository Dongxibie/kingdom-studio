package com.kingdomstudio.modules.motion.controller;

import com.kingdomstudio.common.PageVO;
import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.motion.dto.MotionCodeSaveDTO;
import com.kingdomstudio.modules.motion.dto.MotionQueryDTO;
import com.kingdomstudio.modules.motion.dto.MotionSaveDTO;
import com.kingdomstudio.modules.motion.service.MotionService;
import com.kingdomstudio.modules.motion.vo.MotionDetailVO;
import com.kingdomstudio.modules.motion.vo.MotionListItemVO;
import com.kingdomstudio.modules.motion.vo.MotionModuleVO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 动效基因库接口。
 *
 * <p>控制器只做接收与转发，业务规则在 {@link MotionService}。
 */
@Tag(name = "10. 动效基因库", description = "AI 动效基因库：GitHub / Web 动效的采集、分类、去重、预览与代码生成")
@RestController
@RequestMapping("/motion")
@RequiredArgsConstructor
public class MotionController {

	private final MotionService motionService;

	@Operation(summary = "模块自检", description = "返回模块名称、当前阶段与能力清单")
	@GetMapping("/ping")
	public Result<MotionModuleVO> ping() {
		return Result.success(motionService.moduleInfo());
	}

	@Operation(summary = "分类列表", description = "十个固定分类，前端分类树可直接使用")
	@GetMapping("/categories")
	public Result<List<String>> categories() {
		return Result.success(motionService.categories());
	}

	@Operation(summary = "动效列表", description = "支持分类 / 技术栈 / 关键词筛选与分页")
	@GetMapping
	public Result<PageVO<MotionListItemVO>> page(MotionQueryDTO query) {
		return Result.success(motionService.page(query));
	}

	@Operation(summary = "动效详情", description = "含四种代码产物与提示词")
	@GetMapping("/{id}")
	public Result<MotionDetailVO> detail(@PathVariable Long id) {
		return Result.success(motionService.detail(id));
	}

	@Operation(summary = "新增动效")
	@PostMapping
	public Result<Long> create(@Valid @RequestBody MotionSaveDTO dto) {
		return Result.success("新增成功", motionService.create(dto));
	}

	@Operation(summary = "修改动效")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MotionSaveDTO dto) {
		motionService.update(id, dto);
		return Result.success("修改成功", null);
	}

	@Operation(summary = "删除动效", description = "逻辑删除，其代码记录一并删除")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable Long id) {
		motionService.delete(id);
		return Result.success("删除成功", null);
	}

	@Operation(summary = "保存代码产物", description = "Prompt / Vue / React / CSS / Three.js 五种，一条资源一条记录")
	@PutMapping("/{id}/code")
	public Result<Void> saveCode(@PathVariable Long id, @RequestBody MotionCodeSaveDTO dto) {
		motionService.saveCode(id, dto);
		return Result.success("保存成功", null);
	}
}
