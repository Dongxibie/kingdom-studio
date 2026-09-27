package com.kingdomstudio.modules.technology.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/** 技术新增 / 修改入参 */
@Data
@Schema(description = "技术新增或修改")
public class TechnologySaveDTO {

	@Schema(description = "技术名称", example = "Spring Boot 3")
	@NotBlank(message = "技术名称不能为空")
	@Size(max = 50, message = "技术名称不能超过 50 字")
	private String name;

	@Schema(description = "分类：Java / Spring / Database / AI / Frontend / DevOps", example = "Spring")
	@NotBlank(message = "分类不能为空")
	@Pattern(regexp = "Java|Spring|Database|AI|Frontend|DevOps", message = "分类只能是 Java / Spring / Database / AI / Frontend / DevOps")
	private String category;

	@Schema(description = "掌握程度 1-5 星", example = "4")
	@Min(value = 1, message = "掌握程度至少 1 星")
	@Max(value = 5, message = "掌握程度最多 5 星")
	private Integer level;

	@Schema(description = "说明 / 学习心得")
	@Size(max = 500, message = "说明不能超过 500 字")
	private String description;

	@Schema(description = "项目应用，英文逗号分隔")
	@Size(max = 255, message = "项目应用不能超过 255 字")
	private String usedProjects;

	@Schema(description = "开始学习日期", example = "2024-04-01")
	private LocalDate learnDate;

	@Schema(description = "图标标识")
	@Size(max = 255, message = "图标标识不能超过 255 字")
	private String icon;
}
