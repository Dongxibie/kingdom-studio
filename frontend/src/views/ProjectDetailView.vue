<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { fetchProject, type ProjectDetail } from '@/api/project'
import { renderMarkdown } from '@/utils/markdown'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref<ProjectDetail | null>(null)

/** 技术栈按「前端 / 后端 / 数据库 / AI / 工具」分组：一整片同色标签读起来比平铺更快 */
const TECH_GROUPS: { label: string; keys: string[] }[] = [
	{ label: '前端', keys: ['vue', 'react', 'next', 'typescript', 'javascript', 'tailwind', 'vite', 'element', 'echarts', 'css', 'html'] },
	{ label: '后端', keys: ['java', 'spring', 'mybatis', 'jwt', 'security', 'node', 'api'] },
	{ label: '数据库', keys: ['mysql', 'redis', 'supabase', 'sql', 'postgres'] },
	{ label: 'AI', keys: ['ai', 'deepseek', 'llm', 'rag', 'agent', 'prompt'] },
	{ label: '部署与工具', keys: ['vercel', 'docker', 'nginx', 'git', 'actions', 'vitest', 'maven', 'linux'] }
]

const groupedTech = computed(() => {
	const stack = detail.value?.technologyStack ?? []
	const used = new Set<string>()
	const groups = TECH_GROUPS.map(group => {
		const matched = stack.filter(item => {
			if (used.has(item)) {
				return false
			}
			const lower = item.toLowerCase()
			const hit = group.keys.some(key => lower.includes(key))
			if (hit) {
				used.add(item)
			}
			return hit
		})
		return { label: group.label, items: matched }
	}).filter(group => group.items.length)

	const rest = stack.filter(item => !used.has(item))
	if (rest.length) {
		groups.push({ label: '其他', items: rest })
	}
	return groups
})

const highlightsHtml = computed(() => renderMarkdown(detail.value?.highlights))

/** 项目成果：只有能如实统计出来的项才显示那一格 */
const outcomes = computed(() => {
	const value = detail.value
	if (!value) {
		return []
	}
	const rows: { label: string; value: string; hint?: string }[] = []
	if (value.codeLines) {
		rows.push({ label: '代码量', value: value.codeLines.toLocaleString('zh-CN') + ' 行', hint: '非空行' })
	}
	if (value.testCount) {
		rows.push({ label: '测试数量', value: value.testCount + ' 个', hint: '自动化测试' })
	}
	if (value.commitCount) {
		rows.push({ label: 'Git 提交', value: value.commitCount + ' 次' })
	}
	if (value.demoUrl) {
		rows.push({ label: 'Demo', value: '打开演示', hint: value.demoUrl })
	}
	rows.push({ label: '完成度', value: value.progress + '%' })
	return rows
})

const devTime = computed(() => {
	const value = detail.value
	if (!value) {
		return ''
	}
	const start = value.createTime?.slice(0, 10) ?? ''
	const end = value.updateTime?.slice(0, 10) ?? ''
	return start === end ? start : `${start} → ${end}`
})

async function load() {
	const id = Number(route.params.id)
	if (!id) {
		router.replace('/projects')
		return
	}
	loading.value = true
	try {
		detail.value = await fetchProject(id)
	} catch {
		detail.value = null
	} finally {
		loading.value = false
	}
}

onMounted(load)
</script>

<template>
	<div class="ks-page">
		<div class="ks-toolbar">
			<el-button :icon="ArrowLeft" text @click="router.push('/projects')">返回项目王国</el-button>
		</div>

		<el-skeleton v-if="loading" :rows="6" animated />

		<el-empty v-else-if="!detail" description="项目不存在或已被删除" />

		<template v-else>
			<div class="ks-head">
				<div>
					<div class="ks-eyebrow">Project Detail</div>
					<h2>{{ detail.name }}</h2>
					<p class="ks-sub">{{ detail.description || '还没有写简介。' }}</p>
				</div>
				<div class="ks-toolbar">
					<span class="ks-chip" :class="{ 'ks-chip--gold': detail.status === 'DEVELOPING' }">
						{{ detail.statusLabel }}
					</span>
					<a v-if="detail.githubUrl" class="detail-link" :href="detail.githubUrl" target="_blank" rel="noreferrer">仓库</a>
					<a v-if="detail.demoUrl" class="detail-link" :href="detail.demoUrl" target="_blank" rel="noreferrer">演示</a>
				</div>
			</div>

			<div class="detail-grid">
				<div class="detail-main">
					<div class="ks-panel">
						<div class="ks-panel-title">项目亮点</div>
						<div v-if="highlightsHtml" class="md-body" v-html="highlightsHtml" />
						<p v-else class="ks-muted">还没有写亮点，点「项目王国」列表里的编辑可以补上。</p>
					</div>

					<div class="ks-panel">
						<div class="ks-panel-title">技术架构</div>
						<div v-for="group in groupedTech" :key="group.label" class="tech-group">
							<div class="tech-group-label">{{ group.label }}</div>
							<div class="tech-group-items">
								<span v-for="tech in group.items" :key="tech" class="ks-chip">{{ tech }}</span>
							</div>
						</div>
						<p v-if="!groupedTech.length" class="ks-muted">未标注技术栈。</p>
					</div>
				</div>

				<aside class="detail-side">
					<div class="ks-panel">
						<div class="ks-panel-title">基本信息</div>
						<div class="info-row"><span>当前状态</span><b>{{ detail.statusLabel }}</b></div>
						<div class="info-row"><span>完成度</span><b>{{ detail.progress }}%</b></div>
						<div class="info-row"><span>开发时间</span><b>{{ devTime }}</b></div>
						<div class="info-row"><span>技术栈</span><b>{{ detail.technologyStack.length }} 项</b></div>
						<div class="info-row"><span>排序</span><b>{{ detail.sortOrder }}</b></div>
					</div>

					<div class="ks-panel">
						<div class="ks-panel-title">项目成果</div>
						<div class="outcome-grid">
							<div v-for="row in outcomes" :key="row.label" class="ks-stat">
								<div class="ks-stat-value">{{ row.value }}</div>
								<div class="ks-stat-label">{{ row.label }}</div>
								<div v-if="row.hint" class="ks-stat-hint">{{ row.hint }}</div>
							</div>
						</div>
					</div>
				</aside>
			</div>
		</template>
	</div>
</template>

<style scoped>
.detail-grid {
	display: grid;
	grid-template-columns: minmax(0, 2fr) minmax(260px, 1fr);
	gap: 16px;
	align-items: start;
}

.detail-main,
.detail-side {
	display: flex;
	flex-direction: column;
	gap: 16px;
}

.detail-link {
	color: var(--ks-gold-deep);
	font-size: 13px;
}

.detail-link:hover {
	text-decoration: underline;
}

.tech-group {
	display: flex;
	align-items: flex-start;
	gap: 12px;
	padding: 8px 0;
	border-bottom: 1px dashed var(--ks-line-soft);
}

.tech-group:last-child {
	border-bottom: none;
}

.tech-group-label {
	width: 84px;
	flex-shrink: 0;
	color: var(--ks-ink-2);
	font-size: 13px;
}

.tech-group-items {
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
}

.info-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 7px 0;
	border-bottom: 1px dashed var(--ks-line-soft);
	font-size: 13px;
	color: var(--ks-ink-2);
}

.info-row:last-child {
	border-bottom: none;
}

.info-row b {
	color: var(--ks-ink);
	font-weight: 600;
}

.outcome-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 10px;
}

@media (max-width: 900px) {
	.detail-grid {
		grid-template-columns: 1fr;
	}
}
</style>
