import { http } from '@/api/request'
import { fetchModuleInfo } from '@/extensions/_shared/api/ping'
import type { ExtModuleInfo } from '@/extensions/_shared/types/common'
import type {
	MotionCodeSet,
	MotionDetail,
	MotionListItem,
	MotionQuery,
	MotionSavePayload,
	PageResult,
} from '@/extensions/motion-lab/types/motion'

/** Motion Lab 的接口前缀（后端 context-path 是 /api，由 Vite 代理过去） */
export const MOTION_API_BASE = '/motion'

/** 模块自检：确认前端能调到 motion 模块 */
export function fetchMotionModuleInfo(): Promise<ExtModuleInfo> {
	return fetchModuleInfo(MOTION_API_BASE)
}

/** 分页列表：分类 / 技术栈 / 关键词可组合 */
export function listMotions(query: MotionQuery): Promise<PageResult<MotionListItem>> {
	return http.get<PageResult<MotionListItem>>(MOTION_API_BASE, { ...query })
}

/** 详情：含四种代码与提示词 */
export function getMotion(id: number): Promise<MotionDetail> {
	return http.get<MotionDetail>(MOTION_API_BASE + '/' + id)
}

export function createMotion(payload: MotionSavePayload): Promise<number> {
	return http.post<number>(MOTION_API_BASE, payload)
}

export function updateMotion(id: number, payload: MotionSavePayload): Promise<void> {
	return http.put<void>(MOTION_API_BASE + '/' + id, payload)
}

export function deleteMotion(id: number): Promise<void> {
	return http.delete<void>(MOTION_API_BASE + '/' + id)
}

/** 保存代码产物（一条资源一条记录，后端做 upsert） */
export function saveMotionCode(id: number, code: Partial<MotionCodeSet>): Promise<void> {
	return http.put<void>(MOTION_API_BASE + '/' + id + '/code', code)
}
