<script setup lang="ts">
import { useNarrowScreen } from '@/composables/useNarrowScreen'
// 主题样式只在这里引入：扩展页路由是懒加载的，样式跟随扩展 chunk，不进主站首屏包
import '@/extensions/_shared/styles/ext-theme.css'

interface Props {
	title: string
	subtitle?: string
	mark?: string
}

withDefaults(defineProps<Props>(), { subtitle: '', mark: 'K' })

// 复用主站已有的窄屏判断：窄屏下三栏改纵向堆叠，
// 与 BasicLayout 侧边栏收起用同一套断点，避免出现两种「窄屏」行为
const { isNarrow } = useNarrowScreen()
</script>

<template>
	<div class="ext-shell">
		<header class="ext-head">
			<div class="ext-mark">{{ mark }}</div>
			<div>
				<div class="ext-title">{{ title }}</div>
				<div v-if="subtitle" class="ext-sub">{{ subtitle }}</div>
			</div>
			<div class="ext-actions"><slot name="actions" /></div>
		</header>

		<div class="ext-body" :class="{ narrow: isNarrow }">
			<aside class="ext-col">
				<slot name="left" />
			</aside>
			<section class="ext-col">
				<div class="ext-panel"><slot /></div>
			</section>
			<aside class="ext-col">
				<slot name="right" />
			</aside>
		</div>
	</div>
</template>
