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

export function listMusicTasks(keyword: string, page: number, size: number): Promise<PageResult<MusicTaskListItem>> {
	return http.get<PageResult<MusicTaskListItem>>(TASK_BASE, { keyword, page, size })
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
