#!/usr/bin/env node
/**
 * 版本号同步：以 frontend/package.json 为唯一来源，把版本写到其它三处。
 *
 *   package.json（唯一手改的地方）
 *        ↓
 *   backend/pom.xml                   项目版本
 *   backend/.../application.yml       info.app.version → 健康检查、接口文档、模块自检都读它
 *   README.md                         版本徽章、当前版本行、模块状态表
 *
 * 用法：npm run version:sync（在 frontend 目录下执行；root 目录也能跑）
 */
import { readFileSync, writeFileSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const here = dirname(fileURLToPath(import.meta.url))
const frontendDir = resolve(here, '..')
const rootDir = resolve(frontendDir, '..')

const pkgPath = join(frontendDir, 'package.json')
const pomPath = join(rootDir, 'backend', 'pom.xml')
const ymlPath = join(rootDir, 'backend', 'src', 'main', 'resources', 'application.yml')
const readmePath = join(rootDir, 'README.md')

const pkg = JSON.parse(readFileSync(pkgPath, 'utf8'))
const version = pkg.version
if (!/^\d+\.\d+\.\d+$/.test(version)) {
	throw new Error(`package.json 里的版本号不是 x.y.z 形式：${version}`)
}
const tag = `v${version}`

/** 替换前先确认目标文本存在，避免「看起来跑成功了、其实什么都没改」 */
function replaceOnce(content, pattern, replacement, label) {
	if (!pattern.test(content)) {
		throw new Error(`没有在 ${label} 里找到要替换的版本号，请检查文件格式`)
	}
	return content.replace(pattern, replacement)
}

// 1. 后端项目版本
let pom = readFileSync(pomPath, 'utf8')
pom = replaceOnce(
	pom,
	/(<artifactId>kingdom-studio-backend<\/artifactId>\s*<version>)[^<]+(<\/version>)/,
	`$1${version}$2`,
	'backend/pom.xml'
)
writeFileSync(pomPath, pom)

// 2. 后端运行时读到的版本（健康检查 / 接口文档 / 模块自检共用）
let yml = readFileSync(ymlPath, 'utf8')
yml = replaceOnce(yml, /(info:\s*\n\s*app:\s*\n\s*name: [^\n]+\n\s*version: )\S+/, `$1${tag}`, 'application.yml')
writeFileSync(ymlPath, yml)

// 3. README：徽章、当前版本行、模块状态表
let readme = readFileSync(readmePath, 'utf8')
readme = replaceOnce(readme, /(!\[版本\]\(https:\/\/img\.shields\.io\/badge\/[^)]*\))/, `![版本](https://img.shields.io/badge/版本-${tag}-c2963a)`, 'README.md 版本徽章')
readme = replaceOnce(readme, /(当前版本 \*\*)v\d+\.\d+\.\d+(\*\*)/, `$1${tag}$2`, 'README.md 当前版本行')
readme = replaceOnce(readme, /(\| 扩展 · 动效基因库 \|[\s\S]*?\| ✅ )v\d+\.\d+\.\d+( \|)/, `$1${tag}$2`, 'README.md 动效状态')
readme = replaceOnce(readme, /(\| 扩展 · 音乐 Agent \|[\s\S]*?\| ✅ )v\d+\.\d+\.\d+( \|)/, `$1${tag}$2`, 'README.md 音乐状态')
readme = replaceOnce(readme, /(\| 扩展 · 桌面代理 \|[\s\S]*?\| ✅ )v\d+\.\d+\.\d+( \|)/, `$1${tag}$2`, 'README.md 桌面代理状态')
writeFileSync(readmePath, readme)

console.log(`版本已同步：${tag}`)
console.log(`  backend/pom.xml`)
console.log(`  backend/src/main/resources/application.yml`)
console.log(`  README.md`)
