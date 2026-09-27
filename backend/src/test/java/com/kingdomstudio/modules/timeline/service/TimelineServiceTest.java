package com.kingdomstudio.modules.timeline.service;

import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.timeline.dto.TimelineQueryDTO;
import com.kingdomstudio.modules.timeline.dto.TimelineSaveDTO;
import com.kingdomstudio.modules.timeline.entity.TimelineEvent;
import com.kingdomstudio.modules.timeline.mapper.TimelineEventMapper;
import com.kingdomstudio.modules.timeline.vo.TimelineNodeVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** TimelineService 单元测试：排序规则、时间文字与维护 */
@ExtendWith(MockitoExtension.class)
class TimelineServiceTest {

	@Mock
	private TimelineEventMapper timelineEventMapper;

	@InjectMocks
	private TimelineService timelineService;

	@Test
	@DisplayName("列表：年份倒序，同年内具体日期排在年度节点前面")
	void listShouldOrderByYearThenDate() {
		when(timelineEventMapper.selectList(any())).thenReturn(List.of(
				yearNode(1L, 2024, "Java Apprentice", 2),
				datedNode(2L, 2026, "2026-09-24", "两个扩展模块落地", 4),
				yearNode(3L, 2026, "AI Kingdom Builder", 4),
				datedNode(4L, 2026, "2026-09-27", "AI 演奏工作台 v1.2", 5)));

		List<TimelineNodeVO> nodes = timelineService.list(new TimelineQueryDTO());

		assertEquals(List.of("AI 演奏工作台 v1.2", "两个扩展模块落地", "AI Kingdom Builder", "Java Apprentice"),
				nodes.stream().map(TimelineNodeVO::getTitle).toList());
	}

	@Test
	@DisplayName("时间文字：有日期用日期，只有年份用年份")
	void timeTextShouldPreferDate() {
		assertEquals("2026-09-27", TimelineService.timeText(2026, LocalDate.of(2026, 9, 27)));
		assertEquals("2026", TimelineService.timeText(2026, null));
		assertEquals("", TimelineService.timeText(null, null));
	}

	@Test
	@DisplayName("新增：等级缺省 1、排序缺省 0")
	void createShouldApplyDefaults() {
		TimelineSaveDTO request = new TimelineSaveDTO();
		request.setYear(2026);
		request.setTitle("  新节点  ");

		timelineService.create(request);

		ArgumentCaptor<TimelineEvent> captor = ArgumentCaptor.forClass(TimelineEvent.class);
		verify(timelineEventMapper).insert(captor.capture());
		assertEquals("新节点", captor.getValue().getTitle());
		assertEquals(1, captor.getValue().getLevel());
		assertEquals(0, captor.getValue().getSortOrder());
		assertEquals("", captor.getValue().getRelatedProject());
	}

	@Test
	@DisplayName("修改：内容整体覆盖，关联项目保留")
	void updateShouldOverwriteFields() {
		TimelineEvent row = yearNode(5L, 2026, "旧标题", 4);
		when(timelineEventMapper.selectById(5L)).thenReturn(row);
		TimelineSaveDTO request = new TimelineSaveDTO();
		request.setYear(2026);
		request.setEventDate(LocalDate.of(2026, 9, 27));
		request.setTitle("主站四个模块补齐");
		request.setRelatedProject("Kingdom Studio");
		request.setLevel(5);

		timelineService.update(5L, request);

		verify(timelineEventMapper).updateById(row);
		assertEquals(LocalDate.of(2026, 9, 27), row.getEventDate());
		assertEquals("Kingdom Studio", row.getRelatedProject());
	}

	@Test
	@DisplayName("删除：不存在时抛 404")
	void deleteShouldFailWhenMissing() {
		when(timelineEventMapper.selectById(66L)).thenReturn(null);

		BusinessException error = assertThrows(BusinessException.class, () -> timelineService.delete(66L));

		assertEquals(404, error.getCode());
	}

	@Test
	@DisplayName("列表：按年份过滤")
	void listShouldPassYearFilter() {
		when(timelineEventMapper.selectList(any())).thenReturn(List.of());
		TimelineQueryDTO query = new TimelineQueryDTO();
		query.setYear(2026);

		timelineService.list(query);

		verify(timelineEventMapper).selectList(any());
	}

	private TimelineEvent yearNode(Long id, int year, String title, int level) {
		TimelineEvent row = new TimelineEvent();
		row.setId(id);
		row.setYear(year);
		row.setTitle(title);
		row.setDescription("描述");
		row.setLevel(level);
		row.setSortOrder(1);
		return row;
	}

	private TimelineEvent datedNode(Long id, int year, String date, String title, int level) {
		TimelineEvent row = yearNode(id, year, title, level);
		row.setEventDate(LocalDate.parse(date));
		return row;
	}
}
