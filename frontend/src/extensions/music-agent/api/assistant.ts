import { http } from '@/api/request'
import type { MusicAssistantAnswer } from '@/extensions/music-agent/types/assistant'

const BASE = (taskId: number) => `/music/tasks/${taskId}/assistant`

/** 理解一句要求：返回难度档、目标乐器、建议与逐条改动原因 */
export function analyzePrompt(taskId: number, prompt: string): Promise<MusicAssistantAnswer> {
	return http.post<MusicAssistantAnswer>(BASE(taskId) + '/analyze', { prompt })
}

/** 应用方案：把调整后的曲子落成一条新曲目 */
export function applyStrategy(taskId: number, strategy: string): Promise<MusicAssistantAnswer> {
	return http.post<MusicAssistantAnswer>(BASE(taskId) + '/apply', { strategy })
}
