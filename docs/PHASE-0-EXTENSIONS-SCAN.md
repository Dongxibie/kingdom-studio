# Kingdom Studio Extensions · Phase 0 扫描报告

> 执行时间：2026-09-24 ｜ 本阶段**未修改任何代码**（只读扫描 + 一次基线构建/测试）
> 扫描范围：`kingdom-studio/`（frontend、backend、db、docs）

---

## 1. 当前项目结构（实测）

```
kingdom-studio/
├── frontend/                Vue 3.5 + TypeScript 5.6 + Vite 6 + Element Plus 2.9
│   ├── package.json         scripts: dev / build / preview / type-check（没有 lint）
│   ├── vite.config.ts       @ → ./src；/api 代理到 http://localhost:8080
│   ├── tsconfig.json        strict + noUnusedLocals/Parameters（严格）
│   └── src/
│       ├── api/             request.ts（Axios：剥 Result 外壳、token 注入、中文错误）、health.ts
│       ├── composables/     useNarrowScreen.ts
│       ├── layout/          BasicLayout.vue（第 9–15 行是硬编码的 menus 数组）
│       ├── router/          index.ts（5 条路由 + 404 兜底，4 条指向同一个占位组件）
│       ├── styles/          main.css（--kingdom-red / gold / gold-light / bg / text）
│       └── views/           DashboardView、ModulePlaceholder、NotFoundView
├── backend/                 Spring Boot 3.3.5 + Java 21 + MyBatis-Plus 3.5.9
│   ├── pom.xml              web / validation / security / jjwt / mybatis-plus(+jsqlparser)
│   │                        mysql / redis / cache / actuator / springdoc / lombok
│   └── src/main/java/com/kingdomstudio/
│       ├── KingdomStudioApplication.java
│       ├── common/          Result、ResultCode、BusinessException、GlobalExceptionHandler
│       ├── config/          Security、Cors、MybatisPlus、Redis、OpenApi
│       ├── controller/      HealthController（唯一）
│       ├── service/         HealthService
│       ├── handler/         MyMetaObjectHandler（公共字段自动填充）
│       └── vo/              HealthVO
│   └── src/main/resources/  application.yml（context-path=/api，mapper-locations=/mapper/**/*.xml）
├── db/kingdom_studio.sql    user / project / technology / timeline / code_snippet（5 表，脚本为 DROP 重建式）
├── docs/PHASE-1.md
├── Kingdom Studio V1 测试报告.md
├── README.md ／ .gitignore
└── personal-ops-dashboard.html（单文件运维台看板，与本任务无关）
```

**技术栈确认（与 Prompt 描述一致）**

- 前端：Vue 3 + TypeScript + Vite + Element Plus ✓（Element Plus 为全量引入）
- 后端：Java 21 + Spring Boot 3 + MyBatis-Plus + MySQL 8 + Redis ✓

**基线实测（今天跑的）**

| 检查 | 结果 |
| --- | --- |
| `npm run build` | 通过，7.97s；警告：index chunk 1.06 MB（Element Plus 全量引入） |
| `mvn -o -q test` | 通过，但**实际跑了 0 个测试**（`backend/src/test` 目录不存在） |
| `npm run lint` | **不存在**（package.json 里没有这个脚本，也没装 eslint/prettier） |
| `git status` | **不是 git 仓库**（有 .gitignore，但没 init） |

**对扩展最关键的现状**：后端业务层基本是空的——只有一个 HealthController，**没有 entity / mapper，也没有那 4 个业务模块的接口**；前端 4 条业务路由全部指向同一个占位组件。也就是说"不破坏已有业务"的实际压力很小，**现在是最干净的扩展时机**。

---

## 2. 可以扩展的位置（挂载点）

| 位置 | 说明 | 是否需要改动既有文件 |
| --- | --- | --- |
| 前端路由 `router/index.ts` | 往 `routes` 数组**追加**记录即可 | 追加（不修改已有项） |
| 前端菜单 `BasicLayout.vue:9-15` | 硬编码的 `menus` 数组 | **是（唯一必须触碰的既有文件，追加 2 行）** |
| 路径别名 | `@` → `./src` 已配好，`@/extensions/...` 直接可用 | 否 |
| API 封装 | `api/request.ts` 导出的 `http.get/post/put/delete` 直接复用 | 否 |
| 后端组件扫描 | 启动类在 `com.kingdomstudio` 下，新模块放 `com.kingdomstudio.modules.*` 会被自动扫描 | 否 |
| MyBatis-Plus | `mapper-locations: classpath*:/mapper/**/*.xml` 已就绪 → 新 XML 放 `resources/mapper/motion/` 即可 | 否 |
| Mapper 注册 | **当前没有 @MapperScan** → 新 Mapper 接口逐个加 `@Mapper` 即可（推荐），或新增一个 @MapperScan 配置类 | 否（推荐加注解） |
| 安全放行 | 现在 `anyRequest().permitAll()`，新接口立刻可用；**但 Phase 2 接上 JWT 那天**，新扩展路径要么加进 `PUBLIC_ENDPOINTS`，要么走鉴权 | 届时需要一次改动 |
| 数据库 | 新表**另写脚本**（如 `db/extensions_motion.sql`）；现有脚本是 DROP 重建式，误执行会清空数据 | 否（不要动现有 5 表） |
| 缓存 | `RedisConfig` 已有 CacheManager，扩展模块可直接 `@Cacheable` | 否 |

---

## 3. 需要新增的目录

**前端 `frontend/src/extensions/`**

```
extensions/
├── _shared/          扩展公共层（三栏外壳、代码块、空状态、标签组件）—— 建议加，避免两个扩展各写一套
├── motion-lab/       views / components / api / types / utils        → 路由 /extensions/motion-lab
└── music-agent/      views / components / api / types / utils        → 路由 /extensions/music-agent
```

**后端 `backend/src/main/java/com/kingdomstudio/modules/`**

```
modules/
├── motion/    controller / service / mapper / entity / dto / vo      （采集、去重、分类、代码生成）
└── music/     controller / service / mapper / entity / dto / vo      （任务、音符、映射引擎）
    └── agent/ 只放 Desktop Agent 的接口契约（WebSocket 消息 DTO + 协议说明），不实现系统输入
resources/mapper/motion/*.xml ／ resources/mapper/music/*.xml
```

**数据库新增（不动现有 5 表）**

- `motion_resource`：Prompt 给的字段 + 建议补 `repo_url`、`stars`、`license`、`code_path`、`content_hash`（去重用）、`status`、`updated_time`
- `motion_code`：`motion_id` + `vue_code` / `react_code` / `css_code` / `three_code` / `prompt`
- `music_task`：Prompt 给的字段 + `progress`、`error_msg`、`updated_time`
- `music_note`：`task_id` + `timestamp` / `note` / `keyboard_key` + `duration` / `velocity`
- `instrument_profile`：**Prompt 说"乐器映射不要写死"** → 需要一张表存 Profile（乐器名、音域、按键布局、映射规则 JSON、是否启用）

---

## 4. 可能存在的风险

1. **git 缺失**：Prompt 要求每阶段提交，但当前不是仓库 → 必须先 `git init`。
2. **视觉风格冲突（最需要你定的一条）**：现有工作台是「深红 + 金 + 皇冠」的王国配色（`--kingdom-red/gold`），而新 Prompt 要求"黑白灰、少量强调色、类 Linear / Vercel / Raycast，不要花哨渐变"。扩展页照 Prompt 做，会和主站视觉分裂。
3. **`npm run lint` 不存在**：验收要求里写了要跑 lint，但工程没有 lint 配置，也没装 eslint/prettier。
4. **Python 服务是新增运行时**：Music Agent 的 librosa / music21 需要 Python（本机有 Python，之前也用它做过本地语音转写）。但那是**第二个运行时**（Java + Python 并存），带来端口/进程/依赖管理成本，与"不引入无必要大型框架"的精神有张力。
5. **GitHub 采集的限额与合规**：搜索 API 未认证 10 次/分钟、认证后 30 次/分钟；保存仓库代码片段与预览图涉及开源许可，**必须存 license 并在展示时标注来源**。另外本机访问 github.com 需要走代理/DoH（已知环境坑）。
6. **iframe 预览的安全**：预览第三方动效必须 `sandbox="allow-scripts"` 且**不加 `allow-same-origin`**；抓来的 JS 绝不在前端 eval。
7. **Element Plus 全量引入**：主 bundle 已经 1.06 MB，扩展页再堆组件会更臃肿；扩展页建议按需引入或直接用原生 CSS（也更贴合 Linear 风）。
8. **"视觉相似"去重**：像素级感知哈希需要图像处理，纯 Java 比较吃力。建议第一版只做 名称 / URL / 代码 hash / README 文本相似，视觉相似放后面。
9. **Desktop Agent 底线**：Prompt 明确"只设计接口、不要直接实现系统输入"。只写 WebSocket 协议 + DTO + mock 端点，不做任何真实的键盘/系统输入调用。

---

## 5. 推荐实施方案

| 阶段 | 内容 | 验收 |
| --- | --- | --- |
| **Phase 0**（本次） | 扫描 + 报告，不改代码 | 本文件 |
| **Phase 1** | 扩展架构落地：`extensions/_shared` + 两个扩展的空壳（路由 + 菜单 + 三栏骨架 + 空状态）；后端 `modules/{motion,music}` 包 + ping 端点；前端 api 接上 | `npm run build` 通过、`mvn test` 通过、两个新页面能打开 |
| **Phase 2** | Motion Lab MVP：建 `motion_resource` / `motion_code`、CRUD 接口、三栏 UI、Prompt/Code 两个 Tab（Vue/React/CSS/Three 四种模板）、"手动新增一条动效"闭环 | 不依赖任何外部采集即可离线跑通 |
| **Phase 3** | Motion Crawler：GitHub 搜索采集 + 去重（名称/URL/hash/文本）+ 分类（先规则+关键词表，避免一上来依赖大模型 API） | 采集到足量数据并完成去重分类 |
| **Phase 4** | Music Agent：**先纯 Java 做 MIDI + 简谱文本**（`javax.sound.midi`），`instrument_profile` 映射引擎 + 时间线 UI + Demo 模式（网页模拟）；MP3 与 Python 服务放最后 | 上传 MIDI → 时间线 → 按键序列导出 |
| **Phase 5** | Desktop Agent：只出协议文档 + Java 侧接口 + mock 回显 | 不实现系统输入 |

**每一步只新增文件**；唯一需要改的既有文件是 `BasicLayout.vue` 的 menus 数组（追加 2 行）和 `router/index.ts`（追加路由记录）。

---

## 需要你拍板的 4 件事

1. **git**：要不要我 `git init` 并补 .gitignore（Prompt 要求每阶段提交）
2. **lint**：加一套 eslint + prettier（新增文件，不动业务代码），还是把验收口径改成 `npm run build` + `vue-tsc --noEmit`
3. **视觉**：扩展区走中性色（A，与主站并存）／沿用金红（B）／做成独立的"工具风"主题（C）
4. **Music Agent 第一版范围**：只做 MIDI + 简谱（纯 Java，成功率最高，推荐）／直接上 MP3（需要引入 Python 服务）
