package com.kingdomstudio.modules.motion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 新增 / 修改动效资源的入参。 */
@Data
@Schema(description = "动效资源保存入参")
public class MotionSaveDTO {

	@NotBlank(message = "动效名称不能为空")
	@Size(max = 160, message = "动效名称不能超过 160 个字符")
	@Schema(description = "动效名称", example = "玻璃卡片悬停")
	private String name;

	@Size(max = 600, message = "说明不能超过 600 个字符")
	@Schema(description = "一句话说明")
	private String description;

	@NotBlank(message = "分类不能为空")
	@Schema(description = "分类：Entrance / Hover / Scroll / Text / Particle / 3D / Glass / Cursor / Background / Loading")
	private String category;

	@Schema(description = "技术栈", example = "CSS")
	private String technology;

	@Schema(description = "来源页面地址")
	private String sourceUrl;

	@Schema(description = "仓库地址")
	private String repoUrl;

	@Schema(description = "预览地址")
	private String previewUrl;

	@Schema(description = "标签，英文逗号分隔", example = "glass,premium,hover")
	private String tags;

	@Schema(description = "开源许可", example = "MIT")
	private String license;

	@Schema(description = "代码在仓库中的位置")
	private String codePath;

	@Schema(description = "状态：DRAFT / READY / ARCHIVED", example = "READY")
	private String status;
}
