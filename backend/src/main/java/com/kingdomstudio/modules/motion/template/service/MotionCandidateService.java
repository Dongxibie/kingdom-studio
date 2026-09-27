package com.kingdomstudio.modules.motion.template.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kingdomstudio.common.PageVO;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.motion.template.dto.CandidatePromoteDTO;
import com.kingdomstudio.modules.motion.template.dto.CandidateQueryDTO;
import com.kingdomstudio.modules.motion.template.dto.CandidateReviewDTO;
import com.kingdomstudio.modules.motion.template.entity.MotionCandidate;
import com.kingdomstudio.modules.motion.template.entity.MotionTemplate;
import com.kingdomstudio.modules.motion.template.mapper.MotionCandidateMapper;
import com.kingdomstudio.modules.motion.template.mapper.MotionTemplateMapper;
import com.kingdomstudio.modules.motion.template.vo.CandidateStatsVO;
import com.kingdomstudio.modules.motion.template.vo.MotionCandidateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 候选池：GitHub 发现 → 规则分析 → 人工筛选 → 转成 Motion Pattern 的最后三步。
 *
 * <p>两条边界写死在代码里：
 * <ol>
 *   <li><b>只搬运元数据</b>：候选表里没有任何源码；</li>
 *   <li><b>转成模板时，代码来自我们自己的 Pattern</b>：{@link #promote} 要求显式指定用哪个内置模板承载，
 *       它只把候选的名字、来源、许可贴上去，绝不读取候选仓库的代码。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotionCandidateService {

	/** 人工可以改成的状态；PROMOTED 只能由提升接口产生 */
	static final List<String> REVIEWABLE = List.of("NEW", "ANALYZED", "SELECTED", "REJECTED");

	private static final Map<String, String> STATUS_LABELS = Map.of(
			"NEW", "待看",
			"ANALYZED", "已分析",
			"SELECTED", "已选入",
			"PROMOTED", "已入库",
			"REJECTED", "已淘汰");

	private static final Map<String, String> TRIGGER_LABELS = Map.of(
			"load", "加载时", "hover", "悬停", "scroll", "滚动", "click", "点击");

	private static final Map<Integer, String> DIFFICULTY_LABELS = Map.of(1, "入门", 2, "进阶", 3, "高阶");

	private static final Map<String, String> TIER_LABELS = Map.of(
			"LIGHTWEIGHT", "轻量", "BALANCED", "均衡", "GPU_ENHANCED", "依赖 GPU 加速");

	private final MotionCandidateMapper candidateMapper;
	private final MotionTemplateMapper templateMapper;
	private final MotionCandidateAnalyzer analyzer;
	private final MotionTemplateService templateService;

	/** 候选分页：状态 / 分类 / 技术 / 关键词 / 星数下限任意组合 */
	public PageVO<MotionCandidateVO> page(CandidateQueryDTO query) {
		long current = query.getPage() == null ? 1 : Math.max(1, query.getPage());
		long size = query.getSize() == null ? 12 : Math.min(Math.max(1, query.getSize()), 100);
		LambdaQueryWrapper<MotionCandidate> wrapper = new LambdaQueryWrapper<MotionCandidate>()
				.eq(notBlank(query.getStatus()), MotionCandidate::getStatus, query.getStatus())
				.eq(notBlank(query.getCategory()), MotionCandidate::getCategory, query.getCategory())
				.eq(notBlank(query.getTechnology()), MotionCandidate::getTechnology, query.getTechnology())
				.ge(query.getMinStars() != null, MotionCandidate::getStars, query.getMinStars());
		if (notBlank(query.getKeyword())) {
			String like = query.getKeyword().trim();
			wrapper.and(inner -> inner.like(MotionCandidate::getName, like)
					.or().like(MotionCandidate::getFullName, like)
					.or().like(MotionCandidate::getDescription, like)
					.or().like(MotionCandidate::getTopics, like));
		}
		String sort = query.getSort() == null ? "STARS" : query.getSort().toUpperCase(Locale.ROOT);
		switch (sort) {
			case "VISUAL" -> wrapper.orderByDesc(MotionCandidate::getVisualScore).orderByDesc(MotionCandidate::getStars);
			case "NAME" -> wrapper.orderByAsc(MotionCandidate::getName);
			default -> wrapper.orderByDesc(MotionCandidate::getStars);
		}
		Page<MotionCandidate> result = candidateMapper.selectPage(Page.of(current, size), wrapper);
		return PageVO.of(result, this::toVO);
	}

	/** 概览：候选池总数、各状态与各分类的数量，以及资源库当前的官方 / 社区配比 */
	public CandidateStatsVO stats() {
		List<MotionCandidate> all = candidateMapper.selectList(new LambdaQueryWrapper<>());
		Map<String, Long> byStatus = new LinkedHashMap<>();
		for (String status : List.of("NEW", "ANALYZED", "SELECTED", "PROMOTED", "REJECTED")) {
			byStatus.put(status, all.stream().filter(item -> status.equals(item.getStatus())).count());
		}
		Map<String, Long> byCategory = all.stream()
				.filter(item -> item.getCategory() != null)
				.collect(Collectors.groupingBy(MotionCandidate::getCategory, LinkedHashMap::new, Collectors.counting()));
		List<MotionTemplate> templates = templateMapper.selectList(new LambdaQueryWrapper<>());
		return CandidateStatsVO.builder()
				.total((long) all.size())
				.byStatus(byStatus)
				.byCategory(byCategory)
				.libraryTotal((long) templates.size())
				.officialTotal(templates.stream().filter(item -> "OFFICIAL".equals(item.getSource())).count())
				.communityTotal(templates.stream().filter(item -> "COMMUNITY".equals(item.getSource())).count())
				.build();
	}

	/** 重新跑一遍规则分析（改了关键词权重之后，可以随时把结论刷一遍） */
	public MotionCandidateVO analyze(Long id) {
		MotionCandidate candidate = require(id);
		applyAnalysis(candidate);
		candidateMapper.updateById(candidate);
		return toVO(candidate);
	}

	/** 人工筛选：改状态 + 写意见 */
	public MotionCandidateVO review(Long id, CandidateReviewDTO request) {
		String status = request.getStatus().trim().toUpperCase(Locale.ROOT);
		if (!REVIEWABLE.contains(status)) {
			throw new BusinessException(ResultCode.BAD_REQUEST,
					"只能改成 " + String.join(" / ", REVIEWABLE) + "；「已入库」由转换接口产生");
		}
		if ("REJECTED".equals(status) && (request.getNote() == null || request.getNote().isBlank())) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "淘汰要写清楚理由，后面才好复盘");
		}
		MotionCandidate candidate = require(id);
		if ("PROMOTED".equals(candidate.getStatus())) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "这条候选已经入库，状态不能再改");
		}
		candidate.setStatus(status);
		candidate.setReviewNote(request.getNote() == null ? "" : request.getNote().trim());
		candidateMapper.updateById(candidate);
		return toVO(candidate);
	}

	/**
	 * 把候选转成模板：实现代码来自指定的内置 Pattern，候选只贡献名字、来源与许可。
	 *
	 * <p>幂等：已经入库的候选直接返回它对应的模板，不会重复建。
	 */
	@Transactional
	public Map<String, Object> promote(Long id, CandidatePromoteDTO request) {
		MotionCandidate candidate = require(id);
		if ("PROMOTED".equals(candidate.getStatus()) && notBlank(candidate.getPromotedTemplateKey())) {
			MotionTemplate existing = templateService.require(candidate.getPromotedTemplateKey());
			return Map.of("templateKey", existing.getTemplateKey(), "name", existing.getName(),
					"created", false, "patternKey", candidate.getPatternKey());
		}
		MotionTemplate pattern = templateService.require(request.getPatternTemplateKey());
		String templateKey = templateKeyOf(candidate);
		MotionTemplate target = templateMapper.selectOne(new LambdaQueryWrapper<MotionTemplate>()
				.eq(MotionTemplate::getTemplateKey, templateKey));
		boolean created = target == null;
		if (created) {
			target = new MotionTemplate();
			target.setTemplateKey(templateKey);
		}
		// 代码一律来自 Pattern：这里只做「换名字、贴来源」这件事
		target.setName(notBlank(request.getName()) ? request.getName().trim() : shorten(candidate.getName(), 120));
		target.setNameEn(shorten(candidate.getFullName(), 120));
		target.setDescription(notBlank(request.getDescription()) ? request.getDescription().trim()
				: shorten(candidate.getDescription(), 400));
		target.setCategory(pattern.getCategory());
		target.setScene(pattern.getScene());
		target.setStyle(pattern.getStyle());
		target.setTechnology(pattern.getTechnology());
		target.setDifficulty(pattern.getDifficulty());
		target.setBestFor(pattern.getBestFor());
		target.setTriggerType(pattern.getTriggerType());
		target.setScoreVisual(pattern.getScoreVisual());
		target.setScoreCode(pattern.getScoreCode());
		target.setScoreReuse(pattern.getScoreReuse());
		target.setScorePerf(pattern.getScorePerf());
		target.setScore(pattern.getScore());
		target.setRuntimeTier(pattern.getRuntimeTier());
		target.setRuntimeNote(pattern.getRuntimeNote());
		target.setParams(pattern.getParams());
		target.setPreviewHtml(pattern.getPreviewHtml());
		target.setPreviewJs(pattern.getPreviewJs());
		target.setCssCode(pattern.getCssCode());
		target.setVueCode(pattern.getVueCode());
		target.setReactCode(pattern.getReactCode());
		target.setThreeCode(pattern.getThreeCode());
		target.setPrompt(pattern.getPrompt());
		target.setTags(join(pattern.getTags(), "community," + candidate.getCandidateKey()));
		target.setSource("COMMUNITY");
		target.setSourceUrl(candidate.getSourceUrl());
		target.setSourceLicense(candidate.getLicense());
		target.setStatus("READY");
		if (created) {
			templateMapper.insert(target);
		} else {
			templateMapper.updateById(target);
		}
		candidate.setStatus("PROMOTED");
		candidate.setPatternKey(pattern.getTemplateKey());
		candidate.setPromotedTemplateKey(templateKey);
		candidate.setReviewNote("已转为 Motion Pattern（实现来自 " + pattern.getTemplateKey()
				+ "），来源仅作标注：" + candidate.getSourceUrl());
		candidateMapper.updateById(candidate);
		log.info("候选 {} 已入库为模板 {}", candidate.getCandidateKey(), templateKey);
		return Map.of("templateKey", templateKey, "name", target.getName(), "created", created,
				"patternKey", pattern.getTemplateKey());
	}

	/** 取标签：与模板服务同一套兜底逻辑，null 键或未知值都回落到默认文案 */
	private static String label(Map<String, String> labels, String key, String fallback) {
		if (key == null) {
			return fallback;
		}
		String value = labels.get(key);
		return value == null ? fallback : value;
	}

	MotionCandidate require(Long id) {
		MotionCandidate candidate = id == null ? null : candidateMapper.selectById(id);
		if (candidate == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "候选不存在");
		}
		return candidate;
	}

	/** 分析结果写回实体（analyze 与入库时共用） */
	void applyAnalysis(MotionCandidate candidate) {
		MotionCandidateAnalyzer.Analysis analysis = analyzer.analyze(candidate);
		candidate.setCategory(analysis.getCategory());
		candidate.setTechnology(analysis.getTechnology());
		candidate.setTriggerType(analysis.getTrigger());
		candidate.setDifficulty(analysis.getDifficulty());
		candidate.setPerformanceLevel(analysis.getPerformanceLevel());
		candidate.setVisualScore(analysis.getVisualScore());
		candidate.setMatchedHints(String.join(",", analysis.getHints()));
	}

	/** 入库后的模板 key：由候选标识派生，稳定可读，并截断到列宽以内 */
	String templateKeyOf(MotionCandidate candidate) {
		String base = "community-" + candidate.getCandidateKey().replace("__", "-").replaceAll("[^a-z0-9-]", "-");
		base = base.replaceAll("-{2,}", "-");
		String key = base.length() > 64 ? base.substring(0, 64) : base;
		return key.endsWith("-") ? key.substring(0, key.length() - 1) : key;
	}

	MotionCandidateVO toVO(MotionCandidate candidate) {
		return MotionCandidateVO.builder()
				.id(candidate.getId())
				.candidateKey(candidate.getCandidateKey())
				.name(candidate.getName())
				.fullName(candidate.getFullName())
				.sourceUrl(candidate.getSourceUrl())
				.description(candidate.getDescription())
				.category(candidate.getCategory())
				.technology(candidate.getTechnology())
				.triggerType(candidate.getTriggerType())
				.triggerLabel(label(TRIGGER_LABELS, candidate.getTriggerType(), "加载时"))
				.difficulty(candidate.getDifficulty())
				.difficultyLabel(candidate.getDifficulty() == null ? "入门"
						: DIFFICULTY_LABELS.getOrDefault(candidate.getDifficulty(), "入门"))
				.performanceLevel(candidate.getPerformanceLevel())
				.performanceLabel(label(TIER_LABELS, candidate.getPerformanceLevel(), "均衡"))
				.visualScore(candidate.getVisualScore())
				.visualStars(candidate.getVisualScore() == null ? 0
						: Math.round(candidate.getVisualScore() / 20.0 * 2) / 2.0)
				.license(candidate.getLicense())
				.stars(candidate.getStars())
				.language(candidate.getLanguage())
				.topics(split(candidate.getTopics()))
				.matchedHints(split(candidate.getMatchedHints()))
				.prompt(candidate.getPrompt())
				.status(candidate.getStatus())
				.statusLabel(label(STATUS_LABELS, candidate.getStatus(), "待看"))
				.reviewNote(candidate.getReviewNote())
				.patternKey(candidate.getPatternKey())
				.promotedTemplateKey(candidate.getPromotedTemplateKey())
				.build();
	}

	private String join(String left, String right) {
		return (left == null || left.isBlank()) ? right : left + "," + right;
	}

	private String shorten(String value, int max) {
		if (value == null) {
			return "";
		}
		String text = value.trim();
		return text.length() > max ? text.substring(0, max) : text;
	}

	private List<String> split(String value) {
		if (value == null || value.isBlank()) {
			return List.of();
		}
		return Arrays.stream(value.split(",")).map(String::trim).filter(item -> !item.isEmpty()).toList();
	}

	private boolean notBlank(String value) {
		return value != null && !value.isBlank();
	}
}
