import { http } from '@/api/request'
import type { ExecutionResult, MacroExport, PerformancePlan } from '@/extensions/music-agent/types/macro'

/** 演奏宏导出接口前缀（按任务挂在既有的音乐任务下面） */
const BASE = (taskId: number) => `/music/tasks/${taskId}/performance-plan`

/**
 * 生成（或重新生成）演奏计划：按键序列 → 校验过的命令流 → 按键事件流。
 * 三种导出格式都读这一份计划，所以同一个曲子每次导出结果一致。
 */
export function generatePerformancePlan(
	taskId: number,
	profileId: number,
	strategy?: string,
): Promise<PerformancePlan> {
	return http.post<PerformancePlan>(BASE(taskId), { profileId, strategy })
}

/** 最近一次生成的计划 */
export function fetchPerformancePlan(taskId: number): Promise<PerformancePlan> {
	return http.get<PerformancePlan>(BASE(taskId))
}

/** 导出脚本：TXT / AHK / JSON */
export function exportMacro(taskId: number, format: string): Promise<MacroExport> {
	return http.get<MacroExport>(BASE(taskId) + '/export', { format })
}

/** 执行模式清单：每个模式可不可用、不可用时为什么 */
export function fetchExecutionModes(taskId: number): Promise<import('@/extensions/music-agent/types/macro').ExecutionMode[]> {
	return http.get(BASE(taskId) + '/modes')
}

/** 执行（默认仅模拟：列出将要发出的命令，不产生任何真实按键输入） */
export function executePlan(taskId: number, mode = 'PREVIEW'): Promise<ExecutionResult> {
	return http.post<ExecutionResult>(BASE(taskId) + '/execute', { mode })
}
