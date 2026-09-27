/**
 * 曲库与演奏方案的数据类型（产品化增强）。
 *
 * 与后端 modules/music/vo 下的 SongAnalysisVO / OptimizationVO / PerformancePresetVO 一一对应。
 */

/** 难度计分依据：命中才计分，未命中的也列出来，说明「为什么不是更难」 */
export interface DifficultyReason {
	label: string
	hit: boolean
}

/** 备选键位方案 */
export interface AnalysisAlternative {
	profileId: number
	profileName: string
	keyCount: number
	coversAll: boolean
	unmappedCount: number
}

/** 曲目分析卡 */
export interface SongAnalysis {
	taskId: number
	taskName: string
	difficultyStars: number
	difficultyLabel: string
	difficultyHint: string
	reasons: DifficultyReason[]
	tempoBpm: number
	timeSignature: string
	pitchRange: string
	pitchSpan: number
	noteCount: number
	estimatedDuration: number
	estimatedText: string
	chordRatio: number
	recommendedPresetName: string | null
	recommendedPresetNote: string | null
	recommendedProfileId: number | null
	recommendedProfileName: string | null
	mappedCount: number
	unmappedCount: number
	alternatives: AnalysisAlternative[]
}

/** 一条优化建议里可直接应用的参数 */
export interface OptimizationFix {
	minGapMs?: number | null
	speedScale?: number | null
	strategy?: string | null
	profileId?: number | null
	profileName?: string | null
}

/** 一条优化建议 */
export interface OptimizationFinding {
	code: string
	severity: 'WARN' | 'ADVICE' | 'INFO'
	title: string
	detail: string
	suggestion: string
	affected: number
	fix: OptimizationFix | null
}

/** 演奏优化报告 */
export interface OptimizationReport {
	taskId: number
	taskName: string
	strokeCount: number
	eventCount: number
	summary: string
	findings: OptimizationFinding[]
}

/** 演奏方案 */
export interface PerformancePreset {
	id: number
	taskId: number
	name: string
	profileId: number
	profileName: string
	keyCount: number | null
	strategy: string | null
	speedScale: number
	speedText: string
	minGapMs: number
	builtin: boolean
	note: string
	createTime: string | null
}

/** 新建 / 修改方案的入参 */
export interface PerformancePresetPayload {
	name: string
	profileId: number
	strategy?: string | null
	speedScale?: number
	minGapMs?: number
	note?: string
}

/** 把星级画成 ★★★☆☆ */
export function starsText(stars: number): string {
	return '★'.repeat(Math.max(0, Math.min(5, stars))) + '☆'.repeat(Math.max(0, 5 - stars))
}

/** 严重程度的中文名与色调 */
export const SEVERITY_META: Record<string, { label: string; tone: string }> = {
	WARN: { label: '建议处理', tone: 'warn' },
	ADVICE: { label: '可以更好', tone: 'advice' },
	INFO: { label: '仅说明', tone: 'info' },
}

/** 问题代码的中文名 */
export const FINDING_TITLES: Record<string, string> = {
	REPEAT_CONFLICT: '同键重按太密',
	OUT_OF_RANGE: '有音超出音域',
	CHORD: '多键和弦',
	DENSE: '按键过密',
	LONG_HOLD: '长按音',
}
