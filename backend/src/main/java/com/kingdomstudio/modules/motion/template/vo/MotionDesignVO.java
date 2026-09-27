package com.kingdomstudio.modules.motion.template.vo;

import com.kingdomstudio.modules.motion.template.recommend.MotionIntent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 完整动效设计方案（AI Motion Designer 的输出）。
 *
 * <p>这个 VO 要能回答一个设计师会被追问的全部问题：<b>你理解成了什么需求</b>（intent）、
 * <b>选了哪套组合</b>（recipe）、<b>每一步怎么做、参数是多少、为什么这么调</b>（animations）、
 * <b>整体代价多大</b>（performance）、<b>这套方案是怎么来的</b>（source / notes）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动效设计方案")
public class MotionDesignVO {

	@Schema(description = "原始需求")
	private String query;

	@Schema(description = "理解到的需求：场景 / 风格 / 情绪 / 性能 / 触发")
	private MotionIntent intent;

	@Schema(description = "选中的组合方案（含步骤与整体性能成本）")
	private MotionRecipeVO recipe;

	@Schema(description = "设计方案里的动画清单：顺序 / 动画 / 作用 / 参数 / 代价")
	private List<PlanStep> animations;

	@Schema(description = "整体性能成本")
	private RecipePerformanceVO performance;

	@Schema(description = "设计说明：为什么这么搭、做了哪些调整，逐条可核对")
	private List<String> notes;

	@Schema(description = "一句话设计说明（模型给的就用它，规则路径则自动生成）")
	private String explanation;

	@Schema(description = "方案来源：MODEL 由模型选方案与调参 / RULE 由内置规则设计")
	private String source;

	@Schema(description = "模型名（source=MODEL 时有值）")
	private String modelName;

	@Schema(description = "回退原因（source=RULE 且因为模型不可用时说明原因）")
	private String fallbackReason;

	/** 方案里的一步：动画 + 作用 + 落地参数 + 代价 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "设计方案里的一步")
	public static class PlanStep {

		private Integer order;
		private String templateKey;
		private String name;

		@Schema(description = "负责哪一层：背景 / 内容 / 滚动 / 交互 / 收尾")
		private String stage;

		@Schema(description = "这一步的作用")
		private String role;

		@Schema(description = "落地参数：已应用的建议值（含被预算调整过的）")
		private List<Param> params;

		private String technology;
		private String triggerLabel;
		private String runtimeTier;
		private String runtimeTierLabel;
		private String performanceGrade;
	}

	/** 一个参数的建议值：默认值、建议值，以及是否被调整、为什么 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "参数建议")
	public static class Param {

		private String key;
		private String label;
		private String unit;

		@Schema(description = "模板的默认值")
		private Double defaultValue;

		@Schema(description = "本方案建议使用的值")
		private Double value;

		@Schema(description = "是否与默认值不同")
		private Boolean adjusted;

		@Schema(description = "调整原因（未调整时为空）")
		private String reason;
	}
}
