import { http } from './request'

export interface TimelineNode {
	id: number
	year: number
	/** 精确到日的时间点，年度节点为 null */
	eventDate: string | null
	/** 展示用时间文字：有日期用日期，否则用年份 */
	timeText: string
	title: string
	description: string
	relatedProject: string
	/** 阶段等级 1-5 */
	level: number
	sortOrder: number
	createTime: string
}

export interface TimelineSavePayload {
	year: number
	eventDate?: string | null
	title: string
	description?: string
	relatedProject?: string
	level?: number
	sortOrder?: number
}

export function fetchTimeline(year?: number) {
	return http.get<TimelineNode[]>('/timeline', { year })
}

export function createTimelineNode(payload: TimelineSavePayload) {
	return http.post<number>('/timeline', payload)
}

export function updateTimelineNode(id: number, payload: TimelineSavePayload) {
	return http.put<void>('/timeline/' + id, payload)
}

export function deleteTimelineNode(id: number) {
	return http.delete<void>('/timeline/' + id)
}
