<script setup lang="ts">
defineProps<{
	title: string
	subtitle: string
	description: string
	phase: string
}>()
</script>

<template>
	<el-card shadow="never" class="placeholder">
		<div class="subtitle">{{ subtitle }}</div>
		<h2>{{ title }}</h2>
		<p>{{ description }}</p>
		<el-alert type="info" show-icon :closable="false" :title="`该模块的接口与页面将在${phase}实现`" />
		<el-divider />
		<p class="hint">当前已完成工程骨架、数据库与前后端联通；这一页的功能尚未开发，先留作占位。</p>
	</el-card>
</template>

<style scoped>
.placeholder {
	max-width: 720px;
}

.subtitle {
	font-size: 12px;
	letter-spacing: 0.12em;
	text-transform: uppercase;
	color: var(--kingdom-gold);
}

h2 {
	margin: 6px 0 10px;
}

p {
	color: #606266;
	line-height: 1.8;
}

.hint {
	font-size: 13px;
	color: #909399;
}
</style>
