# Motion Workbench 2.0 · Phase 5 变更报告（Motion Code Generator）

> 目标：让工作台从「展示工具」变成「开发工具」——
> 选好一套方案，点一下就能拿到**能拷进项目就跑**的工程文件，而不是一段需要自己拼的片段。
> 本阶段新增代码导出：Vue 3 / React / HTML+CSS 三种格式，多文件产物、复制、打包下载、在线预览。

---

## 一、这一阶段交付了什么

| # | 交付物 | 说明 |
| --- | --- | --- |
| 1 | **导出服务 `MotionCodeExportService`** | 一套方案 → 主组件 + 每步一个文件 + 配置 + 样式；三种格式各一套产物 |
| 2 | **CSS 作用域化 `CssScoper`** | 选择器加包装前缀、`@keyframes` 按步重命名、`:root` 收到包装元素、`@media` 递归 |
| 3 | **导出接口** | `POST /api/motion/templates/export`（`recipeKey` 或 `templateKeys` + `format` + `params`） |
| 4 | **组合预览** | 与导出的 HTML 同源的组合页面，前端塞进沙箱 iframe 就能看到导出结果的样子 |
| 5 | **导出面板** | 格式切换、文件清单、逐个文件代码与复制、单文件下载、打包下载 ZIP、预览与重播 |
| 6 | **零依赖 ZIP 打包器** | 只用 store 模式，自己写 CRC32 与 ZIP 结构；产物字节可复现 |

---

## 二、三种格式各自给什么

| 格式 | 文件 | 隔离方式 |
| --- | --- | --- |
| **Vue 3** | `MotionPlan.vue` + `steps/Step1…N.vue` + `motion.config.ts` | 每步一个 SFC，`<style scoped>` 天然隔离；`:root` 变量额外收到包装元素上 |
| **React** | `MotionPlan.tsx` + `steps/Step1…N.tsx` + 每个步骤一份 `.css` + `motion.config.ts` + `motion.css` | 服务端把每步 CSS 作用域化后再落盘 |
| **HTML + CSS** | `index.html` + `motion.css` | 同上，双击即可打开 |

导出的每一步都长这样（示意）：

```vue
<!-- steps/Step1GalaxyBackground.vue -->
<script setup lang="ts">
/** 星系背景 —— 作用：星系渐变打底，给整页定下冷色调 */
const styleVars = {
  '--m-duration': '18s',
  '--m-distance': '14%',
} as Record<string, string>
</script>

<template>
  <div class="motion-step" :style="styleVars">
    <div class="motion-root motion-root--bleed"><div class="m-galaxy"></div></div>
  </div>
</template>

<style scoped>
.motion-step { position: relative; min-height: 320px; border-radius: 16px; overflow: hidden; }
/* 模板自己的样式，已作用域化 */
</style>
```

**参数不写死在样式里，而是作为 CSS 变量挂在每一步的包装元素上**：
调参只改 `motion.config.ts` 或那一行的 style 绑定，不用动样式表。

---

## 三、两件必须做对的事

**一、样式必须隔离，否则拼起来就串。**
三十多个模板都有 `.motion-root`，参数变量也都叫 `--m-duration`，`@keyframes` 名字也有重名。
所以导出前统一作用域化：

| 情况 | 处理 |
| --- | --- |
| 普通规则 `.m-wipe` | `.mlab-step-2 .m-wipe` |
| `:root` / `html` / `body` | 换成包装元素本身（否则第二步的变量会盖掉第一步） |
| `@keyframes m-drift` | 重命名为 `mlab-step-1-m-drift`，同一份 CSS 里的 `animation:` 引用一起改 |
| `@media` / `@supports` | 递归处理内部规则 |
| Vue 的 `<style scoped>` | 额外把 `:root` 收进包装元素：scoped 样式里的 `:root` 会变成 `:root[data-v-x]`，匹配不到任何元素，变量会**静默失效** |

**二、产物要真的能跑，所以不能自己造轮子拼字符串。**
代码里的结构、样式、参数全部来自模板库那一份已经验证过的实现（就是沙箱预览用的同一份数据），
生成器只做「包装 + 隔离 + 落参数」，所以预览里看到的效果与导出结果一致 ——
这不是靠对图验收，而是靠同一份来源。

---

## 四、验收

| 项 | 结果 |
| --- | --- |
| 后端测试 | **238 个用例全绿**（0 失败 / 0 错误），本阶段新增 17 个（`CssScoperTest` 8 + `MotionCodeExportServiceTest` 9） |
| 前端类型检查 | `vue-tsc --noEmit` 通过 |
| 前端构建 | `npm run build` 通过 |
| 接口实测 | Vue 6 个文件 / React 11 个文件 / HTML 2 个文件；参数覆盖生效（`--m-duration: 6s` 写进了预览与产物） |
| 作用域实测 | HTML 产物里 `.mlab-step-1 .m-galaxy` 与 `.mlab-step-2 .motion-root` 各归各步；`@keyframes mlab-step-1-m-warp` 重命名到位 |
| 浏览器实测 | 设计页点「生成代码」出 6 个文件、预览 iframe 挂载（srcdoc 5.5 kB）、说明逐条正确；文件可切换、可复制 |
| **ZIP 字节级校验** | 打包器产物的 `local file header` 签名 `04034b50`、EOCD 签名 `06054b50`、压缩方式 0（store）、条目数 2 全部正确；**CRC32 与 Node 的 `zlib.crc32` 完全一致**（`hello` → 907060870，HTML → 187268658） |

---

## 五、边界

- **导出的是「已入库模板的组装」，不是模型生成代码**：模板的 CSS/结构本身就是成品，
  生成器不会写出没被验证过的样式片段。所以「导出的代码跑不起来」这类问题在结构上被排除。
- **每步一个文件是刻意的**：一个动效出问题时能单独改、单独删，而不是在一份巨型组件里翻。
- **ZIP 用 store 不压缩**：产物是纯文本、体积本来就小（4 步方案约 8 KB），
  为此引一个压缩库不值得；换来的是零依赖与一眼看得懂的实现，字节还可复现（打包时间固定）。
- **预览是沙箱 iframe**（`sandbox="allow-scripts"`，不给 `allow-same-origin`），
  与工作台里所有预览走同一套纪律。

---

## 六、代码位置

- 后端：`modules/motion/template/export/MotionCodeExportService.java`、`export/CssScoper.java`、
  `vo/MotionExportVO.java`、`dto/MotionExportDTO.java`；
  接口在 `template/controller/MotionTemplateController.java`
- 前端：`extensions/motion-lab/components/MotionExportPanel.vue`、`utils/zip.ts`、`types/export.ts`、
  `api/template.ts`（`exportMotions`）；挂在 AI 设计页右栏
- 测试：`backend/src/test/java/com/kingdomstudio/modules/motion/template/export/`
