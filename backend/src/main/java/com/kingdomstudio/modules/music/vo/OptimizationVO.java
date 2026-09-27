package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 演奏优化报告（Performance Optimizer）。
 *
 * <p>它不替用户决定，只做两件事：**指出问题**（在哪、为什么）和**给出可直接应用的参数**
 * （{@code fix} 里就是这套方案的旋钮）。前端拿到 applyHint 就能一键应用，
 * 不需要再猜「那你说怎么办」。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "演奏优化报告")
public class OptimizationVO {

	private Long taskId;
	private String taskName;

	@Schema(description = "检查了多少组按键")
	private Integer strokeCount;

	@Schema(description = "检查了多少条按键动作")
	private Integer eventCount;

	@Schema(description = "总体结论：一行话说清这套方案能不能直接用")
	private String summary;

	@Schema(description = "发现的问题，按严重程度从高到低排")
	private List<Finding> findings;

	/** 一条发现 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "一条优化建议")
	public static class Finding {

		@Schema(description = "问题代码：REPEAT_CONFLICT / OUT_OF_RANGE / CHORD / DENSE / LONG_HOLD")
		private String code;

		@Schema(description = "严重程度：WARN 建议处理 / ADVICE 可以更好 / INFO 仅说明")
		private String severity;

		private String title;

		@Schema(description = "具体出现在哪：时间点、按键或音名")
		private String detail;

		@Schema(description = "建议怎么做")
		private String suggestion;

		@Schema(description = "受影响的按键组数或音符数")
		private Integer affected;

		@Schema(description = "一键应用所需的参数：{ 旋钮名 : 建议值 }")
		private Fix fix;
	}

	/** 可直接应用的参数 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "可应用的参数")
	public static class Fix {

		@Schema(description = "同键重按最小间隔（毫秒）")
		private Integer minGapMs;

		@Schema(description = "速度倍率")
		private Double speedScale;

		@Schema(description = "超范围策略：SKIP / NEAREST / SHIFT_OCTAVE")
		private String strategy;

		@Schema(description = "用哪个乐器档案（键更多、音域更宽的那个）")
		private Long profileId;

		private String profileName;
	}
}
