<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ExtCodeBlock } from '@/extensions/_shared/components'
import {
	executePlan,
	fetchRuntimePreflight,
	fetchRuntimeStatus,
	pauseLocalRun,
	resumeLocalRun,
	startLocalRun,
	stopLocalRun,
} from '@/extensions/music-agent/api/macro'
import type { ExecutionResult, ExecutionStatus, RuntimePreflight } from '@/extensions/music-agent/types/macro'
import { formatMs } from '@/extensions/music-agent/utils/note-format'

/**
 * 演奏控制面板：生成计划 → 预览 → 本地演奏。
 *
 * 这个面板刻意把「会发生什么」摆在按钮前面：开关状态、此刻的前台窗口、注入方式、
 * 必须先确认的四条，都写在按钮上方；开始前必须勾选确认，且演奏期间状态每 400ms 刷新一次。
 */
interface Props {
	taskId: number | null
}

const props = defineProps<Props>()

const preflight = ref<RuntimePreflight | null>(null)
const status = ref<ExecutionStatus | null>(null)
const preview = ref<ExecutionResult | null>(null)
const accepted = ref(false)
const busy = ref(false)
let timer: number | null = null

const running = computed(() => status.value?.status === 'RUNNING' || status.value?.status === 'PAUSED')
const canStart = computed(() => Boolean(preflight.value?.ready) && accepted.value && !running.value)
const tone = computed(() => {
	switch (status.value?.status) {
		case 'RUNNING':
			return 'ok'
		case 'PAUSED':
			return 'warn'
		case 'STOPPED':
			return 'red'
		default:
			return 'mute'
	}
})

const previewCommands = computed(() => {
	const commands = preview.value?.commands ?? []
	return commands.slice(0, 30).map((command) => {
		const hold = command.holdMs ? `（按住 ${command.holdMs}ms）` : ''
		return `${String(command.seq).padStart(4, '0')}  ${formatMs(command.atMs)}  ${command.action === 'DOWN' ? '按下' : '松开'} ${command.key}${hold}`
	}).join('\n')
})

async function refreshPreflight() {
	if (!props.taskId) {
		return
	}
	try {
		preflight.value = await fetchRuntimePreflight(props.taskId)
	} catch (error) {
		preflight.value = null
	}
}

async function refreshStatus() {
	if (!props.taskId) {
		return
	}
	try {
		status.value = await fetchRuntimeStatus(props.taskId)
	} catch {
		// 没有会话时后端也会返回一个 READY 空状态，这里只在网络异常时静默
	}
}

function startPolling() {
	stopPolling()
	timer = window.setInterval(refreshStatus, 400)
}

function stopPolling() {
	if (timer !== null) {
		window.clearInterval(timer)
		timer = null
	}
}

async function runPreview() {
	if (!props.taskId) {
		return
	}
	busy.value = true
	try {
		preview.value = await executePlan(props.taskId, 'PREVIEW')
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '预览失败')
	} finally {
		busy.value = false
	}
}

async function start() {
	const value = preflight.value
	if (!props.taskId || !value) {
		return
	}
	if (!value.ready) {
		ElMessage.warning(value.reason || '当前环境不满足本机演奏的条件')
		return
	}
	busy.value = true
	try {
		status.value = await startLocalRun(props.taskId, value.currentWindow)
		startPolling()
		ElMessage.success('已开始本机演奏；ESC 可随时急停')
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '启动失败')
	} finally {
		busy.value = false
	}
}

async function pause() {
	if (!props.taskId) {
		return
	}
	status.value = await pauseLocalRun(props.taskId)
}

async function resume() {
	if (!props.taskId) {
		return
	}
	status.value = await resumeLocalRun(props.taskId)
}

async function stop() {
	if (!props.taskId) {
		return
	}
	try {
		status.value = await stopLocalRun(props.taskId)
		ElMessage.success('已停止并松开所有按键')
	} finally {
		stopPolling()
	}
}

watch(() => props.taskId, async () => {
	preview.value = null
	status.value = null
	accepted.value = false
	await Promise.all([refreshPreflight(), refreshStatus()])
	if (running.value) {
		startPolling()
	}
}, { immediate: true })

onBeforeUnmount(async () => {
	stopPolling()
	// 离开页面时如果还在演奏：主动停一次，避免留下一直按着的键
	if (running.value && props.taskId) {
		try {
			await stopLocalRun(props.taskId)
		} catch {
			// 停不掉也不阻塞离开：后端会话在异常退出时同样会释放按键
		}
	}
})
</script>

<template>
	<div class="perf">
		<div class="perf-warn">
			<div>⚠ 本功能需要你主动开启：默认关闭，需要后端打开开关后才会出现「开始」按钮</div>
			<div>⚠ 执行前请把目标窗口切到前台并确认：演奏期间只对确认过的窗口生效，窗口一换就自动暂停</div>
			<div>⚠ ESC 随时急停：立刻停止并松开所有按下的键；不会后台隐藏执行，也不注入任何进程</div>
		</div>

		<div class="perf-step">
			<div class="perf-kv">
				<span class="k">环境自检</span>
				<span class="v" :class="{ ok: preflight?.ready, bad: preflight && !preflight.ready }">
					{{ preflight ? (preflight.ready ? '可以开始' : '暂不可开始') : '读取中…' }}
				</span>
			</div>
			<div class="perf-kv">
				<span class="k">功能开关</span>
				<span class="v">{{ preflight?.enabled ? '已开启' : '未开启' }}</span>
			</div>
			<div class="perf-kv">
				<span class="k">当前前台窗口</span>
				<span class="v mono">{{ preflight?.currentWindow || '（读不到）' }}</span>
			</div>
			<div class="perf-kv">
				<span class="k">注入方式</span>
				<span class="v">{{ preflight?.injector || '—' }}</span>
			</div>
			<div v-if="preflight && !preflight.ready" class="perf-reason">{{ preflight.reason }}</div>
			<ul class="perf-requirements">
				<li v-for="item in preflight?.requirements ?? []" :key="item">{{ item }}</li>
			</ul>
		</div>

		<div class="perf-actions">
			<label class="perf-confirm">
				<input v-model="accepted" type="checkbox" :disabled="running" />
				我知道这会在本机发送按键，且目标窗口已经切到前台
			</label>
			<div class="perf-buttons">
				<button class="ext-btn" type="button" :disabled="busy || !preflight?.ready" @click="runPreview">预览命令流</button>
				<button class="ext-btn primary" type="button" :disabled="busy || !canStart" @click="start">开始本地演奏</button>
				<button class="ext-btn" type="button" :disabled="status?.status !== 'RUNNING'" @click="pause">暂停</button>
				<button class="ext-btn" type="button" :disabled="status?.status !== 'PAUSED'" @click="resume">继续</button>
				<button class="ext-btn danger" type="button" :disabled="!running" @click="stop">停止（急停）</button>
			</div>
		</div>

		<div v-if="status && status.status !== 'READY'" class="perf-monitor">
			<div class="monitor-head">
				<span class="badge" :class="'tone-' + tone">{{ status.statusLabel }}</span>
				<span class="title">{{ status.taskName }} · {{ status.profileName }}</span>
				<button class="ext-btn" type="button" @click="refreshStatus">刷新</button>
			</div>
			<div class="monitor-grid">
				<div class="cell"><span class="k">当前按键</span><span class="v">{{ status.currentKey || '—' }}</span></div>
				<div class="cell"><span class="k">进度</span><span class="v">{{ status.progress }}%（{{ status.executedCount }}/{{ status.commandCount }}）</span></div>
				<div class="cell"><span class="k">已用 / 总时长</span><span class="v">{{ formatMs(status.elapsedMs) }} / {{ formatMs(status.duration) }}</span></div>
				<div class="cell"><span class="k">剩余时间</span><span class="v">{{ formatMs(status.remainingMs) }}</span></div>
				<div class="cell"><span class="k">按住的键</span><span class="v">{{ status.heldKeys.length ? status.heldKeys.join(' / ') : '无' }}</span></div>
				<div class="cell"><span class="k">窗口</span><span class="v mono">{{ status.currentWindow || '—' }}</span></div>
			</div>
			<div class="monitor-bar"><span :style="{ width: status.progress + '%' }"></span></div>
			<div v-if="status.stopReason" class="monitor-note">停止原因：{{ status.stopReason }}</div>
			<ul v-if="status.warnings.length" class="monitor-warnings">
				<li v-for="(warning, index) in status.warnings" :key="index">{{ warning }}</li>
			</ul>
		</div>

		<ExtCodeBlock v-if="previewCommands" :code="previewCommands" title="预览：将要发出的命令（前 30 条）" />
	</div>
</template>

<style scoped>
.perf { display: flex; flex-direction: column; gap: 12px; }
.perf-warn {
	border: 1px solid var(--ext-gold);
	background: var(--ext-gold-soft);
	border-radius: var(--ext-radius);
	padding: 10px 12px;
	font-size: 11.5px;
	line-height: 1.9;
	color: var(--ext-gold-light);
}
.perf-step { display: flex; flex-direction: column; gap: 6px; }
.perf-kv { display: flex; justify-content: space-between; gap: 10px; font-size: 11.5px; }
.perf-kv .k { color: var(--ext-text-mute); white-space: nowrap; }
.perf-kv .v { color: var(--ext-text); text-align: right; }
.perf-kv .v.ok { color: #7ee0a2; }
.perf-kv .v.bad { color: #ff8f8f; }
.perf-kv .v.mono { font-family: var(--ext-font-mono); font-size: 11px; word-break: break-all; }
.perf-reason { font-size: 11.5px; line-height: 1.75; color: #ff8f8f; }
.perf-requirements { margin: 0; padding-left: 16px; font-size: 11px; line-height: 1.8; color: var(--ext-text-mute); }
.perf-actions { display: flex; flex-direction: column; gap: 8px; }
.perf-confirm { display: flex; align-items: center; gap: 6px; font-size: 11.5px; color: var(--ext-text-dim); }
.perf-buttons { display: flex; gap: 6px; flex-wrap: wrap; }
.ext-btn.primary { border-color: var(--ext-gold); background: var(--ext-gold-soft); color: var(--ext-gold-light); }
.ext-btn.danger { border-color: var(--ext-red); color: #ff8f8f; }
.perf-monitor {
	border: 1px solid var(--ext-line);
	border-radius: var(--ext-radius);
	padding: 10px;
	background: var(--ext-bg-raised);
	display: flex;
	flex-direction: column;
	gap: 8px;
}
.monitor-head { display: flex; align-items: center; gap: 8px; }
.monitor-head .title { font-size: 12px; color: var(--ext-text); flex: 1; }
.badge { font-size: 11px; padding: 2px 8px; border-radius: 999px; border: 1px solid var(--ext-line); }
.badge.tone-ok { color: #7ee0a2; border-color: rgba(126, 224, 162, 0.5); }
.badge.tone-warn { color: var(--ext-gold-light); border-color: var(--ext-gold); }
.badge.tone-red { color: #ff8f8f; border-color: var(--ext-red); }
.badge.tone-mute { color: var(--ext-text-mute); }
.monitor-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 6px 12px; }
.cell { display: flex; justify-content: space-between; gap: 8px; font-size: 11.5px; }
.cell .k { color: var(--ext-text-mute); }
.cell .v { color: var(--ext-text); }
.cell .v.mono { font-family: var(--ext-font-mono); font-size: 11px; word-break: break-all; }
.monitor-bar { height: 4px; border-radius: 999px; background: rgba(255, 255, 255, 0.08); overflow: hidden; }
.monitor-bar span { display: block; height: 100%; background: linear-gradient(90deg, #f0cd72, #5eead4); transition: width 0.3s linear; }
.monitor-note { font-size: 11px; color: var(--ext-text-mute); }
.monitor-warnings { margin: 0; padding-left: 16px; font-size: 11px; line-height: 1.8; color: var(--ext-gold-light); }
</style>
