<script setup lang="ts">
import { computed, ref } from 'vue'
import { formatMs, gridStepMs, isBlackKeyName, midiToNoteName } from '@/extensions/music-agent/utils/note-format'
import type { KeyStroke, MusicNote } from '@/extensions/music-agent/types/music'

/**
 * 演奏时间线（Perfor mance Timeline）。
 *
 * 相比原来的钢琴卷帘，这一版加了四件事：时间刻度带、音高坐标轴、当前音符高亮、悬停读数。
 * 全部用百分比定位（不画 canvas）：音符数量在千级以内，DOM 足够快，
 * 而且可点、可悬停、可被无障碍读到。
 */
interface Props {
	notes: MusicNote[]
	strokes?: KeyStroke[]
	durationMs: number
	currentMs: number
	activeStroke?: KeyStroke | null
	/** 当前正在发声的音符序号：高亮它 */
	currentNoteSeq?: number | null
	/** 卷帘高度（像素） */
	height?: number
	/** 是否显示下方按键轨 */
	showStrokes?: boolean
	/** 是否显示时间刻度 */
	showRuler?: boolean
}

const props = withDefaults(defineProps<Props>(), {
	strokes: () => [],
	activeStroke: null,
	currentNoteSeq: null,
	height: 300,
	showStrokes: true,
	showRuler: true,
})

const emit = defineEmits<{
	seek: [millis: number]
}>()

const roll = ref<HTMLElement | null>(null)
const hoverMs = ref<number | null>(null)
const dragging = ref(false)

/** 音高范围：至少留 6 个半音，避免单音曲子被拉成一条巨块 */
const pitchLow = computed(() => Math.min(...props.notes.map((note) => note.pitch), 127))
const pitchHigh = computed(() => Math.max(...props.notes.map((note) => note.pitch), 0))
const span = computed(() => Math.max(6, pitchHigh.value - pitchLow.value + 1))

/** 时间轴上的刻度：按曲长自动选步长，最多 12 条 */
const ticks = computed(() => {
	if (!props.durationMs) {
		return []
	}
	const step = gridStepMs(props.durationMs)
	const list: number[] = []
	for (let value = 0; value <= props.durationMs; value += step) {
		list.push(value)
	}
	return list
})

/** 左侧音高标签：每一行一个音名 */
const rows = computed(() => {
	const list: { pitch: number; name: string; bottom: number }[] = []
	for (let pitch = pitchHigh.value; pitch >= pitchLow.value; pitch--) {
		list.push({
			pitch,
			name: midiToNoteName(pitch),
			bottom: ((pitch - pitchLow.value) / span.value) * 100,
		})
	}
	return list
})

function percentOfTime(millis: number) {
	if (!props.durationMs) {
		return 0
	}
	return (millis / props.durationMs) * 100
}

/** 音符块的位置与大小 */
function noteStyle(note: MusicNote) {
	const bottom = ((note.pitch - pitchLow.value) / span.value) * 100
	const height = Math.max(3.2, (1 / span.value) * 100 * 0.82)
	const left = percentOfTime(note.startMs)
	const width = Math.max(0.35, percentOfTime(note.durationMs))
	return {
		left: left + '%',
		width: width + '%',
		bottom: bottom + '%',
		height: height + '%',
	}
}

function strokeStyle(stroke: KeyStroke) {
	return {
		left: percentOfTime(stroke.startMs) + '%',
		width: Math.max(0.6, percentOfTime(stroke.durationMs)) + '%',
	}
}

function isActive(note: MusicNote) {
	return props.currentNoteSeq !== null && note.seqNo === props.currentNoteSeq
}

function labelOf(note: MusicNote) {
	// 太窄的音符不写字，避免糊成一片
	return percentOfTime(note.durationMs) > 1.6 ? note.noteName : ''
}

/** 鼠标位置 → 毫秒 */
function timeFromEvent(event: MouseEvent) {
	const element = roll.value
	if (!element || !props.durationMs) {
		return null
	}
	const box = element.getBoundingClientRect()
	const ratio = (event.clientX - box.left) / box.width
	return Math.max(0, Math.min(props.durationMs, ratio * props.durationMs))
}

function onPointerDown(event: MouseEvent) {
	dragging.value = true
	const millis = timeFromEvent(event)
	if (millis !== null) {
		emit('seek', millis)
	}
	window.addEventListener('mousemove', onPointerMove)
	window.addEventListener('mouseup', onPointerUp)
}

function onPointerMove(event: MouseEvent) {
	if (!dragging.value) {
		return
	}
	const millis = timeFromEvent(event)
	if (millis !== null) {
		emit('seek', millis)
	}
}

function onPointerUp() {
	dragging.value = false
	window.removeEventListener('mousemove', onPointerMove)
	window.removeEventListener('mouseup', onPointerUp)
}

function onHover(event: MouseEvent) {
	hoverMs.value = timeFromEvent(event)
}

/** 悬停所在的音符：用来在读数条上显示音名 */
const hoverNote = computed(() => {
	const millis = hoverMs.value
	if (millis === null) {
		return null
	}
	return props.notes.find((note) => millis >= note.startMs && millis < note.startMs + note.durationMs) ?? null
})

function seekTo(note: MusicNote) {
	emit('seek', note.startMs)
}
</script>

<template>
	<div class="te">
		<div v-if="showRuler" class="te-ruler">
			<div class="te-axis-head" />
			<div class="te-ruler-track">
				<div v-for="tick in ticks" :key="tick" class="te-tick" :style="{ left: percentOfTime(tick) + '%' }">
					<span class="te-tick-line" />
					<span class="te-tick-text">{{ formatMs(tick) }}</span>
				</div>
			</div>
		</div>

		<div class="te-body">
			<div class="te-axis">
				<span v-for="row in rows" :key="row.pitch" class="te-axis-name" :style="{ bottom: row.bottom + '%' }">
					{{ row.name }}
				</span>
			</div>

			<div
				ref="roll"
				class="te-roll"
				:style="{ height: height + 'px' }"
				@mousedown="onPointerDown"
				@mousemove="onHover"
				@mouseleave="hoverMs = null">
				<div v-for="row in rows" :key="'grid-' + row.pitch" class="te-row-line" :style="{ bottom: row.bottom + '%' }" />
				<div v-for="tick in ticks" :key="'line-' + tick" class="te-col-line" :style="{ left: percentOfTime(tick) + '%' }" />

				<button
					v-for="note in notes"
					:key="note.seqNo"
					class="te-note"
					:class="{ black: isBlackKeyName(note.noteName), on: isActive(note) }"
					:style="noteStyle(note)"
					type="button"
					:title="`${note.noteName} · ${formatMs(note.startMs)} 起，持续 ${formatMs(note.durationMs)}`"
					@click.stop="seekTo(note)">
					<span class="te-note-text">{{ labelOf(note) }}</span>
				</button>

				<div v-if="hoverNote" class="te-readout" :style="{ left: percentOfTime(hoverMs ?? 0) + '%' }">
					{{ hoverNote.noteName }} · {{ formatMs(hoverNote.startMs) }}
				</div>

				<div class="te-playhead" :style="{ left: percentOfTime(currentMs) + '%' }">
					<span class="te-playhead-cap">{{ formatMs(currentMs) }}</span>
				</div>
			</div>
		</div>

		<div v-if="showStrokes" class="te-lane">
			<div class="te-axis">
				<span class="te-axis-lane">按键</span>
			</div>
			<div class="te-lane-track">
				<button
					v-for="stroke in strokes"
					:key="stroke.seq"
					class="te-stroke"
					:class="{ on: activeStroke && activeStroke.seq === stroke.seq, adj: stroke.adjusted }"
					:style="strokeStyle(stroke)"
					type="button"
					:title="`${stroke.keys.join(' + ')} · ${formatMs(stroke.startMs)}`"
					@click.stop="emit('seek', stroke.startMs)" />
				<div class="te-playhead te-playhead--lane" :style="{ left: percentOfTime(currentMs) + '%' }" />
			</div>
		</div>
	</div>
</template>

<style scoped>
.te {
	display: flex;
	flex-direction: column;
	gap: 6px;
	user-select: none;
}

.te-ruler {
	display: grid;
	grid-template-columns: 46px minmax(0, 1fr);
}

.te-ruler-track {
	position: relative;
	height: 20px;
	border-bottom: 1px solid var(--st-grid);
}

.te-tick {
	position: absolute;
	top: 0;
	bottom: 0;
}

.te-tick-line {
	position: absolute;
	top: 12px;
	width: 1px;
	height: 8px;
	background: var(--st-grid-strong);
}

.te-tick-text {
	position: absolute;
	left: 4px;
	top: 2px;
	font-family: var(--ext-font-mono);
	font-size: 9.5px;
	color: var(--ext-text-mute);
	white-space: nowrap;
}

.te-body {
	display: grid;
	grid-template-columns: 46px minmax(0, 1fr);
}

.te-axis {
	position: relative;
}

.te-axis-name {
	position: absolute;
	right: 6px;
	transform: translateY(50%);
	font-family: var(--ext-font-mono);
	font-size: 9.5px;
	color: var(--ext-text-mute);
}

.te-axis-lane {
	position: absolute;
	right: 6px;
	top: 50%;
	transform: translateY(-50%);
	font-size: 10px;
	color: var(--ext-text-mute);
}

.te-roll {
	position: relative;
	border: 1px solid var(--st-grid);
	border-radius: 10px;
	background:
		linear-gradient(180deg, rgba(255, 255, 255, 0.02), transparent),
		var(--st-track);
	overflow: hidden;
	cursor: crosshair;
}

.te-row-line {
	position: absolute;
	left: 0;
	right: 0;
	height: 1px;
	background: var(--st-grid);
}

.te-col-line {
	position: absolute;
	top: 0;
	bottom: 0;
	width: 1px;
	background: var(--st-grid);
}

.te-note {
	position: absolute;
	border: 0;
	border-radius: 4px;
	padding: 0 4px;
	background: var(--st-note);
	box-shadow: 0 2px 6px rgba(0, 0, 0, 0.35);
	cursor: pointer;
	overflow: hidden;
	transition: filter 0.15s ease, transform 0.15s ease;
}

.te-note.black {
	background: var(--st-note-black);
}

.te-note:hover {
	filter: brightness(1.15);
}

.te-note.on {
	background: var(--st-note-hot);
	box-shadow: 0 0 0 1px rgba(255, 233, 168, 0.8), 0 0 18px rgba(240, 205, 114, 0.55);
	transform: scaleY(1.12);
	z-index: 3;
}

.te-note-text {
	font-family: var(--ext-font-mono);
	font-size: 9px;
	color: rgba(6, 10, 18, 0.85);
	white-space: nowrap;
}

.te-readout {
	position: absolute;
	top: 4px;
	transform: translateX(-50%);
	padding: 2px 7px;
	border-radius: 999px;
	background: rgba(6, 8, 14, 0.85);
	border: 1px solid var(--st-edge);
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-text-dim);
	pointer-events: none;
	white-space: nowrap;
}

.te-playhead {
	position: absolute;
	top: 0;
	bottom: 0;
	width: 2px;
	background: linear-gradient(180deg, var(--ext-gold-light), rgba(240, 205, 114, 0.25));
	box-shadow: 0 0 12px rgba(240, 205, 114, 0.6);
	pointer-events: none;
	z-index: 4;
}

.te-playhead-cap {
	position: absolute;
	top: -1px;
	left: 3px;
	padding: 1px 5px;
	border-radius: 4px;
	background: var(--ext-gold-light);
	color: #1a1408;
	font-family: var(--ext-font-mono);
	font-size: 9.5px;
	white-space: nowrap;
}

.te-lane {
	display: grid;
	grid-template-columns: 46px minmax(0, 1fr);
	margin-top: 2px;
}

.te-lane-track {
	position: relative;
	height: 40px;
	border: 1px solid var(--st-grid);
	border-radius: 10px;
	background: var(--st-lane);
	overflow: hidden;
}

.te-stroke {
	position: absolute;
	top: 8px;
	bottom: 8px;
	border: 0;
	border-radius: 4px;
	background: rgba(167, 139, 250, 0.55);
	cursor: pointer;
	transition: background 0.15s ease;
}

.te-stroke.adj {
	background: rgba(240, 205, 114, 0.55);
}

.te-stroke.on {
	background: var(--ext-gold-light);
	box-shadow: 0 0 14px rgba(240, 205, 114, 0.7);
}

.te-playhead--lane {
	top: 0;
	bottom: 0;
	box-shadow: none;
	background: rgba(240, 205, 114, 0.75);
}
</style>
