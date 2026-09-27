package com.kingdomstudio.modules.motion.template.vo;

import com.kingdomstudio.modules.motion.template.recommend.MotionIntent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 智能推荐结果：意图 + Top N + 每条推荐的理由 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动效智能推荐结果")
public class MotionRecommendVO {

	@Schema(description = "原始需求")
	private String query;

	@Schema(description = "解析出的意图（五轴）")
	private MotionIntent intent;

	@Schema(description = "参与打分的模板总数")
	private Integer scanned;

	private List<Item> recommendations;

	/** 一条推荐 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "一条推荐")
	public static class Item {

		private String templateKey;
		private String name;
		private String category;
		private String description;

		private String sceneLabel;
		private String styleLabel;
		private String technology;
		private String triggerLabel;

		private String runtimeTier;
		private String runtimeTierLabel;

		@Schema(description = "性能等级：A 轻量 / B 均衡 / C 依赖 GPU 加速")
		private String performanceGrade;

		@Schema(description = "本次推荐得分（各轴加权求和）")
		private Integer score;

		@Schema(description = "模板自身的推荐指数")
		private Integer recommendScore;

		private Double stars;

		@Schema(description = "命中了的轴：scene / style / emotion / performance / interaction")
		private List<String> matchedAxes;

		@Schema(description = "逐条理由，界面右侧直接展示")
		private List<String> reasons;
	}
}
