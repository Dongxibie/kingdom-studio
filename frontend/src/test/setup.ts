import { vi } from 'vitest'

/**
 * jsdom 里没有实现下载相关的 API，而「导出」「下载」正是要覆盖的关键流程，
 * 这里补两个最小实现：只记录调用，不做真实下载。
 */
if (typeof URL.createObjectURL !== 'function') {
	Object.defineProperty(URL, 'createObjectURL', {
		value: vi.fn(() => 'blob:mock'),
		writable: true
	})
}

if (typeof URL.revokeObjectURL !== 'function') {
	Object.defineProperty(URL, 'revokeObjectURL', {
		value: vi.fn(),
		writable: true
	})
}

// Element Plus 的部分组件会读 matchMedia，jsdom 默认没有
if (typeof window.matchMedia !== 'function') {
	Object.defineProperty(window, 'matchMedia', {
		value: vi.fn(() => ({
			matches: false,
			addEventListener: vi.fn(),
			removeEventListener: vi.fn()
		})),
		writable: true
	})
}

/**
 * jsdom 不支持真实导航：下载用到的 <a download>.click() 会抛
 * "Not implemented: navigation"。这里把点击换成空实现 ——
 * 「有没有触发下载」由 URL.createObjectURL 的调用来断言。
 */
HTMLAnchorElement.prototype.click = vi.fn()
