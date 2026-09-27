package com.kingdomstudio.modules.motion.template.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 官方动效模板（对应表 motion_template）。
 *
 * <p>与 {@code MotionResource}（采集/手建的动效资源）分开两张表：
 * 模板是「策展过的官方集合」，带推荐指数的四项子分、可调参数与可运行的预览结构与脚本；
 * 资源是「外部线索」，只有元数据与代码产物。混在一张表里会让两者的字段互相将就。
 */
@Data
@TableName("motion_template")
public class MotionTemplate {

	@TableId(type = IdType.AUTO)
	private Long id;

	/** 稳定标识：组合方案与前端用它引用，不随自增 id 变化 */
	private String templateKey;

	private String name;

	private String nameEn;

	private String description;

	/** 分组：基础交互 / 产品页面 / 高级效果 */
	private String category;

	/** 主要场景：Landing Page / Dashboard / Portfolio / Login / AI SaaS / Game UI */
	private String scene;

	private String style;

	private String technology;

	/** 1 入门 / 2 进阶 / 3 高阶 */
	private Integer difficulty;

	/** 适合场景，逗号分隔 */
	private String bestFor;

	/** 触发方式：load 加载时 / hover 悬停 / scroll 滚动 / click 点击 */
	private String triggerType;

	/** 四项子分：推荐指数由它们加权算出，不单独手写 */
	private Integer scoreVisual;
	private Integer scoreCode;
	private Integer scoreReuse;
	private Integer scorePerf;

	/** 推荐指数 0-100 */
	private Integer score;

	/**
	 * 运行档位：LIGHTWEIGHT 轻量 / BALANCED 均衡 / GPU_ENHANCED 依赖 GPU 加速。
	 *
	 * <p>这一列由服务端的规则算出（见 MotionTemplateService#runtimeTier），
	 * 这里存一份只是为了在 SQL 层按档位筛选 —— 判断规则只在代码里维护一处。
	 */
	private String runtimeTier;

	/** 运行建议：这一档的代价在哪、怎么降级 */
	private String runtimeNote;

	/** 可调参数 JSON：[{key,label,unit,min,max,step,default}] */
	private String params;

	private String previewUrl;

	/** 预览用的 DOM 结构（前端渲染预览、代码生成共用同一份） */
	private String previewHtml;

	/** 预览用的脚本，仅需要交互的模板非空 */
	private String previewJs;

	private String cssCode;
	private String vueCode;
	private String reactCode;
	private String threeCode;
	private String prompt;
	private String tags;

	/** OFFICIAL 官方模板 / COMMUNITY 社区精选 / IMPORTED 采集导入 */
	private String source;

	/** 灵感来源地址：社区精选标注用，官方模板留空 */
	private String sourceUrl;

	/** 来源许可（只记录来源项目的许可；实现代码一律是 Kingdom Studio 原创） */
	private String sourceLicense;

	private String status;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
