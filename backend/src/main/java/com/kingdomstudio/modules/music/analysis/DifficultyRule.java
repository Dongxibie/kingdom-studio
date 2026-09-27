package com.kingdomstudio.modules.music.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * 难度规则：给一首曲子打 1-5 颗星。
 *
 * <p>刻意做成**只看库里已有的四个字段**（音符数、时长、速度、音域跨度），不引入新的分析层：
 * 曲库列表与详情卡都要显示难度，而列表页不该为了一个星级去把音符全查出来。
 * 同一个方法两处共用，所以列表里的星级与详情卡里的星级永远一致。
 *
 * <p>和弦比例不参与星级：它是「演奏方式」的复杂度，写在分析卡里单独说明，
 * 而不是让同一首歌在两个页面上显示不同星级。
 */
public final class DifficultyRule {

	/** 每满足一条加一分，最后折算成星 */
	private static final int SPAN_ONE_STAR = 12;
	private static final int SPAN_TWO_STAR = 19;
	private static final int TEMPO_ONE_STAR = 120;
	private static final int TEMPO_TWO_STAR = 160;
	private static final double DENSITY_ONE_STAR = 2.5;
	private static final double DENSITY_TWO_STAR = 4.0;

	private DifficultyRule() {
	}

	/** 计算的输入：都是库里已有的字段 */
	public record Input(int noteCount, int durationMs, int tempoBpm, int pitchLow, int pitchHigh) {
	}

	/** 一条计分理由，用来解释「为什么是这个难度」 */
	public record Reason(String label, boolean hit) {
	}

	public record Result(int stars, String label, List<Reason> reasons) {
	}

	public static Result evaluate(Input input) {
		int span = Math.max(0, input.pitchHigh() - input.pitchLow());
		double seconds = Math.max(0.001, input.durationMs() / 1000.0);
		double density = input.noteCount() / seconds;

		List<Reason> reasons = new ArrayList<>();
		int score = 0;

		boolean wide = span >= SPAN_ONE_STAR;
		boolean veryWide = span >= SPAN_TWO_STAR;
		reasons.add(new Reason("音域跨度 " + span + " 个半音" + (veryWide ? "（超过两个八度）" : wide ? "（超过一个八度）" : ""), wide));
		score += wide ? 1 : 0;
		score += veryWide ? 1 : 0;

		boolean fast = input.tempoBpm() >= TEMPO_ONE_STAR;
		boolean veryFast = input.tempoBpm() >= TEMPO_TWO_STAR;
		reasons.add(new Reason("速度 " + input.tempoBpm() + " BPM" + (veryFast ? "（很快）" : fast ? "（偏快）" : ""), fast));
		score += fast ? 1 : 0;
		score += veryFast ? 1 : 0;

		boolean dense = density >= DENSITY_ONE_STAR;
		boolean veryDense = density >= DENSITY_TWO_STAR;
		reasons.add(new Reason(String.format("音符密度 %.1f 个/秒", density), dense));
		score += dense ? 1 : 0;
		score += veryDense ? 1 : 0;

		return new Result(starsOf(score), labelOf(starsOf(score)), reasons);
	}

	/**
	 * 分数折算星级：三条规则各能贡献 0-2 分，总共 6 分。
	 *
	 * <p>用一张明确的对照表而不是公式：改阈值时能一眼看出每档怎么变，
	 * 也不会出现「算满 6 分却到不了 5 星」这种公式与阈值对不上的情况。
	 */
	static int starsOf(int score) {
		if (score <= 0) {
			return 1;
		}
		if (score <= 2) {
			return 2;
		}
		if (score <= 4) {
			return 3;
		}
		return score == 5 ? 4 : 5;
	}

	public static String labelOf(int stars) {
		return switch (stars) {
			case 1 -> "入门";
			case 2 -> "简单";
			case 3 -> "进阶";
			case 4 -> "较难";
			default -> "挑战";
		};
	}

	/** 星级的中文说明，界面上直接显示 */
	public static String hintOf(int stars) {
		return switch (stars) {
			case 1 -> "单音为主、节奏舒缓，第一次上手就选它";
			case 2 -> "有一点跨度或速度，练两遍就能顺下来";
			case 3 -> "有连续的快速音或跨八度，建议先用简单版";
			default -> "密度与跨度都不低，建议先用简单版熟悉键位";
		};
	}
}
