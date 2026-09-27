package com.kingdomstudio.modules.timeline.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成长节点（timeline 表）。
 *
 * <p>年度节点只填年份（eventDate 留空），具体到某天的事才填 eventDate；
 * 界面上精确节点排在年度节点前面。
 */
@Data
@TableName("timeline")
public class TimelineEvent {

	@TableId(type = IdType.AUTO)
	private Long id;

	private Integer year;

	/** 精确到日的时间点，年度节点留空 */
	private LocalDate eventDate;

	private String title;

	private String description;

	/** 关联项目名 */
	private String relatedProject;

	/** 阶段等级 1-5 */
	private Integer level;

	/** 同一年内的排序值 */
	private Integer sortOrder;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
