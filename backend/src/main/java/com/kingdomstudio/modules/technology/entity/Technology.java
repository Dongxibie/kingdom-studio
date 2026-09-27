package com.kingdomstudio.modules.technology.entity;

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
 * 技术（technology 表）。
 *
 * <p>技术图鉴里的一张卡：这套技术练到什么程度、用在了哪些项目上、什么时候开始学的。
 */
@Data
@TableName("technology")
public class Technology {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String name;

	/** 分类：Java / Spring / Database / AI / Frontend / DevOps */
	private String category;

	/** 掌握程度 1-5 星 */
	private Integer level;

	/** 说明 / 学习心得 */
	private String description;

	/** 项目应用，英文逗号分隔 */
	private String usedProjects;

	/** 开始学习日期 */
	private LocalDate learnDate;

	private String icon;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
