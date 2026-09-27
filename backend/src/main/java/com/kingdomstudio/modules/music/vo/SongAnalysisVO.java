package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 曲目分析卡（Song Analysis）。
 *
 * <p>解析完一首曲子，使用者想立刻知道的是五件事：难不难、多快、音域多宽、
 * 要花多久弹完、该用哪套键位。这一张卡就是这五个答案，外加「凭什么这么判」的依据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "曲目分析")
public class SongAnalysisVO {

	private Long taskId;
	private String taskName;

	@Schema(description = "难度星级 1-5")
	private Integer difficultyStars;

	@Schema(description = "难度文字：入门 / 简单 / 进阶 / 较难 / 挑战")
	private String difficultyLabel;
	private String difficultyHint;

	@Schema(description = "分层：EASY 简单 / NORMAL 普通 / ADVANCED 高级")
	private String difficultyTier;
	private String difficultyTierLabel;

	@Schema(description = "适合谁：新手 / 有基础 / 熟练")
	private String audience;

	@Schema(description = "计分依据：每一条都说明命中与否，避免只给一个星级")
	private List<Reason> reasons;

	@Schema(description = "速度 BPM")
	private Integer tempoBpm;

	@Schema(description = "拍号")
	private String timeSignature;

	@Schema(description = "音域，例如 C4–A4")
	private String pitchRange;

	@Schema(description = "音域跨度（半音数）")
	private Integer pitchSpan;

	private Integer noteCount;

	@Schema(description = "预计演奏时长（毫秒）：按推荐方案的键位算出来的实际按键时长")
	private Integer estimatedDuration;

	@Schema(description = "预计演奏时长的文字，例如 10 秒")
	private String estimatedText;

	@Schema(description = "和弦音占比（0-1）：同一个起点上按多个键的音")
	private Double chordRatio;

	@Schema(description = "推荐键位：推荐方案名与理由")
	private String recommendedPresetName;
	private String recommendedPresetNote;

	@Schema(description = "推荐用的乐器档案 id 与名称")
	private Long recommendedProfileId;
	private String recommendedProfileName;

	@Schema(description = "推荐方案下的落键情况")
	private Integer mappedCount;
	private Integer unmappedCount;

	@Schema(description = "可选的其它方案（键数更少 / 更快）")
	private List<Alternative> alternatives;

	/** 一条计分依据 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "难度计分依据")
	public static class Reason {

		private String label;

		@Schema(description = "是否命中（命中才计分）")
		private Boolean hit;
	}

	/** 备选方案 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "备选键位方案")
	public static class Alternative {

		private Long profileId;
		private String profileName;
		private Integer keyCount;

		@Schema(description = "这个方案能不能把所有音都落下来")
		private Boolean coversAll;

		private Integer unmappedCount;
	}
}
