package com.kingdomstudio.modules.motion.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 顶部搜索栏 / 助手的输入 */
@Data
@Schema(description = "动效搜索输入")
public class MotionSearchDTO {

	@NotBlank(message = "请输入你想做的效果或场景")
	@Size(max = 200, message = "搜索内容最长 200 个字符")
	@Schema(description = "自然语言描述，例如「苹果官网风格的 Hero 动画」",
			example = "做一个苹果官网风格的 Hero 动画")
	private String query;

	@Schema(description = "最多返回几条推荐", example = "6")
	private Integer limit;
}
