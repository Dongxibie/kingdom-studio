<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Clock, Collection, Document, Headset, HomeFilled, MagicStick, OfficeBuilding } from '@element-plus/icons-vue'
import { useNarrowScreen } from '@/composables/useNarrowScreen'

const route = useRoute()

const menus = [
	{ path: '/dashboard', title: '工作台', icon: HomeFilled },
	{ path: '/projects', title: '项目王国', icon: OfficeBuilding },
	{ path: '/technologies', title: '技术图鉴', icon: Collection },
	{ path: '/journey', title: '成长时间线', icon: Clock },
	{ path: '/code-library', title: '代码知识库', icon: Document }
]

// 扩展模块单独成组：后续扩展会越来越多（AI Agent Lab、Prompt Tools 等），
// 平铺进主菜单会把侧边栏挤爆，这里用子菜单收拢
const extensionMenus = [
	{ path: '/extensions/motion-lab', title: '动效工作台', icon: MagicStick },
	{ path: '/extensions/music-agent', title: '音乐 Agent', icon: Headset }
]

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => (route.meta.title as string) || 'Kingdom Studio')

// 窄屏（平板竖屏 / 手机）把侧边栏收成图标条：固定 232px 会占掉手机屏幕六成宽度，
// 内容区被压到只剩一百多像素，中文只能逐字换行
const { isNarrow } = useNarrowScreen()
</script>

<template>
	<el-container class="layout">
		<el-aside :width="isNarrow ? '72px' : '232px'" class="sidebar">
			<div class="brand">
				<svg class="crown" viewBox="0 0 24 24" aria-hidden="true">
					<path
						d="M3 8l4 3 5-6 5 6 4-3-2 11H5L3 8z"
						fill="var(--kingdom-gold)"
						stroke="var(--kingdom-gold-light)"
						stroke-width="0.8" />
				</svg>
				<div v-show="!isNarrow">
					<div class="brand-name">Kingdom Studio</div>
					<div class="brand-sub">个人开发者工作台</div>
				</div>
			</div>

			<el-menu
				:default-active="activeMenu"
				:collapse="isNarrow"
				:collapse-transition="false"
				router
				class="menu"
				background-color="transparent">
				<el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
					<el-icon><component :is="item.icon" /></el-icon>
					<template #title>{{ item.title }}</template>
				</el-menu-item>
				<el-sub-menu index="extensions">
					<template #title>
						<el-icon><MagicStick /></el-icon>
						<span>扩展</span>
					</template>
					<el-menu-item v-for="item in extensionMenus" :key="item.path" :index="item.path">
						<el-icon><component :is="item.icon" /></el-icon>
						<template #title>{{ item.title }}</template>
					</el-menu-item>
				</el-sub-menu>
			</el-menu>

			<div v-show="!isNarrow" class="sidebar-footer">v1.1.0</div>
		</el-aside>

		<el-container>
			<el-header class="header">
				<div class="header-title">{{ pageTitle }}</div>
				<div class="header-actions">
					<el-link href="/api/swagger-ui.html" target="_blank" type="primary">接口文档</el-link>
					<el-divider direction="vertical" />
					<el-link href="https://github.com/Dongxibie" target="_blank">GitHub</el-link>
				</div>
			</el-header>

			<el-main class="main">
				<router-view />
			</el-main>
		</el-container>
	</el-container>
</template>

<style scoped>
.layout {
	height: 100vh;
}

.sidebar {
	display: flex;
	flex-direction: column;
	background: linear-gradient(180deg, var(--kingdom-red-dark) 0%, #2a0b0d 100%);
	padding: 18px 12px;
}

.brand {
	display: flex;
	align-items: center;
	gap: 10px;
	padding: 6px 10px 18px;
	border-bottom: 1px solid rgba(240, 205, 114, 0.18);
	margin-bottom: 12px;
}

.crown {
	width: 28px;
	height: 28px;
	flex-shrink: 0;
}

.brand-name {
	color: #fff;
	font-weight: 700;
	letter-spacing: 0.4px;
	font-size: 16px;
}

.brand-sub {
	color: rgba(255, 255, 255, 0.55);
	font-size: 12px;
	margin-top: 2px;
}

.menu {
	border-right: none;
	flex: 1;
}

.menu :deep(.el-menu-item) {
	color: rgba(255, 255, 255, 0.72);
	border-radius: 10px;
	margin-bottom: 4px;
	height: 44px;
}

.menu :deep(.el-menu-item:hover) {
	background-color: rgba(240, 205, 114, 0.12);
	color: #fff;
}

.menu :deep(.el-menu-item.is-active) {
	background: linear-gradient(90deg, rgba(194, 150, 58, 0.9) 0%, rgba(194, 150, 58, 0.5) 100%);
	color: #fff;
	font-weight: 600;
}

.sidebar-footer {
	color: rgba(255, 255, 255, 0.35);
	font-size: 12px;
	text-align: center;
	padding-top: 12px;
}

.header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	background: #fff;
	border-bottom: 1px solid #ebeef5;
}

.header-title {
	font-size: 16px;
	font-weight: 600;
}

.header-actions {
	display: flex;
	align-items: center;
}

.main {
	padding: 20px;
	overflow-y: auto;
}

/* 窄屏：侧边栏由脚本切成 72px 图标条，这里同步收掉内边距（72 - 8 = 64，
   正好等于 el-menu 折叠宽度），让图标居中 */
@media (max-width: 900px) {
	.sidebar {
		padding: 14px 4px;
	}

	.brand {
		justify-content: center;
		padding: 4px 0 14px;
	}
}

@media (max-width: 600px) {
	.header {
		padding: 0 12px;
	}

	.main {
		padding: 12px;
	}
}
</style>
