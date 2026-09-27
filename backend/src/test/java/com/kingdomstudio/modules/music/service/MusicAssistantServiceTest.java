package com.kingdomstudio.modules.music.service;

import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.music.assistant.MusicModelClient;
import com.kingdomstudio.modules.music.dto.MusicPromptDTO;
import com.kingdomstudio.modules.music.dto.StrategyApplyDTO;
import com.kingdomstudio.modules.music.entity.MusicTask;
import com.kingdomstudio.modules.music.mapper.MusicTaskMapper;
import com.kingdomstudio.modules.music.parser.ParsedSong;
import com.kingdomstudio.modules.music.strategy.StrategyApplier;
import com.kingdomstudio.modules.music.vo.MusicAssistantVO;
import com.kingdomstudio.modules.music.vo.MusicTaskDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 音乐助手的单元测试。
 *
 * <p>三条线：规则判断要认得出 spec 里那三句原话；模型给什么都不能直接执行（必须过白名单校验）；
 * 应用方案要落成一条新曲目而不是覆盖原曲。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MusicAssistantServiceTest {

	@Mock
	private MusicTaskMapper musicTaskMapper;
	@Mock
	private MusicTaskService musicTaskService;
	@Mock
	private MusicModelClient modelClient;

	private MusicAssistantService service;

	@BeforeEach
	void setUp() {
		service = new MusicAssistantService(musicTaskMapper, musicTaskService, new StrategyApplier(), modelClient);
		when(modelClient.enabled()).thenReturn(false);
		when(modelClient.modelName()).thenReturn("deepseek-chat");
		when(musicTaskService.songOf(1L)).thenReturn(song());
		MusicTask task = new MusicTask();
		task.setId(1L);
		task.setName("小星星");
		when(musicTaskMapper.selectById(1L)).thenReturn(task);
	}

	private ParsedSong song() {
		return new ParsedSong("小星星", "MIDI", "twinkle.mid", 120, "4/4", 4000, List.of(
				new ParsedSong.Note(60, 90, 0, 500, 0),
				new ParsedSong.Note(62, 90, 500, 500, 0),
				new ParsedSong.Note(64, 90, 1000, 1500, 0)));
	}

	private MusicPromptDTO prompt(String text) {
		MusicPromptDTO dto = new MusicPromptDTO();
		dto.setPrompt(text);
		return dto;
	}

	@Test
	@DisplayName("「让它听起来简单一点」→ 初学模式（规则判断）")
	void understandsSimple() {
		MusicAssistantVO vo = service.analyze(1L, prompt("让它听起来简单一点"));
		assertEquals("RULE", vo.getSource());
		assertEquals("BEGINNER", vo.getIntent().getDifficulty());
		assertEquals("初学模式", vo.getIntent().getDifficultyLabel());
		assertTrue(vo.getFallbackReason().contains("未配置模型"));
		assertTrue(vo.getExplanation().startsWith("初学模式："));
		assertFalse(vo.getAdjustments().isEmpty());
		assertTrue(vo.getPreview().getTempoAfter() < vo.getPreview().getTempoBefore());
	}

	@Test
	@DisplayName("「做一个适合游戏展示的版本」→ 展示模式，且不动旋律")
	void understandsShowcase() {
		MusicAssistantVO vo = service.analyze(1L, prompt("做一个适合游戏展示的版本"));
		assertEquals("SHOWCASE", vo.getIntent().getDifficulty());
		assertEquals(vo.getPreview().getNoteCountBefore(), vo.getPreview().getNoteCountAfter());
		assertTrue(vo.getExplanation().contains("旋律与音符数量保持原样"));
	}

	@Test
	@DisplayName("「帮我把这首歌变成适合15键口风琴演奏」→ 识别到目标乐器")
	void understandsInstrument() {
		MusicAssistantVO vo = service.analyze(1L, prompt("帮我把这首歌变成适合15键口风琴演奏"));
		assertEquals("15键", vo.getIntent().getInstrument());
		assertNotNull(vo.getIntent().getSuggestions());
		assertFalse(vo.getIntent().getSuggestions().isEmpty());
	}

	@Test
	@DisplayName("没提要求 → 标准模式，建议里说明原曲即可")
	void defaultIsNormal() {
		MusicAssistantVO vo = service.analyze(1L, prompt("这首歌怎么样"));
		assertEquals("NORMAL", vo.getIntent().getDifficulty());
		assertTrue(vo.getAdjustments().isEmpty());
		assertTrue(vo.getExplanation().contains("保持原曲"));
	}

	@Test
	@DisplayName("模型给了合法难度：来源标成 MODEL，难度与建议都采用")
	void usesModelWhenValid() {
		when(modelClient.enabled()).thenReturn(true);
		when(modelClient.analyze(anyString(), anyString())).thenReturn(
				new MusicModelClient.Answer("BEGINNER", "15-key kalimba", List.of("降低速度", "调整八度")));
		MusicAssistantVO vo = service.analyze(1L, prompt("帮我改成适合上台的版本"));
		assertEquals("MODEL", vo.getSource());
		assertEquals("deepseek-chat", vo.getModelName());
		assertEquals("BEGINNER", vo.getIntent().getDifficulty());
		assertEquals("15-key kalimba", vo.getIntent().getInstrument());
		assertEquals(2, vo.getIntent().getSuggestions().size());
		assertNull(vo.getFallbackReason());
	}

	@Test
	@DisplayName("模型给了认不出的难度：不采信，回退规则判断并说明原因")
	void rejectsUnknownDifficultyFromModel() {
		when(modelClient.enabled()).thenReturn(true);
		when(modelClient.analyze(anyString(), anyString())).thenReturn(
				new MusicModelClient.Answer("SUPER_HARD", null, List.of("随便加点东西")));
		MusicAssistantVO vo = service.analyze(1L, prompt("让这首歌更适合舞台"));
		assertEquals("RULE", vo.getSource(), "认不出的难度档不能被当成策略执行");
		assertEquals("SHOWCASE", vo.getIntent().getDifficulty(), "应当由规则判断补上");
		assertTrue(vo.getFallbackReason().contains("无法识别"), vo.getFallbackReason());
	}

	@Test
	@DisplayName("模型调用失败：回退规则判断，助手照常可用")
	void fallsBackWhenModelFails() {
		when(modelClient.enabled()).thenReturn(true);
		when(modelClient.analyze(anyString(), anyString())).thenReturn(null);
		MusicAssistantVO vo = service.analyze(1L, prompt("简单一点"));
		assertEquals("RULE", vo.getSource());
		assertEquals("BEGINNER", vo.getIntent().getDifficulty());
		assertTrue(vo.getFallbackReason().contains("没有返回可用结果"));
	}

	@Test
	@DisplayName("模型建议一律截断：最多 5 条、每条最多 60 字")
	void suggestionsAreSanitized() {
		List<String> raw = List.of("一".repeat(120), "第二条", "第三条", "第四条", "第五条", "第六条", "第七条");
		List<String> cleaned = service.sanitizeSuggestions(raw);
		assertEquals(5, cleaned.size(), "最多留 5 条");
		assertEquals(60, cleaned.get(0).length(), "超长建议要截断");
		assertTrue(service.sanitizeSuggestions(List.of("  ", "")).isEmpty());
	}

	@Test
	@DisplayName("应用方案：落成一条新曲目，名字带方案名，原曲不动")
	void applyCreatesDerivedTask() {
		MusicTaskDetailVO created = new MusicTaskDetailVO();
		created.setId(9L);
		created.setName("小星星（初学模式）");
		when(musicTaskService.createDerived(any(ParsedSong.class), anyString())).thenReturn(created);

		StrategyApplyDTO request = new StrategyApplyDTO();
		request.setStrategy("beginner");
		MusicAssistantVO vo = service.apply(1L, request);

		assertEquals(9L, vo.getTaskId());
		assertEquals("小星星（初学模式）", vo.getTaskName());
		verify(musicTaskService).createDerived(any(ParsedSong.class), anyString());
	}

	@Test
	@DisplayName("应用方案是幂等的：同名版本已存在就复用，不重复建")
	void applyIsIdempotent() {
		MusicTask existing = new MusicTask();
		existing.setId(21L);
		existing.setName("小星星（初学模式）");
		when(musicTaskMapper.selectOne(any())).thenReturn(existing);
		// 幂等键 = 派生名 + 源任务 id（见 MusicAssistantService#apply）

		StrategyApplyDTO request = new StrategyApplyDTO();
		request.setStrategy("BEGINNER");
		MusicAssistantVO vo = service.apply(1L, request);

		assertEquals(21L, vo.getTaskId(), "应复用已存在的版本");
		verify(musicTaskService, never()).createDerived(any(ParsedSong.class), anyString());
	}

	@Test
	@DisplayName("应用方案：不认识的策略直接拒绝，不做任何写操作")
	void applyRejectsUnknownStrategy() {
		StrategyApplyDTO request = new StrategyApplyDTO();
		request.setStrategy("TURBO");
		BusinessException error = assertThrows(BusinessException.class, () -> service.apply(1L, request));
		assertTrue(error.getMessage().contains("BEGINNER"), error.getMessage());
		verify(musicTaskService, never()).createDerived(any(ParsedSong.class), anyString());
	}

	@Test
	@DisplayName("应用标准模式：没有可改的地方就说明白，不生成重复曲目")
	void applyNormalIsRefusedPolitely() {
		StrategyApplyDTO request = new StrategyApplyDTO();
		request.setStrategy("NORMAL");
		BusinessException error = assertThrows(BusinessException.class, () -> service.apply(1L, request));
		assertTrue(error.getMessage().contains("标准模式就是原曲本身"), error.getMessage());
		verify(musicTaskService, never()).createDerived(any(ParsedSong.class), anyString());
	}
}
