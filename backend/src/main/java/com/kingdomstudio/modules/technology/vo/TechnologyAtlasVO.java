package com.kingdomstudio.modules.technology.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 技术图鉴整页数据：分类导航（含数量）+ 当前筛选下的技术卡片 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "技术图鉴整页数据")
public class TechnologyAtlasVO {

	@Schema(description = "全部分类及数量，用于左侧导航；第一个是「全部」")
	private List<CategoryVO> categories;

	@Schema(description = "当前筛选下的技术卡片")
	private List<TechnologyVO> items;

	@Schema(description = "当前筛选下的总数")
	private Integer total;

	/** 分类 + 该分类下的技术数量 */
	@Data
	@Builder
	@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "技术分类")
	public static class CategoryVO {

		@Schema(description = "分类码，ALL 表示全部")
		private String category;

		@Schema(description = "分类中文")
		private String label;

		@Schema(description = "该分类下的技术数量")
		private Integer count;
	}
}
