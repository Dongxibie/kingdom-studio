package com.kingdomstudio.modules.motion.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 把候选转成模板的入参。
 *
 * <p>关键是 {@code patternTemplateKey}：**实现代码必须来自我们自己的 Pattern**，
 * 接口不接受任何外部代码，也不去读候选仓库的源码。候选只提供「叫什么、从哪来、什么许可」。
 */
@Data
@Schema(description = "候选转模板")
public class CandidatePromoteDTO {

	@NotBlank(message = "请选择承载这个候选的 Motion Pattern")
	@Schema(description = "用哪个内置模板的实现来承载它（模板 key）", example = "text-reveal")
	private String patternTemplateKey;

	@Schema(description = "模板名，留空用候选名")
	private String name;

	@Schema(description = "说明，留空用候选描述")
	private String description;
}
