/** 采集请求：与后端 CrawlRequestDTO 对齐 */
export interface CrawlRequest {
	/** 关键词；留空则用后端的默认关键词 */
	keywords: string[]
	/** 每个关键词取回多少条（后端上限 30） */
	limitPerKeyword: number
	/** true = 只试算不写库 */
	dryRun: boolean
}

/** 采集结果：与后端 CrawlResultVO 对齐 */
export interface CrawlResult {
	scanned: number
	created: number
	skippedByUrl: number
	skippedByContent: number
	skippedBySimilarity: number
	/** 关键词级的失败次数（限流不计入这里，限流会单独在 notes 里说明） */
	failed: number
	dryRun: boolean
	/** 分类 → 新增条数 */
	byCategory: Record<string, number>
	/** 每个关键词的执行情况，含命中缓存与限流说明 */
	notes: string[]
}
