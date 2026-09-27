package com.kingdomstudio.modules.motion.template.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingdomstudio.common.PageVO;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.motion.template.dto.MotionRatingDTO;
import com.kingdomstudio.modules.motion.template.dto.TemplateQueryDTO;
import com.kingdomstudio.modules.motion.template.entity.MotionRating;
import com.kingdomstudio.modules.motion.template.entity.MotionRecipe;
import com.kingdomstudio.modules.motion.template.entity.MotionTemplate;
import com.kingdomstudio.modules.motion.template.mapper.MotionRatingMapper;
import com.kingdomstudio.modules.motion.template.mapper.MotionRecipeMapper;
import com.kingdomstudio.modules.motion.template.mapper.MotionTemplateMapper;
import com.kingdomstudio.modules.motion.template.vo.MotionFacetVO;
import com.kingdomstudio.modules.motion.template.vo.MotionTemplateDetailVO;
import com.kingdomstudio.modules.motion.template.vo.MotionTemplateItemVO;
import com.kingdomstudio.modules.motion.template.vo.TemplateParamVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 官方模板：检索、分面、详情、评分。
 *
 * <p>两条规矩值得单独说：
 *
 * <ol>
 *   <li><b>推荐指数只有一个算法</b>：由四项子分加权算出（视觉 30% / 代码 25% / 复用 25% / 性能 20%），
 *       库里存的那一列只是缓存，详情与列表都走 {@link #recommendScore} 重算 ——
 *       否则改了权重之后，老数据会带着旧口径继续显示。</li>
 *   <li><b>参数是数据不是代码</b>：可调参数存在 params JSON 里（就是 CSS 变量名与取值范围），
 *       接口直接吐给前端生成控件；模板作者想加一个可调项，不需要改前后端任何代码。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotionTemplateService {

	/** 四项子分的权重：视觉 30% / 代码 25% / 复用 25% / 性能 20% */
	private static final double W_VISUAL = 0.30;
	private static final double W_CODE = 0.25;
	private static final double W_REUSE = 0.25;
	private static final double W_PERF = 0.20;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/** 场景与风格的中文对照：界面上要显示中文，库里存英文值（组合、筛选都按英文值走） */
	private static final Map<String, String> SCENE_LABELS = Map.of(
			"Landing Page", "网站首页",
			"Dashboard", "数据后台",
			"Portfolio", "个人主页",
			"Login", "登录页面",
			"AI SaaS", "AI 产品页",
			"Game UI", "游戏界面");

	private static final Map<String, String> STYLE_LABELS = Map.of(
			"Minimal", "极简",
			"Luxury", "高级",
			"Cyber", "赛博",
			"Glass", "玻璃",
			"Organic", "有机");

	private static final Map<Integer, String> DIFFICULTY_LABELS = Map.of(
			1, "入门", 2, "进阶", 3, "高阶");

	/** 来源：官方集合与社区精选。界面按它打标，筛选也按它走 */
	private static final Map<String, String> SOURCE_LABELS = Map.of(
			"OFFICIAL", "官方",
			"COMMUNITY", "社区精选",
			"IMPORTED", "采集导入");

	/** 触发方式：同一套值在候选池与模板库共用 */
	private static final Map<String, String> TRIGGER_LABELS = Map.of(
			"load", "加载时",
			"hover", "悬停",
			"scroll", "滚动",
			"click", "点击");

	private final MotionTemplateMapper templateMapper;
	private final MotionRecipeMapper recipeMapper;
	private final MotionRatingMapper ratingMapper;

	/**
	 * 取中文标签。
	 *
	 * <p>不能直接用 {@code Map.of(...).getOrDefault(key, fallback)}：不可变集合不接受 null 键，
	 * 而老数据里 source / trigger_type 可能为空，那样渲染列表时会抛 NPE。这里统一兜住。
	 */
	private static String label(Map<String, String> labels, String key, String fallback) {
		if (key == null) {
			return fallback;
		}
		String value = labels.get(key);
		return value == null ? fallback : value;
	}

	/** 全部模板：给推荐层做全量打分用 */
	public List<MotionTemplate> allTemplates() {
		return templateMapper.selectList(new LambdaQueryWrapper<>());
	}

	/** 触发方式的中文名（推荐理由里要用） */
	public String triggerLabel(String triggerType) {
		return label(TRIGGER_LABELS, triggerType, "加载时");
	}

	/** 推荐指数：四项子分加权，四舍五入到整数 */
	public int recommendScore(MotionTemplate template) {
		double score = nz(template.getScoreVisual()) * W_VISUAL
				+ nz(template.getScoreCode()) * W_CODE
				+ nz(template.getScoreReuse()) * W_REUSE
				+ nz(template.getScorePerf()) * W_PERF;
		return (int) Math.round(score);
	}

	/** 推荐指数的文字等级：S 90+ / A 80+ / B 70+ / C 其余 */
	public String grade(int score) {
		if (score >= 90) {
			return "S";
		}
		if (score >= 80) {
			return "A";
		}
		if (score >= 70) {
			return "B";
		}
		return "C";
	}

	/** 星级：推荐指数 / 20，取到半星，便于前端画 ★★★★☆ */
	public double stars(int score) {
		return Math.round(score / 20.0 * 2) / 2.0;
	}


	/**
	 * 运行档位：告诉用户「这个动效的性能代价有多大」，而不是把高级效果藏起来。
	 *
	 * <p>规则刻意简单、可解释：
	 * <ol>
	 *   <li>Three.js 与 Canvas 一律 GPU_ENHANCED —— 它们要么走 WebGL，要么逐像素/逐帧计算，
	 *       观感最好也最吃硬件，标注清楚比悄悄降级更有用；</li>
	 *   <li>纯 CSS 按性能子分分档：92 分以上是纯合成属性动画（transform / opacity），
	 *       算 LIGHTWEIGHT；80-91 分常用到模糊、离屏合成或大面积动画，算 BALANCED；
	 *       低于 80 分的 CSS 实现（例如多层 blur 叠加）同样归入 GPU_ENHANCED。</li>
	 * </ol>
	 */
	public String runtimeTier(MotionTemplate template) {
		String technology = template.getTechnology() == null ? "" : template.getTechnology();
		if ("Three.js".equals(technology) || "Canvas".equals(technology)) {
			return "GPU_ENHANCED";
		}
		int perf = nz(template.getScorePerf());
		if (perf >= 92) {
			return "LIGHTWEIGHT";
		}
		if (perf >= 80) {
			return "BALANCED";
		}
		return "GPU_ENHANCED";
	}

	/** 档位中文名 */
	public String runtimeTierLabel(String tier) {
		if ("LIGHTWEIGHT".equals(tier)) {
			return "轻量";
		}
		if ("GPU_ENHANCED".equals(tier)) {
			return "依赖 GPU 加速";
		}
		return "均衡";
	}

	/**
	 * 性能等级：由运行档位与性能子分算出。
	 *
	 * <p>只有这一份算法：推荐结果、组合方案的每一步、模板详情都走它，
	 * 免得同一张卡片在不同页面显示出两个等级。
	 */
	public String performanceGrade(String tier, Integer scorePerf) {
		int perf = scorePerf == null ? 80 : scorePerf;
		if ("LIGHTWEIGHT".equals(tier)) {
			return perf >= 92 ? "A" : "B";
		}
		if ("BALANCED".equals(tier)) {
			return "B";
		}
		return "C";
	}

	/** 模板的性能等级 */
	public String performanceGrade(MotionTemplate template) {
		return performanceGrade(runtimeTier(template), template.getScorePerf());
	}

	/** 运行建议：库里存过就用库里的，没存过按档位给一句通用建议 */
	public String runtimeNote(MotionTemplate template) {
		String stored = template.getRuntimeNote();
		if (stored != null && !stored.isBlank()) {
			return stored;
		}
		return switch (runtimeTier(template)) {
			case "LIGHTWEIGHT" -> "纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。";
			case "GPU_ENHANCED" -> "高级视觉效果：依赖 GPU 加速，推荐在桌面设备上查看与使用；"
					+ "移动端启用时可适当减少粒子数量或降低分辨率。";
			default -> "用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。";
		};
	}

	public PageVO<MotionTemplateItemVO> page(TemplateQueryDTO query) {
		long current = query.getPage() == null ? 1 : Math.max(1, query.getPage());
		long size = query.getSize() == null ? 12 : Math.min(Math.max(1, query.getSize()), 100);

		LambdaQueryWrapper<MotionTemplate> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(notBlank(query.getCategory()), MotionTemplate::getCategory, query.getCategory())
				.eq(notBlank(query.getScene()), MotionTemplate::getScene, query.getScene())
				.eq(notBlank(query.getStyle()), MotionTemplate::getStyle, query.getStyle())
				.eq(notBlank(query.getTechnology()), MotionTemplate::getTechnology, query.getTechnology())
				.eq(query.getDifficulty() != null, MotionTemplate::getDifficulty, query.getDifficulty())
				.eq(notBlank(query.getRuntimeTier()), MotionTemplate::getRuntimeTier, query.getRuntimeTier())
				.eq(notBlank(query.getSource()), MotionTemplate::getSource, query.getSource())
				.eq(notBlank(query.getTrigger()), MotionTemplate::getTriggerType, query.getTrigger());
		if (notBlank(query.getKeyword())) {
			String like = query.getKeyword().trim();
			wrapper.and(inner -> inner.like(MotionTemplate::getName, like)
					.or().like(MotionTemplate::getNameEn, like)
					.or().like(MotionTemplate::getDescription, like)
					.or().like(MotionTemplate::getTags, like)
					.or().like(MotionTemplate::getBestFor, like));
		}
		applySort(wrapper, query.getSort());

		Page<MotionTemplate> result = templateMapper.selectPage(new Page<>(current, size), wrapper);
		return PageVO.of(result, this::toItem);
	}

	private void applySort(LambdaQueryWrapper<MotionTemplate> wrapper, String sort) {
		String value = sort == null ? "SCORE" : sort.toUpperCase(Locale.ROOT);
		switch (value) {
			case "NAME" -> wrapper.orderByAsc(MotionTemplate::getNameEn);
			case "DIFFICULTY" -> wrapper.orderByAsc(MotionTemplate::getDifficulty).orderByDesc(MotionTemplate::getScoreVisual);
			default -> wrapper.orderByDesc(MotionTemplate::getScore).orderByAsc(MotionTemplate::getId);
		}
	}


	/** 按运行档位分面：让用户一眼看到「有多少是轻量的、多少要 GPU」 */
	private List<MotionFacetVO.FacetOption> tierFacet(List<MotionTemplate> all) {
		List<MotionFacetVO.FacetOption> options = new ArrayList<>();
		for (String tier : List.of("LIGHTWEIGHT", "BALANCED", "GPU_ENHANCED")) {
			List<MotionTemplate> same = all.stream().filter(template -> tier.equals(runtimeTier(template))).toList();
			options.add(MotionFacetVO.FacetOption.builder()
					.value(tier)
					.label(runtimeTierLabel(tier))
					.count((long) same.size())
					.averageScore((int) Math.round(same.stream().mapToInt(this::recommendScore).average().orElse(0)))
					.build());
		}
		return options;
	}

	public MotionTemplateDetailVO detail(String templateKey) {
		MotionTemplate template = require(templateKey);
		List<String> usedBy = recipeMapper.selectList(new LambdaQueryWrapper<MotionRecipe>()).stream()
				.filter(recipe -> parseKeys(recipe.getTemplateKeys()).contains(templateKey))
				.map(MotionRecipe::getName)
				.toList();
		MotionRating rating = findRating("TEMPLATE", templateKey);
		return MotionTemplateDetailVO.builder()
				.id(template.getId())
				.templateKey(template.getTemplateKey())
				.name(template.getName())
				.nameEn(template.getNameEn())
				.description(template.getDescription())
				.category(template.getCategory())
				.scene(template.getScene())
				.sceneLabel(sceneLabel(template.getScene()))
				.style(template.getStyle())
				.styleLabel(styleLabel(template.getStyle()))
				.technology(template.getTechnology())
				.difficulty(template.getDifficulty())
				.difficultyLabel(DIFFICULTY_LABELS.getOrDefault(nz(template.getDifficulty()), "入门"))
				.bestFor(split(template.getBestFor()))
				.triggerType(template.getTriggerType())
				.triggerLabel(label(TRIGGER_LABELS, template.getTriggerType(), "加载时"))
				.source(template.getSource())
				.sourceLabel(label(SOURCE_LABELS, template.getSource(), "官方"))
				.sourceUrl(template.getSourceUrl())
				.sourceLicense(template.getSourceLicense())
				.community("COMMUNITY".equals(template.getSource()))
				.runtimeTier(runtimeTier(template))
				.runtimeTierLabel(runtimeTierLabel(runtimeTier(template)))
				.runtimeNote(runtimeNote(template))
				.score(recommendScore(template))
				.stars(stars(recommendScore(template)))
				.grade(grade(recommendScore(template)))
				.scoreVisual(nz(template.getScoreVisual()))
				.scoreCode(nz(template.getScoreCode()))
				.scoreReuse(nz(template.getScoreReuse()))
				.scorePerf(nz(template.getScorePerf()))
				.tags(split(template.getTags()))
				.previewHtml(template.getPreviewHtml())
				.previewJs(template.getPreviewJs())
				.params(parseParams(template.getParams()))
				.cssCode(template.getCssCode())
				.vueCode(template.getVueCode())
				.reactCode(template.getReactCode())
				.threeCode(template.getThreeCode())
				.prompt(template.getPrompt())
				.usedByRecipes(usedBy)
				.manualScore(rating == null ? null : rating.getScore())
				.manualReason(rating == null ? null : rating.getReason())
				.build();
	}

	/** 左侧「发现方式」：四组分面各自带数量与平均推荐指数 */
	public MotionFacetVO facets() {
		List<MotionTemplate> all = templateMapper.selectList(new LambdaQueryWrapper<>());
		return MotionFacetVO.builder()
				.scenes(facet(all, MotionTemplate::getScene, SCENE_LABELS))
				.styles(facet(all, MotionTemplate::getStyle, STYLE_LABELS))
				.technologies(facet(all, MotionTemplate::getTechnology, Map.of()))
				.categories(facet(all, MotionTemplate::getCategory, Map.of()))
				.difficulties(difficultyFacet(all))
				.runtimeTiers(tierFacet(all))
				.sources(facet(all, MotionTemplate::getSource, SOURCE_LABELS))
				.triggers(facet(all, MotionTemplate::getTriggerType, TRIGGER_LABELS))
				.total((long) all.size())
				.recipeTotal(recipeMapper.selectCount(new LambdaQueryWrapper<>()))
				.build();
	}

	private List<MotionFacetVO.FacetOption> facet(List<MotionTemplate> all,
			java.util.function.Function<MotionTemplate, String> getter, Map<String, String> labels) {
		Map<String, List<MotionTemplate>> grouped = new LinkedHashMap<>();
		for (MotionTemplate template : all) {
			String value = getter.apply(template);
			if (value == null || value.isBlank()) {
				continue;
			}
			grouped.computeIfAbsent(value, key -> new ArrayList<>()).add(template);
		}
		List<MotionFacetVO.FacetOption> options = new ArrayList<>();
		grouped.forEach((value, list) -> options.add(MotionFacetVO.FacetOption.builder()
				.value(value)
				.label(labels.getOrDefault(value, value))
				.count((long) list.size())
				.averageScore((int) Math.round(list.stream().mapToInt(this::recommendScore).average().orElse(0)))
				.build()));
		options.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
		return options;
	}

	private List<MotionFacetVO.FacetOption> difficultyFacet(List<MotionTemplate> all) {
		List<MotionFacetVO.FacetOption> options = new ArrayList<>();
		for (Map.Entry<Integer, String> entry : new java.util.TreeMap<>(DIFFICULTY_LABELS).entrySet()) {
			List<MotionTemplate> same = all.stream()
					.filter(template -> entry.getKey().equals(template.getDifficulty()))
					.toList();
			options.add(MotionFacetVO.FacetOption.builder()
					.value(String.valueOf(entry.getKey()))
					.label(entry.getValue())
					.count((long) same.size())
					.averageScore((int) Math.round(same.stream().mapToInt(this::recommendScore).average().orElse(0)))
					.build());
		}
		return options;
	}

	/** 人工评分：同一对象重复评分执行覆盖（唯一键保证一条） */
	public void rate(MotionRatingDTO request) {
		MotionRating existing = findRating(request.getTargetType(), request.getTargetKey());
		if (existing == null) {
			MotionRating rating = new MotionRating();
			rating.setTargetType(request.getTargetType());
			rating.setTargetKey(request.getTargetKey());
			rating.setScore(request.getScore());
			rating.setReason(request.getReason() == null ? "" : request.getReason());
			ratingMapper.insert(rating);
			return;
		}
		existing.setScore(request.getScore());
		existing.setReason(request.getReason() == null ? "" : request.getReason());
		ratingMapper.updateById(existing);
	}

	public MotionRating findRating(String targetType, String targetKey) {
		return ratingMapper.selectOne(new LambdaQueryWrapper<MotionRating>()
				.eq(MotionRating::getTargetType, targetType)
				.eq(MotionRating::getTargetKey, targetKey));
	}

	public MotionTemplate require(String templateKey) {
		MotionTemplate template = templateMapper.selectOne(new LambdaQueryWrapper<MotionTemplate>()
				.eq(MotionTemplate::getTemplateKey, templateKey));
		if (template == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "动效模板不存在（key=" + templateKey + "）");
		}
		return template;
	}

	MotionTemplateItemVO toItem(MotionTemplate template) {
		int score = recommendScore(template);
		return MotionTemplateItemVO.builder()
				.id(template.getId())
				.templateKey(template.getTemplateKey())
				.name(template.getName())
				.nameEn(template.getNameEn())
				.description(template.getDescription())
				.category(template.getCategory())
				.scene(template.getScene())
				.sceneLabel(sceneLabel(template.getScene()))
				.style(template.getStyle())
				.styleLabel(styleLabel(template.getStyle()))
				.technology(template.getTechnology())
				.difficulty(template.getDifficulty())
				.difficultyLabel(DIFFICULTY_LABELS.getOrDefault(nz(template.getDifficulty()), "入门"))
				.bestFor(split(template.getBestFor()))
				.triggerType(template.getTriggerType())
				.triggerLabel(label(TRIGGER_LABELS, template.getTriggerType(), "加载时"))
				.runtimeTier(runtimeTier(template))
				.runtimeTierLabel(runtimeTierLabel(runtimeTier(template)))
				.runtimeNote(runtimeNote(template))
				.score(score)
				.stars(stars(score))
				.grade(grade(score))
				.scoreVisual(nz(template.getScoreVisual()))
				.scoreCode(nz(template.getScoreCode()))
				.scoreReuse(nz(template.getScoreReuse()))
				.scorePerf(nz(template.getScorePerf()))
				.tags(split(template.getTags()))
				.source(template.getSource())
				.sourceLabel(label(SOURCE_LABELS, template.getSource(), "官方"))
				.sourceUrl(template.getSourceUrl())
				.sourceLicense(template.getSourceLicense())
				.community("COMMUNITY".equals(template.getSource()))
				.build();
	}

	/** 场景中文名：列表、详情与推荐结果都走这一份对照表 */
	public String sceneLabel(String scene) {
		return SCENE_LABELS.getOrDefault(scene, scene);
	}

	/** 风格中文名 */
	public String styleLabel(String style) {
		return STYLE_LABELS.getOrDefault(style, style);
	}

	List<TemplateParamVO> parseParams(String json) {
		if (json == null || json.isBlank()) {
			return List.of();
		}
		try {
			List<Map<String, Object>> raw = OBJECT_MAPPER.readValue(json, new TypeReference<>() {
			});
			return raw.stream().map(item -> TemplateParamVO.builder()
					.key(String.valueOf(item.get("key")))
					.label(String.valueOf(item.getOrDefault("label", item.get("key"))))
					.unit(item.get("unit") == null ? "" : String.valueOf(item.get("unit")))
					.min(toDouble(item.get("min")))
					.max(toDouble(item.get("max")))
					.step(toDouble(item.get("step")))
					.defaultValue(toDouble(item.get("default")))
					.build()).toList();
		} catch (Exception e) {
			log.warn("模板参数 JSON 解析失败，按无参数处理：{}", e.getMessage());
			return List.of();
		}
	}

	List<String> parseKeys(String json) {
		if (json == null || json.isBlank()) {
			return List.of();
		}
		try {
			return OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>() {
			});
		} catch (Exception e) {
			log.warn("组合方案模板清单解析失败：{}", e.getMessage());
			return List.of();
		}
	}

	private Double toDouble(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		try {
			return Double.parseDouble(String.valueOf(value));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	List<String> split(String value) {
		if (value == null || value.isBlank()) {
			return List.of();
		}
		return Arrays.stream(value.split(",")).map(String::trim).filter(item -> !item.isEmpty())
				.collect(Collectors.toList());
	}

	private int nz(Integer value) {
		return value == null ? 0 : value;
	}

	private boolean notBlank(String value) {
		return value != null && !value.isBlank();
	}
}
