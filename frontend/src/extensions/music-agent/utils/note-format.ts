/**
 * 音符与时间格式转换（纯函数，便于单独验证）。
 *
 * 约定：MIDI 60 = C4（中央 C），与 javax.sound.midi 的音高编号、以及后端
 * PitchNames 的写法保持一致 —— 变化音写作「字母在前」：#F4 而不是 F#4。
 */

const NOTE_NAMES = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B']

/** MIDI 音高编号 → 音名（60 → C4） */
export function midiToNoteName(midi: number): string {
	const value = Math.round(midi)
	const name = NOTE_NAMES[((value % 12) + 12) % 12]
	const octave = Math.floor(value / 12) - 1
	return name + octave
}

/** 音名 → MIDI 音高编号（C4 → 60）；格式不对返回 null，避免抛异常打断解析 */
export function noteNameToMidi(name: string): number | null {
	const matched = /^([A-G])(#|b)?(-?\d+)$/.exec(name.trim().toUpperCase())
	if (!matched) {
		return null
	}
	const base = NOTE_NAMES.indexOf(matched[1])
	if (base < 0) {
		return null
	}
	const accidental = matched[2] === '#' ? 1 : matched[2] === 'b' ? -1 : 0
	return (Number(matched[3]) + 1) * 12 + base + accidental
}

/**
 * 音名 → 简谱数字（相对主音）。
 * 高八度用上标撇表示，低八度用下标点表示：
 * 例如主音 C4 时，E4 → 3、C5 → 1'、C3 → 1,。
 */
export function jianpuOf(midi: number, tonicMidi = 60): string {
	const family: Record<number, string> = { 0: '1', 2: '2', 4: '3', 5: '4', 7: '5', 9: '6', 11: '7' }
	const degree = (((Math.round(midi) - tonicMidi) % 12) + 12) % 12
	const digit = family[degree]
	if (!digit) {
		return '—'
	}
	const octaveShift = Math.floor((Math.round(midi) - tonicMidi) / 12)
	if (octaveShift > 0) {
		return digit + "'".repeat(octaveShift)
	}
	if (octaveShift < 0) {
		return digit + ','.repeat(-octaveShift)
	}
	return digit
}

/** 简谱文本的轻量校验：数字、休止 0、延长线、小节线与空白，外加头行的 = 与 BPM */
export function isJianpuLike(text: string): boolean {
	if (!text.trim()) {
		return false
	}
	return /^[1-7|\-\s,.'0#bB=/:A-Ga-gpPmM]+$/.test(text)
}

/** 简谱文本规整：全角转半角、压缩多余空白，便于后续按小节切分 */
export function normalizeJianpu(text: string): string {
	return text
		.replace(/[１-７０]/g, (ch) => String.fromCharCode(ch.charCodeAt(0) - 0xfee0))
		.replace(/[\u3000\t]+/g, ' ')
		.replace(/ +/g, ' ')
		.trim()
}

/** 毫秒 → 「分:秒.毫秒」，与后端导出文本里的写法一致 */
export function formatMs(millis: number): string {
	const safe = Math.max(0, Math.round(millis))
	const totalSeconds = Math.floor(safe / 1000)
	const minutes = Math.floor(totalSeconds / 60)
	const seconds = totalSeconds % 60
	return `${minutes}:${String(seconds).padStart(2, '0')}.${String(safe % 1000).padStart(3, '0')}`
}

/** 毫秒 → 「1 分 20 秒」，用于列表里看总长 */
export function formatDuration(millis: number): string {
	const totalSeconds = Math.max(0, Math.round(millis / 1000))
	if (totalSeconds < 60) {
		return totalSeconds + ' 秒'
	}
	return Math.floor(totalSeconds / 60) + ' 分 ' + (totalSeconds % 60) + ' 秒'
}

/**
 * 时间线网格间隔：按总时长挑一个「不密不疏」的刻度。
 *
 * 固定间隔会让长曲子出现几百条线、短曲子只有两条，所以按总长分档。
 */
export function gridStepMs(durationMs: number, targetLines = 12): number {
	const candidates = [100, 250, 500, 1000, 2000, 5000, 10000, 15000, 30000]
	const ideal = Math.max(100, durationMs / Math.max(1, targetLines))
	return candidates.find((candidate) => candidate >= ideal) ?? candidates[candidates.length - 1]
}

/** 音名里带升号或降号 = 黑键，用来给音符上不同的颜色 */
export function isBlackKeyName(noteName: string): boolean {
	return /[#b]/.test(noteName)
}

/** 键多的时候一行排不下，按每行 8 个折行（两排虚拟乐器的习惯） */
export function chunkKeys(keys: string[], perRow = 8): string[][] {
	const rows: string[][] = []
	for (let index = 0; index < keys.length; index += perRow) {
		rows.push(keys.slice(index, index + perRow))
	}
	return rows.length ? rows : [[]]
}
