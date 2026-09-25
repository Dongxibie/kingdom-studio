package com.kingdomstudio.modules.motion.vo;

import com.kingdomstudio.modules.motion.dto.MotionCodeSaveDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 详情：资源字段 + 代码产物。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动效详情")
public class MotionDetailVO {

	private Long id;
	private String name;
	private String description;
	private String category;
	private String technology;
	private String sourceUrl;
	private String repoUrl;
	private String previewUrl;
	private List<String> tags;
	private String license;
	private String codePath;
	private String status;
	private String createTime;
	private String updateTime;

	/** 代码产物；还没有生成过时为 null */
	private MotionCodeSaveDTO code;
}
