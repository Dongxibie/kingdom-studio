/** 扩展层公共类型：所有扩展模块共用，避免各自定义一份。 */

/** 后端各扩展模块 /ping 返回的模块信息（见 modules 下各模块 vo 包里的 ModuleVO） */
export interface ExtModuleInfo {
	module: string
	name: string
	englishName: string
	phase: string
	apiBase: string
	capabilities: string[]
	plannedTables: string[]
}

/** 状态标签的色调：金=强调、红=问题、绿=正常、灰=中性 */
export type ExtStatusTone = 'gold' | 'red' | 'ok' | 'mute'

/** 三栏里的栏位标识，供布局与后续键盘导航使用 */
export type ExtColumn = 'left' | 'center' | 'right'
