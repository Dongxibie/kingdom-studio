package com.kingdomstudio.modules.knowledge.controller;

import com.kingdomstudio.common.PageVO;
import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.knowledge.dto.CodeSnippetQueryDTO;
import com.kingdomstudio.modules.knowledge.dto.CodeSnippetSaveDTO;
import com.kingdomstudio.modules.knowledge.service.CodeSnippetService;
import com.kingdomstudio.modules.knowledge.vo.CodeSnippetDetailVO;
import com.kingdomstudio.modules.knowledge.vo.CodeSnippetVO;
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

/** 代码知识库接口：可复用解决方案的沉淀与检索 */
@Tag(name = "04. 代码知识库", description = "代码片段：场景说明、代码正文、标签检索与增删改")
@RestController
@RequestMapping("/code-snippets")
@Validated
@RequiredArgsConstructor
public class CodeSnippetController {

	private final CodeSnippetService codeSnippetService;

	@Operation(summary = "代码片段分页列表", description = "支持关键词与语言过滤；列表不返回代码正文")
	@GetMapping
	public Result<PageVO<CodeSnippetVO>> page(@Valid CodeSnippetQueryDTO query) {
		return Result.success(codeSnippetService.page(query));
	}

	@Operation(summary = "代码片段详情", description = "含完整代码正文")
	@GetMapping("/{id}")
	public Result<CodeSnippetDetailVO> detail(@PathVariable Long id) {
		return Result.success(codeSnippetService.detail(id));
	}

	@Operation(summary = "新增代码片段")
	@PostMapping
	public Result<Long> create(@Valid @RequestBody CodeSnippetSaveDTO request) {
		return Result.success("片段已保存", codeSnippetService.create(request));
	}

	@Operation(summary = "修改代码片段")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CodeSnippetSaveDTO request) {
		codeSnippetService.update(id, request);
		return Result.success("片段已更新", null);
	}

	@Operation(summary = "删除代码片段", description = "逻辑删除：数据保留，列表不再展示")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable Long id) {
		codeSnippetService.delete(id);
		return Result.success("片段已删除", null);
	}
}
