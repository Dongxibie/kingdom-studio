import { http } from './request'

export interface HealthComponent {
	status: 'UP' | 'DOWN'
	detail: string
	latencyMs: number
}

export interface HealthInfo {
	application: string
	status: 'UP' | 'DEGRADED'
	version: string
	javaVersion: string
	serverTime: string
	database: HealthComponent
	redis: HealthComponent
}

/** 健康检查：确认应用 + MySQL + Redis 是否连通 */
export function fetchHealth() {
	return http.get<HealthInfo>('/health')
}
