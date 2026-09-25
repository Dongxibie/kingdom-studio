<script setup lang="ts">
import { computed } from 'vue'
import type { KeyStroke, MusicNote } from '@/extensions/music-agent/types/music'
import { formatMs, gridStepMs, isBlackKeyName } from '@/extensions/music-agent/utils/note-format'

interface Props {
	notes: MusicNote[]
	/** 有映射结果时，额外画一条按键轨 */
	strokes?: KeyStroke[]
	durationMs: number
	/** 当前播放位置（毫秒） */
	currentMs: number
	/** 当前正在按下的那一组 */
	activeStroke?: KeyStroke | null
}

const props = withDefaults(defineProps<Props>(), { strokes: () => [], activeStroke: null })
const emit = defineEmits<{ seek: [millis: number] }>()

const total = computed(() => Math.max(1, props.durationMs))

/** 音高跨度：至少留一个八度，避免整首曲子只有两三个音时条子被拉成巨块 */
const pitchSpan = computed(() => {
	const offsets = props.notes.map((note) => note.pitchOffset)
	const low = offsets.length ? Math.min(...offsets) : 0
	const high = offsets.length ? Math.max(...offsets) : 0
	const span = Math.max(6, high - low)
	return { low, span }
})

/** 每条刻度的位置与标签 */
const gridLines = computed(() => {
	const step = gridStepMs(props.durationMs)
	const lines: { left: number; label: string }[] = []
	for (let at = 0; at <= props.durationMs; at += step) {
		lines.push({ left: (at / total.value) * 100, label: formatMs(at) })
	}
	return lines
})

/** 按键轨上每个动作的位置，标到按键轨那一行 */
const strokeMarks = computed(() => props.strokes.map((stroke) => {
	const left = (stroke.startMs / total.value) * 100
	// 贴边的标记不能再往左/右居中，否则一半会被裁在轨道外
	const anchor = left < 4 ? '0' : left > 96 ? '-100%' : '-50%'
	return {
		stroke,
		left,
		anchor,
		active: props.activeStroke ? props.activeStroke.seq === stroke.seq : false,
	}
}))

const playheadLeft = computed(() => Math.min(100, Math.max(0, (props.currentMs / total.value) * 100)))

function noteStyle(note: MusicNote) {
	const width = Math.max(0.4, (note.durationMs / total.value) * 100)
	const bottom = ((note.pitchOffset - pitchSpan.value.low) / (pitchSpan.value.span + 1)) * 100
	const height = Math.max(6, 100 / (pitchSpan.value.span + 1) - 2)
	return {
		left: (note.startMs / total.value) * 100 + '%',
		width: width + '%',
		bottom: bottom + '%',
		height: height + '%',
	}
}

/** 点击/拖动定位：把像素位置换算回毫秒 */
function seekFromEvent(event: MouseEvent) {
	const element = event.currentTarget as HTMLElement
	const rect = element.getBoundingClientRect()
	if (rect.width <= 0) {
		return
	}
	const ratio = Math.min(1, Math.max(0, (event.clientX - rect.left) / rect.width))
	emit('seek', Math.round(ratio * total.value))
}

let dragging = false

function onMouseDown(event: MouseEvent) {
	dragging = true
	seekFromEvent(event)
}

function onMouseMove(event: MouseEvent) {
	if (dragging) {
		seekFromEvent(event)
	}
}

function stopDrag() {
	dragging = false
}
</script>

<template>
	<div class="timeline" @mouseup="stopDrag" @mouseleave="stopDrag">
		<div
			class="roll"
			:title="'点击或拖动可以定位到任意位置'"
			@mousedown="onMouseDown"
			@mousemove="onMouseMove">
			<div v-for="line in gridLines" :key="line.left" class="grid" :style="{ left: line.left + '%' }">
				<span class="grid-label">{{ line.label }}</span>
			</div>

			<div
				v-for="note in notes"
				:key="note.seqNo"
				class="note"
				:class="{ black: isBlackKeyName(note.noteName) }"
				:style="noteStyle(note)"
				:title="`${note.noteName} · ${formatMs(note.startMs)} 起 · 时长 ${note.durationMs}ms`" />

			<div class="playhead" :style="{ left: playheadLeft + '%' }" />
		</div>

		<div v-if="strokes.length" class="stroke-lane" @mousedown="onMouseDown" @mousemove="onMouseMove">
			<div
				v-for="mark in strokeMarks"
				:key="mark.stroke.seq"
				class="stroke-mark"
				:class="{ active: mark.active, adjusted: mark.stroke.adjusted }"
				:style="{ left: mark.left + '%', transform: 'translateX(' + mark.anchor + ')' }"
				:title="`${formatMs(mark.stroke.startMs)} · ${mark.stroke.keys.join(' + ')}`">
				{{ mark.stroke.keys.join('+') }}
			</div>
			<div class="playhead" :style="{ left: playheadLeft + '%' }" />
		</div>
		<div v-else class="lane-hint">选好乐器档案后，这里会显示每一次按键的位置</div>
	</div>
</template>

<style scoped>
.timeline {
	user-select: none;
}

.roll {
	position: relative;
	height: 260px;
	background:
		linear-gradient(180deg, rgba(255, 255, 255, 0.02), rgba(0, 0, 0, 0.18)),
		var(--ext-bg);
	border: 1px solid var(--ext-line);
	border-radius: var(--ext-radius);
	overflow: hidden;
	cursor: crosshair;
}

.grid {
	position: absolute;
	top: 0;
	bottom: 0;
	width: 1px;
	background: var(--ext-line-soft);
}

.grid-label {
	position: absolute;
	top: 2px;
	left: 4px;
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-text-mute);
	white-space: nowrap;
}

.note {
	position: absolute;
	border-radius: 3px;
	background: linear-gradient(180deg, var(--ext-gold-light), var(--ext-gold));
	opacity: 0.92;
	min-width: 2px;
}

.note.black {
	background: linear-gradient(180deg, #c9584f, var(--ext-red));
}

.playhead {
	position: absolute;
	top: 0;
	bottom: 0;
	width: 2px;
	background: #e6edf3;
	box-shadow: 0 0 8px rgba(230, 237, 243, 0.55);
	pointer-events: none;
}

.stroke-lane {
	position: relative;
	height: 46px;
	margin-top: 8px;
	background: var(--ext-bg-soft);
	border: 1px solid var(--ext-line);
	border-radius: var(--ext-radius);
	overflow: hidden;
	cursor: crosshair;
}

.stroke-lane .playhead {
	background: var(--ext-gold-light);
}

.stroke-mark {
	position: absolute;
	top: 12px;
	font-family: var(--ext-font-mono);
	font-size: 10px;
	line-height: 18px;
	height: 18px;
	padding: 0 5px;
	border-radius: 4px;
	background: var(--ext-bg-raised);
	color: var(--ext-text-dim);
	white-space: nowrap;
}

.stroke-mark.adjusted {
	border: 1px dashed var(--ext-gold);
	color: var(--ext-gold-light);
}

.stroke-mark.active {
	background: linear-gradient(150deg, var(--ext-gold-light), var(--ext-gold));
	color: #14161a;
	font-weight: 700;
}

.lane-hint {
	margin-top: 8px;
	font-size: 12px;
	color: var(--ext-text-mute);
}
</style>
