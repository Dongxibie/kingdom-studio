package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 本机演奏的环境自检。
 *
 * <p>点「本地演奏」之前要先把这几件事摊开给使用者看：功能开没开、现在的前台窗口是什么、
 * 用的是真实注入还是记录模式、有哪些必须先答应的条件。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "本机演奏环境自检")
public class RuntimePreflightVO {

	@Schema(description = "本机执行开关是否打开（配置项 kingdom.music.local-execution.enabled）")
	private Boolean enabled;

	@Schema(description = "此刻的前台窗口标题：界面据此让使用者确认目标窗口")
	private String currentWindow;

	@Schema(description = "注入方式：真实按键 / 记录模式")
	private String injector;

	@Schema(description = "窗口守护是否可用（不可用时不允许真实注入）")
	private Boolean guardAvailable;

	@Schema(description = "是否可以开始本机演奏")
	private Boolean ready;

	@Schema(description = "不能开始时说明原因")
	private String reason;

	@Schema(description = "必须先确认的条件")
	private List<String> requirements;

	@Schema(description = "当前是否有会话在跑")
	private Boolean running;
}
