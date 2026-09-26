<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ExtEmpty, ExtShell, ExtStatusTag } from '@/extensions/_shared/components'
import type { ExtModuleInfo } from '@/extensions/_shared/types/common'
import MotionCategoryTree from '@/extensions/motion-lab/components/MotionCategoryTree.vue'
import MotionResourceList from '@/extensions/motion-lab/components/MotionResourceList.vue'
import MotionPreviewPane from '@/extensions/motion-lab/components/MotionPreviewPane.vue'
import MotionCodePanel from '@/extensions/motion-lab/components/MotionCodePanel.vue'
import MotionEditorDialog from '@/extensions/motion-lab/components/MotionEditorDialog.vue'
import MotionCrawlDialog from '@/extensions/motion-lab/components/MotionCrawlDialog.vue'
import { useRouter } from 'vue-router'
import {
	createMotion,
	deleteMotion,
	fetchMotionModuleInfo,
	getMotion,
	listMotions,
	saveMotionCode,
	updateMotion,
} from '@/extensions/motion-lab/api/motion'
import type { MotionCategory, MotionCodeSet, MotionDetail, MotionListItem, MotionSavePayload } from '@/extensions/motion-lab/types/motion'

const router = useRouter()

/** 资源页是从工作台分出来的：给一个回得去的入口 */
function backToWorkbench() {
	void router.push('/extensions/motion-lab')
}

const info = ref<ExtModuleInfo | null>(null)
const errorMessage = ref('')

const category = ref<MotionCategory | 'ALL'>('ALL')
const keyword = ref('')
const items = ref<MotionListItem[]>([])
const total = ref(0)
const page = ref(1)
const pages = ref(1)
const pageSize = 10
const listLoading = ref(false)

const activeId = ref<number | null>(null)
const detail = ref<MotionDetail | null>(null)
const detailLoading = ref(false)
const savingCode = ref(false)
const savingForm = ref(false)
const dialogVisible = ref(false)
const editingExists = ref(false)
const crawlVisible = ref(false)

/** 采集真正写库之后刷新列表：回到第一页，让新采的资源能被看到 */
async function onCrawled() {
	page.value = 1
	await loadList()
}

/** 头部状态标签 */
const statusTag = computed(() => {
	if (errorMessage.value) {
		return { text: '后端未连通', tone: 'red' as const }
	}
	if (!info.value) {
		return { text: '自检中', tone: 'mute' as const }
	}
	return { text: '模块已连通 · ' + info.value.phase, tone: 'ok' as const }
})

async function loadInfo() {
	errorMessage.value = ''
	try {
		info.value = await fetchMotionModuleInfo()
	} catch (error) {
		info.value = null
		errorMessage.value = error instanceof Error ? error.message : '模块自检失败'
	}
}

async function loadList() {
	listLoading.value = true
	try {
		const result = await listMotions({
			page: page.value,
			size: pageSize,
			category: category.value,
			keyword: keyword.value.trim(),
		})
		items.value = result.records
		total.value = result.total
		pages.value = Math.max(1, result.pages)
		// 列表刷新后如果当前选中项不在这一页，自动选中第一条，避免右侧空着
		if (result.records.length && !result.records.some((item) => item.id === activeId.value)) {
			await selectMotion(result.records[0].id)
		}
		if (!result.records.length) {
			detail.value = null
			activeId.value = null
		}
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '加载列表失败')
	} finally {
		listLoading.value = false
	}
}

async function selectMotion(id: number) {
	activeId.value = id
	detailLoading.value = true
	try {
		detail.value = await getMotion(id)
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '加载详情失败')
	} finally {
		detailLoading.value = false
	}
}

function openCreate() {
	editingExists.value = false
	dialogVisible.value = true
}

function openEdit(id: number) {
	editingExists.value = true
	if (activeId.value !== id) {
		void selectMotion(id)
	}
	dialogVisible.value = true
}

async function submitForm(payload: MotionSavePayload) {
	savingForm.value = true
	try {
		if (editingExists.value && detail.value) {
			await updateMotion(detail.value.id, payload)
			ElMessage.success('已保存修改')
			await selectMotion(detail.value.id)
		} else {
			const id = await createMotion(payload)
			ElMessage.success('已新增')
			dialogVisible.value = false
			page.value = 1
			await loadList()
			await selectMotion(Number(id))
		}
		dialogVisible.value = false
		await loadList()
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '保存失败')
	} finally {
		savingForm.value = false
	}
}

async function removeMotion(id: number) {
	try {
		await ElMessageBox.confirm('删除后可以重新采集，但当前记录会标记为已删除，确定继续？', '删除动效', {
			type: 'warning',
			confirmButtonText: '删除',
			cancelButtonText: '取消',
		})
	} catch {
		return
	}
	try {
		await deleteMotion(id)
		ElMessage.success('已删除')
		if (activeId.value === id) {
			activeId.value = null
			detail.value = null
		}
		await loadList()
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '删除失败')
	}
}

async function saveCode(code: MotionCodeSet) {
	if (!detail.value) {
		return
	}
	savingCode.value = true
	try {
		await saveMotionCode(detail.value.id, code)
		ElMessage.success('代码已保存')
		detail.value = { ...detail.value, code: { ...code } }
		// 列表里的「有代码」标记要跟着变
		const target = items.value.find((item) => item.id === detail.value?.id)
		if (target) {
			target.hasCode = Object.values(code).some((value) => value)
		}
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '保存代码失败')
	} finally {
		savingCode.value = false
	}
}

let searchTimer: number | null = null
watch(keyword, () => {
	if (searchTimer) {
		window.clearTimeout(searchTimer)
	}
	// 输入防抖：避免每敲一个字打一次接口
	searchTimer = window.setTimeout(() => {
		page.value = 1
		void loadList()
	}, 320)
})

watch(category, () => {
	page.value = 1
	void loadList()
})

onMounted(async () => {
	await loadInfo()
	await loadList()
})
</script>

<template>
	<ExtShell title="我的资源" subtitle="MOTION LAB · 自建与采集的动效资源（模板请回工作台）" mark="资">
		<template #actions>
			<ExtStatusTag :text="statusTag.text" :tone="statusTag.tone" />
			<button class="ext-btn" type="button" @click="loadInfo">重新自检</button>
			<button class="ext-btn" type="button" @click="page = 1; loadList()">刷新列表</button>
			<button class="ext-btn" type="button" @click="crawlVisible = true">采集 GitHub</button>
			<button class="ext-btn" type="button" @click="backToWorkbench">回到工作台</button>
			<button class="ext-btn primary" type="button" @click="openCreate">新增动效</button>
		</template>

		<template #left>
			<div class="ext-panel">
				<div class="ext-panel-title">动效分类</div>
				<MotionCategoryTree v-model="category" />
			</div>
			<div class="ext-panel">
				<div class="ext-panel-title">关键词</div>
				<input v-model="keyword" class="search" type="text" placeholder="名称 / 说明 / 标签" spellcheck="false" />
			</div>
			<div class="ext-panel">
				<div class="ext-panel-title">资源列表</div>
				<MotionResourceList
					:items="items"
					:loading="listLoading"
					:active-id="activeId"
					:total="total"
					:page="page"
					:pages="pages"
					@select="selectMotion"
					@edit="openEdit"
					@remove="removeMotion"
					@page="(value: number) => { page = value; loadList() }" />
			</div>
		</template>

		<template v-if="detail">
			<MotionPreviewPane
				:css-code="(detail.code && detail.code.cssCode) || ''"
				:name="detail.name"
				:category="detail.category"
				:technology="detail.technology"
				:license="detail.license" />
			<div class="ext-card" style="margin-top: 16px">
				<div class="ext-kv"><span class="k">说明</span><span class="v">{{ detail.description || '—' }}</span></div>
				<div class="ext-kv" style="margin-top: 8px">
					<span class="k">标签</span>
					<span class="v">{{ detail.tags.length ? detail.tags.join(' · ') : '—' }}</span>
				</div>
				<div class="ext-kv" style="margin-top: 8px">
					<span class="k">来源</span>
					<span class="v">{{ detail.sourceUrl || '—' }}</span>
				</div>
				<div class="ext-kv" style="margin-top: 8px">
					<span class="k">更新时间</span><span class="v">{{ detail.updateTime }}</span>
				</div>
			</div>
		</template>
		<template v-else>
			<ExtEmpty
				tag="v1.0.1"
				title="选一条动效开始"
				hint="左侧选分类或关键词筛出资源，点列表里的一条就会在这里实时预览（沙箱 iframe），右侧可以生成并编辑四种代码。" />
		</template>

		<template #right>
			<div class="ext-panel">
				<div class="ext-panel-title">AI 产出</div>
				<MotionCodePanel :detail="detail" :saving="savingCode" @save="saveCode" />
			</div>
			<div v-if="info" class="ext-panel">
				<div class="ext-panel-title">模块能力</div>
				<ul class="ext-list">
					<li v-for="item in info.capabilities" :key="item">{{ item }}</li>
				</ul>
				<div class="ext-hint">数据表：{{ info.plannedTables.join('、') }}</div>
			</div>
		</template>
	</ExtShell>

	<MotionEditorDialog
		v-model="dialogVisible"
		:detail="editingExists ? detail : null"
		:saving="savingForm"
		@submit="submitForm" />

	<MotionCrawlDialog v-model="crawlVisible" @crawled="onCrawled" />
</template>

<style scoped>
.search {
	width: 100%;
	box-sizing: border-box;
	background: var(--ext-bg-raised);
	border: 1px solid var(--ext-line);
	border-radius: 8px;
	color: var(--ext-text);
	font-family: var(--ext-font-mono);
	font-size: 12px;
	padding: 8px 10px;
	outline: none;
}

.search:focus {
	border-color: var(--ext-gold);
}

.ext-btn.primary {
	background: linear-gradient(150deg, var(--ext-gold-light), var(--ext-gold));
	border-color: transparent;
	color: #14161a;
	font-weight: 700;
}
</style>
