import { fileURLToPath, URL } from 'node:url'
import { readFileSync } from 'node:fs'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 版本号唯一来源是 package.json：页脚、健康检查、README 都由它同步
const pkg = JSON.parse(readFileSync(fileURLToPath(new URL('./package.json', import.meta.url)), 'utf8'))

// https://vitejs.dev/config/
export default defineConfig({
	plugins: [vue()],
	define: {
		__APP_VERSION__: JSON.stringify(pkg.version)
	},
	resolve: {
		alias: {
			'@': fileURLToPath(new URL('./src', import.meta.url))
		}
	},
	server: {
		port: 5173,
		open: false,
		proxy: {
			// 前端统一请求 /api/**，由 Vite 代理到后端，避免开发期跨域
			'/api': {
				target: 'http://localhost:8080',
				changeOrigin: true
			}
		}
	}
})
