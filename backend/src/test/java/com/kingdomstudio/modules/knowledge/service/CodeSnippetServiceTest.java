package com.kingdomstudio.modules.knowledge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kingdomstudio.common.PageVO;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.knowledge.dto.CodeSnippetQueryDTO;
import com.kingdomstudio.modules.knowledge.dto.CodeSnippetSaveDTO;
import com.kingdomstudio.modules.knowledge.entity.CodeSnippet;
import com.kingdomstudio.modules.knowledge.mapper.CodeSnippetMapper;
import com.kingdomstudio.modules.knowledge.vo.CodeSnippetDetailVO;
import com.kingdomstudio.modules.knowledge.vo.CodeSnippetVO;
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

/** CodeSnippetService 单元测试：列表不带正文、行数计算与维护 */
@ExtendWith(MockitoExtension.class)
class CodeSnippetServiceTest {

	@Mock
	private CodeSnippetMapper codeSnippetMapper;

	@InjectMocks
	private CodeSnippetService codeSnippetService;

	@Test
	@DisplayName("列表：只带摘要与行数，不带代码正文")
	void pageShouldNotCarryCodeContent() {
		CodeSnippet row = snippet(1L, "Stream 分组统计", "Java", "a\nb\nc");
		stubPage(row);

		PageVO<CodeSnippetVO> page = codeSnippetService.page(new CodeSnippetQueryDTO());

		assertEquals(1, page.getTotal());
		assertEquals(3, page.getRecords().get(0).getLineCount());
		assertEquals(List.of("Stream"), page.getRecords().get(0).getTags());
	}

	@Test
	@DisplayName("详情：带完整代码正文")
	void detailShouldCarryCodeContent() {
		when(codeSnippetMapper.selectById(2L)).thenReturn(snippet(2L, "防抖函数", "JavaScript", "function debounce() {}"));

		CodeSnippetDetailVO detail = codeSnippetService.detail(2L);

		assertTrue(detail.getCodeContent().contains("debounce"));
		assertEquals(1, detail.getLineCount());
	}

	@Test
	@DisplayName("行数：空内容算 0 行，末尾换行不重复计数")
	void lineCountShouldHandleEdgeCases() {
		assertEquals(0, CodeSnippetService.lineCount(null));
		assertEquals(0, CodeSnippetService.lineCount("   "));
		assertEquals(1, CodeSnippetService.lineCount("select 1;"));
		assertEquals(2, CodeSnippetService.lineCount("select 1;\n"));
		assertEquals(3, CodeSnippetService.lineCount("a\nb\nc"));
	}

	@Test
	@DisplayName("新增：同名片段直接拒绝")
	void createShouldRejectDuplicatedTitle() {
		when(codeSnippetMapper.selectCount(any())).thenReturn(1L);
		CodeSnippetSaveDTO request = new CodeSnippetSaveDTO();
		request.setTitle("MySQL 幂等 ALTER 模板");
		request.setLanguage("SQL");
		request.setCodeContent("select 1;");

		BusinessException error = assertThrows(BusinessException.class, () -> codeSnippetService.create(request));

		assertTrue(error.getMessage().contains("已存在同名片段"));
		verify(codeSnippetMapper, never()).insert(any(CodeSnippet.class));
	}

	@Test
	@DisplayName("新增：描述与标签缺省为空串")
	void createShouldApplyDefaults() {
		when(codeSnippetMapper.selectCount(any())).thenReturn(0L);
		CodeSnippetSaveDTO request = new CodeSnippetSaveDTO();
		request.setTitle("分页插件依赖");
		request.setLanguage("Java");
		request.setCodeContent("// pom");

		codeSnippetService.create(request);

		ArgumentCaptor<CodeSnippet> captor = ArgumentCaptor.forClass(CodeSnippet.class);
		verify(codeSnippetMapper).insert(captor.capture());
		assertEquals("", captor.getValue().getDescription());
		assertEquals("", captor.getValue().getTags());
		assertEquals("Java", captor.getValue().getLanguage());
	}

	@Test
	@DisplayName("修改与删除：都先确认存在，不存在抛 404")
	void updateAndDeleteShouldRequireExistingRow() {
		when(codeSnippetMapper.selectById(33L)).thenReturn(null);
		CodeSnippetSaveDTO request = new CodeSnippetSaveDTO();
		request.setTitle("任意");
		request.setLanguage("SQL");
		request.setCodeContent("select 1;");

		assertThrows(BusinessException.class, () -> codeSnippetService.update(33L, request));
		assertThrows(BusinessException.class, () -> codeSnippetService.delete(33L));
	}

	@Test
	@DisplayName("标签拆分：空值返回空数组，多余空格去掉")
	void splitTagsShouldTrim() {
		assertTrue(CodeSnippetService.splitTags(null).isEmpty());
		assertEquals(List.of("MySQL", "幂等"), CodeSnippetService.splitTags(" MySQL , 幂等 , "));
	}

	private CodeSnippet snippet(Long id, String title, String language, String code) {
		CodeSnippet row = new CodeSnippet();
		row.setId(id);
		row.setTitle(title);
		row.setLanguage(language);
		row.setDescription("说明");
		row.setCodeContent(code);
		row.setTags("Stream");
		return row;
	}

	@SuppressWarnings("unchecked")
	private void stubPage(CodeSnippet... rows) {
		when(codeSnippetMapper.selectPage(any(), any())).thenAnswer(invocation -> {
			Page<CodeSnippet> page = invocation.getArgument(0);
			page.setRecords(rows == null ? List.of() : List.of(rows));
			page.setTotal(rows == null ? 0 : rows.length);
			return page;
		});
	}
}
