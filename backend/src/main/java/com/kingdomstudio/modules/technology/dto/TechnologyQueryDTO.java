package com.kingdomstudio.modules.technology.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 技术图鉴查询条件 */
@Data
@Schema(description = "技术图鉴查询")
public class TechnologyQueryDTO {

	@Schema(description = "分类过滤：Java / Spring / Database / AI / Frontend / DevOps")
	private String category;

	@Schema(description = "关键词：匹配技术名、说明与项目应用")
	private String keyword;
}
