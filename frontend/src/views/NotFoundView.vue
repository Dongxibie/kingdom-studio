<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
	<el-card shadow="never" class="not-found">
		<div class="code">404</div>
		<h2>这座建筑还没建成</h2>
		<p>你访问的页面不在王国版图内，可能是地址写错了，或者该功能还没开工。</p>
		<div class="actions">
			<el-button type="primary" @click="router.push('/dashboard')">回到工作台</el-button>
			<el-button @click="router.back()">返回上一页</el-button>
		</div>
	</el-card>
</template>

<style scoped>
.not-found {
	max-width: 560px;
	text-align: center;
	padding: 12px 0;
}

.code {
	font-size: 56px;
	font-weight: 800;
	line-height: 1;
	letter-spacing: 2px;
	background: linear-gradient(135deg, var(--kingdom-red) 0%, var(--kingdom-gold) 100%);
	-webkit-background-clip: text;
	-webkit-text-fill-color: transparent;
	background-clip: text;
}

h2 {
	margin: 14px 0 8px;
	font-size: 20px;
}

p {
	color: #606266;
	line-height: 1.8;
	margin: 0 0 20px;
}

.actions {
	display: flex;
	justify-content: center;
	gap: 12px;
}
</style>
