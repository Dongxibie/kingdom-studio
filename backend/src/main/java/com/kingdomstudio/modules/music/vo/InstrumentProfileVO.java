package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 乐器按键档案：除原始配置外还带上展开后的键位音高，前端画键盘时不用自己算。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "乐器按键档案")
public class InstrumentProfileVO {

	private Long id;

	private String name;

	private String instrument;

	@Schema(description = "CHROMATIC / DIATONIC / CUSTOM")
	private String mappingMode;

	private String scale;

	private List<String> keyLayout;

	@Schema(description = "keyLayout 对应的音高号")
	private List<Integer> keyPitches;

	@Schema(description = "keyLayout 对应的音名")
	private List<String> keyNoteNames;

	private Integer basePitch;

	private Integer transpose;

	private Integer octaveShift;

	@Schema(description = "SKIP / NEAREST / SHIFT_OCTAVE")
	private String unmappedStrategy;

	private String description;

	private String status;

	private LocalDateTime createTime;
}
