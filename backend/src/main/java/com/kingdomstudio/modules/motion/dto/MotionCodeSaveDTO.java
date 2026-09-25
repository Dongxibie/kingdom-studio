package com.kingdomstudio.modules.motion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 保存某条动效的代码产物（四种代码 + 提示词）。 */
@Data
@Schema(description = "动效代码保存入参")
public class MotionCodeSaveDTO {

	@Schema(description = "生成该动效的提示词")
	private String prompt;

	@Schema(description = "Vue 3 代码")
	private String vueCode;

	@Schema(description = "React 代码")
	private String reactCode;

	@Schema(description = "纯 CSS 代码")
	private String cssCode;

	@Schema(description = "Three.js 代码")
	private String threeCode;
}
