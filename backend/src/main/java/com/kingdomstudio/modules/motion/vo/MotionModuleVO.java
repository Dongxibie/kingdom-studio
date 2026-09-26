package com.kingdomstudio.modules.motion.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 动效基因库 模块信息（VO：只暴露给前端看的字段）。
 *
 * <p>与 {@code HealthVO} 保持同一套注解：显式声明无参构造器，避免 {@code @Builder}
 * 抑制默认构造器后 Jackson 反序列化失败（原因见 HealthVO 上的说明）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动效基因库 模块信息")
public class MotionModuleVO {

	@Schema(description = "模块标识", example = "motion")
	private String module;

	@Schema(description = "模块中文名", example = "动效基因库")
	private String name;

	@Schema(description = "模块英文名", example = "Kingdom Motion Lab")
	private String englishName;

	@Schema(description = "当前状态", example = "v1.1.0 · 已上线（动效工作台 / 智能助手 / 模板组合 / 代码生成）")
	private String phase;

	@Schema(description = "接口前缀", example = "/api/motion")
	private String apiBase;

	@Schema(description = "规划中的能力清单")
	private List<String> capabilities;

	@Schema(description = "规划中的数据表")
	private List<String> plannedTables;
}
