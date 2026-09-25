package com.kingdomstudio.modules.desktop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 桌面代理状态：本机有没有接上真实代理、怎么接。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "桌面代理状态")
public class AgentStatusVO {

	@Schema(description = "是否已接入真实代理；本期固定 false")
	private Boolean available;

	@Schema(description = "运行模式：MOCK（模拟回显）/ LIVE（真实执行）")
	private String mode;

	@Schema(description = "协议版本")
	private String protocolVersion;

	@Schema(description = "代理要连的 WebSocket 地址")
	private String endpoint;

	@Schema(description = "已注册的客户端会话数（模拟）")
	private Integer sessions;

	@Schema(description = "说明：为什么现在是模拟，以及接真实代理需要做什么")
	private List<String> notes;
}
