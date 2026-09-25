<script setup lang="ts">
import { computed, ref } from 'vue'
import { buildPreviewHtml } from '@/extensions/motion-lab/utils/motion-codegen'

interface Props {
	/** 预览用的 CSS；为空时展示空状态 */
	cssCode: string
	name: string
	category: string
	technology: string
	license: string
}

const props = defineProps<Props>()
const scale = ref(1)
const reloadKey = ref(0)

/**
 * 预览走 sandbox iframe + srcdoc：
 * - 只给 allow-scripts，**不给 allow-same-origin**，iframe 处于独立源，拿不到宿主页面
 * - 绝不用 eval / new Function 执行资源里的 JS
 * - 这里注入的只是 CSS，脚本不会被执行（不写 allow-scripts 也能动画，为了后续 JS 类动效才留）
 */
const srcdoc = computed(() => buildPreviewHtml(props.cssCode || ''))
</script>

<template>
	<div class="preview">
		<div class="head">
			<span class="t">实时预览</span>
			<span class="cat">{{ category }} · {{ technology || '未标注技术' }}</span>
			<span v-if="license" class="lic">许可：{{ license }}</span>
			<div class="tools">
				<span class="hint">缩放</span>
				<input v-model.number="scale" type="range" min="0.6" max="1.4" step="0.1" />
				<button class="ext-btn" type="button" @click="reloadKey++">重播</button>
			</div>
		</div>
		<div class="frame">
			<iframe
				v-if="cssCode"
				:key="reloadKey"
				:srcdoc="srcdoc"
				sandbox="allow-scripts"
				title="动效预览"
				:style="{ transform: 'scale(' + scale + ')' }" />
			<div v-else class="empty">
				<p>这条动效还没有 CSS 代码，先生成或粘贴一份，预览会自动出现。</p>
				<p class="tip">预览在沙箱 iframe 里运行；不会执行任何未知脚本。</p>
			</div>
		</div>
	</div>
</template>

<style scoped>
.preview {
	display: flex;
	flex-direction: column;
	gap: 10px;
	height: 100%;
}

.head {
	display: flex;
	align-items: center;
	gap: 10px;
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
	color: var(--ext-text-mute);
	letter-spacing: 0.1em;
}

.head .cat {
	color: var(--ext-gold-light);
}

.head .lic {
	color: var(--ext-text-mute);
}

.tools {
	margin-left: auto;
	display: flex;
	align-items: center;
	gap: 8px;
}

.tools input[type='range'] {
	width: 96px;
	accent-color: var(--ext-gold);
}

.frame {
	position: relative;
	height: 420px;
	border: 1px solid var(--ext-line);
	border-radius: var(--ext-radius);
	overflow: hidden;
	background: #101319;
}

iframe {
	width: 100%;
	height: 100%;
	border: 0;
	display: block;
	transform-origin: center center;
}

.empty {
	position: absolute;
	inset: 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 10px;
	text-align: center;
	font-size: 12.5px;
	color: var(--ext-text-mute);
	padding: 0 30px;
	line-height: 1.9;
}

.empty .tip {
	font-family: var(--ext-font-mono);
	font-size: 11px;
	opacity: 0.75;
}
</style>
