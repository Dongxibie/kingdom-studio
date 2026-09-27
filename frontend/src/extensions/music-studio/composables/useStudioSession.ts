import { computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
	createPreset,
	deletePreset,
	fetchOptimization,
	fetchSongAnalysis,
	getMusicTask,
	listInstruments,
	listMusicTasks,
	listPresets,
	mapTaskKeys,
	mapWithPreset,
	toggleFavorite,
	updatePreset,
} from '@/extensions/music-agent/api/music'
import { buildPlayEvents, play, type PlayHandle } from '@/extensions/music-agent/utils/demo-player'
import { formatMs, midiToNoteName } from '@/extensions/music-agent/utils/note-format'
import type {
	OptimizationFix,
	OptimizationReport,
	PerformancePreset,
	PerformancePresetPayload,
	SongAnalysis,
} from '@/extensions/music-studio/types/studio'
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
	favoriteOnly: false,
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

	/** 曲目洞察：分析卡与优化建议 */
	analysis: null as SongAnalysis | null,
	optimization: null as OptimizationReport | null,
	insightLoading: false,

	/** 最近演奏：只在本地记，属于使用者的习惯，不写服务端 */
	recent: [] as MusicTaskListItem[],

	/** 演奏方案 */
	presets: [] as PerformancePreset[],
	activePresetId: null as number | null,

	/** 播放 */
	playing: false,
	currentMs: 0,
	volume: 0.6,
	activeStroke: null as KeyStroke | null,
})

let handle: PlayHandle | null = null
loadRecent()
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

async function loadTasks(page = state.page, keyword = state.keyword, favoriteOnly = state.favoriteOnly) {
	state.tasksLoading = true
	try {
		const result = await listMusicTasks(keyword, page, state.size, favoriteOnly)
		state.tasks = result.records
		state.total = result.total
		state.page = page
		state.keyword = keyword
		state.favoriteOnly = favoriteOnly
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

const RECENT_KEY = 'music_studio_recent'
const RECENT_LIMIT = 12

/** 从本地读出「最近演奏」（换设备就没了，这符合它的语义） */
function loadRecent() {
	try {
		const raw = localStorage.getItem(RECENT_KEY)
		if (raw) {
			state.recent = JSON.parse(raw) as MusicTaskListItem[]
		}
	} catch {
		state.recent = []
	}
}

/** 记一次演奏：同名的移到最前，最多留 12 条 */
function rememberPlayed(task: MusicTaskListItem | null) {
	if (!task) {
		return
	}
	const rest = state.recent.filter((item) => item.id !== task.id)
	state.recent = [task, ...rest].slice(0, RECENT_LIMIT)
	try {
		localStorage.setItem(RECENT_KEY, JSON.stringify(state.recent))
	} catch {
		// 隐私模式下写不了本地存储：只影响「最近演奏」这一处，不影响其它功能
	}
}

/** 按 id 打开一首曲子（左侧音乐库点击时用） */
async function openById(taskId: number) {
	await selectTask(taskId, { silent: true })
}

/**
 * 把 AI 助手的建议落成一套方案。
 *
 * 这是「AI 输出建议 → 规则校验 → 演奏方案」的最后一环：助手只给难度与建议，
 * 具体用哪套键位、要不要移八度，由分析结果与规则决定，最后存成方案（可改名、可删）。
 */
async function createPresetFromAdvice(label: string, strategy?: string | null) {
	const task = state.task
	if (!task) {
		return
	}
	const recommended = state.analysis?.recommendedProfileId
	const profileId = recommended ?? state.profileId
	if (profileId === null || profileId === undefined) {
		return
	}
	const name = 'AI 建议 · ' + label
	if (state.presets.some((preset) => preset.name === name)) {
		return
	}
	try {
		const saved = await createPreset(task.id, {
			name,
			profileId,
			strategy: strategy ?? null,
			speedScale: 1,
			minGapMs: 0,
			note: '由 AI 助手的难度建议生成（' + label + '）',
		})
		await loadPresets()
		await applyPreset(saved)
		ElMessage.success('已按 AI 建议生成方案「' + name + '」')
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '按建议生成方案失败')
	}
}

/** 收藏 / 取消收藏：先在本地翻转（点了就有反馈），再以后端返回为准 */
async function switchFavorite(taskId: number) {
	const item = state.tasks.find((task) => task.id === taskId)
	const before = item?.favorite ?? 0
	if (item) {
		item.favorite = before === 1 ? 0 : 1
	}
	try {
		const next = await toggleFavorite(taskId)
		if (item) {
			item.favorite = next ? 1 : 0
		}
		if (state.favoriteOnly && !next) {
			state.tasks = state.tasks.filter((task) => task.id !== taskId)
			state.total = Math.max(0, state.total - 1)
		}
		ElMessage.success(next ? '已收藏「' + (item?.name ?? '') + '」' : '已取消收藏')
	} catch (error) {
		if (item) {
			item.favorite = before
		}
		ElMessage.error(error instanceof Error ? error.message : '收藏失败')
	}
}

/** 曲目洞察：分析卡与优化建议一起取，两者都依赖当前档案 */
async function loadInsight() {
	const task = state.task
	if (!task) {
		state.analysis = null
		state.optimization = null
		return
	}
	state.insightLoading = true
	try {
		state.analysis = await fetchSongAnalysis(task.id, state.profileId)
		if (state.profileId !== null && state.sequence) {
			state.optimization = await fetchOptimization(
				task.id,
				state.profileId,
				state.strategy || undefined,
				state.activePresetId,
			)
		} else {
			state.optimization = null
		}
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取曲目分析失败')
	} finally {
		state.insightLoading = false
	}
}

/** 方案列表：后端会在首次访问时补齐内置的三套 */
async function loadPresets() {
	const task = state.task
	if (!task) {
		state.presets = []
		state.activePresetId = null
		return
	}
	try {
		state.presets = await listPresets(task.id)
		// 换曲子后上一首的方案不属于这一首，必须换成这首自己的方案，
		// 否则后面的分析 / 计划会带着别人的方案 id 被后端拒绝。
		if (!state.presets.some((preset) => preset.id === state.activePresetId)) {
			state.activePresetId = state.presets.length ? state.presets[0].id : null
		}
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取演奏方案失败')
	}
}

/**
 * 把当前设置另存为一套方案。
 *
 * 名字重复时后端会拒绝（并说明已经有同名方案），这里直接把原因透给用户 ——
 * 重名会让「方案」这件事失去意义，静默改名反而更难解释。
 */
async function savePreset(name: string) {
	const task = state.task
	if (!task || state.profileId === null) {
		ElMessage.warning('先选一首曲子和乐器档案')
		return
	}
	try {
		const saved = await createPreset(task.id, {
			name: name.trim(),
			profileId: state.profileId,
			strategy: state.strategy || null,
			speedScale: 1,
			minGapMs: 0,
			note: '手动保存的方案',
		})
		await loadPresets()
		await applyPreset(saved)
		ElMessage.success('已保存方案「' + saved.name + '」')
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '保存方案失败')
	}
}

/** 删除方案（内置方案也能删；删完可以再存） */
async function deletePresetById(preset: PerformancePreset) {
	const task = state.task
	if (!task) {
		return
	}
	try {
		await deletePreset(task.id, preset.id)
		if (state.activePresetId === preset.id) {
			state.activePresetId = null
		}
		await loadPresets()
		ElMessage.success('已删除方案「' + preset.name + '」')
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '删除方案失败')
	}
}

/** 应用方案：按方案的档案与策略重新映射（速度与间隔在生成计划时生效） */
async function applyPreset(preset: PerformancePreset) {
	if (!state.task) {
		return
	}
	state.activePresetId = preset.id
	state.mapping = true
	try {
		state.profileId = preset.profileId
		state.strategy = (preset.strategy as '' | UnmappedStrategy) ?? ''
		state.sequence = await mapWithPreset(state.task.id, preset.id)
		await loadInsight()
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '应用方案失败')
	} finally {
		state.mapping = false
	}
}

/**
 * 把优化建议写进方案。
 *
 * 内置方案（原版 / 简单版 / 快速版）是基准，不该被建议改掉 —— 改掉之后就再也回不到原样了。
 * 所以规则是：当前用的是内置方案时，**新建一份「优化版」**承接这次调整；用的是自己存的方案时，
 * 就地更新它。
 */
async function applyFix(fix: OptimizationFix, label = '优化版') {
	const task = state.task
	if (!task) {
		return
	}
	const active = state.presets.find((preset) => preset.id === state.activePresetId) ?? null
	const current = active && !active.builtin ? active : null
	const payload: PerformancePresetPayload = {
		name: current ? current.name : nextPresetName(label),
		profileId: fix.profileId ?? current?.profileId ?? state.profileId ?? 0,
		strategy: fix.strategy ?? current?.strategy ?? (state.strategy || null),
		speedScale: fix.speedScale ?? current?.speedScale ?? 1,
		minGapMs: fix.minGapMs ?? current?.minGapMs ?? 0,
		note: current?.note ?? '按优化建议生成：' + describeFix(fix),
	}
	try {
		const saved = current
			? await updatePreset(task.id, current.id, payload)
			: await createPreset(task.id, payload)
		await loadPresets()
		await applyPreset(saved)
		ElMessage.success('已生成方案「' + saved.name + '」，生成计划与导出都会按它执行')
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '应用建议失败')
	}
}

/** 新方案名：优化版 / 优化版 2 / 优化版 3 …（重名后端会拒，所以先自己避重） */
function nextPresetName(base: string) {
	const taken = new Set(state.presets.map((preset) => preset.name))
	if (!taken.has(base)) {
		return base
	}
	for (let index = 2; index < 20; index++) {
		const candidate = base + ' ' + index
		if (!taken.has(candidate)) {
			return candidate
		}
	}
	return base + ' ' + Date.now()
}

function describeFix(fix: OptimizationFix) {
	const parts: string[] = []
	if (fix.minGapMs) {
		parts.push('同键间隔 ' + fix.minGapMs + 'ms')
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
	return parts.join('、') || '按优化建议'
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
		await loadPresets()
		await remap()
		await loadInsight()
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
	state.activePresetId = null
	await remap()
	await loadInsight()
}

async function setStrategy(strategy: '' | UnmappedStrategy) {
	if (state.strategy === strategy) {
		return
	}
	state.strategy = strategy
	state.activePresetId = null
	await remap()
	await loadInsight()
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
	rememberPlayed(state.tasks.find((item) => item.id === state.task?.id) ?? null)

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
		switchFavorite,
		loadInsight,
		loadPresets,
		applyPreset,
		savePreset,
		deletePresetById,
		openById,
		createPresetFromAdvice,
		rememberPlayed,
		applyFix,
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
