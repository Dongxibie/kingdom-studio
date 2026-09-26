# 动效示例源码 / extensions-samples

> 这份文档是「动效基因库」里十条内置示例的源码页。
> 种子数据（`db/extensions_motion_seed.sql`）里每条资源的「来源」都指到本文的对应小节，
> 所以点开来源就能看到这个动效的实际代码，而不是一个失效链接。
>
> 十条示例由本项目自行编写（许可：MIT），覆盖动效基因库的十个分类，每个分类一条。
> 每条都带五样产物：说明用的 Prompt、Vue 3 组件、CSS、以及（部分分类才有的）React 与 Three.js 实现。
> 库里存的是完整五份，本文为了让页面读起来清楚，**只展开 CSS**——它也是实时预览里真正生效的那部分；
> 其余产物在界面上选中资源后即可查看与编辑。

## 目录

| 分类 | 技术栈 | 一句话说明 |
| --- | --- | --- |
| [入场 Entrance](#entrance) | CSS | 一组玻璃卡片依次上浮出现，间隔 70ms，带轻微模糊收尾。 |
| [悬停 Hover](#hover) | CSS | 鼠标悬停时一道斜向光线扫过卡片表面，同时轻微上浮。 |
| [滚动 Scroll](#scroll) | IntersectionObserver | 列表进入视口后依次点亮，滚动离开不影响已点亮状态。 |
| [文字 Text](#text) | JavaScript | 一段文字逐字写出，末尾光标闪烁。 |
| [粒子 Particle](#particle) | Canvas | 节点缓慢漂移，距离足够近时自动连线，鼠标附近会点亮。 |
| [三维 3D](#3d) | CSS 3D | 鼠标在卡片上移动时整块轻微倾斜，带透视和抬升。 |
| [玻璃 Glass](#glass) | CSS | 半透明模糊面板，顶部有一道高光边，像玻璃的厚度。 |
| [光标 Cursor](#cursor) | JavaScript | 一团柔光带阻尼地跟着鼠标走，停下即停，不硬跟。 |
| [背景 Background](#background) | CSS | 一层向下淡出的透视网格，用来做技术感背景。 |
| [加载 Loading](#loading) | CSS | 占位骨架上一道微光反复扫过，比转圈更安静。 |

---

<a id="entrance"></a>

## 入场 · 玻璃卡片错位入场

| | |
| --- | --- |
| 分类 | `Entrance` |
| 技术栈 | CSS |
| 标签 | entrance · stagger · glass |
| 说明 | 一组玻璃卡片依次上浮出现，间隔 70ms，带轻微模糊收尾。 |

**Prompt**

```text
做一个卡片错位入场动效：
- 卡片依次从下方 22px 浮上来，间隔 70ms
- 起始带 3px 模糊，结束时清晰
- 缓动 cubic-bezier(.16,.84,.24,1)，时长 700ms
```

**CSS**

```css
.stage{display:flex;gap:10px;justify-content:center;align-items:center;height:100%}
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
  to{opacity:1;transform:none;filter:none}}
```

> 这条在库里还带 React 实现，选中资源后在右侧「AI 产出」里切换页签即可查看。

---

<a id="hover"></a>

## 悬停 · 卡片光线扫过

| | |
| --- | --- |
| 分类 | `Hover` |
| 技术栈 | CSS |
| 标签 | hover · sweep · premium |
| 说明 | 鼠标悬停时一道斜向光线扫过卡片表面，同时轻微上浮。 |

**Prompt**

```text
做一个高级感的卡片悬停动效：
- 悬浮时上浮 6px
- 一道 105 度的光线从左到右扫过，时长 900ms
- 缓动 cubic-bezier(.2,.7,.2,1)
```

**CSS**

```css
.stage{display:flex;justify-content:center;align-items:center;height:100%}
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
  55%,100%{transform:translateX(130%)}}
```

> 这条在库里还带 React 实现，选中资源后在右侧「AI 产出」里切换页签即可查看。

---

<a id="scroll"></a>

## 滚动 · 滚动逐级点亮

| | |
| --- | --- |
| 分类 | `Scroll` |
| 技术栈 | IntersectionObserver |
| 标签 | scroll · reveal · list |
| 说明 | 列表进入视口后依次点亮，滚动离开不影响已点亮状态。 |

**Prompt**

```text
做一个滚动逐级点亮的效果：
- 元素进入视口时从透明到显现，位移 24px
- 用 IntersectionObserver，不要监听 scroll 事件（性能）
- 已点亮的元素不要因为再次滚动而闪烁
```

**CSS**

```css
.stage{display:flex;flex-direction:column;gap:10px;justify-content:center;height:100%;padding:0 40px}
.item{height:34px;border-radius:9px;display:flex;align-items:center;padding:0 14px;
  font:12px/1 ui-monospace,monospace;color:#cfd6df;
  border:1px solid rgba(255,255,255,.12);background:rgba(255,255,255,.05);
  opacity:0;transform:translateY(24px);
  animation:light .8s cubic-bezier(.16,.84,.24,1) both}
.item:nth-child(2){animation-delay:.12s}
.item:nth-child(3){animation-delay:.24s}
.item:nth-child(4){animation-delay:.36s}
@keyframes light{to{opacity:1;transform:none;border-color:rgba(194,150,58,.7)}}
```

---

<a id="text"></a>

## 文字 · 打字机标题

| | |
| --- | --- |
| 分类 | `Text` |
| 技术栈 | JavaScript |
| 标签 | text · typing · cursor |
| 说明 | 一段文字逐字写出，末尾光标闪烁。 |

**Prompt**

```text
做一个打字机文字动效：
- 每帧推进若干字符，整段在 1.2 秒左右写完
- 末尾竖线光标闪烁，频率 500ms
```

**CSS**

```css
.stage{display:flex;align-items:center;justify-content:center;height:100%}
.type{font:15px/1.8 ui-monospace,monospace;color:#e8eaed;white-space:nowrap;
  overflow:hidden;border-right:2px solid #c2963a;
  width:0;animation:type 1.6s steps(18,end) forwards,caret .5s step-end infinite alternate}
@keyframes type{to{width:18ch}}
@keyframes caret{50%{border-color:transparent}}
```

---

<a id="particle"></a>

## 粒子 · 粒子星野与近邻连线

| | |
| --- | --- |
| 分类 | `Particle` |
| 技术栈 | Canvas |
| 标签 | particle · links · starfield |
| 说明 | 节点缓慢漂移，距离足够近时自动连线，鼠标附近会点亮。 |

**Prompt**

```text
做一个粒子星野动效（Canvas）：
- 数量可控，位置与速度随机，边界回绕
- 两点距离小于阈值时连线，透明度按距离衰减
- 鼠标附近 160px 内的节点变亮并放大
- 支持 prefers-reduced-motion：开启时只画一帧静态图
```

**CSS**

```css
.stage{position:relative;height:100%;overflow:hidden;
  background:radial-gradient(120% 100% at 50% 0%,#1a2130,#0e131c 70%)}
.star{position:absolute;width:3px;height:3px;border-radius:50%;background:#cfe0ff;
  box-shadow:0 0 8px 1px rgba(160,190,255,.7);animation:twinkle 3s ease-in-out infinite}
.star:nth-child(1){left:18%;top:30%}
.star:nth-child(2){left:42%;top:58%;animation-delay:.7s}
.star:nth-child(3){left:68%;top:34%;animation-delay:1.3s}
.star:nth-child(4){left:78%;top:68%;animation-delay:2s}
.star:nth-child(5){left:30%;top:76%;animation-delay:1s}
@keyframes twinkle{0%,100%{opacity:.35;transform:scale(.8)}50%{opacity:1;transform:scale(1.3)}}
```

---

<a id="3d"></a>

## 三维 · 卡片 3D 倾斜

| | |
| --- | --- |
| 分类 | `3D` |
| 技术栈 | CSS 3D |
| 标签 | 3d · tilt · perspective |
| 说明 | 鼠标在卡片上移动时整块轻微倾斜，带透视和抬升。 |

**Prompt**

```text
做一个卡片 3D 倾斜动效：
- 鼠标位置决定 rotateX/rotateY，幅度不超过 6 度
- perspective 900px，悬浮时抬升 6px
- 离开时归位，过渡 0.18s ease-out
```

**CSS**

```css
.stage{display:flex;align-items:center;justify-content:center;height:100%;perspective:900px}
.card{width:170px;height:110px;border-radius:16px;border:1px solid rgba(255,255,255,.16);
  background:linear-gradient(160deg,rgba(194,150,58,.22),rgba(255,255,255,.05));
  transform-style:preserve-3d;animation:tilt 5s ease-in-out infinite}
.card::after{content:"";position:absolute;inset:14px;border-radius:10px;
  border:1px dashed rgba(255,255,255,.25);transform:translateZ(24px)}
@keyframes tilt{
  0%,100%{transform:rotateX(0) rotateY(0)}
  25%{transform:rotateX(6deg) rotateY(-8deg) translateY(-6px)}
  75%{transform:rotateX(-5deg) rotateY(8deg) translateY(-6px)}}
```

> 这条在库里还带 Three.js 实现，选中资源后在右侧「AI 产出」里切换页签即可查看。

---

<a id="glass"></a>

## 玻璃 · 玻璃拟态面板

| | |
| --- | --- |
| 分类 | `Glass` |
| 技术栈 | CSS |
| 标签 | glass · blur · border |
| 说明 | 半透明模糊面板，顶部有一道高光边，像玻璃的厚度。 |

**Prompt**

```text
做一个玻璃拟态面板：
- 背景模糊 backdrop-filter: blur(18px)
- 1px 半透明描边 + 顶部 1px 高光模拟厚度
- 内侧一层极淡的径向高光
```

**CSS**

```css
.stage{position:relative;display:flex;align-items:center;justify-content:center;height:100%;
  background:radial-gradient(60% 60% at 30% 20%,rgba(194,150,58,.5),transparent 70%),
             radial-gradient(50% 50% at 75% 70%,rgba(125,20,24,.55),transparent 70%),#14161a}
.panel{position:relative;width:210px;height:130px;border-radius:18px;
  border:1px solid rgba(255,255,255,.2);
  background:linear-gradient(165deg,rgba(255,255,255,.16),rgba(255,255,255,.04));
  backdrop-filter:blur(18px);
  box-shadow:0 30px 60px -30px rgba(0,0,0,.9),inset 0 1px 0 rgba(255,255,255,.45);
  animation:float 6s ease-in-out infinite}
@keyframes float{0%,100%{transform:translateY(0)}50%{transform:translateY(-8px)}}
```

---

<a id="cursor"></a>

## 光标 · 鼠标跟随柔光

| | |
| --- | --- |
| 分类 | `Cursor` |
| 技术栈 | JavaScript |
| 标签 | cursor · glow · damp |
| 说明 | 一团柔光带阻尼地跟着鼠标走，停下即停，不硬跟。 |

**Prompt**

```text
做一个鼠标跟随柔光：
- 用 requestAnimationFrame + 阻尼（每帧插值 13%）跟随，避免硬跟的生硬感
- 光团尺寸与透明度可调，边缘完全羽化
- 鼠标离开容器时淡出
```

**CSS**

```css
.stage{position:relative;display:flex;align-items:center;justify-content:center;height:100%;overflow:hidden}
.glow{width:190px;height:190px;border-radius:50%;
  background:radial-gradient(circle,rgba(138,180,255,.55),transparent 62%);
  filter:blur(6px);animation:drift 4s ease-in-out infinite}
@keyframes drift{
  0%,100%{transform:translate(-40px,-20px) scale(1)}
  33%{transform:translate(40px,10px) scale(1.08)}
  66%{transform:translate(0,30px) scale(.95)}}
```

---

<a id="background"></a>

## 背景 · 透视网格背景

| | |
| --- | --- |
| 分类 | `Background` |
| 技术栈 | CSS |
| 标签 | background · grid · mask |
| 说明 | 一层向下淡出的透视网格，用来做技术感背景。 |

**Prompt**

```text
做一个透视网格背景：
- 1px 网格线，格子 64px
- 整体 rotateX(34deg) 形成透视，向下用 mask 淡出
- 线条透明度 0.14，不要太抢眼
```

**CSS**

```css
.stage{position:relative;height:100%;overflow:hidden;background:#101319}
.grid{position:absolute;inset:-20% -10% 0;
  background-image:linear-gradient(rgba(126,160,200,.45) 1px,transparent 1px),
                   linear-gradient(90deg,rgba(126,160,200,.45) 1px,transparent 1px);
  background-size:64px 64px;opacity:.42;
  transform:perspective(700px) rotateX(34deg);transform-origin:50% 0;
  mask-image:radial-gradient(120% 80% at 50% 0%,#000 0%,transparent 74%);
  -webkit-mask-image:radial-gradient(120% 80% at 50% 0%,#000 0%,transparent 74%);
  animation:slide 6s linear infinite}
@keyframes slide{to{background-position:0 64px,0 0}}
```

---

<a id="loading"></a>

## 加载 · 骨架屏微光

| | |
| --- | --- |
| 分类 | `Loading` |
| 技术栈 | CSS |
| 标签 | loading · skeleton · shimmer |
| 说明 | 占位骨架上一道微光反复扫过，比转圈更安静。 |

**Prompt**

```text
做一个骨架屏微光：
- 骨架底色 #1e2229，圆角 6px
- 一道 90 度微光每 1.6 秒扫过一次
- 不要用纯白，避免闪烁刺眼
```

**CSS**

```css
.stage{display:flex;flex-direction:column;gap:12px;justify-content:center;height:100%;padding:0 46px}
.sk{height:16px;border-radius:6px;background:#1e2229;position:relative;overflow:hidden}
.sk:nth-child(2){width:72%}
.sk:nth-child(3){width:48%}
.sk::after{content:"";position:absolute;inset:0;
  background:linear-gradient(90deg,transparent,rgba(255,255,255,.14),transparent);
  transform:translateX(-100%);animation:shimmer 1.6s ease-in-out infinite}
@keyframes shimmer{to{transform:translateX(100%)}}
```

---

## 关于许可与来源

- 本文所有示例代码由本项目自行编写，以 MIT 许可发布，可自由使用与修改。
- 资源表里的 `license` 字段对这批数据标为「MIT（自建示例）」，与本文一致；采集来的第三方资源会记录它自己的许可（如 MIT / Apache-2.0），展示与复用时必须标注来源。
