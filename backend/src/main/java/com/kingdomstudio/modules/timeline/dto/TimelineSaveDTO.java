package com.kingdomstudio.modules.timeline.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/** 成长节点新增 / 修改入参 */
@Data
@Schema(description = "成长节点新增或修改")
public class TimelineSaveDTO {

	@Schema(description = "年份", example = "2026")
	@NotNull(message = "年份不能为空")
	@Min(value = 2000, message = "年份不早于 2000")
	@Max(value = 2100, message = "年份不晚于 2100")
	private Integer year;

	@Schema(description = "精确到日的时间点，年度节点留空", example = "2026-09-27")
	private LocalDate eventDate;

	@Schema(description = "节点标题", example = "AI 演奏工作台 v1.2")
	@NotBlank(message = "节点标题不能为空")
	@Size(max = 100, message = "节点标题不能超过 100 字")
	private String title;

	@Schema(description = "节点描述")
	@Size(max = 500, message = "节点描述不能超过 500 字")
	private String description;

	@Schema(description = "关联项目名")
	@Size(max = 100, message = "关联项目名不能超过 100 字")
	private String relatedProject;

	@Schema(description = "阶段等级 1-5（对应王国晋升）", example = "5")
	@Min(value = 1, message = "阶段等级至少 1")
	@Max(value = 5, message = "阶段等级最多 5")
	private Integer level;

	@Schema(description = "同一年内的排序值", example = "10")
	private Integer sortOrder;
}
