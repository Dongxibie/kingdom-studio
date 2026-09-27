package com.kingdomstudio.modules.music.analysis;

import com.kingdomstudio.modules.music.dto.MappingRequestDTO;
import com.kingdomstudio.modules.music.entity.InstrumentProfile;
import com.kingdomstudio.modules.music.mapper.InstrumentProfileMapper;
import com.kingdomstudio.modules.music.service.MusicTaskService;
import com.kingdomstudio.modules.music.service.PerformancePresetService;
import com.kingdomstudio.modules.music.service.PerformanceTempo;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import com.kingdomstudio.modules.music.vo.OptimizationVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 演奏优化器：读一遍按键序列，指出会被「吞键、超出音域、按不过来」的地方。
 *
 * <p>五条检查都是有明确后果的，不做玄学评分：
 * <ol>
 *   <li><b>同键连按太密</b>：两次按下挨得太近，在某些输入路径上会被当成重复而丢掉 —— 建议加最小间隔；</li>
 *   <li><b>超出音域</b>：有音根本没落键 —— 建议自动八度调整（或换音域更宽的档案）；</li>
 *   <li><b>和弦</b>：同时按三个以上键，多数小乐器按不出来 —— 提示并给替代档案；</li>
 *   <li><b>密度过高</b>：某一秒里按键组太多，手速跟不上 —— 建议降速到某个倍率；</li>
 *   <li><b>单次按住过久</b>：超过 1.5 秒的按住，部分乐器不支持 —— 提示。</li>
 * </ol>
 *
 * <p>每条建议都带 {@code fix}：就是这套方案里能直接改的旋钮，前端一键应用即可。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceOptimizerService {

	/** 同键两次按下之间建议至少留这么多毫秒 */
	static final int SUGGESTED_GAP_MS = 60;

	/** 单次按住超过这个时长就提示 */
	static final int LONG_HOLD_MS = 1_500;

	/** 一秒内的按键组数超过这个值就算密 */
	static final int DENSE_STROKES_PER_SECOND = 7;

	/** 和弦键数上限：超过就提醒 */
	static final int CHORD_KEY_LIMIT = 3;

	private final MusicTaskService musicTaskService;
	private final InstrumentProfileMapper instrumentProfileMapper;
	private final PerformancePresetService presetService;

	/** 优化报告：按当前乐器档案与策略试算一遍再检查 */
	public OptimizationVO optimize(Long taskId, Long profileId, String strategy) {
		return optimize(taskId, profileId, strategy, null);
	}

	/**
	 * 优化报告（带方案）。
	 *
	 * <p>带方案时先按方案把序列调好（速度倍率 + 同键最小间隔）再检查 ——
	 * 检查的对象必须是**实际会执行的按键序列**。否则应用了「间隔 60ms」之后，
	 * 报告还会照着原始序列报同一处冲突，用户会以为建议没生效。
	 */
	public OptimizationVO optimize(Long taskId, Long profileId, String strategy, Long presetId) {
		MappingRequestDTO request = new MappingRequestDTO();
		request.setProfileId(profileId);
		request.setStrategy(strategy);
		double speedScale = 1.0;
		int minGapMs = 0;
		if (presetId != null) {
			PerformancePresetService.Resolution resolution = presetService.resolve(taskId, presetId);
			if (resolution.mapping() != null) {
				request = resolution.mapping();
			}
			speedScale = resolution.speedScale();
			minGapMs = resolution.minGapMs();
		}
		KeySequenceVO sequence = musicTaskService.mapKeys(taskId, request);
		sequence = PerformanceTempo.applyMinGap(PerformanceTempo.scale(sequence, speedScale), minGapMs);
		OptimizationVO report = analyze(sequence, instrumentProfileMapper.selectList(new LambdaQueryWrapper<>()));
		if (minGapMs > 0) {
			report.setSummary(report.getSummary() + "（已按当前方案留出 " + minGapMs + "ms 同键间隔后重新检查）");
		}
		return report;
	}

	/** 纯分析：序列 + 可选档案 → 报告（单独抽出来便于单测） */
	public OptimizationVO analyze(KeySequenceVO sequence, List<InstrumentProfile> profiles) {
		List<KeySequenceVO.Stroke> strokes = sequence.getStrokes() == null ? List.of() : sequence.getStrokes();
		List<OptimizationVO.Finding> findings = new ArrayList<>();

		findings.addAll(checkRepeatConflict(strokes));
		findings.addAll(checkOutOfRange(sequence, profiles));
		findings.addAll(checkChord(sequence, strokes, profiles));
		findings.addAll(checkDensity(strokes));
		findings.addAll(checkLongHold(strokes));

		// WARN 在前，其次 ADVICE，最后 INFO
		findings.sort(Comparator.comparingInt((OptimizationVO.Finding item) -> weight(item.getSeverity()))
				.thenComparing(OptimizationVO.Finding::getCode));

		return OptimizationVO.builder()
				.taskId(sequence.getTaskId())
				.taskName(sequence.getTaskName())
				.strokeCount(strokes.size())
				.eventCount(strokes.size() * 2)
				.summary(summary(findings, strokes.size()))
				.findings(findings)
				.build();
	}

	/** 同键连按太密：两张相邻的组里出现同一个键，且间隔小于建议值 */
	private List<OptimizationVO.Finding> checkRepeatConflict(List<KeySequenceVO.Stroke> strokes) {
		Map<String, Integer> lastEnd = new HashMap<>();
		int affected = 0;
		int worstGap = Integer.MAX_VALUE;
		for (KeySequenceVO.Stroke stroke : strokes) {
			int start = value(stroke.getStartMs());
			for (String key : keysOf(stroke)) {
				Integer previousEnd = lastEnd.get(key);
				if (previousEnd == null) {
					continue;
				}
				// 判定口径：上一次「松开」到这一次「按下」之间的空档。
				// 空档不足 60ms（含「还没松开就又按」这种 0 空档）都算冲突 ——
				// 太紧的连按在部分输入路径上会被当成重复而丢掉。
				int gap = start - previousEnd;
				if (gap < SUGGESTED_GAP_MS) {
					affected++;
					worstGap = Math.min(worstGap, Math.max(0, gap));
				}
			}
			int hold = value(stroke.getDurationMs());
			for (String key : keysOf(stroke)) {
				lastEnd.put(key, start + hold);
			}
		}
		List<OptimizationVO.Finding> findings = new ArrayList<>();
		if (affected > 0) {
			findings.add(OptimizationVO.Finding.builder()
					.code("REPEAT_CONFLICT")
					.severity("WARN")
					.title("同键重按太密")
					.detail("有 " + affected + " 处同一个键在上一次松开后 " + Math.max(0, worstGap) + "ms 就又被按下")
					.suggestion("把这些按键往后挪，让同一个键「松开到再按下」之间至少留 " + SUGGESTED_GAP_MS + "ms，"
							+ "避免输入路径把第二次按下当成重复而丢掉")
					.affected(affected)
					.fix(OptimizationVO.Fix.builder().minGapMs(SUGGESTED_GAP_MS).build())
					.build());
		}
		return findings;
	}

	/** 超出音域：有音没落键 → 建议八度调整，或换键数更多的档案 */
	private List<OptimizationVO.Finding> checkOutOfRange(KeySequenceVO sequence, List<InstrumentProfile> profiles) {
		List<OptimizationVO.Finding> findings = new ArrayList<>();
		int unmapped = sequence.getUnmapped() == null ? 0 : sequence.getUnmapped().size();
		if (unmapped == 0) {
			return findings;
		}
		List<String> names = (sequence.getUnmapped() == null ? List.<KeySequenceVO.Unmapped>of() : sequence.getUnmapped())
				.stream().map(KeySequenceVO.Unmapped::getNoteName).distinct().limit(4).toList();
		InstrumentProfile wider = widestProfile(profiles, sequence);
		findings.add(OptimizationVO.Finding.builder()
				.code("OUT_OF_RANGE")
				.severity("WARN")
				.title("有音超出当前音域")
				.detail("未落键 " + unmapped + " 个音（例如 " + String.join("、", names) + "）")
				.suggestion(wider != null
						? "把这些音自动移八度落键（策略改为移八度），或换成「" + wider.getName() + "」"
						: "把这些音自动移八度落键（策略改为移八度）")
				.affected(unmapped)
				.fix(OptimizationVO.Fix.builder()
						.strategy("SHIFT_OCTAVE")
						.profileId(wider == null ? null : wider.getId())
						.profileName(wider == null ? null : wider.getName())
						.build())
				.build());
		return findings;
	}

	/** 和弦：同时按三个以上的键 */
	private List<OptimizationVO.Finding> checkChord(KeySequenceVO sequence, List<KeySequenceVO.Stroke> strokes,
			List<InstrumentProfile> profiles) {
		int chords = (int) strokes.stream().filter(stroke -> keysOf(stroke).size() >= CHORD_KEY_LIMIT).count();
		List<OptimizationVO.Finding> findings = new ArrayList<>();
		if (chords == 0) {
			return findings;
		}
		InstrumentProfile wider = widestProfile(profiles, sequence);
		findings.add(OptimizationVO.Finding.builder()
				.code("CHORD")
				.severity("ADVICE")
				.title("有需要同时按多个键的和弦")
				.detail("有 " + chords + " 处需要一次按下 " + CHORD_KEY_LIMIT + " 个以上的键")
				.suggestion(wider != null
						? "用小乐器演奏时这几处容易漏音，可以考虑换成「" + wider.getName() + "」"
						: "小乐器演奏这几处容易漏音，可以先在练习模式里单独过一遍")
				.affected(chords)
				.fix(wider == null ? null : OptimizationVO.Fix.builder()
						.profileId(wider.getId()).profileName(wider.getName()).build())
				.build());
		return findings;
	}

	/** 密度：某一秒内按键组过多 */
	private List<OptimizationVO.Finding> checkDensity(List<KeySequenceVO.Stroke> strokes) {
		Map<Integer, Integer> perSecond = new HashMap<>();
		for (KeySequenceVO.Stroke stroke : strokes) {
			int second = value(stroke.getStartMs()) / 1000;
			perSecond.merge(second, 1, Integer::sum);
		}
		int loudest = perSecond.values().stream().max(Integer::compareTo).orElse(0);
		List<OptimizationVO.Finding> findings = new ArrayList<>();
		if (loudest <= DENSE_STROKES_PER_SECOND) {
			return findings;
		}
		double scale = Math.max(0.6, Math.round(DENSE_STROKES_PER_SECOND * 100.0 / loudest) / 100.0);
		findings.add(OptimizationVO.Finding.builder()
				.code("DENSE")
				.severity("ADVICE")
				.title("某一段按键过于密集")
				.detail("最密的一秒里有 " + loudest + " 组按键")
				.suggestion("建议把速度调到 " + scale + " 倍（或改用简单版方案）再练，避免手速跟不上")
				.affected(loudest)
				.fix(OptimizationVO.Fix.builder().speedScale(scale).build())
				.build());
		return findings;
	}

	/** 长按：单次按住过久 */
	private List<OptimizationVO.Finding> checkLongHold(List<KeySequenceVO.Stroke> strokes) {
		List<KeySequenceVO.Stroke> longs = strokes.stream()
				.filter(stroke -> value(stroke.getDurationMs()) > LONG_HOLD_MS).toList();
		List<OptimizationVO.Finding> findings = new ArrayList<>();
		if (longs.isEmpty()) {
			return findings;
		}
		findings.add(OptimizationVO.Finding.builder()
				.code("LONG_HOLD")
				.severity("INFO")
				.title("有需要长按的音")
				.detail("有 " + longs.size() + " 处按住超过 " + LONG_HOLD_MS + "ms")
				.suggestion("部分乐器不支持长按，实际演奏时这几处按到最短时长即可，观感差别很小")
				.affected(longs.size())
				.build());
		return findings;
	}

	/** 音域最宽的档案（键最多），用来兜住超范围与和弦 */
	private InstrumentProfile widestProfile(List<InstrumentProfile> profiles, KeySequenceVO sequence) {
		return profiles.stream()
				.filter(profile -> sequence.getProfileId() == null || !profile.getId().equals(sequence.getProfileId()))
				.max(Comparator.comparingInt(profile -> layoutSize(profile)))
				.orElse(null);
	}

	private int layoutSize(InstrumentProfile profile) {
		String layout = profile.getKeyLayout();
		if (layout == null || layout.isBlank()) {
			return 0;
		}
		return layout.split(",").length;
	}

	private String summary(List<OptimizationVO.Finding> findings, int strokes) {
		if (strokes == 0) {
			return "这套方案还没有按键组，先选一个乐器档案做一次映射。";
		}
		long warn = findings.stream().filter(item -> "WARN".equals(item.getSeverity())).count();
		if (warn > 0) {
			return "计划里有 " + warn + " 处会影响实际演奏的问题，建议先处理下面的第一条。";
		}
		if (findings.isEmpty()) {
			return "这份计划可以直接照着弹：没有吞键风险、没有超出音域的漏音。";
		}
		return "这份计划可以演奏，下面的建议能让它更稳一些。";
	}

	private int weight(String severity) {
		return switch (severity == null ? "" : severity) {
			case "WARN" -> 0;
			case "ADVICE" -> 1;
			default -> 2;
		};
	}

	private List<String> keysOf(KeySequenceVO.Stroke stroke) {
		return stroke.getKeys() == null ? List.of() : stroke.getKeys();
	}

	private int value(Integer number) {
		return number == null ? 0 : number;
	}
}
