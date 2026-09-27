/**
 * 应用信息。
 *
 * 版本号由 vite.config.ts 从 frontend/package.json 注入（`__APP_VERSION__`），
 * 这里转成一个普通模块导出 —— 模板里直接用全局常量 vue-tsc 检查不到，
 * 走 import 既能被类型检查覆盖，单元测试里也好替换。
 */
export const APP_VERSION = typeof __APP_VERSION__ === 'string' ? __APP_VERSION__ : '0.0.0'

/** 页面页脚展示用，带 v 前缀 */
export const APP_VERSION_TAG = 'v' + APP_VERSION
