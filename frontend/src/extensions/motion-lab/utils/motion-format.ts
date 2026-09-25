import { MOTION_CATEGORIES } from '@/extensions/motion-lab/types/motion'

/** 分类 key → 中文标签；未知 key 原样返回，避免采集到新分类时界面出现空白 */
export function categoryLabel(key: string): string {
	const found = MOTION_CATEGORIES.find((item) => item.key === key)
	return found ? found.label : key
}

/**
 * 相对时间：不引入 dayjs 这类依赖，用原生 Date 自己算。
 * 后端返回的格式是「yyyy-MM-dd HH:mm:ss」，Safari 不认这种带空格的写法，先换成 ISO 风格。
 */
export function formatRelativeTime(value?: string): string {
	if (!value) {
		return '—'
	}
	const time = new Date(value.replace(' ', 'T')).getTime()
	if (Number.isNaN(time)) {
		return value
	}
	const diff = Date.now() - time
	const minute = 60 * 1000
	const hour = 60 * minute
	const day = 24 * hour
	if (diff < minute) {
		return '刚刚'
	}
	if (diff < hour) {
		return Math.floor(diff / minute) + ' 分钟前'
	}
	if (diff < day) {
		return Math.floor(diff / hour) + ' 小时前'
	}
	if (diff < 30 * day) {
		return Math.floor(diff / day) + ' 天前'
	}
	return value.slice(0, 10)
}

/**
 * 去重键：名称 + 来源地址 + 内容哈希。
 * Phase 3 的采集器用同一个函数生成键，保证「判定重复」的口径和前端一致。
 */
/** 只依赖三个字段，用结构化类型而不是绑定某个具体 VO，列表项与详情都能直接传 */
export function dedupeKeyOf(input: { name: string; sourceUrl: string; contentHash?: string }): string {
	return [input.name.trim().toLowerCase(), input.sourceUrl.trim().toLowerCase(), input.contentHash ?? ''].join('::')
}

/** 长地址截断显示，保留首尾以便辨认 */
export function shortenUrl(url: string, max = 46): string {
	if (!url || url.length <= max) {
		return url || '—'
	}
	const head = url.slice(0, Math.floor(max * 0.6))
	const tail = url.slice(-Math.floor(max * 0.3))
	return head + '…' + tail
}
