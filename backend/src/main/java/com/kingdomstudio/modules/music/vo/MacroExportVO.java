package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 导出结果：一份可以直接存下来的脚本文本 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "演奏脚本导出")
public class MacroExportVO {

	private Long planId;
	private Long taskId;

	@Schema(description = "格式：TXT / AHK / JSON")
	private String format;

	@Schema(description = "建议的文件名")
	private String filename;

	@Schema(description = "内容类型")
	private String contentType;

	@Schema(description = "字符数")
	private Integer size;

	@Schema(description = "脚本内容")
	private String content;

	@Schema(description = "格式说明与使用提醒")
	private String hint;
}
