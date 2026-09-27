package com.kingdomstudio.modules.music.analysis;

import com.kingdomstudio.modules.music.entity.InstrumentProfile;
import com.kingdomstudio.modules.music.service.MusicTaskService;
import com.kingdomstudio.modules.music.mapper.InstrumentProfileMapper;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 乐器匹配的单元测试。
 *
 * <p>匹配的全部价值在于「挑得准不准」：能全落下的优先、键数接近目标优先、
 * 指定了游戏就把该游戏的档案排前面、没有该游戏的档案时退回通用并说明。
 * 这些判断都会直接影响用户点「生成方案」之后拿到什么，所以逐条钉住。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProfileMatchServiceTest {

	@Mock
	private MusicTaskService musicTaskService;
	@Mock
	private InstrumentProfileMapper instrumentProfileMapper;

	private ProfileMatchService service;

	@BeforeEach
	void setUp() {
		service = new ProfileMatchService(musicTaskService, instrumentProfileMapper);
		when(instrumentProfileMapper.selectList(any())).thenReturn(List.of(
				profile(1L, "光遇式 15 键", "手机游戏虚拟乐器（两排十五键）", "光遇 Sky", 15),
				profile(2L, "八音盒 8 键", "单八度小乐器", "通用", 8),
				profile(3L, "Minecraft 音符盒 25 键", "音符盒（Note Block）", "Minecraft", 25)));
		// 试算结果：15 键与 25 键能全落下，8 键漏 2 个音
		when(musicTaskService.mapKeys(anyLong(), any())).thenAnswer(invocation -> {
			var request = invocation.getArgument(1, com.kingdomstudio.modules.music.dto.MappingRequestDTO.class);
			long profileId = request.getProfileId();
			if (profileId == 2L) {
				return sequence(2L, 8, 12, 2);
			}
			if (profileId == 3L) {
				return sequence(3L, 25, 14, 0);
			}
			return sequence(1L, 15, 14, 0);
		});
	}

	private InstrumentProfile profile(long id, String name, String instrument, String game, int keys) {
		InstrumentProfile profile = new InstrumentProfile();
		profile.setId(id);
		profile.setName(name);
		profile.setInstrument(instrument);
		profile.setGame(game);
		profile.setKeyLayout(String.join(",", java.util.Collections.nCopies(keys, "K")));
		return profile;
	}

	private KeySequenceVO sequence(long profileId, int keyCount, int mapped, int unmapped) {
		KeySequenceVO vo = new KeySequenceVO();
		vo.setTaskId(7L);
		vo.setProfileId(profileId);
		vo.setKeyCount(keyCount);
		vo.setNoteCount(mapped + unmapped);
		vo.setMappedCount(mapped);
		vo.setUnmappedCount(unmapped);
		vo.setStrokes(List.of());
		vo.setUnmapped(List.of());
		return vo;
	}

	@Test
	@DisplayName("默认排序：能全落下的排前面，键数少的更靠前（8 键漏音所以垫底）")
	void shouldRankByCoverageThenKeys() {
		List<ProfileMatchService.Candidate> ranked = service.rank(7L, null, null, null);

		assertEquals(3, ranked.size());
		assertEquals("光遇式 15 键", ranked.get(0).profile().getName(), "全落下 + 15 键优于 25 键");
		assertEquals("Minecraft 音符盒 25 键", ranked.get(1).profile().getName());
		assertEquals("八音盒 8 键", ranked.get(2).profile().getName(), "漏 2 个音所以垫底");
		assertTrue(ranked.get(2).sequence().getUnmappedCount() > 0);
	}

	@Test
	@DisplayName("指定游戏：该游戏的档案排到第一，即使键数不是最少")
	void shouldPrioritizeGame() {
		List<ProfileMatchService.Candidate> ranked = service.rank(7L, "Minecraft", null, null);

		assertEquals("Minecraft 音符盒 25 键", ranked.get(0).profile().getName());
		assertTrue(ranked.get(0).reasons().stream().anyMatch(reason -> reason.contains("Minecraft")),
				ranked.get(0).reasons().toString());
	}

	@Test
	@DisplayName("指定键数：键数接近的排前面")
	void shouldPreferClosestKeyCount() {
		List<ProfileMatchService.Candidate> ranked = service.rank(7L, null, null, 15);

		assertEquals("光遇式 15 键", ranked.get(0).profile().getName());
		assertTrue(ranked.get(0).reasons().stream().anyMatch(reason -> reason.contains("正好是你要的 15 键")),
				ranked.get(0).reasons().toString());
	}

	@Test
	@DisplayName("指定乐器：按乐器名匹配，命中会拿到加分与理由")
	void shouldPreferInstrument() {
		ProfileMatchService.Candidate candidate = service.score(
				profile(4L, "三角洲行动 口风琴", "口风琴（15 键）", "三角洲行动", 15),
				sequence(4L, 15, 14, 0), null, "口风琴", null);

		assertTrue(candidate.reasons().stream().anyMatch(reason -> reason.contains("口风琴")),
				candidate.reasons().toString());
	}

	@Test
	@DisplayName("没有该游戏的档案：退回通用档案并在理由里说明，而不是返回空")
	void shouldFallBackToGeneric() {
		List<ProfileMatchService.Candidate> ranked = service.rank(7L, "三角洲行动", null, null);

		assertEquals(3, ranked.size(), "库里没有这个游戏也要给出可用档案");
		assertTrue(ranked.stream().anyMatch(candidate ->
						candidate.reasons().stream().anyMatch(reason -> reason.contains("通用键位"))),
				"通用档案要在理由里说明「没有该游戏的专用档案」");
	}

	@Test
	@DisplayName("理由逐条可读：能落键 / 键数 / 游戏 / 乐器最多各一条")
	void shouldExplainEveryCandidate() {
		ProfileMatchService.Candidate candidate = service.score(
				profile(1L, "光遇式 15 键", "手机游戏虚拟乐器", "光遇 Sky", 15),
				sequence(1L, 15, 14, 0), "光遇", "虚拟乐器", 15);

		assertEquals(4, candidate.reasons().size(), candidate.reasons().toString());
		assertTrue(candidate.coversAll());
	}

	@Test
	@DisplayName("top：只要前 N 个，N 大于总数时也不越界")
	void shouldLimitResults() {
		assertEquals(2, service.top(7L, null, null, null, 2).size());
		assertEquals(3, service.top(7L, null, null, null, 10).size());
	}
}
