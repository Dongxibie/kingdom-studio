<script setup lang="ts">
import type { MotionTemplateItem } from '@/extensions/motion-lab/types/workbench'

interface Props {
	items: MotionTemplateItem[]
	activeKey: string
	loading: boolean
	total: number
	page: number
	pages: number
}

defineProps<Props>()
const emit = defineEmits<{
	select: [templateKey: string]
	page: [page: number]
}>()

/** 星级画成 ★★★★☆，半星用「半」字符更省空间 */
function stars(value: number) {
	const full = Math.floor(value)
	const half = value - full >= 0.5
	return '★'.repeat(full) + (half ? '⯪' : '') + '☆'.repeat(Math.max(0, 5 - full - (half ? 1 : 0)))
}
</script>

<template>
	<div class="cards">
		<div v-if="loading" class="tip">读取中…</div>
		<div v-else-if="!items.length" class="tip">没有符合条件的模板，换个筛选条件试试。</div>

		<button
			v-for="item in items"
			v-else
			:key="item.templateKey"
			class="card"
			:class="{ on: item.templateKey === activeKey }"
			type="button"
			@click="emit('select', item.templateKey)">
			<div class="top">
				<span class="name">{{ item.name }}</span>
				<span class="grade" :class="'g-' + item.grade">{{ item.grade }}</span>
			</div>
			<div class="meta">
				<span class="stars" :title="`推荐指数 ${item.score}`">{{ stars(item.stars) }}</span>
				<span class="score">{{ item.score }}</span>
			</div>
			<div class="sub">{{ item.sceneLabel }} · {{ item.technology }} · {{ item.difficultyLabel }}</div>
			<div class="why">
				<span v-for="tag in item.bestFor.slice(0, 2)" :key="tag" class="fit">{{ tag }}</span>
			</div>
		</button>

		<div v-if="pages > 1" class="pager">
			<button class="mini" type="button" :disabled="page <= 1" @click="emit('page', page - 1)">上一页</button>
			<span class="page">{{ page }} / {{ pages }}</span>
			<button class="mini" type="button" :disabled="page >= pages" @click="emit('page', page + 1)">下一页</button>
		</div>
		<div v-if="total" class="total">共 {{ total }} 个模板</div>
	</div>
</template>

<style scoped>
.cards {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(178px, 1fr));
	gap: 10px;
}

.card {
	text-align: left;
	padding: 12px 13px;
	border-radius: 14px;
	border: 1px solid var(--ext-line);
	background: linear-gradient(155deg, rgba(28, 31, 40, 0.72), rgba(12, 14, 19, 0.6));
	cursor: pointer;
	transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.card:hover {
	transform: translateY(-3px);
	border-color: rgba(240, 205, 114, 0.45);
	box-shadow: 0 18px 36px rgba(0, 0, 0, 0.45);
}

.card.on {
	border-color: var(--ext-gold);
	box-shadow: 0 0 0 1px rgba(240, 205, 114, 0.4), 0 18px 40px rgba(240, 205, 114, 0.12);
}

.top {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
}

.card .name {
	font-size: 13px;
	color: var(--ext-text);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.grade {
	flex: none;
	font-family: var(--ext-font-mono);
	font-size: 10px;
	padding: 1px 6px;
	border-radius: 6px;
	border: 1px solid currentColor;
}

.g-S { color: var(--ext-gold); }
.g-A { color: var(--ext-neon-cyan); }
.g-B { color: var(--ext-neon-violet); }
.g-C { color: var(--ext-text-mute); }

.meta {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
	margin-top: 7px;
}

.stars {
	color: var(--ext-gold);
	font-size: 11px;
	letter-spacing: 0.04em;
}

.score {
	font-family: var(--ext-font-mono);
	font-size: 12px;
	color: var(--ext-gold-light);
}

.sub {
	margin-top: 5px;
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.why {
	display: flex;
	flex-wrap: wrap;
	gap: 4px;
	margin-top: 7px;
}

.fit {
	font-size: 10px;
	padding: 1px 6px;
	border-radius: 999px;
	background: rgba(94, 234, 212, 0.1);
	color: var(--ext-neon-cyan);
}

.pager {
	grid-column: 1 / -1;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 10px;
	margin-top: 2px;
}

.mini {
	border: 1px solid var(--ext-line);
	background: rgba(255, 255, 255, 0.04);
	color: var(--ext-text-dim);
	border-radius: 8px;
	font-size: 11.5px;
	padding: 3px 10px;
	cursor: pointer;
}

.mini:disabled {
	opacity: 0.4;
	cursor: default;
}

.page {
	font-family: var(--ext-font-mono);
	font-size: 11px;
	color: var(--ext-text-mute);
}

.total {
	grid-column: 1 / -1;
	font-size: 11px;
	color: var(--ext-text-mute);
}

.tip {
	grid-column: 1 / -1;
	font-size: 12px;
	color: var(--ext-text-mute);
	padding: 8px 0;
}
</style>
