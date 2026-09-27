package com.kingdomstudio.modules.technology.service;

import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.technology.dto.TechnologyQueryDTO;
import com.kingdomstudio.modules.technology.dto.TechnologySaveDTO;
import com.kingdomstudio.modules.technology.entity.Technology;
import com.kingdomstudio.modules.technology.mapper.TechnologyMapper;
import com.kingdomstudio.modules.technology.vo.TechnologyAtlasVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** TechnologyService 单元测试：分类导航、排序、星级与维护 */
@ExtendWith(MockitoExtension.class)
class TechnologyServiceTest {

	@Mock
	private TechnologyMapper technologyMapper;

	@InjectMocks
	private TechnologyService technologyService;

	@Test
	@DisplayName("图鉴：分类导航带全部与各分类数量，顺序固定为后端到工程化")
	void atlasShouldBuildCategoryRail() {
		when(technologyMapper.selectList(any())).thenReturn(List.of(
				tech(1L, "Java 21", "Java", 4),
				tech(2L, "Spring Boot 3", "Spring", 4),
				tech(3L, "GitHub Actions", "DevOps", 2)));

		TechnologyAtlasVO atlas = technologyService.atlas(new TechnologyQueryDTO());

		List<String> categories = atlas.getCategories().stream()
				.map(TechnologyAtlasVO.CategoryVO::getCategory).toList();
		assertEquals(List.of("ALL", "Java", "Spring", "Database", "AI", "Frontend", "DevOps"), categories);
		assertEquals(3, atlas.getCategories().get(0).getCount());
		assertEquals(1, atlas.getCategories().get(6).getCount(), "DevOps 分类应有 1 条");
		assertEquals(3, atlas.getTotal());
	}

	@Test
	@DisplayName("图鉴：同分类内按掌握程度从高到低排")
	void atlasShouldSortByLevelInsideCategory() {
		when(technologyMapper.selectList(any())).thenReturn(List.of(
				tech(1L, "Redis", "Database", 3),
				tech(2L, "MySQL 8", "Database", 4),
				tech(3L, "GitHub Actions", "DevOps", 2)));

		TechnologyAtlasVO atlas = technologyService.atlas(new TechnologyQueryDTO());

		assertEquals(List.of("MySQL 8", "Redis", "GitHub Actions"),
				atlas.getItems().stream().map(item -> item.getName()).toList());
	}

	@Test
	@DisplayName("图鉴：分类与关键词过滤同时生效，关键词忽略大小写")
	void atlasShouldFilterByCategoryAndKeyword() {
		when(technologyMapper.selectList(any())).thenReturn(List.of(
				tech(1L, "Vite", "Frontend", 4),
				tech(2L, "Vue 3 + TypeScript", "Frontend", 4),
				tech(3L, "MySQL 8", "Database", 4)));
		TechnologyQueryDTO query = new TechnologyQueryDTO();
		query.setCategory("Frontend");
		query.setKeyword("VUE");

		TechnologyAtlasVO atlas = technologyService.atlas(query);

		assertEquals(1, atlas.getTotal());
		assertEquals("Vue 3 + TypeScript", atlas.getItems().get(0).getName());
	}

	@Test
	@DisplayName("星级文字：4 星是四实一空，空值不炸")
	void levelTextShouldRenderStars() {
		assertEquals("★★★★☆", TechnologyService.levelText(4));
		assertEquals("★★★★★", TechnologyService.levelText(5));
		assertEquals("☆☆☆☆☆", TechnologyService.levelText(null));
		assertEquals("★★★★★", TechnologyService.levelText(9), "越界值夹到 5 星");
	}

	@Test
	@DisplayName("新增：同名技术直接拒绝")
	void createShouldRejectDuplicatedName() {
		when(technologyMapper.selectCount(any())).thenReturn(1L);
		TechnologySaveDTO request = new TechnologySaveDTO();
		request.setName("Spring Boot 3");
		request.setCategory("Spring");

		BusinessException error = assertThrows(BusinessException.class, () -> technologyService.create(request));

		assertTrue(error.getMessage().contains("已存在同名技术"));
		verify(technologyMapper, never()).insert(any(Technology.class));
	}

	@Test
	@DisplayName("新增：等级缺省为 1 星，项目应用落库成空串")
	void createShouldApplyDefaults() {
		when(technologyMapper.selectCount(any())).thenReturn(0L);
		TechnologySaveDTO request = new TechnologySaveDTO();
		request.setName("Vercel 部署");
		request.setCategory("DevOps");

		technologyService.create(request);

		ArgumentCaptor<Technology> captor = ArgumentCaptor.forClass(Technology.class);
		verify(technologyMapper).insert(captor.capture());
		assertEquals(1, captor.getValue().getLevel());
		assertEquals("", captor.getValue().getUsedProjects());
	}

	@Test
	@DisplayName("详情：id 不存在时抛 404；存在时项目应用拆成数组")
	void detailShouldMapProjects() {
		Technology row = tech(7L, "MyBatis Plus", "Spring", 4);
		row.setUsedProjects("Kingdom Studio,九八 · 校园快递代取订单管理系统");
		when(technologyMapper.selectById(7L)).thenReturn(row);

		var detail = technologyService.detail(7L);

		assertEquals(2, detail.getUsedProjects().size());
		assertEquals("Spring", detail.getCategoryLabel());

		when(technologyMapper.selectById(8L)).thenReturn(null);
		assertThrows(BusinessException.class, () -> technologyService.detail(8L));
	}

	@Test
	@DisplayName("删除：先确认存在再删除")
	void deleteShouldRequireExistingRow() {
		when(technologyMapper.selectById(9L)).thenReturn(tech(9L, "Redis", "Database", 3));

		technologyService.delete(9L);

		verify(technologyMapper).deleteById(9L);
	}

	@Test
	@DisplayName("项目应用拆分：空值返回空数组，多余空格去掉")
	void splitProjectsShouldTrim() {
		assertTrue(TechnologyService.splitProjects(null).isEmpty());
		assertEquals(List.of("Kingdom Studio"), TechnologyService.splitProjects(" Kingdom Studio , "));
	}

	private Technology tech(Long id, String name, String category, int level) {
		Technology row = new Technology();
		row.setId(id);
		row.setName(name);
		row.setCategory(category);
		row.setLevel(level);
		row.setDescription("说明");
		return row;
	}
}
