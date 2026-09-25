<script setup lang="ts">
import { computed } from 'vue'
import { chunkKeys } from '@/extensions/music-agent/utils/note-format'

interface Props {
	keyLayout: string[]
	keyNoteNames: string[]
	/** 当前按下的键 */
	activeKeys: string[]
	/** 还没配置任何档案时给出提示 */
	empty?: boolean
}

const props = withDefaults(defineProps<Props>(), { empty: false })

/** 两排折行：键多时一行排不下会挤成一团 */
const rows = computed(() => chunkKeys(props.keyLayout, 8))

const activeSet = computed(() => new Set(props.activeKeys))

function noteOf(key: string): string {
	const index = props.keyLayout.indexOf(key)
	return index >= 0 ? props.keyNoteNames[index] ?? '' : ''
}
</script>

<template>
	<div class="keyboard">
		<div v-if="empty || !keyLayout.length" class="kb-hint">选择一个乐器档案后，这里会显示它的虚拟键盘</div>
		<div v-for="(row, rowIndex) in rows" :key="rowIndex" class="kb-row">
			<button
				v-for="key in row"
				:key="key + rowIndex"
				type="button"
				class="kb-key"
				:class="{ on: activeSet.has(key) }"
				disabled>
				<span class="kb-cap">{{ key }}</span>
				<span class="kb-note">{{ noteOf(key) }}</span>
			</button>
		</div>
		<div v-if="keyLayout.length" class="kb-foot">
			共 {{ keyLayout.length }} 个键 · 高亮的是当前按下的键（Demo 回放时同步点亮）
		</div>
	</div>
</template>

<style scoped>
.keyboard {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.kb-row {
	display: flex;
	gap: 6px;
	flex-wrap: wrap;
}

.kb-key {
	flex: 1 1 54px;
	min-width: 48px;
	padding: 8px 4px 6px;
	border-radius: 8px;
	border: 1px solid var(--ext-line);
	background: linear-gradient(180deg, var(--ext-bg-raised), var(--ext-bg-soft));
	color: var(--ext-text-dim);
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 2px;
	cursor: default;
	transition: transform 90ms ease, box-shadow 140ms ease, background 140ms ease, color 140ms ease;
}

.kb-cap {
	font-family: var(--ext-font-mono);
	font-size: 14px;
	font-weight: 700;
	color: var(--ext-text);
}

.kb-note {
	font-size: 10px;
	letter-spacing: 0.02em;
}

.kb-key.on {
	background: linear-gradient(150deg, var(--ext-gold-light), var(--ext-gold));
	border-color: transparent;
	color: #14161a;
	transform: translateY(2px);
	box-shadow: 0 0 14px rgba(194, 150, 58, 0.55);
}

.kb-key.on .kb-cap,
.kb-key.on .kb-note {
	color: #14161a;
}

.kb-hint,
.kb-foot {
	font-size: 12px;
	color: var(--ext-text-mute);
}
</style>
