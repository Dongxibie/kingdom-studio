<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createShare, describeShare, importShare, listShares } from '@/extensions/music-agent/api/music'
import { formatDuration } from '@/extensions/music-agent/utils/note-format'
import { useStudioSession } from '@/extensions/music-studio/composables/useStudioSession'
import type { PerformanceShare } from '@/extensions/music-studio/types/studio'

/**
 * 音乐库（左侧栏）。
 *
 * 四个页签对应四种「找一首曲子」的方式：我的歌曲（库里全部）、最近演奏（刚弹过的）、
 * 收藏（挑出来要常练的）、分享（演奏码：别人的曲子导进来、自己的曲子发出去）。
 *
 * 「最近演奏」只在本地记 —— 它记录的是「我最近点了什么」，属于使用者的习惯，不该写进服务端。
 */
const emit = defineEmits<{
	/** 让外层决定打开方式（曲目台跳编排台、回放页保持原页） */
	open: [taskId: number]
}>()

const { state, switchFavorite, loadTasks, openById } = useStudioSession()

const tab = ref<'songs' | 'recent' | 'favorite' | 'share'>('songs')
const keyword = ref('')

const shares = ref<PerformanceShare[]>([])
const sharesLoading = ref(false)
const codeInput = ref('')
const shareLoading = ref(false)
const creator = ref('')
const preview = ref<PerformanceShare | null>(null)

const TABS: { value: typeof tab.value; label: string; hint: string }[] = [
	{ value: 'songs', label: '我的歌曲', hint: '曲库全部' },
	{ value: 'recent', label: '最近演奏', hint: '刚弹过的' },
	{ value: 'favorite', label: '收藏', hint: '常练的' },
	{ value: 'share', label: '分享', hint: '演奏码' },
]

const list = computed(() => {
	if (tab.value === 'recent') {
		return state.recent
	}
	return state.tasks
})

async function switchTab(value: typeof tab.value) {
	tab.value = value
	if (value === 'share') {
		await loadShares()
		return
	}
	if (value === 'songs') {
		await loadTasks(1, keyword.value.trim(), false)
		return
	}
	if (value === 'favorite') {
		await loadTasks(1, keyword.value.trim(), true)
		return
	}
	// 最近演奏用本地记录，不发请求
	if (!state.tasks.length) {
		await loadTasks(1, '', false)
	}
}

async function search() {
	if (tab.value === 'share' || tab.value === 'recent') {
		return
	}
	await loadTasks(1, keyword.value.trim(), tab.value === 'favorite')
}

async function loadShares() {
	sharesLoading.value = true
	try {
		shares.value = await listShares()
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取分享列表失败')
	} finally {
		sharesLoading.value = false
	}
}

/** 生成演奏码：带上当前方案，快照里会记录速度与最小间隔 */
async function share() {
	if (!state.task) {
		ElMessage.warning('先选一首曲子')
		return
	}
	shareLoading.value = true
	try {
		const created = await createShare({
			taskId: state.task.id,
			creator: creator.value.trim() || undefined,
			presetId: state.activePresetId,
		})
		shares.value = [created, ...shares.value]
		ElMessage.success('已生成演奏码 ' + created.shareCode)
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '生成演奏码失败')
	} finally {
		shareLoading.value = false
	}
}

/** 先看摘要：拿到码的人不导入也能知道这是首什么曲子 */
async function peek() {
	const code = codeInput.value.trim()
	if (!code) {
		ElMessage.warning('请输入演奏码')
		return
	}
	try {
		preview.value = await describeShare(code)
	} catch (error) {
		preview.value = null
		ElMessage.error(error instanceof Error ? error.message : '没有找到这个演奏码')
	}
}

/** 导入：在本库里还原成一首新曲目（复制一份，不影响原作者） */
async function doImport(code?: string) {
	const value = (code ?? codeInput.value).trim()
	if (!value) {
		ElMessage.warning('请输入演奏码')
		return
	}
	shareLoading.value = true
	try {
		const imported = await importShare(value)
		await loadTasks(1, '', false)
		if (tab.value === 'share') {
			// 导入次数会变，分享列表要跟着刷新，不然看到的还是旧数字
			await loadShares()
		}
		preview.value = null
		codeInput.value = ''
		if (imported.importedTaskId) {
			await openById(imported.importedTaskId)
			emit('open', imported.importedTaskId)
		}
		ElMessage.success(imported.message ?? '已导入')
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '导入失败')
	} finally {
		shareLoading.value = false
	}
}

async function openSong(taskId: number) {
	await openById(taskId)
	emit('open', taskId)
}

onMounted(async () => {
	if (!state.tasks.length) {
		await loadTasks(1, '', false)
	}
})
</script>

<template>
	<div class="rail st-glass">
		<div class="rail-tabs">
			<button
				v-for="item in TABS"
				:key="item.value"
				class="rail-tab"
				:class="{ on: tab === item.value }"
				type="button"
				:title="item.hint"
				@click="switchTab(item.value)">
				{{ item.label }}
			</button>
		</div>

		<!-- 分享：演奏码 -->
		<template v-if="tab === 'share'">
			<div class="rail-share">
				<input v-model="codeInput" type="text" placeholder="输入演奏码，例如 KS-MUSIC-2026-A001" @keydown.enter="peek" />
				<div class="rail-share-actions">
					<button class="st-btn" type="button" :disabled="shareLoading" @click="peek">查看</button>
					<button class="st-btn st-btn--primary" type="button" :disabled="shareLoading" @click="doImport()">导入</button>
				</div>
				<input v-model="creator" type="text" placeholder="署名（可选）" />
				<button class="st-btn" type="button" :disabled="shareLoading || !state.task" @click="share">
					把当前曲子生成演奏码
				</button>
				<p class="rail-hint">演奏码里带完整快照：别人导入后就能直接演奏与导出，不需要先有你这份数据库。</p>
			</div>

			<div v-if="preview" class="rail-preview">
				<div class="rail-preview-title">{{ preview.title }}</div>
				<div class="rail-preview-meta">
					<span>{{ preview.creator }}</span>
					<i>·</i>
					<span>{{ preview.difficulty }}</span>
				</div>
				<ul class="rail-preview-list">
					<li v-for="(item, index) in preview.highlights" :key="index">{{ item }}</li>
				</ul>
				<button class="st-btn st-btn--primary" type="button" @click="doImport(preview.shareCode)">导入这首</button>
			</div>

			<div class="rail-list">
				<div v-if="sharesLoading" class="rail-hint">正在读取…</div>
				<button v-for="item in shares" :key="item.id" class="rail-share-card" type="button" @click="codeInput = item.shareCode; peek()">
					<div class="rail-share-code">{{ item.shareCode }}</div>
					<div class="rail-share-title">{{ item.title }}</div>
					<div class="rail-share-meta">
						{{ item.difficulty }} · 被导入 {{ item.importCount }} 次
					</div>
				</button>
				<p v-if="!sharesLoading && !shares.length" class="rail-hint">还没有分享过曲子。</p>
			</div>
		</template>

		<!-- 曲目列表 -->
		<template v-else>
			<div class="rail-search">
				<input v-model="keyword" type="text" placeholder="搜索曲名" @keydown.enter="search" />
				<button class="st-btn" type="button" @click="search">搜索</button>
			</div>

			<div class="rail-list">
				<div v-if="state.tasksLoading && !list.length" class="rail-hint">正在读取…</div>
				<p v-else-if="!list.length" class="rail-hint">
					{{ tab === 'recent' ? '还没演奏过：去编排台按一次播放就会记在这里。' : tab === 'favorite' ? '还没有收藏的曲子。' : '曲库是空的，先导入一首。' }}
				</p>
				<button
					v-for="song in list"
					:key="song.id"
					class="rail-item"
					:class="{ on: state.task?.id === song.id }"
					type="button"
					@click="openSong(song.id)">
					<div class="rail-item-head">
						<button
							class="rail-star"
							:class="{ on: song.favorite === 1 }"
							type="button"
							@click.stop="switchFavorite(song.id)">
							{{ song.favorite === 1 ? '★' : '☆' }}
						</button>
						<span class="rail-item-name">{{ song.name }}</span>
					</div>
					<div class="rail-item-meta">
						{{ song.noteCount }} 音 · {{ song.tempoBpm }} BPM · {{ formatDuration(song.durationMs) }}
					</div>
					<div class="rail-item-foot">
						<span class="rail-item-stars">{{ '★'.repeat(song.difficultyStars) }}</span>
						<span>{{ song.difficultyLabel }}</span>
					</div>
				</button>
			</div>
		</template>
	</div>
</template>

<style scoped>
.rail {
	display: flex;
	flex-direction: column;
	gap: 10px;
	padding: 12px;
}

.rail-tabs {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 4px;
}

.rail-tab {
	border: 1px solid var(--st-edge);
	background: transparent;
	color: var(--ext-text-mute);
	border-radius: 8px;
	font-size: 10.5px;
	padding: 5px 2px;
	cursor: pointer;
	transition: color 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.rail-tab:hover {
	color: var(--ext-text-dim);
}

.rail-tab.on {
	border-color: var(--st-violet);
	background: var(--st-violet-soft);
	color: #d9ccff;
}

.rail-search,
.rail-share {
	display: flex;
	flex-direction: column;
	gap: 6px;
}

.rail-share-actions {
	display: flex;
	gap: 6px;
}

.rail-search input,
.rail-share input {
	width: 100%;
	border: 1px solid var(--st-edge);
	border-radius: 9px;
	background: rgba(255, 255, 255, 0.04);
	color: var(--ext-text);
	font-size: 11.5px;
	padding: 7px 9px;
	font-family: inherit;
}

.rail-search input:focus,
.rail-share input:focus {
	outline: none;
	border-color: var(--st-violet);
}

.rail-list {
	display: flex;
	flex-direction: column;
	gap: 6px;
	max-height: 460px;
	overflow: auto;
}

.rail-item {
	display: flex;
	flex-direction: column;
	gap: 3px;
	text-align: left;
	border: 1px solid var(--st-edge);
	border-radius: 10px;
	background: var(--st-glass);
	padding: 8px 10px;
	cursor: pointer;
	transition: border-color 0.18s ease, transform 0.18s ease, background 0.18s ease;
}

.rail-item:hover {
	border-color: var(--st-violet);
	transform: translateY(-1px);
}

.rail-item.on {
	border-color: var(--st-violet);
	background: var(--st-violet-soft);
}

.rail-item-head {
	display: flex;
	align-items: center;
	gap: 6px;
}

.rail-star {
	border: 0;
	background: transparent;
	color: var(--ext-text-mute);
	font-size: 12px;
	padding: 0;
	cursor: pointer;
	line-height: 1;
}

.rail-star.on {
	color: var(--st-violet);
}

.rail-item-name {
	font-size: 12px;
	color: var(--ext-text);
}

.rail-item-meta {
	font-size: 10px;
	color: var(--ext-text-mute);
}

.rail-item-foot {
	display: flex;
	align-items: baseline;
	gap: 6px;
	font-size: 10px;
	color: var(--ext-text-mute);
}

.rail-item-stars {
	color: var(--st-violet);
	letter-spacing: 1px;
}

.rail-preview {
	border: 1px solid var(--st-violet);
	border-radius: 10px;
	background: var(--st-violet-soft);
	padding: 9px 11px;
	display: flex;
	flex-direction: column;
	gap: 5px;
	animation: st-rise 0.28s ease both;
}

.rail-preview-title {
	font-size: 12.5px;
	color: var(--ext-text);
}

.rail-preview-meta {
	display: flex;
	gap: 6px;
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.rail-preview-meta i {
	font-style: normal;
	opacity: 0.5;
}

.rail-preview-list {
	margin: 0;
	padding-left: 15px;
	display: flex;
	flex-direction: column;
	gap: 3px;
	font-size: 10.5px;
	color: var(--ext-text-dim);
}

.rail-share-card {
	text-align: left;
	border: 1px solid var(--st-edge);
	border-radius: 10px;
	background: var(--st-glass);
	padding: 8px 10px;
	cursor: pointer;
	display: flex;
	flex-direction: column;
	gap: 2px;
}

.rail-share-code {
	font-family: var(--ext-font-mono);
	font-size: 11px;
	color: var(--st-green);
}

.rail-share-title {
	font-size: 12px;
	color: var(--ext-text);
}

.rail-share-meta {
	font-size: 10px;
	color: var(--ext-text-mute);
}

.rail-hint {
	margin: 0;
	font-size: 10.5px;
	color: var(--ext-text-mute);
	line-height: 1.7;
}
</style>
