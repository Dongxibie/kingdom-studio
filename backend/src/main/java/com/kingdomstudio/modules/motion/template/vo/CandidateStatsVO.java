package com.kingdomstudio.modules.motion.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** 候选池概览：一共发现多少、各状态多少、各类多少 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "候选池概览")
public class CandidateStatsVO {

	private Long total;

	@Schema(description = "各状态数量：NEW / ANALYZED / SELECTED / PROMOTED / REJECTED")
	private Map<String, Long> byStatus;

	@Schema(description = "各分类数量")
	private Map<String, Long> byCategory;

	private Long libraryTotal;

	@Schema(description = "资源库里官方模板数")
	private Long officialTotal;

	@Schema(description = "资源库里社区精选数")
	private Long communityTotal;
}
