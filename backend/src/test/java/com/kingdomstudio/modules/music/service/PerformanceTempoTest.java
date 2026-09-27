package com.kingdomstudio.modules.music.service;

import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 速度变换与最小间隔的单元测试。
 *
 * <p>这两个变换是「快速版方案」与「连按冲突」的落地点：做错了要么速度没变，
 * 要么把原始序列改坏（调用方还在用）。所以既要验数值，也要验「不改传入对象」。
 */
class PerformanceTempoTest {

	private KeySequenceVO.Stroke stroke(int seq, int startMs, int durationMs, String... keys) {
		KeySequenceVO.Stroke stroke = new KeySequenceVO.Stroke();
		stroke.setSeq(seq);
		stroke.setStartMs(startMs);
		stroke.setDurationMs(durationMs);
		stroke.setKeys(List.of(keys));
		stroke.setNoteNames(List.of(keys));
		return stroke;
	}

	private KeySequenceVO sequence(KeySequenceVO.Stroke... strokes) {
		KeySequenceVO vo = new KeySequenceVO();
		vo.setTaskId(1L);
		vo.setProfileId(1L);
		vo.setKeyCount(15);
		vo.setStrokes(new java.util.ArrayList<>(List.of(strokes)));
		vo.setUnmapped(List.of());
		vo.setExportText("原文");
		return vo;
	}

	@Test
	@DisplayName("速度 1.35 倍：起点与按住时长一起缩短，顺序不变")
	void shouldScaleFaster() {
		KeySequenceVO source = sequence(
				stroke(1, 0, 400, "Z"),
				stroke(2, 1000, 200, "X"));

		KeySequenceVO fast = PerformanceTempo.scale(source, 1.35);

		assertEquals(0, fast.getStrokes().get(0).getStartMs());
		assertEquals(296, fast.getStrokes().get(0).getDurationMs());
		assertEquals(741, fast.getStrokes().get(1).getStartMs());
		assertEquals(148, fast.getStrokes().get(1).getDurationMs());
		assertEquals(2, fast.getStrokes().size());
	}

	@Test
	@DisplayName("速度 0.8 倍：整体变慢，结束时刻跟着变")
	void shouldScaleSlower() {
		KeySequenceVO source = sequence(stroke(1, 0, 500, "Z"), stroke(2, 1000, 500, "X"));

		KeySequenceVO slow = PerformanceTempo.scale(source, 0.8);

		assertEquals(1250, slow.getStrokes().get(1).getStartMs());
		assertEquals(625, slow.getStrokes().get(1).getDurationMs());
		assertEquals(1875, PerformanceTempo.durationOf(slow));
	}

	@Test
	@DisplayName("倍率 1.0：原样返回，不做无意义的拷贝")
	void shouldKeepOriginalWhenScaleIsOne() {
		KeySequenceVO source = sequence(stroke(1, 0, 100, "Z"));

		assertSame(source, PerformanceTempo.scale(source, 1.0));
		assertSame(source, PerformanceTempo.applyMinGap(source, 0));
	}

	@Test
	@DisplayName("不改传入的序列：变换结果在新对象上")
	void shouldNotMutateSource() {
		KeySequenceVO source = sequence(stroke(1, 0, 400, "Z"), stroke(2, 1000, 200, "X"));

		KeySequenceVO fast = PerformanceTempo.scale(source, 2.0);

		assertEquals(1000, source.getStrokes().get(1).getStartMs(), "原对象没被动过");
		assertEquals(500, fast.getStrokes().get(1).getStartMs());
		assertTrue(fast.getStrokes() != source.getStrokes());
	}

	@Test
	@DisplayName("最小间隔：同键连按被推开，后面的组跟着顺延")
	void shouldApplyMinGap() {
		KeySequenceVO source = sequence(
				stroke(1, 0, 30, "Z"),
				stroke(2, 40, 30, "Z"),
				stroke(3, 1000, 30, "X"));

		KeySequenceVO gap = PerformanceTempo.applyMinGap(source, 60);

		assertEquals(0, gap.getStrokes().get(0).getStartMs());
		assertEquals(90, gap.getStrokes().get(1).getStartMs(), "松开在 30ms，再留 60ms → 90ms 才按第二次");
		assertEquals(1050, gap.getStrokes().get(2).getStartMs(), "后面的组顺延同样多的 50ms");
	}

	@Test
	@DisplayName("最小间隔：上一组还没松开时，先等它松开再留出间隔")
	void shouldWaitForRelease() {
		KeySequenceVO source = sequence(
				stroke(1, 0, 300, "Z"),
				stroke(2, 100, 100, "Z"));

		KeySequenceVO gap = PerformanceTempo.applyMinGap(source, 60);

		assertEquals(360, gap.getStrokes().get(1).getStartMs(), "300ms 松开 + 60ms 间隔");
		assertTrue(gap.getStrokes().get(1).getStartMs() >= 300, "两次按下不重叠");
	}

	@Test
	@DisplayName("最小间隔只管同一个键：不同键的紧密演奏不受影响")
	void shouldOnlyAffectSameKey() {
		KeySequenceVO source = sequence(
				stroke(1, 0, 60, "Z"),
				stroke(2, 30, 60, "X"),
				stroke(3, 60, 60, "C"));

		KeySequenceVO gap = PerformanceTempo.applyMinGap(source, 60);

		assertEquals(30, gap.getStrokes().get(1).getStartMs());
		assertEquals(60, gap.getStrokes().get(2).getStartMs());
	}
}
