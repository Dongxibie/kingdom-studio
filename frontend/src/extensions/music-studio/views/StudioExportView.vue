<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ExtEmpty } from '@/extensions/_shared/components'
import MusicStudioLayout from '@/extensions/music-studio/components/MusicStudioLayout.vue'
import ExportCenter from '@/extensions/music-studio/components/ExportCenter.vue'
import PerformancePresetBar from '@/extensions/music-studio/components/PerformancePresetBar.vue'
import TimelineEditor from '@/extensions/music-studio/components/TimelineEditor.vue'
import { formatDuration } from '@/extensions/music-agent/utils/note-format'
import { useStudioSession } from '@/extensions/music-studio/composables/useStudioSession'
import type { PerformancePreset } from '@/extensions/music-studio/types/studio'

/**
 * 导出中心（Export Center）。
 *
 * 这一页只做一件事：把「按键序列 → 演奏脚本」的结果摊开，三种格式各一张卡。
 * 计划本身仍由后端从原有映射结果算出（这一层只展示与下载）。
 */
const router = useRouter()
const { state, loadTasks, selectTask, applyPreset, savePreset, deletePresetById } = useStudioSession()

function onApplyPreset(preset: PerformancePreset) {
	void applyPreset(preset)
}

function onSavePreset(name: string) {
	void savePreset(name)
}

function onRemovePreset(preset: PerformancePreset) {
	void deletePresetById(preset)
}

const formats = [
	{ name: 'AutoHotkey', value: 'AHK', accent: 'violet', use: '本机按键脚本，含暂停与急停键' },
	{ name: 'JSON', value: 'JSON', accent: 'hot', use: '结构化演奏计划，便于二次开发' },
	{ name: 'TXT', value: 'TXT', accent: 'ok', use: '带时间戳的按键表，照着练' },
]

const summary = computed(() => {
	const task = state.task
	if (!task) {
		return []
	}
	return [
		{ label: '曲目', value: task.name },
		{ label: '音符数', value: String(task.noteCount) },
		{ label: '时长', value: formatDuration(task.durationMs) },
		{ label: '乐器档案', value: state.sequence?.profileName ?? '未映射' },
		{ label: '按键组', value: String(state.sequence?.strokes.length ?? 0) },
		{ label: '当前方案', value: state.presets.find((item) => item.id === state.activePresetId)?.name ?? '未选方案' },
	]
})

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
		title="导出中心"
		subtitle="AI 演奏工作室 · 演奏脚本与结构数据"
		mark="♪">
		<template #actions>
			<button class="st-btn" type="button" @click="router.push('/extensions/music-studio/composer')">回到编排台</button>
		</template>

		<template #left>
			<div class="st-glass">
				<div class="st-title">三种格式</div>
				<div class="fmt-list">
					<div v-for="item in formats" :key="item.value" class="fmt-item">
						<span class="fmt-name" :class="'fmt-name--' + item.accent">{{ item.name }}</span>
						<span class="fmt-use">{{ item.use }}</span>
					</div>
				</div>
			</div>

			<div class="st-glass">
				<div class="st-title">导出的边界</div>
				<ul class="edge-list">
					<li>导出的是按键动作与时间戳，不会自动在后台运行。</li>
					<li>真的要本机演奏，需要在编排台里主动开启，并确认目标窗口。</li>
					<li>按键序列来自当前乐器档案与超范围策略，换档案请重新导出。</li>
				</ul>
			</div>
		</template>

		<div v-if="!state.task" class="st-glass">
			<ExtEmpty tag="未选择曲目" title="先选一首曲子" hint="到曲目台导入或打开一首，再回来导出。" />
		</div>

		<template v-else>
			<div class="st-glass">
				<div class="st-title">当前方案</div>
				<div class="sum-grid">
					<div v-for="item in summary" :key="item.label" class="st-metric">
						<span class="st-metric-value">{{ item.value }}</span>
						<span class="st-metric-label">{{ item.label }}</span>
					</div>
				</div>
			</div>

			<div class="st-glass">
				<PerformancePresetBar
					:presets="state.presets"
					:active-id="state.activePresetId"
					:current-profile-name="state.sequence?.profileName ?? ''"
					:busy="state.mapping"
					@apply="onApplyPreset"
					@save="onSavePreset"
					@remove="onRemovePreset" />
			</div>

			<div class="st-glass">
				<div class="st-title">演奏脚本<span class="st-sub">生成 → 预览 → 复制或下载</span></div>
				<ExportCenter
					:task-id="state.task.id"
					:profile-id="state.profileId"
					:strategy="state.strategy"
					:preset-id="state.activePresetId" />
			</div>

			<div class="st-glass">
				<div class="st-title">按键轨<span class="st-sub">导出的就是这上面的动作</span></div>
				<TimelineEditor
					:notes="state.task.notes"
					:strokes="state.sequence?.strokes ?? []"
					:duration-ms="state.task.durationMs"
					:current-ms="state.currentMs"
					:active-stroke="state.activeStroke"
					:current-note-seq="null"
					:height="140" />
			</div>
		</template>
	</MusicStudioLayout>
</template>

<style scoped>
.sum-grid {
	display: flex;
	gap: 8px;
	flex-wrap: wrap;
}

.fmt-list {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.fmt-item {
	display: flex;
	flex-direction: column;
	gap: 2px;
	border: 1px solid var(--st-edge);
	border-radius: 10px;
	background: var(--st-glass);
	padding: 8px 11px;
}

.fmt-name {
	font-family: var(--ext-font-mono);
	font-size: 12.5px;
}

.fmt-name--violet {
	color: var(--ext-neon-violet);
}

.fmt-name--hot {
	color: var(--ext-gold-light);
}

.fmt-name--ok {
	color: #7ed6a5;
}

.fmt-use {
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.edge-list {
	margin: 0;
	padding-left: 16px;
	display: flex;
	flex-direction: column;
	gap: 5px;
	font-size: 11px;
	color: var(--ext-text-dim);
	line-height: 1.7;
}
</style>
