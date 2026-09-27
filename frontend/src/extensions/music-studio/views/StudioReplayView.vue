<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ExtEmpty } from '@/extensions/_shared/components'
import MusicStudioLayout from '@/extensions/music-studio/components/MusicStudioLayout.vue'
import TimelineEditor from '@/extensions/music-studio/components/TimelineEditor.vue'
import PerformanceKeyboard from '@/extensions/music-studio/components/PerformanceKeyboard.vue'
import { formatMs } from '@/extensions/music-agent/utils/note-format'
import { useStudioSession } from '@/extensions/music-studio/composables/useStudioSession'

/**
 * 演奏回放（Performance Replay）。
 *
 * 给「拿给人看」用的一个页面：时间、当前音符、当前按键、键盘高亮四块放大摆开，
 * 视线不用移动就能看出此刻在弹什么。也可以全屏，投屏或录屏时用它。
 */
const router = useRouter()
const { state, currentNote, activeKeys, keyNoteNames, toggle, rewind, seek, loadTasks, selectTask } = useStudioSession()

const stage = ref<HTMLElement | null>(null)

const currentKeyText = computed(() => (activeKeys.value.length ? activeKeys.value.join(' + ') : '—'))
const currentNoteText = computed(() => (currentNote.value ? currentNote.value.noteName : '—'))
const noteProgress = computed(() => {
	const note = currentNote.value
	if (!note || note.durationMs <= 0) {
		return 0
	}
	const elapsed = state.currentMs - note.startMs
	return Math.max(0, Math.min(100, (elapsed / note.durationMs) * 100))
})

const upcoming = computed(() => {
	const strokes = state.sequence?.strokes ?? []
	const now = state.currentMs
	return strokes.filter((stroke) => stroke.startMs >= now).slice(0, 6)
})

async function fullscreen() {
	const element = stage.value
	if (!element) {
		return
	}
	if (document.fullscreenElement) {
		await document.exitFullscreen()
		return
	}
	await element.requestFullscreen()
}

onMounted(async () => {
	if (!state.tasks.length) {
		await loadTasks(1)
	}
	if (!state.task && state.tasks.length) {
		await selectTask(state.tasks[0].id, { silent: true })
	}
})
</script>

<template>
	<MusicStudioLayout
		title="演奏回放"
		subtitle="AI 演奏工作室 · 全屏演示模式"
		mark="♪">
		<template #actions>
			<button class="st-btn" type="button" @click="router.push('/extensions/music-studio/composer')">回到编排台</button>
			<button class="st-btn st-btn--primary" type="button" @click="fullscreen">切换全屏</button>
		</template>

		<template #left>
			<div class="st-glass">
				<div class="st-title">演示提示</div>
				<ul class="tip-list">
					<li>时间、当前音符、当前按键、键盘高亮会同步刷新。</li>
					<li>按需全屏，适合投屏或录屏。</li>
					<li>下面列出接下来几组按键，方便提前准备手位。</li>
				</ul>
			</div>

			<div v-if="state.sequence" class="st-glass">
				<div class="st-title">接下来</div>
				<div class="next-list">
					<div v-for="stroke in upcoming" :key="stroke.seq" class="next-item">
						<span class="next-keys">{{ stroke.keys.join(' + ') }}</span>
						<span class="next-time">{{ formatMs(stroke.startMs) }}</span>
					</div>
					<p v-if="!upcoming.length" class="st-label">已经到最后了。</p>
				</div>
			</div>
		</template>

		<div v-if="!state.task" class="st-glass">
			<ExtEmpty tag="未选择曲目" title="先选一首曲子" hint="回到曲目台，或去编排台打开一首。" />
		</div>

		<template v-else>
			<div ref="stage" class="stage st-glass" :class="{ 'stage--live': state.playing }">
				<div class="stage-head">
					<span class="stage-song">{{ state.task.name }}</span>
					<span v-if="state.sequence" class="st-chip">{{ state.sequence.profileName }}</span>
					<span class="stage-state" :class="{ on: state.playing }">{{ state.playing ? '演奏中' : '已暂停' }}</span>
				</div>

				<div class="stage-grid">
					<div class="stage-cell">
						<span class="stage-label">当前时间</span>
						<span class="stage-clock">{{ formatMs(state.currentMs) }}</span>
						<span class="stage-sub">总长 {{ formatMs(state.task.durationMs) }}</span>
					</div>
					<div class="stage-cell stage-cell--note">
						<span class="stage-label">当前音符</span>
						<span class="stage-note">{{ currentNoteText }}</span>
						<div class="stage-note-bar"><i :style="{ width: noteProgress + '%' }" /></div>
					</div>
					<div class="stage-cell stage-cell--key">
						<span class="stage-label">当前按键</span>
						<span class="stage-key">{{ currentKeyText }}</span>
						<span class="stage-sub">{{ state.sequence ? state.sequence.profileName : '尚未映射' }}</span>
					</div>
				</div>

				<div class="stage-keys">
					<PerformanceKeyboard
						:key-layout="state.sequence?.keyLayout ?? []"
						:key-note-names="keyNoteNames"
						:key-pitches="state.sequence?.keyPitches ?? []"
						:active-keys="activeKeys"
						:volume="state.volume"
						:audition="true"
						:compact="false"
						:per-row="state.sequence && state.sequence.keyCount > 12 ? 10 : 8" />
				</div>

				<div class="stage-foot">
					<button class="tp-btn" type="button" title="回到开头" @click="rewind">⏮</button>
					<button class="tp-btn tp-btn--main" type="button" @click="toggle">{{ state.playing ? '❚❚' : '▶' }}</button>
					<button class="tp-btn" type="button" title="跳到结尾" @click="seek(state.task.durationMs)">⏭</button>
					<div class="stage-bar" @mousedown="(event: MouseEvent) => seek(((event.clientX - (event.currentTarget as HTMLElement).getBoundingClientRect().left) / (event.currentTarget as HTMLElement).getBoundingClientRect().width) * state.task!.durationMs)">
						<div class="stage-bar-fill" :style="{ width: (state.currentMs / (state.task.durationMs || 1)) * 100 + '%' }" />
					</div>
				</div>
			</div>

			<div class="st-glass">
				<div class="st-title">时间线<span class="st-sub">回放时指针会跟着走</span></div>
				<TimelineEditor
					:notes="state.task.notes"
					:strokes="state.sequence?.strokes ?? []"
					:duration-ms="state.task.durationMs"
					:current-ms="state.currentMs"
					:active-stroke="state.activeStroke"
					:current-note-seq="currentNote?.seqNo ?? null"
					:height="180" />
			</div>
		</template>
	</MusicStudioLayout>
</template>

<style scoped>
.stage {
	display: flex;
	flex-direction: column;
	gap: 14px;
	padding: 18px 20px;
}

.stage--live {
	box-shadow: var(--st-shadow), 0 0 0 1px rgba(240, 205, 114, 0.3), 0 0 60px rgba(240, 205, 114, 0.08) inset;
}

.stage-head {
	display: flex;
	align-items: center;
	gap: 10px;
	flex-wrap: wrap;
}

.stage-song {
	font-size: 20px;
	color: var(--ext-text);
}

.stage-state {
	margin-left: auto;
	font-size: 11px;
	color: var(--ext-text-mute);
}

.stage-state.on {
	color: var(--ext-gold-light);
}

.stage-grid {
	display: grid;
	grid-template-columns: 1fr 1.2fr 1fr;
	gap: 12px;
}

.stage-cell {
	border: 1px solid var(--st-edge);
	border-radius: 14px;
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.015));
	padding: 14px 16px;
	display: flex;
	flex-direction: column;
	gap: 4px;
}

.stage-cell--note,
.stage-cell--key {
	align-items: center;
}

.stage-label {
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.stage-clock {
	font-family: var(--ext-font-mono);
	font-size: 34px;
	color: var(--ext-gold-light);
	line-height: 1.1;
}

.stage-note {
	font-family: var(--ext-font-mono);
	font-size: 42px;
	color: #7ee0d0;
	line-height: 1.1;
}

.stage-key {
	font-family: var(--ext-font-mono);
	font-size: 42px;
	color: var(--ext-gold-light);
	line-height: 1.1;
}

.stage-sub {
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.stage-note-bar {
	margin-top: 6px;
	width: 100%;
	height: 4px;
	border-radius: 999px;
	background: rgba(255, 255, 255, 0.08);
	overflow: hidden;
}

.stage-note-bar i {
	display: block;
	height: 100%;
	background: linear-gradient(90deg, #6ee7d7, #2fa8a0);
	transition: width 0.08s linear;
}

.stage-keys {
	padding: 4px 0 2px;
}

.stage-foot {
	display: flex;
	align-items: center;
	gap: 10px;
}

.stage-bar {
	position: relative;
	flex: 1;
	height: 8px;
	border-radius: 999px;
	background: rgba(255, 255, 255, 0.07);
	cursor: pointer;
}

.stage-bar-fill {
	position: absolute;
	inset: 0 auto 0 0;
	border-radius: 999px;
	background: linear-gradient(90deg, rgba(240, 205, 114, 0.5), var(--ext-gold-light));
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
}

.tp-btn--main {
	width: 40px;
	height: 40px;
	border-color: var(--ext-gold);
	background: linear-gradient(180deg, rgba(240, 205, 114, 0.24), rgba(240, 205, 114, 0.08));
	color: var(--ext-gold-light);
}

.next-list {
	display: flex;
	flex-direction: column;
	gap: 5px;
}

.next-item {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
	gap: 8px;
	font-size: 11.5px;
}

.next-keys {
	font-family: var(--ext-font-mono);
	color: var(--ext-text-dim);
}

.next-time {
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-text-mute);
}

.tip-list {
	margin: 0;
	padding-left: 16px;
	display: flex;
	flex-direction: column;
	gap: 5px;
	font-size: 11px;
	color: var(--ext-text-dim);
	line-height: 1.7;
}

@media (max-width: 1100px) {
	.stage-grid {
		grid-template-columns: 1fr;
	}
}
</style>
