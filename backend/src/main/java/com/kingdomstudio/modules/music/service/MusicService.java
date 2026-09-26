package com.kingdomstudio.modules.music.service;

import com.kingdomstudio.modules.music.vo.MusicModuleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 音乐 Agent 模块信息。
 *
 * <p>Phase 1 只提供模块自检接口，Phase 4 起提供音乐任务的解析与映射能力。
 */
@Slf4j
@Service
public class MusicService {

	/** 模块自检：模块信息集中在这里，避免散落到 Controller 或前端常量里 */
	public MusicModuleVO moduleInfo() {
		log.debug("音乐 Agent 模块自检");
		return MusicModuleVO.builder()
				.module("music")
				.name("音乐 Agent")
				.englishName("Kingdom Music Agent")
				.phase("v1.1.0 · 已上线（解析 / 映射 / 时间线 / 回放）")
				.apiBase("/api/music")
				.capabilities(List.of("MIDI 文件解析（JDK 自带 javax.sound.midi，纯 Java 实现，不引入 Python 服务）", "简谱文本输入", "音符时间线（BPM、时值、节拍对齐）", "乐器映射（Instrument Profile：音域、按键布局、映射规则可配置，不写死）", "按键序列导出", "Demo 回放（网页内模拟：时间轴移动、音符高亮、按键动画）"))
				.plannedTables(List.of("music_task", "music_note", "instrument_profile"))
				.build();
	}
}
