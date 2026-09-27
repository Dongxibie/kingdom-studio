<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { ExtShell, ExtStatusTag } from '@/extensions/_shared/components'
import {
	analyzeCandidate,
	fetchCandidateStats,
	listCandidates,
	listTemplates,
	promoteCandidate,
	reviewCandidate,
} from '@/extensions/motion-lab/api/template'
import { REVIEWABLE_STATUS } from '@/extensions/motion-lab/types/candidate'
import type { CandidateQuery, CandidateStats, MotionCandidate } from '@/extensions/motion-lab/types/candidate'
import type { MotionTemplateItem } from '@/extensions/motion-lab/types/workbench'

/**
 * 候选池：GitHub 发现 → 规则分析 → 人工筛选 → 转成 Motion Pattern 的界面。
 *
 * 两条原则直接体现在界面上：
 *  1. 每条候选都能看到「为什么分到这一类」（命中的关键词）与「为什么被淘汰」（淘汰理由）；
 *  2. 转成模板时必须选一个内置 Pattern —— 代码来自我们自己的实现，候选只提供名字、来源与许可。
 */
const router = useRouter()

const stats = ref<CandidateStats | null>(null)
const items = ref<MotionCandidate[]>([])
const templates = ref<MotionTemplateItem[]>([])
const active = ref<MotionCandidate | null>(null)
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = 12
const pages = ref(1)

const query = ref<CandidateQuery>({ sort: 'STARS' })
const reviewNote = ref('')
const patternKey = ref('')
const promoteName = ref('')

const statusOptions = [
	{ value: 'ANALYZED', label: '已分析' },
	{ value: 'SELECTED', label: '已选入' },
	{ value: 'PROMOTED', label: '已入库' },
	{ value: 'REJECTED', label: '已淘汰' },
	{ value: 'NEW', label: '待看' },
]

const categoryOptions = ['文字动画', '卡片交互', '按钮交互', '滚动动画', '首屏动画', '背景效果', '三维 WebGL']

const patternOptions = computed(() => templates.value
	.filter((item) => !active.value || item.category === active.value.category)
	.map((item) => ({ value: item.templateKey, label: `${item.name}（${item.templateKey}）` })))

const statsLine = computed(() => {
	const value = stats.value
	if (!value) {
		return '读取中…'
	}
	return `候选 ${value.total} 条 · 已入库 ${value.byStatus.PROMOTED ?? 0} · 已选入 ${value.byStatus.SELECTED ?? 0}`
		+ ` · 已淘汰 ${value.byStatus.REJECTED ?? 0} ｜ 资源库 ${value.libraryTotal} 个模板（官方 ${value.officialTotal} / 社区 ${value.communityTotal}）`
})

async function loadStats() {
	try {
		stats.value = await fetchCandidateStats()
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取候选池概览失败')
	}
}

async function loadList() {
	loading.value = true
	try {
		const result = await listCandidates({ ...query.value, page: page.value, size })
		items.value = result.records
		total.value = result.total
		pages.value = result.pages
		// 只在还没选中任何候选时自动选第一条：否则评审后（候选可能已不在当前筛选内）
		// 右手边会被悄悄换成另一条候选，看起来像「操作没生效」
		if (items.value.length && !active.value) {
			select(items.value[0])
		}
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取候选失败')
	} finally {
		loading.value = false
	}
}

async function loadTemplates() {
	try {
		const result = await listTemplates({ size: 100 })
		templates.value = result.records
	} catch {
		// Pattern 列表读不到时只是不能做转换，不影响浏览候选
	}
}

function select(candidate: MotionCandidate) {
	active.value = candidate
	reviewNote.value = candidate.reviewNote
	patternKey.value = candidate.patternKey || ''
	promoteName.value = ''
}

function applyFilter(field: 'status' | 'category', value: string) {
	query.value = { ...query.value, [field]: query.value[field] === value ? undefined : value }
	page.value = 1
	void loadList()
}

function search() {
	page.value = 1
	void loadList()
}

function clearFilters() {
	query.value = { sort: 'STARS' }
	page.value = 1
	void loadList()
}

async function reanalyze() {
	if (!active.value) {
		return
	}
	try {
		const updated = await analyzeCandidate(active.value.id)
		select(updated)
		await loadList()
		ElMessage.success('已按当前规则重新分析')
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '重新分析失败')
	}
}

async function review(status: string) {
	const candidate = active.value
	if (!candidate) {
		return
	}
	const keepId = candidate.id
	if (status === 'REJECTED' && !reviewNote.value.trim()) {
		ElMessage.warning('淘汰要写清楚理由，后面才好复盘')
		return
	}
	try {
		const updated = await reviewCandidate(candidate.id, status, reviewNote.value)
		await Promise.all([loadList(), loadStats()])
		// 先刷新列表，再把这条重新选上：这样「已不在当前筛选内」也不会切走视图
		select(updated)
		ElMessage.success(`已更新筛选状态：${updated.statusLabel}（${keepId}）`.replace(`（${keepId}）`, ''))
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '更新失败')
	}
}

async function promote() {
	const candidate = active.value
	if (!candidate) {
		return
	}
	if (!patternKey.value) {
		ElMessage.warning('请先选一个内置 Pattern：代码必须来自我们自己的实现')
		return
	}
	try {
		await ElMessageBox.confirm(
			`用「${patternKey.value}」的实现承载这条候选，代码来自 Pattern，候选只提供名字、来源与许可。`,
			'转为 Motion Pattern',
			{ confirmButtonText: '转换', cancelButtonText: '取消', type: 'warning' },
		)
	} catch {
		return
	}
	try {
		const result = await promoteCandidate(candidate.id, patternKey.value, promoteName.value || undefined)
		await Promise.all([loadList(), loadStats()])
		// 已入库：右侧保留这条候选并展示入库结果，不跟着筛选结果跳走
		select({
			...candidate,
			status: 'PROMOTED',
			statusLabel: '已入库',
			patternKey: String(result.patternKey ?? patternKey.value),
			promotedTemplateKey: String(result.templateKey),
			reviewNote: `已转为 Motion Pattern（实现来自 ${String(result.patternKey ?? patternKey.value)}），来源仅作标注`,
		})
		ElMessage.success(`已入库为模板 ${String(result.templateKey)}`)
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '转换失败')
	}
}

function openWorkbench() {
	void router.push('/extensions/motion-lab')
}

function openSource(url: string) {
	window.open(url, '_blank', 'noopener')
}

onMounted(async () => {
	await Promise.all([loadStats(), loadTemplates()])
	await loadList()
})
</script>

<template>
	<ExtShell title="动效候选池" subtitle="MOTION DISCOVERY · 发现 → 分析 → 筛选 → 转成 Pattern" mark="C">
		<template #actions>
			<ExtStatusTag :text="statsLine" tone="ok" />
			<button class="ext-btn" type="button" @click="openWorkbench">回工作台</button>
		</template>

		<template #left>
			<div class="discover">
				<div class="group">
					<div class="group-title">按状态</div>
					<div class="options">
						<button
							v-for="option in statusOptions"
							:key="option.value"
							class="option"
							:class="{ on: query.status === option.value }"
							type="button"
							@click="applyFilter('status', option.value)">
							<span class="name">{{ option.label }}</span>
							<span class="count">{{ stats?.byStatus[option.value] ?? 0 }}</span>
						</button>
					</div>
				</div>

				<div class="group">
					<div class="group-title">按建议分类</div>
					<div class="options">
						<button
							v-for="option in categoryOptions"
							:key="option"
							class="option"
							:class="{ on: query.category === option }"
							type="button"
							@click="applyFilter('category', option)">
							<span class="name">{{ option }}</span>
							<span class="count">{{ stats?.byCategory[option] ?? 0 }}</span>
						</button>
					</div>
				</div>

				<div class="group">
					<div class="group-title">关键词 / 星数</div>
					<div class="search-row">
						<input v-model="query.keyword" placeholder="仓库名、说明、主题" @keyup.enter="search" />
						<button class="ext-btn" type="button" @click="search">筛选</button>
					</div>
					<div class="search-row">
						<input v-model.number="query.minStars" type="number" min="0" placeholder="星数下限" @keyup.enter="search" />
						<button class="ext-btn" type="button" @click="clearFilters">清空</button>
					</div>
				</div>

				<div class="note">
					候选池只保存元数据（名称 / 地址 / 许可 / 星数 / 主题），<b>不保存任何第三方源码</b>。
					转成模板时，代码来自你选的那个内置 Pattern。
				</div>
			</div>
		</template>

		<div class="pool">
			<div class="pool-head">
				<span class="pool-count">共 {{ total }} 条候选</span>
				<span class="pool-page">
					<button class="ext-btn" type="button" :disabled="page <= 1" @click="page -= 1; loadList()">上一页</button>
					<span class="page-text">{{ page }} / {{ pages }}</span>
					<button class="ext-btn" type="button" :disabled="page >= pages" @click="page += 1; loadList()">下一页</button>
				</span>
			</div>

			<div v-if="loading" class="empty">读取中…</div>
			<div v-else-if="!items.length" class="empty">没有符合条件的候选</div>

			<div class="rows">
				<button
					v-for="item in items"
					:key="item.id"
					class="row"
					:class="{ on: active?.id === item.id }"
					type="button"
					@click="select(item)">
					<div class="row-main">
						<span class="row-name">{{ item.name }}</span>
						<span class="row-repo">{{ item.fullName }}</span>
					</div>
					<div class="row-tags">
						<span class="tag">{{ item.category }}</span>
						<span class="tag">{{ item.triggerLabel }}</span>
						<span class="tag tier">{{ item.performanceLabel }}</span>
						<span class="tag">{{ item.stars }} ★</span>
						<span class="tag license">{{ item.license }}</span>
					</div>
					<span class="row-status" :class="'s-' + item.status.toLowerCase()">{{ item.statusLabel }}</span>
				</button>
			</div>
		</div>

		<template #right>
			<div v-if="!active" class="detail-empty">选一条候选看详情</div>
			<div v-else class="detail">
				<div class="detail-title">{{ active.name }}</div>
				<div class="detail-repo">{{ active.fullName }} · {{ active.language || '未知语言' }} · {{ active.stars }} ★</div>
				<div class="detail-desc">{{ active.description || '（仓库没有写说明）' }}</div>

				<div class="detail-actions">
					<button class="ext-btn" type="button" @click="openSource(active.sourceUrl)">打开来源</button>
					<button class="ext-btn" type="button" @click="reanalyze">重新分析</button>
				</div>

				<div class="meta">
					<div class="meta-row"><span class="k">建议分类</span><span class="v">{{ active.category }}</span></div>
					<div class="meta-row"><span class="k">触发方式</span><span class="v">{{ active.triggerLabel }}</span></div>
					<div class="meta-row"><span class="k">难度</span><span class="v">{{ active.difficultyLabel }}</span></div>
					<div class="meta-row"><span class="k">运行档位</span><span class="v">{{ active.performanceLabel }}</span></div>
					<div class="meta-row"><span class="k">视觉潜力</span><span class="v">{{ active.visualStars }} 星（{{ active.visualScore }}）</span></div>
					<div class="meta-row"><span class="k">许可</span><span class="v">{{ active.license }}</span></div>
					<div class="meta-row"><span class="k">状态</span><span class="v">{{ active.statusLabel }}</span></div>
				</div>

				<div v-if="active.matchedHints.length" class="hints">
					<span class="k">凭什么分到这一类</span>
					<span v-for="hint in active.matchedHints" :key="hint" class="hint">{{ hint }}</span>
				</div>

				<div v-if="active.status === 'PROMOTED'" class="promoted">
					已入库为模板 <code>{{ active.promotedTemplateKey }}</code>（承载 Pattern：<code>{{ active.patternKey }}</code>）
				</div>

				<div v-if="active.status !== 'PROMOTED'" class="review">
					<div class="review-title">人工筛选</div>
					<textarea v-model="reviewNote" rows="2" placeholder="意见；淘汰时必填，写清楚为什么不要它"></textarea>
					<div class="review-buttons">
						<button
							v-for="option in REVIEWABLE_STATUS"
							:key="option.value"
							class="ext-btn"
							type="button"
							@click="review(option.value)">
							{{ option.label }}
						</button>
					</div>
				</div>

				<div v-if="active.status !== 'PROMOTED'" class="promote">
					<div class="promote-title">转为 Motion Pattern</div>
					<div class="promote-hint">代码来自选中的 Pattern；候选只提供名字、来源与许可，不复制任何源码。</div>
					<select v-model="patternKey" class="promote-select">
						<option value="">选择承载用的 Pattern…</option>
						<option v-for="option in patternOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
					</select>
					<input v-model="promoteName" placeholder="模板名（留空用候选名）" />
					<button class="ext-btn primary" type="button" @click="promote">转换并入库</button>
				</div>

				<div v-if="active.prompt" class="prompt">{{ active.prompt }}</div>
			</div>
		</template>
	</ExtShell>
</template>

<style scoped>
.discover { display: flex; flex-direction: column; gap: 14px; }
.group-title { font-size: 11.5px; color: var(--ext-text-mute); margin-bottom: 7px; }
.options { display: flex; flex-wrap: wrap; gap: 6px; }
.option {
	display: flex; align-items: center; gap: 6px;
	border: 1px solid var(--ext-line); background: rgba(255, 255, 255, 0.03);
	color: var(--ext-text-dim); border-radius: 999px; font-size: 11.5px; padding: 4px 10px; cursor: pointer;
}
.option.on { border-color: var(--ext-gold); background: var(--ext-gold-soft); color: var(--ext-gold-light); }
.option .count { font-family: var(--ext-font-mono); font-size: 10px; color: var(--ext-text-mute); }
.search-row { display: flex; gap: 6px; margin-bottom: 6px; }
.search-row input {
	flex: 1; min-width: 0; background: rgba(255, 255, 255, 0.04);
	border: 1px solid var(--ext-line); border-radius: 8px; color: var(--ext-text);
	padding: 6px 9px; font-size: 12px;
}
.note { font-size: 11px; line-height: 1.75; color: var(--ext-text-mute); }
.note b { color: var(--ext-gold-light); }

.pool { display: flex; flex-direction: column; gap: 10px; }
.pool-head { display: flex; align-items: center; justify-content: space-between; }
.pool-count { font-size: 12px; color: var(--ext-text-mute); }
.pool-page { display: flex; align-items: center; gap: 8px; }
.page-text { font-family: var(--ext-font-mono); font-size: 11px; color: var(--ext-text-mute); }
.rows { display: flex; flex-direction: column; gap: 6px; }
.row {
	display: grid; grid-template-columns: 1fr auto auto; align-items: center; gap: 10px;
	text-align: left; padding: 9px 11px; border-radius: 12px; cursor: pointer;
	border: 1px solid var(--ext-line); background: rgba(255, 255, 255, 0.03);
	color: var(--ext-text);
}
.row.on { border-color: var(--ext-gold); background: var(--ext-gold-soft); }
.row-main { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.row-name { font-size: 13px; }
.row-repo { font-size: 10.5px; color: var(--ext-text-mute); font-family: var(--ext-font-mono); }
.row-tags { display: flex; gap: 5px; flex-wrap: wrap; }
.tag {
	font-size: 10px; padding: 1px 6px; border-radius: 999px;
	border: 1px solid var(--ext-line); color: var(--ext-text-dim);
}
.tag.tier { color: var(--ext-neon-cyan); border-color: rgba(94, 234, 212, 0.4); }
.tag.license { color: var(--ext-text-mute); }
.row-status { font-size: 11px; color: var(--ext-text-mute); white-space: nowrap; }
.row-status.s-promoted { color: #7ee0a2; }
.row-status.s-rejected { color: #ff8f8f; }
.empty, .detail-empty { padding: 20px; text-align: center; color: var(--ext-text-mute); font-size: 12px; }

.detail { display: flex; flex-direction: column; gap: 10px; }
.detail-title { font-size: 15px; color: var(--ext-text); }
.detail-repo { font-size: 11px; color: var(--ext-text-mute); font-family: var(--ext-font-mono); }
.detail-desc { font-size: 12px; line-height: 1.7; color: var(--ext-text-dim); }
.detail-actions { display: flex; gap: 6px; }
.meta { display: flex; flex-direction: column; gap: 4px; }
.meta-row { display: flex; justify-content: space-between; font-size: 11.5px; }
.meta-row .k { color: var(--ext-text-mute); }
.meta-row .v { color: var(--ext-text); }
.hints { display: flex; flex-wrap: wrap; gap: 5px; align-items: center; font-size: 11px; }
.hints .k { color: var(--ext-text-mute); margin-right: 4px; }
.hint { padding: 1px 6px; border-radius: 999px; background: rgba(94, 234, 212, 0.12); color: var(--ext-neon-cyan); }
.promoted { font-size: 11.5px; line-height: 1.7; color: #7ee0a2; }
.promoted code { font-family: var(--ext-font-mono); color: var(--ext-gold-light); }
.review, .promote {
	display: flex; flex-direction: column; gap: 6px;
	padding: 10px; border-radius: 12px; border: 1px solid var(--ext-line); background: rgba(255, 255, 255, 0.03);
}
.review-title, .promote-title { font-size: 11.5px; color: var(--ext-text-mute); }
.review textarea, .promote input, .promote-select {
	background: rgba(0, 0, 0, 0.25); border: 1px solid var(--ext-line); border-radius: 8px;
	color: var(--ext-text); padding: 6px 9px; font-size: 12px; font-family: inherit;
}
.review-buttons { display: flex; gap: 6px; flex-wrap: wrap; }
.promote-hint { font-size: 11px; line-height: 1.7; color: var(--ext-text-mute); }
.ext-btn.primary { border-color: var(--ext-gold); background: var(--ext-gold-soft); color: var(--ext-gold-light); }
.prompt { font-size: 11px; line-height: 1.75; color: var(--ext-text-mute); }
</style>
