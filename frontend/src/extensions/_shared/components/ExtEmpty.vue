<script setup lang="ts">
interface Props {
	title: string
	hint?: string
	tag?: string
}

withDefaults(defineProps<Props>(), { hint: '', tag: '' })
</script>

<template>
	<div class="ext-empty">
		<div v-if="tag" class="tag">{{ tag }}</div>
		<div class="title">{{ title }}</div>
		<div v-if="hint" class="hint">{{ hint }}</div>
		<div class="slot"><slot /></div>
	</div>
</template>

<style scoped>
.ext-empty {
	border: 1px dashed var(--ext-line);
	border-radius: var(--ext-radius);
	padding: 34px 26px;
	text-align: center;
	background: repeating-linear-gradient(45deg, var(--ext-bg-soft) 0 12px, var(--ext-bg) 12px 24px);
}

.tag {
	display: inline-block;
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
	color: var(--ext-gold-light);
	border: 1px solid var(--ext-gold);
	background: var(--ext-gold-soft);
	border-radius: 999px;
	padding: 3px 10px;
}

.title {
	font-size: 15px;
	font-weight: 600;
	margin-top: 14px;
}

.hint {
	font-size: 12.5px;
	line-height: 1.95;
	color: var(--ext-text-mute);
	max-width: 560px;
	margin: 12px auto 0;
}
</style>
