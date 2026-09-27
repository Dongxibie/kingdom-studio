import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import MotionExportPanel from '@/extensions/motion-lab/components/MotionExportPanel.vue'
import { exportMotions } from '@/extensions/motion-lab/api/template'

/**
 * 动效工作台的关键流程：选好方案 → 生成代码 → 下载。
 *
 * 出码接口整体替换：这条测试盯的是「点生成之后文件列表出现、点下载真的触发浏览器下载」。
 * 面板里的代码高亮组件用 stub 顶掉，它不属于这条链路。
 */
vi.mock('element-plus', () => ({
	ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn() }
}))

vi.mock('@/extensions/motion-lab/api/template', () => ({
	exportMotions: vi.fn()
}))

const exportResult = {
	planName: 'Apple 产品页',
	format: 'VUE',
	files: [
		{ path: 'MotionHero.vue', language: 'vue', content: '<template><div class="motion-hero" /></template>', bytes: 46 },
		{ path: 'motion-core.css', language: 'css', content: '.motion-hero { animation: rise 0.6s ease-out; }', bytes: 48 }
	],
	notes: []
}

describe('动效工作台：选择方案 → 生成代码 → 下载', () => {
	beforeEach(() => {
		vi.mocked(exportMotions).mockResolvedValue(exportResult as never)
	})

	it('点生成拿到文件列表，点打包下载触发 ZIP 下载', async () => {
		const createObjectURL = vi.spyOn(URL, 'createObjectURL')
		const wrapper = mount(MotionExportPanel, {
			props: { recipeKey: 'apple-product-page', planName: 'Apple 产品页' },
			global: { stubs: { ExtCodeBlock: true } }
		})

		// 一开始没有产物，下载按钮不该出现
		expect(wrapper.findAll('button').some(button => button.text().includes('打包下载'))).toBe(false)

		await wrapper.find('.mexp-run').trigger('click')
		await flushPromises()

		expect(exportMotions).toHaveBeenCalledWith(
			expect.objectContaining({ recipeKey: 'apple-product-page', format: 'VUE' })
		)
		expect(wrapper.text()).toContain('MotionHero.vue')

		const zipButton = wrapper.findAll('button').find(button => button.text().includes('打包下载'))
		expect(zipButton).toBeTruthy()
		await zipButton!.trigger('click')

		expect(createObjectURL).toHaveBeenCalledTimes(1)
	})

	it('切换导出格式后再生成，请求里带的是新格式', async () => {
		const wrapper = mount(MotionExportPanel, {
			props: { recipeKey: 'apple-product-page', planName: 'Apple 产品页' },
			global: { stubs: { ExtCodeBlock: true } }
		})

		const reactChip = wrapper.findAll('button').find(button => button.text().trim() === 'React')
		expect(reactChip).toBeTruthy()
		await reactChip!.trigger('click')
		await wrapper.find('.mexp-run').trigger('click')
		await flushPromises()

		expect(exportMotions).toHaveBeenLastCalledWith(
			expect.objectContaining({ format: 'REACT' })
		)
	})
})
