package com.kingdomstudio.modules.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kingdomstudio.common.PageVO;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.project.dto.ProjectQueryDTO;
import com.kingdomstudio.modules.project.dto.ProjectSaveDTO;
import com.kingdomstudio.modules.project.entity.Project;
import com.kingdomstudio.modules.project.mapper.ProjectMapper;
import com.kingdomstudio.modules.project.vo.ProjectDetailVO;
import com.kingdomstudio.modules.project.vo.ProjectListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 项目王国：列表、详情与维护。
 *
 * <p>列表与详情分成两个 VO：列表要轻（卡片只需要简介与技术栈），
 * 详情才带 Markdown 亮点 —— 一次拉十几张卡片时，把亮点一起带回来是白流量。
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

	/** 状态码 → 中文：库里存码，界面上给人看的是中文 */
	private static final Map<String, String> STATUS_LABELS = Map.of(
			"PLANNING", "规划中",
			"DEVELOPING", "持续开发",
			"COMPLETED", "已完成");

	private static final String DEFAULT_STATUS = "PLANNING";

	private final ProjectMapper projectMapper;

	public PageVO<ProjectListItemVO> page(ProjectQueryDTO query) {
		Page<Project> page = new Page<>(query.getPage(), query.getSize());
		String keyword = trim(query.getKeyword());
		LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
				.eq(StringUtils.hasText(query.getStatus()), Project::getStatus, trim(query.getStatus()))
				.and(StringUtils.hasText(keyword), w -> w
						.like(Project::getName, keyword)
						.or().like(Project::getDescription, keyword)
						.or().like(Project::getTechnologyStack, keyword))
				.orderByAsc(Project::getSortOrder)
				.orderByDesc(Project::getId);
		return PageVO.of(projectMapper.selectPage(page, wrapper), this::toListItem);
	}

	public ProjectDetailVO detail(Long id) {
		return toDetail(require(id));
	}

	public Long create(ProjectSaveDTO request) {
		checkName(request.getName(), null);
		Project entity = new Project();
		apply(entity, request);
		projectMapper.insert(entity);
		return entity.getId();
	}

	public void update(Long id, ProjectSaveDTO request) {
		Project entity = require(id);
		checkName(request.getName(), id);
		apply(entity, request);
		projectMapper.updateById(entity);
	}

	public void delete(Long id) {
		require(id);
		projectMapper.deleteById(id);
	}

	/** 取出一条项目，取不到就抛 404：调用方不必重复写「存在吗」 */
	public Project require(Long id) {
		Project entity = projectMapper.selectById(id);
		if (entity == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在：" + id);
		}
		return entity;
	}

	/** 名称唯一：同一个项目出现两条，列表页会分不清点哪个 */
	public void checkName(String name, Long selfId) {
		LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
				.eq(Project::getName, trim(name));
		if (selfId != null) {
			wrapper.ne(Project::getId, selfId);
		}
		if (projectMapper.selectCount(wrapper) > 0) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "已存在同名项目：" + trim(name));
		}
	}

	private void apply(Project entity, ProjectSaveDTO request) {
		entity.setName(trim(request.getName()));
		entity.setDescription(nullToEmpty(request.getDescription()));
		entity.setCoverImage(nullToEmpty(request.getCoverImage()));
		entity.setTechnologyStack(nullToEmpty(request.getTechnologyStack()));
		entity.setGithubUrl(nullToEmpty(request.getGithubUrl()));
		entity.setDemoUrl(nullToEmpty(request.getDemoUrl()));
		entity.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : DEFAULT_STATUS);
		entity.setProgress(request.getProgress() == null ? 0 : request.getProgress());
		entity.setCodeLines(request.getCodeLines());
		entity.setTestCount(request.getTestCount());
		entity.setCommitCount(request.getCommitCount());
		entity.setHighlights(request.getHighlights());
		entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
	}

	private ProjectListItemVO toListItem(Project entity) {
		return ProjectListItemVO.builder()
				.id(entity.getId())
				.name(entity.getName())
				.description(entity.getDescription())
				.coverImage(entity.getCoverImage())
				.technologyStack(splitStack(entity.getTechnologyStack()))
				.githubUrl(entity.getGithubUrl())
				.demoUrl(entity.getDemoUrl())
				.status(entity.getStatus())
				.statusLabel(STATUS_LABELS.getOrDefault(entity.getStatus(), entity.getStatus()))
				.progress(entity.getProgress())
				.codeLines(entity.getCodeLines())
				.testCount(entity.getTestCount())
				.commitCount(entity.getCommitCount())
				.sortOrder(entity.getSortOrder())
				.createTime(entity.getCreateTime())
				.updateTime(entity.getUpdateTime())
				.build();
	}

	private ProjectDetailVO toDetail(Project entity) {
		return ProjectDetailVO.builder()
				.id(entity.getId())
				.name(entity.getName())
				.description(entity.getDescription())
				.coverImage(entity.getCoverImage())
				.technologyStack(splitStack(entity.getTechnologyStack()))
				.githubUrl(entity.getGithubUrl())
				.demoUrl(entity.getDemoUrl())
				.status(entity.getStatus())
				.statusLabel(STATUS_LABELS.getOrDefault(entity.getStatus(), entity.getStatus()))
				.progress(entity.getProgress())
				.codeLines(entity.getCodeLines())
				.testCount(entity.getTestCount())
				.commitCount(entity.getCommitCount())
				.highlights(entity.getHighlights())
				.sortOrder(entity.getSortOrder())
				.createTime(entity.getCreateTime())
				.updateTime(entity.getUpdateTime())
				.build();
	}

	/** 技术栈入库时是一个字符串，出接口时拆成数组，免得每个前端各拆一遍 */
	public static List<String> splitStack(String stack) {
		if (!StringUtils.hasText(stack)) {
			return List.of();
		}
		List<String> items = new ArrayList<>();
		for (String part : stack.split(",")) {
			String item = part.trim();
			if (!item.isEmpty()) {
				items.add(item);
			}
		}
		return items;
	}

	private static String trim(String value) {
		return value == null ? null : value.trim();
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value.trim();
	}
}
