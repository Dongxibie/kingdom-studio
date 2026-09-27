<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ExtCodeBlock } from '@/extensions/_shared/components'
import { exportMotions } from '@/extensions/motion-lab/api/template'
import { EXPORT_FORMATS, type MotionExport } from '@/extensions/motion-lab/types/export'
import { buildZip, downloadBlob } from '@/extensions/motion-lab/utils/zip'

/**
 * 代码导出面板（Phase 5）。
 *
 * 工作台里每个模板都已经有代码面板，但那给的是「一个动效」的片段；
 * 这里给的是「一整套方案」的工程文件：主组件 + 每个步骤一个文件 + 配置 + 样式。
 *
 * 预览用的是后端返回的组合页面，与导出的 HTML 同一份数据渲染 —— 所见即所得不是靠对图，
 * 而是靠同一份来源。
 */
interface Props {
	/** 方案 key（组合方案） */
	recipeKey: string
	/** 方案名，用于标题与注释 */
	planName: string
	/** 临时组合时直接给模板清单（与 recipeKey 二选一） */
	templateKeys?: string[]
	/** 参数覆盖：模板 key → { CSS 变量名 → 数值 } */
	params?: Record<string, Record<string, number>>
}

const props = defineProps<Props>()

const format = ref('VUE')
const loading = ref(false)
const result = ref<MotionExport | null>(null)
const activeIndex = ref(0)
const restartToken = ref(0)
const error = ref('')

const activeFile = computed(() => result.value?.files[activeIndex.value] ?? null)
const totalBytes = computed(() => (result.value?.files ?? []).reduce((sum, file) => sum + file.bytes, 0))
const canExport = computed(() => Boolean(props.recipeKey || props.templateKeys?.length))

async function run() {
	if (!canExport.value) {
		error.value = '先选好一套方案再导出'
		return
	}
	loading.value = true
	error.value = ''
	try {
		result.value = await exportMotions({
			recipeKey: props.recipeKey || undefined,
			planName: props.planName,
			format: format.value,
			templateKeys: props.templateKeys,
			params: props.params,
		})
		activeIndex.value = 0
		restartToken.value += 1
	} catch (e) {
		error.value = e instanceof Error ? e.message : '导出失败，请稍后再试'
	} finally {
		loading.value = false
	}
}

function pickFormat(value: string) {
	format.value = value
	if (result.value) {
		void run()
	}
}

function downloadOne(path: string) {
	const file = result.value?.files.find((item) => item.path === path)
	if (!file) {
		return
	}
	downloadBlob(new Blob([file.content], { type: 'text/plain;charset=utf-8' }), file.path.split('/').pop() ?? 'file.txt')
}

function downloadZip() {
	const value = result.value
	if (!value) {
		return
	}
	const prefix = (value.recipeKey || 'motion-plan').replace(/[^\w-]/g, '')
	downloadBlob(buildZip(value.files.map((file) => ({ path: file.path, content: file.content }))), prefix + '-' + value.format.toLowerCase() + '.zip')
	ElMessage.success('已打包下载 ' + value.files.length + ' 个文件')
}

function replay() {
	restartToken.value += 1
}

defineExpose({ run })
</script>

<template>
	<div class="mexp">
		<div class="mexp-formats">
			<button
				v-for="item in EXPORT_FORMATS"
				:key="item.value"
				class="mexp-format"
				:class="{ on: format === item.value }"
				type="button"
				:title="item.hint"
				@click="pickFormat(item.value)">
				{{ item.label }}
			</button>
		</div>
		<p class="mexp-hint">{{ EXPORT_FORMATS.find((item) => item.value === format)?.hint }}</p>

		<button class="mexp-run" type="button" :disabled="loading || !canExport" @click="run">
			{{ loading ? '正在生成…' : '生成代码' }}
		</button>
		<p v-if="error" class="mexp-error">{{ error }}</p>

		<template v-if="result">
			<div class="mexp-meta">
				<span>{{ result.formatLabel }}</span>
				<i>·</i>
				<span>{{ result.files.length }} 个文件</span>
				<i>·</i>
				<span>{{ (totalBytes / 1024).toFixed(1) }} KB</span>
				<i>·</i>
				<span>{{ result.stepCount }} 步</span>
			</div>
			<div class="mexp-actions">
				<button class="ext-btn" type="button" @click="downloadZip">打包下载 ZIP</button>
				<button v-if="activeFile" class="ext-btn" type="button" @click="downloadOne(activeFile.path)">下载当前文件</button>
			</div>

			<div class="mexp-preview">
				<div class="mexp-preview-head">
					<span>组合预览（导出结果的样子）</span>
					<button class="ext-btn" type="button" @click="replay">重播</button>
				</div>
				<iframe
					:key="restartToken"
					class="mexp-frame"
					title="导出预览"
					sandbox="allow-scripts"
					:srcdoc="result.previewHtml"></iframe>
			</div>

			<div class="mexp-files">
				<button
					v-for="(file, index) in result.files"
					:key="file.path"
					class="mexp-file"
					:class="{ on: index === activeIndex }"
					type="button"
					@click="activeIndex = index">
					<span class="mexp-path">{{ file.path }}</span>
					<span class="mexp-role">{{ file.role }}</span>
				</button>
			</div>

			<ExtCodeBlock v-if="activeFile" :code="activeFile.content" :lang="activeFile.language" :title="activeFile.path" />

			<ul class="mexp-notes">
				<li v-for="(note, index) in result.notes" :key="index">{{ note }}</li>
			</ul>
		</template>
	</div>
</template>

<style scoped>
.mexp {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.mexp-formats {
	display: flex;
	gap: 6px;
}

.mexp-format {
	flex: 1;
	border: 1px solid var(--ext-line);
	background: rgba(255, 255, 255, 0.03);
	color: var(--ext-text-dim);
	border-radius: 9px;
	font-size: 12px;
	padding: 6px 8px;
	cursor: pointer;
	transition: border-color 0.18s ease, color 0.18s ease, background 0.18s ease;
}

.mexp-format:hover {
	border-color: rgba(240, 205, 114, 0.45);
	color: var(--ext-gold-light);
}

.mexp-format.on {
	border-color: var(--ext-gold);
	background: var(--ext-gold-soft);
	color: var(--ext-gold-light);
}

.mexp-hint {
	margin: 0;
	font-size: 10.5px;
	color: var(--ext-text-mute);
	line-height: 1.7;
}

.mexp-run {
	border: 1px solid var(--ext-gold);
	background: var(--ext-gold-soft);
	color: var(--ext-gold-light);
	border-radius: 10px;
	font-size: 12.5px;
	padding: 8px 12px;
	cursor: pointer;
}

.mexp-run:disabled {
	opacity: 0.6;
	cursor: default;
}

.mexp-error {
	margin: 0;
	font-size: 11.5px;
	color: #ff9a9a;
}

.mexp-meta {
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
	font-size: 11px;
	color: var(--ext-text-mute);
}

.mexp-meta i {
	font-style: normal;
	opacity: 0.5;
}

.mexp-actions {
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
}

.mexp-preview {
	border: 1px solid var(--ext-line);
	border-radius: 12px;
	overflow: hidden;
}

.mexp-preview-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
	padding: 7px 10px;
	font-size: 11px;
	color: var(--ext-text-dim);
	border-bottom: 1px solid var(--ext-line);
}

.mexp-frame {
	width: 100%;
	height: 320px;
	border: 0;
	background: #05060a;
	display: block;
}

.mexp-files {
	display: flex;
	flex-direction: column;
	gap: 5px;
	max-height: 220px;
	overflow: auto;
}

.mexp-file {
	text-align: left;
	border: 1px solid var(--ext-line);
	background: rgba(255, 255, 255, 0.03);
	border-radius: 9px;
	padding: 6px 9px;
	cursor: pointer;
	display: flex;
	flex-direction: column;
	gap: 2px;
}

.mexp-file:hover {
	border-color: rgba(240, 205, 114, 0.4);
}

.mexp-file.on {
	border-color: var(--ext-gold);
	background: var(--ext-gold-soft);
}

.mexp-path {
	font-family: var(--ext-font-mono);
	font-size: 11px;
	color: var(--ext-text);
}

.mexp-role {
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.mexp-notes {
	margin: 0;
	padding-left: 16px;
	display: flex;
	flex-direction: column;
	gap: 5px;
	font-size: 11px;
	color: var(--ext-text-mute);
	line-height: 1.7;
}
</style>
