package com.kingdomstudio.modules.music.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 按键映射请求 */
@Data
@Schema(description = "按键映射请求")
public class MappingRequestDTO {

	@NotNull(message = "请选择乐器档案")
	@Schema(description = "乐器档案 id")
	private Long profileId;

	@Schema(description = "临时覆盖超范围策略：SKIP / NEAREST / SHIFT_OCTAVE；不填则用档案里的设置")
	private String strategy;
}
