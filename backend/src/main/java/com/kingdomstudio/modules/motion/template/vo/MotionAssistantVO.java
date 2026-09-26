package com.kingdomstudio.modules.motion.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 搜索栏的回答。
 *
 * <p>这一版是「可解释的检索」而不是模型生成：把输入里的场景、风格、技术与动效关键词识别出来，
 * 按命中权重与推荐指数排序，并把命中理由逐条列出来。
 * 好处是每次推荐都能追溯到具体的词；后续换成模型时这份返回结构不用变。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动效检索结果")
public class MotionAssistantVO {

	@Schema(description = "原始输入")
	private String query;

	@Schema(description = "识别出的意图")
	private Intent intent;

	@Schema(description = "推荐的模板（含命中理由）")
	private List<Match> matches;

	@Schema(description = "推荐的组合方案")
	private List<Match> recipes;

	@Schema(description = "一句话建议")
	private String advice;

	@Schema(description = "结果来源：MODEL 由模型分析 / RULE 由内置检索兜底")
	private String source;

	@Schema(description = "模型名（source=MODEL 时有值）")
	private String modelName;

	@Schema(description = "模型给出的组合方案：用已有模板拼一套，不生成代码")
	private RecipeSuggestion recipeSuggestion;

	@Schema(description = "兜底原因（source=RULE 且因为模型失败时有值）")
	private String fallbackReason;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "由模型给出的组合方案（成员全部来自现有模板）")
	public static class RecipeSuggestion {

		@Schema(description = "方案名")
		private String name;

		@Schema(description = "为什么这么组")
		private String description;

		@Schema(description = "适用场景，逗号分隔")
		private String bestFor;

		@Schema(description = "按应用顺序排列的成员")
		private List<Step> steps;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "组合方案的一个步骤")
	public static class Step {

		@Schema(description = "模板 key，必须来自现有模板")
		private String templateKey;

		@Schema(description = "模板名（服务端按 key 回填）")
		private String templateName;

		@Schema(description = "这一步的作用，例如「铺背景氛围」")
		private String role;

		@Schema(description = "参数建议：CSS 变量名 → 建议值")
		private Map<String, Object> params;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "识别到的意图")
	public static class Intent {

		private String scene;

		@Schema(description = "场景中文名")
		private String sceneLabel;

		private String style;

		@Schema(description = "风格中文名")
		private String styleLabel;

		private String technology;

		@Schema(description = "识别到的动效关键词")
		private List<String> keywords;

		@Schema(description = "命中了什么、没命中什么")
		private String note;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "一条推荐")
	public static class Match {

		private String key;
		private String name;

		@Schema(description = "TEMPLATE / RECIPE")
		private String kind;

		private String scene;
		private String style;
		private String technology;
		private Integer score;
		private Double stars;
		private String grade;

		@Schema(description = "命中理由，逐条列出命中的词")
		private List<String> reasons;
	}
}
