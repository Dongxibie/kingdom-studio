package com.kingdomstudio.modules.motion.template.recommend;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 动效意图：把「科技感首页」这类说法拆成五个可计算的轴。
 *
 * <p>为什么要拆：一句话里其实混着好几种信息 —— 做在什么页面上（scene）、想看起来像什么（style）、
 * 要什么情绪（emotion）、性能上能花多少（performance）、以及靠什么触发（interaction）。
 * 拆开之后每一轴都能单独给出「命中了哪个词」，推荐结果因此**可解释**，而不是一个黑盒分数。
 *
 * <p>五个轴都是可空的：说不清就不猜，这一轴就不参与打分。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动效意图：场景 / 风格 / 情绪 / 性能 / 触发")
public class MotionIntent {

	@Schema(description = "场景：Landing Page / Dashboard / Portfolio / Login / AI SaaS / Game UI")
	private String scene;

	@Schema(description = "风格：Minimal / Luxury / Cyber / Glass / Organic")
	private String style;

	@Schema(description = "情绪：premium 高级 / tech 科技 / calm 克制 / playful 有趣 / warm 温暖 / bold 冲击")
	private String emotion;

	@Schema(description = "性能预算：LOW 轻量优先 / MEDIUM 均衡 / HIGH 效果优先")
	private String performance;

	@Schema(description = "触发方式：load / hover / scroll / click")
	private String interaction;

	@Schema(description = "各轴的中文说法，给界面直接用")
	private Map<String, String> labels;

	@Schema(description = "每一轴命中的关键词：解释「为什么这么理解」")
	private Map<String, List<String>> hits;

	@Schema(description = "一句话复述理解到的需求")
	private String summary;
}
