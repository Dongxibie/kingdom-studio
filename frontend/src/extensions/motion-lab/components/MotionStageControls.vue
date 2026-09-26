<script setup lang="ts">
import { computed, watch, ref } from 'vue'
import { FALLBACK_PARAMS } from '@/extensions/motion-lab/types/workbench'
import type { MotionTemplateDetail, TemplateParam } from '@/extensions/motion-lab/types/workbench'

interface Props {
	detail: MotionTemplateDetail | null
	playing: boolean
	speed: number
	scale: number
	/** 参数覆盖值：按模板 key 分组，来自「模型给的组合方案」；只覆盖该模板确实有的参数 */
	overrides?: Record<string, Record<string, number>>
}

const props = defineProps<Props>()
const emit = defineEmits<{
	/** 参数变化：把当前值交出去，由父组件注入预览 */
	change: [values: Record<string, number>]
	/** 重播：父组件重建预览 iframe */
	restart: []
	'update:playing': [value: boolean]
	'update:speed': [value: number]
	'update:scale': [value: number]
}>()

const values = ref<Record<string, number>>({})

/** 参数定义：后端有就用后端的，没有（老数据）用兜底的一项 */
const params = computed<TemplateParam[]>(() => {
	const list = props.detail?.params ?? []
	return list.length ? list : FALLBACK_PARAMS
})

/**
 * 换模板、或模型给了新的参数建议时，重建参数值：
 * 该模板有覆盖值就用覆盖值，没有才回到默认值。
 */
function buildValues() {
	const next: Record<string, number> = {}
	const override = props.detail ? props.overrides?.[props.detail.templateKey] : undefined
	for (const param of params.value) {
		const suggested = override?.[param.key]
		next[param.key] = typeof suggested === 'number' && !Number.isNaN(suggested)
			? suggested
			: param.defaultValue ?? param.min ?? 0
	}
	return next
}

watch(
	[() => props.detail?.templateKey, () => props.overrides],
	() => {
		const next = buildValues()
		values.value = next
		emit('change', { ...next })
	},
	{ immediate: true },
)

function updateParam(key: string, value: number) {
	values.value = { ...values.value, [key]: value }
	emit('change', { ...values.value })
}

/** 展示用：0.9 → 0.9s，28 → 28px */
function display(param: TemplateParam, value: number | undefined) {
	if (value === undefined) {
		return '—'
	}
	return `${value}${param.unit || ''}`
}
</script>

<template>
	<div class="stage-controls">
		<button class="ext-btn" type="button" @click="emit('update:playing', !playing)">
			{{ playing ? '暂停' : '播放' }}
		</button>
		<button class="ext-btn" type="button" @click="emit('restart')">重播</button>

		<div class="speeds">
			<button
				v-for="option in [0.5, 1, 2]"
				:key="option"
				class="chip"
				:class="{ on: speed === option }"
				type="button"
				@click="emit('update:speed', option)">
				{{ option }}×
			</button>
		</div>

		<div class="zoom">
			<span class="k">缩放</span>
			<!-- 顺序要紧：先把 type / min / max / step 定好，再给 :value，
			     否则浏览器会拿默认步长（1）把初始值取整 -->
			<input
				type="range"
				min="0.5"
				max="1.4"
				step="0.05"
				:value="scale"
				@input="emit('update:scale', Number(($event.target as HTMLInputElement).value))" />
			<span class="v">{{ scale.toFixed(2) }}×</span>
		</div>
	</div>

	<div v-if="params.length" class="param-list">
		<div v-for="param in params" :key="param.key" class="param-row">
			<span class="name" :title="param.key">{{ param.label }}</span>
			<input
				type="range"
				:min="param.min ?? 0"
				:max="param.max ?? 1"
				:step="param.step ?? 0.01"
				:value="values[param.key]"
				@input="updateParam(param.key, Number(($event.target as HTMLInputElement).value))" />
			<span class="value">{{ display(param, values[param.key]) }}</span>
		</div>
		<div class="hint">参数就是 CSS 变量：改完立刻反映在预览里，导出的代码里也是同一批变量。</div>
	</div>
	<div v-else class="hint">这个模板没有可调参数。</div>
</template>

<style scoped>
.stage-controls {
	display: flex;
	align-items: center;
	gap: 8px;
	flex-wrap: wrap;
	margin-bottom: 12px;
}

.speeds {
	display: flex;
	gap: 4px;
	margin-left: 4px;
}

.chip {
	border: 1px solid var(--ext-line);
	background: rgba(255, 255, 255, 0.04);
	color: var(--ext-text-mute);
	border-radius: 999px;
	font-size: 11px;
	padding: 3px 9px;
	cursor: pointer;
}

.chip.on {
	border-color: var(--ext-gold);
	color: var(--ext-gold-light);
	background: var(--ext-gold-soft);
}

.zoom {
	display: flex;
	align-items: center;
	gap: 6px;
	margin-left: auto;
	font-size: 11.5px;
	color: var(--ext-text-mute);
}

.zoom input {
	width: 96px;
}

.zoom .v {
	font-family: var(--ext-font-mono);
}

.param-list {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.param-row {
	display: grid;
	grid-template-columns: 64px 1fr 62px;
	align-items: center;
	gap: 8px;
	font-size: 12px;
	color: var(--ext-text-dim);
}

.param-row .name {
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.param-row .value {
	font-family: var(--ext-font-mono);
	font-size: 11px;
	text-align: right;
	color: var(--ext-gold-light);
}

.hint {
	font-size: 11.5px;
	line-height: 1.7;
	color: var(--ext-text-mute);
	margin-top: 6px;
}
</style>
