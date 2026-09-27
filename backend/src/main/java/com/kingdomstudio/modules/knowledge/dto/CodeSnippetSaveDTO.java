package com.kingdomstudio.modules.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 代码片段新增 / 修改入参 */
@Data
@Schema(description = "代码片段新增或修改")
public class CodeSnippetSaveDTO {

	@Schema(description = "片段标题", example = "MySQL 幂等 ALTER 模板")
	@NotBlank(message = "标题不能为空")
	@Size(max = 100, message = "标题不能超过 100 字")
	private String title;

	@Schema(description = "语言 / 分类：Java / Vue / AI / SQL / 工具", example = "SQL")
	@NotBlank(message = "语言不能为空")
	@Size(max = 20, message = "语言不能超过 20 字")
	private String language;

	@Schema(description = "说明：什么场景下用")
	@Size(max = 500, message = "说明不能超过 500 字")
	private String description;

	@Schema(description = "代码内容")
	@NotBlank(message = "代码内容不能为空")
	private String codeContent;

	@Schema(description = "标签，英文逗号分隔", example = "MySQL,迁移,幂等")
	@Size(max = 255, message = "标签不能超过 255 字")
	private String tags;
}
