<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ExtCodeBlock } from '@/extensions/_shared/components'
import { exportMacro, generatePerformancePlan } from '@/extensions/music-agent/api/macro'
import { MACRO_FORMATS, type MacroExport, type PerformancePlan } from '@/extensions/music-agent/types/macro'

/**
 * 导出中心（Export Center）。
 *
 * 演奏计划是从按键序列与原有映射算出来的（这里一行逻辑都没改），
 * 这一层只负责把它摆清楚：三种格式各是一张卡，选中哪个就预览哪个，可复制可下载。
 */
interface Props {
	taskId: number | null
	profileId: number | null
	strategy?: string
	/** 当前演奏方案：生成计划时按它执行（档案 / 策略 / 速度倍率 / 最小间隔） */
	presetId?: number | null
	/** 紧凑模式：嵌在编排台右栏时只留卡片与预览 */
	embedded?: boolean
}

const props = withDefaults(defineProps<Props>(), {
	strategy: '',
	presetId: null,
	embedded: false,
})

const plan = ref<PerformancePlan | null>(null)
const exports = ref<Record<string, MacroExport>>({})
const format = ref<string>(MACRO_FORMATS[0].value)
const loading = ref(false)
const error = ref('')

const activeExport = computed(() => exports.value[format.value] ?? null)

const metrics = computed(() => {
	const value = plan.value
	if (!value) {
		return []
	}
	return [
		{ label: '按键事件', value: String(value.noteCount) + ' events' },
		{ label: '时长', value: (value.duration / 1000).toFixed(1) + ' 秒' },
		{ label: '按键组', value: String(value.strokeCount) },
		{ label: '用到的键', value: String(value.keyCount) },
	]
})

const formatHint: Record<string, string> = {
	AHK: '可直接双击运行的 AutoHotkey 脚本，含暂停与急停键',
	TXT: '带时间戳的按键表，照着练或打印出来都行',
	JSON: '结构化演奏计划，接自己的程序或做二次加工',
}

async function build() {
	// 先取到局部变量：props 在闭包里不会被 TypeScript 收窄
	const taskId = props.taskId
	const profileId = props.profileId
	if (!taskId || profileId === null) {
		error.value = '先选一首曲子与乐器档案'
		return
	}
	loading.value = true
	error.value = ''
	try {
		plan.value = await generatePerformancePlan(taskId, profileId, props.strategy || undefined, props.presetId)
		await loadExports()
	} catch (e) {
		error.value = e instanceof Error ? e.message : '生成演奏计划失败'
	} finally {
		loading.value = false
	}
}

async function loadExports() {
	const taskId = props.taskId
	if (!taskId) {
		return
	}
	const entries = await Promise.all(
		MACRO_FORMATS.map(async (item) => {
			try {
				const result = await exportMacro(taskId, item.value)
				return [item.value, result] as const
			} catch {
				// 某一种格式拿不到不影响另外两种
				return null
			}
		}),
	)
	const next: Record<string, MacroExport> = {}
	for (const entry of entries) {
		if (entry) {
			next[entry[0]] = entry[1]
		}
	}
	exports.value = next
}

function download() {
	const value = activeExport.value
	if (!value) {
		ElMessage.warning('先选一个格式')
		return
	}
	const blob = new Blob([value.content], { type: value.contentType || 'text/plain;charset=utf-8' })
	const url = URL.createObjectURL(blob)
	const link = document.createElement('a')
	link.href = url
	link.download = value.filename
	document.body.appendChild(link)
	link.click()
	link.remove()
	window.setTimeout(() => URL.revokeObjectURL(url), 3000)
}

watch(
	() => [props.taskId, props.profileId, props.strategy, props.presetId],
	() => {
		plan.value = null
		exports.value = {}
		if (props.taskId && props.profileId !== null) {
			void build()
		}
	},
	{ immediate: true },
)
</script>

<template>
	<div class="ec">
		<div class="ec-head">
			<div class="ec-metrics">
				<div v-for="item in metrics" :key="item.label" class="st-metric">
					<span class="st-metric-value">{{ item.value }}</span>
					<span class="st-metric-label">{{ item.label }}</span>
				</div>
			</div>
			<button class="st-btn" type="button" :disabled="loading || !taskId" @click="build">
				{{ loading ? '生成中…' : plan ? '重新生成' : '生成演奏计划' }}
			</button>
		</div>

		<p v-if="error" class="ec-error">{{ error }}</p>
		<p v-if="!taskId" class="ec-hint">先选一首曲子，这里会给出它的演奏脚本。</p>

		<div v-if="plan" class="ec-supported">
			<span class="st-label">支持导出</span>
			<span v-for="item in MACRO_FORMATS" :key="item.value" class="ec-supported-item">
				✓ {{ item.label }}<span class="ec-supported-size">{{ exports[item.value] ? (exports[item.value].size / 1024).toFixed(1) + ' KB' : '—' }}</span>
			</span>
		</div>

		<div v-if="plan" class="ec-formats">
			<button
				v-for="item in MACRO_FORMATS"
				:key="item.value"
				class="ec-format"
				:class="{ on: format === item.value }"
				type="button"
				@click="format = item.value">
				<div class="ec-format-top">
					<span class="ec-format-name">{{ item.label }}</span>
					<span class="ec-format-size">{{ exports[item.value] ? (exports[item.value].size / 1024).toFixed(1) + ' KB' : '—' }}</span>
				</div>
				<div class="ec-format-hint">{{ formatHint[item.value] }}</div>
				<div class="ec-format-file">{{ exports[item.value]?.filename ?? '尚未生成' }}</div>
			</button>
		</div>

		<div v-if="plan?.warnings?.length" class="ec-warn">
			<div class="st-label">需要注意的地方</div>
			<ul>
				<li v-for="(warning, index) in plan.warnings" :key="index">{{ warning }}</li>
			</ul>
		</div>

		<div v-if="plan" class="ec-actions">
			<button class="st-btn st-btn--primary" type="button" :disabled="!activeExport" @click="download">
				下载 {{ activeExport?.filename ?? '' }}
			</button>
			<span class="ec-hint">复制用下面代码块右上角的按钮</span>
		</div>

		<ExtCodeBlock
			v-if="activeExport"
			:key="format"
			:code="activeExport.content"
			:lang="activeExport.format === 'AHK' ? 'ahk' : activeExport.format === 'JSON' ? 'json' : 'text'"
			:title="activeExport.filename" />

		<p v-if="plan && !activeExport" class="ec-hint">这个格式暂时拿不到内容，换一个试试。</p>

		<p v-if="plan" class="ec-hint">
			演奏计划：{{ plan.noteCount }} 个事件、{{ (plan.duration / 1000).toFixed(1) }} 秒、{{ plan.strokeCount }} 组按键。
			{{ embedded ? '' : '导出的是按键动作，不会自动在后台运行。' }}
		</p>
	</div>
</template>

<style scoped>
.ec {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.ec-head {
	display: flex;
	align-items: flex-end;
	justify-content: space-between;
	gap: 10px;
	flex-wrap: wrap;
}

.ec-metrics {
	display: flex;
	gap: 8px;
	flex-wrap: wrap;
}

.ec-supported {
	display: flex;
	flex-wrap: wrap;
	align-items: center;
	gap: 8px;
	border: 1px solid var(--st-edge);
	border-radius: 10px;
	padding: 8px 11px;
	background: var(--st-glass);
}

.ec-supported-item {
	display: inline-flex;
	align-items: baseline;
	gap: 6px;
	font-family: var(--ext-font-mono);
	font-size: 11px;
	color: #7ed6a5;
}

.ec-supported-size {
	font-size: 9.5px;
	color: var(--ext-text-mute);
}

.ec-formats {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.ec-format {
	text-align: left;
	border: 1px solid var(--st-edge);
	border-radius: 12px;
	background: var(--st-glass);
	padding: 10px 12px;
	cursor: pointer;
	transition: border-color 0.18s ease, transform 0.18s ease, background 0.18s ease;
}

.ec-format:hover {
	border-color: var(--st-edge-hot);
	transform: translateY(-1px);
}

.ec-format.on {
	border-color: var(--ext-gold);
	background: linear-gradient(180deg, rgba(240, 205, 114, 0.16), rgba(240, 205, 114, 0.05));
}

.ec-format-top {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
	gap: 8px;
}

.ec-format-name {
	font-family: var(--ext-font-mono);
	font-size: 12.5px;
	color: var(--ext-text);
}

.ec-format-size {
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-text-mute);
}

.ec-format-hint {
	margin-top: 3px;
	font-size: 11px;
	color: var(--ext-text-dim);
}

.ec-format-file {
	margin-top: 3px;
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-text-mute);
}

.ec-warn {
	border: 1px solid rgba(240, 205, 114, 0.35);
	border-radius: 10px;
	background: rgba(240, 205, 114, 0.08);
	padding: 9px 11px;
}

.ec-warn ul {
	margin: 4px 0 0;
	padding-left: 16px;
	font-size: 11px;
	color: var(--ext-text-dim);
	line-height: 1.7;
}

.ec-actions {
	display: flex;
	align-items: center;
	gap: 10px;
	flex-wrap: wrap;
}

.ec-hint {
	margin: 0;
	font-size: 10.5px;
	color: var(--ext-text-mute);
	line-height: 1.7;
}

.ec-error {
	margin: 0;
	font-size: 11px;
	color: #ff9a9a;
}
</style>
