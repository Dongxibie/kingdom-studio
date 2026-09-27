import { computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
	getMusicTask,
	listInstruments,
	listMusicTasks,
	mapTaskKeys,
} from '@/extensions/music-agent/api/music'
import { buildPlayEvents, play, type PlayHandle } from '@/extensions/music-agent/utils/demo-player'
import { formatMs, midiToNoteName } from '@/extensions/music-agent/utils/note-format'
import type {
	InstrumentProfile,
	KeySequence,
	KeyStroke,
	MusicNote,
	MusicTaskDetail,
	MusicTaskListItem,
	UnmappedStrategy,
} from '@/extensions/music-agent/types/music'

/**
 * 工作室会话：四个页面共享的「当前曲目 + 映射 + 播放状态」。
 *
 * 为什么放在模块级单例而不是每个页面各存一份：编排台选好的曲子，切到演奏回放或导出中心
 * 必须还是同一首、同一个乐器档案、同一个播放位置 —— 否则每次切页都要重选一遍。
 *
 * 这一层**只做编排与状态**：解析、映射、演奏计划、宏导出全部继续走原有的 api 与类型，
 * 一行没改；播放用原有的 WebAudio 播放器（demo-player）。
 */
const state = reactive({
	/** 曲库 */
	tasks: [] as MusicTaskListItem[],
	total: 0,
	page: 1,
	size: 12,
	keyword: '',
	tasksLoading: false,

	/** 当前曲目 */
	task: null as MusicTaskDetail | null,
	taskLoading: false,

	/** 乐器档案与映射 */
	profiles: [] as InstrumentProfile[],
	profileId: null as number | null,
	strategy: '' as '' | UnmappedStrategy,
	sequence: null as KeySequence | null,
	mapping: false,

	/** 播放 */
	playing: false,
	currentMs: 0,
	volume: 0.6,
	activeStroke: null as KeyStroke | null,
})

let handle: PlayHandle | null = null
let raf = 0
let timer = 0
let wallStart = 0

const activeProfile = computed(() => state.profiles.find((item) => item.id === state.profileId) ?? null)

const activeKeys = computed(() => state.activeStroke?.keys ?? [])

/** 每个键对应的音名：优先按当前序列的音高算，档案未加载也能显示 */
const keyNoteNames = computed(() => {
	const pitches = state.sequence?.keyPitches
	if (pitches?.length) {
		return pitches.map((pitch) => midiToNoteName(pitch))
	}
	return activeProfile.value?.keyNoteNames ?? []
})

/** 当前发声的音符：时间轴与回放页都用它高亮 */
const currentNote = computed<MusicNote | null>(() => {
	const notes = state.task?.notes
	if (!notes?.length) {
		return null
	}
	const now = state.currentMs
	return notes.find((note) => now >= note.startMs && now < note.startMs + note.durationMs) ?? null
})

/** 时间轴上的短标签，例如 00:12.4 */
const clockText = computed(() => formatMs(state.currentMs))

const duration = computed(() => state.task?.durationMs ?? 0)

const progress = computed(() => {
	if (!duration.value) {
		return 0
	}
	return Math.min(100, Math.max(0, (state.currentMs / duration.value) * 100))
})

// ------------------------------------------------------------------ 曲库

async function loadTasks(page = state.page, keyword = state.keyword) {
	state.tasksLoading = true
	try {
		const result = await listMusicTasks(keyword, page, state.size)
		state.tasks = result.records
		state.total = result.total
		state.page = page
		state.keyword = keyword
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取曲库失败')
	} finally {
		state.tasksLoading = false
	}
}

async function loadProfiles() {
	if (state.profiles.length) {
		return
	}
	try {
		state.profiles = await listInstruments()
		if (state.profileId === null && state.profiles.length) {
			state.profileId = state.profiles[0].id
		}
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取乐器档案失败')
	}
}

/** 选中一首曲子：详情 + 乐器档案 + 重新映射（映射失败不影响看谱） */
async function selectTask(id: number, options: { silent?: boolean } = {}) {
	stop()
	state.taskLoading = true
	try {
		state.task = await getMusicTask(id)
		state.currentMs = 0
		state.activeStroke = null
		await loadProfiles()
		await remap()
		if (!options.silent) {
			ElMessage.success('已打开「' + state.task.name + '」')
		}
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取曲目失败')
	} finally {
		state.taskLoading = false
	}
}

/** 重新做一次按键映射（换档案或换策略时调用） */
async function remap() {
	if (!state.task || state.profileId === null) {
		state.sequence = null
		return
	}
	state.mapping = true
	try {
		state.sequence = await mapTaskKeys(state.task.id, state.profileId, state.strategy || undefined)
	} catch (error) {
		state.sequence = null
		ElMessage.error(error instanceof Error ? error.message : '按键映射失败')
	} finally {
		state.mapping = false
	}
}

async function setProfile(profileId: number) {
	if (state.profileId === profileId) {
		return
	}
	state.profileId = profileId
	await remap()
}

async function setStrategy(strategy: '' | UnmappedStrategy) {
	if (state.strategy === strategy) {
		return
	}
	state.strategy = strategy
	await remap()
}

/** 新曲子解析出来后：刷新曲库并直接打开它 */
async function adopt(detail: MusicTaskDetail) {
	await loadTasks(1)
	await selectTask(detail.id, { silent: true })
}

// ------------------------------------------------------------------ 播放

function stopLoop() {
	if (raf) {
		cancelAnimationFrame(raf)
		raf = 0
	}
	if (timer) {
		window.clearInterval(timer)
		timer = 0
	}
}

/**
 * 推进播放位置：返回 false 表示已经走到结尾。
 *
 * 位置由墙钟算，不用帧计数 —— 掉帧不会让时钟变慢。
 */
function advance(): boolean {
	if (!state.task) {
		return false
	}
	const elapsed = performance.now() - wallStart
	state.currentMs = Math.min(elapsed, state.task.durationMs)
	const stroke = state.activeStroke
	if (stroke && elapsed > stroke.startMs + stroke.durationMs + 60) {
		state.activeStroke = null
	}
	if (elapsed >= state.task.durationMs) {
		state.playing = false
		state.activeStroke = null
		return false
	}
	return true
}

function stop() {
	stopLoop()
	handle?.stop()
	handle = null
	state.playing = false
	state.activeStroke = null
}

/** 从某个时刻开始播放（不传就从头） */
function start(fromMs = state.currentMs) {
	const sequence = state.sequence
	if (!sequence) {
		ElMessage.warning('这首曲子还没有按键序列，先选一个乐器档案')
		return
	}
	stop()
	state.currentMs = fromMs
	wallStart = performance.now() - fromMs
	state.playing = true

	handle = play({
		events: buildPlayEvents(sequence),
		fromMs,
		volume: state.volume,
		onEvent: (event) => {
			state.activeStroke = sequence.strokes.find((stroke) => stroke.startMs === event.atMs) ?? null
		},
		onEnd: () => {
			stopLoop()
			state.playing = false
			state.activeStroke = null
			state.currentMs = state.task?.durationMs ?? state.currentMs
		},
	})

	const loop = () => {
		if (!state.playing) {
			return
		}
		raf = 0
		if (advance()) {
			raf = requestAnimationFrame(loop)
			return
		}
		stopLoop()
	}
	raf = requestAnimationFrame(loop)
	// 标签页切到后台时浏览器会把 requestAnimationFrame 节流到不触发，但音频还在走。
	// 用定时器把播放位置兜住，切回来时不会出现「声音在前面、指针停在原地」。
	timer = window.setInterval(() => {
		if (state.playing && document.hidden && !advance()) {
			stopLoop()
		}
	}, 80)
}

function toggle() {
	if (state.playing) {
		stop()
		return
	}
	start()
}

/** 跳到某个时刻：正在播放就接着播，否则只移动指针 */
function seek(millis: number) {
	const value = Math.max(0, Math.min(duration.value || millis, millis))
	if (state.playing) {
		start(value)
		return
	}
	state.currentMs = value
	state.activeStroke = null
}

function rewind() {
	stop()
	state.currentMs = 0
	state.activeStroke = null
}

function setVolume(value: number) {
	state.volume = value
	if (state.playing) {
		start(state.currentMs)
	}
}

export function useStudioSession() {
	return {
		state,
		activeProfile,
		activeKeys,
		keyNoteNames,
		currentNote,
		clockText,
		duration,
		progress,
		loadTasks,
		loadProfiles,
		selectTask,
		remap,
		setProfile,
		setStrategy,
		adopt,
		start,
		stop,
		toggle,
		seek,
		rewind,
		setVolume,
	}
}
