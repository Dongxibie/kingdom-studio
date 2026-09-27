package com.kingdomstudio.modules.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 项目新增 / 修改入参 */
@Data
@Schema(description = "项目新增或修改")
public class ProjectSaveDTO {

	@Schema(description = "项目名称", example = "Kingdom Studio")
	@NotBlank(message = "项目名称不能为空")
	@Size(max = 100, message = "项目名称不能超过 100 字")
	private String name;

	@Schema(description = "项目简介")
	@Size(max = 500, message = "项目简介不能超过 500 字")
	private String description;

	@Schema(description = "封面图地址")
	@Size(max = 255, message = "封面图地址不能超过 255 字")
	private String coverImage;

	@Schema(description = "技术栈，英文逗号分隔", example = "Java,Spring Boot,MySQL")
	@Size(max = 255, message = "技术栈不能超过 255 字")
	private String technologyStack;

	@Schema(description = "GitHub 地址")
	@Size(max = 255, message = "GitHub 地址不能超过 255 字")
	private String githubUrl;

	@Schema(description = "演示地址")
	@Size(max = 255, message = "演示地址不能超过 255 字")
	private String demoUrl;

	@Schema(description = "状态：PLANNING 规划中 / DEVELOPING 开发中 / COMPLETED 已完成", example = "DEVELOPING")
	@Pattern(regexp = "PLANNING|DEVELOPING|COMPLETED", message = "状态只能是 PLANNING / DEVELOPING / COMPLETED")
	private String status;

	@Schema(description = "完成度百分比 0-100", example = "60")
	@Min(value = 0, message = "完成度不能小于 0")
	@Max(value = 100, message = "完成度不能大于 100")
	private Integer progress;

	@Schema(description = "代码行数（非空行）", example = "30955")
	@Min(value = 0, message = "代码行数不能为负")
	private Integer codeLines;

	@Schema(description = "自动化测试数量", example = "333")
	@Min(value = 0, message = "测试数量不能为负")
	private Integer testCount;

	@Schema(description = "Git 提交数", example = "17")
	@Min(value = 0, message = "提交数不能为负")
	private Integer commitCount;

	@Schema(description = "项目亮点，Markdown 文本")
	private String highlights;

	@Schema(description = "排序值，越小越靠前", example = "1")
	private Integer sortOrder;
}
