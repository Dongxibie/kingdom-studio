<script setup lang="ts">
import { computed } from 'vue'
import type { MotionRecipe, RecipeStep } from '@/extensions/motion-lab/types/workbench'

/**
 * 组合模式：把一套方案拆成看得懂的步骤。
 *
 * 单个动效是素材，组合才是答案 —— 但「组合」必须能被检查：
 * 每一步用哪个动画、负责哪一层、作用是什么、有什么可调、代价多大，都在这一列里。
 * 点某一步就把中间舞台切到那一步，配合「依次预览」可以按顺序把整页效果走一遍。
 */
interface Props {
	recipe: MotionRecipe | null
	/** 当前预览的模板 key：用来高亮正在看的那一步 */
	activeKey: string
	/** 依次预览是否在跑 */
	autoPlaying: boolean
	/** 依次预览当前停在第几步（从 1 开始） */
	autoStep: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
	pick: [templateKey: string]
	toggleAuto: []
}>()

/** 方案里的步骤；模型临时给的方案没有 steps，用成员清单兜底 */
const steps = computed<RecipeStep[]>(() => {
	const recipe = props.recipe
	if (!recipe) {
		return []
	}
	if (recipe.steps?.length) {
		return recipe.steps
	}
	return recipe.members.map((member, index) => ({
		order: index + 1,
		templateKey: member.templateKey,
		name: member.name,
		nameEn: member.nameEn,
		stage: '',
		role: member.description,
		description: member.description,
		category: member.category,
		technology: member.technology,
		triggerType: member.triggerType ?? 'load',
		triggerLabel: member.triggerLabel ?? '加载时',
		runtimeTier: member.runtimeTier,
		runtimeTierLabel: member.runtimeTierLabel,
		performanceGrade: member.runtimeTier === 'LIGHTWEIGHT' ? 'A' : member.runtimeTier === 'BALANCED' ? 'B' : 'C',
		score: member.score,
		stars: member.stars,
		params: [],
		previewReady: true,
	}))
})

const performance = computed(() => props.recipe?.performance ?? null)

/** 整体性能等级：优先用后端算出的，没有就按最重的一步自己判 */
const grade = computed(() => {
	if (performance.value) {
		return performance.value.grade
	}
	const tiers = steps.value.map((step) => step.runtimeTier)
	if (tiers.includes('GPU_ENHANCED')) {
		return 'C'
	}
	if (tiers.includes('BALANCED')) {
		return 'B'
	}
	return 'A'
})

const tierLine = computed(() => {
	const item = performance.value
	if (!item) {
		return ''
	}
	return `${item.lightweight} 步轻量 · ${item.balanced} 步均衡 · ${item.gpuEnhanced} 步依赖 GPU 加速`
})

const GRADE_HINT: Record<string, string> = {
	A: 'A 级 · 整套都是轻量动效',
	B: 'B 级 · 桌面无压力，低端移动端注意数量',
	C: 'C 级 · 有步骤依赖 GPU 加速',
}

function stepClass(step: RecipeStep) {
	return {
		on: props.activeKey === step.templateKey,
		'auto-on': props.autoPlaying && props.autoStep === step.order,
	}
}
</script>

<template>
	<div v-if="recipe" class="steps">
		<div class="head">
			<div class="head-left">
				<span class="title">组合模式「{{ recipe.name }}」</span>
				<span class="count">{{ steps.length }} 步</span>
			</div>
			<button class="auto" type="button" :disabled="!steps.length" @click="emit('toggleAuto')">
				{{ autoPlaying ? '停止依次预览' : '依次预览' }}
			</button>
		</div>

		<div class="perf" :class="'grade-' + grade">
			<span class="perf-grade">性能 {{ GRADE_HINT[grade] ?? grade }}</span>
			<span v-if="tierLine" class="perf-tier">{{ tierLine }}</span>
			<span v-if="performance?.worstTierLabel" class="perf-worst">最重一步：{{ performance.worstTierLabel }}</span>
		</div>
		<p v-if="performance?.note" class="perf-note">{{ performance.note }}</p>

		<div class="list">
			<button
				v-for="step in steps"
				:key="step.templateKey + step.order"
				class="step"
				:class="stepClass(step)"
				type="button"
				@click="emit('pick', step.templateKey)">
				<span class="order">{{ step.order }}</span>
				<span class="body">
					<span class="row-1">
						<span v-if="step.stage" class="stage">{{ step.stage }}</span>
						<span class="name">{{ step.name }}</span>
						<span class="meta">{{ step.technology }} · {{ step.triggerLabel }} · {{ step.score }} 分</span>
					</span>
					<span v-if="step.role" class="role">{{ step.role }}</span>
					<span class="row-3">
						<span class="cost" :class="'grade-' + step.performanceGrade">性能 {{ step.performanceGrade }}</span>
						<span class="tier">{{ step.runtimeTierLabel }}</span>
						<span v-if="step.params.length" class="params">{{ step.params.length }} 个可调参数</span>
						<span v-else class="params">无参数</span>
					</span>
				</span>
			</button>
		</div>

		<div v-if="recipe.description" class="why">{{ recipe.description }}</div>
		<div v-if="recipe.prompt" class="prompt">{{ recipe.prompt }}</div>
	</div>
</template>

<style scoped>
.steps {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
}

.head-left {
	display: flex;
	align-items: baseline;
	gap: 8px;
}

.title {
	font-size: 13px;
	color: var(--ext-text);
}

.count {
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.auto {
	border: 1px solid var(--ext-line);
	background: rgba(255, 255, 255, 0.03);
	color: var(--ext-text-dim);
	border-radius: 999px;
	font-size: 11px;
	padding: 3px 11px;
	cursor: pointer;
}

.auto:hover {
	border-color: rgba(240, 205, 114, 0.45);
	color: var(--ext-gold-light);
}

.auto:disabled {
	opacity: 0.5;
	cursor: default;
}

.perf {
	display: flex;
	flex-wrap: wrap;
	gap: 8px;
	align-items: center;
	border: 1px solid var(--ext-line);
	border-radius: 10px;
	padding: 7px 10px;
	font-size: 11px;
	color: var(--ext-text-dim);
}

.perf.grade-A {
	border-color: rgba(126, 214, 165, 0.35);
}

.perf.grade-B {
	border-color: rgba(240, 205, 114, 0.35);
}

.perf.grade-C {
	border-color: rgba(167, 139, 250, 0.4);
}

.perf-grade {
	color: var(--ext-text);
}

.perf-tier,
.perf-worst {
	color: var(--ext-text-mute);
}

.perf-note {
	margin: 0;
	font-size: 11px;
	color: var(--ext-text-mute);
	line-height: 1.7;
}

.list {
	display: flex;
	flex-direction: column;
	gap: 6px;
}

.step {
	display: flex;
	gap: 9px;
	text-align: left;
	border: 1px solid var(--ext-line);
	background: linear-gradient(150deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.02));
	border-radius: 11px;
	padding: 9px 11px;
	cursor: pointer;
	transition: border-color 0.18s ease, background 0.18s ease, transform 0.18s ease;
}

.step:hover {
	border-color: rgba(240, 205, 114, 0.4);
	transform: translateY(-1px);
}

.step.on {
	border-color: var(--ext-gold);
	background: var(--ext-gold-soft);
}

.step.auto-on {
	box-shadow: 0 0 0 1px rgba(167, 139, 250, 0.5);
}

.order {
	width: 20px;
	height: 20px;
	line-height: 20px;
	text-align: center;
	border-radius: 6px;
	background: rgba(255, 255, 255, 0.08);
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
	color: var(--ext-text-dim);
	flex: none;
}

.body {
	display: flex;
	flex-direction: column;
	gap: 4px;
	min-width: 0;
	flex: 1;
}

.row-1 {
	display: flex;
	align-items: baseline;
	flex-wrap: wrap;
	gap: 7px;
}

.stage {
	border: 1px solid var(--ext-line);
	border-radius: 999px;
	font-size: 10px;
	padding: 1px 7px;
	color: var(--ext-text-mute);
}

.name {
	font-size: 12.5px;
	color: var(--ext-text);
}

.meta {
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-text-mute);
}

.role {
	font-size: 11.5px;
	color: var(--ext-text-dim);
	line-height: 1.65;
}

.row-3 {
	display: flex;
	flex-wrap: wrap;
	gap: 7px;
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.cost.grade-A {
	color: #7ed6a5;
}

.cost.grade-B {
	color: var(--ext-gold-light);
}

.cost.grade-C {
	color: var(--ext-neon-violet);
}

.why {
	font-size: 11.5px;
	color: var(--ext-text-dim);
	line-height: 1.7;
}

.prompt {
	font-size: 11px;
	color: var(--ext-text-mute);
	line-height: 1.7;
	border-left: 2px solid var(--ext-line);
	padding-left: 9px;
}
</style>
