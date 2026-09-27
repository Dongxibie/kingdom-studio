package com.kingdomstudio.modules.technology.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.technology.dto.TechnologyQueryDTO;
import com.kingdomstudio.modules.technology.dto.TechnologySaveDTO;
import com.kingdomstudio.modules.technology.entity.Technology;
import com.kingdomstudio.modules.technology.mapper.TechnologyMapper;
import com.kingdomstudio.modules.technology.vo.TechnologyAtlasVO;
import com.kingdomstudio.modules.technology.vo.TechnologyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 技术图鉴：分类导航 + 技术卡片 + 维护。
 *
 * <p>分类顺序写死在 {@link #CATEGORY_LABELS} 里而不是按字典序排：
 * 界面上「Java → Spring → Database → AI → Frontend → DevOps」是一条「从后端走到工程化」的路线，
 * 按字母排会把这个顺序打乱。
 */
@Service
@RequiredArgsConstructor
public class TechnologyService {

	/** 分类展示顺序与中文名 */
	public static final Map<String, String> CATEGORY_LABELS = new LinkedHashMap<>();

	static {
		CATEGORY_LABELS.put("Java", "Java");
		CATEGORY_LABELS.put("Spring", "Spring");
		CATEGORY_LABELS.put("Database", "数据库");
		CATEGORY_LABELS.put("AI", "AI");
		CATEGORY_LABELS.put("Frontend", "前端");
		CATEGORY_LABELS.put("DevOps", "工程化");
	}

	private final TechnologyMapper technologyMapper;

	public TechnologyAtlasVO atlas(TechnologyQueryDTO query) {
		List<Technology> all = technologyMapper.selectList(
				new LambdaQueryWrapper<Technology>()
						.orderByAsc(Technology::getId));
		String category = trim(query.getCategory());
		String keyword = trim(query.getKeyword());

		List<TechnologyVO> items = new ArrayList<>();
		for (Technology row : all) {
			if (StringUtils.hasText(category) && !category.equalsIgnoreCase(row.getCategory())) {
				continue;
			}
			if (StringUtils.hasText(keyword) && !matchesKeyword(row, keyword)) {
				continue;
			}
			items.add(toVO(row));
		}
		// 同分类内按掌握程度从高到低排：一眼能看出哪几项最熟
		items.sort((left, right) -> {
			int leftIndex = categoryIndex(left.getCategory());
			int rightIndex = categoryIndex(right.getCategory());
			if (leftIndex != rightIndex) {
				return Integer.compare(leftIndex, rightIndex);
			}
			int byLevel = Integer.compare(nvl(right.getLevel()), nvl(left.getLevel()));
			if (byLevel != 0) {
				return byLevel;
			}
			return Long.compare(nvl(left.getId()), nvl(right.getId()));
		});

		return TechnologyAtlasVO.builder()
				.categories(buildCategories(all))
				.items(items)
				.total(items.size())
				.build();
	}

	public TechnologyVO detail(Long id) {
		return toVO(require(id));
	}

	public Long create(TechnologySaveDTO request) {
		checkName(request.getName(), null);
		Technology entity = new Technology();
		apply(entity, request);
		technologyMapper.insert(entity);
		return entity.getId();
	}

	public void update(Long id, TechnologySaveDTO request) {
		Technology entity = require(id);
		checkName(request.getName(), id);
		apply(entity, request);
		technologyMapper.updateById(entity);
	}

	public void delete(Long id) {
		require(id);
		technologyMapper.deleteById(id);
	}

	public Technology require(Long id) {
		Technology entity = technologyMapper.selectById(id);
		if (entity == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "技术不存在：" + id);
		}
		return entity;
	}

	public void checkName(String name, Long selfId) {
		LambdaQueryWrapper<Technology> wrapper = new LambdaQueryWrapper<Technology>()
				.eq(Technology::getName, trim(name));
		if (selfId != null) {
			wrapper.ne(Technology::getId, selfId);
		}
		if (technologyMapper.selectCount(wrapper) > 0) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "已存在同名技术：" + trim(name));
		}
	}

	/** 掌握程度画成星星：前端不必知道 1-5 怎么变成图形 */
	public static String levelText(Integer level) {
		int value = level == null ? 0 : Math.max(0, Math.min(5, level));
		return "★".repeat(value) + "☆".repeat(5 - value);
	}

	/** 项目应用出接口时拆成数组，前端直接渲染成小标签 */
	public static List<String> splitProjects(String usedProjects) {
		if (!StringUtils.hasText(usedProjects)) {
			return List.of();
		}
		List<String> items = new ArrayList<>();
		for (String part : usedProjects.split(",")) {
			String item = part.trim();
			if (!item.isEmpty()) {
				items.add(item);
			}
		}
		return items;
	}

	private List<TechnologyAtlasVO.CategoryVO> buildCategories(List<Technology> all) {
		List<TechnologyAtlasVO.CategoryVO> categories = new ArrayList<>();
		categories.add(TechnologyAtlasVO.CategoryVO.builder()
				.category("ALL").label("全部").count(all.size()).build());
		for (Map.Entry<String, String> entry : CATEGORY_LABELS.entrySet()) {
			int count = 0;
			for (Technology row : all) {
				if (entry.getKey().equalsIgnoreCase(row.getCategory())) {
					count++;
				}
			}
			categories.add(TechnologyAtlasVO.CategoryVO.builder()
					.category(entry.getKey()).label(entry.getValue()).count(count).build());
		}
		return categories;
	}

	private boolean matchesKeyword(Technology row, String keyword) {
		String lower = keyword.toLowerCase();
		return contains(row.getName(), lower)
				|| contains(row.getDescription(), lower)
				|| contains(row.getUsedProjects(), lower)
				|| contains(row.getCategory(), lower);
	}

	private static boolean contains(String value, String lowerKeyword) {
		return value != null && value.toLowerCase().contains(lowerKeyword);
	}

	private static int categoryIndex(String category) {
		int index = 0;
		for (String key : CATEGORY_LABELS.keySet()) {
			if (key.equalsIgnoreCase(category)) {
				return index;
			}
			index++;
		}
		return CATEGORY_LABELS.size();
	}

	private void apply(Technology entity, TechnologySaveDTO request) {
		entity.setName(trim(request.getName()));
		entity.setCategory(request.getCategory());
		entity.setLevel(request.getLevel() == null ? 1 : request.getLevel());
		entity.setDescription(request.getDescription() == null ? "" : request.getDescription().trim());
		entity.setUsedProjects(request.getUsedProjects() == null ? "" : request.getUsedProjects().trim());
		entity.setLearnDate(request.getLearnDate());
		entity.setIcon(request.getIcon() == null ? "" : request.getIcon().trim());
	}

	private TechnologyVO toVO(Technology entity) {
		return TechnologyVO.builder()
				.id(entity.getId())
				.name(entity.getName())
				.category(entity.getCategory())
				.categoryLabel(CATEGORY_LABELS.getOrDefault(entity.getCategory(), entity.getCategory()))
				.level(entity.getLevel())
				.levelText(levelText(entity.getLevel()))
				.description(entity.getDescription())
				.usedProjects(splitProjects(entity.getUsedProjects()))
				.learnDate(entity.getLearnDate())
				.icon(entity.getIcon())
				.createTime(entity.getCreateTime())
				.updateTime(entity.getUpdateTime())
				.build();
	}

	private static String trim(String value) {
		return value == null ? null : value.trim();
	}

	private static int nvl(Integer value) {
		return value == null ? 0 : value;
	}

	private static long nvl(Long value) {
		return value == null ? 0L : value;
	}
}
