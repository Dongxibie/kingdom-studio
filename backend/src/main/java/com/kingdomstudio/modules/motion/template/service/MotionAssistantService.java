package com.kingdomstudio.modules.motion.template.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kingdomstudio.modules.motion.template.dto.MotionSearchDTO;
import com.kingdomstudio.modules.motion.template.entity.MotionRecipe;
import com.kingdomstudio.modules.motion.template.entity.MotionTemplate;
import com.kingdomstudio.modules.motion.template.mapper.MotionRecipeMapper;
import com.kingdomstudio.modules.motion.template.mapper.MotionTemplateMapper;
import com.kingdomstudio.modules.motion.template.vo.MotionAssistantVO;
import com.kingdomstudio.modules.motion.template.vo.MotionTemplateItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 顶部搜索栏的「可解释检索」。
 *
 * <p>设计取舍：这一版**不调用模型**。把输入拆成四类线索——场景、风格、技术、动效关键词，
 * 与模板的结构化字段逐一匹配，按命中权重加推荐指数排序，并把命中理由逐条列出来。
 *
 * <p>这样做的好处有三个，都是模型给不了的：<br>
 * 1) 每次推荐都能追溯到具体的词（用户能看到「为什么推荐它」）；<br>
 * 2) 同样的输入永远得到同样的结果，便于做回归与测试；<br>
 * 3) 返回结构（意图 + 命中理由 + 建议）不变的情况下，后续换成模型只改这一层。
 */
@Service
@RequiredArgsConstructor
public class MotionAssistantService {

	/** 输入里出现这些词就认为用户说的是这个场景 */
	private static final Map<String, List<String>> SCENE_HINTS = Map.of(
			"Landing Page", List.of("官网", "首页", "landing", "hero", "首屏", "营销页", "落地页"),
			"Dashboard", List.of("后台", "看板", "dashboard", "控制台", "数据面板", "管理台"),
			"Portfolio", List.of("作品集", "个人主页", "portfolio", "简历", "自我介绍"),
			"Login", List.of("登录", "注册", "login", "sign in", "账号"),
			"AI SaaS", List.of("ai", "saas", "智能", "模型", "产品页", "订阅"),
			"Game UI", List.of("游戏", "game", "关卡", "hud", "赛博游戏"));

	private static final Map<String, List<String>> STYLE_HINTS = Map.of(
			"Minimal", List.of("极简", "简洁", "minimal", "克制", "干净"),
			"Luxury", List.of("高级", "奢华", "luxury", "质感", "苹果", "apple", "premium", "贵"),
			"Cyber", List.of("赛博", "科技", "cyber", "未来", "发光", "neon", "霓虹"),
			"Glass", List.of("玻璃", "毛玻璃", "glass", "磨砂", "透明", "frosted"),
			"Organic", List.of("有机", "自然", "柔和", "organic", "流体", "液体"));

	private static final Map<String, List<String>> TECH_HINTS = Map.of(
			"CSS", List.of("css", "纯样式", "不写 js", "简单"),
			"Canvas", List.of("canvas", "粒子", "星网", "波纹", "画布"),
			"Three.js", List.of("three", "3d", "三维", "webgl", "模型"),
			"GSAP", List.of("gsap", "时间轴"),
			"Framer Motion", List.of("framer", "motion"));

	/** 动效关键词 → 命中的模板 key（值里给权重，专用词权重高） */
	private static final Map<String, List<String>> MOTION_HINTS = new LinkedHashMap<>();

	static {
		MOTION_HINTS.put("hero-entrance", List.of("hero", "首屏", "主视觉", "开场"));
		MOTION_HINTS.put("text-reveal", List.of("文字", "标题", "文案", "揭示", "打字", "reveal"));
		MOTION_HINTS.put("aurora-background", List.of("极光", "aurora", "背景", "氛围", "渐变背景"));
		MOTION_HINTS.put("particle-network", List.of("粒子", "星网", "particle", "连线"));
		MOTION_HINTS.put("magnetic-button", List.of("按钮", "button", "磁吸", "cta"));
		MOTION_HINTS.put("glow-border", List.of("描边", "流光", "边框", "发光边"));
		MOTION_HINTS.put("glass-card-hover", List.of("玻璃卡", "玻璃", "卡片悬停", "glass"));
		MOTION_HINTS.put("tilt-card-3d", List.of("3d", "倾斜", "tilt", "立体卡"));
		MOTION_HINTS.put("card-stagger", List.of("错落", "依次", "卡片列表", "stagger"));
		MOTION_HINTS.put("scroll-reveal", List.of("滚动", "scroll", "逐级点亮", "进入视口", "滚动动画"));
		MOTION_HINTS.put("image-parallax", List.of("视差", "parallax", "分层"));
		MOTION_HINTS.put("dashboard-counter", List.of("数字", "计数", "counter", "统计"));
		MOTION_HINTS.put("notification-popup", List.of("通知", "弹窗提示", "toast"));
		MOTION_HINTS.put("modal-morph", List.of("弹窗", "modal", "对话框", "形变"));
		MOTION_HINTS.put("login-animation", List.of("登录", "表单", "输入框"));
		MOTION_HINTS.put("mesh-gradient", List.of("网格渐变", "mesh", "多彩背景"));
		MOTION_HINTS.put("noise-texture", List.of("噪点", "颗粒", "noise", "质感"));
		MOTION_HINTS.put("floating-orb", List.of("光球", "球体", "orb", "漂浮"));
		MOTION_HINTS.put("galaxy-background", List.of("星系", "星空", "galaxy", "星际"));
		MOTION_HINTS.put("shader-background", List.of("着色器", "shader", "波纹"));
		MOTION_HINTS.put("three-scene", List.of("三维场景", "three", "模型", "3d 场景"));
		MOTION_HINTS.put("smooth-fade", List.of("淡入", "fade", "渐显"));
		MOTION_HINTS.put("scale-reveal", List.of("缩放", "scale", "放大出现"));
		MOTION_HINTS.put("slide-up", List.of("滑入", "上移", "slide"));
		MOTION_HINTS.put("blur-reveal", List.of("模糊", "blur", "对焦"));
		MOTION_HINTS.put("floating-card", List.of("悬浮", "飘", "floating"));
		MOTION_HINTS.put("cursor-follow", List.of("跟随鼠标", "光斑", "cursor", "光标"));
		MOTION_HINTS.put("page-transition", List.of("转场", "切换页面", "transition"));
		MOTION_HINTS.put("pricing-card-hover", List.of("价格", "套餐", "定价", "pricing"));
		MOTION_HINTS.put("liquid-gradient", List.of("流体", "流动渐变", "liquid"));
	}

	private final MotionTemplateMapper templateMapper;
	private final MotionRecipeMapper recipeMapper;
	private final MotionTemplateService templateService;
	private final MotionRecipeService recipeService;

	public MotionAssistantVO search(MotionSearchDTO request) {
		String query = request.getQuery().trim();
		String lower = query.toLowerCase(Locale.ROOT);
		int limit = request.getLimit() == null ? 6 : Math.min(Math.max(1, request.getLimit()), 20);

		String scene = matchHint(lower, SCENE_HINTS);
		String style = matchHint(lower, STYLE_HINTS);
		String technology = matchHint(lower, TECH_HINTS);

		List<String> keywords = new ArrayList<>();
		List<String> matchedKeys = new ArrayList<>();
		MOTION_HINTS.forEach((key, words) -> {
			for (String word : words) {
				if (lower.contains(word.toLowerCase(Locale.ROOT))) {
					matchedKeys.add(key);
					keywords.add(word);
					return;
				}
			}
		});

		List<MotionTemplate> all = templateMapper.selectList(new LambdaQueryWrapper<>());
		List<Scored> scored = new ArrayList<>();
		for (MotionTemplate template : all) {
			int weight = 0;
			List<String> reasons = new ArrayList<>();
			if (scene != null && scene.equals(template.getScene())) {
				weight += 40;
				reasons.add("场景匹配「" + templateService.sceneLabel(scene) + "」");
			}
			if (style != null && style.equals(template.getStyle())) {
				weight += 25;
				reasons.add("风格匹配「" + templateService.styleLabel(style) + "」");
			}
			if (technology != null && technology.equals(template.getTechnology())) {
				weight += 15;
				reasons.add("技术匹配「" + technology + "」");
			}
			if (matchedKeys.contains(template.getTemplateKey())) {
				weight += 50;
				reasons.add("动效关键词命中");
			}
			if (lower.contains(template.getName().toLowerCase(Locale.ROOT))) {
				weight += 30;
				reasons.add("名称直接命中");
			}
			for (String tag : templateService.split(template.getTags())) {
				if (!tag.isBlank() && lower.contains(tag.toLowerCase(Locale.ROOT))) {
					weight += 8;
					reasons.add("标签命中「" + tag + "」");
				}
			}
			if (weight == 0) {
				// 一个线索都没命中就不推荐：否则「搜什么都推同一批高分模板」，推荐会失去意义
				continue;
			}
			int score = templateService.recommendScore(template);
			scored.add(new Scored(weight + score, MotionAssistantVO.Match.builder()
					.key(template.getTemplateKey())
					.name(template.getName())
					.kind("TEMPLATE")
					.scene(template.getScene())
					.style(template.getStyle())
					.technology(template.getTechnology())
					.score(score)
					.stars(templateService.stars(score))
					.grade(templateService.grade(score))
					.reasons(reasons)
					.build()));
		}
		// 命中权重为主、推荐指数为辅，两者都高才排前面
		scored.sort(Comparator.comparingInt(Scored::weight).reversed());
		List<MotionAssistantVO.Match> matches = scored.stream().map(Scored::match).toList();
		List<MotionAssistantVO.Match> top = matches.size() > limit ? new ArrayList<>(matches.subList(0, limit)) : matches;

		List<MotionAssistantVO.Match> recipeMatches = new ArrayList<>();
		if (scene != null || !keywords.isEmpty()) {
			for (MotionRecipe recipe : recipeMapper.selectList(new LambdaQueryWrapper<MotionRecipe>()
					.orderByDesc(MotionRecipe::getScore))) {
				List<String> reasons = new ArrayList<>();
				int weight = 0;
				if (scene != null && scene.equals(recipe.getScene())) {
					weight += 40;
					reasons.add("场景匹配「" + templateService.sceneLabel(scene) + "」");
				}
				List<String> memberKeys = templateService.parseKeys(recipe.getTemplateKeys());
				List<String> hitMembers = memberKeys.stream().filter(matchedKeys::contains).toList();
				if (!hitMembers.isEmpty()) {
					weight += 20 * hitMembers.size();
					reasons.add("包含命中的动效：" + String.join("、", hitMembers));
				}
				if (weight == 0) {
					continue;
				}
				var vo = recipeService.toVO(recipe);
				recipeMatches.add(MotionAssistantVO.Match.builder()
						.key(recipe.getRecipeKey())
						.name(recipe.getName())
						.kind("RECIPE")
						.scene(recipe.getScene())
						.style(recipe.getStyle())
						.technology(vo.getMembers().isEmpty() ? "" : vo.getMembers().get(0).getTechnology())
						.score(vo.getScore())
						.stars(vo.getStars())
						.grade(vo.getGrade())
						.reasons(reasons)
						.build());
			}
		}

		String note;
		if (scene == null && style == null && technology == null && keywords.isEmpty()) {
			note = "没有识别到明确的场景、风格或动效关键词，下面是推荐指数最高的几条。";
		} else {
			List<String> parts = new ArrayList<>();
			if (scene != null) {
				parts.add("场景=" + templateService.sceneLabel(scene));
			}
			if (style != null) {
				parts.add("风格=" + templateService.styleLabel(style));
			}
			if (technology != null) {
				parts.add("技术=" + technology);
			}
			if (!keywords.isEmpty()) {
				parts.add("关键词=" + String.join("、", keywords));
			}
			note = "识别到：" + String.join("，", parts);
		}

		String advice = recipeMatches.isEmpty()
				? "想让效果更完整，可以把命中的模板组合起来用：背景类 + 入场类 + 交互类，三者错开时间叠加。"
				: "推荐先看组合方案「" + recipeMatches.get(0).getName() + "」：它把几个命中模板按顺序叠好了。";

		return MotionAssistantVO.builder()
				.query(query)
				.intent(MotionAssistantVO.Intent.builder()
						.scene(scene)
						.sceneLabel(scene == null ? "" : templateService.sceneLabel(scene))
						.style(style)
						.styleLabel(style == null ? "" : templateService.styleLabel(style))
						.technology(technology)
						.keywords(keywords)
						.note(note)
						.build())
				.matches(top)
				.recipes(recipeMatches.size() > 3 ? recipeMatches.subList(0, 3) : recipeMatches)
				.advice(advice)
				.build();
	}

	/** 命中权重 + 推荐指数：临时结构，只用来排序 */
	private record Scored(int weight, MotionAssistantVO.Match match) {
	}

	private String matchHint(String lowerQuery, Map<String, List<String>> hints) {
		for (Map.Entry<String, List<String>> entry : hints.entrySet()) {
			for (String word : entry.getValue()) {
				if (lowerQuery.contains(word.toLowerCase(Locale.ROOT))) {
					return entry.getKey();
				}
			}
		}
		return null;
	}
}
