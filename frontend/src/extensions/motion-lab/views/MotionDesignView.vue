<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ExtCodeBlock, ExtEmpty, ExtShell, ExtStatusTag } from '@/extensions/_shared/components'
import { designMotions } from '@/extensions/motion-lab/api/template'
import {
	DESIGN_BUDGETS,
	DESIGN_PRESETS,
	type DesignStep,
	type MotionDesign,
} from '@/extensions/motion-lab/types/design'
import { INTENT_AXES, PERFORMANCE_GRADE_HINT } from '@/extensions/motion-lab/types/recommend'

/**
 * AI 设计（Phase 4）。
 *
 * 和「智能推荐」的区别：推荐给你的是**若干个候选**，设计给你的是**一套完整方案**
 * —— 包括选中哪套组合、每一步做什么、每个参数落地多少、为什么这么定。
 *
 * 模型在这里只做三件事：在现有组合里挑一套、在参数区间内微调、写一段说明。
 * 它不生成结构、不写代码，所以「说得漂亮但跑不起来」不会发生。没配模型时走规则设计，
 * 输出结构完全一样，来源会在右上角写明。
 */
const router = useRouter()
const query = ref(DESIGN_PRESETS[0]?.query ?? '')
const budget = ref('')
const maxSteps = ref(5)
const loading = ref(false)
const error = ref('')
const design = ref<MotionDesign | null>(null)

const intentRows = computed(() => {
	const intent = design.value?.intent
	if (!intent) {
		return []
	}
	return INTENT_AXES.map((axis) => {
		const key = axis.axis as 'scene' | 'style' | 'emotion' | 'performance' | 'interaction'
		return { axis: key, label: axis.label, value: intent.labels[key] ?? '', hits: intent.hits[key] ?? [] }
	}).filter((row) => row.value)
})

const sourceText = computed(() => {
	const value = design.value
	if (!value) {
		return '规则设计 · 未调用模型'
	}
	if (value.source === 'MODEL') {
		return `模型设计 · ${value.modelName ?? '已配置模型'}`
	}
	return '规则设计'
})

/** 参数是不是被本方案调过：调过的显示「默认 → 建议」 */
function paramLine(step: DesignStep) {
	return step.params.map((param) => ({
		label: param.label,
		text: `${param.value ?? '-'}${param.unit ?? ''}`,
		adjusted: param.adjusted,
		reason: param.reason ?? '',
		from: `${param.defaultValue ?? '-'}${param.unit ?? ''}`,
	}))
}

async function run() {
	const text = query.value.trim()
	if (!text) {
		error.value = '先写一句需求，例如「帮我设计一个科技公司首页」'
		return
	}
	loading.value = true
	error.value = ''
	try {
		design.value = await designMotions(text, budget.value, maxSteps.value)
	} catch (e) {
		error.value = e instanceof Error ? e.message : '设计失败，请稍后再试'
	} finally {
		loading.value = false
	}
}

function usePreset(preset: { query: string }) {
	query.value = preset.query
	void run()
}

/** 一键进工作台：带上方案 key，工作台会自动打开这套方案并进入组合模式 */
function openInWorkbench() {
	const recipe = design.value?.recipe
	if (!recipe) {
		return
	}
	ElMessage.success('已把「' + recipe.name + '」带到工作台')
	void router.push({ path: '/extensions/motion-lab', query: { recipe: recipe.recipeKey } })
}

onMounted(() => {
	void run()
})
</script>

<template>
	<ExtShell
		title="AI 设计"
		subtitle="MOTION LAB · 一句需求 → 完整方案（组合 + 每一步 + 参数 + 说明）"
		mark="D"
		variant="obsidian">
		<template #actions>
			<ExtStatusTag :text="sourceText" :tone="design?.source === 'MODEL' ? 'ok' : 'mute'" />
			<button class="ext-btn" type="button" @click="router.push('/extensions/motion-lab/recommend')">智能推荐</button>
			<button class="ext-btn" type="button" :disabled="loading" @click="run">
				{{ loading ? '设计中…' : '重新设计' }}
			</button>
		</template>

		<template #left>
			<div class="ext-panel">
				<div class="ext-panel-title">输入需求</div>
				<textarea
					v-model="query"
					class="mdes-input"
					rows="4"
					placeholder="例如：帮我设计一个科技公司首页 / 高级酒店官网 / 游戏官网"
					@keydown.ctrl.enter="run"></textarea>

				<div class="mdes-field">
					<span class="mdes-field-label">性能预算</span>
					<div class="mdes-chips">
						<button
							v-for="item in DESIGN_BUDGETS"
							:key="item.value"
							class="mdes-chip"
							:class="{ on: budget === item.value }"
							type="button"
							@click="budget = item.value">
							{{ item.label }}
						</button>
					</div>
				</div>

				<div class="mdes-field">
					<span class="mdes-field-label">最多用几个动效</span>
					<div class="mdes-chips">
						<button
							v-for="size in [3, 4, 5]"
							:key="size"
							class="mdes-chip"
							:class="{ on: maxSteps === size }"
							type="button"
							@click="maxSteps = size">
							{{ size }} 步
						</button>
					</div>
				</div>

				<button class="mdes-run" type="button" :disabled="loading" @click="run">
					{{ loading ? '正在设计…' : '生成设计方案' }}
				</button>
				<p class="mdes-tip">按 Ctrl + Enter 也可以直接生成</p>
				<p v-if="error" class="mdes-error">{{ error }}</p>

				<div class="mdes-presets">
					<div class="mdes-field-label">试试这些需求</div>
					<button v-for="preset in DESIGN_PRESETS" :key="preset.label" class="mdes-chip" type="button" @click="usePreset(preset)">
						{{ preset.label }}
					</button>
				</div>
			</div>

			<div v-if="design" class="ext-panel">
				<div class="ext-panel-title">识别到的需求</div>
				<p class="mdes-summary">{{ design.intent.summary }}</p>
				<div v-for="row in intentRows" :key="row.axis" class="mdes-axis">
					<div class="mdes-axis-head">
						<span class="mdes-axis-name">{{ row.label }}</span>
						<span class="mdes-axis-value">{{ row.value }}</span>
					</div>
					<div v-if="row.hits.length" class="mdes-axis-hits">命中：{{ row.hits.join(' / ') }}</div>
				</div>
				<p v-if="!intentRows.length" class="mdes-tip">这句话里没读出具象的场景或风格，方案按推荐指数与性能预算给出。</p>
			</div>
		</template>

		<div class="mdes-main">
			<div class="mdes-main-head">
				<div class="mdes-main-title">设计方案</div>
				<span v-if="design?.recipe" class="mdes-main-sub">
					「{{ design.recipe.name }}」· {{ design.animations.length }} 步
				</span>
			</div>

			<ExtEmpty
				v-if="!design"
				tag="待设计"
				title="左边写一句需求，这里给出完整方案"
				hint="方案 = 选中的组合 + 每一步的动画/作用/参数 + 整体性能成本 + 逐条说明。" />

			<ExtEmpty
				v-else-if="design.recipe && !design.animations.length"
				tag="无方案"
				title="模板库里还没有组合方案"
				hint="先执行 db/extensions_motion_recipe_seed.sql 导入 30 套组合。" />

			<template v-else-if="design.recipe">
				<div class="mdes-explanation">
					<div class="mdes-explanation-title">这套设计在表达什么</div>
					<p class="mdes-explanation-text">{{ design.explanation }}</p>
					<div class="mdes-explanation-meta">
						<span>{{ design.recipe.sceneLabel }}</span>
						<i>·</i>
						<span>{{ design.recipe.styleLabel }}</span>
						<i>·</i>
						<span>推荐指数 {{ design.recipe.score }}</span>
					</div>
				</div>

				<div class="mdes-steps">
					<div v-for="step in design.animations" :key="step.order + step.templateKey" class="mdes-step">
						<div class="mdes-step-head">
							<span class="mdes-order">{{ step.order }}</span>
							<span v-if="step.stage" class="mdes-stage">{{ step.stage }}</span>
							<span class="mdes-name">{{ step.name }}</span>
							<span class="mdes-cost" :class="'grade-' + step.performanceGrade">
								性能 {{ step.performanceGrade }} · {{ step.runtimeTierLabel }}
							</span>
						</div>
						<div v-if="step.role" class="mdes-role">{{ step.role }}</div>
						<div class="mdes-meta">{{ step.technology }} · {{ step.triggerLabel }}触发</div>
						<div v-if="step.params.length" class="mdes-params">
							<div v-for="param in paramLine(step)" :key="param.label" class="mdes-param">
								<span class="mdes-param-label">{{ param.label }}</span>
								<span class="mdes-param-value" :class="{ on: param.adjusted }">
									{{ param.adjusted ? `${param.from} → ${param.text}` : param.text }}
								</span>
								<span v-if="param.reason" class="mdes-param-reason">{{ param.reason }}</span>
							</div>
						</div>
						<div v-else class="mdes-meta">这一步没有可调参数</div>
					</div>
				</div>
			</template>
		</div>

		<template #right>
			<div v-if="design?.performance" class="ext-panel">
				<div class="ext-panel-title">整体性能成本</div>
				<div class="mdes-perf" :class="'grade-' + design.performance.grade">
					<span class="mdes-perf-grade">{{ PERFORMANCE_GRADE_HINT[design.performance.grade] ?? design.performance.grade }}</span>
					<span class="mdes-perf-tier">
						{{ design.performance.lightweight }} 步轻量 · {{ design.performance.balanced }} 步均衡 ·
						{{ design.performance.gpuEnhanced }} 步依赖 GPU 加速
					</span>
					<span class="mdes-perf-worst">最重一步：{{ design.performance.worstTierLabel }}</span>
				</div>
				<p class="mdes-tip">{{ design.performance.note }}</p>
			</div>

			<div v-if="design" class="ext-panel">
				<div class="ext-panel-title">方案是怎么来的</div>
				<ExtStatusTag :text="sourceText" :tone="design.source === 'MODEL' ? 'ok' : 'mute'" />
				<p v-if="design.fallbackReason" class="mdes-tip">{{ design.fallbackReason }}</p>
				<ul class="mdes-notes">
					<li v-for="(note, index) in design.notes" :key="index">{{ note }}</li>
				</ul>
				<button v-if="design.recipe" class="mdes-open" type="button" @click="openInWorkbench">
					在工作台里打开这套方案
				</button>
			</div>

			<div v-if="design?.recipe?.prompt" class="ext-panel">
				<div class="ext-panel-title">整套提示词</div>
				<p class="mdes-tip">想在自己的项目里复现这套设计，把下面这段直接粘给模型即可。</p>
				<ExtCodeBlock :code="design.recipe.prompt" lang="prompt" title="设计方案提示词" />
			</div>
		</template>
	</ExtShell>
</template>

<style scoped>
.mdes-input {
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

.mdes-input:focus {
	outline: none;
	border-color: rgba(240, 205, 114, 0.5);
}

.mdes-field {
	margin-top: 12px;
}

.mdes-field-label {
	display: block;
	font-size: 11.5px;
	color: var(--ext-text-mute);
	margin-bottom: 7px;
}

.mdes-chips {
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
}

.mdes-chip {
	border: 1px solid var(--ext-line);
	background: rgba(255, 255, 255, 0.03);
	color: var(--ext-text-dim);
	border-radius: 999px;
	font-size: 11.5px;
	padding: 4px 10px;
	cursor: pointer;
	transition: border-color 0.18s ease, color 0.18s ease, background 0.18s ease;
}

.mdes-chip:hover {
	border-color: rgba(240, 205, 114, 0.45);
	color: var(--ext-gold-light);
}

.mdes-chip.on {
	border-color: var(--ext-gold);
	background: var(--ext-gold-soft);
	color: var(--ext-gold-light);
}

.mdes-run {
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

.mdes-run:disabled {
	opacity: 0.6;
	cursor: default;
}

.mdes-tip {
	margin: 8px 0 0;
	font-size: 11px;
	color: var(--ext-text-mute);
	line-height: 1.7;
}

.mdes-error {
	margin: 8px 0 0;
	font-size: 11.5px;
	color: #ff9a9a;
}

.mdes-presets {
	margin-top: 16px;
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
}

.mdes-presets .mdes-field-label {
	width: 100%;
}

.mdes-summary {
	margin: 0 0 10px;
	font-size: 12px;
	color: var(--ext-text-dim);
	line-height: 1.8;
}

.mdes-axis {
	padding: 7px 0;
	border-top: 1px dashed var(--ext-line);
}

.mdes-axis-head {
	display: flex;
	justify-content: space-between;
	gap: 8px;
	font-size: 12px;
}

.mdes-axis-name {
	color: var(--ext-text-mute);
}

.mdes-axis-value {
	color: var(--ext-gold-light);
}

.mdes-axis-hits {
	margin-top: 3px;
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.mdes-main {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.mdes-main-head {
	display: flex;
	align-items: baseline;
	gap: 10px;
}

.mdes-main-title {
	font-size: 13.5px;
	color: var(--ext-text);
}

.mdes-main-sub {
	font-size: 11px;
	color: var(--ext-text-mute);
}

.mdes-explanation {
	border: 1px solid rgba(167, 139, 250, 0.35);
	background: rgba(167, 139, 250, 0.08);
	border-radius: 12px;
	padding: 11px 13px;
}

.mdes-explanation-title {
	font-size: 11.5px;
	color: var(--ext-neon-violet);
	margin-bottom: 6px;
}

.mdes-explanation-text {
	margin: 0;
	font-size: 13px;
	color: var(--ext-text);
	line-height: 1.8;
}

.mdes-explanation-meta {
	margin-top: 8px;
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
	font-size: 11px;
	color: var(--ext-text-mute);
}

.mdes-explanation-meta i {
	font-style: normal;
	opacity: 0.5;
}

.mdes-steps {
	display: flex;
	flex-direction: column;
	gap: 9px;
}

.mdes-step {
	border: 1px solid var(--ext-line);
	border-radius: 12px;
	padding: 11px 13px;
	background: linear-gradient(150deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.02));
}

.mdes-step-head {
	display: flex;
	align-items: baseline;
	flex-wrap: wrap;
	gap: 8px;
}

.mdes-order {
	width: 20px;
	height: 20px;
	line-height: 20px;
	text-align: center;
	border-radius: 6px;
	background: rgba(255, 255, 255, 0.08);
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
	color: var(--ext-text-dim);
}

.mdes-stage {
	border: 1px solid var(--ext-line);
	border-radius: 999px;
	font-size: 10px;
	padding: 1px 7px;
	color: var(--ext-text-mute);
}

.mdes-name {
	font-size: 13px;
	color: var(--ext-text);
}

.mdes-cost {
	margin-left: auto;
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.mdes-cost.grade-A {
	color: #7ed6a5;
}

.mdes-cost.grade-B {
	color: var(--ext-gold-light);
}

.mdes-cost.grade-C {
	color: var(--ext-neon-violet);
}

.mdes-role {
	margin-top: 6px;
	font-size: 12px;
	color: var(--ext-text-dim);
	line-height: 1.75;
}

.mdes-meta {
	margin-top: 5px;
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.mdes-params {
	margin-top: 8px;
	display: flex;
	flex-direction: column;
	gap: 5px;
	border-top: 1px dashed var(--ext-line);
	padding-top: 8px;
}

.mdes-param {
	display: flex;
	flex-wrap: wrap;
	align-items: baseline;
	gap: 7px;
	font-size: 11px;
}

.mdes-param-label {
	color: var(--ext-text-mute);
	min-width: 56px;
}

.mdes-param-value {
	font-family: var(--ext-font-mono);
	color: var(--ext-text-dim);
}

.mdes-param-value.on {
	color: var(--ext-gold-light);
}

.mdes-param-reason {
	color: var(--ext-text-mute);
	font-size: 10.5px;
}

.mdes-perf {
	display: flex;
	flex-direction: column;
	gap: 5px;
	border: 1px solid var(--ext-line);
	border-radius: 10px;
	padding: 9px 11px;
	font-size: 11.5px;
	color: var(--ext-text-dim);
}

.mdes-perf.grade-A {
	border-color: rgba(126, 214, 165, 0.35);
}

.mdes-perf.grade-B {
	border-color: rgba(240, 205, 114, 0.35);
}

.mdes-perf.grade-C {
	border-color: rgba(167, 139, 250, 0.4);
}

.mdes-perf-grade {
	color: var(--ext-text);
}

.mdes-perf-tier,
.mdes-perf-worst {
	color: var(--ext-text-mute);
	font-size: 11px;
}

.mdes-notes {
	margin: 10px 0 0;
	padding-left: 16px;
	display: flex;
	flex-direction: column;
	gap: 6px;
	font-size: 11.5px;
	color: var(--ext-text-dim);
	line-height: 1.7;
}

.mdes-open {
	margin-top: 12px;
	width: 100%;
	border: 1px solid var(--ext-gold);
	background: var(--ext-gold-soft);
	color: var(--ext-gold-light);
	border-radius: 10px;
	font-size: 12.5px;
	padding: 8px 12px;
	cursor: pointer;
}
</style>
