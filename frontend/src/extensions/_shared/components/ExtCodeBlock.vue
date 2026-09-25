<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

interface Props {
	code: string
	lang?: string
	title?: string
}

const props = withDefaults(defineProps<Props>(), { lang: 'text', title: '' })
const copied = ref(false)

/**
 * 复制优先走 navigator.clipboard，失败时退回 textarea + execCommand：
 * 非安全上下文（http 且非 localhost）下 clipboard API 直接不可用。
 */
async function copy() {
	try {
		if (navigator.clipboard && window.isSecureContext) {
			await navigator.clipboard.writeText(props.code)
		} else {
			const ta = document.createElement('textarea')
			ta.value = props.code
			ta.style.position = 'fixed'
			ta.style.left = '-9999px'
			document.body.appendChild(ta)
			ta.select()
			document.execCommand('copy')
			ta.remove()
		}
		copied.value = true
		ElMessage.success('已复制')
		setTimeout(() => (copied.value = false), 1500)
	} catch {
		ElMessage.error('复制失败，请手动选中')
	}
}
</script>

<template>
	<div class="ext-code">
		<div class="bar">
			<span class="t">{{ title || lang }}</span>
			<button class="ext-btn" type="button" @click="copy">{{ copied ? '已复制' : '复制' }}</button>
		</div>
		<pre><code>{{ code }}</code></pre>
	</div>
</template>

<style scoped>
.ext-code {
	border: 1px solid var(--ext-line);
	border-radius: var(--ext-radius);
	background: #101215;
	overflow: hidden;
}

.bar {
	display: flex;
	align-items: center;
	gap: 10px;
	padding: 8px 12px;
	border-bottom: 1px solid var(--ext-line-soft);
	background: var(--ext-bg-soft);
}

.bar .t {
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
	letter-spacing: 0.12em;
	color: var(--ext-text-mute);
	margin-right: auto;
}

pre {
	margin: 0;
	padding: 14px;
	max-height: 340px;
	overflow: auto;
}

code {
	font-family: var(--ext-font-mono);
	font-size: 11.5px;
	line-height: 1.7;
	color: #c9d1d9;
	white-space: pre;
}
</style>
