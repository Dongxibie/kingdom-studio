<script setup lang="ts">
import type { ExtStatusTone } from '@/extensions/_shared/types/common'

interface Props {
	text: string
	tone?: ExtStatusTone
}

withDefaults(defineProps<Props>(), { tone: 'mute' })
</script>

<template>
	<span class="ext-tag" :class="'tone-' + tone">
		<i class="dot" />
		{{ text }}
	</span>
</template>

<style scoped>
.ext-tag {
	display: inline-flex;
	align-items: center;
	gap: 6px;
	font-family: var(--ext-font-mono);
	font-size: 11px;
	padding: 4px 10px;
	border-radius: 999px;
	border: 1px solid var(--ext-line);
	background: var(--ext-bg-raised);
	color: var(--ext-text-dim);
	white-space: nowrap;
}

.ext-tag .dot {
	width: 6px;
	height: 6px;
	border-radius: 50%;
	background: currentColor;
}

.tone-gold {
	color: var(--ext-gold-light);
	border-color: var(--ext-gold);
	background: var(--ext-gold-soft);
}

.tone-red {
	color: #e0a3a5;
	border-color: var(--ext-red);
	background: var(--ext-red-soft);
}

.tone-ok {
	color: var(--ext-ok);
	border-color: rgba(111, 159, 120, 0.5);
}

.tone-mute {
	color: var(--ext-text-mute);
}
</style>
