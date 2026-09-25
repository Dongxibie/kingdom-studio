<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ExtCodeBlock } from '@/extensions/_shared/components'
import request, { http } from '@/api/request'

interface Props {
	taskId: number | null
	profileId: number | null
	strategy?: string
}

const props = defineProps<Props>()

interface KeyCommand {
	seq: number
	key: string
	action: 'PRESS' | 'RELEASE'
	atMs: number
	/** 松开命令不带这个字段：后端序列化时会省略 null */
	holdMs?: number | null
	strokeSeq: number
	adjusted: boolean
}

interface DispatchPlan {
	sessionId: string
	mode: string
	taskName: string
	profileName: string
	durationMs: number
	accepted: number
	warnings: string[]
	commands: KeyCommand[]
	notes: string[]
}

interface AgentStatus {
	available: boolean
	mode: string
	protocolVersion: string
	endpoint: string
	notes: string[]
}

const status = ref<AgentStatus | null>(null)
const plan = ref<DispatchPlan | null>(null)
const loading = ref(false)

/** 计划本身是 JSON，展示时截断到前 40 条，避免面板被几百行淹没 */
const commandsPreview = computed(() => {
	if (!plan.value) {
		return ''
	}
	const head = plan.value.commands.slice(0, 40)
	const lines = head.map((command) =>
		String(command.atMs).padStart(6, ' ') + 'ms  ' +
		command.action.padEnd(7, ' ') + command.key +
		(command.holdMs ? '  按住 ' + command.holdMs + 'ms' : ''))
	if (plan.value.commands.length > head.length) {
		lines.push('… 其余 ' + (plan.value.commands.length - head.length) + ' 条已在计划中，此处不展开')
	}
	return lines.join('\n')
})

async function loadStatus() {
	try {
		status.value = await http.get<AgentStatus>('/desktop/ping')
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '读取代理状态失败')
	}
}

async function makePlan() {
	if (!props.taskId || !props.profileId) {
		ElMessage.warning('先选一首曲子与一个乐器档案')
		return
	}
	loading.value = true
	try {
		plan.value = await request.post('/desktop/dispatch', {
			taskId: props.taskId,
			profileId: props.profileId,
			strategy: props.strategy || undefined,
			clientId: 'kingdom-music-agent',
		}, { timeout: 30000 }) as unknown as DispatchPlan
		ElMessage.success('已生成派发计划（模拟，未产生任何真实输入）')
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '生成派发计划失败')
	} finally {
		loading.value = false
	}
}

void loadStatus()
</script>

<template>
	<div class="agent">
		<div class="ext-kv">
			<span class="k">代理</span>
			<span class="v">{{ status ? (status.available ? '已接入（' + status.mode + '）' : '未接入 · 模拟模式') : '读取中…' }}</span>
		</div>
		<div class="ext-kv" style="margin-top: 6px">
			<span class="k">协议</span>
			<span class="v">{{ status ? status.endpoint + ' · v' + status.protocolVersion : '—' }}</span>
		</div>

		<div class="row">
			<button class="ext-btn primary" type="button" :disabled="loading || !taskId" @click="makePlan">
				{{ loading ? '生成中…' : '生成派发计划（模拟）' }}
			</button>
		</div>

		<div v-if="plan" class="plan">
			<div class="ext-kv">
				<span class="k">会话</span>
				<span class="v">{{ plan.sessionId }} · {{ plan.mode }}</span>
			</div>
			<div class="ext-kv" style="margin-top: 6px">
				<span class="k">命令数</span>
				<span class="v">{{ plan.accepted }} 条（{{ plan.taskName }} → {{ plan.profileName }}）</span>
			</div>

			<div v-if="plan.warnings.length" class="warn">
				<div class="t">被规则调整或拦下的地方</div>
				<ul>
					<li v-for="(warning, index) in plan.warnings.slice(0, 6)" :key="index">{{ warning }}</li>
				</ul>
			</div>

			<ExtCodeBlock :code="commandsPreview" title="待发送的命令流（前 40 条）" />
		</div>

		<div class="ext-hint">
			这条链路只生成「会按什么」的计划：本模块不驱动真实系统输入，
			真实演奏由本机代理按 <span class="mono">docs/desktop-agent-protocol.md</span> 实现，
			并需具备急停与前台窗口限定。
		</div>
	</div>
</template>

<style scoped>
.agent {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.row {
	display: flex;
	gap: 8px;
}

.plan {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.warn {
	border: 1px solid var(--ext-red);
	background: var(--ext-red-soft);
	border-radius: var(--ext-radius);
	padding: 9px 11px;
}

.warn .t {
	font-size: 12px;
	color: #e0a3a5;
	margin-bottom: 6px;
}

.warn ul {
	margin: 0;
	padding-left: 18px;
	font-size: 11.5px;
	line-height: 1.8;
	color: var(--ext-text-dim);
}

.mono {
	font-family: var(--ext-font-mono);
	font-size: 11px;
	color: var(--ext-gold-light);
}
</style>
