/**
 * 音乐 Agent 的数据类型，与后端 modules/music 的 VO / DTO 一一对应。
 *
 * 第一版范围（已确认）：MIDI 解析 + 简谱输入 + 音符时间线 + Instrument Profile 映射
 * + 按键序列导出 + Demo 回放。MP3 音频分析留到后续阶段（需要 Python 服务）。
 */

export type MusicSource = 'MIDI' | 'JIANPU' | 'SHARE'

/** 来源标签：分享导入的曲目要能一眼看出来，不能混进「简谱」里 */
export function sourceLabel(source: MusicSource): string {
	if (source === 'MIDI') return 'MIDI'
	if (source === 'SHARE') return '分享'
	return '简谱'
}

/** 映射方式：半音排列 / 音阶排列 / 自定义 */
export type MappingMode = 'CHROMATIC' | 'DIATONIC' | 'CUSTOM'

/** 超范围策略：跳过 / 就近落键 / 移八度 */
export type UnmappedStrategy = 'SKIP' | 'NEAREST' | 'SHIFT_OCTAVE'

/** 任务列表项 */
export interface MusicTaskListItem {
	id: number
	name: string
	sourceType: MusicSource
	sourceRef: string
	noteCount: number
	tempoBpm: number
	timeSignature: string
	durationMs: number
	/** 音域，如 C4–C6；没有音符时是「—」 */
	pitchRange: string
	status: string
	/** 收藏标记：1 收藏 / 0 普通 */
	favorite: number
	/** 难度星级 1-5（与曲目分析卡同一套规则） */
	difficultyStars: number
	/** 难度文字：入门 / 简单 / 进阶 / 较难 / 挑战 */
	difficultyLabel: string
	/** 难度分层：EASY 简单 / NORMAL 普通 / ADVANCED 高级 */
	difficultyTier: string
	createTime: string
}

/** 单个音符 */
export interface MusicNote {
	seqNo: number
	startMs: number
	durationMs: number
	pitch: number
	noteName: string
	velocity: number
	trackNo: number
	/** 相对全曲最低音的半音数：画音高条时用它定高度 */
	pitchOffset: number
}

/** 任务详情 */
export interface MusicTaskDetail {
	id: number
	name: string
	sourceType: MusicSource
	sourceRef: string
	noteCount: number
	tempoBpm: number
	timeSignature: string
	durationMs: number
	pitchLow: number
	pitchHigh: number
	pitchRange: string
	status: string
	createTime: string
	notes: MusicNote[]
}

/** 一次按键动作 */
export interface KeyStroke {
	seq: number
	startMs: number
	durationMs: number
	/** 需要同时按下的键 */
	keys: string[]
	/** 对应的音名，被调整过的写成「原音→实际音」 */
	noteNames: string[]
	/** 被调整过的组才带原始音高 */
	originalPitches: number[] | null
	chord: boolean
	adjusted: boolean
}

/** 未能落键的音 */
export interface UnmappedNote {
	pitch: number
	noteName: string
	startMs: number
	reason: string
}

/** 按键序列：映射引擎的产物 */
export interface KeySequence {
	taskId: number
	taskName: string
	profileId: number
	profileName: string
	strategy: UnmappedStrategy
	keyCount: number
	keyLayout: string[]
	/** 与 keyLayout 一一对应的音高号 */
	keyPitches: number[]
	noteCount: number
	mappedCount: number
	adjustedCount: number
	unmappedCount: number
	strokes: KeyStroke[]
	unmapped: UnmappedNote[]
	/** 纯文本导出 */
	exportText: string
}

/** 乐器按键档案 */
export interface InstrumentProfile {
	id: number
	name: string
	/** 所属游戏：通用 / 光遇 Sky / Minecraft / 三角洲行动 / 自定义 */
	game: string
	instrument: string
	mappingMode: MappingMode
	scale: string
	keyLayout: string[]
	keyPitches: number[]
	keyNoteNames: string[]
	basePitch: number
	transpose: number
	octaveShift: number
	unmappedStrategy: UnmappedStrategy
	/** 覆盖音域，例如 C4–C6 */
	octaveRange: string
	description: string
	/** 这个游戏乐器的特殊规则 */
	specialRules: string
	status: string
	createTime: string
}

/** 乐器档案保存入参 */
export interface InstrumentProfilePayload {
	/** 所属游戏 */
	game?: string
	/** 覆盖音域 */
	octaveRange?: string
	/** 特殊规则 */
	specialRules?: string
	name: string
	instrument: string
	mappingMode: MappingMode
	scale: string
	keyLayout: string[]
	basePitch: number
	transpose: number
	octaveShift: number
	unmappedStrategy: UnmappedStrategy
	description: string
	status: string
}

/** 分页结果 */
export interface PageResult<T> {
	records: T[]
	total: number
	page: number
	size: number
	pages: number
}

/** 超范围策略的中文名：界面与导出说明共用一份，避免两处写得不一样 */
export const STRATEGY_LABELS: Record<UnmappedStrategy, string> = {
	SKIP: '跳过',
	NEAREST: '就近落键',
	SHIFT_OCTAVE: '移八度',
}

/** 映射方式的中文名 */
export const MODE_LABELS: Record<MappingMode, string> = {
	CHROMATIC: '半音排列',
	DIATONIC: '音阶排列',
	CUSTOM: '自定义',
}
