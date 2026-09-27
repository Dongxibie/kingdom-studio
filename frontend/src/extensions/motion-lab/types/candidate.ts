/** 候选池：GitHub 发现 → 规则分析 → 人工筛选 → 转成 Motion Pattern 的类型定义 */

export interface MotionCandidate {
	id: number
	candidateKey: string
	name: string
	fullName: string
	sourceUrl: string
	description: string
	/** 建议分类（七类之一） */
	category: string
	technology: string
	triggerType: string
	triggerLabel: string
	difficulty: number
	difficultyLabel: string
	performanceLevel: string
	performanceLabel: string
	visualScore: number
	visualStars: number
	license: string
	stars: number
	language: string
	topics: string[]
	/** 命中的分类关键词：解释为什么分到这一类 */
	matchedHints: string[]
	prompt: string
	status: CandidateStatus
	statusLabel: string
	reviewNote: string
	patternKey: string
	promotedTemplateKey: string
}

export type CandidateStatus = 'NEW' | 'ANALYZED' | 'SELECTED' | 'PROMOTED' | 'REJECTED'

/** 可人工改成的状态；PROMOTED 由转换接口产生 */
export const REVIEWABLE_STATUS: { value: CandidateStatus; label: string }[] = [
	{ value: 'ANALYZED', label: '已分析' },
	{ value: 'SELECTED', label: '选入' },
	{ value: 'REJECTED', label: '淘汰' },
	{ value: 'NEW', label: '放回待看' },
]

export interface CandidateQuery {
	status?: string
	category?: string
	technology?: string
	keyword?: string
	minStars?: number
	sort?: 'STARS' | 'VISUAL' | 'NAME'
	page?: number
	size?: number
}

export interface CandidateStats {
	total: number
	byStatus: Record<string, number>
	byCategory: Record<string, number>
	libraryTotal: number
	officialTotal: number
	communityTotal: number
}
