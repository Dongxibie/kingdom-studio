import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
	{
		path: '/',
		redirect: '/dashboard'
	},
	{
		path: '/dashboard',
		name: 'Dashboard',
		component: () => import('@/views/DashboardView.vue'),
		meta: { title: '工作台', icon: 'HomeFilled' }
	},
	{
		path: '/projects',
		name: 'ProjectKingdom',
		component: () => import('@/views/ModulePlaceholder.vue'),
		props: {
			title: '项目王国',
			subtitle: 'Project Kingdom',
			description: '管理个人项目：名称、技术栈、完成度、GitHub 地址。每个项目都是一座建筑。',
			phase: '后续版本'
		},
		meta: { title: '项目王国', icon: 'OfficeBuilding' }
	},
	{
		path: '/technologies',
		name: 'TechnologyLibrary',
		component: () => import('@/views/ModulePlaceholder.vue'),
		props: {
			title: '技术图鉴',
			subtitle: 'Technology Library',
			description: '记录学习中的技术：分类、掌握程度、学习日期，形成自己的技能卡片墙。',
			phase: '后续版本'
		},
		meta: { title: '技术图鉴', icon: 'Collection' }
	},
	{
		path: '/journey',
		name: 'RoyalJourney',
		component: () => import('@/views/ModulePlaceholder.vue'),
		props: {
			title: '成长时间线',
			subtitle: 'Royal Journey',
			description: '按年份记录成长节点：Java Apprentice → Backend Knight → AI Kingdom Builder。',
			phase: '后续版本'
		},
		meta: { title: '成长时间线', icon: 'Clock' }
	},
	{
		path: '/code-library',
		name: 'CodeLibrary',
		component: () => import('@/views/ModulePlaceholder.vue'),
		props: {
			title: '代码知识库',
			subtitle: 'Code Library',
			description: '沉淀常用代码片段：Java / SQL / JavaScript，支持新增、查看、删除。',
			phase: '后续版本'
		},
		meta: { title: '代码知识库', icon: 'Document' }
	},
	{
		// 扩展模块：与上面四个业务模块平级，统一挂在 /extensions 下，
		// 后续新增扩展（AI Agent Lab、Prompt Tools 等）继续往这里追加即可
		path: '/extensions/motion-lab',
		name: 'KingdomMotionLab',
		component: () => import('@/extensions/motion-lab/views/MotionLabView.vue'),
		meta: { title: '动效工作台', icon: 'MagicStick' }
	},
	{
		// 动效资源页：工作台之外的「我的资源」（自建与采集的资源）。
		// 从工作台右上角进入，不占侧边栏菜单位，避免两个入口互相抢注意力。
		path: '/extensions/motion-lab/candidates',
		name: 'motion-lab-candidates',
		meta: { title: '动效候选池' },
		component: () => import('@/extensions/motion-lab/views/MotionCandidateView.vue'),
	},
	{
		// AI 设计页：一句需求 → 完整方案（组合 + 每一步 + 参数建议 + 逐条说明）。
		path: '/extensions/motion-lab/design',
		name: 'motion-lab-design',
		meta: { title: 'AI 设计' },
		component: () => import('@/extensions/motion-lab/views/MotionDesignView.vue'),
	},
	{
		// 智能推荐页：一句话需求 → 五轴意图 → Top N 与理由。
		// 从工作台右上角进入，与候选池一样不占侧边栏菜单位。
		path: '/extensions/motion-lab/recommend',
		name: 'motion-lab-recommend',
		meta: { title: '智能推荐' },
		component: () => import('@/extensions/motion-lab/views/MotionRecommendView.vue'),
	},
	{
		path: '/extensions/motion-lab/resources',
		name: 'KingdomMotionResources',
		component: () => import('@/extensions/motion-lab/views/MotionResourceView.vue'),
		meta: { title: '我的资源', icon: 'MagicStick' }
	},
	{
		path: '/extensions/music-agent',
		name: 'KingdomMusicAgent',
		component: () => import('@/extensions/music-agent/views/MusicAgentView.vue'),
		meta: { title: '音乐 Agent', icon: 'Headset' }
	},
	{
		// 兜底路由：未匹配到任何页面时显示 404，避免出现空白页
		path: '/:pathMatch(.*)*',
		name: 'NotFound',
		component: () => import('@/views/NotFoundView.vue'),
		meta: { title: '页面不存在' }
	}
]

const router = createRouter({
	history: createWebHistory(),
	routes
})

router.afterEach(to => {
	const title = (to.meta.title as string) || 'Kingdom Studio'
	document.title = `${title} · Kingdom Studio`
})

export default router
