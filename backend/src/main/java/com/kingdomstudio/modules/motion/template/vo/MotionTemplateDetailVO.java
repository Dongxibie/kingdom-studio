package com.kingdomstudio.modules.motion.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 模板详情：在列表项基础上补预览结构、五类产物与可调参数 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动效模板详情")
public class MotionTemplateDetailVO {

	private Long id;
	private String templateKey;
	private String name;
	private String nameEn;
	private String description;
	private String category;
	private String scene;
	private String sceneLabel;
	private String style;
	private String styleLabel;
	private String technology;
	private Integer difficulty;
	private String difficultyLabel;
	private List<String> bestFor;
	private Integer score;
	private Double stars;
	private String grade;
	private Integer scoreVisual;
	private Integer scoreCode;
	private Integer scoreReuse;
	private Integer scorePerf;
	private List<String> tags;

	/** 预览结构与脚本：前端把它们塞进沙箱 iframe */
	private String previewHtml;
	private String previewJs;

	/** 可调参数定义，右侧「参数」页签据此生成控件 */
	private List<TemplateParamVO> params;

	private String cssCode;
	private String vueCode;
	private String reactCode;
	private String threeCode;
	private String prompt;

	/** 这条模板被哪些组合方案用到 */
	private List<String> usedByRecipes;

	@Schema(description = "人工评分（若有人评过）")
	private Integer manualScore;

	private String manualReason;
}
