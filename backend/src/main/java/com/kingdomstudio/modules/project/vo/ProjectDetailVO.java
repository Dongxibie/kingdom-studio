package com.kingdomstudio.modules.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 项目详情：在列表项之上补一份 Markdown 项目亮点 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "项目详情")
public class ProjectDetailVO {

	private Long id;

	private String name;

	private String description;

	private String coverImage;

	private List<String> technologyStack;

	private String githubUrl;

	private String demoUrl;

	private String status;

	private String statusLabel;

	private Integer progress;

	@Schema(description = "代码行数（非空行）")
	private Integer codeLines;

	@Schema(description = "自动化测试数量")
	private Integer testCount;

	@Schema(description = "Git 提交数")
	private Integer commitCount;

	@Schema(description = "项目亮点，Markdown 文本")
	private String highlights;

	private Integer sortOrder;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;
}
