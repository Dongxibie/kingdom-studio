package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 任务列表项：列表页只展示这些字段，音名与按键明细走详情接口。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "音乐解析任务列表项")
public class MusicTaskListItemVO {

	private Long id;

	private String name;

	@Schema(description = "来源：MIDI / JIANPU")
	private String sourceType;

	private String sourceRef;

	private Integer noteCount;

	private Integer tempoBpm;

	private String timeSignature;

	@Schema(description = "总时长（毫秒）")
	private Integer durationMs;

	@Schema(description = "音域，如 C4–C6")
	private String pitchRange;

	private String status;

	@Schema(description = "收藏标记：1 收藏 / 0 普通")
	private Integer favorite;

	@Schema(description = "难度星级 1-5（与详情卡同一套规则）")
	private Integer difficultyStars;

	@Schema(description = "难度文字：入门 / 简单 / 进阶 / 较难 / 挑战")
	private String difficultyLabel;

	@Schema(description = "难度分层：EASY 简单 / NORMAL 普通 / ADVANCED 高级")
	private String difficultyTier;

	private LocalDateTime createTime;
}
