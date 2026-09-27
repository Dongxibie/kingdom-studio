package com.kingdomstudio.modules.motion.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 候选池检索条件：状态 / 分类 / 技术 / 关键词 / 星数下限可任意组合 */
@Data
@Schema(description = "候选池检索条件")
public class CandidateQueryDTO {

	@Schema(description = "状态：NEW / ANALYZED / SELECTED / PROMOTED / REJECTED")
	private String status;

	@Schema(description = "建议分类：文字动画 / 卡片交互 / 按钮交互 / 滚动动画 / 首屏动画 / 背景效果 / 三维 WebGL")
	private String category;

	@Schema(description = "技术线索：CSS / Canvas / Three.js / WebGL")
	private String technology;

	@Schema(description = "关键词，匹配名称、仓库全名、说明、主题")
	private String keyword;

	@Schema(description = "星数下限，默认 0")
	private Integer minStars;

	@Schema(description = "排序：STARS 星数（默认）/ VISUAL 视觉分 / NAME 名称")
	private String sort;

	@Schema(description = "页码，从 1 开始", example = "1")
	private Long page;

	@Schema(description = "每页条数，最大 100", example = "12")
	private Long size;
}
