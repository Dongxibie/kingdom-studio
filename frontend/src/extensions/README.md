# Extensions · 扩展层约定

Kingdom Studio 的扩展模块都放在这里，与主站的四个业务模块（项目王国 / 技术图鉴 / 成长时间线 / 代码知识库）互不干扰。

## 目录约定

```
extensions/
├── _shared/              扩展层公共部分（所有扩展共用）
│   ├── components/       ExtShell 三栏外壳 / ExtEmpty 空状态 / ExtCodeBlock 代码块 / ExtStatusTag 状态标签
│   ├── styles/           ext-theme.css：深色工具风主题（作用域 .ext-shell）
│   ├── api/              fetchModuleInfo：统一的 /ping 自检
│   └── types/            ExtModuleInfo 等公共类型
├── motion-lab/           扩展一：动效基因库
│   ├── views/            页面（挂在 /extensions/motion-lab）
│   ├── components/       模块内组件（列表 / 编辑 / 预览 / 代码面板 / 采集弹窗）
│   ├── api/              只放本模块接口，前缀常量写在这里（motion.ts、crawl.ts）
│   ├── types/            与后端实体一一对应（motion.ts、crawl.ts）
│   └── utils/            纯函数（代码生成 motion-codegen、格式化 motion-format）
└── music-agent/          扩展二：音乐 Agent
    ├── views/            页面（挂在 /extensions/music-agent）
    ├── components/       上传面板 / 任务列表 / 时间线 / 虚拟键盘 / 按键序列 / 代理面板
    ├── api/              接口（music.ts）
    ├── types/            与后端 VO 一一对应（music.ts）
    └── utils/            纯函数（note-format 时间与音名、demo-player WebAudio 回放）
```

## 新增一个扩展的步骤

1. 在 `extensions/` 下建目录，照 `motion-lab` 的四个子目录（views / components / api / types，需要时加 utils）
2. `types/` 里先按后端表结构写好类型，`api/` 里定 `XXX_API_BASE` 并复用 `fetchModuleInfo`
3. 视图用 `ExtShell` 搭三栏：`#left` / 默认插槽（中栏）/ `#right`
4. 在 `router/index.ts` 追加路由（`meta.title` 会给浏览器标题用）
5. 在 `layout/BasicLayout.vue` 的 `extensionMenus` 里追加一条菜单

## 主题

- 主题变量集中在 `_shared/styles/ext-theme.css`，全部挂在 `.ext-shell` 下，不污染主站的浅色页面
- 品牌色复用主站的 `--kingdom-gold` / `--kingdom-red`：金 = 强调，暗红 = 状态/问题
- 需要新颜色时加变量，不要在组件里写死十六进制值

## 阶段状态

| 阶段 | 内容 | 状态 |
| --- | --- | --- |
| Phase 1 | 扩展架构（目录、主题、三栏外壳、路由菜单、模块自检接口） | 已完成 |
| Phase 2 | Motion Lab MVP（建表、CRUD、三栏 UI、四种代码生成、沙箱预览） | 已完成 |
| Phase 3 | Motion Crawler（GitHub 采集、三级去重、关键词分类、采集弹窗） | 已完成 |
| Phase 4 | Music Agent（MIDI / 简谱解析、Instrument Profile 映射、按键序列导出） | 已完成 |
| Phase 5 | 演奏时间线（钢琴卷帘、按键轨、虚拟键盘、Demo 回放与按键动画） | 已完成 |
| Phase 6 | Desktop Agent（WebSocket 协议 + 派发计划模拟，不做真实系统输入） | 已完成 |

## Motion Lab 升级（动效工作台）

v1.1 起，动效基因库从「资源管理工具」升级为「动效开发工作台」：模板成为主要内容，
资源退到「我的资源」子页面。数据层新增三张表：

| 表 | 用途 |
| --- | --- |
| `motion_template` | 官方模板：预览结构 + CSS/Vue/React/Three.js + Prompt + 可调参数 + 推荐指数四项子分 |
| `motion_recipe` | 组合方案：把若干模板按顺序叠成场景级方案（推荐指数为成员的加权平均） |
| `motion_rating` | 人工评分与理由（与算法的推荐指数分开存，一个可复现、一个是判断依据） |

界面结构：顶部搜索栏（可解释检索：识别场景 / 风格 / 技术 / 动效关键词并列出命中理由）→
左栏「发现方式」（场景 / 风格 / 技术 / 分组 / 难度 + 组合方案）→ 中栏实时预览（播放 / 暂停 / 重播 / 变速 / 缩放 + 参数）
→ 右栏「推荐指数 / 代码 / Prompt / 参数」。首次进入先问「你想制作什么」，选过之后不再打断。

完整的交付说明、测试结果与运行方式见 `docs/EXTENSIONS-COMPLETION-REPORT.md`。
