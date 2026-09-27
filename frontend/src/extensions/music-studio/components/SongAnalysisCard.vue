<script setup lang="ts">
import { computed } from 'vue'
import { starsText, TIER_META, type SongAnalysis } from '@/extensions/music-studio/types/studio'

/**
 * 曲目分析卡（Song Analysis）。
 *
 * 解析完一首曲子先回答五个问题：难不难、多快、音域多宽、要弹多久、用哪套键位。
 * 「为什么是这个难度」也摆出来（三轴命中情况），而且这张卡的星级与曲库列表上的星级
 * 出自同一条规则，不会出现两处不一致。
 */
interface Props {
	analysis: SongAnalysis | null
	loading?: boolean
	/** 加载时显示骨架而不是空卡 */
	hint?: string
}

const props = withDefaults(defineProps<Props>(), {
	loading: false,
	hint: '',
})

const emit = defineEmits<{
	/** 换一套备选键位 */
	pickProfile: [profileId: number]
}>()

const facts = computed(() => {
	const value = props.analysis
	if (!value) {
		return []
	}
	return [
		{ label: '速度', value: value.tempoBpm + ' BPM', sub: value.timeSignature },
		{ label: '音域', value: value.pitchRange, sub: value.pitchSpan + ' 个半音' },
		{ label: '预计演奏', value: value.estimatedText, sub: '按推荐方案落键' },
		{ label: '音符', value: String(value.noteCount), sub: value.chordRatio > 0 ? '和弦音 ' + Math.round(value.chordRatio * 100) + '%' : '单音为主' },
	]
})
</script>

<template>
	<div class="sa">
		<div v-if="loading && !analysis" class="sa-loading">正在分析这首曲子…</div>

		<template v-else-if="analysis">
			<div class="sa-head">
				<div class="sa-stars">
					<span class="sa-stars-text">{{ starsText(analysis.difficultyStars) }}</span>
					<span class="sa-stars-label">{{ analysis.difficultyLabel }}</span>
					<span class="sa-tier" :class="'sa-tier--' + (TIER_META[analysis.difficultyTier]?.tone ?? 'hot')">
						分层 {{ analysis.difficultyTierLabel }}
					</span>
					<span class="sa-audience">适合：{{ analysis.audience }}</span>
				</div>
				<p class="sa-hint">{{ analysis.difficultyHint }}</p>
			</div>

			<div class="sa-facts">
				<div v-for="item in facts" :key="item.label" class="sa-fact">
					<span class="sa-fact-value">{{ item.value }}</span>
					<span class="sa-fact-label">{{ item.label }}</span>
					<span class="sa-fact-sub">{{ item.sub }}</span>
				</div>
			</div>

			<div class="sa-recommend">
				<div class="sa-recommend-head">
					<span class="st-label">推荐</span>
					<span class="sa-recommend-name">{{ analysis.recommendedPresetName ?? '暂无推荐' }}</span>
				</div>
				<p class="sa-recommend-note">{{ analysis.recommendedPresetNote ?? hint }}</p>
				<div class="sa-recommend-meta">
					<span class="st-chip st-chip--hot">{{ analysis.recommendedProfileName ?? '未选出档案' }}</span>
					<span class="st-chip">落键 {{ analysis.mappedCount }}</span>
					<span v-if="analysis.unmappedCount" class="st-chip">{{ analysis.unmappedCount }} 个音超范围</span>
				</div>
			</div>

			<details class="sa-why">
				<summary>凭什么这么判（难度计分依据）</summary>
				<ul>
					<li v-for="(item, index) in analysis.reasons" :key="index" :class="{ hit: item.hit }">
						<span class="sa-dot" />
						{{ item.label }}
						<span class="sa-hit">{{ item.hit ? '计分' : '不计分' }}</span>
					</li>
				</ul>
			</details>

			<div v-if="analysis.alternatives.length" class="sa-alts">
				<div class="st-label">也可以试试</div>
				<div class="sa-alt-list">
					<button
						v-for="item in analysis.alternatives.slice(0, 3)"
						:key="item.profileId"
						class="sa-alt"
						type="button"
						@click="emit('pickProfile', item.profileId)">
						<span class="sa-alt-name">{{ item.profileName }}</span>
						<span class="sa-alt-meta">
							{{ item.keyCount }} 键 ·
							{{ item.coversAll ? '全部落键' : item.unmappedCount + ' 个音超范围' }}
						</span>
					</button>
				</div>
			</div>
		</template>

		<p v-else class="sa-empty">选一首曲子就会生成分析。</p>
	</div>
</template>

<style scoped>
.sa {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.sa-loading,
.sa-empty {
	margin: 0;
	font-size: 11.5px;
	color: var(--ext-text-mute);
}

.sa-head {
	display: flex;
	flex-direction: column;
	gap: 4px;
}

.sa-stars {
	display: flex;
	align-items: baseline;
	gap: 10px;
}

.sa-stars-text {
	font-size: 20px;
	letter-spacing: 2px;
	color: var(--ext-gold-light);
	animation: st-rise 0.3s ease both;
}

.sa-tier {
	border: 1px solid var(--st-edge);
	border-radius: 999px;
	font-size: 10px;
	padding: 1px 8px;
}

.sa-tier--ok {
	border-color: rgba(110, 222, 154, 0.45);
	color: var(--st-green);
}

.sa-tier--hot {
	border-color: rgba(240, 205, 114, 0.45);
	color: var(--ext-gold-light);
}

.sa-tier--violet {
	border-color: var(--st-violet);
	color: #d9ccff;
}

.sa-audience {
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.sa-stars-label {
	font-size: 13px;
	color: var(--ext-text);
}

.sa-hint {
	margin: 0;
	font-size: 11px;
	color: var(--ext-text-mute);
	line-height: 1.7;
}

.sa-facts {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 8px;
}

.sa-fact {
	border: 1px solid var(--st-edge);
	border-radius: 10px;
	background: var(--st-glass);
	padding: 8px 9px;
	display: flex;
	flex-direction: column;
	gap: 2px;
}

.sa-fact-value {
	font-family: var(--ext-font-mono);
	font-size: 13px;
	color: var(--ext-text);
}

.sa-fact-label {
	font-size: 10px;
	color: var(--ext-text-mute);
}

.sa-fact-sub {
	font-size: 9.5px;
	color: var(--ext-text-mute);
	opacity: 0.8;
}

.sa-recommend {
	border: 1px solid var(--st-edge-hot);
	border-radius: 12px;
	background: linear-gradient(160deg, rgba(240, 205, 114, 0.14), rgba(240, 205, 114, 0.04));
	padding: 10px 12px;
}

.sa-recommend-head {
	display: flex;
	align-items: baseline;
	gap: 8px;
}

.sa-recommend-name {
	font-size: 14px;
	color: var(--ext-gold-light);
}

.sa-recommend-note {
	margin: 4px 0 0;
	font-size: 11px;
	color: var(--ext-text-dim);
	line-height: 1.7;
}

.sa-recommend-meta {
	margin-top: 7px;
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
}

.sa-why {
	border-top: 1px dashed var(--st-edge);
	padding-top: 8px;
}

.sa-why summary {
	cursor: pointer;
	font-size: 11px;
	color: var(--ext-text-mute);
}

.sa-why ul {
	margin: 8px 0 0;
	padding: 0;
	list-style: none;
	display: flex;
	flex-direction: column;
	gap: 5px;
}

.sa-why li {
	display: flex;
	align-items: center;
	gap: 7px;
	font-size: 11px;
	color: var(--ext-text-mute);
}

.sa-why li.hit {
	color: var(--ext-text-dim);
}

.sa-dot {
	width: 6px;
	height: 6px;
	border-radius: 50%;
	background: rgba(255, 255, 255, 0.18);
	flex: none;
}

.sa-why li.hit .sa-dot {
	background: var(--ext-gold-light);
}

.sa-hit {
	margin-left: auto;
	font-family: var(--ext-font-mono);
	font-size: 9.5px;
	opacity: 0.75;
}

.sa-alts {
	display: flex;
	flex-direction: column;
	gap: 6px;
}

.sa-alt-list {
	display: flex;
	flex-direction: column;
	gap: 6px;
}

.sa-alt {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
	gap: 8px;
	border: 1px solid var(--st-edge);
	border-radius: 10px;
	background: var(--st-glass);
	padding: 7px 10px;
	cursor: pointer;
	text-align: left;
	transition: border-color 0.18s ease, transform 0.18s ease;
}

.sa-alt:hover {
	border-color: var(--st-edge-hot);
	transform: translateY(-1px);
}

.sa-alt-name {
	font-size: 12px;
	color: var(--ext-text);
}

.sa-alt-meta {
	font-size: 10px;
	color: var(--ext-text-mute);
}

@media (max-width: 1200px) {
	.sa-facts {
		grid-template-columns: repeat(2, 1fr);
	}
}
</style>
