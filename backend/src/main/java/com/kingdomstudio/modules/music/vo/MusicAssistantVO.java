package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 音乐助手的回答。
 *
 * <p>{@code source} 说明这次是模型分析还是规则判断；{@code adjustments} 就是「AI 解释」——
 * 每条改动都带原因，界面直接展示，不需要再让模型解释一遍。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "音乐助手回答")
public class MusicAssistantVO {

	private Long taskId;
	private String taskName;

	@Schema(description = "来源：MODEL 模型分析 / RULE 规则判断")
	private String source;

	@Schema(description = "模型名（source=MODEL 时有值）")
	private String modelName;

	@Schema(description = "回退原因（source=RULE 且因为模型失败时有值）")
	private String fallbackReason;

	private Intent intent;

	@Schema(description = "一句话说明这次方案改了什么")
	private String explanation;

	@Schema(description = "逐条改动与原因（AI 解释）")
	private List<Change> adjustments;

	@Schema(description = "应用前后的对比，供界面展示预览")
	private Preview preview;

	/** 理解到的意图 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "理解到的意图")
	public static class Intent {

		@Schema(description = "难度档：BEGINNER / NORMAL / SHOWCASE")
		private String difficulty;

		@Schema(description = "难度档中文名")
		private String difficultyLabel;

		@Schema(description = "目标乐器（模型或关键词识别出来才有）")
		private String instrument;

		@Schema(description = "建议清单")
		private List<String> suggestions;
	}

	/** 一条改动 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "一条改动")
	public static class Change {

		private String type;
		private String title;
		private String detail;
		private String before;
		private String after;
	}

	/** 前后对比 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "方案前后对比")
	public static class Preview {

		private Integer noteCountBefore;
		private Integer noteCountAfter;
		private Integer tempoBefore;
		private Integer tempoAfter;
		private Integer durationBefore;
		private Integer durationAfter;
	}
}
