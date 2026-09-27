package com.kingdomstudio.modules.motion.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组合方案的整体性能成本。
 *
 * <p>一个方案的价格不是「最贵那一步」，但也不能平均 —— 用户真正要判断的是
 * 「跑到最重的那一步时会不会卡」。所以最重档位单独给出（worstTier），
 * 同时把三档各占几步摊开（counts），让人看到代价集中在哪一步。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "组合方案的整体性能成本")
public class RecipePerformanceVO {

	@Schema(description = "最重的运行档位：LIGHTWEIGHT / BALANCED / GPU_ENHANCED")
	private String worstTier;
	private String worstTierLabel;

	@Schema(description = "整体性能等级：A 轻量 / B 均衡 / C 依赖 GPU 加速")
	private String grade;

	@Schema(description = "各档位各占几步")
	private Integer lightweight;
	private Integer balanced;
	private Integer gpuEnhanced;

	@Schema(description = "一句话说明这套组合的代价在哪、怎么降级")
	private String note;
}
