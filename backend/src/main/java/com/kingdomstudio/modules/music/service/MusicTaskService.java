package com.kingdomstudio.modules.music.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kingdomstudio.common.PageVO;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.music.dto.JianpuParseDTO;
import com.kingdomstudio.modules.music.dto.MappingRequestDTO;
import com.kingdomstudio.modules.music.entity.InstrumentProfile;
import com.kingdomstudio.modules.music.entity.MusicNote;
import com.kingdomstudio.modules.music.entity.MusicTask;
import com.kingdomstudio.modules.music.mapper.InstrumentProfileMapper;
import com.kingdomstudio.modules.music.mapper.MusicNoteMapper;
import com.kingdomstudio.modules.music.mapper.MusicTaskMapper;
import com.kingdomstudio.modules.music.parser.JianpuParser;
import com.kingdomstudio.modules.music.parser.MidiParser;
import com.kingdomstudio.modules.music.parser.ParsedSong;
import com.kingdomstudio.modules.music.parser.PitchNames;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import com.kingdomstudio.modules.music.vo.MusicNoteVO;
import com.kingdomstudio.modules.music.vo.MusicTaskDetailVO;
import com.kingdomstudio.modules.music.vo.MusicTaskListItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 音乐解析任务：MIDI 上传 / 简谱粘贴 → 存任务与音符 → 按键映射。
 *
 * <p>解析本身放在 {@link MidiParser} 与 {@link JianpuParser} 里（纯逻辑、可单测），
 * 这里只管落库、查询与串起映射。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MusicTaskService {

	private static final int MAX_NOTES = 20_000;

	private final MusicTaskMapper musicTaskMapper;
	private final MusicNoteMapper musicNoteMapper;
	private final InstrumentProfileMapper instrumentProfileMapper;
	private final MidiParser midiParser;
	private final JianpuParser jianpuParser;
	private final InstrumentMappingService mappingService;

	/** 上传 MIDI：解析成功才落库，解析失败直接报错，不留下半条脏任务 */
	@Transactional(rollbackFor = Exception.class)
	public MusicTaskDetailVO createFromMidi(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "请选择要上传的 MIDI 文件");
		}
		byte[] bytes;
		try {
			bytes = file.getBytes();
		} catch (IOException e) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "读取上传文件失败：" + e.getMessage());
		}
		String filename = file.getOriginalFilename() == null ? "未命名.mid" : file.getOriginalFilename();
		ParsedSong song;
		try {
			song = midiParser.parse(bytes, stripExtension(filename));
		} catch (Exception e) {
			throw new BusinessException(ResultCode.BAD_REQUEST,
					"这个文件解析不了，请确认是标准的 .mid 文件（" + e.getClass().getSimpleName() + "）");
		}
		return persist(song, filename);
	}

	/** 粘贴简谱：同样只在解析成功后才落库 */
	@Transactional(rollbackFor = Exception.class)
	public MusicTaskDetailVO createFromJianpu(JianpuParseDTO request) {
		ParsedSong song = jianpuParser.parse(request.getJianpu(), request.getName());
		if (song.noteCount() == 0) {
			throw new BusinessException(ResultCode.BAD_REQUEST,
					"没有从这段简谱里读到音符，请检查写法（例如：1=C 4/4 后接 5 5 6 5 | 1' 7 6 5）");
		}
		return persist(song, "简谱粘贴");
	}

	private MusicTaskDetailVO persist(ParsedSong song, String sourceRef) {
		if (song.noteCount() > MAX_NOTES) {
			throw new BusinessException(ResultCode.BAD_REQUEST,
					"音符数量超过上限（" + MAX_NOTES + "），请先裁剪曲子");
		}
		MusicTask task = new MusicTask();
		task.setName(song.title());
		task.setSourceType(song.sourceType());
		task.setSourceRef(sourceRef == null ? song.sourceRef() : sourceRef);
		task.setNoteCount(song.noteCount());
		task.setTempoBpm(song.tempoBpm());
		task.setTimeSignature(song.timeSignature());
		task.setDurationMs(song.durationMs());
		task.setPitchLow(song.pitchLow());
		task.setPitchHigh(song.pitchHigh());
		task.setStatus("READY");
		musicTaskMapper.insert(task);

		int seq = 1;
		for (ParsedSong.Note note : song.notes()) {
			MusicNote entity = new MusicNote();
			entity.setTaskId(task.getId());
			entity.setSeqNo(seq++);
			entity.setStartMs(note.startMs());
			entity.setDurationMs(note.durationMs());
			entity.setPitch(note.pitch());
			entity.setNoteName(PitchNames.name(note.pitch()));
			entity.setVelocity(note.velocity());
			entity.setTrackNo(note.trackNo());
			musicNoteMapper.insert(entity);
		}
		log.info("音乐任务已入库：id={} 来源={} 音符={} 时长={}ms",
				task.getId(), task.getSourceType(), task.getNoteCount(), task.getDurationMs());
		return detail(task.getId());
	}

	/** 分页列表：关键词匹配曲子名与来源信息 */
	/**
	 * 读出一首曲子的音符结构（AI 助手与策略层用）。
	 *
	 * <p>只读取、不修改：助手拿到的永远是库里那份原始音符。
	 */
	public ParsedSong songOf(Long taskId) {
		MusicTask task = musicTaskMapper.selectById(taskId);
		if (task == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "音乐任务不存在（id=" + taskId + "）");
		}
		return toParsedSong(task);
	}

	/**
	 * 把一份「调整后的曲子」落成新曲目（例如初学版 / 展示版）。
	 *
	 * <p>走的是与上传同一套持久化逻辑，所以音数、速度、音域等统计口径完全一致；
	 * 原曲目不会被改动。
	 */
	public MusicTaskDetailVO createDerived(ParsedSong song, String sourceRef) {
		return persist(song, sourceRef);
	}

	public PageVO<MusicTaskListItemVO> page(String keyword, long page, long size) {
		long current = Math.max(1, page);
		long pageSize = Math.min(Math.max(1, size), 100);
		LambdaQueryWrapper<MusicTask> wrapper = new LambdaQueryWrapper<>();
		if (keyword != null && !keyword.isBlank()) {
			String like = keyword.trim();
			wrapper.and(query -> query.like(MusicTask::getName, like)
					.or().like(MusicTask::getSourceRef, like));
		}
		wrapper.orderByDesc(MusicTask::getId);
		Page<MusicTask> result = musicTaskMapper.selectPage(new Page<>(current, pageSize), wrapper);
		return PageVO.of(result, this::toListItem);
	}

	public MusicTaskDetailVO detail(Long id) {
		MusicTask task = musicTaskMapper.selectById(id);
		if (task == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "音乐任务不存在（id=" + id + "）");
		}
		return toDetail(task);
	}

	/** 删除任务时连音符一起删：留着孤儿音符只会在后续统计里算错 */
	@Transactional(rollbackFor = Exception.class)
	public void delete(Long id) {
		MusicTask task = musicTaskMapper.selectById(id);
		if (task == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "音乐任务不存在（id=" + id + "）");
		}
		musicNoteMapper.delete(new LambdaQueryWrapper<MusicNote>().eq(MusicNote::getTaskId, id));
		musicTaskMapper.deleteById(id);
	}

	/** 按键映射：把任务里的音符交给映射引擎，翻译成按键序列 */
	public KeySequenceVO mapKeys(Long taskId, MappingRequestDTO request) {
		MusicTask task = musicTaskMapper.selectById(taskId);
		if (task == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "音乐任务不存在（id=" + taskId + "）");
		}
		InstrumentProfile profile = instrumentProfileMapper.selectById(request.getProfileId());
		if (profile == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "乐器档案不存在（id=" + request.getProfileId() + "）");
		}
		ParsedSong song = toParsedSong(task);
		KeySequenceVO sequence = mappingService.map(song, profile, request.getStrategy());
		sequence.setTaskId(task.getId());
		sequence.setTaskName(task.getName());
		return sequence;
	}

	/** 把库里存的任务还原成解析结果：映射与导出都复用同一种结构 */
	ParsedSong toParsedSong(MusicTask task) {
		List<MusicNote> rows = musicNoteMapper.selectList(new LambdaQueryWrapper<MusicNote>()
				.eq(MusicNote::getTaskId, task.getId())
				.orderByAsc(MusicNote::getSeqNo));
		List<ParsedSong.Note> notes = new ArrayList<>(rows.size());
		for (MusicNote row : rows) {
			notes.add(new ParsedSong.Note(row.getPitch(), row.getVelocity(),
					row.getStartMs(), row.getDurationMs(), row.getTrackNo()));
		}
		return new ParsedSong(task.getName(), task.getSourceType(), task.getSourceRef(),
				task.getTempoBpm(), task.getTimeSignature(), task.getDurationMs(), ParsedSong.sort(notes));
	}

	MusicTaskListItemVO toListItem(MusicTask task) {
		return MusicTaskListItemVO.builder()
				.id(task.getId())
				.name(task.getName())
				.sourceType(task.getSourceType())
				.sourceRef(task.getSourceRef())
				.noteCount(task.getNoteCount())
				.tempoBpm(task.getTempoBpm())
				.timeSignature(task.getTimeSignature())
				.durationMs(task.getDurationMs())
				.pitchRange(range(task.getPitchLow(), task.getPitchHigh()))
				.status(task.getStatus())
				.createTime(task.getCreateTime())
				.build();
	}

	private MusicTaskDetailVO toDetail(MusicTask task) {
		List<MusicNote> rows = musicNoteMapper.selectList(new LambdaQueryWrapper<MusicNote>()
				.eq(MusicNote::getTaskId, task.getId())
				.orderByAsc(MusicNote::getSeqNo));
		int low = task.getPitchLow() == null ? 0 : task.getPitchLow();
		List<MusicNoteVO> notes = new ArrayList<>(rows.size());
		for (MusicNote row : rows) {
			notes.add(MusicNoteVO.builder()
					.seqNo(row.getSeqNo())
					.startMs(row.getStartMs())
					.durationMs(row.getDurationMs())
					.pitch(row.getPitch())
					.noteName(row.getNoteName())
					.velocity(row.getVelocity())
					.trackNo(row.getTrackNo())
					.pitchOffset(row.getPitch() - low)
					.build());
		}
		return MusicTaskDetailVO.builder()
				.id(task.getId())
				.name(task.getName())
				.sourceType(task.getSourceType())
				.sourceRef(task.getSourceRef())
				.noteCount(task.getNoteCount())
				.tempoBpm(task.getTempoBpm())
				.timeSignature(task.getTimeSignature())
				.durationMs(task.getDurationMs())
				.pitchLow(task.getPitchLow())
				.pitchHigh(task.getPitchHigh())
				.pitchRange(range(task.getPitchLow(), task.getPitchHigh()))
				.status(task.getStatus())
				.createTime(task.getCreateTime())
				.notes(notes)
				.build();
	}

	private String range(Integer low, Integer high) {
		if (low == null || high == null || (low == 0 && high == 0)) {
			return "—";
		}
		return PitchNames.name(low) + "–" + PitchNames.name(high);
	}

	private String stripExtension(String filename) {
		int dot = filename.lastIndexOf('.');
		return dot > 0 ? filename.substring(0, dot) : filename;
	}
}
