<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ExtCodeBlock } from '@/extensions/_shared/components'
import {
	executePlan,
	exportMacro,
	fetchExecutionModes,
	fetchPerformancePlan,
	generatePerformancePlan,
} from '@/extensions/music-agent/api/macro'
import { MACRO_FORMATS } from '@/extensions/music-agent/types/macro'
import type { ExecutionMode, ExecutionResult, MacroExport, PerformancePlan } from '@/extensions/music-agent/types/macro'
import { formatMs } from '@/extensions/music-agent/utils/note-format'

/**
 * 演奏宏导出：把「按键序列」变成可带走的脚本。
 *
 * 界面按三步组织：先生成计划 → 再选格式导出 → 最后（可选）交给执行层。
 * 执行层故意只留「仅模拟」可用：另外两个模式会如实告诉使用者为什么还没开，
 * 而不是给一个「点了没反应」的按钮。
 */
interface Props {
	taskId: number | null
	profileId: number | null
	strategy: string
}

const props = defineProps<Props>()

const emit = defineEmits<{ close: [] }>()

const plan = ref<PerformancePlan | null>(null)
const exporting = ref<MacroExport | null>(null)
const format = ref<string>('AHK')
const result = ref<ExecutionResult | null>(null)
const modes = ref<ExecutionMode[]>([])
const busy = ref(false)
const loading = ref(false)

const commandPreview = computed(() => {
	const commands = result.value?.commands ?? []
	return commands.slice(0, 40).map((command) => {
		const hold = command.holdMs ? `（按住 ${command.holdMs}ms）` : ''
		return `${String(command.seq).padStart(4, '0')}  ${formatMs(command.atMs)}  ${command.action === 'DOWN' ? '按下' : '松开'} ${command.key}${hold}`
	}).join('\n')
})

const summary = computed(() => {
	const value = plan.value
	if (!value) {
		return ''
	}
	return `${value.taskName} · ${value.profileName} ｜ 时长 ${(value.duration / 1000).toFixed(1)} 秒`
		+ ` ｜ 按键 ${value.strokeCount} 次（${value.keyCount} 个键） ｜ 事件 ${value.noteCount} 条`
})

async function loadPlan() {
	if (!props.taskId) {
		return
	}
	loading.value = true
	try {
		plan.value = await fetchPerformancePlan(props.taskId)
	} catch {
		// 还没生成过计划：留空让使用者点生成，不算错误
		plan.value = null
	} finally {
		loading.value = false
	}
}

async function loadModes() {
	if (!props.taskId) {
		return
	}
	try {
		modes.value = await fetchExecutionModes(props.taskId)
	} catch {
		modes.value = []
	}
}

async function generate() {
	if (!props.taskId || !props.profileId) {
		ElMessage.warning('先选好乐器档案并生成按键序列')
		return
	}
	busy.value = true
	try {
		plan.value = await generatePerformancePlan(props.taskId, props.profileId, props.strategy)
		result.value = null
		await loadModes()
		ElMessage.success('已生成演奏计划')
		await exportAs(format.value)
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '生成演奏计划失败')
	} finally {
		busy.value = false
	}
}

async function exportAs(target: string) {
	if (!props.taskId) {
		return
	}
	format.value = target
	busy.value = true
	try {
		exporting.value = await exportMacro(props.taskId, target)
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '导出失败')
	} finally {
		busy.value = false
	}
}

/** 下载：把导出内容存成文件（浏览器里直接触发一次下载） */
function download() {
	const value = exporting.value
	if (!value) {
		return
	}
	const blob = new Blob([value.content], { type: value.contentType })
	const url = URL.createObjectURL(blob)
	const link = document.createElement('a')
	link.href = url
	link.download = value.filename
	document.body.appendChild(link)
	link.click()
	link.remove()
	URL.revokeObjectURL(url)
	ElMessage.success('已下载 ' + value.filename)
}

async function runPreview() {
	if (!props.taskId) {
		return
	}
	busy.value = true
	try {
		result.value = await executePlan(props.taskId, 'PREVIEW')
		ElMessage.success('已列出将要发出的命令（没有任何真实输入）')
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '模拟执行失败')
	} finally {
		busy.value = false
	}
}

async function explainMode(mode: ExecutionMode) {
	if (mode.available) {
		return
	}
	await ElMessageBox.alert(mode.reason, mode.label + '：当前未开启', {
		confirmButtonText: '知道了',
		type: 'warning',
	})
}

watch(() => [props.taskId, props.profileId], async () => {
	result.value = null
	exporting.value = null
	await Promise.all([loadPlan(), loadModes()])
}, { immediate: true })
</script>

<template>
	<el-dialog :model-value="true" title="导出演奏脚本" width="820px" append-to-body @close="emit('close')">
		<div class="macro">
			<div class="macro-step">
				<span class="k">1</span>
				<div class="body">
					<div class="title">演奏计划</div>
					<div v-if="plan" class="line">{{ summary }}</div>
					<div v-else class="line mute">{{ loading ? '读取中…' : '还没有生成计划，点右边按钮生成一份' }}</div>
					<div v-if="plan && plan.truncated" class="line mute">事件流较长，这里的预览已截断，导出的是完整内容。</div>
				</div>
				<button class="ext-btn" type="button" :disabled="busy" @click="generate">
					{{ plan ? '重新生成' : '生成计划' }}
				</button>
			</div>

			<div v-if="plan" class="macro-warn">
				<div class="warn-title">命令流校验</div>
				<ul>
					<li v-for="(warning, index) in plan.warnings" :key="index">{{ warning }}</li>
					<li v-if="!plan.warnings.length" class="ok">没有任何调整：每个按键的时长与间隔都在安全范围内。</li>
				</ul>
			</div>

			<div v-if="plan" class="macro-step">
				<span class="k">2</span>
				<div class="body">
					<div class="title">选择格式</div>
					<div class="formats">
						<button
							v-for="item in MACRO_FORMATS"
							:key="item.value"
							class="format"
							:class="{ on: format === item.value }"
							type="button"
							@click="exportAs(item.value)">
							<span class="name">{{ item.label }}</span>
							<span class="hint">{{ item.hint }}</span>
						</button>
					</div>
				</div>
			</div>

			<div v-if="exporting" class="macro-step">
				<span class="k">3</span>
				<div class="body">
					<div class="title">
						{{ exporting.filename }}
						<span class="mute">（{{ exporting.size }} 字符）</span>
						<button class="ext-btn" type="button" @click="download">下载</button>
					</div>
					<div class="hint">{{ exporting.hint }}</div>
					<ExtCodeBlock :code="exporting.content" :title="exporting.format + ' 内容（可复制）'" />
				</div>
			</div>

			<div v-if="plan" class="macro-exec">
				<div class="exec-head">
					<span class="title">执行</span>
					<span class="mute">默认只模拟：不会产生任何真实按键输入</span>
					<button class="ext-btn" type="button" :disabled="busy" @click="runPreview">仅模拟执行</button>
				</div>
				<div class="exec-modes">
					<button
						v-for="mode in modes"
						:key="mode.mode"
						class="mode"
						:class="{ off: !mode.available }"
						type="button"
						@click="explainMode(mode)">
						{{ mode.label }}
						<span class="state">{{ mode.available ? '可用' : '未开启' }}</span>
					</button>
				</div>
				<div v-if="result" class="exec-result">
					<div class="line">{{ result.message }}</div>
					<ExtCodeBlock v-if="commandPreview" :code="commandPreview" title="将要发出的命令（前 40 条）" />
					<ul class="safety">
						<li v-for="item in result.safety" :key="item">{{ item }}</li>
					</ul>
				</div>
			</div>
		</div>
	</el-dialog>
</template>

<style scoped>
.macro { display: flex; flex-direction: column; gap: 14px; }
.macro-step { display: flex; gap: 10px; align-items: flex-start; }
.macro-step .k {
	flex: 0 0 22px; height: 22px; border-radius: 50%;
	display: grid; place-items: center;
	background: var(--ext-gold-soft); color: var(--ext-gold-light);
	font-family: var(--ext-font-mono); font-size: 12px;
}
.macro-step .body { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.title { font-size: 12.5px; color: var(--ext-text); display: flex; align-items: center; gap: 8px; }
.line { font-size: 11.5px; line-height: 1.75; color: var(--ext-text-dim); }
.mute { color: var(--ext-text-mute); font-size: 11px; }
.macro-warn { border: 1px solid var(--ext-line); border-radius: var(--ext-radius); padding: 8px 10px; background: var(--ext-bg-raised); }
.warn-title { font-size: 11.5px; color: var(--ext-text-mute); margin-bottom: 4px; }
.macro-warn ul { margin: 0; padding-left: 16px; font-size: 11px; line-height: 1.8; color: var(--ext-text-dim); }
.macro-warn .ok { color: #7ee0a2; }
.formats { display: flex; flex-direction: column; gap: 6px; }
.format {
	text-align: left; padding: 8px 10px; border-radius: 10px; cursor: pointer;
	border: 1px solid var(--ext-line); background: rgba(255, 255, 255, 0.03); color: var(--ext-text);
	display: flex; flex-direction: column; gap: 2px;
}
.format.on { border-color: var(--ext-gold); background: var(--ext-gold-soft); }
.format .name { font-size: 12.5px; }
.format .hint { font-size: 10.5px; color: var(--ext-text-mute); }
.macro-exec { border-top: 1px dashed var(--ext-line); padding-top: 12px; display: flex; flex-direction: column; gap: 8px; }
.exec-head { display: flex; align-items: center; gap: 10px; }
.exec-modes { display: flex; gap: 6px; flex-wrap: wrap; }
.mode {
	border: 1px solid var(--ext-line); background: rgba(255, 255, 255, 0.03); color: var(--ext-text-dim);
	border-radius: 999px; padding: 4px 10px; font-size: 11.5px; cursor: pointer;
}
.mode.off { color: var(--ext-text-mute); border-style: dashed; }
.mode .state { font-size: 10px; margin-left: 6px; color: var(--ext-text-mute); }
.exec-result { display: flex; flex-direction: column; gap: 8px; }
.safety { margin: 0; padding-left: 16px; font-size: 11px; line-height: 1.8; color: var(--ext-text-mute); }
</style>
