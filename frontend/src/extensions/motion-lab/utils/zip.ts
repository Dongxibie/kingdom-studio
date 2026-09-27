/**
 * 极简 ZIP 打包器（只用 store 模式，不做压缩）。
 *
 * 为什么自己写：导出要一次性给出多个文件，浏览器里没有原生打包 API，
 * 为了这一个功能引入一个压缩库不值得 —— 我们的产物是纯文本，体积本来就不大，
 * 不压缩只是文件大一点，换来的是零依赖与一眼能看懂的实现。
 *
 * 结构：每个文件一个本地文件头 + 数据，末尾一份中央目录与结束记录（EOCD）。
 * 全部按小端序写入，CRC32 用标准多项式 0xEDB88320。
 */

const CRC_TABLE = (() => {
	const table = new Uint32Array(256)
	for (let index = 0; index < 256; index++) {
		let value = index
		for (let bit = 0; bit < 8; bit++) {
			value = value & 1 ? 0xedb88320 ^ (value >>> 1) : value >>> 1
		}
		table[index] = value >>> 0
	}
	return table
})()

/** 标准 CRC32（ZIP 用的就是它） */
export function crc32(bytes: Uint8Array): number {
	let crc = 0xffffffff
	for (let index = 0; index < bytes.length; index++) {
		crc = CRC_TABLE[(crc ^ bytes[index]) & 0xff] ^ (crc >>> 8)
	}
	return (crc ^ 0xffffffff) >>> 0
}

export interface ZipEntry {
	/** 包内路径，例如 src/components/MotionPlan.vue */
	path: string
	content: string
}

function utf8(text: string): Uint8Array {
	return new TextEncoder().encode(text)
}

/** 组装一个 ZIP（store 模式） */
export function buildZip(entries: ZipEntry[]): Blob {
	const chunks: Uint8Array[] = []
	const central: Uint8Array[] = []
	let offset = 0

	// 打包时间：ZIP 用的是 DOS 时间格式，这里固定成一个合法值，
	// 让同一份产物每次生成的字节完全一致（便于比对与复现）
	const dosTime = 0
	const dosDate = (2024 - 1980) * 512 + 1 * 32 + 1

	for (const entry of entries) {
		const nameBytes = utf8(entry.path)
		const dataBytes = utf8(entry.content)
		const crc = crc32(dataBytes)

		const local = new Uint8Array(30 + nameBytes.length)
		const localView = new DataView(local.buffer)
		localView.setUint32(0, 0x04034b50, true) // 本地文件头签名
		localView.setUint16(4, 20, true) // 需要的版本
		localView.setUint16(6, 0x0800, true) // 通用位标记：文件名是 UTF-8
		localView.setUint16(8, 0, true) // 压缩方法 0 = store
		localView.setUint16(10, dosTime, true)
		localView.setUint16(12, dosDate, true)
		localView.setUint32(14, crc, true)
		localView.setUint32(18, dataBytes.length, true)
		localView.setUint32(22, dataBytes.length, true)
		localView.setUint16(26, nameBytes.length, true)
		localView.setUint16(28, 0, true)
		local.set(nameBytes, 30)

		chunks.push(local, dataBytes)

		const dir = new Uint8Array(46 + nameBytes.length)
		const dirView = new DataView(dir.buffer)
		dirView.setUint32(0, 0x02014b50, true) // 中央目录头签名
		dirView.setUint16(4, 20, true)
		dirView.setUint16(6, 20, true)
		dirView.setUint16(8, 0x0800, true)
		dirView.setUint16(10, 0, true)
		dirView.setUint16(12, dosTime, true)
		dirView.setUint16(14, dosDate, true)
		dirView.setUint32(16, crc, true)
		dirView.setUint32(20, dataBytes.length, true)
		dirView.setUint32(24, dataBytes.length, true)
		dirView.setUint16(28, nameBytes.length, true)
		dirView.setUint16(30, 0, true)
		dirView.setUint16(32, 0, true)
		dirView.setUint16(34, 0, true)
		dirView.setUint16(36, 0, true)
		dirView.setUint32(38, 0, true)
		dirView.setUint32(42, offset, true)
		dir.set(nameBytes, 46)
		central.push(dir)

		offset += local.length + dataBytes.length
	}

	const centralSize = central.reduce((sum, chunk) => sum + chunk.length, 0)
	const end = new Uint8Array(22)
	const endView = new DataView(end.buffer)
	endView.setUint32(0, 0x06054b50, true) // EOCD 签名
	endView.setUint16(8, entries.length, true)
	endView.setUint16(10, entries.length, true)
	endView.setUint32(12, centralSize, true)
	endView.setUint32(16, offset, true)

	return new Blob([...chunks, ...central, end], { type: 'application/zip' })
}

/** 触发浏览器下载 */
export function downloadBlob(blob: Blob, filename: string) {
	const url = URL.createObjectURL(blob)
	const link = document.createElement('a')
	link.href = url
	link.download = filename
	document.body.appendChild(link)
	link.click()
	link.remove()
	// 交给浏览器读完再释放，避免下载被中断
	window.setTimeout(() => URL.revokeObjectURL(url), 4000)
}
