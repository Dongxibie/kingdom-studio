package com.kingdomstudio.modules.motion.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 人工筛选意见。
 *
 * <p>只允许在 NEW / ANALYZED / SELECTED / REJECTED 之间改；
 * PROMOTED 是「已经变成模板」的事实状态，只能由提升接口产生，不能手填。
 */
@Data
@Schema(description = "候选筛选意见")
public class CandidateReviewDTO {

	@NotBlank(message = "请选择要改成的状态")
	@Schema(description = "目标状态：ANALYZED / SELECTED / REJECTED / NEW")
	private String status;

	@Schema(description = "意见；淘汰时必填，说明为什么不要它")
	private String note;
}
