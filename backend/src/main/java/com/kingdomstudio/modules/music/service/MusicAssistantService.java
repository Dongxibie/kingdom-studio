package com.kingdomstudio.modules.music.service;

import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.music.assistant.MusicModelClient;
import com.kingdomstudio.modules.music.dto.MusicPromptDTO;
import com.kingdomstudio.modules.music.dto.StrategyApplyDTO;
import com.kingdomstudio.modules.music.entity.MusicTask;
import com.kingdomstudio.modules.music.mapper.MusicTaskMapper;
import com.kingdomstudio.modules.music.parser.ParsedSong;
import com.kingdomstudio.modules.music.strategy.PerformanceStrategy;
import com.kingdomstudio.modules.music.strategy.StrategyApplier;
import com.kingdomstudio.modules.music.vo.MusicAssistantVO;
import com.kingdomstudio.modules.music.vo.MusicTaskDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 音乐助手：把一句自然语言要求变成「一套可执行的演奏方案 + 逐条解释」。
 *
 * <p>三条设计原则，与动效助手保持完全一致：
 * <ol>
 *   <li><b>模型负责理解，规则负责改写</b>：模型只说「这听起来是初学难度 / 目标是 15 键口风琴」，
 *       真正改曲子的是 {@link StrategyApplier} 的确定性规则；模型没有权限写音符、也不生成音乐；</li>
 *   <li><b>模型返回一律过校验</b>：难度必须落在白名单里、建议条数与长度都截断，
 *       认不出来的值直接丢弃并回退到规则判断 —— 不把模型的自由文本当指令执行；</li>
 *   <li><b>改动必须可解释</b>：每条调整都带「改了什么 + 为什么」，直接作为 AI Explain 展示。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MusicAssistantService {

	/** 建议条数上限与单条长度上限：模型给再多也只留这么多 */
	private static final int MAX_SUGGESTIONS = 5;
	private static final int MAX_SUGGESTION_LENGTH = 60;
	private static final int MAX_INSTRUMENT_LENGTH = 40;

	/** 关键词 → 难度档（规则模式，也是模型失效时的兜底） */
	private static final Map<PerformanceStrategy, List<String>> STRATEGY_HINTS = Map.of(
			PerformanceStrategy.BEGINNER, List.of("简单", "初学", "入门", "容易", "先顺", "慢一点", "简化", "easy", "beginner", "simple"),
			PerformanceStrategy.SHOWCASE, List.of("展示", "演出", "表演", "舞台", "炫", "表现力", "更有感觉", "showcase", "show", "performance"));

	private final MusicTaskMapper musicTaskMapper;
	private final MusicTaskService musicTaskService;
	private final StrategyApplier strategyApplier;
	private final MusicModelClient modelClient;

	/** 理解一句要求，并给出方案预览（不改库） */
	public MusicAssistantVO analyze(Long taskId, MusicPromptDTO request) {
		ParsedSong song = musicTaskService.songOf(taskId);
		String prompt = request.getPrompt().trim();
		String summary = summarize(song);

		PerformanceStrategy strategy = fromRules(prompt);
		String instrument = instrumentFromRules(prompt);
		List<String> suggestions = new ArrayList<>();
		String source = "RULE";
		String modelName = null;
		String fallbackReason = null;

		boolean recognizedByRules = strategy != PerformanceStrategy.NORMAL || !instrument.isEmpty();
		if (modelClient.enabled()) {
			MusicModelClient.Answer answer = modelClient.analyze(prompt, summary);
			if (answer != null) {
				PerformanceStrategy fromModel = PerformanceStrategy.of(answer.difficulty());
				boolean valid = PerformanceStrategy.recognizes(answer.difficulty());
				if (valid) {
					source = "MODEL";
					modelName = modelClient.modelName();
					strategy = fromModel;
				} else {
					fallbackReason = "模型给出的难度档无法识别（" + answer.difficulty()
							+ "），已改用规则判断；建议清单仍来自模型。";
				}
				instrument = sanitize(answer.instrument(), MAX_INSTRUMENT_LENGTH);
				suggestions = sanitizeSuggestions(answer.suggestions());
			} else {
				fallbackReason = "模型本次没有返回可用结果（未配置、超时、格式不合法或服务不可用），已改用规则判断。";
			}
		} else {
			fallbackReason = "未配置模型（MUSIC_LLM_BASE_URL / MUSIC_LLM_API_KEY / MUSIC_LLM_MODEL），当前使用规则判断。";
		}

		if (suggestions.isEmpty()) {
			suggestions = ruleSuggestions(strategy, song);
		}

		StrategyApplier.Result applied = strategyApplier.apply(song, strategy);
		MusicTask task = musicTaskMapper.selectById(taskId);
		return MusicAssistantVO.builder()
				.taskId(taskId)
				.taskName(task == null ? song.title() : task.getName())
				.source(source)
				.modelName(modelName)
				.fallbackReason(fallbackReason)
				.intent(MusicAssistantVO.Intent.builder()
						.difficulty(strategy.name())
						.difficultyLabel(strategy.label())
						.instrument(instrument.isEmpty() ? null : instrument)
						.suggestions(suggestions)
						.build())
				.explanation(applied.summary())
				.adjustments(applied.adjustments().stream()
						.map(item -> MusicAssistantVO.Change.builder()
								.type(item.type()).title(item.title()).detail(item.detail())
								.before(item.before()).after(item.after())
								.build())
						.toList())
				.preview(MusicAssistantVO.Preview.builder()
						.noteCountBefore(song.notes().size())
						.noteCountAfter(applied.song().notes().size())
						.tempoBefore(song.tempoBpm())
						.tempoAfter(applied.song().tempoBpm())
						.durationBefore(song.durationMs())
						.durationAfter(applied.song().durationMs())
						.build())
				.build();
	}

	/**
	 * 应用方案：把调整后的曲子落成一条新曲目（例如「小星星（初学版）」）。
	 *
	 * <p>原曲目一个字段都不动 —— 方案是「另一份演奏版本」，不是覆盖。
	 */
	@Transactional
	public MusicAssistantVO apply(Long taskId, StrategyApplyDTO request) {
		if (!PerformanceStrategy.recognizes(request.getStrategy())) {
			throw new BusinessException(ResultCode.BAD_REQUEST,
					"不认识的方案：" + request.getStrategy() + "，可选 BEGINNER / NORMAL / SHOWCASE");
		}
		PerformanceStrategy strategy = PerformanceStrategy.of(request.getStrategy());
		ParsedSong song = musicTaskService.songOf(taskId);
		StrategyApplier.Result applied = strategyApplier.apply(song, strategy);
		if (strategy == PerformanceStrategy.NORMAL) {
			throw new BusinessException(ResultCode.BAD_REQUEST,
					"标准模式就是原曲本身，不需要生成新的版本；想换一种演奏方式请选初学或展示模式");
		}
		if (applied.adjustments().isEmpty()) {
			throw new BusinessException(ResultCode.BAD_REQUEST,
					"这首曲子在这套方案下没有可调整的地方，直接按原曲演奏即可");
		}
		String name = song.title() + "（" + strategy.label() + "）";
		// 来源里带源任务 id：幂等判断就是按「同名 + 同来源」找，不会误用别的曲子的同名版本
		String sourceRef = "AI 助手方案：" + strategy.label() + " ← 任务 " + taskId;
		// 幂等：同一个方案反复点「应用」不该堆出一串同名曲目
		MusicTask existing = musicTaskMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query
				.LambdaQueryWrapper<MusicTask>()
				.eq(MusicTask::getName, name)
				.eq(MusicTask::getSourceRef, sourceRef)
				.last("limit 1"));
		MusicTaskDetailVO created = existing != null ? detailOf(existing)
				: musicTaskService.createDerived(new ParsedSong(name, applied.song().sourceType(), sourceRef,
						applied.song().tempoBpm(), applied.song().timeSignature(), applied.song().durationMs(),
						applied.song().notes()), sourceRef);
		log.info("音乐助手应用方案：源任务={} 目标任务={} 方案={} 调整={} 条{}",
				taskId, created.getId(), strategy.name(), applied.adjustments().size(),
				existing != null ? "（已存在同名版本，直接复用）" : "");
		return MusicAssistantVO.builder()
				.taskId(created.getId())
				.taskName(created.getName())
				.source("RULE")
				.intent(MusicAssistantVO.Intent.builder()
						.difficulty(strategy.name())
						.difficultyLabel(strategy.label())
						.suggestions(List.of())
						.build())
				.explanation(applied.summary())
				.adjustments(applied.adjustments().stream()
						.map(item -> MusicAssistantVO.Change.builder()
								.type(item.type()).title(item.title()).detail(item.detail())
								.before(item.before()).after(item.after())
								.build())
						.toList())
				.preview(MusicAssistantVO.Preview.builder()
						.noteCountBefore(song.notes().size())
						.noteCountAfter(applied.song().notes().size())
						.tempoBefore(song.tempoBpm())
						.tempoAfter(applied.song().tempoBpm())
						.durationBefore(song.durationMs())
						.durationAfter(applied.song().durationMs())
						.build())
				.build();
	}

	/** 复用已存在的派生曲目时，也要给出同一形状的详情 */
	private MusicTaskDetailVO detailOf(MusicTask task) {
		MusicTaskDetailVO detail = new MusicTaskDetailVO();
		detail.setId(task.getId());
		detail.setName(task.getName());
		return detail;
	}

	/** 规则判断难度档：关键词命中，命中不了就是标准模式 */
	PerformanceStrategy fromRules(String prompt) {
		String text = prompt.toLowerCase(Locale.ROOT);
		for (Map.Entry<PerformanceStrategy, List<String>> entry : STRATEGY_HINTS.entrySet()) {
			for (String hint : entry.getValue()) {
				if (text.contains(hint)) {
					return entry.getKey();
				}
			}
		}
		return PerformanceStrategy.NORMAL;
	}

	/** 规则识别乐器：只做很轻的线索提取，识别不到就留空 */
	String instrumentFromRules(String prompt) {
		String text = prompt.toLowerCase(Locale.ROOT);
		for (String hint : List.of("15 键", "15键", "口风琴", "卡林巴", "八音盒", "钢琴", "键盘")) {
			if (text.contains(hint.toLowerCase(Locale.ROOT))) {
				return hint;
			}
		}
		return "";
	}

	private List<String> ruleSuggestions(PerformanceStrategy strategy, ParsedSong song) {
		List<String> items = new ArrayList<>();
		items.add("目标方案：" + strategy.label() + " —— " + strategy.description());
		switch (strategy) {
			case BEGINNER -> {
				items.add("先按 " + Math.round(song.tempoBpm() * 0.8) + " BPM 左右练，能顺下来再提速");
				items.add("把超过一个八度的跳进单独拎出来多练两遍");
				items.add("同时按下的音先只弹最低那一个，节奏稳住再加");
			}
			case SHOWCASE -> {
				items.add("重拍稍微加力，句尾长音留足呼吸");
				items.add("演奏前把乐器音量与音色先调好，展示模式的力度差更明显");
			}
			default -> items.add("原曲直接演奏即可；想更轻松或更有表现力，可以试试初学 / 展示模式");
		}
		return items;
	}

	private String summarize(ParsedSong song) {
		if (song.notes().isEmpty()) {
			return "空曲子";
		}
		int low = song.notes().stream().mapToInt(ParsedSong.Note::pitch).min().orElse(0);
		int high = song.notes().stream().mapToInt(ParsedSong.Note::pitch).max().orElse(0);
		return song.title() + "：" + song.notes().size() + " 个音，音域 " + low + "–" + high
				+ "，速度 " + song.tempoBpm() + " BPM，" + song.timeSignature()
				+ "，时长 " + (song.durationMs() / 1000) + " 秒";
	}

	private String sanitize(String value, int maxLength) {
		if (value == null || value.isBlank() || "null".equalsIgnoreCase(value.trim())) {
			return "";
		}
		String text = value.trim().replaceAll("[\\r\\n\\t]+", " ");
		return text.length() > maxLength ? text.substring(0, maxLength) : text;
	}

	/** 建议清单：条数与长度都截断，空行去掉 */
	List<String> sanitizeSuggestions(List<String> raw) {
		Set<String> seen = new LinkedHashSet<>();
		if (raw != null) {
			for (String item : raw) {
				String text = sanitize(item, MAX_SUGGESTION_LENGTH);
				if (!text.isEmpty()) {
					seen.add(text);
				}
				if (seen.size() >= MAX_SUGGESTIONS) {
					break;
				}
			}
		}
		return new ArrayList<>(seen);
	}
}
