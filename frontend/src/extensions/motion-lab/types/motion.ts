/**
 * 动效基因库的数据类型。
 *
 * 与后端实体 / VO 一一对应；Phase 3 采集器写入的数据也用同一套类型。
 */

/** 十个动效分类（与后端 CATEGORIES、数据库 CHECK 约束三处一致） */
export type MotionCategory =
	| 'Entrance'
	| 'Hover'
	| 'Scroll'
	| 'Text'
	| 'Particle'
	| '3D'
	| 'Glass'
	| 'Cursor'
	| 'Background'
	| 'Loading'

export type MotionStatus = 'DRAFT' | 'READY' | 'ARCHIVED'

export interface MotionCategoryMeta {
	key: MotionCategory
	label: string
	hint: string
}

export const MOTION_CATEGORIES: MotionCategoryMeta[] = [
	{ key: 'Entrance', label: '入场', hint: '元素出现：淡入、位移、错位、遮罩揭示' },
	{ key: 'Hover', label: '悬停', hint: '鼠标经过：上浮、倾斜、光影扫过、跟随' },
	{ key: 'Scroll', label: '滚动', hint: '滚动驱动：视差、逐级点亮、吸顶、进度' },
	{ key: 'Text', label: '文字', hint: '文字动效：打字机、逐字错位、描边、滚动字幕' },
	{ key: 'Particle', label: '粒子', hint: '粒子系统：星野、连线、流线、波纹' },
	{ key: '3D', label: '三维', hint: '空间感：透视、倾斜、景深、模型转动' },
	{ key: 'Glass', label: '玻璃', hint: '玻璃拟态：模糊、边框高光、厚度、折射' },
	{ key: 'Cursor', label: '光标', hint: '指针交互：跟随光、磁性吸附、拖尾、状态形变' },
	{ key: 'Background', label: '背景', hint: '背景氛围：网格、光斑、噪点、渐变流动' },
	{ key: 'Loading', label: '加载', hint: '加载与过渡：骨架屏、进度环、页面转场' },
]

/** 列表项（对应后端 MotionListItemVO，不含大段代码） */
export interface MotionListItem {
	id: number
	name: string
	description: string
	category: MotionCategory
	technology: string
	sourceUrl: string
	previewUrl: string
	tags: string[]
	license: string
	status: MotionStatus
	/** 是否已经生成过代码 */
	hasCode: boolean
	updateTime: string
}

/** 四种代码产物 + 提示词 */
export interface MotionCodeSet {
	prompt: string
	vueCode: string
	reactCode: string
	cssCode: string
	threeCode: string
}

/** 详情（对应后端 MotionDetailVO） */
export interface MotionDetail extends Omit<MotionListItem, 'hasCode'> {
	repoUrl: string
	codePath: string
	createTime: string
	code: MotionCodeSet | null
}

/** 分页结果（与后端 PageVO 一致） */
export interface PageResult<T> {
	records: T[]
	total: number
	page: number
	size: number
	pages: number
}

/** 列表查询条件 */
export interface MotionQuery {
	page: number
	size: number
	category?: MotionCategory | 'ALL'
	technology?: string
	keyword?: string
}

/** 新增 / 修改入参 */
export interface MotionSavePayload {
	name: string
	description: string
	category: MotionCategory
	technology: string
	sourceUrl: string
	repoUrl: string
	previewUrl: string
	tags: string
	license: string
	codePath: string
	status: MotionStatus
}

export const MOTION_TECHNOLOGIES = [
	'CSS',
	'CSS 3D',
	'Canvas',
	'WebGL',
	'Three.js',
	'GSAP',
	'Framer Motion',
	'Vue Motion',
	'JavaScript',
	'IntersectionObserver',
	'Lottie',
] as const

/** 代码面板的五个页签 */
export type MotionCodeTab = keyof MotionCodeSet

export const MOTION_CODE_TABS: { key: MotionCodeTab; label: string; lang: string }[] = [
	{ key: 'prompt', label: 'Prompt', lang: 'text' },
	{ key: 'vueCode', label: 'Vue 3', lang: 'vue' },
	{ key: 'reactCode', label: 'React', lang: 'tsx' },
	{ key: 'cssCode', label: 'CSS', lang: 'css' },
	{ key: 'threeCode', label: 'Three.js', lang: 'js' },
]

export function emptyCodeSet(): MotionCodeSet {
	return { prompt: '', vueCode: '', reactCode: '', cssCode: '', threeCode: '' }
}
