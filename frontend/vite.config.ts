import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
	plugins: [vue()],
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
