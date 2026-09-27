<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ExtShell } from '@/extensions/_shared/components'
import StudioTransport from '@/extensions/music-studio/components/StudioTransport.vue'
import { useStudioSession } from '@/extensions/music-studio/composables/useStudioSession'
// 主题样式只在这里引入：工作室路由是懒加载的，样式跟随本模块的 chunk
import '@/extensions/music-studio/styles/studio.css'

/**
 * 工作室外壳（Music Studio Layout）。
 *
 * 把四个页面共用的一层框起来：标题、页面切换、走带控制条、当前曲目条。
 * 页面本身只关心自己的主内容，不需要各自再摆一遍播放按钮。
 */
interface Props {
	title: string
	subtitle?: string
	mark?: string
	/** 右侧窄栏宽度（编排台要宽一点放助手） */
	rightWidth?: string
}

const props = withDefaults(defineProps<Props>(), {
	subtitle: '',
	mark: 'S',
	rightWidth: '',
})

const route = useRoute()
const router = useRouter()
const { state } = useStudioSession()

const NAV = [
	{ path: '/extensions/music-studio', label: '曲目台', hint: '曲库与导入' },
	{ path: '/extensions/music-studio/composer', label: '编排台', hint: '时间线与键盘' },
	{ path: '/extensions/music-studio/replay', label: '演奏回放', hint: '全屏演示' },
	{ path: '/extensions/music-studio/export', label: '导出中心', hint: 'AHK / JSON / TXT' },
]

const activePath = computed(() => route.path)

function go(path: string) {
	void router.push(path)
}
</script>

<template>
	<ExtShell
		:title="props.title"
		:subtitle="props.subtitle"
		:mark="props.mark"
		variant="obsidian"
		full-width
		class="studio-root">
		<template #actions>
			<slot name="actions" />
		</template>

		<template #top>
			<div class="studio-top">
				<nav class="studio-nav">
					<button
						v-for="item in NAV"
						:key="item.path"
						class="studio-nav-item"
						:class="{ on: activePath === item.path }"
						type="button"
						@click="go(item.path)">
						<span class="studio-nav-label">{{ item.label }}</span>
						<span class="studio-nav-hint">{{ item.hint }}</span>
					</button>
				</nav>

				<div class="studio-song" :class="{ empty: !state.task }">
					<span class="st-label">当前曲目</span>
					<span class="studio-song-name">{{ state.task?.name ?? '未选择' }}</span>
					<span v-if="state.task" class="st-chip">{{ state.task.pitchRange }}</span>
					<span v-if="state.sequence" class="st-chip st-chip--hot">
						{{ state.sequence.profileName }} · 落键 {{ state.sequence.mappedCount }}
					</span>
					<span v-if="state.mapping" class="st-chip">映射中…</span>
				</div>

				<StudioTransport />
			</div>
		</template>

		<template #left>
			<slot name="left" />
		</template>

		<slot />

		<template #right>
			<slot name="right" />
		</template>
	</ExtShell>
</template>

<style scoped>
.studio-top {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.studio-nav {
	display: flex;
	gap: 8px;
	flex-wrap: wrap;
}

.studio-nav-item {
	display: flex;
	flex-direction: column;
	align-items: flex-start;
	gap: 1px;
	border: 1px solid var(--st-edge);
	border-radius: 11px;
	background: var(--st-glass);
	padding: 7px 14px;
	cursor: pointer;
	transition: border-color 0.18s ease, background 0.18s ease, transform 0.18s ease;
}

.studio-nav-item:hover {
	border-color: var(--st-edge-hot);
	transform: translateY(-1px);
}

.studio-nav-item.on {
	border-color: var(--ext-gold);
	background: linear-gradient(180deg, rgba(240, 205, 114, 0.2), rgba(240, 205, 114, 0.06));
}

.studio-nav-label {
	font-size: 12.5px;
	color: var(--ext-text-dim);
}

.studio-nav-item.on .studio-nav-label {
	color: var(--ext-gold-light);
}

.studio-nav-hint {
	font-size: 10px;
	color: var(--ext-text-mute);
}

.studio-song {
	display: flex;
	align-items: center;
	gap: 10px;
	flex-wrap: wrap;
	padding: 7px 12px;
	border: 1px solid var(--st-edge);
	border-radius: 11px;
	background: var(--st-glass);
}

.studio-song.empty {
	opacity: 0.72;
}

.studio-song-name {
	font-size: 13.5px;
	color: var(--ext-text);
}
</style>
