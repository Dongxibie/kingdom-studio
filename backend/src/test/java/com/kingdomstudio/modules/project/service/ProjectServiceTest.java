package com.kingdomstudio.modules.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kingdomstudio.common.PageVO;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.project.dto.ProjectQueryDTO;
import com.kingdomstudio.modules.project.dto.ProjectSaveDTO;
import com.kingdomstudio.modules.project.entity.Project;
import com.kingdomstudio.modules.project.mapper.ProjectMapper;
import com.kingdomstudio.modules.project.vo.ProjectDetailVO;
import com.kingdomstudio.modules.project.vo.ProjectListItemVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** ProjectService 单元测试：分页、详情、唯一名与默认值 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

	@Mock
	private ProjectMapper projectMapper;

	@InjectMocks
	private ProjectService projectService;

	@Test
	@DisplayName("分页：状态码翻成中文，技术栈拆成数组")
	void pageShouldMapLabelsAndStack() {
		Project row = sampleProject(1L, "Kingdom Studio", "DEVELOPING");
		row.setTechnologyStack("Java, Spring Boot ,MySQL");
		stubPage(row);

		PageVO<ProjectListItemVO> result = projectService.page(new ProjectQueryDTO());

		assertEquals(1, result.getTotal());
		ProjectListItemVO item = result.getRecords().get(0);
		assertEquals("持续开发", item.getStatusLabel());
		assertEquals(List.of("Java", "Spring Boot", "MySQL"), item.getTechnologyStack());
	}

	@Test
	@DisplayName("分页：查不到数据时返回空列表而不是 null")
	void pageShouldReturnEmptyWhenNoData() {
		stubPage(null);

		PageVO<ProjectListItemVO> result = projectService.page(new ProjectQueryDTO());

		assertTrue(result.getRecords().isEmpty());
		assertEquals(0, result.getTotal());
	}

	@Test
	@DisplayName("分页：页码与每页条数原样传给 Mapper")
	void pageShouldPassPagination() {
		stubPage(null);
		ProjectQueryDTO query = new ProjectQueryDTO();
		query.setPage(2);
		query.setSize(5);

		projectService.page(query);

		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(projectMapper).selectPage((Page<Project>) captor.capture(), any());
		Page<Project> page = (Page<Project>) captor.getValue();
		assertEquals(2, page.getCurrent());
		assertEquals(5, page.getSize());
	}

	@Test
	@DisplayName("详情：返回 Markdown 亮点与完成度")
	void detailShouldCarryHighlights() {
		Project row = sampleProject(2L, "AI Builder Studio", "COMPLETED");
		row.setHighlights("### 核心创新\n- Prompt 生命周期管理");
		row.setProgress(100);
		when(projectMapper.selectById(2L)).thenReturn(row);

		ProjectDetailVO detail = projectService.detail(2L);

		assertEquals("已完成", detail.getStatusLabel());
		assertEquals(100, detail.getProgress());
		assertTrue(detail.getHighlights().contains("Prompt 生命周期管理"));
	}

	@Test
	@DisplayName("详情：id 不存在时抛 404")
	void detailShouldFailWhenMissing() {
		when(projectMapper.selectById(99L)).thenReturn(null);

		BusinessException error = assertThrows(BusinessException.class, () -> projectService.detail(99L));

		assertEquals(404, error.getCode());
	}

	@Test
	@DisplayName("新增：同名项目直接拒绝")
	void createShouldRejectDuplicatedName() {
		when(projectMapper.selectCount(any())).thenReturn(1L);
		ProjectSaveDTO request = sampleRequest("Kingdom Studio");

		BusinessException error = assertThrows(BusinessException.class, () -> projectService.create(request));

		assertTrue(error.getMessage().contains("已存在同名项目"));
		verify(projectMapper, never()).insert(any(Project.class));
	}

	@Test
	@DisplayName("新增：没填的字段落库成空串，状态默认规划中、完成度默认 0")
	void createShouldApplyDefaults() {
		when(projectMapper.selectCount(any())).thenReturn(0L);
		ProjectSaveDTO request = new ProjectSaveDTO();
		request.setName("  新项目  ");

		projectService.create(request);

		ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
		verify(projectMapper).insert(captor.capture());
		Project saved = captor.getValue();
		assertEquals("新项目", saved.getName(), "名称应去掉首尾空格");
		assertEquals("PLANNING", saved.getStatus());
		assertEquals(0, saved.getProgress());
		assertEquals("", saved.getTechnologyStack());
		assertNull(saved.getHighlights());
	}

	@Test
	@DisplayName("修改：改成自己原来的名字不算重名")
	void updateShouldAllowSameName() {
		Project row = sampleProject(3L, "Offer Hunter AI · AI 求职助手", "COMPLETED");
		when(projectMapper.selectById(3L)).thenReturn(row);
		when(projectMapper.selectCount(any())).thenReturn(0L);
		ProjectSaveDTO request = sampleRequest("Offer Hunter AI · AI 求职助手");
		request.setStatus("DEVELOPING");

		projectService.update(3L, request);

		verify(projectMapper).updateById(row);
		assertEquals("DEVELOPING", row.getStatus());
	}

	@Test
	@DisplayName("删除：逻辑删除前先确认存在；不存在则抛 404")
	void deleteShouldRequireExistingRow() {
		Project row = sampleProject(4L, "WEDA 博客站", "COMPLETED");
		when(projectMapper.selectById(4L)).thenReturn(row);

		projectService.delete(4L);
		verify(projectMapper).deleteById(4L);

		when(projectMapper.selectById(5L)).thenReturn(null);
		assertThrows(BusinessException.class, () -> projectService.delete(5L));
	}

	@Test
	@DisplayName("技术栈拆分：空值与多余空格都处理掉")
	void splitStackShouldIgnoreBlankParts() {
		assertTrue(ProjectService.splitStack(null).isEmpty());
		assertTrue(ProjectService.splitStack("   ").isEmpty());
		assertEquals(List.of("Vue3", "TypeScript"), ProjectService.splitStack("Vue3, TypeScript, "));
	}

	private Project sampleProject(Long id, String name, String status) {
		Project row = new Project();
		row.setId(id);
		row.setName(name);
		row.setDescription("简介");
		row.setStatus(status);
		row.setProgress(60);
		row.setSortOrder(1);
		row.setCreateTime(LocalDateTime.of(2026, 9, 27, 10, 0));
		return row;
	}

	private ProjectSaveDTO sampleRequest(String name) {
		ProjectSaveDTO request = new ProjectSaveDTO();
		request.setName(name);
		request.setDescription("简介");
		request.setTechnologyStack("Java,MySQL");
		return request;
	}

	@SuppressWarnings("unchecked")
	private void stubPage(Project... rows) {
		when(projectMapper.selectPage(any(), any())).thenAnswer(invocation -> {
			Page<Project> page = invocation.getArgument(0);
			page.setRecords(rows == null ? List.of() : List.of(rows));
			page.setTotal(rows == null ? 0 : rows.length);
			return page;
		});
	}
}
