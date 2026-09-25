package com.kingdomstudio.modules.motion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 动效列表查询条件。
 *
 * <p>分页参数由前端传，Service 里做边界收敛（页码最小 1、每页 1~100），
 * 避免前端传 100000 这类值把库拖死。
 */
@Data
@Schema(description = "动效列表查询条件")
public class MotionQueryDTO {

	@Schema(description = "页码，从 1 开始", example = "1")
	private Integer page = 1;

	@Schema(description = "每页条数，最大 100", example = "20")
	private Integer size = 20;

	@Schema(description = "分类，ALL 或留空表示不限", example = "Hover")
	private String category;

	@Schema(description = "技术栈，留空表示不限", example = "CSS")
	private String technology;

	@Schema(description = "关键词：匹配名称、说明、标签", example = "玻璃")
	private String keyword;
}
