<script setup lang="ts">
import type { MotionListItem } from '@/extensions/motion-lab/types/motion'

interface Props {
	items: MotionListItem[]
	loading?: boolean
	activeId: number | null
	total: number
	page: number
	pages: number
}

withDefaults(defineProps<Props>(), { loading: false })

const emit = defineEmits<{
	select: [id: number]
	edit: [id: number]
	remove: [id: number]
	page: [page: number]
}>()
</script>

<template>
	<div class="list">
		<div v-if="loading" class="state">加载中…</div>
		<div v-else-if="!items.length" class="state">
			没有匹配的动效。<br />
			换个分类或关键词，或者点右上角「新增动效」。
		</div>
		<template v-else>
			<button
				v-for="item in items"
				:key="item.id"
				class="row"
				:class="{ on: item.id === activeId }"
				type="button"
				@click="emit('select', item.id)">
				<div class="top">
					<span class="name">{{ item.name }}</span>
					<span class="tech">{{ item.technology || '—' }}</span>
				</div>
				<div class="meta">
					<span class="tag">{{ item.category }}</span>
					<span v-if="item.hasCode" class="code-flag">有代码</span>
					<span class="time">{{ item.updateTime }}</span>
				</div>
				<div class="acts">
					<span class="act" @click.stop="emit('edit', item.id)">编辑</span>
					<span class="act danger" @click.stop="emit('remove', item.id)">删除</span>
				</div>
			</button>
		</template>
		<div v-if="pages > 1" class="pager">
			<button class="ext-btn" type="button" :disabled="page <= 1" @click="emit('page', page - 1)">上一页</button>
			<span class="pn">{{ page }} / {{ pages }}</span>
			<button class="ext-btn" type="button" :disabled="page >= pages" @click="emit('page', page + 1)">下一页</button>
		</div>
		<div v-if="total" class="count">共 {{ total }} 条</div>
	</div>
</template>

<style scoped>
.list {
	display: flex;
	flex-direction: column;
	gap: 7px;
}

.state {
	font-size: 12.5px;
	line-height: 1.9;
	color: var(--ext-text-mute);
	border: 1px dashed var(--ext-line);
	border-radius: var(--ext-radius);
	padding: 18px 14px;
}

.row {
	position: relative;
	text-align: left;
	padding: 10px 11px;
	border-radius: 9px;
	border: 1px solid var(--ext-line-soft);
	background: var(--ext-bg-soft);
	color: var(--ext-text-dim);
	cursor: pointer;
	transition: border-color 0.18s, background-color 0.18s;
}

.row:hover {
	border-color: rgba(194, 150, 58, 0.5);
}

.row.on {
	border-color: var(--ext-gold);
	background: var(--ext-gold-soft);
}

.top {
	display: flex;
	align-items: baseline;
	gap: 8px;
}

.name {
	font-size: 13px;
	color: var(--ext-text);
	font-weight: 600;
}

.tech {
	margin-left: auto;
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-text-mute);
}

.meta {
	display: flex;
	align-items: center;
	gap: 8px;
	margin-top: 7px;
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-text-mute);
}

.tag {
	color: var(--ext-gold-light);
}

.code-flag {
	color: var(--ext-ok);
}

.time {
	margin-left: auto;
}

.acts {
	display: none;
	gap: 10px;
	margin-top: 8px;
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
}

.row:hover .acts {
	display: flex;
}

.act {
	color: var(--ext-text-dim);
}

.act:hover {
	color: var(--ext-gold-light);
}

.act.danger:hover {
	color: #e0a3a5;
}

.pager {
	display: flex;
	align-items: center;
	gap: 8px;
	margin-top: 10px;
}

.pager .pn {
	font-family: var(--ext-font-mono);
	font-size: 11px;
	color: var(--ext-text-mute);
}

.count {
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
	color: var(--ext-text-mute);
	margin-top: 4px;
}
</style>
