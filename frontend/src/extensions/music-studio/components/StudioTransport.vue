<script setup lang="ts">
import { computed, ref } from 'vue'
import { useStudioSession } from '@/extensions/music-studio/composables/useStudioSession'
import { formatMs } from '@/extensions/music-agent/utils/note-format'

/**
 * 走带控制条（Transport）。
 *
 * 播放状态属于会话而不是某个页面，所以放在布局里：在编排台按了播放，切到回放页接着播，
 * 到导出中心仍然是同一个位置 —— 这是「工作室」和「后台页面」的区别。
 */
const { state, currentNote, duration, progress, toggle, rewind, seek, setVolume } = useStudioSession()

const bar = ref<HTMLElement | null>(null)
const dragging = ref(false)

const currentKeyText = computed(() => (state.activeStroke ? state.activeStroke.keys.join(' + ') : '—'))
const currentNoteText = computed(() => (currentNote.value ? currentNote.value.noteName : '—'))
const canPlay = computed(() => Boolean(state.sequence) && Boolean(state.task))

function millisFromEvent(event: MouseEvent) {
	const element = bar.value
	if (!element || !duration.value) {
		return null
	}
	const box = element.getBoundingClientRect()
	const ratio = Math.max(0, Math.min(1, (event.clientX - box.left) / box.width))
	return ratio * duration.value
}

function onBarDown(event: MouseEvent) {
	dragging.value = true
	const millis = millisFromEvent(event)
	if (millis !== null) {
		seek(millis)
	}
	window.addEventListener('mousemove', onBarMove)
	window.addEventListener('mouseup', onBarUp)
}

function onBarMove(event: MouseEvent) {
	if (!dragging.value) {
		return
	}
	const millis = millisFromEvent(event)
	if (millis !== null) {
		seek(millis)
	}
}

function onBarUp() {
	dragging.value = false
	window.removeEventListener('mousemove', onBarMove)
	window.removeEventListener('mouseup', onBarUp)
}
</script>

<template>
	<div class="tp" :class="{ 'tp--off': !canPlay }">
		<div class="tp-buttons">
			<button class="tp-btn" type="button" title="回到开头" @click="rewind">⏮</button>
			<button class="tp-btn tp-btn--main" :class="{ playing: state.playing }" type="button" :disabled="!canPlay" @click="toggle">
				{{ state.playing ? '❚❚' : '▶' }}
			</button>
			<button class="tp-btn" type="button" title="停止" @click="rewind">⏹</button>
		</div>

		<div class="tp-clock">
			<span class="tp-time">{{ formatMs(state.currentMs) }}</span>
			<span class="tp-total">/ {{ formatMs(duration) }}</span>
		</div>

		<div ref="bar" class="tp-bar" @mousedown="onBarDown">
			<div class="tp-bar-fill" :style="{ width: progress + '%' }" />
			<div class="tp-bar-knob" :style="{ left: progress + '%' }" />
			<div
				v-for="stroke in state.sequence?.strokes ?? []"
				:key="stroke.seq"
				class="tp-mark"
				:style="{ left: (stroke.startMs / (duration || 1)) * 100 + '%' }" />
		</div>

		<div class="tp-readouts">
			<div class="tp-readout">
				<span class="tp-readout-label">当前音符</span>
				<span class="tp-readout-value">{{ currentNoteText }}</span>
			</div>
			<div class="tp-readout">
				<span class="tp-readout-label">当前按键</span>
				<span class="tp-readout-value tp-readout-value--key">{{ currentKeyText }}</span>
			</div>
		</div>

		<div class="tp-volume">
			<span class="tp-readout-label">音量</span>
			<input
				type="range"
				min="0"
				max="1"
				step="0.05"
				:value="state.volume"
				@input="setVolume(Number(($event.target as HTMLInputElement).value))" />
		</div>

		<span v-if="!canPlay" class="tp-hint">先选曲子与乐器档案</span>
	</div>
</template>

<style scoped>
.tp {
	display: flex;
	align-items: center;
	gap: 14px;
	padding: 9px 14px;
	border: 1px solid var(--st-edge);
	border-radius: 12px;
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.06), rgba(255, 255, 255, 0.02));
	backdrop-filter: blur(10px);
}

.tp-buttons {
	display: flex;
	align-items: center;
	gap: 6px;
}

.tp-btn {
	width: 32px;
	height: 32px;
	border: 1px solid var(--st-edge);
	border-radius: 9px;
	background: var(--st-glass);
	color: var(--ext-text-dim);
	font-size: 12px;
	cursor: pointer;
	transition: border-color 0.18s ease, color 0.18s ease, transform 0.18s ease;
}

.tp-btn:hover:not(:disabled) {
	border-color: var(--st-edge-hot);
	color: var(--ext-gold-light);
	transform: translateY(-1px);
}

.tp-btn:disabled {
	opacity: 0.4;
	cursor: default;
}

.tp-btn--main {
	width: 42px;
	height: 42px;
	font-size: 14px;
	border-color: var(--ext-gold);
	background: linear-gradient(180deg, rgba(240, 205, 114, 0.24), rgba(240, 205, 114, 0.08));
	color: var(--ext-gold-light);
}

.tp-btn--main.playing {
	box-shadow: 0 0 18px rgba(240, 205, 114, 0.5);
}

.tp-clock {
	display: flex;
	align-items: baseline;
	gap: 5px;
	min-width: 118px;
}

.tp-time {
	font-family: var(--ext-font-mono);
	font-size: 15px;
	color: var(--ext-gold-light);
}

.tp-total {
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.tp-bar {
	position: relative;
	flex: 1;
	height: 8px;
	border-radius: 999px;
	background: rgba(255, 255, 255, 0.07);
	cursor: pointer;
}

.tp-bar-fill {
	position: absolute;
	left: 0;
	top: 0;
	bottom: 0;
	border-radius: 999px;
	background: linear-gradient(90deg, rgba(240, 205, 114, 0.55), var(--ext-gold-light));
}

.tp-bar-knob {
	position: absolute;
	top: 50%;
	width: 12px;
	height: 12px;
	margin-left: -6px;
	border-radius: 50%;
	background: var(--ext-gold-light);
	transform: translateY(-50%);
	box-shadow: 0 0 12px rgba(240, 205, 114, 0.7);
	pointer-events: none;
}

.tp-mark {
	position: absolute;
	top: -3px;
	width: 1px;
	height: 14px;
	background: rgba(255, 255, 255, 0.14);
}

.tp-readouts {
	display: flex;
	gap: 12px;
}

.tp-readout {
	display: flex;
	flex-direction: column;
	gap: 1px;
	min-width: 62px;
}

.tp-readout-label {
	font-size: 9.5px;
	color: var(--ext-text-mute);
}

.tp-readout-value {
	font-family: var(--ext-font-mono);
	font-size: 12.5px;
	color: var(--ext-text);
}

.tp-readout-value--key {
	color: var(--ext-gold-light);
}

.tp-volume {
	display: flex;
	align-items: center;
	gap: 7px;
}

.tp-volume input {
	width: 86px;
	accent-color: var(--ext-gold);
}

.tp-hint {
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.tp--off {
	opacity: 0.82;
}

@media (max-width: 1100px) {
	.tp {
		flex-wrap: wrap;
	}
}
</style>
