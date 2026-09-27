package com.kingdomstudio.modules.motion.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 候选池列表项 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动效候选")
public class MotionCandidateVO {

	private Long id;
	private String candidateKey;
	private String name;
	private String fullName;
	private String sourceUrl;
	private String description;

	@Schema(description = "建议分类（七类之一）")
	private String category;

	private String technology;
	private String triggerType;

	@Schema(description = "触发方式中文名")
	private String triggerLabel;

	private Integer difficulty;

	@Schema(description = "难度中文名")
	private String difficultyLabel;

	private String performanceLevel;

	@Schema(description = "运行档位中文名")
	private String performanceLabel;

	private Integer visualScore;

	@Schema(description = "视觉潜力星级 0-5")
	private Double visualStars;

	private String license;
	private Integer stars;
	private String language;
	private java.util.List<String> topics;

	@Schema(description = "命中的分类关键词：解释为什么分到这一类")
	private java.util.List<String> matchedHints;

	private String prompt;
	private String status;

	@Schema(description = "状态中文名")
	private String statusLabel;

	private String reviewNote;

	@Schema(description = "已转成的内置 Pattern")
	private String patternKey;

	@Schema(description = "入库后的模板 key")
	private String promotedTemplateKey;
}
