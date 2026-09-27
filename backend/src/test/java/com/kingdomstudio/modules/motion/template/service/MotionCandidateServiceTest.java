package com.kingdomstudio.modules.motion.template.service;

import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.motion.template.dto.CandidatePromoteDTO;
import com.kingdomstudio.modules.motion.template.dto.CandidateReviewDTO;
import com.kingdomstudio.modules.motion.template.entity.MotionCandidate;
import com.kingdomstudio.modules.motion.template.entity.MotionTemplate;
import com.kingdomstudio.modules.motion.template.mapper.MotionCandidateMapper;
import com.kingdomstudio.modules.motion.template.mapper.MotionTemplateMapper;
import com.kingdomstudio.modules.motion.template.vo.MotionCandidateVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 候选池服务的单元测试。
 *
 * <p>重点守两条线：
 * <ol>
 *   <li>状态机不能乱：PROMOTED 只能由转换接口产生，淘汰必须写理由；</li>
 *   <li>转换只搬元数据：模板代码必须来自指定的 Pattern，候选只提供名字、来源与许可。</li>
 * </ol>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MotionCandidateServiceTest {

	@Mock
	private MotionCandidateMapper candidateMapper;
	@Mock
	private MotionTemplateMapper templateMapper;
	@Mock
	private MotionTemplateService templateService;

	private MotionCandidateService service;

	@BeforeEach
	void setUp() {
		service = new MotionCandidateService(candidateMapper, templateMapper,
				new MotionCandidateAnalyzer(), templateService);
	}

	private MotionCandidate candidate(String status) {
		MotionCandidate candidate = new MotionCandidate();
		candidate.setId(7L);
		candidate.setCandidateKey("acme__magnetic-button");
		candidate.setName("Magnetic Button Demo");
		candidate.setFullName("acme/magnetic-button");
		candidate.setSourceUrl("https://github.com/acme/magnetic-button");
		candidate.setDescription("A magnetic pull button effect");
		candidate.setLicense("MIT");
		candidate.setStatus(status);
		candidate.setStars(900);
		candidate.setCategory("按钮交互");
		return candidate;
	}

	private MotionTemplate pattern() {
		MotionTemplate template = new MotionTemplate();
		template.setTemplateKey("magnetic-button");
		template.setName("磁吸按钮");
		template.setCategory("按钮交互");
		template.setScene("Landing Page");
		template.setStyle("Luxury");
		template.setTechnology("CSS");
		template.setDifficulty(2);
		template.setTriggerType("hover");
		template.setScoreVisual(90);
		template.setScoreCode(88);
		template.setScoreReuse(90);
		template.setScorePerf(92);
		template.setScore(90);
		template.setRuntimeTier("BALANCED");
		template.setCssCode(".m-el { transform: none; }");
		template.setVueCode("<template><div class=\"m-el\"></div></template>");
		template.setPrompt("做磁吸按钮");
		template.setTags("magnetic,cta");
		template.setSource("OFFICIAL");
		template.setStatus("READY");
		return template;
	}

	@Test
	@DisplayName("淘汰必须写理由：不写就拒绝这次筛选")
	void rejectRequiresReason() {
		when(candidateMapper.selectById(7L)).thenReturn(candidate("ANALYZED"));
		CandidateReviewDTO request = new CandidateReviewDTO();
		request.setStatus("REJECTED");
		request.setNote("  ");
		BusinessException error = assertThrows(BusinessException.class, () -> service.review(7L, request));
		assertTrue(error.getMessage().contains("理由"), error.getMessage());
	}

	@Test
	@DisplayName("状态机：人工不能把候选改成「已入库」")
	void promotedIsNotReviewable() {
		when(candidateMapper.selectById(7L)).thenReturn(candidate("ANALYZED"));
		CandidateReviewDTO request = new CandidateReviewDTO();
		request.setStatus("PROMOTED");
		assertThrows(BusinessException.class, () -> service.review(7L, request));
		verify(candidateMapper, never()).updateById(any(MotionCandidate.class));
	}

	@Test
	@DisplayName("人工筛选：改成「已选入」并留下意见")
	void reviewUpdatesStatus() {
		when(candidateMapper.selectById(7L)).thenReturn(candidate("ANALYZED"));
		CandidateReviewDTO request = new CandidateReviewDTO();
		request.setStatus("selected");
		request.setNote("做法干净，值得做成 Pattern");
		MotionCandidateVO vo = service.review(7L, request);
		assertEquals("SELECTED", vo.getStatus());
		assertEquals("已选入", vo.getStatusLabel());
		assertEquals("做法干净，值得做成 Pattern", vo.getReviewNote());
	}

	@Test
	@DisplayName("已入库的候选不能再改状态")
	void promotedCandidateIsFrozen() {
		when(candidateMapper.selectById(7L)).thenReturn(candidate("PROMOTED"));
		CandidateReviewDTO request = new CandidateReviewDTO();
		request.setStatus("REJECTED");
		request.setNote("反悔了");
		assertThrows(BusinessException.class, () -> service.review(7L, request));
	}

	@Test
	@DisplayName("转换：代码来自 Pattern，候选只贡献名字、来源与许可")
	void promoteCopiesFromPatternAndKeepsAttribution() {
		when(candidateMapper.selectById(7L)).thenReturn(candidate("SELECTED"));
		when(templateService.require("magnetic-button")).thenReturn(pattern());
		when(templateMapper.selectOne(any())).thenReturn(null);
		CandidatePromoteDTO request = new CandidatePromoteDTO();
		request.setPatternTemplateKey("magnetic-button");

		Map<String, Object> result = service.promote(7L, request);

		ArgumentCaptor<MotionTemplate> saved = ArgumentCaptor.forClass(MotionTemplate.class);
		verify(templateMapper).insert(saved.capture());
		MotionTemplate inserted = saved.getValue();
		assertEquals("community-acme-magnetic-button", inserted.getTemplateKey());
		assertEquals("COMMUNITY", inserted.getSource());
		assertEquals("https://github.com/acme/magnetic-button", inserted.getSourceUrl());
		assertEquals("MIT", inserted.getSourceLicense());
		// 代码与参数一律来自 Pattern，而不是候选
		assertEquals(".m-el { transform: none; }", inserted.getCssCode());
		assertEquals("hover", inserted.getTriggerType());
		assertEquals("BALANCED", inserted.getRuntimeTier());
		assertTrue(inserted.getTags().contains("community"));
		// 返回里带上入库结果
		assertEquals("community-acme-magnetic-button", result.get("templateKey"));
		assertEquals(true, result.get("created"));
	}

	@Test
	@DisplayName("转换是幂等的：已入库的候选直接返回原模板，不重复建")
	void promoteIsIdempotent() {
		MotionCandidate promoted = candidate("PROMOTED");
		promoted.setPromotedTemplateKey("community-acme-magnetic-button");
		promoted.setPatternKey("magnetic-button");
		when(candidateMapper.selectById(7L)).thenReturn(promoted);
		MotionTemplate existing = pattern();
		existing.setTemplateKey("community-acme-magnetic-button");
		when(templateService.require("community-acme-magnetic-button")).thenReturn(existing);

		CandidatePromoteDTO request = new CandidatePromoteDTO();
		request.setPatternTemplateKey("magnetic-button");
		Map<String, Object> result = service.promote(7L, request);

		assertEquals(false, result.get("created"));
		verify(templateMapper, never()).insert(any(MotionTemplate.class));
	}

	@Test
	@DisplayName("转换必须指定一个真实存在的 Pattern")
	void promoteRequiresExistingPattern() {
		when(candidateMapper.selectById(7L)).thenReturn(candidate("SELECTED"));
		when(templateService.require("not-exists"))
				.thenThrow(new BusinessException(com.kingdomstudio.common.ResultCode.NOT_FOUND, "模板不存在"));
		CandidatePromoteDTO request = new CandidatePromoteDTO();
		request.setPatternTemplateKey("not-exists");
		assertThrows(BusinessException.class, () -> service.promote(7L, request));
		verify(templateMapper, never()).insert(any(MotionTemplate.class));
	}

	@Test
	@DisplayName("入库模板 key 由候选标识派生：斜杠换短横线，长度截到列宽以内")
	void templateKeyIsDerivedAndBounded() {
		MotionCandidate longName = candidate("SELECTED");
		longName.setCandidateKey("someone-with-a-very-long-name__a-repo-with-an-extremely-long-name-as-well");
		String key = service.templateKeyOf(longName);
		assertTrue(key.startsWith("community-someone-with-a-very-long-name-a-repo"));
		assertTrue(key.length() <= 64, "模板 key 超长：" + key.length());
		assertFalse(key.endsWith("-"), "结尾不应该留下短横线：" + key);
	}
}
