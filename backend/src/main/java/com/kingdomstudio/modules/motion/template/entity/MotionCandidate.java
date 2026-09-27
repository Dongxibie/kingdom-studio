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
 * 动效候选（对应表 motion_candidate）。
 *
 * <p><b>只存元数据，不存源码</b>：候选是从 GitHub 上「发现」的案例线索，
 * 记下它叫什么、在哪、许可是什么、我们觉得它属于哪一类；
 * 真正进入资源库的代码一律是 Kingdom Studio 自己实现的 Motion Pattern（见 {@link MotionTemplate}）。
 * 这条边界决定了这个项目不会变成「换个地方收集别人的代码」。
 */
@Data
@TableName("motion_candidate")
public class MotionCandidate {

	@TableId(type = IdType.AUTO)
	private Long id;

	/** 稳定标识：owner__repo（归一化小写），用来挡住重复发现 */
	private String candidateKey;

	private String name;

	private String fullName;

	private String sourceUrl;

	private String description;

	/** 建议分类（七类之一），由 {@code MotionCandidateAnalyzer} 规则分析得出，可人工改 */
	private String category;

	/** 技术线索：CSS / Canvas / Three.js / WebGL */
	private String technology;

	/** 触发方式：load / hover / scroll / click */
	private String triggerType;

	/** 1 入门 / 2 进阶 / 3 高阶 */
	private Integer difficulty;

	/** 运行档位：LIGHTWEIGHT / BALANCED / GPU_ENHANCED */
	private String performanceLevel;

	/** 视觉潜力分 0-100（按星数与关键词估算，只在筛选阶段用） */
	private Integer visualScore;

	/** 来源项目许可（SPDX），标注用 */
	private String license;

	private Integer stars;

	private String language;

	private String topics;

	/** 命中的分类关键词，留痕：解释「为什么分到这一类」 */
	private String matchedHints;

	/** 转换说明：要做成 Pattern 该怎么描述（选中后由 Pattern 覆盖） */
	private String prompt;

	/** NEW 待看 / ANALYZED 已分析 / SELECTED 已选入 / PROMOTED 已入库 / REJECTED 已淘汰 */
	private String status;

	/** 人工筛选意见，尤其是淘汰理由 */
	private String reviewNote;

	/** 转成哪个内置 Motion Pattern */
	private String patternKey;

	/** 入库后的模板 key */
	private String promotedTemplateKey;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
