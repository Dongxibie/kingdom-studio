/**
 * 扩展层公共组件的统一出口。
 *
 * 只导出组件、不导出类型：类型请从 @/extensions/_shared/types/common 直接引入，
 * 避免 barrel 里混入类型后在 isolatedModules 下出现重导出问题。
 */
export { default as ExtShell } from '@/extensions/_shared/components/ExtShell.vue'
export { default as ExtEmpty } from '@/extensions/_shared/components/ExtEmpty.vue'
export { default as ExtCodeBlock } from '@/extensions/_shared/components/ExtCodeBlock.vue'
export { default as ExtStatusTag } from '@/extensions/_shared/components/ExtStatusTag.vue'
