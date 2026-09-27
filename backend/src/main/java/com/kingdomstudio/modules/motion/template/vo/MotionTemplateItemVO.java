package com.kingdomstudio.modules.motion.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 模板列表项：卡片上要展示的全部信息（推荐指数、四维子分、适合场景、难度） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动效模板列表项")
public class MotionTemplateItemVO {

	private Long id;
	private String templateKey;
	private String name;
	private String nameEn;
	private String description;
	private String category;
	private String scene;

	@Schema(description = "场景中文名，如「网站首页」")
	private String sceneLabel;

	private String style;

	@Schema(description = "风格中文名，如「高级」")
	private String styleLabel;

	private String technology;
	private Integer difficulty;

	@Schema(description = "难度文案：入门 / 进阶 / 高阶")
	private String difficultyLabel;

	private List<String> bestFor;

	@Schema(description = "触发方式：load / hover / scroll / click")
	private String triggerType;

	@Schema(description = "触发方式中文名，如「滚动」")
	private String triggerLabel;

	@Schema(description = "运行档位：LIGHTWEIGHT 轻量 / BALANCED 均衡 / GPU_ENHANCED 依赖 GPU 加速")
	private String runtimeTier;

	@Schema(description = "档位中文名，如「依赖 GPU 加速」")
	private String runtimeTierLabel;

	@Schema(description = "运行建议")
	private String runtimeNote;

	@Schema(description = "推荐指数 0-100")
	private Integer score;

	@Schema(description = "星级 0-5（推荐指数 / 20，取到半星）")
	private Double stars;

	@Schema(description = "推荐指数等级：S / A / B / C")
	private String grade;

	/** 四项子分：解释推荐指数是怎么来的 */
	private Integer scoreVisual;
	private Integer scoreCode;
	private Integer scoreReuse;
	private Integer scorePerf;

	private List<String> tags;

	@Schema(description = "来源：OFFICIAL 官方 / COMMUNITY 社区精选")
	private String source;

	@Schema(description = "来源中文名，如「社区精选」")
	private String sourceLabel;

	@Schema(description = "灵感来源地址（社区精选才有）")
	private String sourceUrl;

	@Schema(description = "来源项目许可（仅标注，实现代码为本项目原创）")
	private String sourceLicense;

	@Schema(description = "是否社区精选（前端据此打标）")
	private Boolean community;
}
