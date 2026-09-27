package com.kingdomstudio.modules.music.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 应用一套演奏方案 */
@Data
@Schema(description = "应用演奏方案")
public class StrategyApplyDTO {

	@NotBlank(message = "请选择要应用的方案")
	@Schema(description = "策略：BEGINNER 初学 / NORMAL 标准 / SHOWCASE 展示", example = "BEGINNER")
	private String strategy;
}
