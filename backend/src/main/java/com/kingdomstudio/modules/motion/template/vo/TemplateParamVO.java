package com.kingdomstudio.modules.motion.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一个可调参数。
 *
 * <p>参数以 CSS 变量形式暴露（key 就是变量名，如 --m-duration），
 * 前端把它渲染成滑块并实时注入预览；导出的代码里也是同一批变量，调参与成品一致。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板可调参数")
public class TemplateParamVO {

	@Schema(description = "CSS 变量名", example = "--m-duration")
	private String key;

	@Schema(description = "中文标签", example = "时长")
	private String label;

	@Schema(description = "单位：s / px / deg / % 或空")
	private String unit;

	private Double min;

	private Double max;

	private Double step;

	@Schema(description = "默认值")
	private Double defaultValue;
}
