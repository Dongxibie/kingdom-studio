package com.kingdomstudio.modules.music.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** 乐器档案新增 / 修改入参 */
@Data
@Schema(description = "乐器档案入参")
public class InstrumentProfileSaveDTO {

	@NotBlank(message = "请填写档案名称")
	@Size(max = 96, message = "档案名称最长 96 个字符")
	private String name;

	@Size(max = 96, message = "乐器说明最长 96 个字符")
	private String instrument;

	@NotBlank(message = "请选择映射方式：CHROMATIC / DIATONIC / CUSTOM")
	private String mappingMode;

	private String scale;

	@Schema(description = "按键顺序，从最低音到最高音")
	private List<String> keyLayout;

	@Min(value = 0, message = "基准音高不能小于 0")
	@Max(value = 127, message = "基准音高不能大于 127")
	private Integer basePitch;

	@Min(value = -24, message = "移调范围 -24 ~ 24 个半音")
	@Max(value = 24, message = "移调范围 -24 ~ 24 个半音")
	private Integer transpose;

	@Min(value = -4, message = "升降八度范围 -4 ~ 4")
	@Max(value = 4, message = "升降八度范围 -4 ~ 4")
	private Integer octaveShift;

	private String unmappedStrategy;

	@Size(max = 400, message = "说明最长 400 个字符")
	private String description;

	private String status;
}
