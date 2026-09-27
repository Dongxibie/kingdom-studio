package com.kingdomstudio.modules.timeline.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 成长时间线上的一个节点 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "成长节点")
public class TimelineNodeVO {

	private Long id;

	private Integer year;

	@Schema(description = "精确到日的时间点，年度节点为空")
	private LocalDate eventDate;

	@Schema(description = "展示用的时间文字：有具体日期就用日期，否则用年份")
	private String timeText;

	private String title;

	private String description;

	private String relatedProject;

	@Schema(description = "阶段等级 1-5")
	private Integer level;

	private Integer sortOrder;

	private LocalDateTime createTime;
}
