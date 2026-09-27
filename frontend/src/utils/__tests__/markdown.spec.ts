import { describe, expect, it } from 'vitest'
import { markdownSummary, renderMarkdown } from '@/utils/markdown'

/**
 * 项目亮点的 Markdown 渲染。
 *
 * 重点在安全：亮点是用户自己填的文本，最终走 v-html，所以先转义再拼标签这件事必须有测试守着。
 */
describe('Markdown 渲染（项目亮点）', () => {
	it('标题、列表、加粗与行内代码都能渲染', () => {
		const html = renderMarkdown('### 核心能力\n- 支持 **Markdown**\n- 支持 `行内代码`\n\n1. 第一步')

		expect(html).toContain('<h3>核心能力</h3>')
		expect(html).toContain('<ul>')
		expect(html).toContain('<strong>Markdown</strong>')
		expect(html).toContain('<code>行内代码</code>')
		expect(html).toContain('<ol>')
		expect(html).toContain('<li>第一步</li>')
	})

	it('脚本与标签被转义，不会变成可执行 HTML', () => {
		const html = renderMarkdown('<script>alert(1)</script>\n<img src=x onerror=alert(1)>')

		expect(html).not.toContain('<script>')
		expect(html).not.toContain('<img')
		expect(html).toContain('&lt;script&gt;')
	})

	it('空内容返回空串，不产生多余标签', () => {
		expect(renderMarkdown(null)).toBe('')
		expect(renderMarkdown('   ')).toBe('')
	})

	it('标题层级最多到四级，再深也不会生成 h5/h6', () => {
		expect(renderMarkdown('##### 很深的标题')).toContain('<h4>')
	})

	it('摘要去掉标记符号，保留可读文字', () => {
		const summary = markdownSummary('### 核心能力\n- 支持 **Markdown**\n- 支持 `行内代码`')

		expect(summary).toBe('核心能力 · 支持 Markdown · 支持 行内代码')
	})
})
