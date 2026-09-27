<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ExtEmpty, ExtStatusTag } from '@/extensions/_shared/components'
import MusicStudioLayout from '@/extensions/music-studio/components/MusicStudioLayout.vue'
import TimelineEditor from '@/extensions/music-studio/components/TimelineEditor.vue'
import PerformanceKeyboard from '@/extensions/music-studio/components/PerformanceKeyboard.vue'
import AIAssistantPanel from '@/extensions/music-studio/components/AIAssistantPanel.vue'
import PerformanceControlPanel from '@/extensions/music-agent/components/PerformanceControlPanel.vue'
import { formatDuration } from '@/extensions/music-agent/utils/note-format'
import { useStudioSession } from '@/extensions/music-studio/composables/useStudioSession'
import { STRATEGY_LABELS, type MusicTaskListItem } from '@/extensions/music-agent/types/music'

/**
 * 编排台（Composer）—— 工作室的核心页面。
 *
 * 四块按「做音乐时眼睛看的位置」排：顶部是这首歌是什么，中间是时间线（谱子与按键轨），
 * 下面是键盘（手要放的地方），右边是助手与演奏控制。
 */
const router = useRouter()
const { state, activeProfile, currentNote, keyNoteNames, loadTasks, selectTask, setProfile, setStrategy } = useStudioSession()

const STRATEGY_OPTIONS: { value: '' | 'SKIP' | 'NEAREST' | 'SHIFT_OCTAVE'; label: string }[] = [
	{ value: '', label: '按档案默认' },
	{ value: 'SKIP', label: STRATEGY_LABELS.SKIP },
	{ value: 'NEAREST', label: STRATEGY_LABELS.NEAREST },
	{ value: 'SHIFT_OCTAVE', label: STRATEGY_LABELS.SHIFT_OCTAVE },
]

const infoCards = computed(() => {
	const task = state.task
	if (!task) {
		return []
	}
	return [
		{ label: '音符', value: String(task.noteCount) },
		{ label: '速度', value: task.tempoBpm + ' BPM' },
		{ label: '拍号', value: task.timeSignature },
		{ label: '时长', value: formatDuration(task.durationMs) },
		{ label: '音域', value: task.pitchRange },
	]
})

const unmappedCount = computed(() => state.sequence?.unmappedCount ?? 0)
const adjustedCount = computed(() => state.sequence?.adjustedCount ?? 0)

async function openSong(song: MusicTaskListItem) {
	await selectTask(song.id, { silent: true })
}

onMounted(async () => {
	if (!state.tasks.length) {
		await loadTasks(1)
	}
	// 直接进编排台（例如从别的页面跳过来）：没选曲子就自动打开第一首
	if (!state.task && state.tasks.length) {
		await selectTask(state.tasks[0].id, { silent: true })
	}
})
</script>

<template>
	<MusicStudioLayout
		title="编排台"
		subtitle="AI 演奏工作室 · 谱子 → 键位 → 演奏"
		mark="♪">
		<template #actions>
			<ExtStatusTag
				:text="state.sequence ? `已映射 ${state.sequence.mappedCount} / ${state.sequence.noteCount} 个音` : '尚未映射'"
				:tone="unmappedCount ? 'gold' : state.sequence ? 'ok' : 'mute'" />
			<button class="st-btn" type="button" @click="router.push('/extensions/music-studio/replay')">进入演奏回放</button>
		</template>

		<template #left>
			<div class="st-glass">
				<div class="st-title">曲目<span class="st-sub">点一下切歌</span></div>
				<div class="side-list">
					<button
						v-for="song in state.tasks"
						:key="song.id"
						class="side-item"
						:class="{ on: state.task?.id === song.id }"
						type="button"
						@click="openSong(song)">
						<span class="side-name">{{ song.name }}</span>
						<span class="side-meta">{{ song.noteCount }} 音 · {{ song.tempoBpm }} BPM</span>
					</button>
					<p v-if="!state.tasks.length" class="st-label">曲库为空，先回曲目台导入一首。</p>
				</div>
			</div>

			<div class="st-glass">
				<div class="st-title">乐器档案</div>
				<select
					class="side-select"
					:value="state.profileId ?? ''"
					@change="setProfile(Number(($event.target as HTMLSelectElement).value))">
					<option v-for="profile in state.profiles" :key="profile.id" :value="profile.id">
						{{ profile.name }}（{{ profile.keyLayout.length }} 键 · {{ profile.instrument }}）
					</option>
				</select>
				<p class="st-label">{{ activeProfile?.description || '换档案会立刻重新映射这首曲子。' }}</p>

				<div class="st-title" style="margin-top: 12px">超范围音处理</div>
				<div class="side-chips">
					<button
						v-for="item in STRATEGY_OPTIONS"
						:key="item.value"
						class="st-chip"
						:class="{ 'st-chip--hot': state.strategy === item.value }"
						type="button"
						@click="setStrategy(item.value)">
						{{ item.label }}
					</button>
				</div>
			</div>

			<div v-if="state.sequence" class="st-glass">
				<div class="st-title">映射结果</div>
				<div class="side-metrics">
					<div class="st-metric">
						<span class="st-metric-value">{{ state.sequence.keyCount }}</span>
						<span class="st-metric-label">键数</span>
					</div>
					<div class="st-metric">
						<span class="st-metric-value">{{ adjustedCount }}</span>
						<span class="st-metric-label">被调整</span>
					</div>
					<div class="st-metric">
						<span class="st-metric-value">{{ unmappedCount }}</span>
						<span class="st-metric-label">未落键</span>
					</div>
				</div>
				<div v-if="state.sequence.unmapped.length" class="side-warn">
					<div v-for="(item, index) in state.sequence.unmapped.slice(0, 5)" :key="index" class="side-warn-item">
						{{ item.noteName }} · {{ item.reason }}
					</div>
					<p v-if="state.sequence.unmapped.length > 5" class="st-label">
						还有 {{ state.sequence.unmapped.length - 5 }} 个未列出
					</p>
				</div>
			</div>
		</template>

		<div v-if="!state.task" class="st-glass">
			<ExtEmpty tag="未选择曲目" title="先选一首曲子" hint="左边点一首，或回到曲目台导入新的。" />
		</div>

		<template v-else>
			<div class="st-glass st-sweep" :class="{ 'playing-glow': state.playing }">
				<div class="composer-head">
					<div class="composer-title">
						<span class="composer-name">{{ state.task.name }}</span>
						<span class="st-chip">{{ state.task.sourceType === 'MIDI' ? 'MIDI' : '简谱' }}</span>
						<span v-if="state.sequence" class="st-chip st-chip--violet">{{ state.sequence.profileName }}</span>
					</div>
					<div class="composer-metrics">
						<div v-for="item in infoCards" :key="item.label" class="st-metric">
							<span class="st-metric-value">{{ item.value }}</span>
							<span class="st-metric-label">{{ item.label }}</span>
						</div>
					</div>
				</div>
			</div>

			<div class="st-glass">
				<div class="st-title">
					演奏时间线
					<span class="st-sub">点或拖动定位 · 点音符跳到它的起点</span>
				</div>
				<TimelineEditor
					:notes="state.task.notes"
					:strokes="state.sequence?.strokes ?? []"
					:duration-ms="state.task.durationMs"
					:current-ms="state.currentMs"
					:active-stroke="state.activeStroke"
					:current-note-seq="currentNote?.seqNo ?? null" />
			</div>

			<div class="st-glass">
				<div class="st-title">
					虚拟键盘
					<span class="st-sub">{{ state.sequence ? state.sequence.profileName : '尚未映射' }}</span>
				</div>
				<PerformanceKeyboard
					:key-layout="state.sequence?.keyLayout ?? []"
					:key-note-names="keyNoteNames"
					:key-pitches="state.sequence?.keyPitches ?? []"
					:active-keys="state.activeStroke?.keys ?? []"
					:volume="state.volume"
					:per-row="state.sequence && state.sequence.keyCount > 12 ? 10 : 8" />
			</div>
		</template>

		<template #right>
			<div class="st-glass">
				<div class="st-title">AI 助手<span class="st-sub">分析 · 难度 · 键位 · 建议</span></div>
				<AIAssistantPanel
					:task-id="state.task?.id ?? null"
					:current-profile="state.sequence?.profileName ?? ''"
					:mapped-count="state.sequence?.mappedCount ?? 0"
					:unmapped-count="unmappedCount"
					@applied="(id: number) => selectTask(id, { silent: true })" />
			</div>

			<div class="st-glass">
				<div class="st-title">本机演奏<span class="st-sub">需用户主动开启</span></div>
				<PerformanceControlPanel :task-id="state.task?.id ?? null" />
			</div>
		</template>
	</MusicStudioLayout>
</template>

<style scoped>
.composer-head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 14px;
	flex-wrap: wrap;
}

.composer-title {
	display: flex;
	align-items: center;
	gap: 10px;
	flex-wrap: wrap;
}

.composer-name {
	font-size: 17px;
	color: var(--ext-text);
}

.composer-metrics {
	display: flex;
	gap: 8px;
	flex-wrap: wrap;
}

.playing-glow {
	box-shadow: var(--st-shadow), 0 0 0 1px rgba(240, 205, 114, 0.28);
}

.side-list {
	display: flex;
	flex-direction: column;
	gap: 6px;
	max-height: 320px;
	overflow: auto;
}

.side-item {
	display: flex;
	flex-direction: column;
	align-items: flex-start;
	gap: 2px;
	text-align: left;
	border: 1px solid var(--st-edge);
	border-radius: 10px;
	background: var(--st-glass);
	padding: 7px 10px;
	cursor: pointer;
	transition: border-color 0.18s ease, transform 0.18s ease;
}

.side-item:hover {
	border-color: var(--st-edge-hot);
	transform: translateY(-1px);
}

.side-item.on {
	border-color: var(--ext-gold);
	background: linear-gradient(180deg, rgba(240, 205, 114, 0.18), rgba(240, 205, 114, 0.04));
}

.side-name {
	font-size: 12px;
	color: var(--ext-text);
}

.side-meta {
	font-size: 10px;
	color: var(--ext-text-mute);
}

.side-select {
	width: 100%;
	border: 1px solid var(--st-edge);
	border-radius: 10px;
	background: rgba(255, 255, 255, 0.04);
	color: var(--ext-text);
	font-size: 12px;
	padding: 7px 9px;
	font-family: inherit;
}

.side-select:focus {
	outline: none;
	border-color: var(--st-edge-hot);
}

.side-chips {
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
}

.side-metrics {
	display: flex;
	gap: 8px;
}

.side-warn {
	margin-top: 10px;
	display: flex;
	flex-direction: column;
	gap: 4px;
}

.side-warn-item {
	font-size: 10.5px;
	color: var(--ext-gold-light);
	line-height: 1.6;
}
</style>
