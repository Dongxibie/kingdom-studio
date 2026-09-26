/**
 * Motion Lab 工作台的数据类型（v1.1）。
 *
 * 与后端 modules/motion/template 的 VO 一一对应：
 *   MotionTemplateItemVO   → MotionTemplateItem
 *   MotionTemplateDetailVO → MotionTemplateDetail
 *   MotionRecipeVO         → MotionRecipe
 *   MotionFacetVO          → MotionFacets
 *   MotionAssistantVO      → MotionSearchResult
 */

/** 模板列表项 */
export interface MotionTemplateItem {
	id: number
	templateKey: string
	name: string
	nameEn: string
	description: string
	category: string
	scene: string
	/** 场景中文名，如「网站首页」 */
	sceneLabel: string
	style: string
	/** 风格中文名，如「高级」 */
	styleLabel: string
	technology: string
	difficulty: number
	/** 难度文案：入门 / 进阶 / 高阶 */
	difficultyLabel: string
	bestFor: string[]
	/** 推荐指数 0-100 */
	score: number
	/** 星级 0-5，取到半星 */
	stars: number
	/** 等级 S / A / B / C */
	grade: string
	/** 四项子分：解释推荐指数怎么来的 */
	scoreVisual: number
	scoreCode: number
	scoreReuse: number
	scorePerf: number
	tags: string[]
}

/** 可调参数：key 就是 CSS 变量名 */
export interface TemplateParam {
	key: string
	label: string
	unit: string
	min: number | null
	max: number | null
	step: number | null
	defaultValue: number | null
}

/** 模板详情 */
export interface MotionTemplateDetail extends MotionTemplateItem {
	previewHtml: string
	previewJs: string
	params: TemplateParam[]
	cssCode: string
	vueCode: string
	reactCode: string
	threeCode: string
	prompt: string
	/** 被哪些组合方案用到 */
	usedByRecipes: string[]
	manualScore: number | null
	manualReason: string | null
}

/** 组合方案 */
export interface MotionRecipe {
	id: number
	recipeKey: string
	name: string
	description: string
	scene: string
	sceneLabel: string
	style: string
	styleLabel: string
	bestFor: string[]
	score: number
	stars: number
	grade: string
	members: MotionTemplateItem[]
	memberKeys: string[]
	prompt: string
	manualScore: number | null
	manualReason: string | null
}

/** 发现方式的一个选项 */
export interface FacetOption {
	value: string
	label: string
	count: number
	averageScore: number
}

/** 发现方式分面 */
export interface MotionFacets {
	scenes: FacetOption[]
	styles: FacetOption[]
	technologies: FacetOption[]
	categories: FacetOption[]
	difficulties: FacetOption[]
	total: number
	recipeTotal: number
}

/** 检索命中的一条 */
export interface SearchMatch {
	key: string
	name: string
	kind: 'TEMPLATE' | 'RECIPE'
	scene: string
	style: string
	technology: string
	score: number
	stars: number
	grade: string
	reasons: string[]
}

/** 检索结果 */
export interface MotionSearchResult {
	query: string
	intent: {
		scene: string | null
		sceneLabel: string
		style: string | null
		styleLabel: string
		technology: string | null
		keywords: string[]
		note: string
	}
	matches: SearchMatch[]
	recipes: SearchMatch[]
	advice: string
}

/** 模板检索条件 */
export interface TemplateQuery {
	category?: string
	scene?: string
	style?: string
	technology?: string
	difficulty?: number
	keyword?: string
	sort?: 'SCORE' | 'NAME' | 'DIFFICULTY'
	page?: number
	size?: number
}

/** 纯 CSS 实现的动效默认调参范围：无法从后端拿到时（例如旧数据）用它兜底 */
export const FALLBACK_PARAMS: TemplateParam[] = [
	{ key: '--m-duration', label: '时长', unit: 's', min: 0.2, max: 3, step: 0.05, defaultValue: 1 },
	{ key: '--m-delay', label: '延迟', unit: 's', min: 0, max: 2, step: 0.05, defaultValue: 0 },
]

/** 新手引导的场景选项：对应后端 scene 值 */
export const ONBOARDING_SCENES: { value: string; label: string; hint: string }[] = [
	{ value: 'Landing Page', label: '网站首页', hint: '首屏、产品介绍、定价页' },
	{ value: 'Login', label: '登录页面', hint: '登录注册、表单引导' },
	{ value: 'Portfolio', label: '产品展示 / 个人主页', hint: '作品集、案例展示' },
	{ value: 'Dashboard', label: '数据后台', hint: '看板、报表、控制台' },
	{ value: 'AI SaaS', label: 'AI 产品页', hint: '能力介绍、订阅转化' },
]
