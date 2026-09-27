package com.kingdomstudio.modules.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 项目列表查询条件 */
@Data
@Schema(description = "项目列表查询")
public class ProjectQueryDTO {

	@Schema(description = "关键词：匹配项目名、简介与技术栈")
	private String keyword;

	@Schema(description = "状态过滤：PLANNING / DEVELOPING / COMPLETED")
	private String status;

	@Schema(description = "页码，从 1 开始", example = "1")
	@Min(value = 1, message = "页码从 1 开始")
	private Integer page = 1;

	@Schema(description = "每页条数，最大 60", example = "12")
	@Min(value = 1, message = "每页至少 1 条")
	@Max(value = 60, message = "每页最多 60 条")
	private Integer size = 12;
}
