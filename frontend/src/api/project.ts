import { http } from './request'

/** 项目状态：库里存码，界面上展示 statusLabel */
export type ProjectStatus = 'PLANNING' | 'DEVELOPING' | 'COMPLETED'

export interface ProjectOutcome {
	codeLines: number | null
	testCount: number | null
	commitCount: number | null
}

export interface ProjectListItem extends ProjectOutcome {
	id: number
	name: string
	description: string
	coverImage: string
	technologyStack: string[]
	githubUrl: string
	demoUrl: string
	status: ProjectStatus
	statusLabel: string
	progress: number
	sortOrder: number
	createTime: string
	updateTime: string
}

export interface ProjectDetail extends ProjectListItem {
	/** 项目亮点，Markdown 文本 */
	highlights: string | null
}

export interface ProjectSavePayload {
	name: string
	description?: string
	coverImage?: string
	technologyStack?: string
	githubUrl?: string
	demoUrl?: string
	status?: ProjectStatus
	progress?: number
	codeLines?: number | null
	testCount?: number | null
	commitCount?: number | null
	highlights?: string
	sortOrder?: number
}

export interface ProjectQuery {
	keyword?: string
	status?: ProjectStatus | ''
	page?: number
	size?: number
}

export function fetchProjects(query: ProjectQuery = {}) {
	return http.get<import('./request').PageResult<ProjectListItem>>('/projects', {
		keyword: query.keyword || undefined,
		status: query.status || undefined,
		page: query.page ?? 1,
		size: query.size ?? 12
	})
}

export function fetchProject(id: number) {
	return http.get<ProjectDetail>('/projects/' + id)
}

export function createProject(payload: ProjectSavePayload) {
	return http.post<number>('/projects', payload)
}

export function updateProject(id: number, payload: ProjectSavePayload) {
	return http.put<void>('/projects/' + id, payload)
}

export function deleteProject(id: number) {
	return http.delete<void>('/projects/' + id)
}
