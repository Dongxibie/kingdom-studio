package com.kingdomstudio.modules.music.analysis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kingdomstudio.modules.music.dto.MappingRequestDTO;
import com.kingdomstudio.modules.music.entity.InstrumentProfile;
import com.kingdomstudio.modules.music.mapper.InstrumentProfileMapper;
import com.kingdomstudio.modules.music.service.MusicTaskService;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 乐器匹配：从「游戏乐器档案」里挑出最适合这首曲子的那一套。
 *
 * <p>判断依据是真的跑一遍映射，不是查配置表：
 * <ol>
 *   <li><b>能全落下</b>优先 —— 有音落在音域外，演奏时就会漏音；</li>
 *   <li>其次<b>键数接近</b> —— 用户点了「15 键口风琴」，就不该给他 25 键的音符盒；</li>
 *   <li>再其次<b>键数少</b> —— 同等条件下键越少越好上手。</li>
 * </ol>
 *
 * <p>筛选条件（游戏名 / 乐器名）是「优先」而不是「只允许」：库里没有该游戏的档案时，
 * 会退回到通用档案并把这件事写进理由里，而不是返回空列表让用户干瞪眼。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileMatchService {

	private final MusicTaskService musicTaskService;
	private final InstrumentProfileMapper instrumentProfileMapper;

	/** 一个候选：档案 + 试算结果 */
	public record Candidate(InstrumentProfile profile, KeySequenceVO sequence, int score, List<String> reasons) {

		public boolean coversAll() {
			return sequence.getUnmappedCount() != null && sequence.getUnmappedCount() == 0;
		}
	}

	/**
	 * 给一首曲子排序可用的乐器档案。
	 *
	 * @param game       目标游戏（可空）：命中的排前面
	 * @param instrument 目标乐器（可空）：命中的排前面
	 * @param keyCount   期望键数（可空）：越接近排越前
	 */
	public List<Candidate> rank(Long taskId, String game, String instrument, Integer keyCount) {
		List<InstrumentProfile> profiles = instrumentProfileMapper.selectList(new LambdaQueryWrapper<>());
		List<Candidate> candidates = new ArrayList<>();
		for (InstrumentProfile profile : profiles) {
			try {
				MappingRequestDTO request = new MappingRequestDTO();
				request.setProfileId(profile.getId());
				KeySequenceVO sequence = musicTaskService.mapKeys(taskId, request);
				candidates.add(score(profile, sequence, game, instrument, keyCount));
			} catch (Exception e) {
				// 单个档案试算失败不影响其它档案的匹配结果
				log.warn("档案 {} 试算失败：{}", profile.getName(), e.getMessage());
			}
		}
		candidates.sort(Comparator.comparingInt(Candidate::score).reversed()
				.thenComparing(candidate -> candidate.profile().getId()));
		return candidates;
	}

	/** 打分与理由：分数越高越合适，理由逐条写清为什么 */
	Candidate score(InstrumentProfile profile, KeySequenceVO sequence, String game, String instrument, Integer keyCount) {
		List<String> reasons = new ArrayList<>();
		int score = 0;

		if (sequence.getUnmappedCount() != null && sequence.getUnmappedCount() == 0) {
			score += 100;
			reasons.add("所有音都能落键，不需要改动曲谱");
		} else {
			score -= sequence.getUnmappedCount() * 5;
			reasons.add("有 " + sequence.getUnmappedCount() + " 个音超出音域");
		}

		int keys = sequence.getKeyCount() == null ? 0 : sequence.getKeyCount();
		if (keyCount != null && keyCount > 0) {
			int distance = Math.abs(keys - keyCount);
			score += Math.max(0, 20 - distance * 2);
			if (distance == 0) {
				reasons.add("键数正好是你要的 " + keyCount + " 键");
			} else {
				reasons.add("键数与目标差 " + distance + " 个（实际 " + keys + " 键）");
			}
		}
		score += Math.max(0, 15 - keys);

		String profileGame = profile.getGame() == null ? "" : profile.getGame();
		if (game != null && !game.isBlank()) {
			if (profileGame.toLowerCase(Locale.ROOT).contains(game.trim().toLowerCase(Locale.ROOT))) {
				score += 40;
				reasons.add("属于目标游戏「" + profileGame + "」");
			} else if ("通用".equals(profileGame)) {
				reasons.add("没有该游戏的专用档案，这是一套通用键位");
			}
		}
		if (instrument != null && !instrument.isBlank()
				&& profile.getInstrument() != null
				&& profile.getInstrument().toLowerCase(Locale.ROOT).contains(instrument.trim().toLowerCase(Locale.ROOT))) {
			score += 25;
			reasons.add("乐器正是「" + profile.getInstrument() + "」");
		}
		if (keys == 0) {
			score -= 50;
			reasons.add("这套档案没有可用的键位");
		}
		return new Candidate(profile, sequence, score, reasons);
	}

	/** 简版接口：只要前 N 个候选 */
	public List<Candidate> top(Long taskId, String game, String instrument, Integer keyCount, int limit) {
		List<Candidate> all = rank(taskId, game, instrument, keyCount);
		return all.size() > limit ? all.subList(0, limit) : all;
	}
}
