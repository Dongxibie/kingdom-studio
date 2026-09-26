<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { fetchHealth, type HealthInfo } from '@/api/health'
import { useNarrowScreen } from '@/composables/useNarrowScreen'

const loading = ref(false)
const health = ref<HealthInfo | null>(null)
const errorMessage = ref('')

// 窄屏下信息表退回单列，三列会把每个格子的中文压成竖排
const { isNarrow } = useNarrowScreen()

async function loadHealth() {
	loading.value = true
	errorMessage.value = ''
	try {
		health.value = await fetchHealth()
	} catch (error) {
		health.value = null
		errorMessage.value = error instanceof Error ? error.message : '无法连接后端服务'
	} finally {
		loading.value = false
	}
}

const modules = [
	{ path: '/projects', title: '项目王国', en: 'Project Kingdom', desc: '项目列表、技术栈、完成度与 GitHub 地址' },
	{ path: '/technologies', title: '技术图鉴', en: 'Technology Library', desc: '按分类记录技术掌握程度与学习日期' },
	{ path: '/journey', title: '成长时间线', en: 'Royal Journey', desc: '按年份沉淀自己的成长节点' },
	{ path: '/code-library', title: '代码知识库', en: 'Code Library', desc: 'Java / SQL / JavaScript 片段管理' }
]

onMounted(loadHealth)
</script>

<template>
	<div class="dashboard">
		<el-card shadow="never" class="intro">
			<div class="intro-main">
				<div>
					<h2>Kingdom Studio</h2>
					<p>一个属于开发者自己的数字王国 —— 管理我的项目、技术资产、成长路线和代码知识库。</p>
				</div>
				<div class="intro-meta">
					<el-tag type="warning" effect="plain">Java 21</el-tag>
					<el-tag type="warning" effect="plain">Spring Boot 3</el-tag>
					<el-tag type="warning" effect="plain">MyBatis Plus</el-tag>
					<el-tag type="warning" effect="plain">MySQL 8</el-tag>
					<el-tag type="warning" effect="plain">Redis</el-tag>
					<el-tag type="warning" effect="plain">Vue 3</el-tag>
				</div>
			</div>
		</el-card>

		<el-card shadow="never" class="panel">
			<template #header>
				<div class="panel-header">
					<span>服务状态</span>
					<el-button :icon="Refresh" size="small" :loading="loading" @click="loadHealth">刷新</el-button>
				</div>
			</template>

			<el-alert
				v-if="errorMessage"
				type="error"
				show-icon
				:closable="false"
				title="服务状态获取失败"
				:description="errorMessage" />

			<el-descriptions v-else-if="health" :column="isNarrow ? 1 : 3" border>
				<el-descriptions-item label="服务状态">
					<el-tag :type="health.status === 'UP' ? 'success' : 'warning'">{{ health.status }}</el-tag>
				</el-descriptions-item>
				<el-descriptions-item label="应用">{{ health.application }}</el-descriptions-item>
				<el-descriptions-item label="版本">{{ health.version }} / Java {{ health.javaVersion }}</el-descriptions-item>

				<el-descriptions-item label="MySQL">
					<el-tag :type="health.database.status === 'UP' ? 'success' : 'danger'">
						{{ health.database.status }} · {{ health.database.latencyMs }}ms
					</el-tag>
				</el-descriptions-item>
				<el-descriptions-item label="Redis">
					<el-tag :type="health.redis.status === 'UP' ? 'success' : 'danger'">
						{{ health.redis.status }} · {{ health.redis.latencyMs }}ms
					</el-tag>
				</el-descriptions-item>
				<el-descriptions-item label="服务器时间">{{ health.serverTime }}</el-descriptions-item>
			</el-descriptions>

			<el-skeleton v-else :rows="2" animated />
		</el-card>

		<div class="module-grid">
			<el-card v-for="item in modules" :key="item.path" shadow="hover" class="module-card" @click="$router.push(item.path)">
				<div class="module-en">{{ item.en }}</div>
				<div class="module-title">{{ item.title }}</div>
				<div class="module-desc">{{ item.desc }}</div>
				<el-tag size="small" type="info" effect="plain">规划中</el-tag>
			</el-card>
		</div>

		<el-card shadow="never" class="panel">
			<template #header><span>工程进度</span></template>
			<el-timeline>
				<el-timeline-item type="success" timestamp="已完成" placement="top">Spring Boot 3 工程骨架、统一响应体、全局异常处理</el-timeline-item>
				<el-timeline-item type="success" timestamp="已完成" placement="top">MySQL 建库建表 + 种子数据（主站 5 张表 + 扩展 5 张表）</el-timeline-item>
				<el-timeline-item type="success" timestamp="已完成" placement="top">Redis 缓存配置、Swagger 文档、跨域配置</el-timeline-item>
				<el-timeline-item type="success" timestamp="已完成" placement="top">Vue3 + TS + Element Plus 前端骨架与接口联调</el-timeline-item>
				<el-timeline-item type="success" timestamp="已完成" placement="top">扩展模块：动效基因库、音乐 Agent</el-timeline-item>
				<el-timeline-item timestamp="规划中" placement="top">主站四个业务模块的后端接口与页面</el-timeline-item>
			</el-timeline>
		</el-card>
	</div>
</template>

<style scoped>
.dashboard {
	display: flex;
	flex-direction: column;
	gap: 16px;
}

.intro h2 {
	margin: 0 0 8px;
	font-size: 22px;
}

.intro p {
	margin: 0;
	color: #606266;
	line-height: 1.7;
}

.intro-meta {
	display: flex;
	flex-wrap: wrap;
	gap: 8px;
	margin-top: 14px;
}

.panel-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.module-grid {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
	gap: 16px;
}

.module-card {
	cursor: pointer;
}

.module-en {
	font-size: 12px;
	letter-spacing: 0.12em;
	text-transform: uppercase;
	color: var(--kingdom-gold);
}

.module-title {
	font-size: 17px;
	font-weight: 600;
	margin: 6px 0;
}

.module-desc {
	color: #606266;
	font-size: 13px;
	line-height: 1.6;
	margin-bottom: 12px;
}
</style>
