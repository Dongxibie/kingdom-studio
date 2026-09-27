<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { midiToNoteName } from '@/extensions/music-agent/utils/note-format'

/**
 * 虚拟键盘（Performance Keyboard）。
 *
 * 三件事：跟着播放高亮、点键试听、把键位映射写在键上。
 * 试听用的是原有播放器那套 WebAudio 上下文（audioContext()），不引新依赖、不碰播放器本身。
 */
interface Props {
	keyLayout: string[]
	keyNoteNames: string[]
	/** 当前被按下的键 */
	activeKeys: string[]
	/** 是否允许点击试听 */
	audition?: boolean
	/** 试听音量 */
	volume?: number
	/** 每行几个键 */
	perRow?: number
	/** 音高表：键 → MIDI，用来试听发声 */
	keyPitches?: number[]
	/** 紧凑模式（回放页用大键，这里控制间距） */
	compact?: boolean
}

const props = withDefaults(defineProps<Props>(), {
	audition: true,
	volume: 0.4,
	perRow: 8,
	keyPitches: () => [],
	compact: false,
})

const emit = defineEmits<{
	audition: [index: number]
}>()

const lastPlayed = ref('')

const rows = computed(() => {
	const list: { index: number; key: string; note: string; pitch: number | null }[] = []
	props.keyLayout.forEach((key, index) => {
		list.push({
			index,
			key,
			note: props.keyNoteNames[index] ?? '',
			pitch: props.keyPitches[index] ?? null,
		})
	})
	const chunked: typeof list[] = []
	for (let index = 0; index < list.length; index += props.perRow) {
		chunked.push(list.slice(index, index + props.perRow))
	}
	return chunked
})

const activeSet = computed(() => new Set(props.activeKeys))

/** 试听：三角波 + 快起音，和回放同一种音色，听感一致 */
function auditionKey(index: number, pitch: number | null) {
	if (!props.audition) {
		return
	}
	if (pitch === null) {
		ElMessage.warning('这个键没有对应的音高')
		return
	}
	emit('audition', index)
	void playPitch(pitch)
}

async function playPitch(pitch: number) {
	const { audioContext } = await import('@/extensions/music-agent/utils/demo-player')
	const audio = audioContext()
	const now = audio.currentTime + 0.02
	const gain = audio.createGain()
	gain.connect(audio.destination)
	gain.gain.setValueAtTime(0, now)
	gain.gain.linearRampToValueAtTime(props.volume * 0.5, now + 0.012)
	gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.5)

	const oscillator = audio.createOscillator()
	oscillator.type = 'triangle'
	oscillator.frequency.setValueAtTime(440 * Math.pow(2, (pitch - 69) / 12), now)
	oscillator.connect(gain)
	oscillator.start(now)
	oscillator.stop(now + 0.55)

	lastPlayed.value = midiToNoteName(pitch)
}
</script>

<template>
	<div class="pk" :class="{ compact }">
		<div v-for="(row, rowIndex) in rows" :key="rowIndex" class="pk-row">
			<button
				v-for="item in row"
				:key="item.index"
				class="pk-key"
				:class="{ on: activeSet.has(item.key), muted: !item.note }"
				type="button"
				:title="item.pitch !== null ? `${item.key} → ${item.note}（${item.pitch}）` : `${item.key} → ${item.note}`"
				@click="auditionKey(item.index, item.pitch)">
				<span class="pk-note">{{ item.note || '—' }}</span>
				<span class="pk-keyname">{{ item.key }}</span>
			</button>
		</div>
		<div class="pk-foot">
			<span class="pk-hint">
				{{ audition ? '点击任意键试听' : '仅展示键位映射' }}
				<span v-if="lastPlayed"> · 刚试听：{{ lastPlayed }}</span>
			</span>
			<span class="pk-legend">
				<i class="pk-dot pk-dot--on" />正在演奏
				<i class="pk-dot pk-dot--off" />空闲
			</span>
		</div>
	</div>
</template>

<style scoped>
.pk {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.pk-row {
	display: flex;
	gap: 8px;
	flex-wrap: wrap;
}

.pk-key {
	flex: 1 1 0;
	min-width: 64px;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 3px;
	padding: 12px 6px 9px;
	border: 1px solid var(--st-edge);
	border-top: 2px solid rgba(255, 255, 255, 0.16);
	border-radius: 10px;
	background: var(--st-key);
	box-shadow: 0 6px 14px rgba(0, 0, 0, 0.35), inset 0 1px 0 rgba(255, 255, 255, 0.08);
	cursor: pointer;
	transition: transform 0.12s ease, border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.compact .pk-key {
	padding: 9px 5px 7px;
	min-width: 56px;
}

.pk-key:hover {
	border-color: var(--st-edge-hot);
	transform: translateY(-2px);
}

.pk-key.on {
	background: var(--st-key-on);
	border-color: var(--ext-gold-light);
	transform: translateY(2px) scale(0.985);
	box-shadow: 0 0 22px rgba(240, 205, 114, 0.55), inset 0 1px 0 rgba(255, 255, 255, 0.35);
}

.pk-key.muted {
	opacity: 0.45;
}

.pk-note {
	font-family: var(--ext-font-mono);
	font-size: 13px;
	color: var(--ext-text);
}

.pk-key.on .pk-note {
	color: #21180a;
}

.pk-keyname {
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-text-mute);
}

.pk-key.on .pk-keyname {
	color: rgba(33, 24, 10, 0.7);
}

.pk-foot {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 10px;
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.pk-legend {
	display: inline-flex;
	align-items: center;
	gap: 6px;
}

.pk-dot {
	width: 7px;
	height: 7px;
	border-radius: 50%;
	display: inline-block;
}

.pk-dot--on {
	background: var(--ext-gold-light);
}

.pk-dot--off {
	background: rgba(255, 255, 255, 0.22);
}
</style>
