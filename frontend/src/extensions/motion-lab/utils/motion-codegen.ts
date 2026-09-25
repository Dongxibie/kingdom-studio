import type { MotionCodeSet, MotionDetail, MotionListItem } from '@/extensions/motion-lab/types/motion'

/**
 * 动效代码生成器。
 *
 * 设计思路：不按分类硬编码几十套模板，而是**从资源自身的 CSS 反推演示 DOM**，
 * 再把它包成 Vue / React 组件。好处是采集器以后抓到的新动效（只要带 CSS）
 * 也能立刻生成四种产物，不需要为每个新分类补模板。
 */

/** 常见类名要生成几个元素（错位类动效需要多个子元素才能看出效果） */
const COUNT_HINT: Record<string, number> = {
	card: 4,
	item: 4,
	star: 5,
	sk: 3,
	bar: 3,
	dot: 5,
}

/** 从 CSS 里按出现顺序提取类名，过滤掉伪类与属性值里的碎片 */
export function extractClasses(css: string): string[] {
	const found: string[] = []
	const matched = css.matchAll(/\.([a-zA-Z][\w-]*)/g)
	for (const item of matched) {
		const name = item[1]
		if (!found.includes(name)) {
			found.push(name)
		}
	}
	return found
}

/** 按类名搭一棵最简 DOM：stage 当容器，其余当子元素 */
export function buildDemoNodes(css: string): { cls: string; count: number }[] {
	const classes = extractClasses(css)
	if (classes.length === 0) {
		return [{ cls: 'stage', count: 1 }, { cls: 'box', count: 1 }]
	}
	const root = classes.includes('stage') ? 'stage' : classes[0]
	const rest = classes.filter((name) => name !== root)
	return [{ cls: root, count: 1 }, ...rest.map((cls) => ({ cls, count: COUNT_HINT[cls] ?? 1 }))].slice(0, 7)
}

/** 预览用的完整 HTML（沙箱 iframe 的 srcdoc） */
export function buildPreviewHtml(css: string, background = '#101319'): string {
	const nodes = buildDemoNodes(css)
	const root = nodes[0]
	const children = nodes.slice(1)
	const body = children
		.map((node) => Array.from({ length: node.count }, () => '  <div class="' + node.cls + '"></div>').join('\n'))
		.join('\n')
	const stage = ['<div class="' + root.cls + '">', body, '</div>'].join('\n')
	return [
		'<!DOCTYPE html><html lang="zh-CN"><head><meta charset="utf-8">',
		'<style>',
		'html,body{height:100%;margin:0}',
		'body{display:grid;place-items:center;background:' + background + ';',
		'  font-family:Inter,"PingFang SC","Microsoft YaHei",system-ui,sans-serif;overflow:hidden}',
		'.stage,.box{width:100%;height:100%}',
		css,
		'</style></head><body>',
		stage,
		'</body></html>',
	].join('\n')
}


/** 生成 Vue 3 单文件组件：把 CSS 原样放进 style，模板按类名搭出来 */
export function generateVue(name: string, css: string): string {
	const nodes = buildDemoNodes(css)
	const root = nodes[0]
	const children = nodes.slice(1)
	const body = children
		.map((node) => Array.from({ length: node.count }, () => '    <div class="' + node.cls + '" />').join('\n'))
		.join('\n')
	return [
		'<script setup lang="ts">',
		'// ' + name,
		'// 用法：把下面的样式换到自己的组件里即可，模板只是演示用的骨架',
		'</script>',
		'',
		'<template>',
		'  <div class="' + root.cls + '">',
		body,
		'  </div>',
		'</template>',
		'',
		'<style scoped>',
		css,
		'</style>',
	].join('\n')
}

/** 生成 React 组件（CSS 用 style 标签内联，便于直接粘贴） */
export function generateReact(name: string, css: string): string {
	const nodes = buildDemoNodes(css)
	const root = nodes[0]
	const children = nodes.slice(1)
	const body = children
		.map((node) => Array.from({ length: node.count }, () => '      <div className="' + node.cls + '" />').join('\n'))
		.join('\n')
	return [
		'// ' + name,
		'const STYLE = `',
		css,
		'`',
		'',
		'export default function MotionDemo() {',
		'  return (',
		'    <>',
		'      <style>{STYLE}</style>',
		'      <div className="' + root.cls + '">',
		body,
		'      </div>',
		'    </>',
		'  )',
		'}',
	].join('\n')
}

/** Three.js 只在三维 / 粒子类目下生成，其它分类返回 null（前端会给出提示） */
export function generateThree(category: string): string | null {
	if (category === '3D') {
		return [
			'// Three.js 版本：鼠标驱动平面旋转（真正的三维效果建议用这个而不是 CSS）',
			"import * as THREE from 'three'",
			'',
			'const scene = new THREE.Scene()',
			'const camera = new THREE.PerspectiveCamera(45, 1, 0.1, 100)',
			'camera.position.z = 3',
			'const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })',
			'renderer.setSize(window.innerWidth, window.innerHeight)',
			'document.body.appendChild(renderer.domElement)',
			'',
			'const mesh = new THREE.Mesh(',
			'  new THREE.PlaneGeometry(1.7, 1.1),',
			'  new THREE.MeshBasicMaterial({ color: 0xc2963a, transparent: true, opacity: 0.35 })',
			')',
			'scene.add(mesh)',
			'',
			'window.addEventListener("mousemove", (event) => {',
			'  const dx = event.clientX / window.innerWidth - 0.5',
			'  const dy = event.clientY / window.innerHeight - 0.5',
			'  mesh.rotation.y = dx * 0.35',
			'  mesh.rotation.x = -dy * 0.25',
			'})',
			'',
			'renderer.setAnimationLoop(() => renderer.render(scene, camera))',
		].join('\n')
	}
	if (category === 'Particle') {
		return [
			'// Three.js 粒子版本：用 Points 承载上千粒子，比 Canvas 逐点画更适合大规模场景',
			"import * as THREE from 'three'",
			'',
			'const scene = new THREE.Scene()',
			'const camera = new THREE.PerspectiveCamera(60, 1, 0.1, 100)',
			'camera.position.z = 6',
			'const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })',
			'renderer.setSize(window.innerWidth, window.innerHeight)',
			'document.body.appendChild(renderer.domElement)',
			'',
			'const COUNT = 1200',
			'const positions = new Float32Array(COUNT * 3)',
			'for (let i = 0; i < COUNT; i++) {',
			'  positions[i * 3] = (Math.random() - 0.5) * 12',
			'  positions[i * 3 + 1] = (Math.random() - 0.5) * 8',
			'  positions[i * 3 + 2] = (Math.random() - 0.5) * 8',
			'}',
			'const geometry = new THREE.BufferGeometry()',
			'geometry.setAttribute("position", new THREE.BufferAttribute(positions, 3))',
			'scene.add(new THREE.Points(geometry, new THREE.PointsMaterial({',
			'  color: 0x9fb6d6, size: 0.03, transparent: true, opacity: 0.9',
			'})))',
			'',
			'renderer.setAnimationLoop(() => {',
			'  scene.rotation.y += 0.0015',
			'  renderer.render(scene, camera)',
			'})',
		].join('\n')
	}
	return null
}

/** 根据资源信息拼一段可复用的提示词骨架 */
export function generatePrompt(resource: {
	name: string
	category: string
	description: string
	tags: string[]
	technology: string
}): string {
	const lines = [
		'做一个「' + resource.name + '」动效（' + resource.category + ' 类）：',
		'- 效果：' + (resource.description || '按名称与分类实现'),
		'- 技术：' + (resource.technology || 'CSS'),
	]
	if (resource.tags.length) {
		lines.push('- 风格关键词：' + resource.tags.join('、'))
	}
	lines.push('- 缓动优先 cubic-bezier(.16,.84,.24,1)，时长 200~900ms')
	lines.push('- 尊重 prefers-reduced-motion：开启时只呈现静态结果')
	lines.push('- 输出 Vue 3 <script setup> 版本，样式放 scoped')
	return lines.join('\n')
}

/** 一键生成：缺哪个补哪个，已有的不动（避免覆盖用户手写的内容） */
export function generateMissing(
	resource: Pick<MotionDetail, 'name' | 'category' | 'description' | 'tags' | 'technology'>,
	current: MotionCodeSet,
): { next: MotionCodeSet; filled: string[] } {
	const next: MotionCodeSet = { ...current }
	const filled: string[] = []
	const css = current.cssCode || ''
	if (!next.prompt) {
		next.prompt = generatePrompt(resource)
		filled.push('Prompt')
	}
	if (css && !next.vueCode) {
		next.vueCode = generateVue(resource.name, css)
		filled.push('Vue 3')
	}
	if (css && !next.reactCode) {
		next.reactCode = generateReact(resource.name, css)
		filled.push('React')
	}
	const three = generateThree(resource.category)
	if (!next.threeCode && three) {
		next.threeCode = three
		filled.push('Three.js')
	}
	return { next, filled }
}

/** 列表项 / 详情都能用来生成，统一取字段 */
export function canGenerateThree(category: string): boolean {
	return category === '3D' || category === 'Particle'
}

export type { MotionCodeSet, MotionListItem }
