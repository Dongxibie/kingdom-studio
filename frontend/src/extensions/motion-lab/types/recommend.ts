/**
 * 智能推荐的数据类型（Phase 2）。
 *
 * 与后端 modules/motion/template/vo/MotionRecommendVO 一一对应；
 * 意图（MotionIntent）与 recommend 包里的同名类对应。
 */

/** 动效意图：一句话拆成五个可计算的轴，每个轴都可空 */
export interface MotionIntent {
	/** 场景：Landing Page / Dashboard / Portfolio / Login / AI SaaS / Game UI */
	scene: string | null
	/** 风格：Minimal / Luxury / Cyber / Glass / Organic */
	style: string | null
	/** 情绪：premium / tech / calm / playful / warm / bold */
	emotion: string | null
	/** 性能预算：LOW / MEDIUM / HIGH */
	performance: string | null
	/** 触发方式：load / hover / scroll / click */
	interaction: string | null
	/** 各轴的中文说法（只放识别到的轴） */
	labels: Record<string, string>
	/** 每一轴命中的关键词，用来解释「为什么这么理解」 */
	hits: Record<string, string[]>
	/** 一句话复述理解到的需求 */
	summary: string
}

/** 一条推荐 */
export interface MotionRecommendItem {
	templateKey: string
	name: string
	category: string
	description: string
	sceneLabel: string
	styleLabel: string
	technology: string
	triggerLabel: string
	runtimeTier: 'LIGHTWEIGHT' | 'BALANCED' | 'GPU_ENHANCED'
	runtimeTierLabel: string
	/** 性能等级：A 轻量 / B 均衡 / C 依赖 GPU 加速 */
	performanceGrade: 'A' | 'B' | 'C'
	/** 本次推荐得分（各轴加权求和） */
	score: number
	/** 模板自身的推荐指数 */
	recommendScore: number
	stars: number
	/** 命中的轴：scene / style / emotion / performance / interaction */
	matchedAxes: string[]
	/** 逐条理由 */
	reasons: string[]
}

/** 推荐结果：意图 + Top N */
export interface MotionRecommendResult {
	query: string
	intent: MotionIntent
	/** 参与打分的模板总数 */
	scanned: number
	recommendations: MotionRecommendItem[]
}

/** 演示用的一句话需求：覆盖 spec 里点名的五个场景，另加两个偏性能与触发的说法 */
export const RECOMMEND_PRESETS: { label: string; query: string }[] = [
	{ label: '科技感首页', query: '科技感首页' },
	{ label: '苹果官网风格', query: '苹果官网风格' },
	{ label: '高级酒店官网', query: '高级酒店官网' },
	{ label: '后台数据面板', query: '后台数据面板' },
	{ label: '游戏登录页面', query: '游戏登录页面' },
	{ label: '滚动视差作品集', query: '作品集，滚动时慢慢出现' },
	{ label: '移动端轻量首页', query: '移动端优先，首页动效要轻量一点' },
]

/** 五轴的中文名：界面上按这个顺序展示 */
export const INTENT_AXES: { axis: keyof MotionIntent; label: string }[] = [
	{ axis: 'scene', label: '场景' },
	{ axis: 'style', label: '风格' },
	{ axis: 'emotion', label: '情绪' },
	{ axis: 'performance', label: '性能' },
	{ axis: 'interaction', label: '触发' },
]

/** 性能等级的中文说明 */
export const PERFORMANCE_GRADE_HINT: Record<string, string> = {
	A: 'A 级 · 几乎无性能代价',
	B: 'B 级 · 桌面无压力，低端移动端注意数量',
	C: 'C 级 · 依赖 GPU 加速，建议桌面设备',
}
