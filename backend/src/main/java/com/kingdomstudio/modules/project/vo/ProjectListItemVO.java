package com.kingdomstudio.modules.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 项目列表项：卡片上要展示的全部信息 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "项目列表项")
public class ProjectListItemVO {

	private Long id;

	private String name;

	private String description;

	@Schema(description = "封面图地址")
	private String coverImage;

	@Schema(description = "技术栈，已拆成数组")
	private List<String> technologyStack;

	private String githubUrl;

	private String demoUrl;

	@Schema(description = "状态码：PLANNING / DEVELOPING / COMPLETED")
	private String status;

	@Schema(description = "状态中文：规划中 / 持续开发 / 已完成")
	private String statusLabel;

	@Schema(description = "完成度百分比")
	private Integer progress;

	@Schema(description = "代码行数（非空行）")
	private Integer codeLines;

	@Schema(description = "自动化测试数量")
	private Integer testCount;

	@Schema(description = "Git 提交数")
	private Integer commitCount;

	private Integer sortOrder;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;
}
