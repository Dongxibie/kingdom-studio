package com.kingdomstudio.modules.motion.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模板检索条件。
 *
 * <p>四组筛选（分组 / 场景 / 风格 / 技术）与关键词可以任意组合；
 * 场景、风格、技术这三组同时也是左侧「发现方式」的选项来源。
 */
@Data
@Schema(description = "模板检索条件")
public class TemplateQueryDTO {

	@Schema(description = "分组：基础交互 / 产品页面 / 高级效果")
	private String category;

	@Schema(description = "场景：Landing Page / Dashboard / Portfolio / Login / AI SaaS / Game UI")
	private String scene;

	@Schema(description = "风格：Minimal / Luxury / Cyber / Glass / Organic")
	private String style;

	@Schema(description = "技术：CSS / GSAP / Framer Motion / Three.js / Canvas")
	private String technology;

	@Schema(description = "难度：1 入门 / 2 进阶 / 3 高阶")
	private Integer difficulty;

	@Schema(description = "关键词，匹配名称、说明、标签、适合场景")
	private String keyword;

	@Schema(description = "排序：SCORE 推荐指数（默认）/ NAME 名称 / DIFFICULTY 难度")
	private String sort;

	@Schema(description = "页码，从 1 开始", example = "1")
	private Long page;

	@Schema(description = "每页条数，最大 100", example = "12")
	private Long size;
}
