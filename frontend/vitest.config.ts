import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

/**
 * 前端单元测试配置。
 *
 * 与 vite.config.ts 分开写：测试要 jsdom 环境与自带的 define 常量，
 * 而生产构建不需要知道测试框架的存在。
 */
export default defineConfig({
	plugins: [vue()],
	define: {
		__APP_VERSION__: JSON.stringify('0.0.0-test')
	},
	resolve: {
		alias: {
			'@': fileURLToPath(new URL('./src', import.meta.url))
		}
	},
	test: {
		environment: 'jsdom',
		globals: true,
		include: ['src/**/*.spec.ts'],
		setupFiles: ['./src/test/setup.ts'],
		restoreMocks: true
	}
})
