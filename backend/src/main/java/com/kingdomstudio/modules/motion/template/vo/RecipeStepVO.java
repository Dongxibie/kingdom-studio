package com.kingdomstudio.modules.motion.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 组合方案里的一步。
 *
 * <p>组合模式的核心就是这一张表：每一步都要能回答四个问题 ——
 * <b>用哪个动画</b>（name）、<b>它负责什么</b>（role / stage）、
 * <b>有什么可调的</b>（params）、<b>代价多大</b>（runtimeTier + performanceGrade）。
 * 界面按这四列渲染，点某一步就把中间舞台切到那一步的模板上。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "组合方案的一步")
public class RecipeStepVO {

	@Schema(description = "第几步，从 1 开始")
	private Integer order;

	private String templateKey;
	private String name;
	private String nameEn;

	@Schema(description = "这一步负责哪一层：背景 / 内容 / 滚动 / 交互 / 收尾")
	private String stage;

	@Schema(description = "这一步的作用：为什么在这里用它")
	private String role;

	private String description;
	private String category;
	private String technology;
	private String triggerType;
	private String triggerLabel;

	@Schema(description = "运行档位：LIGHTWEIGHT / BALANCED / GPU_ENHANCED")
	private String runtimeTier;
	private String runtimeTierLabel;

	@Schema(description = "性能等级：A 轻量 / B 均衡 / C 依赖 GPU 加速")
	private String performanceGrade;

	@Schema(description = "这一步的推荐指数与星级")
	private Integer score;
	private Double stars;

	@Schema(description = "可调参数：key 就是 CSS 变量名，界面上直接生成控件")
	private List<TemplateParamVO> params;

	@Schema(description = "该模板是否带沙箱预览结构（决定这一步能不能单独预览）")
	private Boolean previewReady;
}
