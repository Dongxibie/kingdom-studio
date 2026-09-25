<script setup lang="ts">
import { MOTION_CATEGORIES } from '@/extensions/motion-lab/types/motion'
import type { MotionCategory } from '@/extensions/motion-lab/types/motion'

interface Props {
	/** 各分类的条数；Phase 2 之前没有数据，传 undefined 时显示「—」而不是 0 */
	counts?: Partial<Record<MotionCategory, number>>
}

const props = withDefaults(defineProps<Props>(), { counts: undefined })

// 分类选择用 v-model，父组件只管值，不关心怎么渲染
const selected = defineModel<MotionCategory | 'ALL'>({ default: 'ALL' })

function countOf(key: MotionCategory): string {
	const value = props.counts?.[key]
	return typeof value === 'number' ? String(value) : '—'
}
</script>

<template>
	<div class="tree">
		<button class="row all" :class="{ on: selected === 'ALL' }" type="button" @click="selected = 'ALL'">
			<span class="name">全部动效</span>
			<span class="cnt">—</span>
		</button>
		<button
			v-for="item in MOTION_CATEGORIES"
			:key="item.key"
			class="row"
			:class="{ on: selected === item.key }"
			type="button"
			:title="item.hint"
			@click="selected = item.key">
			<span class="name">{{ item.label }}</span>
			<span class="mono">{{ item.key }}</span>
			<span class="cnt">{{ countOf(item.key) }}</span>
		</button>
	</div>
</template>

<style scoped>
.tree {
	display: flex;
	flex-direction: column;
	gap: 4px;
}

.row {
	display: flex;
	align-items: center;
	gap: 8px;
	width: 100%;
	text-align: left;
	padding: 8px 10px;
	border-radius: 8px;
	border: 1px solid transparent;
	background: transparent;
	color: var(--ext-text-dim);
	cursor: pointer;
	font-size: 13px;
	transition: color 0.18s, border-color 0.18s, background-color 0.18s;
}

.row:hover {
	color: var(--ext-text);
	background: var(--ext-bg-raised);
}

.row.on {
	color: var(--ext-gold-light);
	border-color: var(--ext-gold);
	background: var(--ext-gold-soft);
}

.row.all {
	font-weight: 600;
	color: var(--ext-text);
}

.mono {
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-text-mute);
}

.cnt {
	margin-left: auto;
	font-family: var(--ext-font-mono);
	font-size: 11px;
	color: var(--ext-text-mute);
}
</style>
