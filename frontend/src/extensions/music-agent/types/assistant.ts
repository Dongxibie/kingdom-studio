/** AI 音乐助手：与后端 MusicAssistantVO 一一对应 */

export interface AssistantChange {
	type: string
	title: string
	detail: string
	before: string
	after: string
}

export interface AssistantIntent {
	difficulty: 'BEGINNER' | 'NORMAL' | 'SHOWCASE'
	difficultyLabel: string
	instrument: string | null
	suggestions: string[]
}

export interface AssistantPreview {
	noteCountBefore: number
	noteCountAfter: number
	tempoBefore: number
	tempoAfter: number
	durationBefore: number
	durationAfter: number
}

export interface MusicAssistantAnswer {
	taskId: number
	taskName: string
	/** MODEL 模型分析 / RULE 规则判断 */
	source: 'MODEL' | 'RULE'
	modelName: string | null
	fallbackReason: string | null
	intent: AssistantIntent
	explanation: string
	adjustments: AssistantChange[]
	preview: AssistantPreview
}

/** 快捷说法：点一下就等于说了这句话 */
export const PROMPT_PRESETS = [
	'让它听起来简单一点',
	'做一个适合游戏展示的版本',
	'帮我把这首歌变成适合 15 键口风琴演奏',
] as const
