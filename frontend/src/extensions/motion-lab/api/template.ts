import { http } from '@/api/request'
import type { PageResult } from '@/extensions/motion-lab/types/motion'
import type {
	MotionFacets,
	MotionRecipe,
	MotionSearchResult,
	MotionTemplateDetail,
	MotionTemplateItem,
	TemplateQuery,
} from '@/extensions/motion-lab/types/workbench'

/** 工作台接口前缀（后端 context-path 是 /api） */
export const TEMPLATE_BASE = '/motion/templates'

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
