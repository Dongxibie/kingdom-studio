<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { ExtCodeBlock, ExtShell, ExtStatusTag } from '@/extensions/_shared/components'
import '@/extensions/motion-lab/styles/obsidian.css'
import '@/extensions/motion-lab/styles/workbench.css'
import MotionSearchBar from '@/extensions/motion-lab/components/MotionSearchBar.vue'
import MotionDiscoverPanel from '@/extensions/motion-lab/components/MotionDiscoverPanel.vue'
import MotionTemplateCards from '@/extensions/motion-lab/components/MotionTemplateCards.vue'
import MotionPreviewStage from '@/extensions/motion-lab/components/MotionPreviewStage.vue'
import MotionStageControls from '@/extensions/motion-lab/components/MotionStageControls.vue'
import { fetchMotionModuleInfo } from '@/extensions/motion-lab/api/motion'
import {
	fetchFacets,
	getRecipe,
	getTemplate,
	listRecipes,
	listTemplates,
	searchMotions,
} from '@/extensions/motion-lab/api/template'
import type { ExtModuleInfo } from '@/extensions/_shared/types/common'
import type {
	MotionFacets,
	MotionRecipe,
	MotionSearchResult,
	MotionTemplateDetail,
	MotionTemplateItem,
	TemplateQuery,
} from '@/extensions/motion-lab/types/workbench'
import { ONBOARDING_SCENES } from '@/extensions/motion-lab/types/workbench'

const router = useRouter()

const info = ref<ExtModuleInfo | null>(null)
const errorMessage = ref('')
const facets = ref<MotionFacets | null>(null)
const recipes = ref<MotionRecipe[]>([])
const items = ref<MotionTemplateItem[]>([])
const total = ref(0)
const pages = ref(1)
const page = ref(1)
const pageSize = 12
const listLoading = ref(false)

const query = ref<TemplateQuery>({ sort: 'SCORE' })
const activeKey = ref('')
const detail = ref<MotionTemplateDetail | null>(null)
const detailLoading = ref(false)
const activeRecipe = ref<MotionRecipe | null>(null)

const searchLoading = ref(false)
const searchResult = ref<MotionSearchResult | null>(null)

const paramValues = ref<Record<string, number>>({})
const playing = ref(true)
const speed = ref(1)
const scale = ref(1)
const restartToken = ref(0)
const stageRef = ref<InstanceType<typeof MotionPreviewStage> | null>(null)

const outputTab = ref<'code' | 'prompt' | 'params'>('code')
const codeTab = ref<'vueCode' | 'reactCode' | 'cssCode' | 'threeCode'>('vueCode')
const codeTabs: { key: typeof codeTab.value; label: string; lang: string }[] = [
	{ key: 'vueCode', label: 'Vue', lang: 'vue' },
	{ key: 'reactCode', label: 'React', lang: 'tsx' },
	{ key: 'cssCode', label: 'CSS', lang: 'css' },
	{ key: 'threeCode', label: 'Three.js', lang: 'js' },
]

/** 新手引导：第一次进入先问「你想制作什么」，选过之后不再打断 */
const onboarded = ref(true)

const statusTag = computed(() => {
	if (errorMessage.value) {
		return { text: '后端未连通', tone: 'red' as const }
	}
	if (!info.value) {
		return { text: '自检中', tone: 'mute' as const }
	}
	return { text: '工作台已就绪 · ' + info.value.phase, tone: 'ok' as const }
})

const currentCode = computed(() => {
	const source = detail.value
	return source ? source[codeTab.value] || '' : ''
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

async function loadFacets() {
	try {
		facets.value = await fetchFacets()
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取发现方式失败')
	}
}

async function loadRecipes(scene?: string) {
	try {
		recipes.value = await listRecipes(scene)
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取组合方案失败')
	}
}

async function loadList() {
	listLoading.value = true
	try {
		const result = await listTemplates({ ...query.value, page: page.value, size: pageSize })
		items.value = result.records
		total.value = result.total
		pages.value = result.pages
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取模板失败')
	} finally {
		listLoading.value = false
	}
}

async function selectTemplate(templateKey: string) {
	activeKey.value = templateKey
	detailLoading.value = true
	try {
		detail.value = await getTemplate(templateKey)
		outputTab.value = 'code'
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取模板详情失败')
	} finally {
		detailLoading.value = false
	}
}

/** 选中组合方案：先加载它的第一个成员做预览，再回到方案视图（效果 = 这几个叠起来） */
async function selectRecipe(recipeKey: string) {
	try {
		const recipe = await getRecipe(recipeKey)
		const first = recipe.memberKeys[0]
		if (first) {
			await selectTemplate(first)
		}
		activeRecipe.value = recipe
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取组合方案失败')
	}
}

function onFacetSelect(field: 'scene' | 'style' | 'technology' | 'category' | 'difficulty', value: string) {
	const next: TemplateQuery = { ...query.value }
	if (field === 'difficulty') {
		next.difficulty = String(query.value.difficulty ?? '') === value ? undefined : Number(value)
	} else if (query.value[field] === value) {
		next[field] = undefined
	} else {
		next[field] = value
	}
	query.value = next
	searchResult.value = null
	activeRecipe.value = null
	page.value = 1
	void loadList()
}

function clearFilters() {
	query.value = { sort: 'SCORE' }
	page.value = 1
	searchResult.value = null
	activeRecipe.value = null
	void loadList()
	void loadRecipes()
}

/** 快捷场景（搜索栏下方与新手引导都走这里） */
function onQuickScene(scene: string) {
	query.value = { ...query.value, scene, sort: 'SCORE' }
	page.value = 1
	activeRecipe.value = null
	searchResult.value = null
	void loadList()
	void loadRecipes(scene)
}

async function onSearch(text: string) {
	searchLoading.value = true
	try {
		searchResult.value = await searchMotions(text, 6)
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '检索失败')
	} finally {
		searchLoading.value = false
	}
}

function onSearchPick(kind: 'TEMPLATE' | 'RECIPE', key: string) {
	if (kind === 'RECIPE') {
		void selectRecipe(key)
	} else {
		void selectTemplate(key)
	}
}

function onRestart() {
	restartToken.value += 1
	playing.value = true
	window.setTimeout(() => stageRef.value?.restart(), 60)
}

function onParamChange(values: Record<string, number>) {
	paramValues.value = values
}

function toResourcePage() {
	void router.push('/extensions/motion-lab/resources')
}

function startWith(scene: string) {
	localStorage.setItem('mlab_onboarded', '1')
	onboarded.value = true
	onQuickScene(scene)
	const picked = ONBOARDING_SCENES.find((item) => item.value === scene)
	ElMessage.success('已按「' + (picked?.label ?? scene) + '」推荐')
}

function skipGuide() {
	localStorage.setItem('mlab_onboarded', '1')
	onboarded.value = true
}

onMounted(async () => {
	onboarded.value = localStorage.getItem('mlab_onboarded') === '1'
	await Promise.all([loadInfo(), loadFacets(), loadRecipes(), loadList()])
	if (items.value.length) {
		// 默认打开推荐指数最高的那条，首次进入就有东西可看
		await selectTemplate(items.value[0].templateKey)
	}
})
</script>

<template>
	<ExtShell
		title="动效工作台"
		subtitle="MOTION LAB · 发现 → 组合 → 调参 → 出代码"
		mark="M"
		variant="obsidian"
		full-width>
		<template #actions>
			<ExtStatusTag :text="statusTag.text" :tone="statusTag.tone" />
			<button class="ext-btn" type="button" @click="loadInfo">重新自检</button>
			<button class="ext-btn" type="button" @click="toResourcePage">我的资源</button>
		</template>

		<template #top>
			<MotionSearchBar
				:loading="searchLoading"
				:result="searchResult"
				@search="onSearch"
				@pick="onSearchPick"
				@scene="startWith" />
		</template>

		<template #left>
			<div class="ext-panel">
				<div class="ext-panel-title">发现方式</div>
				<MotionDiscoverPanel
					:facets="facets"
					:recipes="recipes"
					:query="query"
					:active-recipe="activeRecipe?.recipeKey ?? ''"
					@select="onFacetSelect"
					@recipe="selectRecipe"
					@clear="clearFilters" />
			</div>
		</template>

		<template v-if="!onboarded">
			<div class="mlab-guide">
				<h2 class="mlab-guide-title">你想制作什么？</h2>
				<p class="mlab-guide-sub">选一个场景，我按推荐指数给你能直接用的动效与组合方案。</p>
				<div class="mlab-guide-grid">
					<button
						v-for="item in ONBOARDING_SCENES"
						:key="item.value"
						class="mlab-guide-card"
						type="button"
						@click="startWith(item.value)">
						<div class="mlab-guide-name">{{ item.label }}</div>
						<div class="mlab-guide-hint">{{ item.hint }}</div>
					</button>
				</div>
				<button class="mlab-guide-skip" type="button" @click="skipGuide">跳过，直接进入工作台</button>
			</div>
		</template>

		<template v-else>
			<div class="mlab-workbench">
				<div class="mlab-stage-head">
					<div class="mlab-stage-title">
						<span v-if="activeRecipe" class="mlab-recipe-badge">组合方案 · {{ activeRecipe.name }}</span>
						<span class="mlab-name">{{ detail ? detail.name : '实时预览' }}</span>
						<span v-if="detail" class="mlab-meta">
							{{ detail.sceneLabel }} · {{ detail.styleLabel }} · {{ detail.technology }} · {{ detail.difficultyLabel }}
						</span>
					</div>
					<div v-if="detail" class="mlab-stage-score">
						<span class="mlab-stars">{{ '★'.repeat(Math.floor(detail.stars)) }}{{ detail.stars % 1 ? '⯪' : '' }}{{ '☆'.repeat(Math.max(0, 5 - Math.ceil(detail.stars))) }}</span>
						<span class="mlab-value">推荐指数 {{ detail.score }}</span>
					</div>
				</div>

				<MotionStageControls
					:detail="detail"
					:playing="playing"
					:speed="speed"
					:scale="scale"
					@change="onParamChange"
					@restart="onRestart"
					@update:playing="(value: boolean) => (playing = value)"
					@update:speed="(value: number) => (speed = value)"
					@update:scale="(value: number) => (scale = value)" />

				<MotionPreviewStage
					ref="stageRef"
					:detail="detail"
					:param-values="paramValues"
					:playing="playing"
					:speed="speed"
					:scale="scale"
					:restart-token="restartToken" />

				<div v-if="activeRecipe" class="mlab-recipe-box">
					<div class="mlab-recipe-title">组合方案「{{ activeRecipe.name }}」由这些动效叠成</div>
					<div class="mlab-recipe-members">
						<button
							v-for="member in activeRecipe.members"
							:key="member.templateKey"
							class="mlab-member"
							type="button"
							@click="selectTemplate(member.templateKey)">
							<span class="mlab-member-name">{{ member.name }}</span>
							<span class="mlab-member-score">{{ member.score }}</span>
						</button>
					</div>
					<div class="mlab-recipe-why">{{ activeRecipe.description }}</div>
					<div v-if="activeRecipe.prompt" class="mlab-recipe-prompt">{{ activeRecipe.prompt }}</div>
				</div>

				<div class="mlab-list-head">
					<span class="mlab-t">全部模板</span>
					<span class="mlab-s">按推荐指数排序，点卡片即可实时预览并取代码</span>
				</div>
				<MotionTemplateCards
					:items="items"
					:active-key="activeKey"
					:loading="listLoading"
					:total="total"
					:page="page"
					:pages="pages"
					@select="selectTemplate"
					@page="(value: number) => { page = value; loadList() }" />
			</div>
		</template>

		<template #right>
			<div class="ext-panel">
				<div class="ext-panel-title">推荐指数</div>
				<div v-if="detail" class="mlab-score-card">
					<div class="mlab-score-main">
						<span class="mlab-num">{{ detail.score }}</span>
						<span class="mlab-of">/ 100</span>
						<span class="grade" :class="'mlab-grade-' + detail.grade">{{ detail.grade }}</span>
					</div>
					<div class="mlab-score-bars">
						<div class="mlab-bar-row">
							<span class="mlab-k">视觉效果</span>
							<div class="mlab-track"><i :style="{ width: detail.scoreVisual + '%' }" /></div>
							<span class="mlab-v">{{ detail.scoreVisual }}</span>
						</div>
						<div class="mlab-bar-row">
							<span class="mlab-k">代码质量</span>
							<div class="mlab-track"><i :style="{ width: detail.scoreCode + '%' }" /></div>
							<span class="mlab-v">{{ detail.scoreCode }}</span>
						</div>
						<div class="mlab-bar-row">
							<span class="mlab-k">复用价值</span>
							<div class="mlab-track"><i :style="{ width: detail.scoreReuse + '%' }" /></div>
							<span class="mlab-v">{{ detail.scoreReuse }}</span>
						</div>
						<div class="mlab-bar-row">
							<span class="mlab-k">性能表现</span>
							<div class="mlab-track"><i :style="{ width: detail.scorePerf + '%' }" /></div>
							<span class="mlab-v">{{ detail.scorePerf }}</span>
						</div>
					</div>
					<div class="mlab-fit">
						<span class="mlab-k">适合</span>
						<span v-for="tag in detail.bestFor" :key="tag" class="mlab-chip">{{ tag }}</span>
					</div>
					<div v-if="detail.manualScore" class="mlab-manual">人工评分 {{ detail.manualScore }} 星：{{ detail.manualReason }}</div>
					<div v-if="detail.usedByRecipes.length" class="mlab-used">被「{{ detail.usedByRecipes.join('、') }}」用到</div>
				</div>
				<div v-else class="ext-hint">选中一个模板后显示它的推荐指数构成。</div>
			</div>

			<div class="ext-panel">
				<div class="mlab-tabs">
					<button class="mlab-tab" :class="{ on: outputTab === 'code' }" type="button" @click="outputTab = 'code'">代码</button>
					<button class="mlab-tab" :class="{ on: outputTab === 'prompt' }" type="button" @click="outputTab = 'prompt'">Prompt</button>
					<button class="mlab-tab" :class="{ on: outputTab === 'params' }" type="button" @click="outputTab = 'params'">参数</button>
				</div>

				<div v-if="outputTab === 'code'">
					<div class="mlab-subtabs">
						<button
							v-for="tab in codeTabs"
							:key="tab.key"
							class="mlab-subtab"
							:class="{ on: codeTab === tab.key }"
							type="button"
							:disabled="tab.key === 'threeCode' && detail ? !detail.threeCode : false"
							@click="codeTab = tab.key">
							{{ tab.label }}
						</button>
					</div>
					<div v-if="detailLoading" class="ext-hint">读取中…</div>
					<ExtCodeBlock v-else-if="currentCode" :code="currentCode" :title="detail?.name ?? ''" />
					<div v-else class="ext-hint">这个模板没有这一类产物（纯 CSS 动效没有 Three.js 版本）。</div>
				</div>

				<div v-else-if="outputTab === 'prompt'">
					<ExtCodeBlock v-if="detail?.prompt" :code="detail.prompt" title="把它交给 AI 复现" />
					<div v-else class="ext-hint">这个模板还没有 Prompt。</div>
				</div>

				<div v-else class="ext-hint">
					参数控件在预览上方（时长、延迟、位移等），它们就是模板里的 CSS 变量：
					调完后导出的代码里是同一批值，所见即所得。
				</div>
			</div>
		</template>
	</ExtShell>
</template>
