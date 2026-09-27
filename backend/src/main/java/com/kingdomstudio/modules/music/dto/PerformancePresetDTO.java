package com.kingdomstudio.modules.music.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 新建 / 修改演奏方案 */
@Data
@Schema(description = "演奏方案入参")
public class PerformancePresetDTO {

	@NotBlank(message = "请给方案起个名字")
	@Size(max = 64, message = "方案名最长 64 个字符")
	@Schema(description = "方案名，例如 简单版", example = "简单版")
	private String name;

	@NotNull(message = "请选择乐器档案")
	private Long profileId;

	@Schema(description = "超范围策略覆盖：SKIP / NEAREST / SHIFT_OCTAVE；不填用档案默认")
	private String strategy;

	@DecimalMin(value = "0.5", message = "速度倍率最小 0.5")
	@DecimalMax(value = "2.0", message = "速度倍率最大 2.0")
	@Schema(description = "速度倍率：1.0 原速，>1 更快", example = "1.35")
	private Double speedScale;

	@Min(value = 0, message = "最小间隔不能为负")
	@Max(value = 500, message = "最小间隔最大 500 毫秒")
	@Schema(description = "同键重按的最小间隔（毫秒）", example = "60")
	private Integer minGapMs;

	@Size(max = 200, message = "说明最长 200 个字符")
	@Schema(description = "一句话说明这套方案适合什么场景")
	private String note;
}
