import request, { http } from '@/api/request'
import { fetchModuleInfo } from '@/extensions/_shared/api/ping'
import type { ExtModuleInfo } from '@/extensions/_shared/types/common'
import type {
	InstrumentProfile,
	InstrumentProfilePayload,
	KeySequence,
	MusicTaskDetail,
	MusicTaskListItem,
	PageResult,
	UnmappedStrategy,
} from '@/extensions/music-agent/types/music'
import type {
	OptimizationReport,
	PerformancePreset,
	PerformancePresetPayload,
	PerformanceShare,
	ProfileMatch,
	SongAnalysis,
} from '@/extensions/music-studio/types/studio'

/** Music Agent 的接口前缀（后端 context-path 是 /api，由 Vite 代理） */
export const MUSIC_API_BASE = '/music'

const TASK_BASE = MUSIC_API_BASE + '/tasks'
const INSTRUMENT_BASE = MUSIC_API_BASE + '/instruments'

/** 模块自检：确认前端能调到 music 模块 */
export function fetchMusicModuleInfo(): Promise<ExtModuleInfo> {
	return fetchModuleInfo(MUSIC_API_BASE)
}

/**
 * 上传 MIDI。
 *
 * 用 axios 实例而不是 http 封装，原因有二：要发 multipart 表单，且解析大曲子可能超过
 * 默认的 10 秒超时。拦截器同样生效，返回值仍是剥过壳的 data。
 */
export function uploadMidi(file: File): Promise<MusicTaskDetail> {
	const form = new FormData()
	form.append('file', file)
	return request.post(TASK_BASE + '/midi', form, { timeout: 60000 }) as unknown as Promise<MusicTaskDetail>
}

/** 粘贴简谱解析 */
export function parseJianpu(name: string, jianpu: string): Promise<MusicTaskDetail> {
	return http.post<MusicTaskDetail>(TASK_BASE + '/jianpu', { name, jianpu })
}

export function listMusicTasks(
	keyword: string,
	page: number,
	size: number,
	favoriteOnly = false,
): Promise<PageResult<MusicTaskListItem>> {
	return http.get<PageResult<MusicTaskListItem>>(TASK_BASE, { keyword, page, size, favorite: favoriteOnly })
}

/** 收藏 / 取消收藏：返回切换后的状态 */
export function toggleFavorite(taskId: number): Promise<boolean> {
	return http.post<boolean>(TASK_BASE + '/' + taskId + '/favorite')
}

export function getMusicTask(id: number): Promise<MusicTaskDetail> {
	return http.get<MusicTaskDetail>(TASK_BASE + '/' + id)
}

export function deleteMusicTask(id: number): Promise<void> {
	return http.delete<void>(TASK_BASE + '/' + id)
}

/** 按键映射：把音符交给指定乐器档案，拿到按键序列 */
export function mapTaskKeys(id: number, profileId: number, strategy?: UnmappedStrategy): Promise<KeySequence> {
	return http.post<KeySequence>(TASK_BASE + '/' + id + '/keys', { profileId, strategy })
}

/**
 * 曲目分析：难度星级、速度、音域、预计演奏时长与推荐键位。
 * 推荐是后端把每套乐器档案真实试算一遍得出的；不传 profileId 就用推荐档案。
 */
export function fetchSongAnalysis(taskId: number, profileId?: number | null): Promise<SongAnalysis> {
	return http.get<SongAnalysis>(TASK_BASE + '/' + taskId + '/analysis', profileId ? { profileId } : undefined)
}

/** 演奏优化：连按冲突 / 超出音域 / 和弦 / 密度 / 长按，每条建议都带可应用的参数 */
export function fetchOptimization(
	taskId: number,
	profileId: number,
	strategy?: string,
	presetId?: number | null,
): Promise<OptimizationReport> {
	return http.get<OptimizationReport>(TASK_BASE + '/' + taskId + '/optimize', { profileId, strategy, presetId })
}

/** 演奏方案列表：首次访问会补齐「原版 / 简单版 / 快速版」三套内置方案 */
export function listPresets(taskId: number): Promise<PerformancePreset[]> {
	return http.get<PerformancePreset[]>(TASK_BASE + '/' + taskId + '/presets')
}

export function createPreset(taskId: number, payload: PerformancePresetPayload): Promise<PerformancePreset> {
	return http.post<PerformancePreset>(TASK_BASE + '/' + taskId + '/presets', payload)
}

export function updatePreset(taskId: number, presetId: number, payload: PerformancePresetPayload): Promise<PerformancePreset> {
	return http.put<PerformancePreset>(TASK_BASE + '/' + taskId + '/presets/' + presetId, payload)
}

export function deletePreset(taskId: number, presetId: number): Promise<void> {
	return http.delete<void>(TASK_BASE + '/' + taskId + '/presets/' + presetId)
}

/** 按方案跑一遍映射（在方案之间切换时用） */
export function mapWithPreset(taskId: number, presetId: number): Promise<KeySequence> {
	return http.get<KeySequence>(TASK_BASE + '/' + taskId + '/presets/' + presetId + '/keys')
}

/**
 * 匹配游戏乐器档案：按 游戏 / 乐器 / 键数 给可用档案排序。
 * 库里没有该游戏的档案时会退回通用档案，并把这件事写进 reasons。
 */
export function matchProfiles(
	taskId: number,
	game?: string,
	instrument?: string,
	keyCount?: number | null,
): Promise<ProfileMatch[]> {
	return http.get<ProfileMatch[]>(TASK_BASE + '/' + taskId + '/profiles/match', { game, instrument, keyCount })
}

/** 生成演奏码：把曲目 + 演奏方案 + 最近一次计划打包成自包含快照 */
export function createShare(payload: {
	taskId: number
	creator?: string
	title?: string
	presetId?: number | null
}): Promise<PerformanceShare> {
	const body = { creator: payload.creator, title: payload.title, presetId: payload.presetId }
	return http.post<PerformanceShare>(MUSIC_API_BASE + '/shares?taskId=' + payload.taskId, body)
}

/** 我分享出去的曲谱 */
export function listShares(): Promise<PerformanceShare[]> {
	return http.get<PerformanceShare[]>(MUSIC_API_BASE + '/shares')
}

/** 看一个演奏码里有什么（不导入也能先看摘要） */
export function describeShare(shareCode: string): Promise<PerformanceShare> {
	return http.get<PerformanceShare>(MUSIC_API_BASE + '/shares/' + encodeURIComponent(shareCode))
}

/** 导入演奏码：在本库里还原成一首新曲目（复制一份，不影响原作者） */
export function importShare(shareCode: string): Promise<PerformanceShare> {
	return http.post<PerformanceShare>(MUSIC_API_BASE + '/shares/' + encodeURIComponent(shareCode) + '/import')
}

export function listInstruments(): Promise<InstrumentProfile[]> {
	return http.get<InstrumentProfile[]>(INSTRUMENT_BASE)
}

export function createInstrument(payload: InstrumentProfilePayload): Promise<number> {
	return http.post<number>(INSTRUMENT_BASE, payload)
}

export function updateInstrument(id: number, payload: InstrumentProfilePayload): Promise<void> {
	return http.put<void>(INSTRUMENT_BASE + '/' + id, payload)
}

export function deleteInstrument(id: number): Promise<void> {
	return http.delete<void>(INSTRUMENT_BASE + '/' + id)
}
