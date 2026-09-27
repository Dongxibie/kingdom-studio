<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createInstrument, deleteInstrument, matchProfiles, updateInstrument } from '@/extensions/music-agent/api/music'
import { useStudioSession } from '@/extensions/music-studio/composables/useStudioSession'
import type { ProfileMatch } from '@/extensions/music-studio/types/studio'
import type { InstrumentProfilePayload } from '@/extensions/music-agent/types/music'

/**
 * 游戏乐器（Game Instrument Profile）。
 *
 * 「乐器配置」在这一版升级成「游戏乐器档案」：同一套键位在不同游戏里是两回事，
 * 所以档案带上游戏名、覆盖音域与特殊规则，并且能按 游戏 → 乐器 → 键数 去匹配这首曲子
 * 到底能用哪一套。
 *
 * 匹配结果不是查表来的：后端会把每套档案真的跑一遍映射，所以这里能直接看到
 * 「能不能全落下、漏几个音」，以及为什么推荐它。
 */
const emit = defineEmits<{
	/** 换了档案：外层重新映射与刷新洞察 */
	picked: [profileId: number]
}>()

const { state, setProfile, loadProfiles, loadPresets, savePreset } = useStudioSession()

const game = ref('')
const instrument = ref('')
const keyCount = ref<number | null>(15)
const matches = ref<ProfileMatch[]>([])
const matching = ref(false)

const editorOpen = ref(false)
const editingId = ref<number | null>(null)
const form = ref<InstrumentProfilePayload>({
	name: '',
	game: '自定义',
	instrument: '',
	mappingMode: 'DIATONIC',
	scale: 'MAJOR',
	keyLayout: [],
	basePitch: 60,
	transpose: 0,
	octaveShift: 0,
	unmappedStrategy: 'NEAREST',
	octaveRange: '',
	description: '',
	specialRules: '',
	status: 'READY',
})

const layoutText = ref('')

const games = computed(() => {
	const set = new Set<string>()
	state.profiles.forEach((profile) => set.add(profile.game || '通用'))
	return [...set]
})

const instruments = computed(() => {
	const set = new Set<string>()
	state.profiles
		.filter((profile) => !game.value || (profile.game || '通用') === game.value)
		.forEach((profile) => set.add(profile.instrument || '未标注'))
	return [...set]
})

async function runMatch() {
	if (!state.task) {
		ElMessage.warning('先选一首曲子')
		return
	}
	matching.value = true
	try {
		matches.value = await matchProfiles(
			state.task.id,
			game.value || undefined,
			instrument.value || undefined,
			keyCount.value,
		)
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '匹配失败')
	} finally {
		matching.value = false
	}
}

/** 一键采用这套键位：换档案，并把当前设置存成方案 */
async function useProfile(match: ProfileMatch) {
	await setProfile(match.profileId)
	await savePreset(game.value ? game.value + ' · ' + match.profileName : match.profileName)
	await runMatch()
	emit('picked', match.profileId)
}

function openEditor(match?: ProfileMatch) {
	if (match) {
		editingId.value = match.profileId
		const profile = state.profiles.find((item) => item.id === match.profileId)
		if (profile) {
			form.value = {
				name: profile.name,
				game: profile.game || '自定义',
				instrument: profile.instrument,
				mappingMode: profile.mappingMode,
				scale: profile.scale,
				keyLayout: profile.keyLayout,
				basePitch: profile.basePitch,
				transpose: profile.transpose,
				octaveShift: profile.octaveShift,
				unmappedStrategy: profile.unmappedStrategy,
				octaveRange: profile.octaveRange ?? '',
				description: profile.description,
				specialRules: profile.specialRules ?? '',
				status: profile.status,
			}
			layoutText.value = profile.keyLayout.join(',')
		}
	} else {
		editingId.value = null
		layoutText.value = 'Z,X,C,V,B,N,M,A,S,D,F,G,H,J,K'
		form.value = {
			name: '',
			game: '自定义',
			instrument: '',
			mappingMode: 'DIATONIC',
			scale: 'MAJOR',
			keyLayout: [],
			basePitch: 60,
			transpose: 0,
			octaveShift: 0,
			unmappedStrategy: 'NEAREST',
			octaveRange: 'C4–C6',
			description: '',
			specialRules: '',
			status: 'READY',
		}
	}
	editorOpen.value = true
}

async function saveProfile() {
	form.value.keyLayout = layoutText.value
		.split(',')
		.map((item) => item.trim())
		.filter(Boolean)
	if (!form.value.name.trim() || !form.value.keyLayout.length) {
		ElMessage.warning('至少要有名称与键位')
		return
	}
	try {
		if (editingId.value) {
			await updateInstrument(editingId.value, form.value)
		} else {
			await createInstrument(form.value)
		}
		await loadProfiles()
		editorOpen.value = false
		ElMessage.success(editingId.value ? '已更新档案' : '已新建档案')
		await runMatch()
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '保存档案失败')
	}
}

async function removeProfile(match: ProfileMatch) {
	try {
		await deleteInstrument(match.profileId)
		await loadProfiles()
		ElMessage.success('已删除档案「' + match.profileName + '」')
		await runMatch()
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '删除档案失败')
	}
}

watch(game, () => {
	instrument.value = ''
})

onMounted(async () => {
	await loadProfiles()
	if (state.task) {
		await runMatch()
	}
})

void loadPresets
</script>

<template>
	<div class="gi">
		<div class="gi-filters">
			<label class="gi-field">
				<span class="st-label">游戏</span>
				<select v-model="game">
					<option value="">不限</option>
					<option v-for="item in games" :key="item" :value="item">{{ item }}</option>
				</select>
			</label>
			<label class="gi-field">
				<span class="st-label">乐器</span>
				<select v-model="instrument">
					<option value="">不限</option>
					<option v-for="item in instruments" :key="item" :value="item">{{ item }}</option>
				</select>
			</label>
			<label class="gi-field gi-field--narrow">
				<span class="st-label">键数</span>
				<input v-model.number="keyCount" type="number" min="1" max="88" />
			</label>
		</div>

		<div class="gi-actions">
			<button class="st-btn st-btn--primary" type="button" :disabled="matching || !state.task" @click="runMatch">
				{{ matching ? '匹配中…' : '匹配游戏乐器' }}
			</button>
			<button class="st-btn" type="button" @click="openEditor()">新建档案</button>
		</div>

		<div class="gi-list">
			<div v-for="match in matches" :key="match.profileId" class="gi-card" :class="{ on: state.profileId === match.profileId }">
				<div class="gi-card-head">
					<span class="gi-game">{{ match.game || '通用' }}</span>
					<span class="gi-name">{{ match.profileName }}</span>
					<span class="gi-keys">{{ match.keyCount }} 键</span>
				</div>
				<div class="gi-meta">
					<span>{{ match.instrument }}</span>
					<i>·</i>
					<span>{{ match.octaveRange || '音域未标注' }}</span>
					<i>·</i>
					<span :class="match.coversAll ? 'gi-ok' : 'gi-warn'">
						{{ match.coversAll ? '全部可落键' : match.unmappedCount + ' 个音超范围' }}
					</span>
				</div>
				<ul class="gi-reasons">
					<li v-for="(reason, index) in match.reasons" :key="index">{{ reason }}</li>
				</ul>
				<p v-if="match.specialRules" class="gi-rules">{{ match.specialRules }}</p>
				<div class="gi-card-actions">
					<button class="st-btn st-btn--primary" type="button" @click="useProfile(match)">用这套键位</button>
					<button class="st-btn" type="button" @click="openEditor(match)">编辑</button>
					<button class="st-btn" type="button" @click="removeProfile(match)">删除</button>
				</div>
			</div>
			<p v-if="!matches.length" class="gi-hint">
				点「匹配游戏乐器」看看这首曲子能用哪套键位：能全落下的排前面，键数最接近目标的次之。
			</p>
		</div>

		<div v-if="editorOpen" class="gi-editor">
			<div class="st-title">游戏乐器档案</div>
			<div class="gi-editor-grid">
				<label class="gi-field"><span class="st-label">名称</span><input v-model="form.name" type="text" /></label>
				<label class="gi-field"><span class="st-label">游戏</span><input v-model="form.game" type="text" placeholder="光遇 Sky / Minecraft / 自定义" /></label>
				<label class="gi-field"><span class="st-label">乐器</span><input v-model="form.instrument" type="text" placeholder="口风琴 / 钢琴 / 音符盒" /></label>
				<label class="gi-field"><span class="st-label">覆盖音域</span><input v-model="form.octaveRange" type="text" placeholder="C4–C6" /></label>
				<label class="gi-field"><span class="st-label">映射方式</span>
					<select v-model="form.mappingMode">
						<option value="DIATONIC">音阶排列</option>
						<option value="CHROMATIC">半音排列</option>
						<option value="CUSTOM">自定义</option>
					</select>
				</label>
				<label class="gi-field"><span class="st-label">超范围策略</span>
					<select v-model="form.unmappedStrategy">
						<option value="NEAREST">就近落键</option>
						<option value="SHIFT_OCTAVE">移八度</option>
						<option value="SKIP">跳过</option>
					</select>
				</label>
				<label class="gi-field"><span class="st-label">最低音（MIDI）</span><input v-model.number="form.basePitch" type="number" min="0" max="127" /></label>
				<label class="gi-field"><span class="st-label">移调（半音）</span><input v-model.number="form.transpose" type="number" min="-24" max="24" /></label>
			</div>
			<label class="gi-field gi-field--wide">
				<span class="st-label">键位（从低音到高音，逗号分隔）</span>
				<textarea v-model="layoutText" rows="2" placeholder="Z,X,C,V,B,N,M,A,S,D,F,G,H,J,K"></textarea>
			</label>
			<label class="gi-field gi-field--wide">
				<span class="st-label">说明</span>
				<textarea v-model="form.description" rows="2" placeholder="这套键位在这个游戏里怎么用"></textarea>
			</label>
			<label class="gi-field gi-field--wide">
				<span class="st-label">特殊规则</span>
				<textarea v-model="form.specialRules" rows="2" placeholder="长按是否支持、和弦上限、切换方式等"></textarea>
			</label>
			<div class="gi-editor-actions">
				<button class="st-btn st-btn--primary" type="button" @click="saveProfile">保存</button>
				<button class="st-btn" type="button" @click="editorOpen = false">取消</button>
			</div>
		</div>
	</div>
</template>

<style scoped>
.gi {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.gi-filters {
	display: grid;
	grid-template-columns: 1fr 1fr 88px;
	gap: 8px;
}

.gi-field {
	display: flex;
	flex-direction: column;
	gap: 3px;
}

.gi-field select,
.gi-field input,
.gi-field textarea {
	width: 100%;
	border: 1px solid var(--st-edge);
	border-radius: 9px;
	background: rgba(255, 255, 255, 0.04);
	color: var(--ext-text);
	font-size: 11.5px;
	padding: 6px 9px;
	font-family: inherit;
	resize: vertical;
}

.gi-field select:focus,
.gi-field input:focus,
.gi-field textarea:focus {
	outline: none;
	border-color: var(--st-violet);
}

.gi-actions {
	display: flex;
	gap: 6px;
}

.gi-list {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.gi-card {
	border: 1px solid var(--st-edge);
	border-radius: 11px;
	background: var(--st-glass);
	padding: 9px 11px;
	display: flex;
	flex-direction: column;
	gap: 5px;
	animation: st-rise 0.28s ease both;
}

.gi-card.on {
	border-color: var(--st-violet);
	background: var(--st-violet-soft);
}

.gi-card-head {
	display: flex;
	align-items: baseline;
	gap: 8px;
}

.gi-game {
	font-size: 10px;
	border: 1px solid var(--st-edge);
	border-radius: 999px;
	padding: 1px 7px;
	color: #d9ccff;
}

.gi-name {
	font-size: 12.5px;
	color: var(--ext-text);
}

.gi-keys {
	margin-left: auto;
	font-family: var(--ext-font-mono);
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.gi-meta {
	display: flex;
	flex-wrap: wrap;
	gap: 5px;
	font-size: 10.5px;
	color: var(--ext-text-mute);
}

.gi-meta i {
	font-style: normal;
	opacity: 0.5;
}

.gi-ok {
	color: var(--st-green);
}

.gi-warn {
	color: #ffb4b4;
}

.gi-reasons {
	margin: 0;
	padding-left: 15px;
	display: flex;
	flex-direction: column;
	gap: 2px;
	font-size: 10.5px;
	color: var(--ext-text-dim);
}

.gi-rules {
	margin: 0;
	font-size: 10.5px;
	color: var(--ext-text-mute);
	line-height: 1.6;
	border-left: 2px solid var(--st-edge);
	padding-left: 8px;
}

.gi-card-actions {
	display: flex;
	gap: 6px;
	flex-wrap: wrap;
	margin-top: 2px;
}

.gi-hint {
	margin: 0;
	font-size: 10.5px;
	color: var(--ext-text-mute);
	line-height: 1.7;
}

.gi-editor {
	border: 1px solid var(--st-violet);
	border-radius: 11px;
	background: var(--st-violet-soft);
	padding: 10px 12px;
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.gi-editor-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 8px;
}

.gi-field--wide {
	grid-column: 1 / -1;
}

.gi-editor-actions {
	display: flex;
	gap: 6px;
}
</style>
