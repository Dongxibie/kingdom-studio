package com.kingdomstudio.modules.project.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目（project 表）。
 *
 * <p>项目王国里的「一座建筑」：名称、简介、技术栈、完成度、仓库与演示地址，
 * 以及用 Markdown 写的项目亮点。
 */
@Data
@TableName("project")
public class Project {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String name;

	private String description;

	/** 封面图地址 */
	private String coverImage;

	/** 技术栈，英文逗号分隔 */
	private String technologyStack;

	private String githubUrl;

	/** 演示地址 */
	private String demoUrl;

	/** PLANNING 规划中 / DEVELOPING 开发中 / COMPLETED 已完成 */
	private String status;

	/** 完成度百分比 0-100 */
	private Integer progress;

	/** 代码行数（非空行），统计不出来时为空 */
	private Integer codeLines;

	/** 自动化测试数量 */
	private Integer testCount;

	/** Git 提交数 */
	private Integer commitCount;

	/** 项目亮点，Markdown 文本 */
	private String highlights;

	/** 排序值，越小越靠前 */
	private Integer sortOrder;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
