package com.kingdomstudio.modules.music.analysis;

import com.kingdomstudio.modules.music.dto.MappingRequestDTO;
import com.kingdomstudio.modules.music.entity.InstrumentProfile;
import com.kingdomstudio.modules.music.entity.MusicNote;
import com.kingdomstudio.modules.music.entity.MusicTask;
import com.kingdomstudio.modules.music.mapper.InstrumentProfileMapper;
import com.kingdomstudio.modules.music.mapper.MusicNoteMapper;
import com.kingdomstudio.modules.music.service.MusicTaskService;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import com.kingdomstudio.modules.music.vo.SongAnalysisVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 曲目分析：把「这首曲子难不难、多快、多久能弹完、该用哪套键位」一次算清楚。
 *
 * <p>两条纪律：
 * <ol>
 *   <li><b>难度只看库里已有的字段</b>（{@link DifficultyRule}），所以曲库列表与详情卡上的星级一致；</li>
 *   <li><b>推荐键位靠真实试算</b>：把每个乐器档案都跑一遍映射，谁能把所有音落下、谁用的键最少就推谁 ——
 *       不猜、不靠配置表，推荐结果与用户在编排台里手动试出来的结论一致。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SongAnalysisService {

	private final MusicTaskService musicTaskService;
	private final MusicNoteMapper musicNoteMapper;
	private final InstrumentProfileMapper instrumentProfileMapper;

	/** 分析一首曲子；不指定档案时按「推荐键位」试算 */
	public SongAnalysisVO analyze(Long taskId, Long profileId) {
		MusicTask task = musicTaskService.require(taskId);
		List<MusicNote> notes = musicNoteMapper.selectList(new LambdaQueryWrapper<MusicNote>()
				.eq(MusicNote::getTaskId, taskId).orderByAsc(MusicNote::getStartMs));

		DifficultyRule.Input input = new DifficultyRule.Input(
				value(task.getNoteCount()), value(task.getDurationMs()), value(task.getTempoBpm()),
				value(task.getPitchLow()), value(task.getPitchHigh()));
		DifficultyRule.Result difficulty = DifficultyRule.evaluate(input);

		List<InstrumentProfile> profiles = instrumentProfileMapper.selectList(new LambdaQueryWrapper<>());
		List<Candidate> candidates = evaluateProfiles(taskId, profiles, notes);

		long requested = profileId == null ? -1 : profileId;
		Candidate chosen = candidates.stream()
				.filter(item -> item.profile().getId() == requested)
				.findFirst()
				.orElseGet(() -> candidates.stream()
						// 先把所有音都能落下的排在前面，再挑键数最少的（键越少越好上手）
						.min(Comparator.comparing((Candidate item) -> item.sequence().getUnmappedCount())
								.thenComparing(item -> item.sequence().getKeyCount())
								.thenComparing(item -> item.profile().getId()))
						.orElse(null));

		List<SongAnalysisVO.Alternative> alternatives = new ArrayList<>();
		for (Candidate candidate : candidates) {
			if (chosen != null && candidate.profile().getId().equals(chosen.profile().getId())) {
				continue;
			}
			alternatives.add(SongAnalysisVO.Alternative.builder()
					.profileId(candidate.profile().getId())
					.profileName(candidate.profile().getName())
					.keyCount(candidate.sequence().getKeyCount())
					.coversAll(candidate.sequence().getUnmappedCount() == 0)
					.unmappedCount(candidate.sequence().getUnmappedCount())
					.build());
		}
		alternatives.sort(Comparator.comparing(SongAnalysisVO.Alternative::getUnmappedCount)
				.thenComparing(SongAnalysisVO.Alternative::getKeyCount));

		int estimated = chosen == null ? value(task.getDurationMs()) : candidateDuration(chosen.sequence());
		List<SongAnalysisVO.Reason> reasons = difficulty.reasons().stream()
				.map(item -> SongAnalysisVO.Reason.builder().label(item.label()).hit(item.hit()).build())
				.toList();

		return SongAnalysisVO.builder()
				.taskId(taskId)
				.taskName(task.getName())
				.difficultyStars(difficulty.stars())
				.difficultyLabel(difficulty.label())
				.difficultyHint(DifficultyRule.hintOf(difficulty.stars()))
				.difficultyTier(difficulty.tier())
				.difficultyTierLabel(difficulty.tierLabel())
				.audience(difficulty.audience())
				.reasons(reasons)
				.tempoBpm(task.getTempoBpm())
				.timeSignature(task.getTimeSignature())
				.pitchRange(range(task))
				.pitchSpan(input.pitchHigh() - input.pitchLow())
				.noteCount(task.getNoteCount())
				.estimatedDuration(estimated)
				.estimatedText(Math.round(estimated / 1000.0) + " 秒")
				.chordRatio(chordRatio(notes))
				.recommendedPresetName(chosen == null ? null : presetNameOf(chosen))
				.recommendedPresetNote(chosen == null ? null : presetNoteOf(chosen))
				.recommendedProfileId(chosen == null ? null : chosen.profile().getId())
				.recommendedProfileName(chosen == null ? null : chosen.profile().getName())
				.mappedCount(chosen == null ? 0 : chosen.sequence().getMappedCount())
				.unmappedCount(chosen == null ? 0 : chosen.sequence().getUnmappedCount())
				.alternatives(alternatives)
				.build();
	}

	/** 每个乐器档案都试一遍：能不能全落下、用多少个键 */
	private List<Candidate> evaluateProfiles(Long taskId, List<InstrumentProfile> profiles, List<MusicNote> notes) {
		List<Candidate> candidates = new ArrayList<>();
		for (InstrumentProfile profile : profiles) {
			try {
				MappingRequestDTO request = new MappingRequestDTO();
				request.setProfileId(profile.getId());
				KeySequenceVO sequence = musicTaskService.mapKeys(taskId, request);
				candidates.add(new Candidate(profile, sequence));
			} catch (Exception e) {
				// 单个档案试算失败不影响其它档案的推荐
				log.warn("档案 {} 试算失败：{}", profile.getName(), e.getMessage());
			}
		}
		return candidates;
	}

	/** 预计演奏时长：最后一组按键的结束时刻 */
	private int candidateDuration(KeySequenceVO sequence) {
		int end = 0;
		for (KeySequenceVO.Stroke stroke : sequence.getStrokes()) {
			int start = stroke.getStartMs() == null ? 0 : stroke.getStartMs();
			int hold = stroke.getDurationMs() == null ? 0 : stroke.getDurationMs();
			end = Math.max(end, start + hold);
		}
		return end;
	}

	/** 推荐方案的名称与说明：键少意味着好上手，能全落下意味着不用改谱 */
	private String presetNameOf(Candidate candidate) {
		return candidate.sequence().getKeyCount() + " 键模式";
	}

	private String presetNoteOf(Candidate candidate) {
		if (candidate.sequence().getUnmappedCount() == 0) {
			return "用「" + candidate.profile().getName() + "」可以把这首曲子的每个音都落下来";
		}
		return "「" + candidate.profile().getName() + "」有 "
				+ candidate.sequence().getUnmappedCount() + " 个音超出音域，可用策略就近落键或移八度";
	}

	/** 和弦比例：同一时刻（startMs 相同）有多个音的，算和弦音 */
	private double chordRatio(List<MusicNote> notes) {
		if (notes.isEmpty()) {
			return 0;
		}
		Map<Integer, Integer> byStart = new LinkedHashMap<>();
		for (MusicNote note : notes) {
			int start = note.getStartMs() == null ? 0 : note.getStartMs();
			byStart.merge(start, 1, Integer::sum);
		}
		long chords = byStart.values().stream().filter(count -> count > 1).mapToLong(Integer::longValue).sum();
		return Math.round(chords * 1000.0 / notes.size()) / 1000.0;
	}

	private String range(MusicTask task) {
		if (task.getPitchLow() == null || task.getPitchHigh() == null) {
			return "";
		}
		return midiName(task.getPitchLow()) + "–" + midiName(task.getPitchHigh());
	}

	private String midiName(int midi) {
		String[] names = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
		return names[midi % 12] + (midi / 12 - 1);
	}

	private int value(Integer number) {
		return number == null ? 0 : number;
	}

	/** 一个档案的试算结果 */
	private record Candidate(InstrumentProfile profile, KeySequenceVO sequence) {
	}
}
