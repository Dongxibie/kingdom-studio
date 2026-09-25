package com.kingdomstudio.modules.motion.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 列表项：只带列表要展示的字段，不带大段代码，避免列表接口把 MEDIUMTEXT 全捞出来。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动效列表项")
public class MotionListItemVO {

	private Long id;
	private String name;
	private String description;
	private String category;
	private String technology;
	private String sourceUrl;
	private String previewUrl;
	private List<String> tags;
	private String license;
	private String status;
	/** 是否已经生成过代码 */
	private Boolean hasCode;
	private String updateTime;
}
