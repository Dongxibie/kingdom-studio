package com.kingdomstudio.modules.music.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.music.dto.MappingRequestDTO;
import com.kingdomstudio.modules.music.dto.PerformancePresetDTO;
import com.kingdomstudio.modules.music.entity.InstrumentProfile;
import com.kingdomstudio.modules.music.entity.MusicPerformancePreset;
import com.kingdomstudio.modules.music.entity.MusicTask;
import com.kingdomstudio.modules.music.mapper.InstrumentProfileMapper;
import com.kingdomstudio.modules.music.mapper.MusicPerformancePresetMapper;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import com.kingdomstudio.modules.music.vo.PerformancePresetVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 演奏方案（Performance Preset）：同一首曲子可以存多套打法。
 *
 * <p>三套内置方案不是写死的常量，而是**按库里的乐器档案现算的**：
 * <ul>
 *   <li><b>原版</b>：默认档案（第一套），原速；</li>
 *   <li><b>简单版</b>：键数最少的那套档案 —— 键少意味着好上手，超范围的音按移八度处理；</li>
 *   <li><b>快速版</b>：默认档案 + 速度 1.35 倍，适合短视频展示。</li>
 * </ul>
 * 所以换一批乐器档案，内置方案会跟着变；第一次打开某首曲子时落库（只插一次），之后就能改名、删除、再存自己的方案。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformancePresetService {

	/** 快速版的速度倍率 */
	static final double FAST_SCALE = 1.35;

	/** 内置方案的名字，用来判断是否已经生成过 */
	static final List<String> BUILTIN_NAMES = List.of("原版", "简单版", "快速版");

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final MusicPerformancePresetMapper presetMapper;
	private final InstrumentProfileMapper instrumentProfileMapper;
	private final MusicTaskService musicTaskService;

	/** 方案列表：首次访问补齐内置方案 */
	@Transactional
	public List<PerformancePresetVO> list(Long taskId) {
		MusicTask task = musicTaskService.require(taskId);
		ensureBuiltin(task);
		return toVOs(load(taskId));
	}

	/** 新建方案 */
	@Transactional
	public PerformancePresetVO create(Long taskId, PerformancePresetDTO request) {
		musicTaskService.require(taskId);
		requireProfile(request.getProfileId());
		List<MusicPerformancePreset> sameName = presetMapper.selectList(new LambdaQueryWrapper<MusicPerformancePreset>()
				.eq(MusicPerformancePreset::getTaskId, taskId)
				.eq(MusicPerformancePreset::getName, request.getName().trim()));
		if (!sameName.isEmpty()) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "这首曲子已经有一个叫「" + request.getName() + "」的方案了");
		}
		MusicPerformancePreset entity = new MusicPerformancePreset();
		entity.setTaskId(taskId);
		entity.setBuiltin(0);
		apply(entity, request);
		presetMapper.insert(entity);
		log.info("新建演奏方案：task={} name={} profile={} speed={}", taskId, entity.getName(),
				entity.getProfileId(), entity.getSpeedScale());
		return toVO(entity, requireProfile(entity.getProfileId()));
	}

	/** 修改方案（内置方案也可以改，改完就是自己的了） */
	@Transactional
	public PerformancePresetVO update(Long presetId, PerformancePresetDTO request) {
		MusicPerformancePreset entity = require(presetId);
		requireProfile(request.getProfileId());
		apply(entity, request);
		entity.setBuiltin(0);
		presetMapper.updateById(entity);
		return toVO(entity, requireProfile(entity.getProfileId()));
	}

	/** 删除方案（逻辑删除） */
	@Transactional
	public void delete(Long presetId) {
		MusicPerformancePreset entity = require(presetId);
		presetMapper.deleteById(entity.getId());
		log.info("删除演奏方案：id={} name={}", entity.getId(), entity.getName());
	}

	/** 方案解析结果：映射请求 + 速度 + 间隔，供演奏计划使用 */
	public record Resolution(MappingRequestDTO mapping, double speedScale, int minGapMs, PerformancePresetVO preset) {
	}

	/** 取出一个方案的执行参数；presetId 为空时返回原速、无间隔的默认参数 */
	public Resolution resolve(Long taskId, Long presetId) {
		if (presetId == null) {
			return new Resolution(null, 1.0, 0, null);
		}
		MusicPerformancePreset entity = require(presetId);
		if (!entity.getTaskId().equals(taskId)) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "方案与曲目不匹配（presetId=" + presetId + "）");
		}
		MappingRequestDTO mapping = new MappingRequestDTO();
		mapping.setProfileId(entity.getProfileId());
		mapping.setStrategy(entity.getStrategy());
		return new Resolution(mapping, entity.getSpeedScale() == null ? 1.0 : entity.getSpeedScale().doubleValue(),
				entity.getMinGapMs() == null ? 0 : entity.getMinGapMs(),
				toVO(entity, requireProfile(entity.getProfileId())));
	}

	/** 按某个方案跑一遍映射（导出中心用它在方案之间快速切换） */
	public KeySequenceVO mapWithPreset(Long taskId, Long presetId) {
		Resolution resolution = resolve(taskId, presetId);
		if (resolution.mapping() == null) {
			MusicTask task = musicTaskService.require(taskId);
			List<InstrumentProfile> profiles = instrumentProfileMapper.selectList(new LambdaQueryWrapper<>());
			if (profiles.isEmpty()) {
				throw new BusinessException(ResultCode.BAD_REQUEST, "还没有乐器档案，先建一套再用方案");
			}
			MappingRequestDTO fallback = new MappingRequestDTO();
			fallback.setProfileId(profiles.get(0).getId());
			return musicTaskService.mapKeys(task.getId(), fallback);
		}
		return musicTaskService.mapKeys(taskId, resolution.mapping());
	}

	// ------------------------------------------------------------------ 内置方案

	private void ensureBuiltin(MusicTask task) {
		List<MusicPerformancePreset> existing = load(task.getId());
		if (!existing.isEmpty()) {
			return;
		}
		List<InstrumentProfile> profiles = instrumentProfileMapper.selectList(new LambdaQueryWrapper<>());
		if (profiles.isEmpty()) {
			return;
		}
		InstrumentProfile base = profiles.get(0);
		InstrumentProfile simplest = profiles.stream()
				.min(Comparator.comparingInt(this::layoutSize).thenComparing(InstrumentProfile::getId))
				.orElse(base);

		List<MusicPerformancePreset> builtins = new ArrayList<>();
		builtins.add(builtin(task.getId(), "原版", base, null, 1.0, 0, "按原谱演奏，用默认键位"));
		if (!simplest.getId().equals(base.getId())) {
			builtins.add(builtin(task.getId(), "简单版", simplest, "SHIFT_OCTAVE", 1.0, 0,
					"换成键数更少的「" + simplest.getName() + "」，超范围的音自动移八度"));
		}
		builtins.add(builtin(task.getId(), "快速版", base, null, FAST_SCALE, 0, "速度提高 35%，适合短视频展示"));
		for (MusicPerformancePreset entity : builtins) {
			presetMapper.insert(entity);
		}
		log.info("为曲目 {} 生成 {} 套内置方案", task.getId(), builtins.size());
	}

	private MusicPerformancePreset builtin(Long taskId, String name, InstrumentProfile profile, String strategy,
			double speed, int gap, String note) {
		MusicPerformancePreset entity = new MusicPerformancePreset();
		entity.setTaskId(taskId);
		entity.setName(name);
		entity.setProfileId(profile.getId());
		entity.setStrategy(strategy);
		entity.setSpeedScale(BigDecimal.valueOf(speed).setScale(2, RoundingMode.HALF_UP));
		entity.setMinGapMs(gap);
		entity.setBuiltin(1);
		entity.setNote(note);
		return entity;
	}

	private int layoutSize(InstrumentProfile profile) {
		String layout = profile.getKeyLayout();
		if (layout == null || layout.isBlank()) {
			return Integer.MAX_VALUE;
		}
		return layout.split(",").length;
	}

	// ------------------------------------------------------------------ 通用

	private void apply(MusicPerformancePreset entity, PerformancePresetDTO request) {
		entity.setName(request.getName().trim());
		entity.setProfileId(request.getProfileId());
		entity.setStrategy(normalizeStrategy(request.getStrategy()));
		entity.setSpeedScale(BigDecimal.valueOf(request.getSpeedScale() == null ? 1.0 : request.getSpeedScale())
				.setScale(2, RoundingMode.HALF_UP));
		entity.setMinGapMs(request.getMinGapMs() == null ? 0 : request.getMinGapMs());
		entity.setNote(request.getNote() == null ? "" : request.getNote().trim());
	}

	private String normalizeStrategy(String strategy) {
		if (strategy == null || strategy.isBlank()) {
			return null;
		}
		String value = strategy.trim().toUpperCase(java.util.Locale.ROOT);
		if (!List.of("SKIP", "NEAREST", "SHIFT_OCTAVE").contains(value)) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的策略：" + strategy);
		}
		return value;
	}

	private List<MusicPerformancePreset> load(Long taskId) {
		return presetMapper.selectList(new LambdaQueryWrapper<MusicPerformancePreset>()
				.eq(MusicPerformancePreset::getTaskId, taskId)
				.orderByAsc(MusicPerformancePreset::getId));
	}

	private MusicPerformancePreset require(Long presetId) {
		MusicPerformancePreset entity = presetId == null ? null : presetMapper.selectById(presetId);
		if (entity == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "演奏方案不存在（id=" + presetId + "）");
		}
		return entity;
	}

	private InstrumentProfile requireProfile(Long profileId) {
		InstrumentProfile profile = profileId == null ? null : instrumentProfileMapper.selectById(profileId);
		if (profile == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "乐器档案不存在（id=" + profileId + "）");
		}
		return profile;
	}

	private List<PerformancePresetVO> toVOs(List<MusicPerformancePreset> entities) {
		List<InstrumentProfile> profiles = instrumentProfileMapper.selectList(new LambdaQueryWrapper<>());
		List<PerformancePresetVO> result = new ArrayList<>(entities.size());
		for (MusicPerformancePreset entity : entities) {
			InstrumentProfile profile = profiles.stream()
					.filter(item -> item.getId().equals(entity.getProfileId())).findFirst().orElse(null);
			result.add(toVO(entity, profile));
		}
		return result;
	}

	private PerformancePresetVO toVO(MusicPerformancePreset entity, InstrumentProfile profile) {
		double scale = entity.getSpeedScale() == null ? 1.0 : entity.getSpeedScale().doubleValue();
		return PerformancePresetVO.builder()
				.id(entity.getId())
				.taskId(entity.getTaskId())
				.name(entity.getName())
				.profileId(entity.getProfileId())
				.profileName(profile == null ? "已删除的档案" : profile.getName())
				.keyCount(profile == null ? null : layoutSize(profile))
				.strategy(entity.getStrategy())
				.speedScale(scale)
				.speedText(speedText(scale))
				.minGapMs(entity.getMinGapMs())
				.builtin(entity.getBuiltin() != null && entity.getBuiltin() == 1)
				.note(entity.getNote())
				.createTime(entity.getCreateTime() == null ? null : entity.getCreateTime().format(TIME_FORMAT))
				.build();
	}

	static String speedText(double scale) {
		if (Math.abs(scale - 1.0) < 0.01) {
			return "原速";
		}
		int percent = (int) Math.round(Math.abs(scale - 1.0) * 100);
		return (scale > 1 ? "快 " : "慢 ") + percent + "%";
	}
}
