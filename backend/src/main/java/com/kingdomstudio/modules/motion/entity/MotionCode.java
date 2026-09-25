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
 * 动效代码实体（对应表 motion_code）：一条资源对应四种代码产物加一份提示词。
 */
@Data
@TableName("motion_code")
public class MotionCode {

	@TableId(type = IdType.AUTO)
	private Long id;

	private Long motionId;

	private String prompt;

	private String vueCode;

	private String reactCode;

	private String cssCode;

	private String threeCode;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
