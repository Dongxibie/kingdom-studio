package com.kingdomstudio.modules.motion.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 左侧「发现方式」的选项与数量 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发现方式分面")
public class MotionFacetVO {

	private List<FacetOption> scenes;
	private List<FacetOption> styles;
	private List<FacetOption> technologies;
	private List<FacetOption> categories;
	private List<FacetOption> difficulties;

	@Schema(description = "按运行档位（性能成本）分面")
	private List<FacetOption> runtimeTiers;

	@Schema(description = "按来源分面：官方 / 社区精选")
	private List<FacetOption> sources;

	@Schema(description = "按触发方式分面：加载时 / 悬停 / 滚动 / 点击")
	private List<FacetOption> triggers;

	@Schema(description = "模板总数")
	private Long total;

	@Schema(description = "组合方案数")
	private Long recipeTotal;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "一个选项")
	public static class FacetOption {

		@Schema(description = "选项值")
		private String value;

		@Schema(description = "中文标签（场景与风格有对照表）")
		private String label;

		@Schema(description = "该选项下的模板数")
		private Long count;

		@Schema(description = "该选项下模板的平均推荐指数")
		private Integer averageScore;
	}
}
