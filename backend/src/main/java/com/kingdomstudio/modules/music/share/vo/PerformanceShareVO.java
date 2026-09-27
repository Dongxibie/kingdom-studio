package com.kingdomstudio.modules.music.share.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 曲谱分享的对外结构 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "曲谱分享")
public class PerformanceShareVO {

	private Long id;

	@Schema(description = "演奏码，例如 KS-MUSIC-2026-A001")
	private String shareCode;

	private String creator;
	private String title;

	@Schema(description = "难度：星级 + 分层")
	private String difficulty;

	private String game;
	private String instrument;
	private Integer noteCount;
	private Integer durationMs;

	@Schema(description = "被导入次数")
	private Integer importCount;

	private String createTime;

	@Schema(description = "分享内容摘要：拿到码的人先看到这些，再决定导不导入")
	private List<String> highlights;

	@Schema(description = "导入后落在哪首曲子上（仅导入接口返回）")
	private Long importedTaskId;

	@Schema(description = "导入说明")
	private String message;
}
