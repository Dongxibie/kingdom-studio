<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ExtEmpty, ExtStatusTag } from '@/extensions/_shared/components'
import MusicStudioLayout from '@/extensions/music-studio/components/MusicStudioLayout.vue'
import MusicLibraryRail from '@/extensions/music-studio/components/MusicLibraryRail.vue'
import SongAnalysisCard from '@/extensions/music-studio/components/SongAnalysisCard.vue'
import ShowModeOverlay from '@/extensions/music-studio/components/ShowModeOverlay.vue'
import MusicUploadPanel from '@/extensions/music-agent/components/MusicUploadPanel.vue'
import { fetchMusicModuleInfo, parseJianpu, deleteMusicTask } from '@/extensions/music-agent/api/music'
import { formatDuration } from '@/extensions/music-agent/utils/note-format'
import { useStudioSession } from '@/extensions/music-studio/composables/useStudioSession'
import { DEMO_SONGS, type DemoSong } from '@/extensions/music-studio/types/demo'
import { starsText } from '@/extensions/music-studio/types/studio'
import type { ExtModuleInfo } from '@/extensions/_shared/types/common'
import { sourceLabel, type MusicTaskDetail } from '@/extensions/music-agent/types/music'

/**
 * 曲目台（Dashboard）。
 *
 * 进门先看到三件事：我有哪些曲子、怎么导入新曲子、想试一下就用演示曲。
 * 所以这一页不用表格：曲目是卡片墙（带音域、时长、来源），导入与演示各占一块。
 */
const router = useRouter()
const { state, loadTasks, selectTask, adopt, switchFavorite } = useStudioSession()

/** 演示模式：全屏铺开的 Show Mode */
const showMode = ref(false)
/** 只看收藏 */
const favoriteOnly = ref(false)

function toggleFavoriteOnly() {
	favoriteOnly.value = !favoriteOnly.value
	void loadTasks(1, keyword.value.trim(), favoriteOnly.value)
}

/** 卡片上的创建时间只留日期，列表里不需要秒 */
function dayOf(value: string) {
	return value ? value.slice(0, 10) : ''
}

const info = ref<ExtModuleInfo | null>(null)
const importing = ref('')
const keyword = ref('')

const recent = computed(() => state.tasks)
const analysisHint = computed(() =>
	state.task ? '「' + state.task.name + '」的难度、音域与推荐键位都在这里。' : '点一张曲目卡片，这里会给出它的分析卡。')

async function loadInfo() {
	try {
		info.value = await fetchMusicModuleInfo()
	} catch {
		info.value = null
	}
}

async function search() {
	await loadTasks(1, keyword.value.trim())
}

async function importDemo(song: DemoSong) {
	importing.value = song.name
	try {
		const detail: MusicTaskDetail = await parseJianpu(song.name + '（演示）', song.jianpu)
		await adopt(detail)
		ElMessage.success('已导入「' + song.name + '」，正在打开编排台')
		void router.push('/extensions/music-studio/composer')
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '导入演示曲失败')
	} finally {
		importing.value = ''
	}
}

async function open(id: number) {
	await selectTask(id, { silent: true })
}

async function remove(id: number, name: string) {
	try {
		await deleteMusicTask(id)
		if (state.task?.id === id) {
			state.task = null
			state.sequence = null
		}
		await loadTasks(state.page)
		ElMessage.success('已删除「' + name + '」')
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '删除失败')
	}
}

async function onParsed(detail: MusicTaskDetail) {
	await adopt(detail)
	ElMessage.success('解析完成，已打开编排台')
	void router.push('/extensions/music-studio/composer')
}

onMounted(async () => {
	await Promise.all([loadInfo(), loadTasks(1)])
})
</script>

<template>
	<MusicStudioLayout
		title="AI 演奏工作室"
		subtitle="曲目台 · 导入 → 编排 → 回放 → 导出"
		mark="♪">
		<template #actions>
			<ExtStatusTag :text="info ? '模块已就绪 · ' + info.phase : '模块状态未知'" :tone="info ? 'ok' : 'mute'" />
			<button class="st-btn" type="button" :class="{ 'st-btn--primary': favoriteOnly }" @click="toggleFavoriteOnly">
				{{ favoriteOnly ? '只看收藏 · 开' : '只看收藏' }}
			</button>
			<button class="st-btn st-btn--primary" type="button" @click="showMode = true">演示模式</button>
			<button class="st-btn" type="button" @click="loadTasks(1)">刷新曲库</button>
		</template>

		<template #left>
			<MusicLibraryRail @open="open" />

			<div class="st-glass">
				<div class="st-title">演示歌曲<span class="st-sub">点一下直接导入</span></div>
				<div class="demo-list">
					<button
						v-for="song in DEMO_SONGS"
						:key="song.name"
						class="demo-card"
						type="button"
						:disabled="importing === song.name"
						@click="importDemo(song)">
						<span class="demo-name">{{ song.name }}</span>
						<span class="demo-hint">{{ song.hint }}</span>
						<span class="demo-action">{{ importing === song.name ? '导入中…' : '导入这首' }}</span>
					</button>
				</div>
			</div>

			<div class="st-glass">
				<div class="st-title">曲目分析<span class="st-sub">难度 · 音域 · 推荐键位</span></div>
				<SongAnalysisCard :analysis="state.analysis" :loading="state.insightLoading" :hint="analysisHint" />
			</div>

			<div class="st-glass">
				<div class="st-title">工作室能力</div>
				<ul class="cap-list">
					<li v-for="(item, index) in info?.capabilities ?? []" :key="index">{{ item }}</li>
					<li v-if="!info" class="cap-mute">模块自检未返回，稍后重试</li>
				</ul>
				<p class="st-label">
					解析与映射都在后端完成：上传的曲子会被拆成音符时间线，再按乐器档案展开成按键序列。
				</p>
			</div>
		</template>

		<div class="dash">
			<div class="st-glass">
				<div class="st-title">导入歌曲<span class="st-sub">MIDI 文件或简谱文本</span></div>
				<MusicUploadPanel @parsed="onParsed" />
			</div>

			<div class="st-glass">
				<div class="dash-head">
					<div class="st-title">最近曲目<span class="st-sub">共 {{ state.total }} 首</span></div>
					<div class="dash-search">
						<input v-model="keyword" type="text" placeholder="按曲名搜索" @keydown.enter="search" />
						<button class="st-btn" type="button" @click="search">搜索</button>
					</div>
				</div>

				<div v-if="state.tasksLoading" class="dash-loading">正在读取曲库…</div>
				<ExtEmpty
					v-else-if="!recent.length"
					tag="曲库为空"
					title="还没有曲子"
					hint="上传一个 MIDI，或粘贴一段简谱；也可以直接用左边的演示曲。" />
				<div v-else class="song-grid">
					<article
						v-for="song in recent"
						:key="song.id"
						class="song-card"
						:class="{ on: state.task?.id === song.id }"
						@click="open(song.id)">
						<header class="song-head">
							<button
								class="song-star"
								:class="{ on: song.favorite === 1 }"
								type="button"
								:title="song.favorite === 1 ? '取消收藏' : '收藏这首'"
								@click.stop="switchFavorite(song.id)">
								{{ song.favorite === 1 ? '★' : '☆' }}
							</button>
							<span class="song-name">{{ song.name }}</span>
							<span class="st-chip">{{ sourceLabel(song.sourceType) }}</span>
						</header>
						<div class="song-meta">
							<span>{{ song.noteCount }} 个音</span>
							<i>·</i>
							<span>{{ song.tempoBpm }} BPM</span>
							<i>·</i>
							<span>{{ song.timeSignature }}</span>
						</div>
						<div class="song-foot">
							<span class="song-range">{{ song.pitchRange }}</span>
							<span class="song-duration">{{ formatDuration(song.durationMs) }}</span>
						</div>
						<div class="song-difficulty">
							<span class="song-stars" :title="song.difficultyLabel">{{ starsText(song.difficultyStars) }}</span>
							<span class="song-difficulty-label">{{ song.difficultyLabel }}</span>
							<span class="song-day">{{ dayOf(song.createTime) }}</span>
						</div>
						<div class="song-actions">
							<button class="st-btn st-btn--ghost" type="button" @click.stop="open(song.id)">打开编排台</button>
							<button class="st-btn st-btn--ghost" type="button" @click.stop="remove(song.id, song.name)">删除</button>
						</div>
					</article>
				</div>
			</div>
		</div>
	</MusicStudioLayout>

	<ShowModeOverlay v-if="showMode" @close="showMode = false" />
</template>

<style scoped>
.dash {
	display: flex;
	flex-direction: column;
	gap: 14px;
}

.dash-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 10px;
	flex-wrap: wrap;
}

.dash-search {
	display: flex;
	gap: 7px;
}

.dash-search input {
	border: 1px solid var(--st-edge);
	border-radius: 10px;
	background: rgba(255, 255, 255, 0.04);
	color: var(--ext-text);
	font-size: 12px;
	padding: 6px 10px;
	font-family: inherit;
	width: 190px;
}

.dash-search input:focus {
	outline: none;
	border-color: var(--st-edge-hot);
}

.dash-loading {
	font-size: 11.5px;
	color: var(--ext-text-mute);
	padding: 14px 0;
}

.song-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
	gap: 10px;
}

.song-card {
	display: flex;
	flex-direction: column;
	gap: 7px;
	border: 1px solid var(--st-edge);
	border-radius: 12px;
	background: linear-gradient(160deg, rgba(255, 255, 255, 0.06), rgba(255, 255, 255, 0.02));
	padding: 11px 12px;
	cursor: pointer;
	transition: border-color 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
	animation: st-rise 0.3s ease both;
}

.song-card:hover {
	border-color: var(--st-edge-hot);
	transform: translateY(-2px);
	box-shadow: 0 14px 28px rgba(0, 0, 0, 0.42);
}

.song-card.on {
	border-color: var(--ext-gold);
	box-shadow: 0 0 0 1px rgba(240, 205, 114, 0.35);
}

.song-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
}

.song-star {
	border: 0;
	background: transparent;
	color: var(--ext-text-mute);
	font-size: 14px;
	cursor: pointer;
	padding: 0;
	line-height: 1;
	transition: color 0.18s ease, transform 0.18s ease;
}

.song-star:hover {
	transform: scale(1.15);
}

.song-star.on {
	color: var(--ext-gold-light);
}

.song-difficulty {
	display: flex;
	align-items: baseline;
	gap: 6px;
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.song-stars {
	color: var(--ext-gold-light);
	letter-spacing: 1px;
}

.song-day {
	margin-left: auto;
	font-family: var(--ext-font-mono);
}

.song-name {
	font-size: 13.5px;
	color: var(--ext-text);
}

.song-meta {
	display: flex;
	gap: 5px;
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.song-meta i {
	font-style: normal;
	opacity: 0.5;
}

.song-foot {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
	gap: 8px;
	font-family: var(--ext-font-mono);
	font-size: 11px;
	color: var(--ext-text-dim);
}

.song-duration {
	color: var(--ext-text-mute);
}

.song-actions {
	display: flex;
	gap: 6px;
	margin-top: 2px;
}

.demo-list {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.demo-card {
	display: flex;
	flex-direction: column;
	align-items: flex-start;
	gap: 2px;
	text-align: left;
	border: 1px solid var(--st-edge);
	border-radius: 11px;
	background: var(--st-glass);
	padding: 9px 11px;
	cursor: pointer;
	transition: border-color 0.18s ease, transform 0.18s ease;
}

.demo-card:hover:not(:disabled) {
	border-color: var(--st-edge-hot);
	transform: translateY(-1px);
}

.demo-card:disabled {
	opacity: 0.6;
	cursor: default;
}

.demo-name {
	font-size: 12.5px;
	color: var(--ext-text);
}

.demo-hint {
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.demo-action {
	margin-top: 3px;
	font-size: 10.5px;
	color: var(--ext-gold-light);
}

.cap-list {
	margin: 0;
	padding-left: 16px;
	display: flex;
	flex-direction: column;
	gap: 4px;
	font-size: 11px;
	color: var(--ext-text-dim);
	line-height: 1.7;
}

.cap-mute {
	color: var(--ext-text-mute);
}
</style>
