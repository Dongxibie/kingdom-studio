<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { PerformancePreset } from '@/extensions/music-studio/types/studio'

/**
 * 演奏方案栏（Performance Preset）。
 *
 * 同一首曲子可以有好几套打法：原版、简单版（键少）、快速版（节奏紧）。
 * 每张卡把四个旋钮摊开写清楚 —— 用哪套键、超范围怎么办、快多少、同键留多少间隔 ——
 * 点一下就切过去，编排台与导出中心都跟着变。
 */
interface Props {
	presets: PerformancePreset[]
	activeId: number | null
	/** 当前会话的档案名，用来给「另存为方案」当默认名 */
	currentProfileName?: string
	busy?: boolean
}

const props = withDefaults(defineProps<Props>(), {
	currentProfileName: '',
	busy: false,
})

const emit = defineEmits<{
	apply: [preset: PerformancePreset]
	save: [name: string]
	remove: [preset: PerformancePreset]
}>()

const saveName = ref('')
const editing = ref(false)

const active = computed(() => props.presets.find((item) => item.id === props.activeId) ?? null)

function strategyText(strategy: string | null) {
	if (!strategy) {
		return '用档案默认'
	}
	return strategy === 'SHIFT_OCTAVE' ? '超范围移八度' : strategy === 'NEAREST' ? '就近落键' : '跳过超范围音'
}

function doSave() {
	const name = saveName.value.trim()
	if (!name) {
		ElMessage.warning('给方案起个名字')
		return
	}
	emit('save', name)
	saveName.value = ''
	editing.value = false
}
</script>

<template>
	<div class="pp">
		<div class="pp-head">
			<div class="st-title" style="margin: 0">
				演奏方案
				<span class="st-sub">同一首曲子可以有好几套打法</span>
			</div>
			<button class="st-btn" type="button" @click="editing = !editing">
				{{ editing ? '取消' : '另存为方案' }}
			</button>
		</div>

		<div v-if="editing" class="pp-save">
			<input v-model="saveName" type="text" placeholder="方案名，例如「练习版」" @keydown.enter="doSave" />
			<button class="st-btn st-btn--primary" type="button" @click="doSave">保存当前设置</button>
		</div>

		<div class="pp-list">
			<button
				v-for="(preset, index) in presets"
				:key="preset.id"
				class="pp-card"
				:class="{ on: preset.id === activeId }"
				type="button"
				:disabled="busy"
				@click="emit('apply', preset)">
				<div class="pp-card-top">
					<span class="pp-index">{{ String.fromCharCode(65 + index) }}</span>
					<span class="pp-name">{{ preset.name }}</span>
					<span v-if="preset.builtin" class="pp-builtin">内置</span>
				</div>
				<div class="pp-meta">
					<span>{{ preset.profileName }}</span>
					<i>·</i>
					<span>{{ preset.keyCount ?? '—' }} 键</span>
					<i>·</i>
					<span>{{ preset.speedText }}</span>
				</div>
				<div class="pp-knobs">
					<span class="pp-knob">{{ strategyText(preset.strategy) }}</span>
					<span v-if="preset.minGapMs" class="pp-knob">间隔 {{ preset.minGapMs }}ms</span>
				</div>
				<div v-if="preset.note" class="pp-note">{{ preset.note }}</div>
				<div class="pp-actions">
					<span class="pp-apply">{{ preset.id === activeId ? '正在使用' : '用这套方案' }}</span>
					<span
						class="pp-remove"
						role="button"
						tabindex="0"
						@click.stop="emit('remove', preset)"
						@keydown.enter.stop="emit('remove', preset)">
						删除
					</span>
				</div>
			</button>
			<p v-if="!presets.length" class="pp-empty">还没有方案，保存当前设置就会有第一套。</p>
		</div>

		<p v-if="active" class="pp-hint">
			当前方案「{{ active.name }}」：{{ strategyText(active.strategy) }}，{{ active.speedText }}<span v-if="active.minGapMs">，同键间隔 {{ active.minGapMs }}ms</span>。
			生成演奏计划与导出都会按它执行。
		</p>
	</div>
</template>

<style scoped>
.pp {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.pp-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 10px;
}

.pp-save {
	display: flex;
	gap: 7px;
}

.pp-save input {
	flex: 1;
	border: 1px solid var(--st-edge);
	border-radius: 10px;
	background: rgba(255, 255, 255, 0.04);
	color: var(--ext-text);
	font-size: 12px;
	padding: 7px 10px;
	font-family: inherit;
}

.pp-save input:focus {
	outline: none;
	border-color: var(--st-edge-hot);
}

.pp-list {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
	gap: 9px;
}

.pp-card {
	display: flex;
	flex-direction: column;
	gap: 6px;
	text-align: left;
	border: 1px solid var(--st-edge);
	border-radius: 12px;
	background: linear-gradient(160deg, rgba(255, 255, 255, 0.06), rgba(255, 255, 255, 0.02));
	padding: 10px 12px;
	cursor: pointer;
	transition: border-color 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
}

.pp-card:hover:not(:disabled) {
	border-color: var(--st-edge-hot);
	transform: translateY(-2px);
}

.pp-card.on {
	border-color: var(--ext-gold);
	box-shadow: 0 0 0 1px rgba(240, 205, 114, 0.35);
	background: linear-gradient(160deg, rgba(240, 205, 114, 0.16), rgba(240, 205, 114, 0.04));
}

.pp-card:disabled {
	opacity: 0.6;
	cursor: default;
}

.pp-card-top {
	display: flex;
	align-items: baseline;
	gap: 8px;
}

.pp-index {
	width: 18px;
	height: 18px;
	line-height: 18px;
	text-align: center;
	border-radius: 6px;
	background: rgba(255, 255, 255, 0.08);
	font-family: var(--ext-font-mono);
	font-size: 10px;
	color: var(--ext-text-dim);
}

.pp-name {
	font-size: 13px;
	color: var(--ext-text);
}

.pp-builtin {
	margin-left: auto;
	font-size: 9.5px;
	color: var(--ext-text-mute);
}

.pp-meta {
	display: flex;
	flex-wrap: wrap;
	gap: 5px;
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.pp-meta i {
	font-style: normal;
	opacity: 0.5;
}

.pp-knobs {
	display: flex;
	flex-wrap: wrap;
	gap: 5px;
}

.pp-knob {
	border: 1px solid var(--st-edge);
	border-radius: 999px;
	font-size: 10px;
	padding: 2px 8px;
	color: var(--ext-text-dim);
}

.pp-note {
	font-size: 10.5px;
	color: var(--ext-text-mute);
	line-height: 1.6;
}

.pp-actions {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
	margin-top: 2px;
}

.pp-apply {
	font-size: 10.5px;
	color: var(--ext-gold-light);
}

.pp-remove {
	font-size: 10.5px;
	color: var(--ext-text-mute);
	cursor: pointer;
}

.pp-remove:hover {
	color: #ff9a9a;
}

.pp-empty {
	margin: 0;
	font-size: 11px;
	color: var(--ext-text-mute);
}

.pp-hint {
	margin: 0;
	font-size: 10.5px;
	color: var(--ext-text-mute);
	line-height: 1.7;
}
</style>
