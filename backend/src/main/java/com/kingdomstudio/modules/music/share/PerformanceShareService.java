package com.kingdomstudio.modules.music.share;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.music.analysis.DifficultyRule;
import com.kingdomstudio.modules.music.entity.MusicNote;
import com.kingdomstudio.modules.music.entity.MusicTask;
import com.kingdomstudio.modules.music.mapper.MusicNoteMapper;
import com.kingdomstudio.modules.music.parser.ParsedSong;
import com.kingdomstudio.modules.music.mapper.InstrumentProfileMapper;
import com.kingdomstudio.modules.music.service.MusicTaskService;
import com.kingdomstudio.modules.music.service.PerformanceMacroService;
import com.kingdomstudio.modules.music.service.PerformancePresetService;
import com.kingdomstudio.modules.music.share.dto.PerformanceShareDTO;
import com.kingdomstudio.modules.music.share.vo.PerformanceShareVO;
import com.kingdomstudio.modules.music.vo.MusicTaskDetailVO;
import com.kingdomstudio.modules.music.vo.PerformancePlanVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 曲谱分享：把一份演奏方案变成一串可以口头转述的演奏码。
 *
 * <p>三条设计取舍：
 * <ol>
 *   <li><b>快照是自包含的</b>：payload 里带音符、乐器、难度与导出事件。
 *       别人的库里没有这首曲子也能完整还原 —— 分享不能依赖「对方的数据库里正好有同一首歌」；</li>
 *   <li><b>导入只做「复制」，不改来源</b>：导入方得到一首**新的曲目**（复用既有的派生曲目能力），
 *       双方各改各的，不会互相影响；</li>
 *   <li><b>导入前先给摘要</b>：拿到码的人先看到曲名、乐器、难度、时长，再决定导不导入。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceShareService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final PerformanceShareMapper shareMapper;
	private final MusicNoteMapper musicNoteMapper;
	private final MusicTaskService musicTaskService;
	private final PerformancePresetService presetService;
	private final PerformanceMacroService macroService;
	private final InstrumentProfileMapper instrumentProfileMapper;

	/** 生成分享码：把当前曲目 + 方案 + 计划打包成一份快照 */
	@Transactional
	public PerformanceShareVO create(Long taskId, PerformanceShareDTO request) {
		MusicTask task = musicTaskService.require(taskId);
		Long presetId = request == null ? null : request.getPresetId();
		PerformancePresetService.Resolution resolution = presetService.resolve(taskId, presetId);
		PerformancePlanVO plan = latestPlan(taskId);

		List<MusicNote> notes = musicNoteMapper.selectList(new LambdaQueryWrapper<MusicNote>()
				.eq(MusicNote::getTaskId, taskId).orderByAsc(MusicNote::getStartMs));

		PerformanceShare entity = new PerformanceShare();
		entity.setPerformancePlanId(plan == null ? null : plan.getId());
		entity.setTaskId(taskId);
		entity.setCreator(request == null || request.getCreator() == null || request.getCreator().isBlank()
				? "匿名" : request.getCreator().trim());
		entity.setTitle(request != null && request.getTitle() != null && !request.getTitle().isBlank()
				? request.getTitle().trim() : task.getName());
		DifficultyRule.Result difficulty = difficultyOf(task);
		entity.setDifficulty(difficulty.stars() + " 星 · " + difficulty.label() + "（" + difficulty.tierLabel() + "）");
		entity.setInstrument(resolution.preset() != null ? resolution.preset().getProfileName()
				: (plan == null ? "" : plan.getProfileName()));
		entity.setGame(resolution.preset() != null ? gameOfProfile(resolution.preset().getProfileId()) : "");
		entity.setNoteCount(task.getNoteCount());
		entity.setDurationMs(plan == null ? task.getDurationMs() : plan.getDuration());
		entity.setImportCount(0);
		entity.setPayload(snapshot(task, notes, plan, resolution, difficulty));
		// 先插入拿到 id，再用 id 生成演奏码（码里带序号，念一遍就能核对）
		entity.setShareCode("PENDING");
		shareMapper.insert(entity);
		entity.setShareCode(ShareCode.of(entity.getId(), LocalDate.now().getYear()));
		shareMapper.updateById(entity);
		log.info("生成演奏码：code={} task={} 快照={}字节", entity.getShareCode(), taskId,
				entity.getPayload().length());

		return toVO(entity, true);
	}

	/** 我分享出去的（按时间倒序，最多 50 条） */
	public List<PerformanceShareVO> list() {
		List<PerformanceShare> rows = shareMapper.selectList(new LambdaQueryWrapper<PerformanceShare>()
				.orderByDesc(PerformanceShare::getId).last("LIMIT 50"));
		List<PerformanceShareVO> result = new ArrayList<>(rows.size());
		for (PerformanceShare row : rows) {
			result.add(toVO(row, false));
		}
		return result;
	}

	/** 看一个演奏码里有什么（不导入也能先看摘要） */
	public PerformanceShareVO describe(String rawCode) {
		return toVO(require(ShareCode.normalize(rawCode, LocalDate.now().getYear())), true);
	}

	/** 导入演奏码：在本库里还原成一首新曲目 */
	@Transactional
	public PerformanceShareVO importShare(String rawCode) {
		PerformanceShare share = require(ShareCode.normalize(rawCode, LocalDate.now().getYear()));
		ParsedSong song = parseSnapshot(share);
		MusicTaskDetailVO detail;
		try {
			detail = musicTaskService.createDerived(song,
					"演奏码 " + share.getShareCode() + " ← " + share.getCreator());
		} catch (Exception e) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "导入失败：" + e.getMessage());
		}
		share.setImportCount((share.getImportCount() == null ? 0 : share.getImportCount()) + 1);
		shareMapper.updateById(share);
		log.info("导入演奏码：code={} 新曲目={}", share.getShareCode(), detail.getId());

		PerformanceShareVO vo = toVO(share, true);
		vo.setImportedTaskId(detail.getId());
		vo.setMessage("已导入为「" + detail.getName() + "」，可以直接去编排台演奏或导出");
		return vo;
	}

	// ------------------------------------------------------------------ 快照

	/** 快照：音符 + 来源 + 乐器 + 难度 + 导出事件，导入方仅凭它就能还原 */
	String snapshot(MusicTask task, List<MusicNote> notes, PerformancePlanVO plan,
			PerformancePresetService.Resolution resolution, DifficultyRule.Result difficulty) {
		ObjectNode root = OBJECT_MAPPER.createObjectNode();
		root.put("version", 1);
		root.put("taskName", task.getName());
		root.put("sourceType", task.getSourceType());
		root.put("sourceRef", task.getSourceRef());
		root.put("tempoBpm", task.getTempoBpm() == null ? 0 : task.getTempoBpm());
		root.put("timeSignature", task.getTimeSignature() == null ? "" : task.getTimeSignature());
		root.put("pitchLow", task.getPitchLow() == null ? 0 : task.getPitchLow());
		root.put("pitchHigh", task.getPitchHigh() == null ? 0 : task.getPitchHigh());
		ObjectNode difficultyNode = root.putObject("difficulty");
		difficultyNode.put("stars", difficulty.stars());
		difficultyNode.put("label", difficulty.label());
		difficultyNode.put("tier", difficulty.tier());
		difficultyNode.put("audience", difficulty.audience());

		ObjectNode presetNode = root.putObject("preset");
		if (resolution.preset() == null) {
			presetNode.putNull("name");
			presetNode.put("speedScale", 1.0);
			presetNode.put("minGapMs", 0);
		} else {
			presetNode.put("name", resolution.preset().getName());
			presetNode.put("profileName", resolution.preset().getProfileName());
			presetNode.put("keyCount", resolution.preset().getKeyCount() == null ? 0 : resolution.preset().getKeyCount());
			presetNode.put("strategy", resolution.preset().getStrategy() == null ? "" : resolution.preset().getStrategy());
			presetNode.put("speedScale", resolution.preset().getSpeedScale());
			presetNode.put("minGapMs", resolution.preset().getMinGapMs());
		}
		if (plan != null) {
			ObjectNode planNode = root.putObject("plan");
			planNode.put("profileName", plan.getProfileName());
			planNode.put("duration", plan.getDuration());
			planNode.put("noteCount", plan.getNoteCount());
			planNode.put("strokeCount", plan.getStrokeCount());
			ArrayNode events = planNode.putArray("events");
			for (PerformancePlanVO.Note event : plan.getNotes()) {
				ObjectNode item = events.addObject();
				item.put("key", event.getKey());
				item.put("action", event.getAction());
				item.put("timestamp", event.getTimestamp());
			}
		}
		ArrayNode noteArray = root.putArray("notes");
		for (MusicNote note : notes) {
			ObjectNode item = noteArray.addObject();
			item.put("startMs", note.getStartMs());
			item.put("durationMs", note.getDurationMs());
			item.put("pitch", note.getPitch());
			item.put("noteName", note.getNoteName());
			item.put("velocity", note.getVelocity());
			item.put("trackNo", note.getTrackNo());
		}
		return root.toString();
	}

	/** 从快照还原一首曲子的解析结果（走既有的派生曲目能力写库） */
	ParsedSong parseSnapshot(PerformanceShare share) {
		try {
			JsonNode root = OBJECT_MAPPER.readTree(share.getPayload());
			List<ParsedSong.Note> notes = new ArrayList<>();
			for (JsonNode node : root.path("notes")) {
				notes.add(new ParsedSong.Note(
						node.path("pitch").asInt(),
						node.path("velocity").asInt(90),
						node.path("startMs").asInt(),
						node.path("durationMs").asInt(),
						node.path("trackNo").asInt(0)));
			}
			if (notes.isEmpty()) {
				throw new BusinessException(ResultCode.BAD_REQUEST, "这份分享里没有音符数据");
			}
			int duration = 0;
			for (ParsedSong.Note note : notes) {
				duration = Math.max(duration, note.endMs());
			}
			return new ParsedSong(
					root.path("taskName").asText(share.getTitle()),
					"SHARE",
					"演奏码 " + share.getShareCode() + " ← " + share.getCreator(),
					Math.max(30, root.path("tempoBpm").asInt(100)),
					root.path("timeSignature").asText("4/4"),
					duration,
					notes);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "分享内容解析失败：" + e.getMessage());
		}
	}

	// ------------------------------------------------------------------ 通用

	private PerformanceShare require(String shareCode) {
		if (shareCode == null || shareCode.isBlank()) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "请输入演奏码");
		}
		PerformanceShare share = shareMapper.selectList(new LambdaQueryWrapper<PerformanceShare>()
						.eq(PerformanceShare::getShareCode, shareCode))
				.stream().findFirst().orElse(null);
		if (share == null) {
			throw new BusinessException(ResultCode.NOT_FOUND,
					"没有找到这个演奏码：" + shareCode + "（注意格式是 KS-MUSIC-年份-编号，例如 KS-MUSIC-2026-A001）");
		}
		return share;
	}

	/** 最近一次演奏计划：没有生成过就返回 null —— 分享不该被「你还没生成计划」挡住 */
	private PerformancePlanVO latestPlan(Long taskId) {
		try {
			PerformancePlanVO plan = macroService.latest(taskId);
			return plan.getNotes() == null || plan.getNotes().isEmpty() ? plan : plan;
		} catch (Exception e) {
			log.info("曲目 {} 还没有演奏计划，快照里不带事件：{}", taskId, e.getMessage());
			return null;
		}
	}

	private DifficultyRule.Result difficultyOf(MusicTask task) {
		return DifficultyRule.evaluate(new DifficultyRule.Input(
				value(task.getNoteCount()), value(task.getDurationMs()), value(task.getTempoBpm()),
				value(task.getPitchLow()), value(task.getPitchHigh())));
	}

	/** 目标游戏名：从乐器档案里读，写在分享记录上便于按游戏检索 */
	private String gameOfProfile(Long profileId) {
		if (profileId == null) {
			return "";
		}
		var profile = instrumentProfileMapper.selectById(profileId);
		return profile == null || profile.getGame() == null ? "" : profile.getGame();
	}

	private int value(Integer number) {
		return number == null ? 0 : number;
	}

	private PerformanceShareVO toVO(PerformanceShare entity, boolean withHighlights) {
		List<String> highlights = new ArrayList<>();
		if (withHighlights) {
			highlights.add(entity.getNoteCount() + " 个音符 · " + Math.round(value(entity.getDurationMs()) / 1000.0) + " 秒");
			if (entity.getInstrument() != null && !entity.getInstrument().isBlank()) {
				highlights.add("乐器：" + entity.getInstrument());
			}
			highlights.add("难度：" + entity.getDifficulty());
			try {
				JsonNode root = OBJECT_MAPPER.readTree(entity.getPayload());
				int events = root.path("plan").path("events").size();
				if (events > 0) {
					highlights.add(events + " 条按键动作，导入后可直接导出脚本");
				}
			} catch (Exception ignored) {
				// 快照读不出来也不影响展示基本信息
			}
		}
		return PerformanceShareVO.builder()
				.id(entity.getId())
				.shareCode(entity.getShareCode())
				.creator(entity.getCreator())
				.title(entity.getTitle())
				.difficulty(entity.getDifficulty())
				.game(entity.getGame())
				.instrument(entity.getInstrument())
				.noteCount(entity.getNoteCount())
				.durationMs(entity.getDurationMs())
				.importCount(entity.getImportCount())
				.createTime(entity.getCreateTime() == null ? null : entity.getCreateTime().format(TIME_FORMAT))
				.highlights(highlights)
				.build();
	}
}
