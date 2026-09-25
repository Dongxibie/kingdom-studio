package com.kingdomstudio.modules.motion.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动效资源实体（对应表 motion_resource）。
 *
 * <p>字段与 {@code db/extensions_motion.sql} 一一对应；
 * {@code deleted} 配合 application.yml 里配置的逻辑删除字段自动生效。
 */
@Data
@TableName("motion_resource")
public class MotionResource {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String name;

	private String description;

	/** 分类：十个固定值之一，写库前由 Service 校验并向数据库 CHECK 约束兜底 */
	private String category;

	private String technology;

	private String sourceUrl;

	private String repoUrl;

	private String previewUrl;

	/** 英文逗号分隔 */
	private String tags;

	private String license;

	private String codePath;

	/** 去重用：名称 + 来源 + 代码内容算出的哈希 */
	private String contentHash;

	private String status;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
