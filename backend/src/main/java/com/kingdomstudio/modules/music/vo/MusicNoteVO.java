package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 音符：前端时间线直接用它渲染，音名由后端算好，前端不再重复一套换算。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "音符")
public class MusicNoteVO {

	private Integer seqNo;

	private Integer startMs;

	private Integer durationMs;

	private Integer pitch;

	private String noteName;

	private Integer velocity;

	private Integer trackNo;

	@Schema(description = "相对于全曲最低音的半音数，前端画音高条时用它定高度")
	private Integer pitchOffset;
}
