<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { fetchHealth, type HealthInfo } from '@/api/health'
import { fetchProjects } from '@/api/project'
import { fetchTechnologyAtlas } from '@/api/technology'
import { fetchTimeline, type TimelineNode } from '@/api/timeline'
import { fetchCodeSnippets } from '@/api/knowledge'

const router = useRouter()

const loading = ref(false)
const health = ref<HealthInfo | null>(null)
const errorMessage = ref('')

const counts = ref({ projects: 0, developing: 0, technologies: 0, timeline: 0, snippets: 0 })
const recentNodes = ref<TimelineNode[]>([])

const modules = computed(() => [
	{
		path: '/projects',
		en: 'Project Kingdom',
		title: '项目王国',
		desc: '项目列表、技术栈、完成度与仓库 / 演示地址',
		count: counts.value.projects,
		unit: '座建筑',
		hint: counts.value.developing ? counts.value.developing + ' 座在建' : '全部完工'
	},
	{
		path: '/technologies',
		en: 'Technology Library',
		title: '技术图鉴',
		desc: '按分类记录掌握程度、项目应用与学习时间',
		count: counts.value.technologies,
		unit: '项技术',
		hint: '六类技术资产'
	},
	{
		path: '/journey',
		en: 'Royal Journey',
		title: '成长时间线',
		desc: '年份是台阶，具体日期是关键节点',
		count: counts.value.timeline,
		unit: '个节点',
		hint: recentNodes.value[0] ? '最新：' + recentNodes.value[0].title : '暂无节点'
	},
	{
		path: '/code-library',
		en: 'Code Library',
		title: '代码知识库',
		desc: '可复用的解决方案：一段代码 + 使用场景',
		count: counts.value.snippets,
		unit: '段代码',
		hint: '按语言与标签检索'
	}
])

const extensions = [
	{
		path: '/extensions/motion-lab',
		en: 'Motion Lab',
		title: '动效工作台',
		desc: '官方 30 + 社区 30 模板、五轴意图推荐、30 套组合方案，最后一键出 Vue / React / HTML 代码'
	},
	{
		path: '/extensions/music-studio',
		en: 'AI Performance Studio',
		title: 'AI 演奏工作室',
		desc: 'MIDI / 简谱 → 键位映射 → 演奏计划 → 宏导出；难度分层、游戏乐器匹配与曲谱分享'
	}
]

async function load() {
	loading.value = true
	errorMessage.value = ''
	try {
		const [healthValue, projects, atlas, timeline, snippets] = await Promise.all([
			fetchHealth(),
			fetchProjects({ size: 1 }),
			fetchTechnologyAtlas(),
			fetchTimeline(),
			fetchCodeSnippets({ size: 1 })
		])
		health.value = healthValue
		counts.value = {
			projects: projects.total,
			developing: projects.records.filter(item => item.status === 'DEVELOPING').length,
			technologies: atlas.total,
			timeline: timeline.length,
			snippets: snippets.total
		}
		recentNodes.value = timeline.slice(0, 3)
	} catch (error) {
		health.value = null
		errorMessage.value = error instanceof Error ? error.message : '无法连接后端服务'
	} finally {
		loading.value = false
	}
}

onMounted(load)
</script>

<template>
	<div class="ks-page">
		<div class="ks-head">
			<div>
				<div class="ks-eyebrow">Kingdom Overview</div>
				<h2>我的数字王国</h2>
				<p class="ks-sub">
					这里放我的项目、技术资产、成长路线与代码知识库；两个扩展工具挂在下面，
					一个管界面动效，一个把曲子翻译成乐器按键。
				</p>
			</div>
			<div class="ks-toolbar">
				<el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
			</div>
		</div>

		<div class="ks-stat-grid">
			<div class="ks-stat">
				<div class="ks-stat-value">{{ counts.projects }}</div>
				<div class="ks-stat-label">项目</div>
				<div class="ks-stat-hint">其中 {{ counts.developing }} 个在持续开发</div>
			</div>
			<div class="ks-stat">
				<div class="ks-stat-value">{{ counts.technologies }}</div>
				<div class="ks-stat-label">技术记录</div>
				<div class="ks-stat-hint">覆盖 Java / Spring / 数据库 / AI / 前端 / 工程化</div>
			</div>
			<div class="ks-stat">
				<div class="ks-stat-value">{{ counts.timeline }}</div>
				<div class="ks-stat-label">成长节点</div>
				<div class="ks-stat-hint">最近 {{ recentNodes.length }} 个见下方</div>
			</div>
			<div class="ks-stat">
				<div class="ks-stat-value">{{ counts.snippets }}</div>
				<div class="ks-stat-label">代码片段</div>
				<div class="ks-stat-hint">可直接复用的解决方案</div>
			</div>
		</div>

		<el-alert
			v-if="errorMessage"
			type="error"
			show-icon
			:closable="false"
			title="部分数据获取失败"
			:description="errorMessage + '（后端未启动时，页面上的数字会是 0）'" />

		<div class="module-grid">
			<el-card v-for="item in modules" :key="item.path" shadow="hover" class="module-card" @click="router.push(item.path)">
				<div class="module-en">{{ item.en }}</div>
				<div class="module-title">{{ item.title }}</div>
				<div class="module-desc">{{ item.desc }}</div>
				<div class="module-foot">
					<span class="module-count">{{ item.count }}<small>{{ item.unit }}</small></span>
					<span class="ks-muted">{{ item.hint }}</span>
				</div>
			</el-card>
		</div>

		<div class="ks-panel">
			<div class="ks-panel-title">
				<span>扩展工具</span>
				<span class="ks-muted">与主站共用一套后端与数据库</span>
			</div>
			<div class="extension-grid">
				<div v-for="item in extensions" :key="item.path" class="extension-card" @click="router.push(item.path)">
					<div class="ks-eyebrow">{{ item.en }}</div>
					<div class="module-title">{{ item.title }}</div>
					<div class="module-desc">{{ item.desc }}</div>
				</div>
			</div>
		</div>

		<div class="bottom-grid">
			<div class="ks-panel">
				<div class="ks-panel-title">
					<span>最近的成长节点</span>
					<el-link type="primary" @click="router.push('/journey')">全部</el-link>
				</div>
				<el-timeline v-if="recentNodes.length">
					<el-timeline-item
						v-for="node in recentNodes"
						:key="node.id"
						:timestamp="node.timeText"
						placement="top"
						:type="node.eventDate ? 'primary' : 'info'">
						<b>{{ node.title }}</b>
						<div class="ks-muted">{{ node.description }}</div>
					</el-timeline-item>
				</el-timeline>
				<p v-else class="ks-muted">还没有成长节点。</p>
			</div>

			<div class="ks-panel">
				<div class="ks-panel-title">
					<span>服务状态</span>
					<el-tag v-if="health" :type="health.status === 'UP' ? 'success' : 'warning'" size="small">{{ health.status }}</el-tag>
				</div>
				<el-descriptions v-if="health" :column="1" size="small" border>
					<el-descriptions-item label="应用">{{ health.application }} · {{ health.version }}</el-descriptions-item>
					<el-descriptions-item label="Java">{{ health.javaVersion }}</el-descriptions-item>
					<el-descriptions-item label="MySQL">
						{{ health.database.status }} · {{ health.database.detail }} · {{ health.database.latencyMs }}ms
					</el-descriptions-item>
					<el-descriptions-item label="Redis">
						{{ health.redis.status }} · {{ health.redis.latencyMs }}ms
					</el-descriptions-item>
					<el-descriptions-item label="服务器时间">{{ health.serverTime }}</el-descriptions-item>
				</el-descriptions>
				<el-skeleton v-else :rows="3" animated />
			</div>
		</div>
	</div>
</template>

<style scoped>
.module-grid {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
	gap: 16px;
}

.module-card {
	cursor: pointer;
}

.module-en {
	font-size: 11px;
	letter-spacing: 0.16em;
	text-transform: uppercase;
	color: var(--ks-gold-deep);
}

.module-title {
	font-size: 17px;
	font-weight: 600;
	margin: 6px 0;
}

.module-desc {
	color: var(--ks-ink-2);
	font-size: 13px;
	line-height: 1.6;
	margin-bottom: 12px;
}

.module-foot {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
	gap: 8px;
	padding-top: 10px;
	border-top: 1px solid var(--ks-line-soft);
}

.module-count {
	font-size: 20px;
	font-weight: 700;
	color: var(--ks-ink);
}

.module-count small {
	margin-left: 4px;
	font-size: 12px;
	font-weight: 400;
	color: var(--ks-ink-3);
}

.extension-grid {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
	gap: 14px;
}

.extension-card {
	background: var(--ks-surface-2);
	border: 1px solid var(--ks-line);
	border-radius: 12px;
	padding: 14px 16px;
	cursor: pointer;
	transition: border-color 0.18s ease, transform 0.18s ease;
}

.extension-card:hover {
	border-color: var(--ks-gold-soft);
	transform: translateY(-1px);
}

.bottom-grid {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
	gap: 16px;
	align-items: start;
}
</style>
