package com.kingdomstudio.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 健康检查返回结果（VO：只暴露给前端看的字段，不直接用 Entity）。
 *
 * <p>显式声明无参构造器与全参构造器：{@code @Builder} 会让 Lombok 生成全参构造器，
 * 从而抑制 {@code @Data} 的默认无参构造器，导致 Jackson 反序列化时报
 * "no Creators, like default constructor, exist"。只返回给前端时看不出问题，
 * 一旦这类 VO 进 Redis 缓存被回读就会失败。后续新增 VO 沿用同一套注解。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "健康检查结果")
public class HealthVO {

	@Schema(description = "应用名", example = "kingdom-studio")
	private String application;

	@Schema(description = "综合状态：UP 全部正常 / DEGRADED 部分依赖不可用", example = "UP")
	private String status;

	@Schema(description = "版本号", example = "v1.0.0")
	private String version;

	@Schema(description = "Java 版本", example = "21.0.8")
	private String javaVersion;

	@Schema(description = "服务器当前时间", example = "2026-09-22 15:30:00")
	private String serverTime;

	@Schema(description = "MySQL 连接状态")
	private Component database;

	@Schema(description = "Redis 连接状态")
	private Component redis;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "依赖组件状态")
	public static class Component {

		@Schema(description = "状态：UP / DOWN", example = "UP")
		private String status;

		@Schema(description = "详情，如版本号或错误信息", example = "MySQL 8.0.26")
		private String detail;

		@Schema(description = "耗时（毫秒）", example = "12")
		private Long latencyMs;

		public static Component up(String detail, long latencyMs) {
			return Component.builder().status("UP").detail(detail).latencyMs(latencyMs).build();
		}

		public static Component down(String detail, long latencyMs) {
			return Component.builder().status("DOWN").detail(detail).latencyMs(latencyMs).build();
		}
	}
}
