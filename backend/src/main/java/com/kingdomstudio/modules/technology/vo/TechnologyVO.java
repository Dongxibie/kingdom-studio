package com.kingdomstudio.modules.technology.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 技术卡片 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "技术卡片")
public class TechnologyVO {

	private Long id;

	private String name;

	@Schema(description = "分类码")
	private String category;

	@Schema(description = "分类中文")
	private String categoryLabel;

	@Schema(description = "掌握程度 1-5")
	private Integer level;

	@Schema(description = "掌握程度的星星文字，例如 ★★★★☆")
	private String levelText;

	private String description;

	@Schema(description = "用到的项目，已拆成数组")
	private List<String> usedProjects;

	private LocalDate learnDate;

	private String icon;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;
}
