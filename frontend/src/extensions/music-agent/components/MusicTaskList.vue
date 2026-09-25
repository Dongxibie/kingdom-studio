<script setup lang="ts">
import type { MusicTaskListItem } from '@/extensions/music-agent/types/music'
import { formatDuration } from '@/extensions/music-agent/utils/note-format'

interface Props {
	items: MusicTaskListItem[]
	activeId: number | null
	loading: boolean
	total: number
	page: number
	pages: number
}

defineProps<Props>()
const emit = defineEmits<{
	select: [id: number]
	remove: [id: number]
	page: [page: number]
}>()
</script>

<template>
	<div class="list">
		<div v-if="loading" class="tip">读取中…</div>
		<div v-else-if="!items.length" class="tip">还没有解析过任何曲子</div>
		<div
			v-for="item in items"
			v-else
			:key="item.id"
			class="row"
			:class="{ on: item.id === activeId }"
			@click="emit('select', item.id)">
			<div class="top">
				<span class="name">{{ item.name }}</span>
				<span class="src">{{ item.sourceType === 'MIDI' ? 'MIDI' : '简谱' }}</span>
			</div>
			<div class="meta">
				{{ item.noteCount }} 音 · {{ item.pitchRange }} · {{ item.tempoBpm }} BPM · {{ formatDuration(item.durationMs) }}
			</div>
			<div class="ops">
				<button class="mini" type="button" @click.stop="emit('remove', item.id)">删除</button>
			</div>
		</div>

		<div v-if="pages > 1" class="pager">
			<button class="mini" type="button" :disabled="page <= 1" @click="emit('page', page - 1)">上一页</button>
			<span class="page">{{ page }} / {{ pages }}</span>
			<button class="mini" type="button" :disabled="page >= pages" @click="emit('page', page + 1)">下一页</button>
		</div>
		<div v-if="total" class="total">共 {{ total }} 首</div>
	</div>
</template>

<style scoped>
.list {
	display: flex;
	flex-direction: column;
	gap: 6px;
}

.row {
	position: relative;
	padding: 9px 10px;
	border-radius: 8px;
	border: 1px solid var(--ext-line);
	background: var(--ext-bg-soft);
	cursor: pointer;
	transition: border-color 0.18s, background 0.18s;
}

.row:hover {
	border-color: var(--ext-gold);
}

.row.on {
	border-color: var(--ext-gold);
	background: var(--ext-gold-soft);
}

.top {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
}

.name {
	font-size: 12.5px;
	color: var(--ext-text);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.src {
	flex: none;
	font-family: var(--ext-font-mono);
	font-size: 10px;
	padding: 1px 6px;
	border-radius: 999px;
	background: var(--ext-bg-raised);
	color: var(--ext-text-mute);
}

.meta {
	margin-top: 4px;
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.ops {
	position: absolute;
	right: 8px;
	bottom: 6px;
	opacity: 0;
	transition: opacity 0.18s;
}

.row:hover .ops {
	opacity: 1;
}

.mini {
	border: 1px solid var(--ext-line);
	background: var(--ext-bg-raised);
	color: var(--ext-text-dim);
	border-radius: 6px;
	font-size: 11px;
	padding: 2px 7px;
	cursor: pointer;
}

.mini:hover:not(:disabled) {
	border-color: var(--ext-gold);
	color: var(--ext-gold-light);
}

.mini:disabled {
	opacity: 0.4;
	cursor: default;
}

.pager {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-top: 4px;
}

.page {
	font-family: var(--ext-font-mono);
	font-size: 11px;
	color: var(--ext-text-mute);
}

.total {
	font-size: 11px;
	color: var(--ext-text-mute);
}

.tip {
	font-size: 12px;
	color: var(--ext-text-mute);
}
</style>
