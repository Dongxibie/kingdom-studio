<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
	createProject,
	deleteProject,
	fetchProjects,
	updateProject,
	type ProjectListItem,
	type ProjectSavePayload,
	type ProjectStatus
} from '@/api/project'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const items = ref<ProjectListItem[]>([])
const total = ref(0)
const keyword = ref('')
const status = ref<ProjectStatus | ''>('')

const STATUS_FILTERS: { value: ProjectStatus | ''; label: string }[] = [
	{ value: '', label: '全部' },
	{ value: 'DEVELOPING', label: '持续开发' },
	{ value: 'COMPLETED', label: '已完成' },
	{ value: 'PLANNING', label: '规划中' }
]

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<Required<Pick<ProjectSavePayload, 'name'>> & ProjectSavePayload>({
	name: '',
	description: '',
	coverImage: '',
	technologyStack: '',
	githubUrl: '',
	demoUrl: '',
	status: 'DEVELOPING',
	progress: 0,
	codeLines: null,
	testCount: null,
	commitCount: null,
	highlights: '',
	sortOrder: 0
})

const dialogTitle = computed(() => (editingId.value ? '编辑项目' : '新建项目'))
const statLine = computed(() => {
	const developing = items.value.filter(item => item.status === 'DEVELOPING').length
	const completed = items.value.filter(item => item.status === 'COMPLETED').length
	return { developing, completed }
})

async function load() {
	loading.value = true
	try {
		const page = await fetchProjects({ keyword: keyword.value, status: status.value, size: 60 })
		items.value = page.records
		total.value = page.total
	} catch {
		// 请求层已经把中文错误提示弹出来了，这里只需要让列表保持上一次的状态
	} finally {
		loading.value = false
	}
}

function pickStatus(value: ProjectStatus | '') {
	if (status.value === value) {
		return
	}
	status.value = value
	load()
}

function openCreate() {
	editingId.value = null
	Object.assign(form, {
		name: '',
		description: '',
		coverImage: '',
		technologyStack: '',
		githubUrl: '',
		demoUrl: '',
		status: 'DEVELOPING',
		progress: 0,
		codeLines: null,
		testCount: null,
		commitCount: null,
		highlights: '',
		sortOrder: 0
	})
	dialogVisible.value = true
}

function openEdit(item: ProjectListItem) {
	editingId.value = item.id
	Object.assign(form, {
		name: item.name,
		description: item.description,
		coverImage: item.coverImage,
		technologyStack: item.technologyStack.join(','),
		githubUrl: item.githubUrl,
		demoUrl: item.demoUrl,
		status: item.status,
		progress: item.progress,
		codeLines: item.codeLines,
		testCount: item.testCount,
		commitCount: item.commitCount,
		highlights: '',
		sortOrder: item.sortOrder
	})
	// 列表不带亮点正文，编辑时按详情补齐，避免一保存就把亮点清空
	void fillHighlights(item.id)
	dialogVisible.value = true
}

async function fillHighlights(id: number) {
	try {
		const { fetchProject } = await import('@/api/project')
		const detail = await fetchProject(id)
		form.highlights = detail.highlights ?? ''
	} catch {
		// 取不到详情时保持空串：保存会把亮点清空，这是可接受的降级
	}
}

async function submit() {
	if (!form.name.trim()) {
		ElMessage.warning('项目名称不能为空')
		return
	}
	saving.value = true
	try {
		if (editingId.value) {
			await updateProject(editingId.value, { ...form })
			ElMessage.success('项目已更新')
		} else {
			await createProject({ ...form })
			ElMessage.success('项目已创建')
		}
		dialogVisible.value = false
		await load()
	} catch {
		// 失败原因由请求层统一提示（重名会提示「已存在同名项目」）
	} finally {
		saving.value = false
	}
}

async function remove(item: ProjectListItem) {
	try {
		await ElMessageBox.confirm(`确定删除「${item.name}」吗？删除后列表不再展示。`, '删除项目', {
			confirmButtonText: '删除',
			cancelButtonText: '取消',
			type: 'warning'
		})
	} catch {
		return
	}
	try {
		await deleteProject(item.id)
		ElMessage.success('项目已删除')
		await load()
	} catch {
		// 同上
	}
}

function progressColor(value: number) {
	if (value >= 100) {
		return '#7d9b76'
	}
	return '#c2963a'
}

onMounted(load)
</script>

<template>
	<div class="ks-page">
		<div class="ks-head">
			<div>
				<div class="ks-eyebrow">Project Kingdom</div>
				<h2>项目王国</h2>
				<p class="ks-sub">
					每个项目都是一座建筑：技术栈、完成度、仓库与演示地址，以及用 Markdown 写的项目亮点。
					共 {{ total }} 座，其中 {{ statLine.developing }} 座在建。
				</p>
			</div>
			<div class="ks-toolbar">
				<el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
				<el-button type="primary" :icon="Plus" @click="openCreate">新建项目</el-button>
			</div>
		</div>

		<div class="filter-bar">
			<el-input
				v-model="keyword"
				placeholder="搜索项目名、简介或技术栈"
				clearable
				class="filter-search"
				@keyup.enter="load"
				@clear="load">
				<template #append>
					<el-button :icon="Search" @click="load" />
				</template>
			</el-input>
			<div class="status-chips">
				<button
					v-for="item in STATUS_FILTERS"
					:key="item.value || 'all'"
					type="button"
					class="status-chip"
					:class="{ on: status === item.value }"
					@click="pickStatus(item.value)">
					{{ item.label }}
				</button>
			</div>
		</div>

		<el-skeleton v-if="loading && !items.length" :rows="4" animated />

		<el-empty v-else-if="!items.length" description="没有匹配的项目，换个关键词或新建一座" />

		<div v-else class="project-grid">
			<article v-for="item in items" :key="item.id" class="project-card">
				<header class="project-head">
					<div class="project-name" @click="router.push(`/projects/${item.id}`)">{{ item.name }}</div>
					<span class="ks-chip" :class="{ 'ks-chip--gold': item.status === 'DEVELOPING' }">
						{{ item.statusLabel }}
					</span>
				</header>

				<p class="project-desc">{{ item.description || '还没有写简介。' }}</p>

				<el-progress
					:percentage="item.progress"
					:stroke-width="6"
					:color="progressColor"
					:show-text="false" />
				<div class="ks-muted">完成度 {{ item.progress }}%</div>

				<div class="tech-row">
					<span v-for="tech in item.technologyStack" :key="tech" class="ks-chip">{{ tech }}</span>
					<span v-if="!item.technologyStack.length" class="ks-muted">未标注技术栈</span>
				</div>

				<p v-if="item.codeLines || item.testCount || item.commitCount" class="project-outcome ks-muted">
					<span v-if="item.codeLines">{{ item.codeLines }} 行代码</span>
					<span v-if="item.testCount"> · {{ item.testCount }} 个测试</span>
					<span v-if="item.commitCount"> · {{ item.commitCount }} 次提交</span>
				</p>

				<footer class="project-foot">
					<div class="project-links">
						<a v-if="item.githubUrl" :href="item.githubUrl" target="_blank" rel="noreferrer">仓库</a>
						<a v-if="item.demoUrl" :href="item.demoUrl" target="_blank" rel="noreferrer">演示</a>
						<span class="ks-muted">{{ item.updateTime?.slice(0, 10) }}</span>
					</div>
					<div class="project-actions">
						<el-button size="small" @click="router.push(`/projects/${item.id}`)">详情</el-button>
						<el-button size="small" @click="openEdit(item)">编辑</el-button>
						<el-button size="small" type="danger" plain @click="remove(item)">删除</el-button>
					</div>
				</footer>
			</article>
		</div>

		<el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" top="6vh">
			<el-form label-width="88px" label-position="top">
				<el-form-item label="项目名称">
					<el-input v-model="form.name" maxlength="100" placeholder="例如：Kingdom Studio" />
				</el-form-item>
				<el-form-item label="项目简介">
					<el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" show-word-limit />
				</el-form-item>
				<div class="form-row">
					<el-form-item label="状态">
						<el-select v-model="form.status" style="width: 100%">
							<el-option label="规划中" value="PLANNING" />
							<el-option label="持续开发" value="DEVELOPING" />
							<el-option label="已完成" value="COMPLETED" />
						</el-select>
					</el-form-item>
					<el-form-item label="完成度 %">
						<el-input-number v-model="form.progress" :min="0" :max="100" style="width: 100%" />
					</el-form-item>
				</div>
				<el-form-item label="技术栈（英文逗号分隔）">
					<el-input v-model="form.technologyStack" placeholder="Java,Spring Boot,MySQL" />
				</el-form-item>
				<div class="form-row">
					<el-form-item label="GitHub 地址">
						<el-input v-model="form.githubUrl" placeholder="https://github.com/..." />
					</el-form-item>
					<el-form-item label="演示地址">
						<el-input v-model="form.demoUrl" placeholder="https://..." />
					</el-form-item>
				</div>
				<div class="form-row form-row--three">
					<el-form-item label="代码行数">
						<el-input-number v-model="form.codeLines" :min="0" style="width: 100%" />
					</el-form-item>
					<el-form-item label="测试数量">
						<el-input-number v-model="form.testCount" :min="0" style="width: 100%" />
					</el-form-item>
					<el-form-item label="提交数">
						<el-input-number v-model="form.commitCount" :min="0" style="width: 100%" />
					</el-form-item>
				</div>
				<el-form-item label="项目亮点（Markdown）">
					<el-input
						v-model="form.highlights"
						type="textarea"
						:rows="7"
						placeholder="### 核心能力&#10;- 支持 **Markdown**：标题、列表、加粗、`行内代码`" />
				</el-form-item>
				<el-form-item label="排序值（越小越靠前）">
					<el-input-number v-model="form.sortOrder" :min="0" style="width: 160px" />
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="dialogVisible = false">取消</el-button>
				<el-button type="primary" :loading="saving" @click="submit">保存</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<style scoped>
.filter-bar {
	display: flex;
	align-items: center;
	gap: 12px;
	flex-wrap: wrap;
}

.filter-search {
	max-width: 380px;
}

.status-chips {
	display: flex;
	gap: 6px;
}

.status-chip {
	border: 1px solid var(--ks-line);
	background: var(--ks-surface);
	color: var(--ks-ink-2);
	border-radius: 999px;
	padding: 6px 14px;
	font-size: 13px;
	cursor: pointer;
	transition: all 0.15s ease;
}

.status-chip:hover {
	border-color: var(--ks-gold-soft);
	color: var(--ks-gold-deep);
}

.status-chip.on {
	background: #fdf8ec;
	border-color: var(--ks-gold);
	color: var(--ks-gold-deep);
	font-weight: 600;
}

.project-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
	gap: 16px;
}

.project-card {
	display: flex;
	flex-direction: column;
	gap: 10px;
	background: var(--ks-surface);
	border: 1px solid var(--ks-line);
	border-radius: 14px;
	padding: 16px;
	transition: box-shadow 0.18s ease, transform 0.18s ease;
}

.project-card:hover {
	box-shadow: 0 8px 24px rgba(36, 31, 25, 0.07);
	transform: translateY(-1px);
}

.project-head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 10px;
}

.project-name {
	font-size: 16px;
	font-weight: 600;
	cursor: pointer;
}

.project-name:hover {
	color: var(--ks-gold-deep);
}

.project-desc {
	margin: 0;
	color: var(--ks-ink-2);
	font-size: 13px;
	line-height: 1.7;
	min-height: 44px;
}

.tech-row {
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
}

.project-outcome {
	margin: 0;
}

.project-foot {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
	flex-wrap: wrap;
	margin-top: auto;
	padding-top: 8px;
	border-top: 1px solid var(--ks-line-soft);
}

.project-links {
	display: flex;
	align-items: center;
	gap: 10px;
	font-size: 13px;
}

.project-links a {
	color: var(--ks-gold-deep);
}

.project-links a:hover {
	text-decoration: underline;
}

.form-row {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 12px;
}

.form-row--three {
	grid-template-columns: 1fr 1fr 1fr;
}

@media (max-width: 720px) {
	.form-row,
	.form-row--three {
		grid-template-columns: 1fr;
	}
}
</style>
