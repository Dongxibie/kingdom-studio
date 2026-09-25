package com.kingdomstudio.modules.desktop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WebSocket 协议描述。
 *
 * <p>把协议做成接口返回而不是只写在文档里：文档会过期，而接口返回的内容
 * 是能被前端直接渲染、被测试直接断言的。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "桌面代理 WebSocket 协议")
public class ProtocolVO {

	private String version;

	@Schema(description = "传输方式：WebSocket")
	private String transport;

	@Schema(description = "代理需要连的地址")
	private String endpoint;

	@Schema(description = "消息信封字段说明")
	private List<FieldDoc> envelope;

	@Schema(description = "消息类型清单")
	private List<MessageDoc> messages;

	@Schema(description = "安全约束：真实执行前必须满足的前提")
	private List<String> safety;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "字段说明")
	public static class FieldDoc {
		private String name;
		private String type;
		private String description;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "消息说明")
	public static class MessageDoc {
		private String type;
		@Schema(description = "CLIENT_TO_AGENT / AGENT_TO_CLIENT")
		private String direction;
		private String description;
		private String example;
	}
}
