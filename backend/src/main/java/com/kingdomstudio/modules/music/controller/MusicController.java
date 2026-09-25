package com.kingdomstudio.modules.music.controller;

import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.music.service.MusicService;
import com.kingdomstudio.modules.music.vo.MusicModuleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 音乐 Agent 模块自检接口。
 *
 * <p>Phase 1 只用来确认「扩展模块已被 Spring 扫描到、前端能调通」：
 * {@code GET http://localhost:8080/api/music/ping}
 *
 * <p>控制器只做接收与转发，模块信息在 {@link MusicService} 里组装。
 */
@Tag(name = "11. 音乐 Agent", description = "AI 音乐解析与游戏乐器转换实验室：MIDI / 简谱 → 乐器按键映射 → 演奏时间线 → 辅助执行")
@RestController
@RequestMapping("/music")
@RequiredArgsConstructor
public class MusicController {

	private final MusicService musicService;

	@Operation(summary = "模块自检", description = "返回模块名称、当前阶段与规划中的能力清单")
	@GetMapping("/ping")
	public Result<MusicModuleVO> ping() {
		return Result.success(musicService.moduleInfo());
	}
}
