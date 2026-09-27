<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
	createTimelineNode,
	deleteTimelineNode,
	fetchTimeline,
	updateTimelineNode,
	type TimelineNode,
	type TimelineSavePayload
} from '@/api/timeline'

const loading = ref(false)
const saving = ref(false)
const nodes = ref<TimelineNode[]>([])

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<TimelineSavePayload & { eventDateText: string }>({
	year: new Date().getFullYear(),
	title: '',
	description: '',
	relatedProject: '',
	level: 3,
	sortOrder: 0,
	eventDateText: ''
})

const dialogTitle = computed(() => (editingId.value ? '编辑节点' : '记录一个节点'))

/** 按年份分组：一年里可能既有年度节点也有具体日期的事件 */
const groups = computed(() => {
	const map = new Map<number, TimelineNode[]>()
	for (const node of nodes.value) {
		const list = map.get(node.year) ?? []
		list.push(node)
		map.set(node.year, list)
	}
	return [...map.entries()].map(([year, items]) => ({ year, items }))
})

const levelText = computed(() => (level: number) => '●'.repeat(level) + '○'.repeat(Math.max(0, 5 - level)))

async function load() {
	loading.value = true
	try {
		nodes.value = await fetchTimeline()
	} catch {
		// 请求层已提示
	} finally {
		loading.value = false
	}
}

function openCreate() {
	editingId.value = null
	Object.assign(form, {
		year: new Date().getFullYear(),
		title: '',
		description: '',
		relatedProject: '',
		level: 3,
		sortOrder: 0,
		eventDateText: ''
	})
	dialogVisible.value = true
}

function openEdit(node: TimelineNode) {
	editingId.value = node.id
	Object.assign(form, {
		year: node.year,
		title: node.title,
		description: node.description,
		relatedProject: node.relatedProject,
		level: node.level,
		sortOrder: node.sortOrder,
		eventDateText: node.eventDate ?? ''
	})
	dialogVisible.value = true
}

async function submit() {
	if (!form.title.trim()) {
		ElMessage.warning('节点标题不能为空')
		return
	}
	saving.value = true
	try {
		const payload: TimelineSavePayload = {
			year: form.year,
			eventDate: form.eventDateText || null,
			title: form.title,
			description: form.description,
			relatedProject: form.relatedProject,
			level: form.level,
			sortOrder: form.sortOrder
		}
		if (editingId.value) {
			await updateTimelineNode(editingId.value, payload)
			ElMessage.success('节点已更新')
		} else {
			await createTimelineNode(payload)
			ElMessage.success('节点已记录')
		}
		dialogVisible.value = false
		await load()
	} catch {
		// 同上
	} finally {
		saving.value = false
	}
}

async function remove(node: TimelineNode) {
	try {
		await ElMessageBox.confirm(`确定删除「${node.title}」吗？`, '删除节点', {
			confirmButtonText: '删除',
			cancelButtonText: '取消',
			type: 'warning'
		})
	} catch {
		return
	}
	try {
		await deleteTimelineNode(node.id)
		ElMessage.success('节点已删除')
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
				<div class="ks-eyebrow">Royal Journey</div>
				<h2>成长时间线</h2>
				<p class="ks-sub">
					从第一行代码到现在的路线图：年份是台阶，具体日期是关键节点。
					目前 {{ nodes.length }} 个节点，最新一个是「{{ nodes[0]?.title ?? '—' }}」。
				</p>
			</div>
			<div class="ks-toolbar">
				<el-button type="primary" :icon="Plus" @click="openCreate">记录节点</el-button>
			</div>
		</div>

		<el-skeleton v-if="loading && !nodes.length" :rows="5" animated />

		<el-empty v-else-if="!nodes.length" description="还没有成长节点，先记下第一条" />

		<div v-else class="journey">
			<section v-for="group in groups" :key="group.year" class="journey-year">
				<div class="year-label">
					<span class="year-number">{{ group.year }}</span>
					<span class="year-line" />
				</div>
				<div class="year-nodes">
					<article v-for="node in group.items" :key="node.id" class="node">
						<div class="node-marker" :class="{ 'node-marker--dated': !!node.eventDate }">
							<span class="node-time">{{ node.timeText }}</span>
						</div>
						<div class="node-body">
							<header class="node-head">
								<span class="node-title">{{ node.title }}</span>
								<span class="node-level" :title="`阶段等级 ${node.level} / 5`">{{ levelText(node.level) }}</span>
							</header>
							<p class="node-desc">{{ node.description }}</p>
							<div class="node-foot">
								<span v-if="node.relatedProject" class="ks-chip ks-chip--gold">{{ node.relatedProject }}</span>
								<span class="node-actions">
									<el-button size="small" text @click="openEdit(node)">编辑</el-button>
									<el-button size="small" text type="danger" @click="remove(node)">删除</el-button>
								</span>
							</div>
						</div>
					</article>
				</div>
			</section>
		</div>

		<el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" top="8vh">
			<el-form label-position="top">
				<div class="form-row">
					<el-form-item label="年份">
						<el-input-number v-model="form.year" :min="2000" :max="2100" style="width: 100%" />
					</el-form-item>
					<el-form-item label="具体日期（可留空）">
						<el-date-picker
							v-model="form.eventDateText"
							type="date"
							value-format="YYYY-MM-DD"
							placeholder="年度节点可留空"
							style="width: 100%" />
					</el-form-item>
				</div>
				<el-form-item label="节点标题">
					<el-input v-model="form.title" maxlength="100" placeholder="例如：AI 演奏工作台 v1.2" />
				</el-form-item>
				<el-form-item label="节点描述">
					<el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
				</el-form-item>
				<div class="form-row">
					<el-form-item label="关联项目">
						<el-input v-model="form.relatedProject" placeholder="Kingdom Studio" />
					</el-form-item>
					<el-form-item label="阶段等级">
						<el-rate v-model="form.level" :max="5" />
					</el-form-item>
				</div>
				<el-form-item label="同年排序（越大越靠前）">
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
.journey {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.journey-year {
	display: grid;
	grid-template-columns: 120px minmax(0, 1fr);
	gap: 16px;
}

.year-label {
	position: relative;
	display: flex;
	align-items: flex-start;
	gap: 10px;
	padding-top: 6px;
}

.year-number {
	font-size: 20px;
	font-weight: 700;
	color: var(--ks-ink);
	letter-spacing: 0.5px;
}

.year-line {
	flex: 1;
	height: 1px;
	margin-top: 15px;
	background: linear-gradient(90deg, var(--ks-gold-soft), transparent);
}

.year-nodes {
	display: flex;
	flex-direction: column;
	gap: 10px;
	padding-bottom: 14px;
	border-left: 1px solid var(--ks-line);
	padding-left: 16px;
}

.node {
	display: grid;
	grid-template-columns: 108px minmax(0, 1fr);
	gap: 12px;
	position: relative;
}

.node-marker::before {
	content: '';
	position: absolute;
	left: -21px;
	top: 14px;
	width: 8px;
	height: 8px;
	border-radius: 50%;
	background: var(--ks-surface);
	border: 2px solid var(--ks-line);
}

.node-marker--dated::before {
	border-color: var(--ks-gold);
	background: var(--ks-gold);
}

.node-time {
	font-size: 12.5px;
	color: var(--ks-ink-3);
	line-height: 1.6;
}

.node-body {
	background: var(--ks-surface);
	border: 1px solid var(--ks-line);
	border-radius: 12px;
	padding: 13px 15px;
}

.node-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 10px;
}

.node-title {
	font-size: 15px;
	font-weight: 600;
}

.node-level {
	color: var(--ks-gold);
	font-size: 12px;
	letter-spacing: 2px;
}

.node-desc {
	margin: 6px 0 0;
	color: var(--ks-ink-2);
	font-size: 13px;
	line-height: 1.7;
}

.node-foot {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
	margin-top: 8px;
}

.node-actions {
	display: flex;
	gap: 2px;
}

.form-row {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 12px;
}

@media (max-width: 820px) {
	.journey-year {
		grid-template-columns: 1fr;
	}

	.node {
		grid-template-columns: 1fr;
	}

	.node-marker::before {
		display: none;
	}
}
</style>
