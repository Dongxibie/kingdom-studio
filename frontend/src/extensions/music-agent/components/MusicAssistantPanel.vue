<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { analyzePrompt, applyStrategy } from '@/extensions/music-agent/api/assistant'
import { PROMPT_PRESETS } from '@/extensions/music-agent/types/assistant'
import type { MusicAssistantAnswer } from '@/extensions/music-agent/types/assistant'

/**
 * AI 音乐助手面板：聊天式交互。
 *
 * 「AI 不生成音乐」这件事在界面上也看得出来：回答里只有三样东西 ——
 * 它理解到的意图、可执行的建议、以及每条改动的**原因**；
 * 真正改曲子的是后端的策略规则，点「应用方案」才会生成一份新版本曲目。
 */
interface Props {
	taskId: number | null
}

const props = defineProps<Props>()

const emit = defineEmits<{ applied: [taskId: number] }>()

interface Bubble {
	role: 'user' | 'ai'
	text: string
	answer?: MusicAssistantAnswer
}

const input = ref('')
const bubbles = ref<Bubble[]>([])
const busy = ref(false)

const lastAnswer = computed(() => {
	for (let index = bubbles.value.length - 1; index >= 0; index -= 1) {
		if (bubbles.value[index].answer) {
			return bubbles.value[index].answer
		}
	}
	return null
})

async function send(text?: string) {
	const prompt = (text ?? input.value).trim()
	if (!prompt || !props.taskId) {
		if (!props.taskId) {
			ElMessage.warning('先在左边选一首曲子')
		}
		return
	}
	bubbles.value.push({ role: 'user', text: prompt })
	input.value = ''
	busy.value = true
	try {
		const answer = await analyzePrompt(props.taskId, prompt)
		bubbles.value.push({ role: 'ai', text: answer.explanation, answer })
	} catch (error) {
		bubbles.value.push({
			role: 'ai',
			text: error instanceof Error ? error.message : '这次没听明白，再说一次？',
		})
	} finally {
		busy.value = false
	}
}

async function apply() {
	const answer = lastAnswer.value
	if (!answer || !props.taskId) {
		return
	}
	busy.value = true
	try {
		const applied = await applyStrategy(props.taskId, answer.intent.difficulty)
		bubbles.value.push({
			role: 'ai',
			text: `已生成 ${applied.intent.difficultyLabel}版本：「${applied.taskName}」，在左边曲库里可以直接选它。`,
		})
		emit('applied', applied.taskId)
		ElMessage.success('已生成新版本曲目：' + applied.taskName)
	} catch (error) {
		ElMessage.warning(error instanceof Error ? error.message : '应用方案失败')
	} finally {
		busy.value = false
	}
}

watch(() => props.taskId, () => {
	bubbles.value = []
	input.value = ''
})
</script>

<template>
	<div class="ai">
		<div class="ai-hint">
			AI 负责理解与建议、规则负责改写：它不会凭空作曲，也不会直接执行模型给的自由文本。
		</div>

		<div class="ai-chat">
			<div v-if="!bubbles.length" class="ai-empty">
				说说你想要什么样的演奏，比如「让它听起来简单一点」。<br />
				也可以点下面的说法直接试。
			</div>
			<div v-for="(bubble, index) in bubbles" :key="index" class="bubble" :class="bubble.role">
				<div class="who">{{ bubble.role === 'user' ? '我' : '助手' }}</div>
				<div class="text">{{ bubble.text }}</div>
				<div v-if="bubble.answer" class="answer">
					<div class="row">
						<span class="k">方案</span>
						<span class="v">{{ bubble.answer.intent.difficultyLabel }}</span>
						<span class="src" :class="{ model: bubble.answer.source === 'MODEL' }">
							{{ bubble.answer.source === 'MODEL' ? '模型分析 · ' + (bubble.answer.modelName ?? '') : '规则判断' }}
						</span>
					</div>
					<div v-if="bubble.answer.intent.instrument" class="row">
						<span class="k">目标乐器</span><span class="v">{{ bubble.answer.intent.instrument }}</span>
					</div>
					<div v-if="bubble.answer.fallbackReason" class="fallback">{{ bubble.answer.fallbackReason }}</div>
					<ul v-if="bubble.answer.intent.suggestions.length" class="suggestions">
						<li v-for="item in bubble.answer.intent.suggestions" :key="item">{{ item }}</li>
					</ul>
					<div v-if="bubble.answer.adjustments.length" class="changes">
						<div class="changes-title">为什么这么改（逐条解释）</div>
						<div v-for="change in bubble.answer.adjustments" :key="change.title" class="change">
							<div class="change-head">
								<span class="title">{{ change.title }}</span>
								<span class="delta">{{ change.before }} → {{ change.after }}</span>
							</div>
							<div class="detail">{{ change.detail }}</div>
						</div>
					</div>
					<div class="preview">
						音符 {{ bubble.answer.preview.noteCountBefore }} → {{ bubble.answer.preview.noteCountAfter }}
						· 速度 {{ bubble.answer.preview.tempoBefore }} → {{ bubble.answer.preview.tempoAfter }} BPM
						· 时长 {{ (bubble.answer.preview.durationBefore / 1000).toFixed(1) }} → {{ (bubble.answer.preview.durationAfter / 1000).toFixed(1) }} 秒
					</div>
				</div>
			</div>
			<div v-if="busy" class="ai-typing">助手正在看这首曲子…</div>
		</div>

		<div class="ai-presets">
			<button v-for="preset in PROMPT_PRESETS" :key="preset" class="chip" type="button" :disabled="busy" @click="send(preset)">
				{{ preset }}
			</button>
		</div>

		<div class="ai-input">
			<input v-model="input" placeholder="用一句话说说你想要什么" @keyup.enter="send()" />
			<button class="ext-btn" type="button" :disabled="busy" @click="send()">发送</button>
		</div>

		<div v-if="lastAnswer && lastAnswer.intent.difficulty !== 'NORMAL'" class="ai-apply">
			<button class="ext-btn primary" type="button" :disabled="busy" @click="apply">应用方案（生成新版本曲目）</button>
			<span class="tip">原曲不会被改动，新版本会出现在左侧曲库里</span>
		</div>
	</div>
</template>

<style scoped>
.ai { display: flex; flex-direction: column; gap: 10px; }
.ai-hint { font-size: 11px; line-height: 1.75; color: var(--ext-text-mute); }
.ai-chat { display: flex; flex-direction: column; gap: 8px; max-height: 420px; overflow-y: auto; }
.ai-empty { font-size: 11.5px; line-height: 1.9; color: var(--ext-text-mute); }
.bubble { border-radius: 12px; padding: 8px 10px; border: 1px solid var(--ext-line); background: rgba(255, 255, 255, 0.03); }
.bubble.user { border-color: rgba(240, 205, 114, 0.35); background: var(--ext-gold-soft); }
.bubble .who { font-size: 10px; color: var(--ext-text-mute); margin-bottom: 3px; }
.bubble .text { font-size: 12px; line-height: 1.7; color: var(--ext-text); }
.answer { margin-top: 8px; display: flex; flex-direction: column; gap: 5px; }
.row { display: flex; align-items: center; gap: 8px; font-size: 11.5px; }
.row .k { color: var(--ext-text-mute); }
.row .v { color: var(--ext-text); }
.src { font-size: 10px; padding: 1px 6px; border-radius: 999px; border: 1px solid var(--ext-line); color: var(--ext-text-mute); }
.src.model { border-color: var(--ext-gold); color: var(--ext-gold-light); }
.fallback { font-size: 10.5px; line-height: 1.7; color: var(--ext-text-mute); }
.suggestions { margin: 0; padding-left: 16px; font-size: 11px; line-height: 1.8; color: var(--ext-text-dim); }
.changes { border-top: 1px dashed var(--ext-line); padding-top: 6px; display: flex; flex-direction: column; gap: 5px; }
.changes-title { font-size: 10.5px; color: var(--ext-text-mute); }
.change { display: flex; flex-direction: column; gap: 2px; }
.change-head { display: flex; justify-content: space-between; gap: 8px; font-size: 11.5px; }
.change-head .title { color: var(--ext-gold-light); }
.change-head .delta { font-family: var(--ext-font-mono); font-size: 10px; color: var(--ext-text-mute); }
.change .detail { font-size: 11px; line-height: 1.75; color: var(--ext-text-dim); }
.preview { font-size: 10.5px; color: var(--ext-text-mute); font-family: var(--ext-font-mono); }
.ai-typing { font-size: 11px; color: var(--ext-text-mute); }
.ai-presets { display: flex; flex-wrap: wrap; gap: 6px; }
.chip {
	border: 1px solid var(--ext-line); background: rgba(255, 255, 255, 0.03); color: var(--ext-text-dim);
	border-radius: 999px; padding: 3px 10px; font-size: 11px; cursor: pointer;
}
.chip:hover { border-color: var(--ext-gold); color: var(--ext-gold-light); }
.ai-input { display: flex; gap: 6px; }
.ai-input input {
	flex: 1; min-width: 0; background: rgba(255, 255, 255, 0.04); border: 1px solid var(--ext-line);
	border-radius: 8px; color: var(--ext-text); padding: 6px 9px; font-size: 12px;
}
.ai-apply { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.ext-btn.primary { border-color: var(--ext-gold); background: var(--ext-gold-soft); color: var(--ext-gold-light); }
.ai-apply .tip { font-size: 10.5px; color: var(--ext-text-mute); }
</style>
