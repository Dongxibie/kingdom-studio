<script setup lang="ts">
import { ref } from 'vue'
import { ONBOARDING_SCENES, type MotionSearchResult } from '@/extensions/motion-lab/types/workbench'

interface Props {
	loading: boolean
	result: MotionSearchResult | null
}

defineProps<Props>()
const emit = defineEmits<{
	search: [query: string]
	pick: [kind: 'TEMPLATE' | 'RECIPE', key: string]
	scene: [scene: string]
}>()

const text = ref('')

/** 新手引导里的五个场景，同时也是搜索栏下方的快捷入口 */
const scenes = ONBOARDING_SCENES

const PLACEHOLDER = '描述你想要的动效或页面，例如「苹果官网风格的 Hero 动画」'

function submit() {
	const value = text.value.trim()
	if (!value) {
		return
	}
	emit('search', value)
}
</script>

<template>
	<div class="search">
		<div class="bar">
			<span class="icon" aria-hidden="true">✦</span>
			<input
				v-model="text"
				type="text"
				:placeholder="PLACEHOLDER"
				spellcheck="false"
				@keyup.enter="submit" />
			<button class="ext-btn primary" type="button" :disabled="loading || !text.trim()" @click="submit">
				{{ loading ? '分析中…' : '找动效' }}
			</button>
		</div>

		<div class="quick">
			<span class="label">快速开始</span>
			<button
				v-for="item in scenes"
				:key="item.value"
				class="chip"
				type="button"
				:title="item.hint"
				@click="emit('scene', item.value)">
				{{ item.label }}
			</button>
		</div>

		<div v-if="result" class="answer">
			<div class="intent">
				<span class="badge">识别到</span>
				<span class="note">{{ result.intent.note }}</span>
			</div>

			<div v-if="result.recipes.length" class="group">
				<div class="group-title">推荐组合方案（把几个动效按顺序叠好）</div>
				<button
					v-for="match in result.recipes"
					:key="match.key"
					class="row"
					type="button"
					@click="emit('pick', 'RECIPE', match.key)">
					<span class="name">{{ match.name }}</span>
					<span class="score">{{ match.grade }} · {{ match.score }}</span>
					<span class="reasons">{{ match.reasons.join(' / ') }}</span>
				</button>
			</div>

			<div v-if="result.matches.length" class="group">
				<div class="group-title">推荐模板</div>
				<button
					v-for="match in result.matches"
					:key="match.key"
					class="row"
					type="button"
					@click="emit('pick', 'TEMPLATE', match.key)">
					<span class="name">{{ match.name }}</span>
					<span class="score">{{ match.grade }} · {{ match.score }}</span>
					<span class="reasons">{{ match.reasons.join(' / ') }}</span>
				</button>
			</div>

			<div v-if="!result.matches.length && !result.recipes.length" class="empty">
				没有匹配到动效，换一种说法试试（例如「滚动」→「滚动逐级点亮」），或直接用左侧的发现方式筛选。
			</div>

			<div class="advice">{{ result.advice }}</div>
		</div>
	</div>
</template>

<style scoped>
.search {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.bar {
	display: flex;
	align-items: center;
	gap: 10px;
	padding: 10px 12px;
	border-radius: 999px;
	background: linear-gradient(140deg, rgba(30, 34, 44, 0.86), rgba(14, 16, 22, 0.8));
	border: 1px solid var(--ext-line);
	backdrop-filter: blur(18px);
	-webkit-backdrop-filter: blur(18px);
	box-shadow: 0 20px 46px rgba(0, 0, 0, 0.45);
}

.icon {
	color: var(--ext-gold);
	font-size: 15px;
}

.bar input {
	flex: 1;
	background: transparent;
	border: 0;
	outline: none;
	color: var(--ext-text);
	font-size: 13.5px;
	padding: 6px 2px;
}

.bar input::placeholder {
	color: var(--ext-text-mute);
}

.quick {
	display: flex;
	align-items: center;
	gap: 6px;
	flex-wrap: wrap;
	padding-left: 4px;
}

.quick .label {
	font-size: 11.5px;
	color: var(--ext-text-mute);
	margin-right: 2px;
}

.chip {
	border: 1px solid var(--ext-line);
	background: rgba(255, 255, 255, 0.035);
	color: var(--ext-text-dim);
	border-radius: 999px;
	font-size: 11.5px;
	padding: 4px 12px;
	cursor: pointer;
	transition: border-color 0.18s ease, color 0.18s ease, background 0.18s ease;
}

.chip:hover {
	border-color: rgba(240, 205, 114, 0.5);
	color: var(--ext-gold-light);
	background: rgba(240, 205, 114, 0.08);
}

.answer {
	border-radius: var(--ext-radius);
	border: 1px solid var(--ext-line);
	background: linear-gradient(165deg, rgba(24, 27, 36, 0.72), rgba(12, 14, 19, 0.62));
	backdrop-filter: blur(18px);
	-webkit-backdrop-filter: blur(18px);
	padding: 12px 14px;
}

.intent {
	display: flex;
	align-items: baseline;
	gap: 8px;
	flex-wrap: wrap;
}

.badge {
	font-size: 10.5px;
	padding: 2px 8px;
	border-radius: 999px;
	background: rgba(94, 234, 212, 0.14);
	color: var(--ext-neon-cyan);
}

.note {
	font-size: 12px;
	color: var(--ext-text-dim);
}

.group {
	margin-top: 10px;
}

.group-title {
	font-size: 11.5px;
	color: var(--ext-text-mute);
	margin-bottom: 6px;
}

.row {
	display: grid;
	grid-template-columns: minmax(84px, 140px) 62px 1fr;
	align-items: center;
	gap: 10px;
	width: 100%;
	text-align: left;
	padding: 7px 10px;
	border: 1px solid transparent;
	border-radius: 10px;
	background: rgba(255, 255, 255, 0.03);
	color: var(--ext-text-dim);
	cursor: pointer;
	margin-bottom: 4px;
	transition: border-color 0.18s ease, background 0.18s ease;
}

.row:hover {
	border-color: rgba(240, 205, 114, 0.45);
	background: rgba(240, 205, 114, 0.07);
}

.row .name {
	font-size: 12.5px;
	color: var(--ext-text);
}

.row .score {
	font-family: var(--ext-font-mono);
	font-size: 11px;
	color: var(--ext-gold-light);
}

.row .reasons {
	font-size: 11px;
	color: var(--ext-text-mute);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.empty {
	margin-top: 10px;
	font-size: 12px;
	color: var(--ext-text-mute);
	line-height: 1.8;
}

.advice {
	margin-top: 10px;
	padding-top: 10px;
	border-top: 1px solid var(--ext-line-soft);
	font-size: 12px;
	line-height: 1.8;
	color: var(--ext-text-dim);
}
</style>
