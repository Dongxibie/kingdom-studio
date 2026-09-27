/** 演奏宏导出：与后端 Performance Macro Export 层一一对应 */

/** 三种可导出格式 */
export const MACRO_FORMATS = [
	{ value: 'AHK', label: 'AutoHotkey 脚本', hint: '给本机自动演奏测试用：双击运行后按 F9 开始，F8 暂停，ESC 急停' },
	{ value: 'TXT', label: 'TXT 按键时间线', hint: '给人看的版本，照着练或打印都行' },
	{ value: 'JSON', label: 'JSON 演奏计划', hint: '给程序读的结构化事件流（按下 / 松开 + 时间）' },
] as const

export type MacroFormat = (typeof MACRO_FORMATS)[number]['value']

/** 一个按键事件 */
export interface PlanNote {
	key: string
	action: 'DOWN' | 'UP'
	timestamp: number
	strokeSeq?: number | null
}

export interface PerformancePlan {
	id: number
	taskId: number
	profileId: number
	taskName: string
	profileName: string
	strategy: string
	/** 整首时长（ms） */
	duration: number
	noteCount: number
	strokeCount: number
	keyCount: number
	keys: string[]
	warnings: string[]
	notes: PlanNote[]
	/** 事件流是否被截断（完整内容走导出接口） */
	truncated: boolean
	formats: string[]
	createTime?: string
}

export interface MacroExport {
	planId: number
	taskId: number
	format: string
	filename: string
	contentType: string
	size: number
	content: string
	hint: string
}

export interface ExecutionMode {
	mode: 'PREVIEW' | 'MANUAL' | 'LOCAL'
	label: string
	available: boolean
	reason: string
}

export interface ExecutionCommand {
	seq: number
	key: string
	action: 'DOWN' | 'UP'
	atMs: number
	holdMs?: number | null
	strokeSeq?: number | null
}

/** 本机演奏状态：READY / RUNNING / PAUSED / STOPPED / FINISHED */
export interface ExecutionStatus {
	taskId?: number | null
	taskName?: string
	profileName?: string
	status: 'READY' | 'RUNNING' | 'PAUSED' | 'STOPPED' | 'FINISHED'
	statusLabel: string
	progress: number
	currentKey: string
	executedCount: number
	commandCount: number
	elapsedMs: number
	duration: number
	remainingMs: number
	heldKeys: string[]
	targetWindow: string
	currentWindow: string
	guardAlive: boolean
	injector: string
	warnings: string[]
	stopReason: string
}

/** 本机演奏环境自检 */
export interface RuntimePreflight {
	enabled: boolean
	currentWindow: string
	injector: string
	guardAvailable: boolean
	ready: boolean
	reason: string
	requirements: string[]
	running: boolean
}

export interface ExecutionResult {
	mode: string
	modeLabel: string
	accepted: boolean
	message: string
	commandCount: number
	duration: number
	commands: ExecutionCommand[]
	safety: string[]
}
