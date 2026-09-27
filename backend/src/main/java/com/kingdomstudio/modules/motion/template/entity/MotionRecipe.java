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

	/**
	 * 组合步骤 JSON 数组，按应用顺序，每步带 stage（负责哪一层）与 role（这一步的作用）。
	 *
	 * <p>v1.1 只记「包含哪些模板」，界面能做的只有列出成员；但真实网页的组合是有分工的
	 * （谁铺底、谁给纵深、谁收尾），所以把「作用」也变成数据 —— 界面上那一列就是它。
	 * 为空时回退到 templateKeys：老数据仍能展示，只是没有作用说明。
	 */
	private String steps;

	private String prompt;

	private String status;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
