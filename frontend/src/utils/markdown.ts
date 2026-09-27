/**
 * 极简 Markdown 渲染：只支持项目亮点里实际会写的那几种写法。
 *
 * 支持的语法：# ~ #### 标题、- / * 无序列表、1. 有序列表、**加粗**、`行内代码`、
 * 空行分段。不支持的语法会原样显示，不会被当成 HTML。
 *
 * 安全：先整体转义 HTML 再拼标签 —— 亮点是用户自己填的文本，直接 v-html 等于把 XSS 口子留着。
 */

/** 支持的标题层级上限：再深的标题在卡片里显示不出来 */
const MAX_HEADING = 4

export function escapeHtml(source: string): string {
	return source
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;')
		.replace(/"/g, '&quot;')
		.replace(/'/g, '&#39;')
}

/** 行内语法：加粗与行内代码；输入必须是已经转义过的文本 */
function renderInline(text: string): string {
	return text
		.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
		.replace(/`([^`]+)`/g, '<code>$1</code>')
}

export function renderMarkdown(source: string | null | undefined): string {
	if (!source || !source.trim()) {
		return ''
	}
	const lines = escapeHtml(source).split(/\r?\n/)
	const html: string[] = []
	let listType: 'ul' | 'ol' | null = null

	const closeList = () => {
		if (listType) {
			html.push('</' + listType + '>')
			listType = null
		}
	}

	for (const rawLine of lines) {
		const line = rawLine.trim()
		if (!line) {
			closeList()
			continue
		}

		const heading = /^(#{1,6})\s+(.*)$/.exec(line)
		if (heading) {
			closeList()
			const level = Math.min(heading[1].length, MAX_HEADING)
			html.push('<h' + level + '>' + renderInline(heading[2].trim()) + '</h' + level + '>')
			continue
		}

		const unordered = /^[-*]\s+(.*)$/.exec(line)
		if (unordered) {
			if (listType !== 'ul') {
				closeList()
				html.push('<ul>')
				listType = 'ul'
			}
			html.push('<li>' + renderInline(unordered[1].trim()) + '</li>')
			continue
		}

		const ordered = /^\d+\.\s+(.*)$/.exec(line)
		if (ordered) {
			if (listType !== 'ol') {
				closeList()
				html.push('<ol>')
				listType = 'ol'
			}
			html.push('<li>' + renderInline(ordered[1].trim()) + '</li>')
			continue
		}

		closeList()
		html.push('<p>' + renderInline(line) + '</p>')
	}

	closeList()
	return html.join('')
}

/** 亮点纯文本摘要：列表页只想知道「写了几条」，不需要渲染 */
export function markdownSummary(source: string | null | undefined): string {
	if (!source) {
		return ''
	}
	return source
		.replace(/^#{1,6}\s*/gm, '')
		.replace(/^[-*]\s+/gm, '')
		.replace(/\*\*/g, '')
		.replace(/`/g, '')
		.split(/\r?\n/)
		.map(line => line.trim())
		.filter(Boolean)
		.join(' · ')
}
