package com.kingdomstudio.modules.motion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kingdomstudio.common.PageVO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MotionService 单元测试。
 *
 * <p>用 Mockito 把 Mapper 打桩，不依赖 MySQL / Redis——
 * 这样 {@code mvn test} 在任何机器上都能跑过，不需要先起数据库。
 * 端到端的接口测试由前端联调阶段用真实服务验证。
 */
@ExtendWith(MockitoExtension.class)
class MotionServiceTest {

	@Mock
	private MotionResourceMapper motionResourceMapper;

	@Mock
	private MotionCodeMapper motionCodeMapper;

	@InjectMocks
	private MotionService motionService;

	private MotionResource sample(Long id) {
		MotionResource resource = new MotionResource();
		resource.setId(id);
		resource.setName("玻璃卡片悬停");
		resource.setDescription("悬浮时上浮并有一道光扫过");
		resource.setCategory("Hover");
		resource.setTechnology("CSS");
		resource.setTags("glass,premium");
		resource.setStatus("READY");
		resource.setLicense("MIT");
		resource.setCreateTime(LocalDateTime.of(2026, 9, 25, 10, 0, 0));
		resource.setUpdateTime(LocalDateTime.of(2026, 9, 25, 10, 30, 0));
		return resource;
	}

	private MotionSaveDTO saveDto(String category) {
		MotionSaveDTO dto = new MotionSaveDTO();
		dto.setName("玻璃卡片悬停");
		dto.setDescription("悬浮时上浮并有一道光扫过");
		dto.setCategory(category);
		dto.setTechnology("CSS");
		dto.setTags(" glass , premium ,");
		dto.setSourceUrl("https://example.com/card");
		return dto;
	}

	@BeforeEach
	void injectVersion() {
		// @InjectMocks 不会处理 @Value 字段，这里按 application.yml 的值注入一次
		ReflectionTestUtils.setField(motionService, "appVersion", "v1.2.0");
	}

	@Test
	@DisplayName("模块自检：能力清单与数据表不为空，状态文案对应当前版本")
	void moduleInfoShouldDescribeCurrentPhase() {
		var info = motionService.moduleInfo();
		assertEquals("motion", info.getModule());
		assertEquals("动效基因库", info.getName());
		assertTrue(info.getPhase().contains("v1.2.0"), "状态文案应写当前版本，而不是过程式的阶段编号：" + info.getPhase());
		assertTrue(info.getPhase().contains("已上线"));
		assertFalse(info.getCapabilities().isEmpty());
		assertEquals(List.of("motion_resource", "motion_code"), info.getPlannedTables());
	}

	@Test
	@DisplayName("新增：合法分类可以写入，并生成去重哈希")
	void createShouldInsertWithHash() {
		when(motionResourceMapper.insert(any(MotionResource.class))).thenAnswer(invocation -> {
			MotionResource arg = invocation.getArgument(0);
			arg.setId(1001L);
			return 1;
		});

		Long id = motionService.create(saveDto("Hover"));

		ArgumentCaptor<MotionResource> captor = ArgumentCaptor.forClass(MotionResource.class);
		verify(motionResourceMapper).insert(captor.capture());
		MotionResource saved = captor.getValue();
		assertEquals(1001L, id);
		assertEquals("Hover", saved.getCategory());
		assertEquals("READY", saved.getStatus(), "未传状态时默认 READY");
		assertEquals("glass,premium", saved.getTags(), "标签要去空格、去空项");
		assertNotNull(saved.getContentHash());
		assertEquals(40, saved.getContentHash().length(), "SHA-1 哈希应为 40 位");
	}

	@Test
	@DisplayName("新增：非法分类被拒绝，且不会写库")
	void createShouldRejectUnknownCategory() {
		BusinessException error = assertThrows(BusinessException.class,
				() -> motionService.create(saveDto("NotACategory")));
		assertTrue(error.getMessage().contains("分类必须是"));
		verify(motionResourceMapper, never()).insert(any(MotionResource.class));
	}

	@Test
	@DisplayName("修改：资源不存在时抛出 NOT_FOUND")
	void updateShouldFailWhenMissing() {
		when(motionResourceMapper.selectById(999L)).thenReturn(null);
		BusinessException error = assertThrows(BusinessException.class,
				() -> motionService.update(999L, saveDto("Hover")));
		assertTrue(error.getMessage().contains("不存在"));
		verify(motionResourceMapper, never()).updateById(any(MotionResource.class));
	}

	@Test
	@DisplayName("删除：资源与其代码记录一起逻辑删除")
	void deleteShouldRemoveCodeToo() {
		when(motionResourceMapper.selectById(7L)).thenReturn(sample(7L));

		motionService.delete(7L);

		verify(motionResourceMapper).deleteById(7L);
		verify(motionCodeMapper).delete(any(LambdaQueryWrapper.class));
	}

	@Test
	@DisplayName("详情：带上代码产物；没有代码时 code 为 null")
	void detailShouldMapCode() {
		when(motionResourceMapper.selectById(5L)).thenReturn(sample(5L));
		when(motionCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

		MotionDetailVO withoutCode = motionService.detail(5L);
		assertEquals("玻璃卡片悬停", withoutCode.getName());
		assertEquals(List.of("glass", "premium"), withoutCode.getTags());
		assertEquals("2026-09-25 10:30:00", withoutCode.getUpdateTime());
		assertNull(withoutCode.getCode());

		MotionCode code = new MotionCode();
		code.setMotionId(5L);
		code.setPrompt("做一个玻璃拟态悬停");
		code.setCssCode(".card:hover{transform:translateY(-6px)}");
		when(motionCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(code);

		MotionDetailVO withCode = motionService.detail(5L);
		assertNotNull(withCode.getCode());
		assertEquals("做一个玻璃拟态悬停", withCode.getCode().getPrompt());
		assertTrue(withCode.getCode().getCssCode().contains("translateY"));
	}

	@Test
	@DisplayName("保存代码：已有记录走更新，没有则插入")
	void saveCodeShouldUpsert() {
		when(motionResourceMapper.selectById(3L)).thenReturn(sample(3L));
		when(motionCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

		MotionCodeSaveDTO dto = new MotionCodeSaveDTO();
		dto.setPrompt("p");
		dto.setVueCode("<template/>");
		motionService.saveCode(3L, dto);
		verify(motionCodeMapper).insert(any(MotionCode.class));

		MotionCode existing = new MotionCode();
		existing.setId(88L);
		existing.setMotionId(3L);
		when(motionCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
		motionService.saveCode(3L, dto);
		verify(motionCodeMapper).updateById(existing);
		assertEquals("<template/>", existing.getVueCode());
	}

	@Test
	@DisplayName("列表：分页参数被收敛（页码最小 1、每页最大 100），并标记 hasCode")
	void pageShouldClampAndMarkHasCode() {
		Page<MotionResource> page = new Page<>(1, 100);
		page.setRecords(List.of(sample(1L), sample(2L)));
		page.setTotal(2);
		when(motionResourceMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

		MotionCode code = new MotionCode();
		code.setMotionId(1L);
		when(motionCodeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(code));

		MotionQueryDTO query = new MotionQueryDTO();
		query.setPage(0);
		query.setSize(9999);
		PageVO<MotionListItemVO> result = motionService.page(query);

		ArgumentCaptor<Page> captor = ArgumentCaptor.forClass(Page.class);
		verify(motionResourceMapper).selectPage(captor.capture(), any(LambdaQueryWrapper.class));
		assertEquals(1L, captor.getValue().getCurrent(), "页码小于 1 时收敛为 1");
		assertEquals(100L, captor.getValue().getSize(), "每页条数上限 100");

		assertEquals(2, result.getRecords().size());
		assertTrue(result.getRecords().get(0).getHasCode());
		assertFalse(result.getRecords().get(1).getHasCode());
		assertEquals(2L, result.getTotal());
	}

	@Test
	@DisplayName("列表：category=ALL 时不加分类条件（其余筛选照旧生效）")
	void pageShouldIgnoreAllCategory() {
		Page<MotionResource> page = new Page<>(1, 20);
		page.setRecords(List.of());
		page.setTotal(0);
		when(motionResourceMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

		MotionQueryDTO query = new MotionQueryDTO();
		query.setCategory("ALL");
		query.setKeyword("玻璃");
		motionService.page(query);

		verify(motionResourceMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
		// 没有代码记录时不应再查一次 code 表
		verify(motionCodeMapper, never()).selectList(any(LambdaQueryWrapper.class));
	}
}
