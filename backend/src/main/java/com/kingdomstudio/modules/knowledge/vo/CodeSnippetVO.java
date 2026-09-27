package com.kingdomstudio.modules.knowledge.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 代码片段列表项：不带代码正文，列表页不需要把整段代码拉回来 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "代码片段列表项")
public class CodeSnippetVO {

	private Long id;

	private String title;

	private String language;

	private String description;

	@Schema(description = "标签，已拆成数组")
	private List<String> tags;

	@Schema(description = "代码行数：列表上用来判断这段有多长")
	private Integer lineCount;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;
}
