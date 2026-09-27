package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 演奏方案（Performance Preset）。
 *
 * <p>一个方案 = 乐器档案 + 超范围策略 + 速度倍率 + 最小间隔。
 * 界面上把它当成一张卡：名称、用哪套键、快多少、适合什么场景。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "演奏方案")
public class PerformancePresetVO {

	private Long id;
	private Long taskId;
	private String name;

	private Long profileId;
	private String profileName;

	@Schema(description = "键数（来自乐器档案）")
	private Integer keyCount;

	@Schema(description = "超范围策略：SKIP / NEAREST / SHIFT_OCTAVE；为空表示用档案默认")
	private String strategy;

	@Schema(description = "速度倍率：1.00 原速")
	private Double speedScale;

	@Schema(description = "速度说明：原速 / 快 35% / 慢 15%")
	private String speedText;

	@Schema(description = "同键重按的最小间隔（毫秒）")
	private Integer minGapMs;

	@Schema(description = "是否系统预置")
	private Boolean builtin;

	private String note;

	private String createTime;
}
