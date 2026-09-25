<script setup lang="ts">
import { computed } from 'vue'
import { ExtCodeBlock } from '@/extensions/_shared/components'
import type { KeySequence } from '@/extensions/music-agent/types/music'
import { STRATEGY_LABELS } from '@/extensions/music-agent/types/music'
import { formatMs } from '@/extensions/music-agent/utils/note-format'

interface Props {
	sequence: KeySequence | null
}

const props = defineProps<Props>()

const stats = computed(() => {
	const sequence = props.sequence
	if (!sequence) {
		return []
	}
	return [
		{ label: '音符', value: sequence.noteCount },
		{ label: '落键', value: sequence.mappedCount },
		{ label: '按次数', value: sequence.strokes.length },
		{ label: '已调整', value: sequence.adjustedCount },
		{ label: '未落键', value: sequence.unmappedCount },
	]
})

/** 未落键的音名分布：给出「大概缺哪些音」的判断依据 */
const unmappedSummary = computed(() => {
	const sequence = props.sequence
	if (!sequence || !sequence.unmapped.length) {
		return ''
	}
	const names = [...new Set(sequence.unmapped.map((item) => item.noteName))]
	const shown = names.slice(0, 12).join('、')
	return names.length > 12 ? shown + ' 等 ' + names.length + ' 个音' : shown
})
</script>

<template>
	<div v-if="!sequence" class="seq-hint">选择乐器档案后自动生成按键序列</div>
	<div v-else class="seq">
		<div class="seq-stats">
			<div v-for="item in stats" :key="item.label" class="seq-stat">
				<div class="v">{{ item.value }}</div>
				<div class="k">{{ item.label }}</div>
			</div>
		</div>

		<div class="ext-kv">
			<span class="k">档案</span>
			<span class="v">{{ sequence.profileName }} · {{ sequence.keyCount }} 键 · {{ STRATEGY_LABELS[sequence.strategy] }}</span>
		</div>

		<div v-if="sequence.unmappedCount" class="seq-warn">
			<div class="ext-kv">
				<span class="k">未落键</span>
				<span class="v">{{ unmappedSummary }}</span>
			</div>
			<ul class="seq-list">
				<li v-for="item in sequence.unmapped.slice(0, 8)" :key="item.startMs + '-' + item.noteName">
					{{ formatMs(item.startMs) }} · {{ item.noteName }} —— {{ item.reason }}
				</li>
			</ul>
			<div class="ext-hint">可以换一个音域更宽的档案，或把超范围策略改成「移八度」再试。</div>
		</div>

		<ExtCodeBlock :code="sequence.exportText" title="按键序列（可直接复制去练）" />
	</div>
</template>

<style scoped>
.seq {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.seq-stats {
	display: flex;
	gap: 8px;
	flex-wrap: wrap;
}

.seq-stat {
	/* 五个统计要挤在一行里：不设最小宽度的话最后一个会单独换行 */
	flex: 1 1 0;
	min-width: 0;
	padding: 8px 2px;
	text-align: center;
	border-radius: 8px;
	background: var(--ext-bg-raised);
	border: 1px solid var(--ext-line);
}

.seq-stat .v {
	font-family: var(--ext-font-mono);
	font-size: 18px;
	color: var(--ext-gold-light);
}

.seq-stat .k {
	font-size: 11px;
	color: var(--ext-text-mute);
	margin-top: 2px;
}

.seq-warn {
	border: 1px solid var(--ext-red);
	background: var(--ext-red-soft);
	border-radius: var(--ext-radius);
	padding: 10px;
}

.seq-list {
	margin: 8px 0 0;
	padding-left: 18px;
	font-size: 11.5px;
	line-height: 1.8;
	color: var(--ext-text-dim);
}

.seq-hint {
	font-size: 12.5px;
	color: var(--ext-text-mute);
}
</style>
