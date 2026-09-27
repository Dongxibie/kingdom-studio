<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { analyzePrompt, applyStrategy } from '@/extensions/music-agent/api/assistant'
import { formatDuration } from '@/extensions/music-agent/utils/note-format'
import { PROMPT_PRESETS, type MusicAssistantAnswer } from '@/extensions/music-agent/types/assistant'

/**
 * AI 助手面板（AI Assistant）。
 *
 * 助手不生成音乐，只做四件事：判断难度、推荐键位、给优化建议、解释每一处改动。
 * 四个区块按「结论在前、依据在后」排：先给难度与键位，再给建议与预览数字，
 * 最后是逐条改动明细 —— 使用者先看到能用的结论，想深究再看细节。
 */
interface Props {
	taskId: number | null
	/** 当前使用的乐器档案名：助手推荐的键位与它一起看 */
	currentProfile?: string
	/** 当前按键序列的统计，用于「推荐键位」区块做对比 */
	mappedCount?: number
	unmappedCount?: number
}

const props = withDefaults(defineProps<Props>(), {
	currentProfile: '',
	mappedCount: 0,
	unmappedCount: 0,
})

const emit = defineEmits<{
	/** 应用了某个策略并生成了新曲目 */
	applied: [taskId: number]
}>()

const prompt = ref('')
const thinking = ref(false)
const applying = ref(false)
const answer = ref<MusicAssistantAnswer | null>(null)
const history = ref<{ role: 'me' | 'ai'; text: string }[]>([])
const error = ref('')

const DIFFICULTY_HINT: Record<string, string> = {
	BEGINNER: '入门：简化节奏、收窄跨度，适合快速上手',
	NORMAL: '标准：保持原曲，不改动音符',
	SHOWCASE: '展示：保留特色、加强重音，适合演示',
}

const difficulty = computed(() => answer.value?.intent.difficulty ?? '')
const canApply = computed(() => Boolean(answer.value) && difficulty.value !== 'NORMAL')

watch(
	() => props.taskId,
	() => {
		answer.value = null
		history.value = []
		prompt.value = ''
		error.value = ''
	},
)

async function ask(text: string) {
	const value = text.trim()
	if (!props.taskId) {
		error.value = '先在左侧选一首曲子'
		return
	}
	if (!value) {
		return
	}
	history.value.push({ role: 'me', text: value })
	thinking.value = true
	error.value = ''
	try {
		const result = await analyzePrompt(props.taskId, value)
		answer.value = result
		history.value.push({ role: 'ai', text: result.explanation })
		prompt.value = ''
	} catch (e) {
		error.value = e instanceof Error ? e.message : '分析失败，请稍后再试'
	} finally {
		thinking.value = false
	}
}

async function apply() {
	const current = answer.value
	if (!props.taskId || !current) {
		return
	}
	applying.value = true
	try {
		const result = await applyStrategy(props.taskId, current.intent.difficulty)
		answer.value = result
		history.value.push({ role: 'ai', text: '已按「' + current.intent.difficultyLabel + '」生成衍生曲目，可以去曲库打开它。' })
		ElMessage.success('已生成衍生曲目')
		if (result.taskId) {
			emit('applied', result.taskId)
		}
	} catch (e) {
		ElMessage.error(e instanceof Error ? e.message : '应用失败')
	} finally {
		applying.value = false
	}
}
</script>

<template>
	<div class="ai">
		<div class="ai-head">
			<span class="ai-badge" :class="answer?.source === 'MODEL' ? 'ai-badge--model' : 'ai-badge--rule'">
				{{ answer ? (answer.source === 'MODEL' ? '模型分析' : '规则分析') : '待分析' }}
			</span>
			<button class="st-btn" type="button" :disabled="thinking || !taskId" @click="ask('分析这首曲子的难度与演奏建议')">
				{{ thinking ? '分析中…' : '分析这首曲目' }}
			</button>
		</div>

		<p v-if="!taskId" class="ai-hint">先在左侧选一首曲子，助手才能读谱。</p>

		<template v-if="answer">
			<div class="ai-grid">
				<div class="ai-block">
					<div class="st-label">难度判断</div>
					<div class="ai-value">{{ answer.intent.difficultyLabel }}</div>
					<div class="ai-sub">{{ DIFFICULTY_HINT[difficulty] }}</div>
				</div>
				<div class="ai-block">
					<div class="st-label">推荐键位</div>
					<div class="ai-value">{{ answer.intent.instrument || currentProfile || '沿用当前档案' }}</div>
					<div class="ai-sub">
						当前 {{ currentProfile || '未选档案' }} · 落键 {{ mappedCount }} · 未落 {{ unmappedCount }}
					</div>
				</div>
			</div>

			<div v-if="answer.intent.suggestions.length" class="ai-section">
				<div class="st-label">优化建议</div>
				<ul class="ai-list">
					<li v-for="(item, index) in answer.intent.suggestions" :key="index">{{ item }}</li>
				</ul>
			</div>

			<div class="ai-preview">
				<div class="ai-preview-cell">
					<span class="ai-preview-label">音符数</span>
					<span class="ai-preview-value">
						{{ answer.preview.noteCountBefore }}
						<i>→</i>
						<b>{{ answer.preview.noteCountAfter }}</b>
					</span>
				</div>
				<div class="ai-preview-cell">
					<span class="ai-preview-label">速度</span>
					<span class="ai-preview-value">
						{{ answer.preview.tempoBefore }}
						<i>→</i>
						<b>{{ answer.preview.tempoAfter }}</b>
					</span>
				</div>
				<div class="ai-preview-cell">
					<span class="ai-preview-label">时长</span>
					<span class="ai-preview-value">
						{{ formatDuration(answer.preview.durationBefore) }}
						<i>→</i>
						<b>{{ formatDuration(answer.preview.durationAfter) }}</b>
					</span>
				</div>
			</div>

			<div v-if="answer.adjustments.length" class="ai-section">
				<div class="st-label">逐条改动与原因</div>
				<div class="ai-change" v-for="(change, index) in answer.adjustments" :key="index">
					<div class="ai-change-head">
						<span class="ai-change-title">{{ change.title }}</span>
						<span class="ai-change-num">{{ change.before }} → {{ change.after }}</span>
					</div>
					<div class="ai-change-detail">{{ change.detail }}</div>
				</div>
			</div>

			<p v-if="answer.fallbackReason" class="ai-hint">{{ answer.fallbackReason }}</p>

			<div class="ai-actions">
				<button class="st-btn st-btn--primary" type="button" :disabled="!canApply || applying" @click="apply">
					{{ applying ? '生成中…' : '应用方案（生成衍生曲目）' }}
				</button>
				<span v-if="!canApply" class="ai-hint">标准模式就是原曲本身，不需要生成副本。</span>
			</div>
		</template>

		<div class="ai-chat">
			<div v-for="(item, index) in history" :key="index" class="ai-bubble" :class="'ai-bubble--' + item.role">
				{{ item.text }}
			</div>
			<div v-if="thinking" class="ai-bubble ai-bubble--ai ai-typing">
				<i /><i /><i />
			</div>
		</div>

		<div class="ai-presets">
			<button v-for="preset in PROMPT_PRESETS" :key="preset" class="st-chip" type="button" @click="ask(preset)">
				{{ preset }}
			</button>
		</div>

		<div class="ai-input">
			<input
				v-model="prompt"
				type="text"
				placeholder="说说你想要的演奏效果，例如「让它听起来简单一点」"
				@keydown.enter="ask(prompt)" />
			<button class="st-btn" type="button" :disabled="thinking || !prompt.trim()" @click="ask(prompt)">发送</button>
		</div>
		<p v-if="error" class="ai-error">{{ error }}</p>
		<p class="ai-hint">
			助手读取的是已解析的音符与当前映射结果，返回的方案由后端重新计算 —— 不会直接采用模型生成的音符。
		</p>
	</div>
</template>

<style scoped>
.ai {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.ai-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
}

.ai-badge {
	border: 1px solid var(--st-edge);
	border-radius: 999px;
	font-size: 10.5px;
	padding: 3px 10px;
	color: var(--ext-text-mute);
}

.ai-badge--model {
	border-color: rgba(126, 214, 165, 0.45);
	color: #7ed6a5;
}

.ai-badge--rule {
	border-color: rgba(240, 205, 114, 0.42);
	color: var(--ext-gold-light);
}

.ai-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 8px;
}

.ai-block {
	border: 1px solid var(--st-edge);
	border-radius: 10px;
	background: var(--st-glass);
	padding: 9px 11px;
}

.ai-value {
	margin-top: 4px;
	font-size: 14px;
	color: var(--ext-text);
}

.ai-sub {
	margin-top: 3px;
	font-size: 10.5px;
	color: var(--ext-text-mute);
	line-height: 1.6;
}

.ai-section {
	display: flex;
	flex-direction: column;
	gap: 6px;
}

.ai-list {
	margin: 0;
	padding-left: 16px;
	display: flex;
	flex-direction: column;
	gap: 4px;
	font-size: 11.5px;
	color: var(--ext-text-dim);
	line-height: 1.7;
}

.ai-preview {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 8px;
}

.ai-preview-cell {
	border: 1px solid var(--st-edge);
	border-radius: 10px;
	background: var(--st-glass);
	padding: 8px 9px;
	display: flex;
	flex-direction: column;
	gap: 3px;
}

.ai-preview-label {
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.ai-preview-value {
	font-family: var(--ext-font-mono);
	font-size: 12px;
	color: var(--ext-text-dim);
}

.ai-preview-value i {
	font-style: normal;
	margin: 0 4px;
	opacity: 0.6;
}

.ai-preview-value b {
	color: var(--ext-gold-light);
	font-weight: 500;
}

.ai-change {
	border-left: 2px solid rgba(167, 139, 250, 0.5);
	padding-left: 9px;
	margin-top: 6px;
}

.ai-change-head {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
	gap: 8px;
}

.ai-change-title {
	font-size: 11.5px;
	color: var(--ext-text);
}

.ai-change-num {
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-gold-light);
}

.ai-change-detail {
	margin-top: 2px;
	font-size: 10.5px;
	color: var(--ext-text-mute);
	line-height: 1.65;
}

.ai-actions {
	display: flex;
	align-items: center;
	gap: 8px;
	flex-wrap: wrap;
}

.ai-chat {
	display: flex;
	flex-direction: column;
	gap: 7px;
	max-height: 220px;
	overflow: auto;
}

.ai-bubble {
	max-width: 92%;
	border-radius: 12px;
	padding: 7px 11px;
	font-size: 11.5px;
	line-height: 1.7;
	animation: st-rise 0.28s ease both;
}

.ai-bubble--me {
	align-self: flex-end;
	background: linear-gradient(180deg, rgba(240, 205, 114, 0.2), rgba(240, 205, 114, 0.08));
	border: 1px solid var(--st-edge-hot);
	color: var(--ext-gold-light);
}

.ai-bubble--ai {
	align-self: flex-start;
	background: var(--st-glass);
	border: 1px solid var(--st-edge);
	color: var(--ext-text-dim);
}

.ai-typing {
	display: inline-flex;
	gap: 4px;
	align-items: center;
}

.ai-typing i {
	width: 5px;
	height: 5px;
	border-radius: 50%;
	background: var(--ext-text-mute);
	animation: st-pulse 1.2s ease-in-out infinite;
}

.ai-presets {
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
}

.ai-input {
	display: flex;
	gap: 7px;
}

.ai-input input {
	flex: 1;
	border: 1px solid var(--st-edge);
	border-radius: 10px;
	background: rgba(255, 255, 255, 0.04);
	color: var(--ext-text);
	font-size: 12px;
	padding: 8px 11px;
	font-family: inherit;
}

.ai-input input:focus {
	outline: none;
	border-color: var(--st-edge-hot);
}

.ai-hint {
	margin: 0;
	font-size: 10.5px;
	color: var(--ext-text-mute);
	line-height: 1.7;
}

.ai-error {
	margin: 0;
	font-size: 11px;
	color: #ff9a9a;
}
</style>
