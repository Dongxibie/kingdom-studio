import axios, { AxiosError, type AxiosInstance } from 'axios'
import { ElMessage } from 'element-plus'

/** 后端统一响应格式 */
export interface ApiResult<T = unknown> {
	code: number
	message: string
	data: T
}

const TOKEN_KEY = 'kingdom_studio_token'

const service: AxiosInstance = axios.create({
	// 开发环境由 Vite 代理到 http://localhost:8080/api
	baseURL: '/api',
	timeout: 10000
})

service.interceptors.request.use(config => {
	const token = localStorage.getItem(TOKEN_KEY)
	if (token) {
		config.headers.Authorization = `Bearer ${token}`
	}
	return config
})

service.interceptors.response.use(
	response => {
		const result = response.data as ApiResult
		if (result.code !== 200) {
			ElMessage.error(result.message || '请求失败')
			return Promise.reject(new Error(result.message || '请求失败'))
		}
		// 剥掉统一响应外壳，业务代码直接拿到 data
		return result.data as unknown as typeof response
	},
	(error: AxiosError<ApiResult>) => {
		const message = error.response?.data?.message || describeNetworkError(error)
		ElMessage.error(message)
		// 抛 Error 而不是原始 AxiosError：调用方 catch 到的 message 是可以直接展示的中文
		return Promise.reject(new Error(message))
	}
)

/** axios 的网络层报错信息是英文的（Network Error / timeout of 10000ms exceeded），这里统一换成中文 */
function describeNetworkError(error: AxiosError): string {
	if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
		return '请求超时，请稍后重试'
	}
	if (!error.response) {
		return '无法连接后端服务，请确认后端已启动'
	}
	return `请求失败（HTTP ${error.response.status}）`
}

/** 统一出口：泛型请求方法，返回值就是后端 Result.data */
export const http = {
	get: <T>(url: string, params?: Record<string, unknown>) => service.get(url, { params }) as unknown as Promise<T>,
	post: <T>(url: string, data?: unknown) => service.post(url, data) as unknown as Promise<T>,
	put: <T>(url: string, data?: unknown) => service.put(url, data) as unknown as Promise<T>,
	delete: <T>(url: string) => service.delete(url) as unknown as Promise<T>
}

export { TOKEN_KEY }
export default service
