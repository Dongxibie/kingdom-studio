<script setup lang="ts">
import type { MotionTemplateItem } from '@/extensions/motion-lab/types/workbench'
import { RUNTIME_TIER_META } from '@/extensions/motion-lab/types/workbench'

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
			<div class="sub">
			<span v-if="item.community" class="origin community" title="社区精选：实现为本项目的原创实现，来源已标注">社区</span>
			<span v-else class="origin" title="官方模板">官方</span>
			{{ item.sceneLabel }} · {{ item.technology }}
		</div>
			<div class="metrics">
				<span class="metric" title="视觉效果评分">视觉 {{ item.scoreVisual }}</span>
				<span class="metric" title="性能表现评分：越高越省">性能 {{ item.scorePerf }}</span>
				<span class="metric" title="实现难度">难度 {{ item.difficultyLabel }}</span>
			</div>
			<div class="tier" :class="'tier-' + (RUNTIME_TIER_META[item.runtimeTier]?.tone ?? 'balanced')" :title="item.runtimeNote">
				{{ RUNTIME_TIER_META[item.runtimeTier]?.label ?? item.runtimeTierLabel }}
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

.origin {
	display: inline-block;
	margin-right: 5px;
	padding: 0 5px;
	border-radius: 4px;
	font-size: 9.5px;
	border: 1px solid var(--ext-line);
	color: var(--ext-text-mute);
}

.origin.community {
	border-color: rgba(240, 205, 114, 0.5);
	color: var(--ext-gold-light);
}

.metrics {
	display: flex;
	flex-wrap: wrap;
	gap: 8px;
	margin-top: 7px;
}

.metric {
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-text-mute);
}

.tier {
	display: inline-block;
	margin-top: 7px;
	font-size: 10px;
	padding: 1px 7px;
	border-radius: 999px;
	border: 1px solid currentColor;
}

.tier-light { color: #7ee0a2; }
.tier-balanced { color: var(--ext-neon-cyan); }
.tier-gpu { color: var(--ext-neon-violet); }

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
