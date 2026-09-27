<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
	createTechnology,
	deleteTechnology,
	fetchTechnologyAtlas,
	updateTechnology,
	type TechnologyAtlas,
	type TechnologyCategory,
	type TechnologyItem,
	type TechnologySavePayload
} from '@/api/technology'

const loading = ref(false)
const saving = ref(false)
const atlas = ref<TechnologyAtlas | null>(null)
const activeCategory = ref('ALL')
const keyword = ref('')

const CATEGORY_OPTIONS: { value: TechnologyCategory; label: string }[] = [
	{ value: 'Java', label: 'Java' },
	{ value: 'Spring', label: 'Spring' },
	{ value: 'Database', label: '数据库' },
	{ value: 'AI', label: 'AI' },
	{ value: 'Frontend', label: '前端' },
	{ value: 'DevOps', label: '工程化' }
]

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<TechnologySavePayload & { learnDateText: string }>({
	name: '',
	category: 'Java',
	level: 3,
	description: '',
	usedProjects: '',
	learnDate: null,
	learnDateText: ''
})

const dialogTitle = computed(() => (editingId.value ? '编辑技术' : '记录一项技术'))

async function load() {
	loading.value = true
	try {
		atlas.value = await fetchTechnologyAtlas({ category: activeCategory.value, keyword: keyword.value })
	} catch {
		// 请求层已提示
	} finally {
		loading.value = false
	}
}

function pick(category: string) {
	if (activeCategory.value === category) {
		return
	}
	activeCategory.value = category
	load()
}

function openCreate() {
	editingId.value = null
	Object.assign(form, {
		name: '',
		category: 'Java',
		level: 3,
		description: '',
		usedProjects: '',
		learnDate: null,
		learnDateText: ''
	})
	dialogVisible.value = true
}

function openEdit(item: TechnologyItem) {
	editingId.value = item.id
	Object.assign(form, {
		name: item.name,
		category: item.category,
		level: item.level,
		description: item.description,
		usedProjects: item.usedProjects.join(','),
		learnDate: item.learnDate,
		learnDateText: item.learnDate ?? ''
	})
	dialogVisible.value = true
}

async function submit() {
	if (!form.name.trim()) {
		ElMessage.warning('技术名称不能为空')
		return
	}
	saving.value = true
	try {
		const payload: TechnologySavePayload = {
			name: form.name,
			category: form.category,
			level: form.level,
			description: form.description,
			usedProjects: form.usedProjects,
			learnDate: form.learnDateText || null
		}
		if (editingId.value) {
			await updateTechnology(editingId.value, payload)
			ElMessage.success('技术已更新')
		} else {
			await createTechnology(payload)
			ElMessage.success('技术已记录')
		}
		dialogVisible.value = false
		await load()
	} catch {
		// 重名等业务错误由请求层提示
	} finally {
		saving.value = false
	}
}

async function remove(item: TechnologyItem) {
	try {
		await ElMessageBox.confirm(`确定删除「${item.name}」吗？`, '删除技术', {
			confirmButtonText: '删除',
			cancelButtonText: '取消',
			type: 'warning'
		})
	} catch {
		return
	}
	try {
		await deleteTechnology(item.id)
		ElMessage.success('技术已删除')
		await load()
	} catch {
		// 同上
	}
}

onMounted(load)
</script>

<template>
	<div class="ks-page">
		<div class="ks-head">
			<div>
				<div class="ks-eyebrow">Technology Library</div>
				<h2>技术图鉴</h2>
				<p class="ks-sub">
					不是博客，是自己的一本技术百科：每项技术练到什么程度、用在了哪些项目上、什么时候开始学的。
					当前收录 {{ atlas?.categories[0]?.count ?? 0 }} 项。
				</p>
			</div>
			<div class="ks-toolbar">
				<el-button type="primary" :icon="Plus" @click="openCreate">记录技术</el-button>
			</div>
		</div>

		<div class="atlas">
			<aside class="atlas-rail">
				<button
					v-for="item in atlas?.categories ?? []"
					:key="item.category"
					type="button"
					class="rail-item"
					:class="{ on: activeCategory === item.category }"
					@click="pick(item.category)">
					<span class="rail-label">{{ item.label }}</span>
					<span class="rail-count">{{ item.count }}</span>
				</button>
			</aside>

			<div class="atlas-main">
				<div class="ks-toolbar">
					<el-input
						v-model="keyword"
						placeholder="搜索技术名、说明或项目"
						clearable
						class="atlas-search"
						@keyup.enter="load"
						@clear="load">
						<template #append>
							<el-button :icon="Search" @click="load" />
						</template>
					</el-input>
				</div>

				<el-skeleton v-if="loading && !atlas" :rows="4" animated />

				<el-empty v-else-if="!atlas?.items.length" description="这个分类下还没有记录" />

				<div v-else class="tech-grid">
					<article v-for="item in atlas.items" :key="item.id" class="tech-card">
						<header class="tech-head">
							<div>
								<div class="tech-name">{{ item.name }}</div>
								<div class="tech-stars" :title="`掌握程度 ${item.level} / 5`">{{ item.levelText }}</div>
							</div>
							<span class="ks-chip ks-chip--gold">{{ item.categoryLabel }}</span>
						</header>

						<p class="tech-desc">{{ item.description || '还没有写说明。' }}</p>

						<div v-if="item.usedProjects.length" class="tech-projects">
							<span class="ks-muted">项目应用</span>
							<span v-for="project in item.usedProjects" :key="project" class="ks-chip">{{ project }}</span>
						</div>

						<footer class="tech-foot">
							<span class="ks-muted">{{ item.learnDate ? item.learnDate + ' 开始' : '未记录学习时间' }}</span>
							<span class="tech-actions">
								<el-button size="small" text @click="openEdit(item)">编辑</el-button>
								<el-button size="small" text type="danger" @click="remove(item)">删除</el-button>
							</span>
						</footer>
					</article>
				</div>
			</div>
		</div>

		<el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" top="8vh">
			<el-form label-position="top">
				<el-form-item label="技术名称">
					<el-input v-model="form.name" maxlength="50" placeholder="例如：Spring Boot 3" />
				</el-form-item>
				<div class="form-row">
					<el-form-item label="分类">
						<el-select v-model="form.category" style="width: 100%">
							<el-option v-for="item in CATEGORY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
						</el-select>
					</el-form-item>
					<el-form-item label="掌握程度">
						<el-rate v-model="form.level" :max="5" />
					</el-form-item>
				</div>
				<el-form-item label="说明 / 学习心得">
					<el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
				</el-form-item>
				<el-form-item label="项目应用（英文逗号分隔）">
					<el-input v-model="form.usedProjects" placeholder="Kingdom Studio,九八 · 校园快递代取订单管理系统" />
				</el-form-item>
				<el-form-item label="开始学习日期">
					<el-date-picker v-model="form.learnDateText" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
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
.atlas {
	display: grid;
	grid-template-columns: 168px minmax(0, 1fr);
	gap: 16px;
	align-items: start;
}

.atlas-rail {
	display: flex;
	flex-direction: column;
	gap: 4px;
	background: var(--ks-surface);
	border: 1px solid var(--ks-line);
	border-radius: 14px;
	padding: 8px;
	position: sticky;
	top: 0;
}

.rail-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	border: none;
	background: transparent;
	border-radius: 9px;
	padding: 9px 12px;
	font-size: 13.5px;
	color: var(--ks-ink-2);
	cursor: pointer;
	text-align: left;
}

.rail-item:hover {
	background: var(--ks-surface-2);
	color: var(--ks-ink);
}

.rail-item.on {
	background: #fdf8ec;
	color: var(--ks-gold-deep);
	font-weight: 600;
}

.rail-count {
	font-size: 12px;
	color: var(--ks-ink-3);
}

.atlas-main {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.atlas-search {
	max-width: 360px;
}

.tech-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
	gap: 14px;
}

.tech-card {
	display: flex;
	flex-direction: column;
	gap: 8px;
	background: var(--ks-surface);
	border: 1px solid var(--ks-line);
	border-radius: 14px;
	padding: 15px;
}

.tech-head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 10px;
}

.tech-name {
	font-size: 15px;
	font-weight: 600;
}

.tech-stars {
	color: var(--ks-gold);
	font-size: 13px;
	letter-spacing: 1px;
	margin-top: 2px;
}

.tech-desc {
	margin: 0;
	color: var(--ks-ink-2);
	font-size: 13px;
	line-height: 1.7;
}

.tech-projects {
	display: flex;
	flex-wrap: wrap;
	align-items: center;
	gap: 6px;
}

.tech-foot {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-top: auto;
	padding-top: 6px;
	border-top: 1px solid var(--ks-line-soft);
}

.tech-actions {
	display: flex;
	gap: 2px;
}

.form-row {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 12px;
}

@media (max-width: 820px) {
	.atlas {
		grid-template-columns: 1fr;
	}

	.atlas-rail {
		flex-direction: row;
		flex-wrap: wrap;
		position: static;
	}
}
</style>
