import { http } from './request'

export type TechnologyCategory = 'Java' | 'Spring' | 'Database' | 'AI' | 'Frontend' | 'DevOps'

export interface TechnologyCategoryItem {
	/** ALL 表示全部 */
	category: string
	label: string
	count: number
}

export interface TechnologyItem {
	id: number
	name: string
	category: TechnologyCategory
	categoryLabel: string
	/** 掌握程度 1-5 */
	level: number
	/** 星星文字，例如 ★★★★☆ */
	levelText: string
	description: string
	usedProjects: string[]
	learnDate: string | null
	icon: string
	createTime: string
	updateTime: string
}

export interface TechnologyAtlas {
	categories: TechnologyCategoryItem[]
	items: TechnologyItem[]
	total: number
}

export interface TechnologySavePayload {
	name: string
	category: TechnologyCategory
	level?: number
	description?: string
	usedProjects?: string
	learnDate?: string | null
	icon?: string
}

export interface TechnologyQuery {
	category?: string
	keyword?: string
}

export function fetchTechnologyAtlas(query: TechnologyQuery = {}) {
	return http.get<TechnologyAtlas>('/technologies', {
		category: query.category && query.category !== 'ALL' ? query.category : undefined,
		keyword: query.keyword || undefined
	})
}

export function createTechnology(payload: TechnologySavePayload) {
	return http.post<number>('/technologies', payload)
}

export function updateTechnology(id: number, payload: TechnologySavePayload) {
	return http.put<void>('/technologies/' + id, payload)
}

export function deleteTechnology(id: number) {
	return http.delete<void>('/technologies/' + id)
}
