-- =====================================================================
-- Kingdom Studio · Extensions / Motion Lab 种子数据
-- 10 个分类各一条，全部是自建示例（MIT），离线可用；不依赖任何外部采集。
--
-- content_hash 用 SQL 现算，算法与 MotionService#hashOf 一致：
--   SHA1(LOWER(TRIM(name)) | LOWER(TRIM(source_url)) | category)
-- 因此从界面新增同名资源时会正常撞唯一键，不会产生重复数据。
-- 可重复执行：按唯一键 upsert。
-- =====================================================================

USE `kingdom_studio`;

-- 1. Entrance / 玻璃卡片错位入场
INSERT INTO `motion_resource`
	(`name`, `description`, `category`, `technology`, `source_url`, `repo_url`, `preview_url`, `tags`, `license`, `code_path`, `content_hash`, `status`)
VALUES (
	'玻璃卡片错位入场', '一组玻璃卡片依次上浮出现，间隔 70ms，带轻微模糊收尾。', 'Entrance', 'CSS',
	'https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#entrance', 'https://github.com/Dongxibie/kingdom-studio', 'inline:css', 'entrance,stagger,glass', 'MIT（自建示例）', 'docs/extensions-samples.md',
	SHA1(CONCAT(LOWER(TRIM('玻璃卡片错位入场')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#entrance')), '|', 'Entrance')), 'READY'
)
ON DUPLICATE KEY UPDATE
	`description` = VALUES(`description`), `technology` = VALUES(`technology`),
	`tags` = VALUES(`tags`), `license` = VALUES(`license`), `status` = VALUES(`status`);

INSERT INTO `motion_code` (`motion_id`, `prompt`, `vue_code`, `react_code`, `css_code`, `three_code`)
SELECT r.`id`, '做一个卡片错位入场动效：
- 卡片依次从下方 22px 浮上来，间隔 70ms
- 起始带 3px 模糊，结束时清晰
- 缓动 cubic-bezier(.16,.84,.24,1)，时长 700ms',
	'<script setup lang="ts">
const items = [1, 2, 3, 4]
</script>

<template>
  <div class="row">
    <div v-for="(item, index) in items" :key="item" class="card"
         :style="{ animationDelay: index * 70 + ''ms'' }" />
  </div>
</template>

<style scoped>
.card {
  width: 74px;
  height: 74px;
  border-radius: 14px;
  animation: enter 0.7s cubic-bezier(0.16, 0.84, 0.24, 1) both;
}
@keyframes enter {
  from { opacity: 0; transform: translateY(22px); filter: blur(3px); }
  to { opacity: 1; transform: none; filter: none; }
}
</style>', 'export default function StaggerRow() {
  return (
    <div className="row">
      {[0, 1, 2, 3].map((i) => (
        <div
          key={i}
          className="card"
          style={{ animationDelay: `${i * 70}ms` }}
        />
      ))}
    </div>
  )
}', '.stage{display:flex;gap:10px;justify-content:center;align-items:center;height:100%}
.card{width:74px;height:74px;border-radius:14px;
  border:1px solid rgba(255,255,255,.16);
  background:linear-gradient(160deg,rgba(255,255,255,.14),rgba(255,255,255,.04));
  backdrop-filter:blur(8px);
  animation:enter .7s cubic-bezier(.16,.84,.24,1) both}
.card:nth-child(2){animation-delay:.07s}
.card:nth-child(3){animation-delay:.14s}
.card:nth-child(4){animation-delay:.21s}
@keyframes enter{
  from{opacity:0;transform:translateY(22px);filter:blur(3px)}
  to{opacity:1;transform:none;filter:none}}', NULL
FROM `motion_resource` r
WHERE r.`content_hash` = SHA1(CONCAT(LOWER(TRIM('玻璃卡片错位入场')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#entrance')), '|', 'Entrance'))
	AND NOT EXISTS (SELECT 1 FROM `motion_code` c WHERE c.`motion_id` = r.`id`);

-- 2. Hover / 卡片光线扫过
INSERT INTO `motion_resource`
	(`name`, `description`, `category`, `technology`, `source_url`, `repo_url`, `preview_url`, `tags`, `license`, `code_path`, `content_hash`, `status`)
VALUES (
	'卡片光线扫过', '鼠标悬停时一道斜向光线扫过卡片表面，同时轻微上浮。', 'Hover', 'CSS',
	'https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#hover', 'https://github.com/Dongxibie/kingdom-studio', 'inline:css', 'hover,sweep,premium', 'MIT（自建示例）', 'docs/extensions-samples.md',
	SHA1(CONCAT(LOWER(TRIM('卡片光线扫过')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#hover')), '|', 'Hover')), 'READY'
)
ON DUPLICATE KEY UPDATE
	`description` = VALUES(`description`), `technology` = VALUES(`technology`),
	`tags` = VALUES(`tags`), `license` = VALUES(`license`), `status` = VALUES(`status`);

INSERT INTO `motion_code` (`motion_id`, `prompt`, `vue_code`, `react_code`, `css_code`, `three_code`)
SELECT r.`id`, '做一个高级感的卡片悬停动效：
- 悬浮时上浮 6px
- 一道 105 度的光线从左到右扫过，时长 900ms
- 缓动 cubic-bezier(.2,.7,.2,1)',
	'<template>
  <div class="card"><span>悬停看看</span></div>
</template>

<style scoped>
.card {
  position: relative;
  overflow: hidden;
  transition: transform 0.35s cubic-bezier(0.2, 0.7, 0.2, 1);
}
.card:hover { transform: translateY(-6px); }
.card::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(105deg, transparent 34%, rgba(255, 255, 255, 0.28) 50%, transparent 66%);
  transform: translateX(-130%);
}
.card:hover::after { animation: sweep 0.9s cubic-bezier(0.2, 0.7, 0.2, 1); }
@keyframes sweep { to { transform: translateX(130%); } }
</style>', NULL, '.stage{display:flex;justify-content:center;align-items:center;height:100%}
.card{position:relative;width:180px;height:110px;border-radius:16px;overflow:hidden;
  border:1px solid rgba(255,255,255,.16);
  background:linear-gradient(160deg,rgba(255,255,255,.12),rgba(255,255,255,.03));
  transition:transform .35s cubic-bezier(.2,.7,.2,1),box-shadow .35s}
.card::after{content:"";position:absolute;inset:0;
  background:linear-gradient(105deg,transparent 34%,rgba(255,255,255,.28) 50%,transparent 66%);
  transform:translateX(-130%);animation:sweep 2.4s cubic-bezier(.2,.7,.2,1) infinite}
.card:hover{transform:translateY(-6px)}
@keyframes sweep{
  0%{transform:translateX(-130%)}
  55%,100%{transform:translateX(130%)}}', NULL
FROM `motion_resource` r
WHERE r.`content_hash` = SHA1(CONCAT(LOWER(TRIM('卡片光线扫过')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#hover')), '|', 'Hover'))
	AND NOT EXISTS (SELECT 1 FROM `motion_code` c WHERE c.`motion_id` = r.`id`);

-- 3. Scroll / 滚动逐级点亮
INSERT INTO `motion_resource`
	(`name`, `description`, `category`, `technology`, `source_url`, `repo_url`, `preview_url`, `tags`, `license`, `code_path`, `content_hash`, `status`)
VALUES (
	'滚动逐级点亮', '列表进入视口后依次点亮，滚动离开不影响已点亮状态。', 'Scroll', 'IntersectionObserver',
	'https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#scroll', 'https://github.com/Dongxibie/kingdom-studio', 'inline:css', 'scroll,reveal,list', 'MIT（自建示例）', 'docs/extensions-samples.md',
	SHA1(CONCAT(LOWER(TRIM('滚动逐级点亮')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#scroll')), '|', 'Scroll')), 'READY'
)
ON DUPLICATE KEY UPDATE
	`description` = VALUES(`description`), `technology` = VALUES(`technology`),
	`tags` = VALUES(`tags`), `license` = VALUES(`license`), `status` = VALUES(`status`);

INSERT INTO `motion_code` (`motion_id`, `prompt`, `vue_code`, `react_code`, `css_code`, `three_code`)
SELECT r.`id`, '做一个滚动逐级点亮的效果：
- 元素进入视口时从透明到显现，位移 24px
- 用 IntersectionObserver，不要监听 scroll 事件（性能）
- 已点亮的元素不要因为再次滚动而闪烁',
	'<script setup lang="ts">
import { onMounted, ref } from ''vue''

const list = [''Java 21'', ''Spring Boot 3'', ''MyBatis-Plus'', ''Redis'']
const shown = ref<number>(-1)
const listRef = ref<HTMLElement | null>(null)

onMounted(() => {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return
        // 已点亮的不回退，避免滚动抖动
        shown.value = Math.max(shown.value, Number((entry.target as HTMLElement).dataset.index))
        observer.unobserve(entry.target)
      })
    },
    { rootMargin: ''-10% 0px -10% 0px'' }
  )
  listRef.value?.querySelectorAll(''.item'').forEach((el) => observer.observe(el))
})
</script>

<template>
  <ul ref="listRef">
    <li v-for="(item, index) in list" :key="item" class="item"
        :data-index="index" :class="{ on: index <= shown }">{{ item }}</li>
  </ul>
</template>', NULL, '.stage{display:flex;flex-direction:column;gap:10px;justify-content:center;height:100%;padding:0 40px}
.item{height:34px;border-radius:9px;display:flex;align-items:center;padding:0 14px;
  font:12px/1 ui-monospace,monospace;color:#cfd6df;
  border:1px solid rgba(255,255,255,.12);background:rgba(255,255,255,.05);
  opacity:0;transform:translateY(24px);
  animation:light .8s cubic-bezier(.16,.84,.24,1) both}
.item:nth-child(2){animation-delay:.12s}
.item:nth-child(3){animation-delay:.24s}
.item:nth-child(4){animation-delay:.36s}
@keyframes light{to{opacity:1;transform:none;border-color:rgba(194,150,58,.7)}}', NULL
FROM `motion_resource` r
WHERE r.`content_hash` = SHA1(CONCAT(LOWER(TRIM('滚动逐级点亮')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#scroll')), '|', 'Scroll'))
	AND NOT EXISTS (SELECT 1 FROM `motion_code` c WHERE c.`motion_id` = r.`id`);

-- 4. Text / 打字机标题
INSERT INTO `motion_resource`
	(`name`, `description`, `category`, `technology`, `source_url`, `repo_url`, `preview_url`, `tags`, `license`, `code_path`, `content_hash`, `status`)
VALUES (
	'打字机标题', '一段文字逐字写出，末尾光标闪烁。', 'Text', 'JavaScript',
	'https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#text', 'https://github.com/Dongxibie/kingdom-studio', 'inline:css', 'text,typing,cursor', 'MIT（自建示例）', 'docs/extensions-samples.md',
	SHA1(CONCAT(LOWER(TRIM('打字机标题')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#text')), '|', 'Text')), 'READY'
)
ON DUPLICATE KEY UPDATE
	`description` = VALUES(`description`), `technology` = VALUES(`technology`),
	`tags` = VALUES(`tags`), `license` = VALUES(`license`), `status` = VALUES(`status`);

INSERT INTO `motion_code` (`motion_id`, `prompt`, `vue_code`, `react_code`, `css_code`, `three_code`)
SELECT r.`id`, '做一个打字机文字动效：
- 每帧推进若干字符，整段在 1.2 秒左右写完
- 末尾竖线光标闪烁，频率 500ms
',
	'<script setup lang="ts">
import { onBeforeUnmount, ref } from ''vue''

const text = ''把想法做成能跑起来的东西''
const shown = ref('''')
let timer: number | null = null
let index = 0

timer = window.setInterval(() => {
  shown.value = text.slice(0, ++index)
  if (index >= text.length && timer) window.clearInterval(timer)
}, 60)

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<template>
  <p class="type">{{ shown }}<span class="caret" /></p>
</template>', NULL, '.stage{display:flex;align-items:center;justify-content:center;height:100%}
.type{font:15px/1.8 ui-monospace,monospace;color:#e8eaed;white-space:nowrap;
  overflow:hidden;border-right:2px solid #c2963a;
  width:0;animation:type 1.6s steps(18,end) forwards,caret .5s step-end infinite alternate}
@keyframes type{to{width:18ch}}
@keyframes caret{50%{border-color:transparent}}', NULL
FROM `motion_resource` r
WHERE r.`content_hash` = SHA1(CONCAT(LOWER(TRIM('打字机标题')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#text')), '|', 'Text'))
	AND NOT EXISTS (SELECT 1 FROM `motion_code` c WHERE c.`motion_id` = r.`id`);

-- 5. Particle / 粒子星野与近邻连线
INSERT INTO `motion_resource`
	(`name`, `description`, `category`, `technology`, `source_url`, `repo_url`, `preview_url`, `tags`, `license`, `code_path`, `content_hash`, `status`)
VALUES (
	'粒子星野与近邻连线', '节点缓慢漂移，距离足够近时自动连线，鼠标附近会点亮。', 'Particle', 'Canvas',
	'https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#particle', 'https://github.com/Dongxibie/kingdom-studio', 'inline:css', 'particle,links,starfield', 'MIT（自建示例）', 'docs/extensions-samples.md',
	SHA1(CONCAT(LOWER(TRIM('粒子星野与近邻连线')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#particle')), '|', 'Particle')), 'READY'
)
ON DUPLICATE KEY UPDATE
	`description` = VALUES(`description`), `technology` = VALUES(`technology`),
	`tags` = VALUES(`tags`), `license` = VALUES(`license`), `status` = VALUES(`status`);

INSERT INTO `motion_code` (`motion_id`, `prompt`, `vue_code`, `react_code`, `css_code`, `three_code`)
SELECT r.`id`, '做一个粒子星野动效（Canvas）：
- 数量可控，位置与速度随机，边界回绕
- 两点距离小于阈值时连线，透明度按距离衰减
- 鼠标附近 160px 内的节点变亮并放大
- 支持 prefers-reduced-motion：开启时只画一帧静态图',
	'<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from ''vue''

const canvasRef = ref<HTMLCanvasElement | null>(null)
let raf = 0

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext(''2d'')
  if (!ctx) return
  const reduced = window.matchMedia(''(prefers-reduced-motion: reduce)'').matches
  const dpr = Math.min(2, window.devicePixelRatio || 1)

  let width = 0
  let height = 0
  const nodes = Array.from({ length: 46 }, () => ({
    x: Math.random(), y: Math.random(),
    vx: (Math.random() - 0.5) * 2, vy: (Math.random() - 0.5) * 2
  }))

  function resize() {
    const rect = canvas!.getBoundingClientRect()
    width = rect.width
    height = rect.height
    canvas!.width = width * dpr
    canvas!.height = height * dpr
    ctx!.setTransform(dpr, 0, 0, dpr, 0, 0)
  }

  function paint(dt: number) {
    ctx!.clearRect(0, 0, width, height)
    for (const n of nodes) {
      n.x += n.vx * dt * 0.00002
      n.y += n.vy * dt * 0.00002
      if (n.x < 0 || n.x > 1) n.vx *= -1
      if (n.y < 0 || n.y > 1) n.vy *= -1
    }
    ctx!.lineWidth = 1
    for (let i = 0; i < nodes.length; i++) {
      for (let j = i + 1; j < nodes.length; j++) {
        const a = nodes[i]
        const b = nodes[j]
        const distance = Math.hypot((a.x - b.x) * width, (a.y - b.y) * height)
        if (distance < 170) {
          ctx!.beginPath()
          ctx!.moveTo(a.x * width, a.y * height)
          ctx!.lineTo(b.x * width, b.y * height)
          ctx!.strokeStyle = `rgba(126,160,200,${(1 - distance / 170) * 0.25})`
          ctx!.stroke()
        }
      }
    }
    for (const n of nodes) {
      ctx!.beginPath()
      ctx!.arc(n.x * width, n.y * height, 2, 0, Math.PI * 2)
      ctx!.fillStyle = ''#9fb6d6''
      ctx!.fill()
    }
  }

  resize()
  window.addEventListener(''resize'', resize)
  if (reduced) {
    paint(0)
  } else {
    let last = performance.now()
    const loop = (now: number) => {
      const dt = Math.min(50, now - last)
      last = now
      paint(dt)
      raf = requestAnimationFrame(loop)
    }
    raf = requestAnimationFrame(loop)
  }
  onBeforeUnmount(() => {
    if (raf) cancelAnimationFrame(raf)
    window.removeEventListener(''resize'', resize)
  })
})
</script>

<template>
  <canvas ref="canvasRef" class="stage" />
</template>', NULL, '.stage{position:relative;height:100%;overflow:hidden;
  background:radial-gradient(120% 100% at 50% 0%,#1a2130,#0e131c 70%)}
.star{position:absolute;width:3px;height:3px;border-radius:50%;background:#cfe0ff;
  box-shadow:0 0 8px 1px rgba(160,190,255,.7);animation:twinkle 3s ease-in-out infinite}
.star:nth-child(1){left:18%;top:30%}
.star:nth-child(2){left:42%;top:58%;animation-delay:.7s}
.star:nth-child(3){left:68%;top:34%;animation-delay:1.3s}
.star:nth-child(4){left:78%;top:68%;animation-delay:2s}
.star:nth-child(5){left:30%;top:76%;animation-delay:1s}
@keyframes twinkle{0%,100%{opacity:.35;transform:scale(.8)}50%{opacity:1;transform:scale(1.3)}}', NULL
FROM `motion_resource` r
WHERE r.`content_hash` = SHA1(CONCAT(LOWER(TRIM('粒子星野与近邻连线')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#particle')), '|', 'Particle'))
	AND NOT EXISTS (SELECT 1 FROM `motion_code` c WHERE c.`motion_id` = r.`id`);

-- 6. 3D / 卡片 3D 倾斜
INSERT INTO `motion_resource`
	(`name`, `description`, `category`, `technology`, `source_url`, `repo_url`, `preview_url`, `tags`, `license`, `code_path`, `content_hash`, `status`)
VALUES (
	'卡片 3D 倾斜', '鼠标在卡片上移动时整块轻微倾斜，带透视和抬升。', '3D', 'CSS 3D',
	'https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#3d', 'https://github.com/Dongxibie/kingdom-studio', 'inline:css', '3d,tilt,perspective', 'MIT（自建示例）', 'docs/extensions-samples.md',
	SHA1(CONCAT(LOWER(TRIM('卡片 3D 倾斜')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#3d')), '|', '3D')), 'READY'
)
ON DUPLICATE KEY UPDATE
	`description` = VALUES(`description`), `technology` = VALUES(`technology`),
	`tags` = VALUES(`tags`), `license` = VALUES(`license`), `status` = VALUES(`status`);

INSERT INTO `motion_code` (`motion_id`, `prompt`, `vue_code`, `react_code`, `css_code`, `three_code`)
SELECT r.`id`, '做一个卡片 3D 倾斜动效：
- 鼠标位置决定 rotateX/rotateY，幅度不超过 6 度
- perspective 900px，悬浮时抬升 6px
- 离开时归位，过渡 0.18s ease-out',
	'<script setup lang="ts">
import { ref } from ''vue''

const cardRef = ref<HTMLElement | null>(null)

function onMove(event: MouseEvent) {
  const card = cardRef.value
  if (!card) return
  const rect = card.getBoundingClientRect()
  const dx = (event.clientX - rect.left) / rect.width - 0.5
  const dy = (event.clientY - rect.top) / rect.height - 0.5
  card.style.transform = `perspective(900px) rotateX(${-dy * 6}deg) rotateY(${dx * 6}deg) translateY(-6px)`
}

function reset() {
  if (cardRef.value) cardRef.value.style.transform = ''''
}
</script>

<template>
  <div ref="cardRef" class="card" @mousemove="onMove" @mouseleave="reset" />
</template>', NULL, '.stage{display:flex;align-items:center;justify-content:center;height:100%;perspective:900px}
.card{width:170px;height:110px;border-radius:16px;border:1px solid rgba(255,255,255,.16);
  background:linear-gradient(160deg,rgba(194,150,58,.22),rgba(255,255,255,.05));
  transform-style:preserve-3d;animation:tilt 5s ease-in-out infinite}
.card::after{content:"";position:absolute;inset:14px;border-radius:10px;
  border:1px dashed rgba(255,255,255,.25);transform:translateZ(24px)}
@keyframes tilt{
  0%,100%{transform:rotateX(0) rotateY(0)}
  25%{transform:rotateX(6deg) rotateY(-8deg) translateY(-6px)}
  75%{transform:rotateX(-5deg) rotateY(8deg) translateY(-6px)}}', '// Three.js 版本：用一块平面承载贴图，鼠标驱动其旋转
import * as THREE from ''three''

const scene = new THREE.Scene()
const camera = new THREE.PerspectiveCamera(45, 1, 0.1, 100)
camera.position.z = 3
const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
renderer.setSize(window.innerWidth, window.innerHeight)
document.body.appendChild(renderer.domElement)

const card = new THREE.Mesh(
  new THREE.PlaneGeometry(1.7, 1.1),
  new THREE.MeshBasicMaterial({ color: 0xc2963a, transparent: true, opacity: 0.35 })
)
scene.add(card)

window.addEventListener(''mousemove'', (event) => {
  const dx = event.clientX / window.innerWidth - 0.5
  const dy = event.clientY / window.innerHeight - 0.5
  card.rotation.y = dx * 0.35
  card.rotation.x = -dy * 0.25
})

renderer.setAnimationLoop(() => renderer.render(scene, camera))'
FROM `motion_resource` r
WHERE r.`content_hash` = SHA1(CONCAT(LOWER(TRIM('卡片 3D 倾斜')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#3d')), '|', '3D'))
	AND NOT EXISTS (SELECT 1 FROM `motion_code` c WHERE c.`motion_id` = r.`id`);

-- 7. Glass / 玻璃拟态面板
INSERT INTO `motion_resource`
	(`name`, `description`, `category`, `technology`, `source_url`, `repo_url`, `preview_url`, `tags`, `license`, `code_path`, `content_hash`, `status`)
VALUES (
	'玻璃拟态面板', '半透明模糊面板，顶部有一道高光边，像玻璃的厚度。', 'Glass', 'CSS',
	'https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#glass', 'https://github.com/Dongxibie/kingdom-studio', 'inline:css', 'glass,blur,border', 'MIT（自建示例）', 'docs/extensions-samples.md',
	SHA1(CONCAT(LOWER(TRIM('玻璃拟态面板')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#glass')), '|', 'Glass')), 'READY'
)
ON DUPLICATE KEY UPDATE
	`description` = VALUES(`description`), `technology` = VALUES(`technology`),
	`tags` = VALUES(`tags`), `license` = VALUES(`license`), `status` = VALUES(`status`);

INSERT INTO `motion_code` (`motion_id`, `prompt`, `vue_code`, `react_code`, `css_code`, `three_code`)
SELECT r.`id`, '做一个玻璃拟态面板：
- 背景模糊 backdrop-filter: blur(18px)
- 1px 半透明描边 + 顶部 1px 高光模拟厚度
- 内侧一层极淡的径向高光',
	'<template>
  <div class="glass-panel">
    <slot />
  </div>
</template>

<style scoped>
.glass-panel {
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: linear-gradient(165deg, rgba(255, 255, 255, 0.16), rgba(255, 255, 255, 0.04));
  backdrop-filter: blur(18px);
  box-shadow: 0 30px 60px -30px rgba(0, 0, 0, 0.9), inset 0 1px 0 rgba(255, 255, 255, 0.45);
}
</style>', NULL, '.stage{position:relative;display:flex;align-items:center;justify-content:center;height:100%;
  background:radial-gradient(60% 60% at 30% 20%,rgba(194,150,58,.5),transparent 70%),
             radial-gradient(50% 50% at 75% 70%,rgba(125,20,24,.55),transparent 70%),#14161a}
.panel{position:relative;width:210px;height:130px;border-radius:18px;
  border:1px solid rgba(255,255,255,.2);
  background:linear-gradient(165deg,rgba(255,255,255,.16),rgba(255,255,255,.04));
  backdrop-filter:blur(18px);
  box-shadow:0 30px 60px -30px rgba(0,0,0,.9),inset 0 1px 0 rgba(255,255,255,.45);
  animation:float 6s ease-in-out infinite}
@keyframes float{0%,100%{transform:translateY(0)}50%{transform:translateY(-8px)}}', NULL
FROM `motion_resource` r
WHERE r.`content_hash` = SHA1(CONCAT(LOWER(TRIM('玻璃拟态面板')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#glass')), '|', 'Glass'))
	AND NOT EXISTS (SELECT 1 FROM `motion_code` c WHERE c.`motion_id` = r.`id`);

-- 8. Cursor / 鼠标跟随柔光
INSERT INTO `motion_resource`
	(`name`, `description`, `category`, `technology`, `source_url`, `repo_url`, `preview_url`, `tags`, `license`, `code_path`, `content_hash`, `status`)
VALUES (
	'鼠标跟随柔光', '一团柔光带阻尼地跟着鼠标走，停下即停，不硬跟。', 'Cursor', 'JavaScript',
	'https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#cursor', 'https://github.com/Dongxibie/kingdom-studio', 'inline:css', 'cursor,glow,damp', 'MIT（自建示例）', 'docs/extensions-samples.md',
	SHA1(CONCAT(LOWER(TRIM('鼠标跟随柔光')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#cursor')), '|', 'Cursor')), 'READY'
)
ON DUPLICATE KEY UPDATE
	`description` = VALUES(`description`), `technology` = VALUES(`technology`),
	`tags` = VALUES(`tags`), `license` = VALUES(`license`), `status` = VALUES(`status`);

INSERT INTO `motion_code` (`motion_id`, `prompt`, `vue_code`, `react_code`, `css_code`, `three_code`)
SELECT r.`id`, '做一个鼠标跟随柔光：
- 用 requestAnimationFrame + 阻尼（每帧插值 13%）跟随，避免硬跟的生硬感
- 光团尺寸与透明度可调，边缘完全羽化
- 鼠标离开容器时淡出',
	'<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from ''vue''

const hostRef = ref<HTMLElement | null>(null)
const glowRef = ref<HTMLElement | null>(null)
let raf = 0
let currentX = -9999
let currentY = -9999
let targetX = -9999
let targetY = -9999

function step() {
  currentX += (targetX - currentX) * 0.13
  currentY += (targetY - currentY) * 0.13
  if (glowRef.value) {
    glowRef.value.style.transform = `translate3d(${currentX}px, ${currentY}px, 0)`
  }
  raf = requestAnimationFrame(step)
}

onMounted(() => {
  const host = hostRef.value
  if (!host) return
  if (window.matchMedia(''(prefers-reduced-motion: reduce)'').matches) return
  host.addEventListener(''mousemove'', (event) => {
    const rect = host.getBoundingClientRect()
    targetX = event.clientX - rect.left
    targetY = event.clientY - rect.top
  })
  raf = requestAnimationFrame(step)
  onBeforeUnmount(() => cancelAnimationFrame(raf))
})
</script>

<template>
  <div ref="hostRef" class="host">
    <div ref="glowRef" class="glow" />
  </div>
</template>', NULL, '.stage{position:relative;display:flex;align-items:center;justify-content:center;height:100%;overflow:hidden}
.glow{width:190px;height:190px;border-radius:50%;
  background:radial-gradient(circle,rgba(138,180,255,.55),transparent 62%);
  filter:blur(6px);animation:drift 4s ease-in-out infinite}
@keyframes drift{
  0%,100%{transform:translate(-40px,-20px) scale(1)}
  33%{transform:translate(40px,10px) scale(1.08)}
  66%{transform:translate(0,30px) scale(.95)}}', NULL
FROM `motion_resource` r
WHERE r.`content_hash` = SHA1(CONCAT(LOWER(TRIM('鼠标跟随柔光')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#cursor')), '|', 'Cursor'))
	AND NOT EXISTS (SELECT 1 FROM `motion_code` c WHERE c.`motion_id` = r.`id`);

-- 9. Background / 透视网格背景
INSERT INTO `motion_resource`
	(`name`, `description`, `category`, `technology`, `source_url`, `repo_url`, `preview_url`, `tags`, `license`, `code_path`, `content_hash`, `status`)
VALUES (
	'透视网格背景', '一层向下淡出的透视网格，用来做技术感背景。', 'Background', 'CSS',
	'https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#background', 'https://github.com/Dongxibie/kingdom-studio', 'inline:css', 'background,grid,mask', 'MIT（自建示例）', 'docs/extensions-samples.md',
	SHA1(CONCAT(LOWER(TRIM('透视网格背景')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#background')), '|', 'Background')), 'READY'
)
ON DUPLICATE KEY UPDATE
	`description` = VALUES(`description`), `technology` = VALUES(`technology`),
	`tags` = VALUES(`tags`), `license` = VALUES(`license`), `status` = VALUES(`status`);

INSERT INTO `motion_code` (`motion_id`, `prompt`, `vue_code`, `react_code`, `css_code`, `three_code`)
SELECT r.`id`, '做一个透视网格背景：
- 1px 网格线，格子 64px
- 整体 rotateX(34deg) 形成透视，向下用 mask 淡出
- 线条透明度 0.14，不要太抢眼',
	'<template>
  <div class="grid-bg" />
</template>

<style scoped>
.grid-bg {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(126, 160, 200, 0.45) 1px, transparent 1px),
                    linear-gradient(90deg, rgba(126, 160, 200, 0.45) 1px, transparent 1px);
  background-size: 64px 64px;
  opacity: 0.42;
  transform: perspective(700px) rotateX(34deg);
  transform-origin: 50% 0;
  mask-image: radial-gradient(120% 80% at 50% 0%, #000 0%, transparent 74%);
}
</style>', NULL, '.stage{position:relative;height:100%;overflow:hidden;background:#101319}
.grid{position:absolute;inset:-20% -10% 0;
  background-image:linear-gradient(rgba(126,160,200,.45) 1px,transparent 1px),
                   linear-gradient(90deg,rgba(126,160,200,.45) 1px,transparent 1px);
  background-size:64px 64px;opacity:.42;
  transform:perspective(700px) rotateX(34deg);transform-origin:50% 0;
  mask-image:radial-gradient(120% 80% at 50% 0%,#000 0%,transparent 74%);
  -webkit-mask-image:radial-gradient(120% 80% at 50% 0%,#000 0%,transparent 74%);
  animation:slide 6s linear infinite}
@keyframes slide{to{background-position:0 64px,0 0}}', NULL
FROM `motion_resource` r
WHERE r.`content_hash` = SHA1(CONCAT(LOWER(TRIM('透视网格背景')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#background')), '|', 'Background'))
	AND NOT EXISTS (SELECT 1 FROM `motion_code` c WHERE c.`motion_id` = r.`id`);

-- 10. Loading / 骨架屏微光
INSERT INTO `motion_resource`
	(`name`, `description`, `category`, `technology`, `source_url`, `repo_url`, `preview_url`, `tags`, `license`, `code_path`, `content_hash`, `status`)
VALUES (
	'骨架屏微光', '占位骨架上一道微光反复扫过，比转圈更安静。', 'Loading', 'CSS',
	'https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#loading', 'https://github.com/Dongxibie/kingdom-studio', 'inline:css', 'loading,skeleton,shimmer', 'MIT（自建示例）', 'docs/extensions-samples.md',
	SHA1(CONCAT(LOWER(TRIM('骨架屏微光')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#loading')), '|', 'Loading')), 'READY'
)
ON DUPLICATE KEY UPDATE
	`description` = VALUES(`description`), `technology` = VALUES(`technology`),
	`tags` = VALUES(`tags`), `license` = VALUES(`license`), `status` = VALUES(`status`);

INSERT INTO `motion_code` (`motion_id`, `prompt`, `vue_code`, `react_code`, `css_code`, `three_code`)
SELECT r.`id`, '做一个骨架屏微光：
- 骨架底色 #1e2229，圆角 6px
- 一道 90 度微光每 1.6 秒扫过一次
- 不要用纯白，避免闪烁刺眼',
	'<template>
  <div class="skeleton">
    <div class="sk" />
    <div class="sk" />
    <div class="sk" />
  </div>
</template>

<style scoped>
.sk {
  height: 16px;
  border-radius: 6px;
  background: #1e2229;
  position: relative;
  overflow: hidden;
}
.sk::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.14), transparent);
  animation: shimmer 1.6s ease-in-out infinite;
}
@keyframes shimmer {
  from { transform: translateX(-100%); }
  to { transform: translateX(100%); }
}
</style>', NULL, '.stage{display:flex;flex-direction:column;gap:12px;justify-content:center;height:100%;padding:0 46px}
.sk{height:16px;border-radius:6px;background:#1e2229;position:relative;overflow:hidden}
.sk:nth-child(2){width:72%}
.sk:nth-child(3){width:48%}
.sk::after{content:"";position:absolute;inset:0;
  background:linear-gradient(90deg,transparent,rgba(255,255,255,.14),transparent);
  transform:translateX(-100%);animation:shimmer 1.6s ease-in-out infinite}
@keyframes shimmer{to{transform:translateX(100%)}}', NULL
FROM `motion_resource` r
WHERE r.`content_hash` = SHA1(CONCAT(LOWER(TRIM('骨架屏微光')), '|', LOWER(TRIM('https://github.com/Dongxibie/kingdom-studio/blob/master/docs/extensions-samples.md#loading')), '|', 'Loading'))
	AND NOT EXISTS (SELECT 1 FROM `motion_code` c WHERE c.`motion_id` = r.`id`);

