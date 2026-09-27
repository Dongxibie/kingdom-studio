package com.kingdomstudio.modules.knowledge.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 代码片段详情：带完整代码正文 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "代码片段详情")
public class CodeSnippetDetailVO {

	private Long id;

	private String title;

	private String language;

	private String description;

	private List<String> tags;

	@Schema(description = "代码内容")
	private String codeContent;

	private Integer lineCount;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;
}
