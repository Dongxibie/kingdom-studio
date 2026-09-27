package com.kingdomstudio.modules.music.analysis;

import com.kingdomstudio.modules.music.entity.InstrumentProfile;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import com.kingdomstudio.modules.music.vo.OptimizationVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 演奏优化器的单元测试。
 *
 * <p>优化器的价值全在「说得准不准」：不该报的别报（狼来了没人看），该报的必须报，
 * 而且每条都要给出能直接应用的参数。所以五条检查各写一个正例与一个反例。
 */
class PerformanceOptimizerServiceTest {

	// analyze() 是纯函数，不碰这三个依赖；构造器要它们只是为了 optimize() 那条路径
	private final PerformanceOptimizerService service = new PerformanceOptimizerService(
			org.mockito.Mockito.mock(com.kingdomstudio.modules.music.service.MusicTaskService.class),
			org.mockito.Mockito.mock(com.kingdomstudio.modules.music.mapper.InstrumentProfileMapper.class),
			org.mockito.Mockito.mock(com.kingdomstudio.modules.music.service.PerformancePresetService.class));

	private KeySequenceVO.Stroke stroke(int seq, int startMs, int durationMs, String... keys) {
		KeySequenceVO.Stroke stroke = new KeySequenceVO.Stroke();
		stroke.setSeq(seq);
		stroke.setStartMs(startMs);
		stroke.setDurationMs(durationMs);
		stroke.setKeys(List.of(keys));
		stroke.setNoteNames(List.of(keys));
		stroke.setChord(keys.length > 1);
		stroke.setAdjusted(false);
		return stroke;
	}

	private KeySequenceVO.Unmapped unmapped(int pitch, String name) {
		KeySequenceVO.Unmapped item = new KeySequenceVO.Unmapped();
		item.setPitch(pitch);
		item.setNoteName(name);
		item.setStartMs(1000);
		item.setReason("超出音域");
		return item;
	}

	private KeySequenceVO sequence(long profileId, List<KeySequenceVO.Stroke> strokes, List<KeySequenceVO.Unmapped> unmapped) {
		KeySequenceVO vo = new KeySequenceVO();
		vo.setTaskId(1L);
		vo.setTaskName("测试曲目");
		vo.setProfileId(profileId);
		vo.setProfileName("光遇式 15 键");
		vo.setKeyCount(15);
		vo.setStrokes(strokes);
		vo.setUnmapped(unmapped);
		vo.setUnmappedCount(unmapped.size());
		vo.setMappedCount(strokes.size());
		return vo;
	}

	private InstrumentProfile profile(long id, String name, int keys) {
		InstrumentProfile profile = new InstrumentProfile();
		profile.setId(id);
		profile.setName(name);
		profile.setKeyLayout(String.join(",", java.util.Collections.nCopies(keys, "K")));
		return profile;
	}

	private OptimizationVO.Finding find(OptimizationVO report, String code) {
		return report.getFindings().stream().filter(item -> code.equals(item.getCode())).findFirst().orElse(null);
	}

	@Test
	@DisplayName("同键重按太密：松开后不到 60ms 就再按会被报出来，并给出 60ms 的最小间隔")
	void shouldDetectRepeatConflict() {
		KeySequenceVO sequence = sequence(1L, List.of(
				stroke(1, 0, 80, "Z"),
				stroke(2, 100, 80, "Z"),
				stroke(3, 900, 80, "X")), List.of());

		OptimizationVO report = service.analyze(sequence, List.of(profile(1L, "光遇式 15 键", 15)));
		OptimizationVO.Finding finding = find(report, "REPEAT_CONFLICT");

		assertNotNull(finding);
		assertEquals("WARN", finding.getSeverity());
		assertEquals(1, finding.getAffected());
		assertEquals(60, finding.getFix().getMinGapMs());
		assertTrue(finding.getSuggestion().contains("60ms"), finding.getSuggestion());
		assertTrue(report.getSummary().contains("1 处"), report.getSummary());
	}

	@Test
	@DisplayName("同键重按间隔足够：不报这条，也不该有别的 WARN")
	void shouldNotReportCleanRepeat() {
		KeySequenceVO sequence = sequence(1L, List.of(
				stroke(1, 0, 80, "Z"),
				stroke(2, 500, 80, "Z"),
				stroke(3, 1000, 80, "X")), List.of());

		OptimizationVO report = service.analyze(sequence, List.of(profile(1L, "光遇式 15 键", 15)));

		assertNull(find(report, "REPEAT_CONFLICT"));
		assertTrue(report.getFindings().isEmpty());
		assertTrue(report.getSummary().contains("可以直接照着弹"), report.getSummary());
	}

	@Test
	@DisplayName("上一组还没松开就再按同一个键：算冲突（间隔按 0 计）")
	void shouldDetectOverlap() {
		KeySequenceVO sequence = sequence(1L, List.of(
				stroke(1, 0, 300, "Z"),
				stroke(2, 100, 300, "Z")), List.of());

		OptimizationVO report = service.analyze(sequence, List.of(profile(1L, "光遇式 15 键", 15)));
		OptimizationVO.Finding finding = find(report, "REPEAT_CONFLICT");

		assertNotNull(finding);
		assertEquals(60, finding.getFix().getMinGapMs());
		assertEquals(1, finding.getAffected());
	}

	@Test
	@DisplayName("超出音域：报未落键并建议移八度，同时给出更宽的档案")
	void shouldSuggestOctaveShift() {
		KeySequenceVO sequence = sequence(1L, List.of(stroke(1, 0, 100, "Z")),
				List.of(unmapped(84, "C6"), unmapped(86, "D6")));

		OptimizationVO report = service.analyze(sequence, List.of(
				profile(1L, "八音盒 8 键", 8), profile(2L, "光遇式 15 键", 15)));
		OptimizationVO.Finding finding = find(report, "OUT_OF_RANGE");

		assertNotNull(finding);
		assertEquals("WARN", finding.getSeverity());
		assertEquals(2, finding.getAffected());
		assertEquals("SHIFT_OCTAVE", finding.getFix().getStrategy());
		assertEquals(2L, finding.getFix().getProfileId(), "推荐键更多的那个档案");
		assertTrue(finding.getSuggestion().contains("移八度"), finding.getSuggestion());
	}

	@Test
	@DisplayName("没有未落键：不报超出音域")
	void shouldNotReportOutOfRangeWhenAllMapped() {
		KeySequenceVO sequence = sequence(1L, List.of(stroke(1, 0, 100, "Z")), List.of());

		OptimizationVO report = service.analyze(sequence, List.of(profile(1L, "光遇式 15 键", 15)));

		assertNull(find(report, "OUT_OF_RANGE"));
	}

	@Test
	@DisplayName("和弦：同时按三个以上键时提示，并点名更宽的档案")
	void shouldDetectChord() {
		KeySequenceVO sequence = sequence(1L, List.of(
				stroke(1, 0, 200, "Z", "X", "C"),
				stroke(2, 400, 200, "V")), List.of());

		OptimizationVO report = service.analyze(sequence, List.of(
				profile(1L, "八音盒 8 键", 8), profile(2L, "键盘式 12 键", 12)));
		OptimizationVO.Finding finding = find(report, "CHORD");

		assertNotNull(finding);
		assertEquals("ADVICE", finding.getSeverity());
		assertEquals(1, finding.getAffected());
		assertEquals(2L, finding.getFix().getProfileId());
		assertTrue(finding.getDetail().contains("3 个"), finding.getDetail());
	}

	@Test
	@DisplayName("密度过高：最密一秒超过 7 组就建议降速，倍率按实际密度算")
	void shouldSuggestSlowerTempo() {
		List<KeySequenceVO.Stroke> strokes = new ArrayList<>();
		// 每 80ms 一组、只按 20ms：松开后还有 60ms 空档，所以只该报密度这一条
		for (int index = 0; index < 12; index++) {
			strokes.add(stroke(index + 1, index * 80, 20, "Z"));
		}
		KeySequenceVO sequence = sequence(1L, strokes, List.of());

		OptimizationVO report = service.analyze(sequence, List.of(profile(1L, "光遇式 15 键", 15)));
		OptimizationVO.Finding density = find(report, "DENSE");

		assertNotNull(density);
		assertEquals("ADVICE", density.getSeverity());
		assertEquals(12, density.getAffected());
		assertNotNull(density.getFix().getSpeedScale());
		assertTrue(density.getFix().getSpeedScale() < 1.0, "降速");
		assertNull(find(report, "REPEAT_CONFLICT"), "松开后有 60ms 空档，不该误报连按冲突");
	}

	@Test
	@DisplayName("长按：超过 1.5 秒的按住只提示、不当成问题")
	void shouldNoticeLongHold() {
		KeySequenceVO sequence = sequence(1L, List.of(
				stroke(1, 0, 2200, "Z"),
				stroke(2, 3000, 100, "X")), List.of());

		OptimizationVO report = service.analyze(sequence, List.of(profile(1L, "光遇式 15 键", 15)));
		OptimizationVO.Finding finding = find(report, "LONG_HOLD");

		assertNotNull(finding);
		assertEquals("INFO", finding.getSeverity());
		assertNull(finding.getFix(), "长按没有可直接改的参数");
		assertTrue(report.getSummary().contains("可以演奏"), report.getSummary());
	}

	@Test
	@DisplayName("warnings 排序：WARN 在最前，其次 ADVICE，最后 INFO")
	void shouldSortFindingsBySeverity() {
		List<KeySequenceVO.Stroke> strokes = new ArrayList<>();
		strokes.add(stroke(1, 0, 2200, "Z"));
		strokes.add(stroke(2, 100, 60, "Z", "X", "C"));
		KeySequenceVO sequence = sequence(1L, strokes, List.of(unmapped(90, "F#6")));

		OptimizationVO report = service.analyze(sequence, List.of(profile(1L, "八音盒 8 键", 8)));
		List<String> severities = report.getFindings().stream().map(OptimizationVO.Finding::getSeverity).toList();

		assertEquals("WARN", severities.get(0), severities.toString());
		assertTrue(severities.indexOf("ADVICE") < severities.indexOf("INFO") || !severities.contains("INFO"),
				severities.toString());
	}

	@Test
	@DisplayName("空计划：给出「先做一次映射」的结论，而不是一堆空建议")
	void shouldHandleEmptyPlan() {
		OptimizationVO report = service.analyze(sequence(1L, List.of(), List.of()), List.of());

		assertEquals(0, report.getStrokeCount());
		assertTrue(report.getFindings().isEmpty());
		assertTrue(report.getSummary().contains("先选一个乐器档案"), report.getSummary());
	}
}
