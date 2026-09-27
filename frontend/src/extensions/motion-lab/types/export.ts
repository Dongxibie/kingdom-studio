/**
 * 代码导出的数据类型（Phase 5）。
 *
 * 与后端 modules/motion/template/vo/MotionExportVO 一一对应。
 */

/** 一个产物文件 */
export interface ExportFile {
	/** 建议路径，例如 src/components/MotionPlan.vue */
	path: string
	/** vue / tsx / ts / css / html */
	language: string
	/** 这个文件是干嘛的 */
	role: string
	content: string
	bytes: number
}

/** 导出结果：一套方案的完整代码产物 */
export interface MotionExport {
	planName: string
	recipeKey: string
	/** VUE / REACT / HTML */
	format: string
	formatLabel: string
	stepCount: number
	files: ExportFile[]
	/** 组合预览：就是导出结果的样子，塞进沙箱 iframe 看 */
	previewHtml: string
	notes: string[]
}

/** 导出格式选项 */
export const EXPORT_FORMATS: { label: string; value: string; hint: string }[] = [
	{ label: 'Vue 3', value: 'VUE', hint: '每个动效一个 SFC（style scoped 隔离）+ 方案组件 + 配置' },
	{ label: 'React', value: 'REACT', hint: '每个动效一个 TSX + 各自作用域化的 CSS + 方案组件' },
	{ label: 'HTML + CSS', value: 'HTML', hint: '单页 index.html + motion.css，双击就能打开' },
]
