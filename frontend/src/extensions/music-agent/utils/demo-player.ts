/**
 * Demo 回放：用 WebAudio 在网页里把按键序列弹出来。
 *
 * 三点说明：
 * 1. 不加载任何音频素材，用振荡器合成 —— 只需要「能听出旋律与节奏」，
 *    不引入体积与版权问题；真正的音色留给后续接真实音源。
 * 2. 发声的音高取「按键实际对应的音高」（keyPitches），而不是原始音符：
 *    这样听到的就是这套乐器照着按键弹出来的效果，包括被就近落键或移八度调整过的音。
 * 3. 调度用 AudioContext 的时钟而不是 setTimeout 累加：后者在标签页切换、
 *    主线程忙时会累积漂移，几十个音符之后整条旋律就会跑偏。
 */

import type { KeySequence, KeyStroke } from '@/extensions/music-agent/types/music'

/** 一个待演奏的事件：什么时刻、响多久、哪些音 */
export interface PlayEvent {
	atMs: number
	durationMs: number
	pitches: number[]
	stroke: KeyStroke
}

/** 把按键序列转成可演奏的事件表（键 → 音高靠 keyPitches 查） */
export function buildPlayEvents(sequence: KeySequence): PlayEvent[] {
	const pitchOfKey = new Map<string, number>()
	sequence.keyLayout.forEach((key, index) => {
		const pitch = sequence.keyPitches[index]
		if (typeof pitch === 'number') {
			pitchOfKey.set(key, pitch)
		}
	})
	const events: PlayEvent[] = []
	for (const stroke of sequence.strokes) {
		const pitches = stroke.keys
			.map((key) => pitchOfKey.get(key))
			.filter((pitch): pitch is number => typeof pitch === 'number')
		// 落不下键的组不发声：宁可没有声音，也不要凭空响一个没按的音
		if (pitches.length) {
			events.push({ atMs: stroke.startMs, durationMs: stroke.durationMs, pitches, stroke })
		}
	}
	return events.sort((a, b) => a.atMs - b.atMs)
}

export interface PlayHandle {
	stop: () => void
}

export interface PlayOptions {
	events: PlayEvent[]
	/** 从哪个时刻开始（毫秒） */
	fromMs: number
	/** 主音量 0-1 */
	volume?: number
	/** 每个事件开始发声时回调，用于同步高亮与时间轴 */
	onEvent?: (event: PlayEvent) => void
	/** 全部播完时回调 */
	onEnd?: () => void
}

let context: AudioContext | null = null

/** 惰性创建 AudioContext：浏览器要求先有用户手势，创建太早会被挂起 */
export function audioContext(): AudioContext {
	if (!context) {
		context = new AudioContext()
	}
	if (context.state === 'suspended') {
		void context.resume()
	}
	return context
}

/**
 * 开始播放。
 *
 * 返回的句柄里带着 stop()：停止时会立刻掐掉所有已排期但还没响的音，
 * 否则按了停止之后还会继续响一两秒。
 */
export function play(options: PlayOptions): PlayHandle {
	const audio = audioContext()
	const volume = options.volume ?? 0.18
	const startAt = audio.currentTime + 0.08 // 留一点缓冲，避免第一个音被吃掉
	const timers: number[] = []

	for (const event of options.events) {
		if (event.atMs + event.durationMs <= options.fromMs) {
			continue
		}
		const offsetSeconds = Math.max(0, event.atMs - options.fromMs) / 1000
		const when = startAt + offsetSeconds
		const durationSeconds = Math.max(0.08, event.durationMs / 1000)

		const gain = audio.createGain()
		gain.connect(audio.destination)
		// 简单的包络：快速起音 + 指数衰减，听感接近拨弦/敲击，不会有爆音
		const peak = volume / Math.max(1, Math.sqrt(event.pitches.length))
		gain.gain.setValueAtTime(0, when)
		gain.gain.linearRampToValueAtTime(peak, when + 0.012)
		gain.gain.exponentialRampToValueAtTime(0.0001, when + durationSeconds)

		event.pitches.forEach((pitch) => {
			const oscillator = audio.createOscillator()
			oscillator.type = 'triangle'
			oscillator.frequency.setValueAtTime(440 * Math.pow(2, (pitch - 69) / 12), when)
			oscillator.connect(gain)
			// 到点自动停止；排期一次即可，不需要留引用
			oscillator.start(when)
			oscillator.stop(when + durationSeconds + 0.05)
		})

		if (options.onEvent) {
			const delayMs = (when - audio.currentTime) * 1000
			timers.push(window.setTimeout(() => options.onEvent?.(event), Math.max(0, delayMs)))
		}
	}

	const last = options.events[options.events.length - 1]
	const endMs = last ? Math.max(0, last.atMs + last.durationMs - options.fromMs) + 120 : 0
	if (options.onEnd) {
		timers.push(window.setTimeout(options.onEnd, endMs))
	}

	return {
		stop() {
			timers.forEach((timer) => window.clearTimeout(timer))
			// 把输出静音并重建上下文：已经排期的振荡器无法逐个取消，静音是最稳的止损
			void audio.close()
			context = null
		},
	}
}
