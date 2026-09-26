package com.kingdomstudio.modules.motion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kingdomstudio.common.PageVO;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.motion.dto.MotionCodeSaveDTO;
import com.kingdomstudio.modules.motion.dto.MotionQueryDTO;
import com.kingdomstudio.modules.motion.dto.MotionSaveDTO;
import com.kingdomstudio.modules.motion.entity.MotionCode;
import com.kingdomstudio.modules.motion.entity.MotionResource;
import com.kingdomstudio.modules.motion.mapper.MotionCodeMapper;
import com.kingdomstudio.modules.motion.mapper.MotionResourceMapper;
import com.kingdomstudio.modules.motion.vo.MotionDetailVO;
import com.kingdomstudio.modules.motion.vo.MotionListItemVO;
import com.kingdomstudio.modules.motion.vo.MotionModuleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 动效基因库：模块自检 + 动效资源的增删改查。
 *
 * <p>Phase 1 只提供 moduleInfo()，Phase 2 增加 CRUD；业务规则集中在这里，
 * Controller 只做参数接收与转发（项目分层要求）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotionService {

	/** 十个固定分类，与数据库 CHECK 约束、前端分类树三处保持一致 */
	public static final List<String> CATEGORIES = List.of(
			"Entrance", "Hover", "Scroll", "Text", "Particle", "3D", "Glass", "Cursor", "Background", "Loading");

	private static final List<String> STATUSES = List.of("DRAFT", "READY", "ARCHIVED");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final int MAX_PAGE_SIZE = 100;

	private final MotionResourceMapper motionResourceMapper;
	private final MotionCodeMapper motionCodeMapper;

	/** 模块自检：模块信息集中在这里，避免散落到 Controller 或前端常量里 */
	public MotionModuleVO moduleInfo() {
		log.debug("动效基因库模块自检");
		return MotionModuleVO.builder()
				.module("motion")
				.name("动效基因库")
				.englishName("Kingdom Motion Lab")
				.phase("v1.0.1 · 已上线（增删改查 / 采集 / 代码生成）")
				.apiBase("/api/motion")
				.capabilities(List.of(
						"动效资源的增删改查（列表支持分类 / 技术栈 / 关键词筛选与分页）",
						"分类体系（十个固定分类，与数据库 CHECK 约束一致）",
						"代码产物：Prompt / Vue 3 / React / CSS / Three.js 五种",
						"安全预览（sandbox iframe，禁止 eval 与直接执行未知 JS）",
						"去重（名称 + 来源 + 代码哈希，唯一键兜底）"))
				.plannedTables(List.of("motion_resource", "motion_code"))
				.build();
	}

	/** 分页查询：分类 / 技术栈 / 关键词三条件可组合 */
	public PageVO<MotionListItemVO> page(MotionQueryDTO query) {
		long current = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
		long size = query.getSize() == null || query.getSize() < 1 ? 20 : Math.min(query.getSize(), MAX_PAGE_SIZE);

		LambdaQueryWrapper<MotionResource> wrapper = new LambdaQueryWrapper<>();
		if (isSet(query.getCategory()) && !"ALL".equalsIgnoreCase(query.getCategory())) {
			wrapper.eq(MotionResource::getCategory, query.getCategory());
		}
		if (isSet(query.getTechnology())) {
			wrapper.eq(MotionResource::getTechnology, query.getTechnology());
		}
		if (isSet(query.getKeyword())) {
			String keyword = query.getKeyword().trim();
			wrapper.and(w -> w.like(MotionResource::getName, keyword)
					.or().like(MotionResource::getDescription, keyword)
					.or().like(MotionResource::getTags, keyword));
		}
		wrapper.orderByDesc(MotionResource::getUpdateTime).orderByDesc(MotionResource::getId);

		Page<MotionResource> result = motionResourceMapper.selectPage(new Page<>(current, size), wrapper);
		// 只有列表项需要 hasCode，这里一次性查出有代码的资源 id，避免逐条 count
		List<Long> idsWithCode = result.getRecords().isEmpty()
				? List.of()
				: motionCodeMapper.selectList(new LambdaQueryWrapper<MotionCode>()
						.in(MotionCode::getMotionId, result.getRecords().stream().map(MotionResource::getId).toList()))
				.stream().map(MotionCode::getMotionId).toList();
		return PageVO.of(result, item -> toListItem(item, idsWithCode.contains(item.getId())));
	}

	public MotionDetailVO detail(Long id) {
		MotionResource resource = requireResource(id);
		MotionCode code = findCode(id);
		return MotionDetailVO.builder()
				.id(resource.getId())
				.name(resource.getName())
				.description(resource.getDescription())
				.category(resource.getCategory())
				.technology(resource.getTechnology())
				.sourceUrl(resource.getSourceUrl())
				.repoUrl(resource.getRepoUrl())
				.previewUrl(resource.getPreviewUrl())
				.tags(splitTags(resource.getTags()))
				.license(resource.getLicense())
				.codePath(resource.getCodePath())
				.status(resource.getStatus())
				.createTime(format(resource.getCreateTime()))
				.updateTime(format(resource.getUpdateTime()))
				.code(code == null ? null : toCodeDto(code))
				.build();
	}

	@Transactional(rollbackFor = Exception.class)
	public Long create(MotionSaveDTO dto) {
		MotionResource resource = new MotionResource();
		applySave(resource, dto);
		resource.setContentHash(hashOf(dto));
		motionResourceMapper.insert(resource);
		log.info("新增动效资源 id={} name={}", resource.getId(), resource.getName());
		return resource.getId();
	}

	@Transactional(rollbackFor = Exception.class)
	public void update(Long id, MotionSaveDTO dto) {
		MotionResource resource = requireResource(id);
		applySave(resource, dto);
		resource.setContentHash(hashOf(dto));
		motionResourceMapper.updateById(resource);
		log.info("修改动效资源 id={}", id);
	}

	@Transactional(rollbackFor = Exception.class)
	public void delete(Long id) {
		requireResource(id);
		// 逻辑删除资源本身，同时把它的代码一起标记删除，避免残留孤儿数据
		motionResourceMapper.deleteById(id);
		motionCodeMapper.delete(new LambdaQueryWrapper<MotionCode>().eq(MotionCode::getMotionId, id));
		log.info("删除动效资源 id={}", id);
	}

	/** 保存代码产物：没有就插入，有就更新（一条资源只保留一条代码记录） */
	@Transactional(rollbackFor = Exception.class)
	public void saveCode(Long motionId, MotionCodeSaveDTO dto) {
		requireResource(motionId);
		MotionCode existing = findCode(motionId);
		if (existing == null) {
			MotionCode created = new MotionCode();
			created.setMotionId(motionId);
			applyCode(created, dto);
			motionCodeMapper.insert(created);
		} else {
			applyCode(existing, dto);
			motionCodeMapper.updateById(existing);
		}
		log.info("保存动效代码 motionId={}", motionId);
	}

	/** 可选：让分类列表由后端提供，前端不必再维护一份常量 */
	public List<String> categories() {
		return CATEGORIES;
	}

	// ------------------------------------------------------------------
	// 内部方法
	// ------------------------------------------------------------------

	private void applySave(MotionResource resource, MotionSaveDTO dto) {
		String category = dto.getCategory() == null ? "" : dto.getCategory().trim();
		if (!CATEGORIES.contains(category)) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "分类必须是以下之一：" + String.join(" / ", CATEGORIES));
		}
		String status = isSet(dto.getStatus()) ? dto.getStatus().toUpperCase(Locale.ROOT) : "READY";
		if (!STATUSES.contains(status)) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "状态必须是 DRAFT / READY / ARCHIVED 之一");
		}
		resource.setName(dto.getName().trim());
		resource.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
		resource.setCategory(category);
		resource.setTechnology(dto.getTechnology() == null ? "" : dto.getTechnology());
		resource.setSourceUrl(dto.getSourceUrl() == null ? "" : dto.getSourceUrl());
		resource.setRepoUrl(dto.getRepoUrl() == null ? "" : dto.getRepoUrl());
		resource.setPreviewUrl(dto.getPreviewUrl() == null ? "" : dto.getPreviewUrl());
		resource.setTags(normalizeTags(dto.getTags()));
		resource.setLicense(dto.getLicense() == null ? "" : dto.getLicense());
		resource.setCodePath(dto.getCodePath() == null ? "" : dto.getCodePath());
		resource.setStatus(status);
	}

	private void applyCode(MotionCode target, MotionCodeSaveDTO dto) {
		target.setPrompt(dto.getPrompt());
		target.setVueCode(dto.getVueCode());
		target.setReactCode(dto.getReactCode());
		target.setCssCode(dto.getCssCode());
		target.setThreeCode(dto.getThreeCode());
	}

	private MotionResource requireResource(Long id) {
		MotionResource resource = id == null ? null : motionResourceMapper.selectById(id);
		if (resource == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "动效资源不存在（id=" + id + "）");
		}
		return resource;
	}

	private MotionCode findCode(Long motionId) {
		return motionCodeMapper.selectOne(new LambdaQueryWrapper<MotionCode>()
				.eq(MotionCode::getMotionId, motionId)
				.orderByAsc(MotionCode::getId)
				.last("limit 1"));
	}

	private MotionListItemVO toListItem(MotionResource item, boolean hasCode) {
		return MotionListItemVO.builder()
				.id(item.getId())
				.name(item.getName())
				.description(item.getDescription())
				.category(item.getCategory())
				.technology(item.getTechnology())
				.sourceUrl(item.getSourceUrl())
				.previewUrl(item.getPreviewUrl())
				.tags(splitTags(item.getTags()))
				.license(item.getLicense())
				.status(item.getStatus())
				.hasCode(hasCode)
				.updateTime(format(item.getUpdateTime()))
				.build();
	}

	private MotionCodeSaveDTO toCodeDto(MotionCode code) {
		MotionCodeSaveDTO dto = new MotionCodeSaveDTO();
		dto.setPrompt(code.getPrompt());
		dto.setVueCode(code.getVueCode());
		dto.setReactCode(code.getReactCode());
		dto.setCssCode(code.getCssCode());
		dto.setThreeCode(code.getThreeCode());
		return dto;
	}

	private static boolean isSet(String value) {
		return value != null && !value.isBlank();
	}

	private static List<String> splitTags(String tags) {
		if (!isSet(tags)) {
			return List.of();
		}
		return Arrays.stream(tags.split(",")).map(String::trim).filter(t -> !t.isEmpty()).toList();
	}

	private static String normalizeTags(String tags) {
		return String.join(",", splitTags(tags));
	}

	private static String format(LocalDateTime time) {
		return time == null ? "" : time.format(TIME_FORMATTER);
	}

	/**
	 * 内容哈希：名称 + 来源 + 分类。
	 * Phase 3 的采集器用同一算法生成哈希，唯一键 uk_motion_resource_hash 负责兜底去重。
	 */
	private static String hashOf(MotionSaveDTO dto) {
		String raw = (dto.getName() == null ? "" : dto.getName().trim().toLowerCase(Locale.ROOT))
				+ "|" + (dto.getSourceUrl() == null ? "" : dto.getSourceUrl().trim().toLowerCase(Locale.ROOT))
				+ "|" + (dto.getCategory() == null ? "" : dto.getCategory());
		if ("||".equals(raw) || raw.isBlank()) {
			return "";
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder(bytes.length * 2);
			for (byte b : bytes) {
				builder.append(String.format("%02x", b));
			}
			return builder.toString();
		} catch (NoSuchAlgorithmException e) {
			// SHA-1 是 JDK 必备实现，正常不会走到这里；真出现时退回空哈希（唯一键允许重复空值不可行，故退化为随机）
			log.warn("SHA-1 不可用，去重哈希退化为时间戳", e);
			return String.format("%040x", System.nanoTime());
		}
	}
}
