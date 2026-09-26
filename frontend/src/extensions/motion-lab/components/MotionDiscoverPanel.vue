<script setup lang="ts">
import { computed } from 'vue'
import type { FacetOption, MotionFacets, MotionRecipe, TemplateQuery } from '@/extensions/motion-lab/types/workbench'
import { RUNTIME_TIER_META } from '@/extensions/motion-lab/types/workbench'

interface Props {
	facets: MotionFacets | null
	recipes: MotionRecipe[]
	query: TemplateQuery
	activeRecipe: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
	/** 选择某个发现方式：按字段名与值筛选（再点一次取消） */
	select: [field: 'scene' | 'style' | 'technology' | 'category' | 'difficulty' | 'runtimeTier', value: string]
	recipe: [recipeKey: string]
	clear: []
}>()

const groups = computed(() => {
	const facets = props.facets
	if (!facets) {
		return []
	}
	return [
		{ field: 'scene' as const, title: '按场景', options: facets.scenes, active: props.query.scene },
		{ field: 'style' as const, title: '按风格', options: facets.styles, active: props.query.style },
		{ field: 'technology' as const, title: '按技术', options: facets.technologies, active: props.query.technology },
		{ field: 'category' as const, title: '按分组', options: facets.categories, active: props.query.category },
	]
})

function isActive(field: string, option: FacetOption) {
	return props.query[field as 'scene'] === option.value
}

function difficultyActive(value: string) {
	return String(props.query.difficulty ?? '') === value
}
</script>

<template>
	<div class="discover">
		<div v-if="facets" class="head">
			<span class="total">{{ facets.total }} 个模板 · {{ facets.recipeTotal }} 个组合方案</span>
			<button v-if="query.scene || query.style || query.technology || query.category || query.difficulty" class="reset" type="button" @click="emit('clear')">
				清除筛选
			</button>
		</div>

		<div v-for="group in groups" :key="group.field" class="group">
			<div class="group-title">{{ group.title }}</div>
			<div class="options">
				<button
					v-for="option in group.options"
					:key="option.value"
					class="option"
					:class="{ on: isActive(group.field, option) }"
					type="button"
					:title="`平均推荐指数 ${option.averageScore}`"
					@click="emit('select', group.field, option.value)">
					<span class="name">{{ option.label }}</span>
					<span class="count">{{ option.count }}</span>
				</button>
			</div>
		</div>

		<div v-if="facets" class="group">
			<div class="group-title">按性能成本</div>
			<div class="options">
				<button
					v-for="option in facets.runtimeTiers"
					:key="option.value"
					class="option"
					:class="{ on: query.runtimeTier === option.value }"
					type="button"
					:title="RUNTIME_TIER_META[option.value]?.short ?? ''"
					@click="emit('select', 'runtimeTier', option.value)">
					<span class="name">{{ RUNTIME_TIER_META[option.value]?.label ?? option.label }}</span>
					<span class="count">{{ option.count }}</span>
				</button>
			</div>
		</div>

		<div v-if="facets" class="group">
			<div class="group-title">按难度</div>
			<div class="options">
				<button
					v-for="option in facets.difficulties"
					:key="option.value"
					class="option"
					:class="{ on: difficultyActive(option.value) }"
					type="button"
					@click="emit('select', 'difficulty', option.value)">
					<span class="name">{{ option.label }}</span>
					<span class="count">{{ option.count }}</span>
				</button>
			</div>
		</div>

		<div v-if="recipes.length" class="group">
			<div class="group-title">组合方案（多个动效叠出来的效果）</div>
			<div class="recipes">
				<button
					v-for="recipe in recipes"
					:key="recipe.recipeKey"
					class="recipe"
					:class="{ on: activeRecipe === recipe.recipeKey }"
					type="button"
					:title="recipe.description"
					@click="emit('recipe', recipe.recipeKey)">
					<div class="recipe-top">
						<span class="name">{{ recipe.name }}</span>
						<span class="score">{{ recipe.grade }} {{ recipe.score }}</span>
					</div>
					<div class="recipe-sub">{{ recipe.sceneLabel }} · {{ recipe.memberKeys.length }} 个动效</div>
				</button>
			</div>
		</div>
	</div>
</template>

<style scoped>
.discover {
	display: flex;
	flex-direction: column;
	gap: 14px;
}

.head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
}

.total {
	font-size: 11.5px;
	color: var(--ext-text-mute);
}

.reset {
	border: 0;
	background: transparent;
	color: var(--ext-gold);
	font-size: 11.5px;
	cursor: pointer;
	padding: 0;
}

.group-title {
	font-size: 11.5px;
	color: var(--ext-text-mute);
	margin-bottom: 7px;
	letter-spacing: 0.02em;
}

.options {
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
}

.option {
	display: flex;
	align-items: center;
	gap: 6px;
	border: 1px solid var(--ext-line);
	background: rgba(255, 255, 255, 0.03);
	color: var(--ext-text-dim);
	border-radius: 999px;
	font-size: 11.5px;
	padding: 4px 10px;
	cursor: pointer;
	transition: border-color 0.18s ease, color 0.18s ease, background 0.18s ease;
}

.option:hover {
	border-color: rgba(240, 205, 114, 0.45);
	color: var(--ext-gold-light);
}

.option.on {
	border-color: var(--ext-gold);
	background: var(--ext-gold-soft);
	color: var(--ext-gold-light);
}

.option .count {
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-text-mute);
}

.option.on .count {
	color: var(--ext-gold-light);
}

.recipes {
	display: flex;
	flex-direction: column;
	gap: 6px;
}

.recipe {
	text-align: left;
	border: 1px solid var(--ext-line);
	background: linear-gradient(150deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.02));
	border-radius: 12px;
	padding: 9px 11px;
	cursor: pointer;
	transition: border-color 0.18s ease, background 0.18s ease, transform 0.18s ease;
}

.recipe:hover {
	border-color: rgba(167, 139, 250, 0.5);
	transform: translateY(-1px);
}

.recipe.on {
	border-color: var(--ext-neon-violet);
	background: rgba(167, 139, 250, 0.12);
}

.recipe-top {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
	gap: 8px;
}

.recipe .name {
	font-size: 12.5px;
	color: var(--ext-text);
}

.recipe .score {
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
	color: var(--ext-neon-violet);
}

.recipe-sub {
	margin-top: 3px;
	font-size: 10.5px;
	color: var(--ext-text-mute);
}
</style>
