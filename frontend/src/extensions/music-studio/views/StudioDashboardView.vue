<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ExtEmpty, ExtStatusTag } from '@/extensions/_shared/components'
import MusicStudioLayout from '@/extensions/music-studio/components/MusicStudioLayout.vue'
import MusicUploadPanel from '@/extensions/music-agent/components/MusicUploadPanel.vue'
import { fetchMusicModuleInfo, parseJianpu, deleteMusicTask } from '@/extensions/music-agent/api/music'
import { formatDuration } from '@/extensions/music-agent/utils/note-format'
import { useStudioSession } from '@/extensions/music-studio/composables/useStudioSession'
import { DEMO_SONGS, type DemoSong } from '@/extensions/music-studio/types/demo'
import type { ExtModuleInfo } from '@/extensions/_shared/types/common'
import type { MusicTaskDetail } from '@/extensions/music-agent/types/music'

/**
 * 曲目台（Dashboard）。
 *
 * 进门先看到三件事：我有哪些曲子、怎么导入新曲子、想试一下就用演示曲。
 * 所以这一页不用表格：曲目是卡片墙（带音域、时长、来源），导入与演示各占一块。
 */
const router = useRouter()
const { state, loadTasks, selectTask, adopt } = useStudioSession()

const info = ref<ExtModuleInfo | null>(null)
const importing = ref('')
const keyword = ref('')

const recent = computed(() => state.tasks)

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
	await selectTask(id)
	void router.push('/extensions/music-studio/composer')
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
			<button class="st-btn" type="button" @click="loadTasks(1)">刷新曲库</button>
		</template>

		<template #left>
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
							<span class="song-name">{{ song.name }}</span>
							<span class="st-chip">{{ song.sourceType === 'MIDI' ? 'MIDI' : '简谱' }}</span>
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
						<div class="song-actions">
							<button class="st-btn st-btn--ghost" type="button" @click.stop="open(song.id)">打开编排台</button>
							<button class="st-btn st-btn--ghost" type="button" @click.stop="remove(song.id, song.name)">删除</button>
						</div>
					</article>
				</div>
			</div>
		</div>
	</MusicStudioLayout>
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
