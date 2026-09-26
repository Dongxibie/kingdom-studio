package com.kingdomstudio.modules.motion.template.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评分与理由（对应表 motion_rating）。
 *
 * <p>模板的推荐指数是算法按四项子分算出来的；这张表记的是「人的复核评分与理由」。
 * 两者分开存是有意的：一个是可复现的口径，一个是判断依据，混在一起就没法解释分数从哪来。
 */
@Data
@TableName("motion_rating")
public class MotionRating {

	@TableId(type = IdType.AUTO)
	private Long id;

	/** TEMPLATE / RECIPE */
	private String targetType;

	/** 对象的 template_key / recipe_key */
	private String targetKey;

	/** 人工评分 1-5 星 */
	private Integer score;

	private String reason;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
