package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 任务详情：元信息 + 全部音符，供时间线与映射使用。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "音乐解析任务详情")
public class MusicTaskDetailVO {

	private Long id;

	private String name;

	private String sourceType;

	private String sourceRef;

	private Integer noteCount;

	private Integer tempoBpm;

	private String timeSignature;

	private Integer durationMs;

	private Integer pitchLow;

	private Integer pitchHigh;

	@Schema(description = "音域，如 C4–C6")
	private String pitchRange;

	private String status;

	private LocalDateTime createTime;

	@Schema(description = "全部音符，按起始时间排序")
	private List<MusicNoteVO> notes;
}
