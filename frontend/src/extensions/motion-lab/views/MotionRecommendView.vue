<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
	ExtCodeBlock,
	ExtEmpty,
	ExtShell,
	ExtStatusTag,
} from '@/extensions/_shared/components'
import MotionPreviewStage from '@/extensions/motion-lab/components/MotionPreviewStage.vue'
import { getTemplate, recommendMotions } from '@/extensions/motion-lab/api/template'
import type { MotionTemplateDetail } from '@/extensions/motion-lab/types/workbench'
import {
	INTENT_AXES,
	PERFORMANCE_GRADE_HINT,
	RECOMMEND_PRESETS,
	type MotionRecommendItem,
	type MotionRecommendResult,
} from '@/extensions/motion-lab/types/recommend'

/**
 * 智能推荐页（Phase 2）。
 *
 * 三栏对应三件事：左边说清需求、中间给方案、右边讲理由。
 * 这一页刻意不接模型：规则 + 权重，同一句输入每次结果都一样，
 * 所以「为什么推这个」能被逐条念出来，而不是一个说不清的黑盒分数。
 */
const query = ref(RECOMMEND_PRESETS[0]?.query ?? '')
const limit = ref(5)
const loading = ref(false)
const error = ref('')
const result = ref<MotionRecommendResult | null>(null)
const activeKey = ref('')

const detail = ref<MotionTemplateDetail | null>(null)
const detailLoading = ref(false)
const paramValues = ref<Record<string, number>>({})
const playing = ref(true)
const restartToken = ref(0)

const active = computed(() => result.value?.recommendations.find((item) => item.templateKey === activeKey.value) ?? null)

/** 识别到的轴：只列真的识别出来的，空轴不占位置 */
const intentRows = computed(() => {
	const intent = result.value?.intent
	if (!intent) {
		return []
	}
	return INTENT_AXES.map((axis) => {
		const key = axis.axis as 'scene' | 'style' | 'emotion' | 'performance' | 'interaction'
		return { axis: key, label: axis.label, value: intent.labels[key] ?? '', hits: intent.hits[key] ?? [] }
	}).filter((row) => row.value)
})

async function run() {
	const text = query.value.trim()
	if (!text) {
		error.value = '先写一句需求，例如「苹果官网风格」'
		return
	}
	loading.value = true
	error.value = ''
	try {
		const data = await recommendMotions(text, limit.value)
		result.value = data
		const first = data.recommendations[0]
		if (first) {
			await pick(first)
		} else {
			activeKey.value = ''
			detail.value = null
		}
	} catch (e) {
		error.value = e instanceof Error ? e.message : '推荐失败，请稍后再试'
	} finally {
		loading.value = false
	}
}

async function pick(item: MotionRecommendItem) {
	activeKey.value = item.templateKey
	playing.value = true
	await loadDetail(item.templateKey)
}

async function loadDetail(templateKey: string) {
	detailLoading.value = true
	try {
		const data = await getTemplate(templateKey)
		detail.value = data
		const values: Record<string, number> = {}
		data.params.forEach((param) => {
			if (param.defaultValue !== null) {
				values[param.key] = param.defaultValue
			}
		})
		paramValues.value = values
		restartToken.value += 1
	} catch {
		detail.value = null
		paramValues.value = {}
	} finally {
		detailLoading.value = false
	}
}

function usePreset(preset: { query: string }) {
	query.value = preset.query
	void run()
}

function replay() {
	playing.value = true
	restartToken.value += 1
}

onMounted(() => {
	// 进来先跑一次最常见的例子，页面不留空白；也顺带说明「同一句输入结果稳定」
	void run()
})
</script>

<template>
	<ExtShell
		title="智能推荐"
		subtitle="MOTION LAB · 一句话需求 → 五个轴 → Top 5 方案与理由"
		mark="R"
		variant="obsidian">
		<template #actions>
			<ExtStatusTag
				:text="result ? `已扫描 ${result.scanned} 个模板` : '规则推荐 · 不调用模型'"
				:tone="result ? 'ok' : 'mute'" />
			<button class="ext-btn" type="button" :disabled="loading" @click="run">
				{{ loading ? '推荐中…' : '重新推荐' }}
			</button>
		</template>

		<template #left>
			<div class="ext-panel">
				<div class="ext-panel-title">输入需求</div>
				<textarea
					v-model="query"
					class="mrec-input"
					rows="4"
					placeholder="例如：科技感首页 / 苹果官网风格 / 高级酒店官网 / 后台数据面板 / 游戏登录页面"
					@keydown.ctrl.enter="run"></textarea>

				<div class="mrec-field">
					<span class="mrec-field-label">推荐条数</span>
					<div class="mrec-limits">
						<button
							v-for="size in [3, 5, 8, 10]"
							:key="size"
							class="mrec-chip"
							:class="{ on: limit === size }"
							type="button"
							@click="limit = size">
							{{ size }} 条
						</button>
					</div>
				</div>

				<button class="mrec-run" type="button" :disabled="loading" @click="run">
					{{ loading ? '正在挑…' : '开始推荐' }}
				</button>
				<p class="mrec-tip">按 Ctrl + Enter 也可以直接推荐</p>
				<p v-if="error" class="mrec-error">{{ error }}</p>

				<div class="mrec-presets">
					<div class="mrec-field-label">试试这些说法</div>
					<button
						v-for="preset in RECOMMEND_PRESETS"
						:key="preset.label"
						class="mrec-chip"
						type="button"
						@click="usePreset(preset)">
						{{ preset.label }}
					</button>
				</div>
			</div>

			<div v-if="result" class="ext-panel mrec-intent">
				<div class="ext-panel-title">识别到的需求</div>
				<p class="mrec-summary">{{ result.intent.summary }}</p>
				<div v-for="row in intentRows" :key="row.axis" class="mrec-axis">
					<div class="mrec-axis-head">
						<span class="mrec-axis-name">{{ row.label }}</span>
						<span class="mrec-axis-value">{{ row.value }}</span>
					</div>
					<div v-if="row.hits.length" class="mrec-axis-hits">命中：{{ row.hits.join(' / ') }}</div>
				</div>
				<p v-if="!intentRows.length" class="mrec-tip">
					这句话里没读出具象的场景或风格，所以按推荐指数给了一版通用结果 —— 换一种说法可以更准。
				</p>
			</div>
		</template>

		<div class="mrec-main">
			<div class="mrec-main-head">
				<div class="mrec-main-title">推荐方案</div>
				<span v-if="result" class="mrec-main-sub">
					「{{ result.query }}」的前 {{ result.recommendations.length }} 个选择
				</span>
			</div>

			<ExtEmpty
				v-if="!result"
				tag="待推荐"
				title="左边写一句需求，这里给出方案"
				hint="系统会把需求拆成场景 / 风格 / 情绪 / 性能 / 触发五个轴，逐轴匹配后排序。" />

			<ExtEmpty
				v-else-if="!result.recommendations.length"
				tag="无结果"
				title="模板库里没有能打分的模板"
				hint="先确认建表与种子脚本已经执行（db/extensions_motion_seed.sql）。" />

			<div v-else class="mrec-list">
				<button
					v-for="(item, index) in result.recommendations"
					:key="item.templateKey"
					class="mrec-card"
					:class="{ on: activeKey === item.templateKey }"
					type="button"
					@click="pick(item)">
					<div class="mrec-card-top">
						<span class="mrec-rank">{{ index + 1 }}</span>
						<span class="mrec-name">{{ item.name }}</span>
						<span class="mrec-score">{{ item.score }} 分</span>
					</div>
					<div class="mrec-card-meta">
						<span>{{ item.sceneLabel }}</span>
						<i>·</i>
						<span>{{ item.styleLabel }}</span>
						<i>·</i>
						<span>{{ item.technology }}</span>
						<i>·</i>
						<span>{{ item.triggerLabel }}触发</span>
					</div>
					<div class="mrec-card-tags">
						<span class="mrec-badge" :class="'grade-' + item.performanceGrade">
							性能 {{ PERFORMANCE_GRADE_HINT[item.performanceGrade] ?? item.performanceGrade }}
						</span>
						<span class="mrec-badge">{{ item.runtimeTierLabel }}</span>
						<span class="mrec-badge">推荐指数 {{ item.recommendScore }}</span>
					</div>
					<div class="mrec-card-reason">{{ item.reasons[0] }}</div>
				</button>
			</div>
		</div>

		<template #right>
			<div class="ext-panel">
				<div class="ext-panel-title">效果预览</div>
				<template v-if="active">
					<div class="mrec-preview-head">
						<span class="mrec-preview-name">{{ active.name }}</span>
						<button class="ext-btn" type="button" @click="replay">重播</button>
					</div>
					<div class="mrec-stage">
						<MotionPreviewStage
							:detail="detail"
							:param-values="paramValues"
							:playing="playing"
							:speed="1"
							:scale="1"
							:restart-token="restartToken" />
					</div>
					<p v-if="detailLoading" class="mrec-tip">正在载入预览…</p>
					<p class="mrec-tip">{{ active.description }}</p>
				</template>
				<p v-else class="mrec-tip">点中间任意一条推荐，这里显示它的效果与理由。</p>
			</div>

			<div v-if="active" class="ext-panel">
				<div class="ext-panel-title">为什么推荐它</div>
				<div class="mrec-axes">
					<span v-for="axis in active.matchedAxes" :key="axis" class="mrec-axis-tag">命中 {{ axis }}</span>
					<span v-if="!active.matchedAxes.length" class="mrec-axis-tag">按推荐指数入选</span>
				</div>
				<ul class="mrec-reasons">
					<li v-for="(reason, index) in active.reasons" :key="index">{{ reason }}</li>
				</ul>
				<div class="mrec-facts">
					<div><span>性能等级</span><b>{{ PERFORMANCE_GRADE_HINT[active.performanceGrade] ?? active.performanceGrade }}</b></div>
					<div><span>运行档位</span><b>{{ active.runtimeTierLabel }}</b></div>
					<div><span>分组</span><b>{{ active.category }}</b></div>
					<div><span>推荐指数</span><b>{{ active.recommendScore }} 分（{{ active.stars }} 星）</b></div>
				</div>
			</div>

			<div v-if="detail?.prompt" class="ext-panel">
				<div class="ext-panel-title">拿去用</div>
				<p class="mrec-tip">下面这段提示词描述了「{{ detail.name }}」的做法，可直接粘给模型在你的项目里复现。</p>
				<ExtCodeBlock :code="detail.prompt" lang="prompt" title="复现提示词" />
			</div>
		</template>
	</ExtShell>
</template>

<style scoped>
.mrec-input {
	width: 100%;
	border: 1px solid var(--ext-line);
	border-radius: 10px;
	background: rgba(255, 255, 255, 0.03);
	color: var(--ext-text);
	font-size: 12.5px;
	line-height: 1.7;
	padding: 10px 12px;
	resize: vertical;
	font-family: inherit;
}

.mrec-input:focus {
	outline: none;
	border-color: rgba(240, 205, 114, 0.5);
}

.mrec-field {
	margin-top: 12px;
}

.mrec-field-label {
	display: block;
	font-size: 11.5px;
	color: var(--ext-text-mute);
	margin-bottom: 7px;
}

.mrec-limits {
	display: flex;
	gap: 6px;
}

.mrec-chip {
	border: 1px solid var(--ext-line);
	background: rgba(255, 255, 255, 0.03);
	color: var(--ext-text-dim);
	border-radius: 999px;
	font-size: 11.5px;
	padding: 4px 10px;
	cursor: pointer;
	transition: border-color 0.18s ease, color 0.18s ease, background 0.18s ease;
}

.mrec-chip:hover {
	border-color: rgba(240, 205, 114, 0.45);
	color: var(--ext-gold-light);
}

.mrec-chip.on {
	border-color: var(--ext-gold);
	background: var(--ext-gold-soft);
	color: var(--ext-gold-light);
}

.mrec-run {
	width: 100%;
	margin-top: 12px;
	border: 1px solid var(--ext-gold);
	background: var(--ext-gold-soft);
	color: var(--ext-gold-light);
	border-radius: 10px;
	font-size: 13px;
	padding: 9px 12px;
	cursor: pointer;
}

.mrec-run:disabled {
	opacity: 0.6;
	cursor: default;
}

.mrec-tip {
	margin: 8px 0 0;
	font-size: 11px;
	color: var(--ext-text-mute);
	line-height: 1.7;
}

.mrec-error {
	margin: 8px 0 0;
	font-size: 11.5px;
	color: #ff9a9a;
}

.mrec-presets {
	margin-top: 16px;
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
}

.mrec-presets .mrec-field-label {
	width: 100%;
}

.mrec-summary {
	margin: 0 0 10px;
	font-size: 12px;
	color: var(--ext-text-dim);
	line-height: 1.8;
}

.mrec-axis {
	padding: 7px 0;
	border-top: 1px dashed var(--ext-line);
}

.mrec-axis-head {
	display: flex;
	justify-content: space-between;
	gap: 8px;
	font-size: 12px;
}

.mrec-axis-name {
	color: var(--ext-text-mute);
}

.mrec-axis-value {
	color: var(--ext-gold-light);
}

.mrec-axis-hits {
	margin-top: 3px;
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.mrec-main {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.mrec-main-head {
	display: flex;
	align-items: baseline;
	gap: 10px;
}

.mrec-main-title {
	font-size: 13.5px;
	color: var(--ext-text);
}

.mrec-main-sub {
	font-size: 11px;
	color: var(--ext-text-mute);
}

.mrec-list {
	display: flex;
	flex-direction: column;
	gap: 9px;
}

.mrec-card {
	text-align: left;
	border: 1px solid var(--ext-line);
	background: linear-gradient(150deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.02));
	border-radius: 12px;
	padding: 12px 13px;
	cursor: pointer;
	transition: border-color 0.18s ease, background 0.18s ease, transform 0.18s ease;
}

.mrec-card:hover {
	border-color: rgba(240, 205, 114, 0.45);
	transform: translateY(-1px);
}

.mrec-card.on {
	border-color: var(--ext-gold);
	background: var(--ext-gold-soft);
}

.mrec-card-top {
	display: flex;
	align-items: baseline;
	gap: 8px;
}

.mrec-rank {
	width: 18px;
	height: 18px;
	line-height: 18px;
	text-align: center;
	border-radius: 6px;
	background: rgba(255, 255, 255, 0.08);
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
	color: var(--ext-text-dim);
}

.mrec-name {
	flex: 1;
	font-size: 13.5px;
	color: var(--ext-text);
}

.mrec-score {
	font-family: var(--ext-font-mono);
	font-size: 12px;
	color: var(--ext-gold-light);
}

.mrec-card-meta {
	margin-top: 5px;
	display: flex;
	flex-wrap: wrap;
	gap: 5px;
	font-size: 11px;
	color: var(--ext-text-mute);
}

.mrec-card-meta i {
	font-style: normal;
	opacity: 0.5;
}

.mrec-card-tags {
	margin-top: 8px;
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
}

.mrec-badge {
	border: 1px solid var(--ext-line);
	border-radius: 999px;
	font-size: 10.5px;
	padding: 2px 9px;
	color: var(--ext-text-dim);
}

.mrec-badge.grade-A {
	border-color: rgba(126, 214, 165, 0.45);
	color: #7ed6a5;
}

.mrec-badge.grade-B {
	border-color: rgba(240, 205, 114, 0.45);
	color: var(--ext-gold-light);
}

.mrec-badge.grade-C {
	border-color: rgba(167, 139, 250, 0.5);
	color: var(--ext-neon-violet);
}

.mrec-card-reason {
	margin-top: 8px;
	font-size: 11.5px;
	color: var(--ext-text-dim);
	line-height: 1.7;
}

.mrec-preview-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
	margin-bottom: 8px;
}

.mrec-preview-name {
	font-size: 12.5px;
	color: var(--ext-text);
}

.mrec-stage {
	border: 1px solid var(--ext-line);
	border-radius: 12px;
	overflow: hidden;
	background: #08080c;
}

.mrec-axes {
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
	margin-bottom: 9px;
}

.mrec-axis-tag {
	border: 1px solid var(--ext-line);
	border-radius: 999px;
	font-size: 10.5px;
	padding: 3px 9px;
	color: var(--ext-gold-light);
	background: var(--ext-gold-soft);
}

.mrec-reasons {
	margin: 0;
	padding-left: 16px;
	display: flex;
	flex-direction: column;
	gap: 6px;
	font-size: 11.5px;
	color: var(--ext-text-dim);
	line-height: 1.7;
}

.mrec-facts {
	margin-top: 12px;
	display: flex;
	flex-direction: column;
	gap: 6px;
}

.mrec-facts div {
	display: flex;
	justify-content: space-between;
	gap: 8px;
	font-size: 11.5px;
	border-top: 1px dashed var(--ext-line);
	padding-top: 6px;
}

.mrec-facts span {
	color: var(--ext-text-mute);
}

.mrec-facts b {
	font-weight: 500;
	color: var(--ext-text);
	text-align: right;
}
</style>
