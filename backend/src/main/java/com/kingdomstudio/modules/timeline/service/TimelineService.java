package com.kingdomstudio.modules.timeline.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.timeline.dto.TimelineQueryDTO;
import com.kingdomstudio.modules.timeline.dto.TimelineSaveDTO;
import com.kingdomstudio.modules.timeline.entity.TimelineEvent;
import com.kingdomstudio.modules.timeline.mapper.TimelineEventMapper;
import com.kingdomstudio.modules.timeline.vo.TimelineNodeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 成长时间线：按时间倒序的一条成长路径。
 *
 * <p>排序规则：年份新的在前；同一年里，有具体日期的排在年度节点前面（越具体越靠上），
 * 再按 sortOrder、id 兜底，保证顺序稳定。
 */
@Service
@RequiredArgsConstructor
public class TimelineService {

	private final TimelineEventMapper timelineEventMapper;

	public List<TimelineNodeVO> list(TimelineQueryDTO query) {
		// 复制一份再排序：Mapper 返回的列表不该被调用方就地改动
		List<TimelineEvent> rows = new ArrayList<>(timelineEventMapper.selectList(
				new LambdaQueryWrapper<TimelineEvent>()
						.eq(query.getYear() != null, TimelineEvent::getYear, query.getYear())));
		rows.sort((left, right) -> {
			int byYear = Integer.compare(nvl(right.getYear()), nvl(left.getYear()));
			if (byYear != 0) {
				return byYear;
			}
			int byDate = Boolean.compare(right.getEventDate() != null, left.getEventDate() != null);
			if (byDate != 0) {
				return byDate;
			}
			if (left.getEventDate() != null && right.getEventDate() != null) {
				int compareDate = right.getEventDate().compareTo(left.getEventDate());
				if (compareDate != 0) {
					return compareDate;
				}
			}
			int bySort = Integer.compare(nvl(right.getSortOrder()), nvl(left.getSortOrder()));
			if (bySort != 0) {
				return bySort;
			}
			return Long.compare(nvl(right.getId()), nvl(left.getId()));
		});

		List<TimelineNodeVO> nodes = new ArrayList<>(rows.size());
		for (TimelineEvent row : rows) {
			nodes.add(toVO(row));
		}
		return nodes;
	}

	public Long create(TimelineSaveDTO request) {
		TimelineEvent entity = new TimelineEvent();
		apply(entity, request);
		timelineEventMapper.insert(entity);
		return entity.getId();
	}

	public void update(Long id, TimelineSaveDTO request) {
		TimelineEvent entity = require(id);
		apply(entity, request);
		timelineEventMapper.updateById(entity);
	}

	public void delete(Long id) {
		require(id);
		timelineEventMapper.deleteById(id);
	}

	public TimelineEvent require(Long id) {
		TimelineEvent entity = timelineEventMapper.selectById(id);
		if (entity == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "成长节点不存在：" + id);
		}
		return entity;
	}

	/** 时间文字：有具体日期显示到日，只有年份就显示年份 */
	public static String timeText(Integer year, java.time.LocalDate eventDate) {
		if (eventDate != null) {
			return eventDate.toString();
		}
		return year == null ? "" : String.valueOf(year);
	}

	private void apply(TimelineEvent entity, TimelineSaveDTO request) {
		entity.setYear(request.getYear());
		entity.setEventDate(request.getEventDate());
		entity.setTitle(request.getTitle().trim());
		entity.setDescription(request.getDescription() == null ? "" : request.getDescription().trim());
		entity.setRelatedProject(request.getRelatedProject() == null ? "" : request.getRelatedProject().trim());
		entity.setLevel(request.getLevel() == null ? 1 : request.getLevel());
		entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
	}

	private TimelineNodeVO toVO(TimelineEvent entity) {
		return TimelineNodeVO.builder()
				.id(entity.getId())
				.year(entity.getYear())
				.eventDate(entity.getEventDate())
				.timeText(timeText(entity.getYear(), entity.getEventDate()))
				.title(entity.getTitle())
				.description(entity.getDescription())
				.relatedProject(entity.getRelatedProject())
				.level(entity.getLevel())
				.sortOrder(entity.getSortOrder())
				.createTime(entity.getCreateTime())
				.build();
	}

	private static int nvl(Integer value) {
		return value == null ? 0 : value;
	}

	private static long nvl(Long value) {
		return value == null ? 0L : value;
	}
}
