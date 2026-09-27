package com.kingdomstudio.modules.motion.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** AI Motion Designer 的输入 */
@Data
@Schema(description = "设计需求输入")
public class MotionDesignDTO {

	@NotBlank(message = "请描述你想做的页面")
	@Size(max = 200, message = "需求最长 200 个字符")
	@Schema(description = "自然语言需求，例如「帮我设计一个科技公司首页」",
			example = "帮我设计一个科技公司首页")
	private String query;

	@Schema(description = "性能预算：LOW 轻量优先 / MEDIUM 均衡 / HIGH 效果优先；留空则按需求里说的话判断")
	private String performance;

	@Schema(description = "方案最多用几个动效，默认 5，范围 1-8", example = "5")
	private Integer maxSteps;
}
