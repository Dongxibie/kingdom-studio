<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ExtEmpty, ExtShell, ExtStatusTag } from '@/extensions/_shared/components'
import type { ExtModuleInfo } from '@/extensions/_shared/types/common'
import MusicUploadPanel from '@/extensions/music-agent/components/MusicUploadPanel.vue'
import MusicTaskList from '@/extensions/music-agent/components/MusicTaskList.vue'
import MusicNoteTimeline from '@/extensions/music-agent/components/MusicNoteTimeline.vue'
import MusicKeyboard from '@/extensions/music-agent/components/MusicKeyboard.vue'
import KeySequencePanel from '@/extensions/music-agent/components/KeySequencePanel.vue'
import DesktopAgentPanel from '@/extensions/music-agent/components/DesktopAgentPanel.vue'
import PerformanceControlPanel from '@/extensions/music-agent/components/PerformanceControlPanel.vue'
import {
	deleteMusicTask,
	fetchMusicModuleInfo,
	getMusicTask,
	listInstruments,
	listMusicTasks,
	mapTaskKeys,
} from '@/extensions/music-agent/api/music'
import { buildPlayEvents, play, type PlayEvent, type PlayHandle } from '@/extensions/music-agent/utils/demo-player'
import { formatMs } from '@/extensions/music-agent/utils/note-format'
import type {
	InstrumentProfile,
	KeySequence,
	KeyStroke,
	MusicTaskDetail,
	MusicTaskListItem,
	UnmappedStrategy,
} from '@/extensions/music-agent/types/music'
import { MODE_LABELS, STRATEGY_LABELS } from '@/extensions/music-agent/types/music'

const info = ref<ExtModuleInfo | null>(null)
const errorMessage = ref('')

const tasks = ref<MusicTaskListItem[]>([])
const taskTotal = ref(0)
const taskPage = ref(1)
const taskPages = ref(1)
const taskLoading = ref(false)
const pageSize = 8

const detail = ref<MusicTaskDetail | null>(null)
const activeTaskId = ref<number | null>(null)

const profiles = ref<InstrumentProfile[]>([])
const profileId = ref<number | null>(null)
/** 策略覆盖：空串表示用档案自己的设置 */
const strategyOverride = ref<UnmappedStrategy | ''>('')
const sequence = ref<KeySequence | null>(null)
const mapping = ref(false)

const playing = ref(false)
const currentMs = ref(0)
const activeStroke = ref<KeyStroke | null>(null)
const volume = ref(0.18)

let playHandle: PlayHandle | null = null
let rafId: number | null = null
let wallStart = 0

const activeProfile = computed(() => profiles.value.find((profile) => profile.id === profileId.value) ?? null)

const statusTag = computed(() => {
	if (errorMessage.value) {
		return { text: '后端未连通', tone: 'red' as const }
	}
	if (!info.value) {
		return { text: '自检中', tone: 'mute' as const }
	}
	return { text: '模块已连通 · ' + info.value.phase, tone: 'ok' as const }
})

/** 当前按下的键，用于点亮虚拟键盘 */
const activeKeys = computed(() => activeStroke.value?.keys ?? [])

const strategyOptions: { value: UnmappedStrategy | ''; label: string }[] = [
	{ value: '', label: '按档案设置' },
	{ value: 'SKIP', label: STRATEGY_LABELS.SKIP },
	{ value: 'NEAREST', label: STRATEGY_LABELS.NEAREST },
	{ value: 'SHIFT_OCTAVE', label: STRATEGY_LABELS.SHIFT_OCTAVE },
]

async function loadInfo() {
	errorMessage.value = ''
	try {
		info.value = await fetchMusicModuleInfo()
	} catch (error) {
		info.value = null
		errorMessage.value = error instanceof Error ? error.message : '模块自检失败'
	}
}

async function loadTasks() {
	taskLoading.value = true
	try {
		const result = await listMusicTasks('', taskPage.value, pageSize)
		tasks.value = result.records
		taskTotal.value = result.total
		taskPages.value = result.pages
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取任务列表失败')
	} finally {
		taskLoading.value = false
	}
}

async function loadProfiles() {
	try {
		profiles.value = await listInstruments()
		if (!profileId.value && profiles.value.length) {
			profileId.value = profiles.value[0].id
		}
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取乐器档案失败')
	}
}

async function selectTask(id: number) {
	activeTaskId.value = id
	currentMs.value = 0
	activeStroke.value = null
	try {
		detail.value = await getMusicTask(id)
		await remap()
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取任务详情失败')
	}
}

/** 上传或粘贴解析成功后：刷新列表并直接打开这首曲子 */
async function onParsed(parsed: MusicTaskDetail) {
	taskPage.value = 1
	await loadTasks()
	await selectTask(parsed.id)
}

async function removeTask(id: number) {
	try {
		await ElMessageBox.confirm('删除后这首曲子的音符明细也会一起删掉，确定吗？', '删除任务', {
			confirmButtonText: '删除',
			cancelButtonText: '取消',
			type: 'warning',
		})
	} catch {
		return
	}
	try {
		await deleteMusicTask(id)
		ElMessage.success('已删除')
		if (activeTaskId.value === id) {
			activeTaskId.value = null
			detail.value = null
			sequence.value = null
			stopPlayback()
		}
		await loadTasks()
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '删除失败')
	}
}

/** 按键映射：任务或档案变化时重算 */
async function remap() {
	if (!detail.value || !profileId.value) {
		sequence.value = null
		return
	}
	mapping.value = true
	try {
		sequence.value = await mapTaskKeys(
			detail.value.id,
			profileId.value,
			strategyOverride.value === '' ? undefined : strategyOverride.value,
		)
	} catch (error) {
		sequence.value = null
		ElMessage.error(error instanceof Error ? error.message : '按键映射失败')
	} finally {
		mapping.value = false
	}
}

function stopPlayback() {
	playHandle?.stop()
	playHandle = null
	if (rafId !== null) {
		window.cancelAnimationFrame(rafId)
		rafId = null
	}
	playing.value = false
	activeStroke.value = null
}

function startPlayback() {
	if (!sequence.value || !detail.value) {
		return
	}
	const events = buildPlayEvents(sequence.value)
	if (!events.length) {
		ElMessage.warning('这首曲子在这套乐器上没有可弹的音')
		return
	}
	if (currentMs.value >= detail.value.durationMs) {
		currentMs.value = 0
	}
	stopPlayback()
	playing.value = true
	wallStart = performance.now() - currentMs.value

	playHandle = play({
		events,
		fromMs: currentMs.value,
		volume: volume.value,
		onEvent: (event: PlayEvent) => {
			activeStroke.value = event.stroke
		},
		onEnd: () => {
			stopPlayback()
			currentMs.value = detail.value?.durationMs ?? 0
		},
	})

	const tick = () => {
		if (!playing.value) {
			return
		}
		const elapsed = performance.now() - wallStart
		currentMs.value = Math.min(detail.value?.durationMs ?? 0, elapsed)
		if (activeStroke.value && elapsed > activeStroke.value.startMs + activeStroke.value.durationMs) {
			activeStroke.value = null
		}
		rafId = window.requestAnimationFrame(tick)
	}
	rafId = window.requestAnimationFrame(tick)
}

function togglePlayback() {
	if (playing.value) {
		stopPlayback()
	} else {
		startPlayback()
	}
}

function seek(millis: number) {
	currentMs.value = millis
	if (playing.value) {
		startPlayback()
	}
}

watch([profileId, strategyOverride], () => {
	void remap()
})

onMounted(async () => {
	await loadInfo()
	await loadProfiles()
	await loadTasks()
	if (tasks.value.length) {
		await selectTask(tasks.value[0].id)
	}
})

onBeforeUnmount(stopPlayback)
</script>

<template>
	<ExtShell title="音乐 Agent" subtitle="KINGDOM MUSIC AGENT · 解析 → 映射 → 时间线 → 导出" mark="音">
		<template #actions>
			<ExtStatusTag :text="statusTag.text" :tone="statusTag.tone" />
			<button class="ext-btn" type="button" @click="loadInfo">重新自检</button>
			<button class="ext-btn" type="button" @click="loadTasks">刷新列表</button>
		</template>

		<template #left>
			<div class="ext-panel">
				<div class="ext-panel-title">音乐输入</div>
				<MusicUploadPanel @parsed="onParsed" />
			</div>
			<div class="ext-panel">
				<div class="ext-panel-title">已解析的曲子</div>
				<MusicTaskList
					:items="tasks"
					:active-id="activeTaskId"
					:loading="taskLoading"
					:total="taskTotal"
					:page="taskPage"
					:pages="taskPages"
					@select="selectTask"
					@remove="removeTask"
					@page="(value: number) => { taskPage = value; loadTasks() }" />
			</div>
		</template>

		<template v-if="detail">
			<div class="ext-card">
				<div class="head-row">
					<div>
						<div class="title">{{ detail.name }}</div>
						<div class="sub">
							{{ detail.sourceType === 'MIDI' ? 'MIDI 文件' : '简谱输入' }} ·
							{{ detail.noteCount }} 个音符 · {{ detail.tempoBpm }} BPM · {{ detail.timeSignature }} ·
							音域 {{ detail.pitchRange }}
						</div>
					</div>
					<div class="transport">
						<button class="ext-btn primary" type="button" @click="togglePlayback">
							{{ playing ? '停止' : 'Demo 回放' }}
						</button>
						<button class="ext-btn" type="button" :disabled="!playing && currentMs === 0" @click="seek(0)">回到开头</button>
						<span class="clock">{{ formatMs(currentMs) }}</span>
					</div>
				</div>

				<div class="vol-row">
					<span class="k">音量</span>
					<input v-model.number="volume" type="range" min="0" max="0.5" step="0.01" />
					<span class="now">
						当前音符：{{ activeStroke ? activeStroke.keys.join(' + ') + '（' + activeStroke.noteNames.join(' ') + '）' : '—' }}
					</span>
				</div>

				<MusicNoteTimeline
					:notes="detail.notes"
					:strokes="sequence ? sequence.strokes : []"
					:duration-ms="detail.durationMs"
					:current-ms="currentMs"
					:active-stroke="activeStroke"
					@seek="seek" />
			</div>

			<div class="ext-card" style="margin-top: 16px">
				<div class="ext-panel-title">虚拟键盘（{{ activeProfile ? activeProfile.name : '未选择档案' }}）</div>
				<MusicKeyboard
					:key-layout="sequence ? sequence.keyLayout : []"
					:key-note-names="activeProfile ? activeProfile.keyNoteNames : []"
					:active-keys="activeKeys"
					:empty="!sequence" />
			</div>
		</template>
		<template v-else>
			<ExtEmpty
				tag="v1.1.0"
				title="选一首曲子开始"
				hint="左边上传 .mid 文件或粘贴简谱；解析完成后这里会出现音符时间线、虚拟键盘与 Demo 回放，右边可以切换乐器档案并导出按键序列。" />
		</template>

		<template #right>
			<div class="ext-panel">
				<div class="ext-panel-title">乐器档案</div>
				<select v-model.number="profileId" class="picker">
					<option v-for="profile in profiles" :key="profile.id" :value="profile.id">
						{{ profile.name }} · {{ profile.keyLayout.length }} 键 · {{ MODE_LABELS[profile.mappingMode] }}
					</option>
				</select>
				<div v-if="activeProfile" class="profile-desc">{{ activeProfile.description }}</div>

				<div class="ext-kv" style="margin-top: 10px">
					<span class="k">超范围策略</span>
					<span class="v">
						<select v-model="strategyOverride" class="picker small">
							<option v-for="option in strategyOptions" :key="option.value" :value="option.value">
								{{ option.label }}
							</option>
						</select>
					</span>
				</div>
				<div v-if="activeProfile" class="ext-hint">
					档案默认：{{ STRATEGY_LABELS[activeProfile.unmappedStrategy] }}；
					基准音高 {{ activeProfile.basePitch }}、移调 {{ activeProfile.transpose }}、升降八度 {{ activeProfile.octaveShift }}。
				</div>
			</div>

			<div class="ext-panel">
				<div class="ext-panel-title">按键序列</div>
				<div v-if="mapping" class="ext-hint">映射中…</div>
				<KeySequencePanel :sequence="sequence" />
			</div>

			<div v-if="info" class="ext-panel">
				<div class="ext-panel-title">模块信息</div>
				<div class="ext-card">
					<div class="ext-kv"><span class="k">名称</span><span class="v">{{ info.name }} · {{ info.englishName }}</span></div>
					<div class="ext-kv"><span class="k">阶段</span><span class="v">{{ info.phase }}</span></div>
					<div class="ext-kv"><span class="k">数据表</span><span class="v">{{ info.plannedTables.join('、') }}</span></div>
				</div>
			</div>

			<div class="ext-panel">
				<div class="ext-panel-title">演奏控制（本机演奏）</div>
				<PerformanceControlPanel :task-id="activeTaskId" />
			</div>

			<div class="ext-panel">
				<div class="ext-panel-title">桌面代理（模拟）</div>
				<DesktopAgentPanel
					:task-id="activeTaskId"
					:profile-id="profileId"
					:strategy="strategyOverride === '' ? undefined : strategyOverride" />
			</div>

			<div class="ext-panel">
				<div class="ext-panel-title">边界</div>
				<div class="ext-hint">
					Demo 回放只在网页内模拟（时间线、音符高亮、按键动画与合成音）。
					本模块不驱动系统输入：真实演奏由独立的 Desktop Agent 通过 WebSocket 接收按键序列后自行处理。
				</div>
			</div>
		</template>
	</ExtShell>
</template>

<style scoped>
.head-row {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 14px;
	flex-wrap: wrap;
}

.title {
	font-size: 16px;
	font-weight: 600;
}

.sub {
	margin-top: 4px;
	font-family: var(--ext-font-mono);
	font-size: 11.5px;
	color: var(--ext-text-mute);
}

.transport {
	display: flex;
	align-items: center;
	gap: 8px;
}

.clock {
	font-family: var(--ext-font-mono);
	font-size: 13px;
	color: var(--ext-gold-light);
	min-width: 68px;
	text-align: right;
}

.vol-row {
	display: flex;
	align-items: center;
	gap: 10px;
	margin: 12px 0 10px;
	font-size: 12px;
	color: var(--ext-text-dim);
}

.vol-row input[type='range'] {
	flex: 0 0 120px;
}

.now {
	font-family: var(--ext-font-mono);
	font-size: 11.5px;
	color: var(--ext-text-mute);
}

.picker {
	width: 100%;
	box-sizing: border-box;
	background: var(--ext-bg-raised);
	border: 1px solid var(--ext-line);
	border-radius: 8px;
	color: var(--ext-text);
	font-family: var(--ext-font-mono);
	font-size: 12px;
	padding: 7px 8px;
	outline: none;
}

.picker.small {
	width: auto;
	margin-left: 6px;
}

.picker:focus {
	border-color: var(--ext-gold);
}

.profile-desc {
	margin-top: 8px;
	font-size: 11.5px;
	line-height: 1.8;
	color: var(--ext-text-mute);
}
</style>
