package com.kingdomstudio.modules.desktop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 派发请求：把一首曲子的按键序列交给桌面代理执行。
 *
 * <p>本期只做模拟回显：代理不会真的按键，返回的是「如果执行会按下什么」的计划。
 */
@Data
@Schema(description = "桌面代理派发请求")
public class DispatchRequestDTO {

	@NotNull(message = "请指定要派发的音乐任务")
	@Schema(description = "音乐任务 id（按键序列由服务端按档案重新算，不接受前端直接传按键）")
	private Long taskId;

	@NotNull(message = "请指定乐器档案")
	@Schema(description = "乐器档案 id")
	private Long profileId;

	@Schema(description = "临时覆盖超范围策略：SKIP / NEAREST / SHIFT_OCTAVE")
	private String strategy;

	@Schema(description = "客户端标识，便于日志里分辨是谁在调试", example = "kingdom-music-agent")
	private String clientId;
}
