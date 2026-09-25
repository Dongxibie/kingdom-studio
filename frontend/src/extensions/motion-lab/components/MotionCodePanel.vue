<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { canGenerateThree, generateMissing } from '@/extensions/motion-lab/utils/motion-codegen'
import { MOTION_CODE_TABS, emptyCodeSet } from '@/extensions/motion-lab/types/motion'
import type { MotionCodeSet, MotionCodeTab, MotionDetail } from '@/extensions/motion-lab/types/motion'

interface Props {
	detail: MotionDetail | null
	saving?: boolean
}

const props = withDefaults(defineProps<Props>(), { saving: false })
const emit = defineEmits<{
	/** 保存：把当前五个字段交回父组件落库 */
	save: [code: MotionCodeSet]
}>()

const active = ref<MotionCodeTab>('prompt')
const draft = ref<MotionCodeSet>(emptyCodeSet())

// 切换资源时重置草稿；用 watch 而不是 computed，保证用户编辑不会因为重渲染被覆盖
watch(
	() => props.detail,
	(detail) => {
		draft.value = detail?.code ? { ...emptyCodeSet(), ...detail.code } : emptyCodeSet()
	},
	{ immediate: true },
)

const currentTab = computed(() => MOTION_CODE_TABS.find((tab) => tab.key === active.value))
const currentValue = computed({
	get: () => draft.value[active.value] ?? '',
	set: (value: string) => {
		draft.value = { ...draft.value, [active.value]: value }
	},
})

function copy() {
	const text = currentValue.value
	if (!text) {
		ElMessage.warning('这个页签还是空的')
		return
	}
	if (navigator.clipboard && window.isSecureContext) {
		navigator.clipboard.writeText(text).then(
			() => ElMessage.success('已复制'),
			() => ElMessage.error('复制失败'),
		)
		return
	}
	const area = document.createElement('textarea')
	area.value = text
	area.style.position = 'fixed'
	area.style.left = '-9999px'
	document.body.appendChild(area)
	area.select()
	document.execCommand('copy')
	area.remove()
	ElMessage.success('已复制')
}

/** 一键补全：只填空白字段，不覆盖已有内容 */
function generate() {
	if (!props.detail) {
		return
	}
	const { next, filled } = generateMissing(props.detail, draft.value)
	draft.value = next
	if (filled.length === 0) {
		ElMessage.info(
			canGenerateThree(props.detail.category)
				? '五个字段都已有内容，没有需要补的'
				: '该分类不生成 Three.js（只有 三维 / 粒子 两个分类会生成）',
		)
		return
	}
	ElMessage.success('已生成：' + filled.join('、'))
}

const isEmpty = (key: MotionCodeTab) => !draft.value[key]
</script>

<template>
	<div class="panel">
		<div class="tabs">
			<button
				v-for="tab in MOTION_CODE_TABS"
				:key="tab.key"
				class="tab"
				:class="{ on: active === tab.key, empty: isEmpty(tab.key) }"
				type="button"
				@click="active = tab.key">
				{{ tab.label }}
			</button>
		</div>

		<div class="bar">
			<span class="meta">{{ currentTab?.lang }} · {{ currentValue.length }} 字符</span>
			<button class="ext-btn" type="button" :disabled="!detail" @click="generate">生成模板</button>
			<button class="ext-btn" type="button" @click="copy">复制</button>
			<button class="ext-btn primary" type="button" :disabled="!detail || saving" @click="emit('save', draft)">
				{{ saving ? '保存中…' : '保存' }}
			</button>
		</div>

		<textarea
			v-model="currentValue"
			:disabled="!detail"
			spellcheck="false"
			:placeholder="detail ? '这里是 ' + currentTab?.label + '，可以直接编辑' : '先选一条动效'" />
		<div class="hint">
			「生成模板」只补空白字段：会从 CSS 反推演示结构，包成 Vue / React 组件；
			Three.js 只在 三维 / 粒子 两个分类下生成。
		</div>
	</div>
</template>

<style scoped>
.panel {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.tabs {
	display: flex;
	gap: 6px;
	flex-wrap: wrap;
}

.tab {
	font-family: var(--ext-font-mono);
	font-size: 11px;
	padding: 5px 10px;
	border-radius: 7px;
	border: 1px solid var(--ext-line);
	background: var(--ext-bg-raised);
	color: var(--ext-text-dim);
	cursor: pointer;
	transition: color 0.2s, border-color 0.2s, background-color 0.2s;
}

.tab:hover {
	color: var(--ext-gold-light);
}

.tab.on {
	color: #14161a;
	background: linear-gradient(150deg, var(--ext-gold-light), var(--ext-gold));
	border-color: transparent;
	font-weight: 700;
}

.tab.empty {
	border-style: dashed;
}

.bar {
	display: flex;
	align-items: center;
	gap: 8px;
}

.bar .meta {
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
	color: var(--ext-text-mute);
	margin-right: auto;
}

.ext-btn.primary {
	background: linear-gradient(150deg, var(--ext-gold-light), var(--ext-gold));
	border-color: transparent;
	color: #14161a;
	font-weight: 700;
}

textarea {
	width: 100%;
	box-sizing: border-box;
	height: 300px;
	resize: vertical;
	background: #101215;
	border: 1px solid var(--ext-line);
	border-radius: var(--ext-radius);
	color: #c9d1d9;
	font-family: var(--ext-font-mono);
	font-size: 11.5px;
	line-height: 1.7;
	padding: 12px;
	outline: none;
}

textarea:focus {
	border-color: var(--ext-gold);
}

.hint {
	font-size: 11.5px;
	line-height: 1.8;
	color: var(--ext-text-mute);
}
</style>
