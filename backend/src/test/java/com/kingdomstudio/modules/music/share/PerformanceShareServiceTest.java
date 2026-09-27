package com.kingdomstudio.modules.music.share;

import com.kingdomstudio.modules.music.entity.InstrumentProfile;
import com.kingdomstudio.modules.music.entity.MusicNote;
import com.kingdomstudio.modules.music.entity.MusicTask;
import com.kingdomstudio.modules.music.mapper.InstrumentProfileMapper;
import com.kingdomstudio.modules.music.mapper.MusicNoteMapper;
import com.kingdomstudio.modules.music.parser.ParsedSong;
import com.kingdomstudio.modules.music.service.MusicTaskService;
import com.kingdomstudio.modules.music.service.PerformanceMacroService;
import com.kingdomstudio.modules.music.service.PerformancePresetService;
import com.kingdomstudio.modules.music.share.dto.PerformanceShareDTO;
import com.kingdomstudio.modules.music.share.vo.PerformanceShareVO;
import com.kingdomstudio.modules.music.vo.MusicTaskDetailVO;
import com.kingdomstudio.modules.music.vo.PerformancePlanVO;
import com.kingdomstudio.modules.music.vo.PerformancePresetVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 曲谱分享的单元测试。
 *
 * <p>分享这件事能成立的前提是「快照自包含」：别人的库里没有这首曲子，也要能完整还原。
 * 所以这里重点验三件事 —— 快照带齐了音符与事件、导入真的造出一首新曲子、
 * 抄错的码与空快照都要明确报错而不是静默成功。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PerformanceShareServiceTest {

	@Mock
	private PerformanceShareMapper shareMapper;
	@Mock
	private MusicNoteMapper musicNoteMapper;
	@Mock
	private MusicTaskService musicTaskService;
	@Mock
	private PerformancePresetService presetService;
	@Mock
	private PerformanceMacroService macroService;
	@Mock
	private InstrumentProfileMapper instrumentProfileMapper;

	private PerformanceShareService service;
	private final List<PerformanceShare> stored = new ArrayList<>();

	@BeforeEach
	void setUp() {
		stored.clear();
		service = new PerformanceShareService(shareMapper, musicNoteMapper, musicTaskService, presetService,
				macroService, instrumentProfileMapper);

		MusicTask task = new MusicTask();
		task.setId(7L);
		task.setName("小星星");
		task.setSourceType("JIANPU");
		task.setSourceRef("简谱输入");
		task.setNoteCount(14);
		task.setTempoBpm(96);
		task.setTimeSignature("4/4");
		task.setDurationMs(10_000);
		task.setPitchLow(60);
		task.setPitchHigh(69);
		when(musicTaskService.require(7L)).thenReturn(task);
		when(musicTaskService.require(99L)).thenThrow(new RuntimeException("曲目不存在（id=99）"));

		when(musicNoteMapper.selectList(any())).thenReturn(notes());
		when(presetService.resolve(anyLong(), any())).thenReturn(
				new PerformancePresetService.Resolution(null, 1.0, 0, null));
		when(macroService.latest(anyLong())).thenReturn(plan());

		InstrumentProfile profile = new InstrumentProfile();
		profile.setId(1L);
		profile.setName("光遇式 15 键");
		profile.setGame("光遇 Sky");
		when(instrumentProfileMapper.selectById(anyLong())).thenReturn(profile);

		when(shareMapper.insert(any(PerformanceShare.class))).thenAnswer(invocation -> {
			PerformanceShare entity = invocation.getArgument(0);
			entity.setId((long) (stored.size() + 1));
			entity.setCreateTime(java.time.LocalDateTime.now());
			stored.add(entity);
			return 1;
		});
		when(shareMapper.updateById(any(PerformanceShare.class))).thenReturn(1);
		// selectList 有两种用法：按分享码查单条（要求说明里带 NOT NULL 条件）与列全部。
		// 这里用「SQL 里出现的码」来区分，兼顾两种用法。
		when(shareMapper.selectList(any())).thenAnswer(invocation -> {
			Object wrapper = invocation.getArgument(0);
			String sql = String.valueOf(wrapper);
			List<PerformanceShare> matched = stored.stream()
					.filter(item -> item.getShareCode() != null && sql.contains(item.getShareCode()))
					.toList();
			return matched.isEmpty() ? new ArrayList<>(stored) : matched;
		});
	}

	private List<MusicNote> notes() {
		List<MusicNote> result = new ArrayList<>();
		int[] pitches = {60, 60, 67, 67, 69, 69, 67};
		int[] starts = {0, 800, 1600, 2400, 3200, 4000, 4800};
		for (int index = 0; index < pitches.length; index++) {
			MusicNote note = new MusicNote();
			note.setTaskId(7L);
			note.setSeqNo(index + 1);
			note.setPitch(pitches[index]);
			note.setNoteName("C4");
			note.setStartMs(starts[index]);
			note.setDurationMs(700);
			note.setVelocity(90);
			note.setTrackNo(0);
			result.add(note);
		}
		return result;
	}

	private PerformancePlanVO plan() {
		List<PerformancePlanVO.Note> events = new ArrayList<>();
		for (int index = 0; index < 14; index++) {
			PerformancePlanVO.Note event = new PerformancePlanVO.Note();
			event.setKey("Z");
			event.setAction(index % 2 == 0 ? "DOWN" : "UP");
			event.setTimestamp(index * 400);
			events.add(event);
		}
		return PerformancePlanVO.builder()
				.id(12L).taskId(7L).taskName("小星星").profileName("光遇式 15 键")
				.duration(10_000).noteCount(14).strokeCount(7).keyCount(3)
				.notes(events).warnings(List.of())
				.build();
	}

	@Test
	@DisplayName("生成演奏码：编号有序，难度与乐器写进记录")
	void shouldCreateShare() {
		PerformanceShareVO vo = service.create(7L, new PerformanceShareDTO());

		assertEquals("KS-MUSIC-" + java.time.LocalDate.now().getYear() + "-A001", vo.getShareCode());
		assertEquals("小星星", vo.getTitle());
		assertEquals("匿名", vo.getCreator());
		assertEquals("1 星 · 入门（简单）", vo.getDifficulty());
		assertEquals(14, vo.getNoteCount(), "音符数取自曲目本身");
		assertEquals(10_000, vo.getDurationMs());
		assertEquals("光遇式 15 键", vo.getInstrument());
		assertTrue(vo.getHighlights().stream().anyMatch(item -> item.contains("按键动作")), vo.getHighlights().toString());
		assertFalse("PENDING".equals(stored.get(0).getShareCode()), "落库后要把占位码换成正式码");
	}

	@Test
	@DisplayName("快照自包含：带曲目信息、音符与导出事件")
	void shouldBuildSelfContainedSnapshot() {
		service.create(7L, new PerformanceShareDTO());
		String payload = stored.get(0).getPayload();

		assertTrue(payload.contains("\"taskName\":\"小星星\""), payload);
		assertTrue(payload.contains("\"notes\""), payload);
		assertTrue(payload.contains("\"startMs\":800"), payload);
		assertTrue(payload.contains("\"events\""), payload);
		assertTrue(payload.contains("\"stars\":1"), payload);
	}

	@Test
	@DisplayName("自定义署名与标题：署名留空时记为匿名")
	void shouldUseProvidedCreator() {
		PerformanceShareDTO request = new PerformanceShareDTO();
		request.setCreator("  泽龙  ");
		request.setTitle("  小星星（新手版） ");

		PerformanceShareVO vo = service.create(7L, request);

		assertEquals("泽龙", vo.getCreator());
		assertEquals("小星星（新手版）", vo.getTitle());
	}

	@Test
	@DisplayName("没有演奏计划也能分享：快照里不带事件，但曲谱照样能还原")
	void shouldShareWithoutPlan() {
		when(macroService.latest(anyLong())).thenThrow(new RuntimeException("还没有生成过演奏计划"));

		PerformanceShareVO vo = service.create(7L, new PerformanceShareDTO());

		assertNotNull(vo.getShareCode());
		assertTrue(stored.get(0).getPayload().contains("\"notes\""));
		assertFalse(stored.get(0).getPayload().contains("\"events\""));
	}

	@Test
	@DisplayName("导入演奏码：造出一首新曲目，音符数与来源信息完整")
	void shouldImportShare() {
		PerformanceShareVO created = service.create(7L, new PerformanceShareDTO());
		ArgumentCaptor<ParsedSong> captor = ArgumentCaptor.forClass(ParsedSong.class);
		when(musicTaskService.createDerived(captor.capture(), anyString()))
				.thenReturn(MusicTaskDetailVO.builder().id(88L).name("小星星（分享）").build());

		PerformanceShareVO imported = service.importShare(created.getShareCode());

		ParsedSong song = captor.getValue();
		assertEquals(7, song.notes().size());
		assertEquals(60, song.notes().get(0).pitch());
		assertEquals(800, song.notes().get(1).startMs());
		assertEquals(5_500, song.durationMs(), "时长按最后一个音的结束时刻算（4800 + 700）");
		assertEquals(96, song.tempoBpm());
		assertEquals("SHARE", song.sourceType());
		assertTrue(song.sourceRef().contains(created.getShareCode()));
		assertEquals(88L, imported.getImportedTaskId());
		assertTrue(imported.getMessage().contains("已导入"), imported.getMessage());
		assertEquals(1, stored.get(0).getImportCount());
	}

	@Test
	@DisplayName("只抄后半段也能导入：a001 会自动补全年份与前缀")
	void shouldAcceptShorthandCode() {
		PerformanceShareVO created = service.create(7L, new PerformanceShareDTO());
		when(musicTaskService.createDerived(any(), anyString()))
				.thenReturn(MusicTaskDetailVO.builder().id(91L).name("小星星（分享）").build());

		String shorthand = created.getShareCode().substring(created.getShareCode().length() - 4);
		PerformanceShareVO imported = service.importShare(shorthand);

		assertEquals(91L, imported.getImportedTaskId());
	}

	@Test
	@DisplayName("抄错的码：明确报「没有找到这个演奏码」，并给出格式示例")
	void shouldRejectUnknownCode() {
		var error = assertThrows(RuntimeException.class, () -> service.importShare("KS-MUSIC-2026-Z999"));
		assertTrue(error.getMessage().contains("没有找到这个演奏码"), error.getMessage());
		assertTrue(error.getMessage().contains("KS-MUSIC-2026-A001"), error.getMessage());
	}

	@Test
	@DisplayName("空码：直接提示「请输入演奏码」")
	void shouldRejectBlankCode() {
		var error = assertThrows(RuntimeException.class, () -> service.describe("   "));
		assertTrue(error.getMessage().contains("请输入演奏码"), error.getMessage());
	}

	@Test
	@DisplayName("快照里没有音符：导入时明确报错，而不是造出一首空曲子")
	void shouldRejectEmptySnapshot() {
		stored.add(share("KS-MUSIC-2026-C001", "{\"version\":1,\"taskName\":\"空\",\"notes\":[]}"));

		var error = assertThrows(RuntimeException.class, () -> service.importShare("KS-MUSIC-2026-C001"));
		assertTrue(error.getMessage().contains("没有音符数据"), error.getMessage());
	}

	@Test
	@DisplayName("查看演奏码：给出摘要与按键动作数")
	void shouldDescribeShare() {
		PerformanceShareVO created = service.create(7L, new PerformanceShareDTO());

		PerformanceShareVO described = service.describe(created.getShareCode().toLowerCase());

		assertEquals(created.getShareCode(), described.getShareCode());
		assertTrue(described.getHighlights().stream().anyMatch(item -> item.contains("14 条按键动作")),
				described.getHighlights().toString());
	}

	@Test
	@DisplayName("我的分享：按时间倒序返回，带创作时间")
	void shouldListShares() {
		service.create(7L, new PerformanceShareDTO());
		service.create(7L, new PerformanceShareDTO());

		List<PerformanceShareVO> list = service.list();

		assertEquals(2, list.size());
		assertEquals("KS-MUSIC-" + java.time.LocalDate.now().getYear() + "-A001", list.get(0).getShareCode());
		assertNotNull(list.get(0).getCreateTime());
	}

	@Test
	@DisplayName("分享记录带上游戏名：从乐器档案里读，方便按游戏检索")
	void shouldRecordGame() {
		PerformancePresetVO preset = PerformancePresetVO.builder()
				.id(3L).name("简单版").profileId(1L).profileName("光遇式 15 键")
				.keyCount(15).speedScale(1.0).minGapMs(0).build();
		when(presetService.resolve(anyLong(), any())).thenReturn(
				new PerformancePresetService.Resolution(null, 1.0, 0, preset));

		PerformanceShareVO vo = service.create(7L, new PerformanceShareDTO());

		assertEquals("光遇 Sky", vo.getGame());
	}

	private PerformanceShare share(String code, String payload) {
		PerformanceShare entity = new PerformanceShare();
		entity.setId((long) (stored.size() + 1));
		entity.setShareCode(code);
		entity.setTitle("测试");
		entity.setCreator("匿名");
		entity.setPayload(payload);
		entity.setImportCount(0);
		return entity;
	}
}
