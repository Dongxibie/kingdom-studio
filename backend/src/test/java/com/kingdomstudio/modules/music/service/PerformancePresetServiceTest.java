package com.kingdomstudio.modules.music.service;

import com.kingdomstudio.modules.music.dto.PerformancePresetDTO;
import com.kingdomstudio.modules.music.entity.InstrumentProfile;
import com.kingdomstudio.modules.music.entity.MusicPerformancePreset;
import com.kingdomstudio.modules.music.entity.MusicTask;
import com.kingdomstudio.modules.music.mapper.InstrumentProfileMapper;
import com.kingdomstudio.modules.music.mapper.MusicPerformancePresetMapper;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 演奏方案的单元测试。
 *
 * <p>内置的 A/B/C 三套方案是「按库里的乐器档案现算」的，所以这里重点验三件事：
 * 生成的方案符合定义（原版 / 简单版 / 快速版）、只生成一次、以及选项越界时明确报错。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PerformancePresetServiceTest {

	@Mock
	private MusicPerformancePresetMapper presetMapper;
	@Mock
	private InstrumentProfileMapper instrumentProfileMapper;
	@Mock
	private MusicTaskService musicTaskService;

	private PerformancePresetService service;
	private final List<MusicPerformancePreset> stored = new ArrayList<>();

	@BeforeEach
	void setUp() {
		stored.clear();
		service = new PerformancePresetService(presetMapper, instrumentProfileMapper, musicTaskService);
		MusicTask task = new MusicTask();
		task.setId(7L);
		task.setName("小星星");
		when(musicTaskService.require(7L)).thenReturn(task);

		when(instrumentProfileMapper.selectList(any())).thenReturn(List.of(
				profile(1L, "光遇式 15 键", 15),
				profile(2L, "八音盒 8 键", 8)));
		when(instrumentProfileMapper.selectById(1L)).thenReturn(profile(1L, "光遇式 15 键", 15));
		when(instrumentProfileMapper.selectById(2L)).thenReturn(profile(2L, "八音盒 8 键", 8));

		// 内存版「库」：insert 收下，selectList 还回去
		when(presetMapper.insert(any(MusicPerformancePreset.class))).thenAnswer(invocation -> {
			MusicPerformancePreset entity = invocation.getArgument(0);
			entity.setId((long) (stored.size() + 1));
			stored.add(entity);
			return 1;
		});
		// selectList 在业务里有两处用处：列方案（按 taskId）与查同名（按 taskId + name）。
		// 这里按调用顺序的语义返回：默认返回全部，调用方自己再做业务判断。
		when(presetMapper.selectList(any())).thenAnswer(invocation -> new ArrayList<>(stored));
		when(presetMapper.selectById(any())).thenAnswer(invocation -> {
			Long id = invocation.getArgument(0);
			return stored.stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
		});
		when(presetMapper.updateById(any(MusicPerformancePreset.class))).thenReturn(1);
		when(presetMapper.deleteById(anyLong())).thenReturn(1);
	}

	private InstrumentProfile profile(long id, String name, int keys) {
		InstrumentProfile profile = new InstrumentProfile();
		profile.setId(id);
		profile.setName(name);
		profile.setInstrument("口风琴");
		profile.setKeyLayout(String.join(",", java.util.Collections.nCopies(keys, "K")));
		profile.setUnmappedStrategy("SKIP");
		return profile;
	}

	private PerformancePresetDTO dto(String name, Long profileId, Double speed, Integer gap) {
		PerformancePresetDTO request = new PerformancePresetDTO();
		request.setName(name);
		request.setProfileId(profileId);
		request.setSpeedScale(speed);
		request.setMinGapMs(gap);
		return request;
	}

	@Test
	@DisplayName("首次访问生成三套内置方案：原版 / 简单版 / 快速版")
	void shouldCreateBuiltinPresets() {
		List<PerformancePresetVO> presets = service.list(7L);

		assertEquals(3, presets.size());
		assertEquals(List.of("原版", "简单版", "快速版"), presets.stream().map(PerformancePresetVO::getName).toList());

		PerformancePresetVO original = presets.get(0);
		assertEquals(1.0, original.getSpeedScale());
		assertEquals("原速", original.getSpeedText());
		assertTrue(original.getBuiltin());

		PerformancePresetVO simple = presets.get(1);
		assertEquals(2L, simple.getProfileId(), "简单版用键数最少的档案");
		assertEquals(8, simple.getKeyCount());
		assertEquals("SHIFT_OCTAVE", simple.getStrategy());

		PerformancePresetVO fast = presets.get(2);
		assertEquals(1.35, fast.getSpeedScale());
		assertEquals("快 35%", fast.getSpeedText());
	}

	@Test
	@DisplayName("第二次访问不再重复生成：内置方案只插一次")
	void shouldNotDuplicateBuiltinPresets() {
		service.list(7L);
		service.list(7L);

		ArgumentCaptor<MusicPerformancePreset> captor = ArgumentCaptor.forClass(MusicPerformancePreset.class);
		verify(presetMapper, times(3)).insert(captor.capture());
		assertEquals(3, captor.getAllValues().size());
	}

	@Test
	@DisplayName("自定义方案：建档并回填档案名与键数")
	void shouldCreateCustomPreset() {
		PerformancePresetVO created = service.create(7L, dto("练习版", 2L, 0.85, 60));

		assertEquals("练习版", created.getName());
		assertEquals("八音盒 8 键", created.getProfileName());
		assertEquals(8, created.getKeyCount());
		assertEquals(0.85, created.getSpeedScale());
		assertEquals("慢 15%", created.getSpeedText());
		assertEquals(60, created.getMinGapMs());
		assertEquals(false, created.getBuiltin());
	}

	@Test
	@DisplayName("同名方案不允许重复：直接报「已经有同名方案」")
	void shouldRejectDuplicateName() {
		service.list(7L);

		var error = assertThrows(RuntimeException.class, () -> service.create(7L, dto("原版", 1L, 1.0, 0)));
		assertTrue(error.getMessage().contains("已经有一个叫「原版」的方案"), error.getMessage());
	}

	@Test
	@DisplayName("策略只接受三个合法值，乱填明确报错")
	void shouldRejectUnknownStrategy() {
		PerformancePresetDTO request = dto("怪异版", 1L, 1.0, 0);
		request.setStrategy("TELEPORT");

		var error = assertThrows(RuntimeException.class, () -> service.create(7L, request));
		assertTrue(error.getMessage().contains("不支持的策略"), error.getMessage());
	}

	@Test
	@DisplayName("解析方案：映射请求 + 速度 + 间隔；不传方案则是原速无间隔")
	void shouldResolvePreset() {
		PerformancePresetVO created = service.create(7L, dto("快速练习", 2L, 1.5, 60));

		PerformancePresetService.Resolution resolution = service.resolve(7L, created.getId());

		assertEquals(2L, resolution.mapping().getProfileId());
		assertEquals(1.5, resolution.speedScale());
		assertEquals(60, resolution.minGapMs());
		assertEquals("快速练习", resolution.preset().getName());

		PerformancePresetService.Resolution plain = service.resolve(7L, null);
		assertNull(plain.mapping());
		assertEquals(1.0, plain.speedScale());
		assertEquals(0, plain.minGapMs());
	}

	@Test
	@DisplayName("方案与曲目必须匹配：拿别的曲目的方案来用会被拒")
	void shouldRejectPresetOfAnotherTask() {
		PerformancePresetVO created = service.create(7L, dto("练习版", 1L, 1.0, 0));

		var error = assertThrows(RuntimeException.class, () -> service.resolve(99L, created.getId()));
		assertTrue(error.getMessage().contains("方案与曲目不匹配"), error.getMessage());
	}

	@Test
	@DisplayName("修改方案：内置方案改完成自己的（builtin 归零）")
	void shouldUpdatePreset() {
		service.list(7L);
		Long builtinId = stored.get(1).getId();

		PerformancePresetVO updated = service.update(builtinId, dto("简单版（改）", 2L, 1.2, 80));

		assertEquals("简单版（改）", updated.getName());
		assertEquals(80, updated.getMinGapMs());
		assertEquals(false, updated.getBuiltin(), "改过之后就不再是内置的");
	}

	@Test
	@DisplayName("删除方案：调一次逻辑删除；不存在则报 404")
	void shouldDeletePreset() {
		PerformancePresetVO created = service.create(7L, dto("练习版", 1L, 1.0, 0));

		service.delete(created.getId());
		verify(presetMapper, times(1)).deleteById(created.getId());

		var error = assertThrows(RuntimeException.class, () -> service.delete(999L));
		assertTrue(error.getMessage().contains("演奏方案不存在"), error.getMessage());
	}

	@Test
	@DisplayName("库里没有乐器档案时：不生成内置方案，也不报错")
	void shouldSkipBuiltinWithoutProfiles() {
		when(instrumentProfileMapper.selectList(any())).thenReturn(List.of());

		assertTrue(service.list(7L).isEmpty());
		verify(presetMapper, never()).insert(any(MusicPerformancePreset.class));
	}

	@Test
	@DisplayName("速度文案：原速 / 快 20% / 慢 25%")
	void shouldFormatSpeedText() {
		assertEquals("原速", PerformancePresetService.speedText(1.0));
		assertEquals("快 20%", PerformancePresetService.speedText(1.2));
		assertEquals("慢 25%", PerformancePresetService.speedText(0.75));
	}

	@Test
	@DisplayName("数值落库前规整：速度两位小数、策略转大写")
	void shouldNormalizeValues() {
		PerformancePresetDTO request = dto("规整版", 1L, 1.234, 40);
		request.setStrategy("shift_octave");

		PerformancePresetVO created = service.create(7L, request);

		assertEquals(new BigDecimal("1.23"), BigDecimal.valueOf(created.getSpeedScale()).setScale(2));
		assertEquals("SHIFT_OCTAVE", created.getStrategy());
	}
}
