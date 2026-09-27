import { http } from './request'

export interface CodeSnippetItem {
	id: number
	title: string
	/** 语言 / 分类：Java / Vue / AI / SQL / 工具 */
	language: string
	description: string
	tags: string[]
	/** 代码行数 */
	lineCount: number
	createTime: string
	updateTime: string
}

export interface CodeSnippetDetail extends Omit<CodeSnippetItem, 'tags'> {
	tags: string[]
	codeContent: string
}

export interface CodeSnippetSavePayload {
	title: string
	language: string
	description?: string
	codeContent: string
	tags?: string
}

export interface CodeSnippetQuery {
	keyword?: string
	language?: string
	page?: number
	size?: number
}

export function fetchCodeSnippets(query: CodeSnippetQuery = {}) {
	return http.get<import('./request').PageResult<CodeSnippetItem>>('/code-snippets', {
		keyword: query.keyword || undefined,
		language: query.language || undefined,
		page: query.page ?? 1,
		size: query.size ?? 20
	})
}

export function fetchCodeSnippet(id: number) {
	return http.get<CodeSnippetDetail>('/code-snippets/' + id)
}

export function createCodeSnippet(payload: CodeSnippetSavePayload) {
	return http.post<number>('/code-snippets', payload)
}

export function updateCodeSnippet(id: number, payload: CodeSnippetSavePayload) {
	return http.put<void>('/code-snippets/' + id, payload)
}

export function deleteCodeSnippet(id: number) {
	return http.delete<void>('/code-snippets/' + id)
}
