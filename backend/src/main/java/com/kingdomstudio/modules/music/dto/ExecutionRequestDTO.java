package com.kingdomstudio.modules.music.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 本机执行请求。
 *
 * <p>模式只有三种，默认最保守的那个。本机执行通道本轮**没有开启**：
 * 接口会明确拒绝并说明原因，而不是「假装执行」。
 */
@Data
@Schema(description = "本机执行请求")
public class ExecutionRequestDTO {

	@Schema(description = "模式：PREVIEW 只模拟（默认）/ MANUAL 逐条确认 / LOCAL 本机执行",
			example = "PREVIEW")
	private String mode;
}
