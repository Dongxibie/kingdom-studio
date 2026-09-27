package com.kingdomstudio.modules.timeline.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 成长时间线查询条件 */
@Data
@Schema(description = "成长时间线查询")
public class TimelineQueryDTO {

	@Schema(description = "年份过滤", example = "2026")
	private Integer year;
}
