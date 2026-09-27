import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'
import ElementPlus from 'element-plus'
import ProjectKingdomView from '@/views/ProjectKingdomView.vue'
import { fetchProjects, type ProjectListItem } from '@/api/project'

/**
 * 项目王国的关键流程：打开列表 → 进入详情。
 *
 * 接口层整体替换掉：这条测试要盯的是「列表渲染出来、点项目名能跳到详情路由」，
 * 不是后端返回了什么。
 */
// 这条用例要挂真实组件，所以保留 element-plus 的其余导出，只替换消息与确认框
vi.mock('element-plus', async importOriginal => {
	const actual = await importOriginal<typeof import('element-plus')>()
	return {
		...actual,
		ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn() },
		ElMessageBox: { confirm: vi.fn(() => Promise.resolve('confirm')) }
	}
})

vi.mock('@/api/project', () => ({
	fetchProjects: vi.fn(),
	createProject: vi.fn(),
	updateProject: vi.fn(),
	deleteProject: vi.fn(),
	fetchProject: vi.fn()
}))

const project: ProjectListItem = {
	id: 7,
	name: 'Kingdom Studio',
	description: '个人开发者工作台',
	coverImage: '',
	technologyStack: ['Java', 'Vue3'],
	githubUrl: 'https://github.com/Dongxibie/kingdom-studio',
	demoUrl: '',
	status: 'DEVELOPING',
	statusLabel: '持续开发',
	progress: 60,
	codeLines: 30955,
	testCount: 333,
	commitCount: 17,
	sortOrder: 1,
	createTime: '2026-09-22 10:00:00',
	updateTime: '2026-09-27 10:00:00'
}

function buildRouter() {
	return createRouter({
		history: createMemoryHistory(),
		routes: [
			{ path: '/', redirect: '/projects' },
			{ path: '/projects', name: 'ProjectKingdom', component: ProjectKingdomView },
			{ path: '/projects/:id', name: 'ProjectDetail', component: { template: '<div>详情占位</div>' } }
		]
	})
}

describe('项目王国：打开列表 → 进入详情', () => {
	beforeEach(() => {
		vi.mocked(fetchProjects).mockResolvedValue({
			records: [project],
			total: 1,
			page: 1,
			size: 12,
			pages: 1
		})
	})

	it('列表渲染出项目名、状态与成果数字', async () => {
		const router = buildRouter()
		await router.push('/projects')
		const wrapper = mount(ProjectKingdomView, { global: { plugins: [router, ElementPlus] } })
		await flushPromises()

		expect(wrapper.text()).toContain('Kingdom Studio')
		expect(wrapper.text()).toContain('持续开发')
		expect(wrapper.text()).toContain('30955 行代码')
		expect(wrapper.text()).toContain('333 个测试')
	})

	it('点项目名跳到该项目详情路由', async () => {
		const router = buildRouter()
		await router.push('/projects')
		const wrapper = mount(ProjectKingdomView, { global: { plugins: [router, ElementPlus] } })
		await flushPromises()

		expect(wrapper.findAll('.project-card')).toHaveLength(1)
		await wrapper.find('.project-name').trigger('click')
		await flushPromises()

		expect(router.currentRoute.value.path).toBe('/projects/7')
	})

	it('状态筛选会带上对应的查询条件重新请求', async () => {
		const router = buildRouter()
		await router.push('/projects')
		const wrapper = mount(ProjectKingdomView, { global: { plugins: [router, ElementPlus] } })
		await flushPromises()

		const completed = wrapper.findAll('.status-chip').find(chip => chip.text() === '已完成')
		expect(completed).toBeTruthy()
		await completed!.trigger('click')
		await flushPromises()

		expect(fetchProjects).toHaveBeenLastCalledWith(expect.objectContaining({ status: 'COMPLETED' }))
	})
})
