/**
 * AI Motion Designer 的数据类型（Phase 4）。
 *
 * 与后端 modules/motion/template/vo/MotionDesignVO 一一对应。
 */
import type { MotionIntent } from '@/extensions/motion-lab/types/recommend'
import type { MotionRecipe, RecipePerformance } from '@/extensions/motion-lab/types/workbench'

/** 一个参数的建议值 */
export interface DesignParam {
	key: string
	label: string
	unit: string
	/** 模板默认值 */
	defaultValue: number | null
	/** 本方案建议使用的值 */
	value: number | null
	/** 是否与默认值不同 */
	adjusted: boolean
	/** 调整原因（未调整时为空） */
	reason?: string | null
}

/** 方案里的一步 */
export interface DesignStep {
	order: number
	templateKey: string
	name: string
	/** 负责哪一层：背景 / 内容 / 滚动 / 交互 / 收尾 */
	stage: string
	/** 这一步的作用 */
	role: string
	params: DesignParam[]
	technology: string
	triggerLabel: string
	runtimeTier: 'LIGHTWEIGHT' | 'BALANCED' | 'GPU_ENHANCED'
	runtimeTierLabel: string
	performanceGrade: 'A' | 'B' | 'C'
}

/** 完整设计方案 */
export interface MotionDesign {
	query: string
	intent: MotionIntent
	/** 选中的组合方案（库里没有方案时为 null） */
	recipe: MotionRecipe | null
	animations: DesignStep[]
	performance: RecipePerformance | null
	/** 逐条设计说明：为什么这么搭、做了哪些调整 */
	notes: string[]
	explanation: string
	/** MODEL 由模型选方案与调参 / RULE 由内置规则设计 */
	source: 'MODEL' | 'RULE'
	modelName?: string | null
	fallbackReason?: string | null
}

/** 演示用的需求：覆盖 spec 点名的说法，另加几条偏性能与触发的 */
export const DESIGN_PRESETS: { label: string; query: string }[] = [
	{ label: '科技公司首页', query: '帮我设计一个科技公司首页' },
	{ label: '高级酒店官网', query: '做一个高级酒店官网，要有质感' },
	{ label: 'AI 产品落地页', query: 'AI 产品的落地页，科技感一点' },
	{ label: '游戏官网', query: '游戏官网，越炫越好' },
	{ label: '后台数据面板', query: '做一个后台数据面板' },
	{ label: '个人作品集', query: '个人作品集，滚动时慢慢出现' },
	{ label: '轻量版首页', query: '移动端优先的首页，要轻量一点' },
]

/** 性能预算选项：留空表示按需求里说的话判断 */
export const DESIGN_BUDGETS: { label: string; value: string }[] = [
	{ label: '按需求判断', value: '' },
	{ label: '轻量优先', value: 'LOW' },
	{ label: '均衡', value: 'MEDIUM' },
	{ label: '效果优先', value: 'HIGH' },
]
