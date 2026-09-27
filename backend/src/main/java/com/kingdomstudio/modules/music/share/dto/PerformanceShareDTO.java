package com.kingdomstudio.modules.music.share.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 生成分享码的入参 */
@Data
@Schema(description = "生成曲谱分享")
public class PerformanceShareDTO {

	@Size(max = 48, message = "署名最长 48 个字符")
	@Schema(description = "分享者署名", example = "李泽龙")
	private String creator;

	@Size(max = 120, message = "标题最长 120 个字符")
	@Schema(description = "曲目名，留空就用曲目原本的名字")
	private String title;

	@Schema(description = "要分享的演奏计划 id；留空则用该曲目最近一次生成的计划", example = "12")
	private Long planId;

	@Schema(description = "演奏方案 id：带上它，快照里会记录速度与最小间隔", example = "3")
	private Long presetId;
}
