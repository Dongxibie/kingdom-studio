package com.kingdomstudio.modules.motion.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 组合方案：把若干模板按顺序叠成一个场景级方案 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动效组合方案")
public class MotionRecipeVO {

	private Long id;
	private String recipeKey;
	private String name;
	private String description;
	private String scene;

	@Schema(description = "场景中文名")
	private String sceneLabel;

	private String style;

	@Schema(description = "风格中文名")
	private String styleLabel;

	private List<String> bestFor;

	@Schema(description = "推荐指数：组成模板的加权平均（服务端重算，不直接读库里缓存值）")
	private Integer score;

	private Double stars;
	private String grade;

	/** 组成这个方案的模板（按应用顺序） */
	private List<MotionTemplateItemVO> members;

	private List<String> memberKeys;

	@Schema(description = "组合步骤：每一步的动画 / 作用 / 参数 / 性能成本，界面按这个渲染组合模式")
	private List<RecipeStepVO> steps;

	@Schema(description = "整体性能成本：最重档位、各档步数与降级建议")
	private RecipePerformanceVO performance;

	private String prompt;

	@Schema(description = "人工评分（若有人评过）")
	private Integer manualScore;

	private String manualReason;
}
