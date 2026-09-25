package com.kingdomstudio.modules.music.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 简谱解析入参 */
@Data
@Schema(description = "简谱解析入参")
public class JianpuParseDTO {

	@Schema(description = "曲子名，留空则取「未命名简谱」", example = "小星星")
	@Size(max = 160, message = "曲子名最长 160 个字符")
	private String name;

	@NotBlank(message = "请粘贴简谱内容")
	@Size(max = 20000, message = "简谱内容过长，请分段解析")
	@Schema(description = "简谱文本，首行可写 1=C 4/4 BPM=96", example = "1=C 4/4 BPM=96 | 5 5 6 5 | 1' 7 6 5")
	private String jianpu;
}
