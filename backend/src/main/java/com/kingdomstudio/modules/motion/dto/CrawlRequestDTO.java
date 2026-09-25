package com.kingdomstudio.modules.motion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/** 采集入参。 */
@Data
@Schema(description = "动效采集入参")
public class CrawlRequestDTO {

	@Schema(description = "搜索关键词，留空用默认的一组", example = "css animation hover")
	private List<String> keywords;

	@Schema(description = "每个关键词最多取多少条（建议 ≤30，搜索接口有速率限制）", example = "10")
	private Integer limitPerKeyword = 10;

	@Schema(description = "只试算不写库", example = "false")
	private Boolean dryRun = false;
}
