package com.kingdomstudio.modules.music.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.music.dto.InstrumentProfileSaveDTO;
import com.kingdomstudio.modules.music.entity.InstrumentProfile;
import com.kingdomstudio.modules.music.mapper.InstrumentProfileMapper;
import com.kingdomstudio.modules.music.parser.PitchNames;
import com.kingdomstudio.modules.music.vo.InstrumentProfileVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 乐器按键档案：查询与维护。
 *
 * <p>档案里存的是按键 JSON，返回给前端时会展开成「键 → 音高 → 音名」三列并排的形式：
 * 前端的虚拟键盘要显示每个键弹什么音，让它自己去算等于把同一套音阶规则写第二遍。
 */
@Service
@RequiredArgsConstructor
public class InstrumentProfileService {

	/** 一次最多多少个键：虚拟乐器没见过超过两排的，超过多半是配错了 */
	private static final int MAX_KEYS = 48;

	private static final List<String> MODES = List.of("CHROMATIC", "DIATONIC", "CUSTOM");
	private static final List<String> SCALES = List.of("MAJOR", "MINOR", "PENTATONIC", "CHROMATIC");
	private static final List<String> STRATEGIES = List.of("SKIP", "NEAREST", "SHIFT_OCTAVE");
	private static final List<String> STATUSES = List.of("READY", "ARCHIVED");

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final InstrumentProfileMapper instrumentProfileMapper;
	private final InstrumentMappingService mappingService;

	public List<InstrumentProfileVO> list() {
		List<InstrumentProfile> rows = instrumentProfileMapper.selectList(
				new LambdaQueryWrapper<InstrumentProfile>().orderByAsc(InstrumentProfile::getId));
		List<InstrumentProfileVO> result = new ArrayList<>(rows.size());
		for (InstrumentProfile row : rows) {
			result.add(toVO(row));
		}
		return result;
	}

	public InstrumentProfileVO detail(Long id) {
		return toVO(require(id));
	}

	public Long create(InstrumentProfileSaveDTO request) {
		InstrumentProfile existing = instrumentProfileMapper.selectOne(
				new LambdaQueryWrapper<InstrumentProfile>().eq(InstrumentProfile::getName, request.getName().trim()));
		if (existing != null) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "已存在同名档案：" + request.getName());
		}
		InstrumentProfile entity = new InstrumentProfile();
		apply(entity, request);
		instrumentProfileMapper.insert(entity);
		return entity.getId();
	}

	public void update(Long id, InstrumentProfileSaveDTO request) {
		InstrumentProfile entity = require(id);
		InstrumentProfile sameName = instrumentProfileMapper.selectOne(
				new LambdaQueryWrapper<InstrumentProfile>().eq(InstrumentProfile::getName, request.getName().trim()));
		if (sameName != null && !sameName.getId().equals(id)) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "已存在同名档案：" + request.getName());
		}
		apply(entity, request);
		instrumentProfileMapper.updateById(entity);
	}

	public void delete(Long id) {
		require(id);
		instrumentProfileMapper.deleteById(id);
	}

	private InstrumentProfile require(Long id) {
		InstrumentProfile entity = instrumentProfileMapper.selectById(id);
		if (entity == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "乐器档案不存在（id=" + id + "）");
		}
		return entity;
	}

	private void apply(InstrumentProfile entity, InstrumentProfileSaveDTO request) {
		List<String> keys = request.getKeyLayout() == null ? List.of() : request.getKeyLayout().stream()
				.map(key -> key == null ? "" : key.trim())
				.filter(key -> !key.isEmpty())
				.toList();
		if (keys.isEmpty()) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "请至少配置一个按键");
		}
		if (keys.size() > MAX_KEYS) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "按键数量不能超过 " + MAX_KEYS + " 个");
		}
		String mode = upper(request.getMappingMode(), MODES, "映射方式");
		String scale = request.getScale() == null || request.getScale().isBlank()
				? ("CHROMATIC".equals(mode) ? "CHROMATIC" : "MAJOR")
				: upper(request.getScale(), SCALES, "音阶");
		String strategy = request.getUnmappedStrategy() == null || request.getUnmappedStrategy().isBlank()
				? "SKIP" : upper(request.getUnmappedStrategy(), STRATEGIES, "超范围策略");
		String status = request.getStatus() == null || request.getStatus().isBlank()
				? "READY" : upper(request.getStatus(), STATUSES, "状态");

		entity.setName(request.getName().trim());
		entity.setInstrument(request.getInstrument() == null ? "" : request.getInstrument().trim());
		entity.setMappingMode(mode);
		entity.setScale(scale);
		entity.setKeyLayout(toJson(keys));
		entity.setBasePitch(request.getBasePitch() == null ? 60 : request.getBasePitch());
		entity.setTranspose(request.getTranspose() == null ? 0 : request.getTranspose());
		entity.setOctaveShift(request.getOctaveShift() == null ? 0 : request.getOctaveShift());
		entity.setUnmappedStrategy(strategy);
		entity.setDescription(request.getDescription() == null ? "" : request.getDescription().trim());
		entity.setStatus(status);
	}

	private String upper(String value, List<String> allowed, String label) {
		if (value == null) {
			throw new BusinessException(ResultCode.BAD_REQUEST, label + "不能为空");
		}
		String normalized = value.trim().toUpperCase(Locale.ROOT);
		if (!allowed.contains(normalized)) {
			throw new BusinessException(ResultCode.BAD_REQUEST, label + "必须是以下之一：" + String.join(" / ", allowed));
		}
		return normalized;
	}

	private String toJson(List<String> keys) {
		try {
			return OBJECT_MAPPER.writeValueAsString(keys);
		} catch (Exception e) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "按键配置写入失败：" + e.getMessage());
		}
	}

	InstrumentProfileVO toVO(InstrumentProfile entity) {
		List<String> keys = mappingService.parseLayout(entity.getKeyLayout());
		List<Integer> pitches = keys.isEmpty() ? List.of() : mappingService.expandKeyPitches(entity);
		List<String> names = new ArrayList<>(pitches.size());
		for (Integer pitch : pitches) {
			names.add(PitchNames.name(pitch));
		}
		return InstrumentProfileVO.builder()
				.id(entity.getId())
				.name(entity.getName())
				.instrument(entity.getInstrument())
				.mappingMode(entity.getMappingMode())
				.scale(entity.getScale())
				.keyLayout(keys)
				.keyPitches(pitches)
				.keyNoteNames(names)
				.basePitch(entity.getBasePitch())
				.transpose(entity.getTranspose())
				.octaveShift(entity.getOctaveShift())
				.unmappedStrategy(entity.getUnmappedStrategy())
				.description(entity.getDescription())
				.status(entity.getStatus())
				.createTime(entity.getCreateTime())
				.build();
	}
}
