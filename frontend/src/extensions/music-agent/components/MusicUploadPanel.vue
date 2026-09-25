<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { parseJianpu, uploadMidi } from '@/extensions/music-agent/api/music'
import { isJianpuLike, normalizeJianpu } from '@/extensions/music-agent/utils/note-format'
import type { MusicTaskDetail } from '@/extensions/music-agent/types/music'

const emit = defineEmits<{
	/** 解析成功：把后端返回的详情交给上层展示 */
	parsed: [detail: MusicTaskDetail]
}>()

const midiName = ref('')
const midiSize = ref(0)
const midiError = ref('')
const uploading = ref(false)
const jianpu = ref('')
const songName = ref('')
const parsing = ref(false)
const tab = ref<'MIDI' | 'JIANPU'>('MIDI')

const jianpuOk = computed(() => isJianpuLike(jianpu.value))

const SAMPLE = `1=C 4/4 BPM=96
1 1 5 5 | 6 6 5 - | 4 4 3 3 | 2 2 1 -`

function fillSample() {
	jianpu.value = SAMPLE
	songName.value = songName.value || '小星星'
}

/**
 * 先做前端能确定的两件事：扩展名与体积。
 * MIDI 通常只有几十 KB，几 MB 的文件多半是选错了，早说比传完再报错友好。
 */
async function onMidiChange(event: Event) {
	const input = event.target as HTMLInputElement
	const file = input.files?.[0]
	midiError.value = ''
	midiName.value = ''
	midiSize.value = 0
	if (!file) {
		return
	}
	if (!/\.(mid|midi)$/i.test(file.name)) {
		midiError.value = '只接受 .mid / .midi 文件'
		input.value = ''
		return
	}
	if (file.size > 2 * 1024 * 1024) {
		midiError.value = '文件超过 2 MB，MIDI 通常只有几十 KB，请确认文件是否正确'
		input.value = ''
		return
	}
	midiName.value = file.name
	midiSize.value = file.size
	uploading.value = true
	try {
		const detail = await uploadMidi(file)
		ElMessage.success(`解析完成：${detail.noteCount} 个音符`)
		emit('parsed', detail)
	} catch (error) {
		midiError.value = error instanceof Error ? error.message : '解析失败'
	} finally {
		uploading.value = false
		input.value = ''
	}
}

async function submitJianpu() {
	if (!jianpuOk.value) {
		ElMessage.warning('简谱里出现了无法识别的字符，只支持 1-7 / 0 / - / | 以及八度与变化音记号')
		return
	}
	parsing.value = true
	try {
		const detail = await parseJianpu(songName.value.trim(), normalizeJianpu(jianpu.value))
		ElMessage.success(`解析完成：${detail.noteCount} 个音符`)
		emit('parsed', detail)
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '解析失败')
	} finally {
		parsing.value = false
	}
}
</script>

<template>
	<div class="upload">
		<div class="tabs">
			<button class="ext-btn" :class="{ primary: tab === 'MIDI' }" type="button" @click="tab = 'MIDI'">MIDI 文件</button>
			<button class="ext-btn" :class="{ primary: tab === 'JIANPU' }" type="button" @click="tab = 'JIANPU'">简谱文本</button>
		</div>

		<div v-if="tab === 'MIDI'" class="card">
			<label class="drop" :class="{ busy: uploading }">
				<input type="file" accept=".mid,.midi" @change="onMidiChange" />
				<span class="mono">{{ uploading ? '解析中…' : '选择 .mid / .midi' }}</span>
			</label>
			<div v-if="midiName" class="picked">{{ midiName }} · {{ (midiSize / 1024).toFixed(1) }} KB</div>
			<div v-if="midiError" class="err">{{ midiError }}</div>
			<div class="hint">
				解析在后端用 JDK 自带的 javax.sound.midi 完成（纯 Java，不引入 Python 服务）；
				速度快慢只跟文件大小有关。
			</div>
		</div>

		<div v-else class="card">
			<input v-model="songName" class="name-input" type="text" placeholder="曲子名（可留空）" spellcheck="false" />
			<textarea v-model="jianpu" rows="5" placeholder="例：1=C 4/4 BPM=96&#10;5 5 6 5 | 1' - 7 - | 6 6 5 5 | 4 - - -" />
			<div class="row">
				<button class="ext-btn" type="button" @click="fillSample">填入示例</button>
				<button class="ext-btn primary" type="button" :disabled="!jianpu.trim() || parsing" @click="submitJianpu">
					{{ parsing ? '解析中…' : '解析简谱' }}
				</button>
			</div>
			<div v-if="jianpu.trim()" class="picked" :class="{ bad: !jianpuOk }">
				{{ jianpuOk ? '格式可以识别' : '含无法识别的字符：只支持 1-7 / 0 / - / | 与八度、变化音记号' }}
			</div>
			<div class="hint">
				首行可写 <span class="mono">1=C 4/4 BPM=96</span>；
				高八度加撇 <span class="mono">1'</span>，低八度加逗 <span class="mono">1,</span>，
				延长线 <span class="mono">-</span>，附点 <span class="mono">.</span>，减时线 <span class="mono">_</span>，休止 <span class="mono">0</span>。
			</div>
		</div>

		<div class="card off">
			<div class="t">MP3 音频</div>
			<div class="picked">后续阶段</div>
			<div class="hint">需要音频分析（librosa / music21），属于引入 Python 服务的范围，本期不做。</div>
		</div>
	</div>
</template>

<style scoped>
.upload {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.tabs {
	display: flex;
	gap: 8px;
}

.card {
	border: 1px solid var(--ext-line);
	border-radius: var(--ext-radius);
	background: var(--ext-bg-soft);
	padding: 13px 14px;
}

.card.off {
	opacity: 0.55;
}

.card .t {
	font-size: 13px;
	font-weight: 600;
	margin-bottom: 10px;
}

.drop {
	display: block;
	border: 1px dashed var(--ext-line);
	border-radius: 8px;
	padding: 14px;
	text-align: center;
	cursor: pointer;
	color: var(--ext-text-mute);
	transition: border-color 0.2s, color 0.2s;
}

.drop:hover {
	border-color: var(--ext-gold);
	color: var(--ext-gold-light);
}

.drop.busy {
	border-color: var(--ext-gold);
	color: var(--ext-gold-light);
}

.drop input {
	display: none;
}

.mono {
	font-family: var(--ext-font-mono);
	font-size: 11.5px;
}

.name-input,
textarea {
	width: 100%;
	box-sizing: border-box;
	background: var(--ext-bg-raised);
	border: 1px solid var(--ext-line);
	border-radius: 8px;
	color: var(--ext-text);
	font-family: var(--ext-font-mono);
	font-size: 12.5px;
	line-height: 1.75;
	padding: 9px 10px;
	outline: none;
}

.name-input {
	margin-bottom: 9px;
}

textarea {
	resize: vertical;
	margin-bottom: 9px;
}

.name-input:focus,
textarea:focus {
	border-color: var(--ext-gold);
}

.row {
	display: flex;
	gap: 8px;
}

.picked {
	font-family: var(--ext-font-mono);
	font-size: 11.5px;
	color: var(--ext-gold-light);
	margin-top: 9px;
}

.picked.bad,
.err {
	color: #e0a3a5;
}

.err {
	font-size: 12px;
	margin-top: 9px;
}

.hint {
	font-size: 11.5px;
	line-height: 1.8;
	color: var(--ext-text-mute);
	margin-top: 9px;
}
</style>
