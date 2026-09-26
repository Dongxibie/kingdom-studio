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
 * 动效组合方案（对应表 motion_recipe）。
 *
 * <p>一个方案把若干模板按应用顺序叠起来。单个动效是素材，组合才是「场景级的答案」，
 * 这也是这个模块的核心判断：真实网页的高级感来自组合，而不是某个孤立模板。
 */
@Data
@TableName("motion_recipe")
public class MotionRecipe {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String recipeKey;

	private String name;

	private String description;

	private String scene;

	private String style;

	private String bestFor;

	/** 推荐指数：组成模板的加权平均，由服务端算出 */
	private Integer score;

	/** 包含的模板 template_key JSON 数组，按应用顺序 */
	private String templateKeys;

	private String prompt;

	private String status;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
