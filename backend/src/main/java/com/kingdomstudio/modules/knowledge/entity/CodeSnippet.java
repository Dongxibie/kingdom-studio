package com.kingdomstudio.modules.knowledge.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代码片段（code_snippet 表）。
 *
 * <p>代码知识库存的是「解决方案」而不是代码仓库：一段能直接抄走的代码，
 * 加一句「什么场景下用」。
 */
@Data
@TableName("code_snippet")
public class CodeSnippet {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String title;

	/** 语言 / 分类：Java / Vue / AI / SQL / 工具 */
	private String language;

	private String description;

	private String codeContent;

	/** 标签，英文逗号分隔 */
	private String tags;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
