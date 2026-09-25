package com.kingdomstudio.modules.music.controller;

import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.music.dto.InstrumentProfileSaveDTO;
import com.kingdomstudio.modules.music.service.InstrumentProfileService;
import com.kingdomstudio.modules.music.vo.InstrumentProfileVO;
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

/** 乐器按键档案接口（Phase 4）。 */
@Tag(name = "11.2 乐器按键档案", description = "不同乐器的按键与音高对应关系：查询、新增、修改、删除")
@RestController
@RequestMapping("/music/instruments")
@RequiredArgsConstructor
public class InstrumentProfileController {

	private final InstrumentProfileService instrumentProfileService;

	@Operation(summary = "档案列表", description = "含展开后的键位音高与音名，前端画虚拟键盘直接用")
	@GetMapping
	public Result<List<InstrumentProfileVO>> list() {
		return Result.success(instrumentProfileService.list());
	}

	@Operation(summary = "档案详情")
	@GetMapping("/{id}")
	public Result<InstrumentProfileVO> detail(@PathVariable Long id) {
		return Result.success(instrumentProfileService.detail(id));
	}

	@Operation(summary = "新增档案", description = "按键顺序从最低音到最高音")
	@PostMapping
	public Result<Long> create(@Valid @RequestBody InstrumentProfileSaveDTO request) {
		return Result.success(instrumentProfileService.create(request));
	}

	@Operation(summary = "修改档案")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable Long id, @Valid @RequestBody InstrumentProfileSaveDTO request) {
		instrumentProfileService.update(id, request);
		return Result.success();
	}

	@Operation(summary = "删除档案")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable Long id) {
		instrumentProfileService.delete(id);
		return Result.success();
	}
}
