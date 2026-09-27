package com.kingdomstudio.modules.motion.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** 代码导出的输入 */
@Data
@Schema(description = "动效方案代码导出输入")
public class MotionExportDTO {

	@Size(max = 64, message = "方案 key 过长")
	@Schema(description = "组合方案 key（与 templateKeys 二选一）", example = "cyber-launch")
	private String recipeKey;

	@Size(max = 120, message = "方案名过长")
	@Schema(description = "方案名，用于页面标题与注释", example = "科技公司首页")
	private String planName;

	@Schema(description = "导出格式：VUE（默认）/ REACT / HTML", example = "VUE")
	private String format;

	@Schema(description = "临时组合的模板清单（推荐结果直接导出时用，与 recipeKey 二选一）")
	private List<String> templateKeys;

	@Schema(description = "参数覆盖：模板 key → { CSS 变量名 → 数值 }",
			example = "{\"galaxy-background\":{\"--m-duration\":18}}")
	private Map<String, Map<String, Double>> params;
}
