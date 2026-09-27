import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ExportCenter from '@/extensions/music-studio/components/ExportCenter.vue'
import MusicLibraryRail from '@/extensions/music-studio/components/MusicLibraryRail.vue'
import {
	describeShare,
	importShare,
	listMusicTasks,
	listPresets,
	listShares,
	mapTaskKeys,
	getMusicTask,
	listInstruments,
	fetchSongAnalysis,
	fetchOptimization
} from '@/extensions/music-agent/api/music'
import { exportMacro, generatePerformancePlan } from '@/extensions/music-agent/api/macro'

/**
 * AI 演奏工作室的关键流程：导入分享码 → 生成演奏计划 → 导出。
 *
 * 两个接口模块整体替换：测试盯的是「填码 → 看到摘要 → 导入」「拿到计划 → 点下载」，
 * 不是后端算得对不对（那是后端 160 个用例的事）。
 */
vi.mock('element-plus', () => ({
	ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn() }
}))

vi.mock('@/extensions/music-agent/api/music', async importOriginal => {
	const actual = await importOriginal<typeof import('@/extensions/music-agent/api/music')>()
	return {
		...actual,
		listMusicTasks: vi.fn(),
		getMusicTask: vi.fn(),
		listInstruments: vi.fn(),
		listPresets: vi.fn(),
		mapTaskKeys: vi.fn(),
		mapWithPreset: vi.fn(),
		fetchSongAnalysis: vi.fn(),
		fetchOptimization: vi.fn(),
		createPreset: vi.fn(),
		updatePreset: vi.fn(),
		deletePreset: vi.fn(),
		toggleFavorite: vi.fn(),
		listShares: vi.fn(),
		describeShare: vi.fn(),
		importShare: vi.fn(),
		createShare: vi.fn()
	}
})

vi.mock('@/extensions/music-agent/api/macro', async importOriginal => {
	const actual = await importOriginal<typeof import('@/extensions/music-agent/api/macro')>()
	return {
		...actual,
		generatePerformancePlan: vi.fn(),
		exportMacro: vi.fn()
	}
})

const taskRow = {
	id: 18,
	name: '两只老虎（演示）',
	sourceType: 'SHARE' as const,
	sourceRef: '演奏码 KS-MUSIC-2026-A001',
	noteCount: 32,
	tempoBpm: 108,
	timeSignature: '4/4',
	durationMs: 14827,
	pitchRange: 'C4–A4',
	difficultyStars: 1,
	difficultyTier: 'EASY',
	favorite: 0,
	createTime: '2026-09-27 17:25:00'
}

/** 演奏计划：只留导出面板真正会读的字段 */
const plan = {
	taskId: 18,
	profileName: '光遇式 15 键',
	noteCount: 64,
	strokeCount: 15,
	keyCount: 15,
	duration: 14827,
	warnings: []
}

const shareDetail = {
	id: 1,
	shareCode: 'KS-MUSIC-2026-A001',
	creator: '李泽龙',
	title: '两只老虎（演示）',
	difficulty: '1 星 · 入门（简单）',
	game: '',
	instrument: '光遇式 15 键',
	noteCount: 32,
	durationMs: 14827,
	importCount: 1,
	createTime: '2026-09-27 17:25:00',
	highlights: ['32 个音符 · 15 秒', '乐器：光遇式 15 键']
}

describe('AI 演奏工作室：导入分享码', () => {
	beforeEach(() => {
		vi.mocked(listMusicTasks).mockResolvedValue({ records: [taskRow], total: 1, page: 1, size: 20, pages: 1 } as never)
		vi.mocked(getMusicTask).mockResolvedValue({
			id: 18,
			name: '两只老虎（演示）',
			sourceType: 'SHARE',
			noteCount: 32,
			tempoBpm: 108,
			timeSignature: '4/4',
			durationMs: 14827,
			pitchLow: 60,
			pitchHigh: 69,
			status: 'READY',
			createTime: '2026-09-27 17:25:00',
			notes: []
		} as never)
		vi.mocked(listInstruments).mockResolvedValue([] as never)
		vi.mocked(listPresets).mockResolvedValue([] as never)
		vi.mocked(mapTaskKeys).mockResolvedValue({
			profileId: 1,
			profileName: '光遇式 15 键',
			strokes: [],
			mappedCount: 0,
			unmappedCount: 0,
			unmappedNotes: []
		} as never)
		vi.mocked(fetchSongAnalysis).mockResolvedValue({ taskId: 18 } as never)
		vi.mocked(fetchOptimization).mockResolvedValue({ taskId: 18, fixes: [], summary: '' } as never)
		vi.mocked(listShares).mockResolvedValue([shareDetail] as never)
		vi.mocked(describeShare).mockResolvedValue(shareDetail as never)
		vi.mocked(importShare).mockResolvedValue({
			...shareDetail,
			importedTaskId: 21,
			message: '已导入为「两只老虎（演示）」，可以直接去编排台演奏或导出'
		} as never)
	})

	it('填演奏码 → 查看摘要 → 导入成新曲目', async () => {
		const wrapper = mount(MusicLibraryRail)

		// 切到「分享」页签
		const shareTab = wrapper.findAll('.rail-tab').find(item => item.text().includes('分享'))
		expect(shareTab).toBeTruthy()
		await shareTab!.trigger('click')
		await flushPromises()

		// 输入演奏码并查看
		await wrapper.find('.rail-share input').setValue('KS-MUSIC-2026-A001')
		const peekButton = wrapper.findAll('button').find(button => button.text() === '查看')
		expect(peekButton).toBeTruthy()
		await peekButton!.trigger('click')
		await flushPromises()

		expect(describeShare).toHaveBeenCalledWith('KS-MUSIC-2026-A001')
		expect(wrapper.text()).toContain('两只老虎（演示）')
		expect(wrapper.text()).toContain('32 个音符 · 15 秒')

		// 导入这首
		const importButton = wrapper.findAll('button').find(button => button.text() === '导入这首')
		expect(importButton).toBeTruthy()
		await importButton!.trigger('click')
		await flushPromises()

		expect(importShare).toHaveBeenCalledWith('KS-MUSIC-2026-A001')
	})

	it('分享列表里能看到演奏码与被导入次数', async () => {
		const wrapper = mount(MusicLibraryRail)
		const shareTab = wrapper.findAll('.rail-tab').find(item => item.text().includes('分享'))
		await shareTab!.trigger('click')
		await flushPromises()

		expect(wrapper.find('.rail-share-card').text()).toContain('KS-MUSIC-2026-A001')
		expect(wrapper.find('.rail-share-card').text()).toContain('被导入 1 次')
	})
})

describe('AI 演奏工作室：生成演奏计划并导出', () => {
	beforeEach(() => {
		vi.mocked(generatePerformancePlan).mockResolvedValue(plan as never)
		vi.mocked(exportMacro).mockResolvedValue({
			format: 'AHK',
			filename: 'liang-zhi-lao-hu.ahk',
			content: 'Send {z}',
			contentType: 'text/plain;charset=utf-8',
			size: 512
		} as never)
	})

	it('拿到计划显示指标，点下载触发浏览器下载', async () => {
		const createObjectURL = vi.spyOn(URL, 'createObjectURL')
		const wrapper = mount(ExportCenter, {
			props: { taskId: 18, profileId: 1 },
			global: { stubs: { ExtCodeBlock: true } }
		})
		await flushPromises()

		expect(generatePerformancePlan).toHaveBeenCalledWith(18, 1, undefined, null)
		expect(wrapper.text()).toContain('64 events')
		expect(wrapper.text()).toContain('14.8 秒')

		const downloadButton = wrapper.findAll('button').find(button => button.text().includes('下载'))
		expect(downloadButton).toBeTruthy()
		await downloadButton!.trigger('click')

		expect(createObjectURL).toHaveBeenCalledTimes(1)
	})

	it('没选曲子时提示先选曲子，不发请求', async () => {
		const wrapper = mount(ExportCenter, {
			props: { taskId: null, profileId: null },
			global: { stubs: { ExtCodeBlock: true } }
		})
		await flushPromises()

		expect(generatePerformancePlan).not.toHaveBeenCalled()
		expect(wrapper.text()).toContain('先选一首曲子')
	})
})
