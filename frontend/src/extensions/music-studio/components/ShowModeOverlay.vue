<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import TimelineEditor from '@/extensions/music-studio/components/TimelineEditor.vue'
import PerformanceKeyboard from '@/extensions/music-studio/components/PerformanceKeyboard.vue'
import { useStudioSession } from '@/extensions/music-studio/composables/useStudioSession'
import { formatDuration, formatMs } from '@/extensions/music-agent/utils/note-format'

/**
 * 演示模式（Show Mode）。
 *
 * 流程只有两步：选一首曲子 → 点开始。之后全屏铺开四样东西 ——
 * 时间线在走、键盘在亮、声音在放、当前按键在显示。
 * 面向的是「拿给别人看」：大字号、少控件、放着就能看懂。
 */
const emit = defineEmits<{
	close: []
}>()

const { state, activeKeys, currentNote, keyNoteNames, selectTask, start, stop, toggle, rewind, loadTasks } =
	useStudioSession()

const started = ref(false)
const loop = ref(true)
const compactUi = ref(false)

const currentKeyText = computed(() => (activeKeys.value.length ? activeKeys.value.join(' + ') : '—'))
const currentNoteText = computed(() => (currentNote.value ? currentNote.value.noteName : '—'))
const progress = computed(() => ({
	now: formatMs(state.currentMs),
	total: formatMs(state.task?.durationMs ?? 0),
	percent: state.task?.durationMs ? Math.min(100, (state.currentMs / state.task.durationMs) * 100) : 0,
}))

const upcoming = computed(() => {
	const strokes = state.sequence?.strokes ?? []
	return strokes.filter((stroke) => stroke.startMs >= state.currentMs).slice(0, 4)
})

function begin() {
	if (!state.task) {
		return
	}
	started.value = true
	start(0)
}

function finish() {
	stop()
	started.value = false
	emit('close')
}

/** 循环：每 300ms 看一眼有没有走完，走完就从头再来 */
let loopTimer = 0
watch(
	() => started.value && loop.value,
	(enabled) => {
		if (loopTimer) {
			window.clearInterval(loopTimer)
			loopTimer = 0
		}
		if (!enabled) {
			return
		}
		loopTimer = window.setInterval(() => {
			const task = state.task
			if (!task || state.playing) {
				return
			}
			if (state.currentMs >= task.durationMs - 60) {
				start(0)
			}
		}, 300)
	},
)

onBeforeUnmount(() => {
	if (loopTimer) {
		window.clearInterval(loopTimer)
	}
	stop()
})

void loadTasks(1)
</script>

<template>
	<div class="show">
		<div class="show-head">
			<span class="show-mark">演示模式</span>
			<span v-if="state.task" class="show-song">{{ state.task.name }}</span>
			<span v-if="state.sequence" class="st-chip st-chip--hot">{{ state.sequence.profileName }}</span>
			<div class="show-controls">
				<button class="st-btn" type="button" @click="compactUi = !compactUi">
					{{ compactUi ? '显示面板' : '只看演示' }}
				</button>
				<button class="st-btn" type="button" @click="finish">退出演示</button>
			</div>
		</div>

		<!-- 第一步：选曲 -->
		<div v-if="!started" class="show-pick st-glass">
			<div class="show-pick-title">选一首曲子，然后开始演示</div>
			<div class="show-pick-grid">
				<button
					v-for="song in state.tasks"
					:key="song.id"
					class="show-pick-card"
					:class="{ on: state.task?.id === song.id }"
					type="button"
					@click="selectTask(song.id, { silent: true })">
					<span class="show-pick-name">{{ song.name }}</span>
					<span class="show-pick-meta">
						{{ song.noteCount }} 音 · {{ song.tempoBpm }} BPM · {{ formatDuration(song.durationMs) }}
					</span>
					<span class="show-pick-difficulty">{{ '★'.repeat(song.difficultyStars) }}{{ song.difficultyLabel }}</span>
				</button>
				<p v-if="!state.tasks.length" class="show-pick-empty">曲库还是空的，先去曲目台导入一首。</p>
			</div>
			<div class="show-pick-foot">
				<label class="show-loop">
					<input v-model="loop" type="checkbox" />
					循环播放
				</label>
				<button class="st-btn st-btn--primary" type="button" :disabled="!state.task" @click="begin">
					开始演示
				</button>
			</div>
		</div>

		<!-- 第二步：演示 -->
		<template v-else>
			<div class="show-stage" :class="{ 'show-stage--live': state.playing }">
				<div class="show-cell">
					<span class="show-cell-label">当前时间</span>
					<span class="show-clock">{{ progress.now }}</span>
					<span class="show-cell-sub">总长 {{ progress.total }}</span>
				</div>
				<div class="show-cell show-cell--note">
					<span class="show-cell-label">当前音符</span>
					<span class="show-note">{{ currentNoteText }}</span>
				</div>
				<div class="show-cell show-cell--key">
					<span class="show-cell-label">当前按键</span>
					<span class="show-key">{{ currentKeyText }}</span>
				</div>
			</div>

			<div class="show-bar">
				<div class="show-bar-track">
					<div class="show-bar-fill" :style="{ width: progress.percent + '%' }" />
				</div>
				<div class="show-bar-actions">
					<button class="st-btn" type="button" @click="rewind">回到开头</button>
					<button class="st-btn st-btn--primary" type="button" @click="toggle">
						{{ state.playing ? '暂停' : '继续' }}
					</button>
					<label class="show-loop">
						<input v-model="loop" type="checkbox" />
						循环
					</label>
				</div>
			</div>

			<div v-if="!compactUi" class="show-keys st-glass">
				<PerformanceKeyboard
					:key-layout="state.sequence?.keyLayout ?? []"
					:key-note-names="keyNoteNames"
					:key-pitches="state.sequence?.keyPitches ?? []"
					:active-keys="activeKeys"
					:volume="state.volume"
					:per-row="state.sequence && state.sequence.keyCount > 12 ? 10 : 8" />
			</div>

			<div v-if="!compactUi" class="show-bottom">
				<div class="st-glass show-timeline">
					<TimelineEditor
						:notes="state.task?.notes ?? []"
						:strokes="state.sequence?.strokes ?? []"
						:duration-ms="state.task?.durationMs ?? 0"
						:current-ms="state.currentMs"
						:active-stroke="state.activeStroke"
						:current-note-seq="currentNote?.seqNo ?? null"
						:height="150" />
				</div>
				<div class="st-glass show-next">
					<div class="st-label">接下来</div>
					<div v-for="stroke in upcoming" :key="stroke.seq" class="show-next-item">
						<span class="show-next-keys">{{ stroke.keys.join(' + ') }}</span>
						<span class="show-next-time">{{ formatMs(stroke.startMs) }}</span>
					</div>
					<p v-if="!upcoming.length" class="show-next-empty">到最后了。</p>
				</div>
			</div>
		</template>
	</div>
</template>

<style scoped>
.show {
	position: fixed;
	inset: 0;
	z-index: 2000;
	display: flex;
	flex-direction: column;
	gap: 14px;
	padding: 18px 22px 22px;
	overflow: auto;
	background:
		radial-gradient(120% 90% at 12% 0%, rgba(240, 205, 114, 0.1), transparent 60%),
		radial-gradient(90% 80% at 100% 10%, rgba(167, 139, 250, 0.12), transparent 55%),
		#07080c;
	color: var(--ext-text);
}

.show-head {
	display: flex;
	align-items: center;
	gap: 10px;
	flex-wrap: wrap;
}

.show-mark {
	font-size: 13px;
	color: var(--ext-gold-light);
}

.show-song {
	font-size: 17px;
	color: var(--ext-text);
}

.show-controls {
	margin-left: auto;
	display: flex;
	gap: 8px;
}

.show-pick {
	display: flex;
	flex-direction: column;
	gap: 14px;
	max-width: 940px;
	margin: 24px auto 0;
}

.show-pick-title {
	font-size: 16px;
	color: var(--ext-text);
}

.show-pick-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
	gap: 10px;
}

.show-pick-card {
	display: flex;
	flex-direction: column;
	gap: 3px;
	text-align: left;
	border: 1px solid var(--st-edge);
	border-radius: 12px;
	background: var(--st-glass);
	padding: 11px 13px;
	cursor: pointer;
	transition: border-color 0.18s ease, transform 0.18s ease;
}

.show-pick-card:hover {
	border-color: var(--st-edge-hot);
	transform: translateY(-2px);
}

.show-pick-card.on {
	border-color: var(--ext-gold);
	background: linear-gradient(180deg, rgba(240, 205, 114, 0.18), rgba(240, 205, 114, 0.05));
}

.show-pick-name {
	font-size: 13.5px;
	color: var(--ext-text);
}

.show-pick-meta {
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.show-pick-difficulty {
	font-size: 10.5px;
	color: var(--ext-gold-light);
}

.show-pick-empty {
	margin: 0;
	font-size: 11.5px;
	color: var(--ext-text-mute);
}

.show-pick-foot {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 10px;
}

.show-loop {
	display: inline-flex;
	align-items: center;
	gap: 6px;
	font-size: 11.5px;
	color: var(--ext-text-dim);
}

.show-stage {
	display: grid;
	grid-template-columns: 1fr 1.15fr 1fr;
	gap: 14px;
}

.show-stage--live .show-clock,
.show-stage--live .show-key {
	text-shadow: 0 0 18px rgba(240, 205, 114, 0.45);
}

.show-cell {
	border: 1px solid var(--st-edge);
	border-radius: 16px;
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.055), rgba(255, 255, 255, 0.015));
	padding: 16px 20px;
	display: flex;
	flex-direction: column;
	gap: 4px;
}

.show-cell--note,
.show-cell--key {
	align-items: center;
}

.show-cell-label {
	font-size: 11px;
	color: var(--ext-text-mute);
}

.show-cell-sub {
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.show-clock {
	font-family: var(--ext-font-mono);
	font-size: 46px;
	line-height: 1.05;
	color: var(--ext-gold-light);
}

.show-note {
	font-family: var(--ext-font-mono);
	font-size: 54px;
	line-height: 1.05;
	color: #7ee0d0;
}

.show-key {
	font-family: var(--ext-font-mono);
	font-size: 54px;
	line-height: 1.05;
	color: var(--ext-gold-light);
}

.show-bar {
	display: flex;
	align-items: center;
	gap: 14px;
}

.show-bar-track {
	flex: 1;
	height: 8px;
	border-radius: 999px;
	background: rgba(255, 255, 255, 0.08);
	overflow: hidden;
}

.show-bar-fill {
	height: 100%;
	border-radius: 999px;
	background: linear-gradient(90deg, rgba(240, 205, 114, 0.5), var(--ext-gold-light));
	transition: width 0.08s linear;
}

.show-bar-actions {
	display: flex;
	align-items: center;
	gap: 8px;
}

.show-keys {
	padding: 14px 16px;
}

.show-bottom {
	display: grid;
	grid-template-columns: minmax(0, 1fr) 220px;
	gap: 12px;
}

.show-timeline,
.show-next {
	padding: 12px 14px;
}

.show-next-item {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
	gap: 8px;
	font-size: 12px;
	margin-top: 6px;
}

.show-next-keys {
	font-family: var(--ext-font-mono);
	color: var(--ext-text-dim);
}

.show-next-time {
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-text-mute);
}

.show-next-empty {
	margin: 6px 0 0;
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

@media (max-width: 1100px) {
	.show-stage,
	.show-bottom {
		grid-template-columns: 1fr;
	}
}
</style>
