package com.kingdomstudio.modules.music.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 启动本机演奏的入参。
 *
 * <p>{@code confirm} 必须显式为 true —— 这是「用户主动开启」在协议层的落点：
 * 没有这个字段就一律拒绝，不接受任何默认开启。
 */
@Data
@Schema(description = "启动本机演奏")
public class LocalRunRequestDTO {

	@Schema(description = "用户确认过的目标窗口标题；留空表示用当前前台窗口")
	private String targetWindow;

	@Schema(description = "是否已确认「我知道这会在本机发送按键」；必须为 true")
	private Boolean confirm;
}
