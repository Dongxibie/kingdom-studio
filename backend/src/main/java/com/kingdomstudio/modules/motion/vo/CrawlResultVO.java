package com.kingdomstudio.modules.motion.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/** 采集结果统计。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "采集结果")
public class CrawlResultVO {

	private Integer scanned;
	private Integer created;
	private Integer skippedByUrl;
	private Integer skippedByContent;
	private Integer skippedBySimilarity;
	private Integer failed;
	private Boolean dryRun;
	/** 分类 → 数量 */
	private Map<String, Integer> byCategory;
	/** 每个关键词的执行情况，便于排查限流 */
	private List<String> notes;
}
