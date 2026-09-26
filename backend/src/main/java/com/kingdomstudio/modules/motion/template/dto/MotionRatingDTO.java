package com.kingdomstudio.modules.motion.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 人工评分入参 */
@Data
@Schema(description = "评分入参")
public class MotionRatingDTO {

	@NotBlank(message = "请指定评分对象类型：TEMPLATE / RECIPE")
	@Pattern(regexp = "TEMPLATE|RECIPE", message = "类型只能是 TEMPLATE 或 RECIPE")
	private String targetType;

	@NotBlank(message = "请指定评分对象的 key")
	@Size(max = 64, message = "key 最长 64 个字符")
	private String targetKey;

	@Min(value = 1, message = "评分范围 1-5 星")
	@Max(value = 5, message = "评分范围 1-5 星")
	private Integer score;

	@Size(max = 400, message = "理由最长 400 个字符")
	private String reason;
}
