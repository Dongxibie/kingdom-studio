package com.kingdomstudio.modules.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 代码片段查询条件 */
@Data
@Schema(description = "代码片段查询")
public class CodeSnippetQueryDTO {

	@Schema(description = "关键词：匹配标题、说明、标签与语言")
	private String keyword;

	@Schema(description = "语言 / 分类过滤", example = "SQL")
	private String language;

	@Schema(description = "页码，从 1 开始", example = "1")
	@Min(value = 1, message = "页码从 1 开始")
	private Integer page = 1;

	@Schema(description = "每页条数，最大 60", example = "20")
	@Min(value = 1, message = "每页至少 1 条")
	@Max(value = 60, message = "每页最多 60 条")
	private Integer size = 20;
}
