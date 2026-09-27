<script setup lang="ts">
import { computed } from 'vue'
import { SEVERITY_META, type OptimizationFix, type OptimizationReport } from '@/extensions/music-studio/types/studio'

/**
 * 演奏优化器面板（Performance Optimizer）。
 *
 * 它不替用户做决定：每条发现都说清「在哪、为什么、怎么办」，
 * 并把「怎么办」变成方案里能直接改的参数 —— 点一下应用，方案就更新了，
 * 生成计划时会按新方案执行。
 */
interface Props {
	report: OptimizationReport | null
	loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
	loading: false,
})

const emit = defineEmits<{
	apply: [fix: OptimizationFix, label: string]
	refresh: []
}>()

const gradeTone = computed(() => {
	const report = props.report
	if (!report) {
		return 'mute'
	}
	if (report.findings.some((item) => item.severity === 'WARN')) {
		return 'warn'
	}
	return report.findings.length ? 'advice' : 'ok'
})

/** 这条建议能不能一键应用：有 fix 且至少带一个参数 */
function applicable(fix: OptimizationFix | null) {
	return Boolean(fix && (fix.minGapMs || fix.speedScale || fix.strategy || fix.profileId))
}

function fixSummary(fix: OptimizationFix) {
	const parts: string[] = []
	if (fix.minGapMs) {
		parts.push('最小间隔 ' + fix.minGapMs + 'ms')
	}
	if (fix.speedScale) {
		parts.push('速度 ×' + fix.speedScale)
	}
	if (fix.strategy) {
		parts.push('策略 ' + fix.strategy)
	}
	if (fix.profileName) {
		parts.push('档案 ' + fix.profileName)
	}
	return parts.join(' · ')
}
</script>

<template>
	<div class="opt">
		<div class="opt-head">
			<span class="opt-badge" :class="'opt-badge--' + gradeTone">
				{{ gradeTone === 'ok' ? '无需处理' : gradeTone === 'warn' ? '有建议处理项' : '可以更好' }}
			</span>
			<button class="st-btn" type="button" :disabled="loading" @click="emit('refresh')">
				{{ loading ? '检查中…' : '重新检查' }}
			</button>
		</div>

		<p v-if="report" class="opt-summary">{{ report.summary }}</p>
		<p v-if="report" class="opt-meta">
			检查了 {{ report.strokeCount }} 组按键 / {{ report.eventCount }} 条动作
		</p>

		<div v-if="report?.findings.length" class="opt-list">
			<div
				v-for="(item, index) in report.findings"
				:key="item.code + index"
				class="opt-finding"
				:class="'opt-finding--' + (SEVERITY_META[item.severity]?.tone ?? 'info')">
				<div class="opt-finding-head">
					<span class="opt-title">{{ item.title }}</span>
					<span class="opt-severity">{{ SEVERITY_META[item.severity]?.label ?? item.severity }}</span>
				</div>
				<div class="opt-detail">{{ item.detail }}</div>
				<div class="opt-suggestion">{{ item.suggestion }}</div>
				<div v-if="applicable(item.fix)" class="opt-actions">
					<button class="st-btn st-btn--primary" type="button" @click="emit('apply', item.fix as OptimizationFix, '优化版')">
						应用建议
					</button>
					<span class="opt-fix">{{ fixSummary(item.fix as OptimizationFix) }}</span>
				</div>
			</div>
		</div>

		<p v-else-if="report" class="opt-clean">没有发现问题，这份计划可以直接照着弹。</p>
		<p v-else class="opt-empty">选一首曲子与乐器档案后，这里会检查连按冲突、超范围音与密度。</p>
	</div>
</template>

<style scoped>
.opt {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.opt-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
}

.opt-badge {
	border: 1px solid var(--st-edge);
	border-radius: 999px;
	font-size: 10.5px;
	padding: 3px 10px;
	color: var(--ext-text-mute);
}

.opt-badge--ok {
	border-color: rgba(126, 214, 165, 0.45);
	color: #7ed6a5;
}

.opt-badge--warn {
	border-color: rgba(255, 154, 154, 0.45);
	color: #ff9a9a;
}

.opt-badge--advice {
	border-color: rgba(240, 205, 114, 0.45);
	color: var(--ext-gold-light);
}

.opt-summary {
	margin: 0;
	font-size: 12px;
	color: var(--ext-text-dim);
	line-height: 1.75;
}

.opt-meta {
	margin: 0;
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.opt-list {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.opt-finding {
	border: 1px solid var(--st-edge);
	border-left-width: 3px;
	border-radius: 10px;
	background: var(--st-glass);
	padding: 9px 11px;
	display: flex;
	flex-direction: column;
	gap: 5px;
	animation: st-rise 0.28s ease both;
}

.opt-finding--warn {
	border-left-color: #ff9a9a;
}

.opt-finding--advice {
	border-left-color: var(--ext-gold);
}

.opt-finding--info {
	border-left-color: rgba(255, 255, 255, 0.25);
}

.opt-finding-head {
	display: flex;
	align-items: baseline;
	justify-content: space-between;
	gap: 8px;
}

.opt-title {
	font-size: 12.5px;
	color: var(--ext-text);
}

.opt-severity {
	font-size: 10px;
	color: var(--ext-text-mute);
}

.opt-detail {
	font-size: 11px;
	color: var(--ext-text-dim);
}

.opt-suggestion {
	font-size: 11px;
	color: var(--ext-text-mute);
	line-height: 1.7;
}

.opt-actions {
	display: flex;
	align-items: center;
	gap: 9px;
	flex-wrap: wrap;
	margin-top: 2px;
}

.opt-fix {
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-gold-light);
}

.opt-clean {
	margin: 0;
	font-size: 11.5px;
	color: #7ed6a5;
}

.opt-empty {
	margin: 0;
	font-size: 11px;
	color: var(--ext-text-mute);
	line-height: 1.7;
}
</style>
