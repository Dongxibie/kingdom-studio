import { http } from '@/api/request'
import type { PageResult } from '@/extensions/motion-lab/types/motion'
import type { CandidateQuery, CandidateStats, MotionCandidate } from '@/extensions/motion-lab/types/candidate'
import type {
	MotionFacets,
	MotionRecipe,
	MotionSearchResult,
	MotionTemplateDetail,
	MotionTemplateItem,
	TemplateQuery,
} from '@/extensions/motion-lab/types/workbench'
import type { MotionRecommendResult } from '@/extensions/motion-lab/types/recommend'

/** 工作台接口前缀（后端 context-path 是 /api） */
export const TEMPLATE_BASE = '/motion/templates'
export const CANDIDATE_BASE = '/motion/candidates'

/** 候选池分页：状态 / 分类 / 技术 / 关键词 / 星数下限任意组合 */
export function listCandidates(query: CandidateQuery): Promise<PageResult<MotionCandidate>> {
	return http.get<PageResult<MotionCandidate>>(CANDIDATE_BASE, { ...query })
}

/** 候选池概览：总数、各状态、各分类，以及资源库的官方与社区配比 */
export function fetchCandidateStats(): Promise<CandidateStats> {
	return http.get<CandidateStats>(CANDIDATE_BASE + '/stats')
}

/** 重新跑一遍规则分析（分类 / 触发 / 难度 / 档位） */
export function analyzeCandidate(id: number): Promise<MotionCandidate> {
	return http.post<MotionCandidate>(CANDIDATE_BASE + '/' + id + '/analyze')
}

/** 人工筛选：改状态 + 写意见（淘汰必须写理由） */
export function reviewCandidate(id: number, status: string, note: string): Promise<MotionCandidate> {
	return http.post<MotionCandidate>(CANDIDATE_BASE + '/' + id + '/review', { status, note })
}

/** 转成模板：指定用哪个内置 Pattern 承载，代码来自 Pattern，候选只提供名字、来源与许可 */
export function promoteCandidate(id: number, patternTemplateKey: string, name?: string): Promise<Record<string, unknown>> {
	return http.post<Record<string, unknown>>(CANDIDATE_BASE + '/' + id + '/promote', { patternTemplateKey, name })
}

/** 模板分页：分组 / 场景 / 风格 / 技术 / 难度 / 关键词任意组合 */
export function listTemplates(query: TemplateQuery): Promise<PageResult<MotionTemplateItem>> {
	return http.get<PageResult<MotionTemplateItem>>(TEMPLATE_BASE, { ...query })
}

/** 发现方式：场景 / 风格 / 技术 / 分组 / 难度各自的选项与数量 */
export function fetchFacets(): Promise<MotionFacets> {
	return http.get<MotionFacets>(TEMPLATE_BASE + '/facets')
}

/** 搜索栏检索：返回识别到的意图 + 带命中理由的推荐 */
export function searchMotions(query: string, limit = 6): Promise<MotionSearchResult> {
	return http.post<MotionSearchResult>(TEMPLATE_BASE + '/search', { query, limit })
}

/**
 * Motion Assistant：优先由模型分析需求并给组合方案，模型不可用时后端自动回退到内置检索。
 * 返回里的 source 字段说明这次结果来自模型还是检索。
 */
export function assistMotions(query: string, limit = 6): Promise<MotionSearchResult> {
	return http.post<MotionSearchResult>(TEMPLATE_BASE + '/assist', { query, limit })
}

/**
 * 智能推荐：一句话 → 五轴意图 → Top N，每条带命中理由与性能等级。
 * 规则 + 权重，不调用模型，所以同一句输入每次结果都一样。
 */
export function recommendMotions(query: string, limit = 5): Promise<MotionRecommendResult> {
	return http.post<MotionRecommendResult>(TEMPLATE_BASE + '/recommend', { query, limit })
}

/** 组合方案列表 */
export function listRecipes(scene?: string): Promise<MotionRecipe[]> {
	return http.get<MotionRecipe[]>(TEMPLATE_BASE + '/recipes', scene ? { scene } : undefined)
}

export function getRecipe(recipeKey: string): Promise<MotionRecipe> {
	return http.get<MotionRecipe>(TEMPLATE_BASE + '/recipes/' + recipeKey)
}

/** 模板详情：含预览结构、可调参数与五类代码 */
export function getTemplate(templateKey: string): Promise<MotionTemplateDetail> {
	return http.get<MotionTemplateDetail>(TEMPLATE_BASE + '/' + templateKey)
}

/** 人工评分（1-5 星）与理由 */
export function rateMotion(targetType: 'TEMPLATE' | 'RECIPE', targetKey: string, score: number, reason: string): Promise<void> {
	return http.post<void>(TEMPLATE_BASE + '/ratings', { targetType, targetKey, score, reason })
}
