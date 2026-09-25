import request, { http } from '@/api/request'
import type { CrawlRequest, CrawlResult } from '@/extensions/motion-lab/types/crawl'

const CRAWL_BASE = '/motion/crawl'

/** 后端内置的默认关键词，供界面预填 */
export function fetchCrawlKeywords(): Promise<string[]> {
	return http.get<string[]>(CRAWL_BASE + '/keywords')
}

/**
 * 执行采集。
 *
 * 这里特意用 axios 实例而不是 http 封装：采集会真的打 GitHub，耗时以秒计
 * （关键词之间还有固定间隔，限流后还要重试），10 秒的默认超时不够用。
 * 拦截器同样生效，返回值仍是剥过壳的 data。
 */
export function runCrawl(payload: CrawlRequest): Promise<CrawlResult> {
	return request.post(CRAWL_BASE, payload, { timeout: 180000 }) as unknown as Promise<CrawlResult>
}
