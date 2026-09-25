import { http } from '@/api/request'
import type { ExtModuleInfo } from '@/extensions/_shared/types/common'

/**
 * 扩展模块自检：统一走各模块的 /ping，返回值即模块信息。
 *
 * 各扩展的 api 目录只需传自己的前缀（如 '/motion'），
 * 复用主站那套请求封装（自动剥掉 Result 外壳、统一中文报错）。
 */
export function fetchModuleInfo(apiBase: string): Promise<ExtModuleInfo> {
	return http.get<ExtModuleInfo>(apiBase + '/ping')
}
