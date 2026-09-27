# 主站四个业务模块 · 产品化报告（V1.2）

> 目标：把 Kingdom Studio 从「两个成熟工具 + 一个空壳主页」补成一个完整的工作台。
> 四个占位模块（项目王国 / 技术图鉴 / 成长时间线 / 代码知识库）做成真实可用的页面，
> 再加工程化配套（CI、前端测试、版本统一）。
>
> 总原则：**不重写扩展、不动已有的表和接口、新增优先**。
> 动效基因库与音乐 Agent 这一轮只动了一处（工作室的 Element 主色按容器改回紫色），业务代码零改动。

---

## 一、Phase 1 项目王国

**定位**：个人作品展示与管理中心（个人 GitHub Portfolio + 项目管理台）。

### 列表页

每张卡片展示：项目名、状态（规划中 / 持续开发 / 已完成）、完成度进度条、技术栈标签、
代码量 / 测试数 / 提交数（有才显示）、仓库与演示入口、更新时间，以及 详情 / 编辑 / 删除 三个动作。
顶部有关键词搜索（匹配项目名、简介、技术栈）与状态筛选。

### 详情页

四块内容对应需求里的四个小节：

| 区块 | 内容 |
| --- | --- |
| 基本信息 | 当前状态、完成度、开发时间（创建 → 更新）、技术栈数量、排序值 |
| 技术架构 | 技术栈按 前端 / 后端 / 数据库 / AI / 部署与工具 分组展示，未识别的落到「其他」 |
| 项目亮点 | Markdown 渲染（标题、列表、加粗、行内代码），先转义再拼标签 |
| 项目成果 | 代码量、测试数量、Git 提交、Demo 链接、完成度 —— 只显示真实统计到的项 |

### 后端

`entity / mapper / dto / vo / service / controller` 六个包，`ProjectService` 负责分页、详情、增删改与名称唯一校验，
`ProjectController` 暴露 5 个接口（列表 / 详情 / 新增 / 修改 / 删除，删除为逻辑删除）。

### 数据库

`project` 表在原字段基础上新增 `demo_url`、`highlights`（Markdown）、`code_lines`、`test_count`、`commit_count`，
原有的 `cover_image`、`technology_stack`、`github_url`、`status`、`progress`、`sort_order` 全部复用。

---

## 二、Phase 2 技术图鉴

**定位**：技术知识资产库（不是博客，是一本自己的技术百科）。

- 左侧分类导航：全部 / Java / Spring / 数据库 / AI / 前端 / 工程化，各带数量；分类顺序固定为「从后端走到工程化」。
- 右侧技术卡片：名称、掌握程度（★ 星级，1–5）、分类、说明、项目应用标签、开始学习日期。
- 同分类内按掌握程度从高到低排，一眼看出哪几项最熟。
- 支持新增 / 编辑 / 删除，等级用星控件选，项目应用支持逗号分隔多项。

`technology` 表新增 `used_projects`；分类约束放开到包含 `DevOps`（对应界面上的「工程化」）。

---

## 三、Phase 3 成长时间线

- 按年份分组的纵向时间轴：年份是台阶，节点卡片里有时间文字（有具体日期显示到日，否则显示年份）、标题、描述、关联项目与阶段等级（●●●○○）。
- 排序规则：年份倒序；同一年里**有具体日期的节点排在年度节点之前**，再按排序值与 id 兜底，保证顺序稳定。
- 支持新增 / 编辑 / 删除，可只填年份（年度节点）或补上具体日期（关键节点）。

`timeline` 表新增 `event_date`、`related_project` 与 `idx_timeline_event_date` 索引。

---

## 四、Phase 4 代码知识库

**定位**：个人代码资产 —— 存的是「解决方案」而不是代码仓库。

- 列表：标题、语言标签、场景说明、标签、代码行数、创建时间；支持关键词搜索与语言筛选（语言标签按数据实时汇总）。
- 点击任意一条打开右侧抽屉：完整代码（等宽字体、可横向滚动）、标签、行数，以及**复制代码**按钮。
- 支持新增 / 编辑 / 删除；抽屉里也能直接编辑或删除。

`code_snippet` 表**原样复用**（title / language / description / code_content / tags 已经够用），没有新建表。

---

## 五、Phase 5 工程化补强：GitHub Actions

新增 `.github/workflows/ci.yml`，推送 master 或开 PR 时跑两个 job：

| Job | 步骤 |
| --- | --- |
| 后端 · 单元测试 | `actions/setup-java@v4`（temurin 21 + Maven 缓存）→ `mvn -B -ntp test` |
| 前端 · 类型检查 / 单元测试 / 构建 | `actions/setup-node@v4`（Node 20 + npm 缓存）→ `npm ci` → `npm run type-check` → `npm run test` → `npm run build` |

不需要 MySQL / Redis：后端全部是单元测试（Mockito），前端测试用 Vitest + jsdom，都不连真实服务。
README 顶部加了版本徽章与 CI 徽章。

### CI 文件的落地状态

`ci.yml` 已经写好并在本地验证过它跑的每一条命令（与手工执行的一致），但**推送时被 GitHub 拦下**：
当前这台机器上保存的推送凭证是一个 OAuth App 令牌，**没有 `workflow` 权限**，
任何包含 `.github/workflows/` 的推送会被整体拒绝（报错 `refusing to allow an OAuth App to create or update workflow`）。

补上这一步有两个办法，任选其一：

```bash
# 办法一：重新授权一次（会提示浏览器授权，勾上 workflow 权限），然后补一次提交
git add .github/workflows/ci.yml && git commit -m "ci: 后端 mvn test + 前端类型检查 / 测试 / 构建" && git push
```

```text
办法二：在 GitHub 网页上新建文件 .github/workflows/ci.yml，把本机
kingdom-studio/.github/workflows/ci.yml 的内容整段粘进去提交。
```

文件本身的命令与本地验证完全一致：后端 `mvn -B -ntp test`；前端 `npm ci` → `npm run type-check` → `npm run test` → `npm run build`。
另外**「失败阻止合并」需要在仓库设置里把 CI 设为必需检查**，这一步是仓库权限操作，脚本层做不到。

---

## 六、Phase 6 前端测试：Vitest

技术选型：`vitest` + `@vue/test-utils` + `jsdom`，配置独立写在 `vitest.config.ts`（生产构建不需要知道测试框架）。
接口层整体替换，只验证交互链路；`URL.createObjectURL` 与 `<a>.click()` 在 jsdom 里补了最小实现，用来断言「真的触发了下载」。

| 流程 | 用例 | 覆盖点 |
| --- | --- | --- |
| 动效工作台 | 2 | 选好方案 → 点生成拿到文件列表 → 点打包下载触发下载；切格式后重新生成，请求里带的是新格式 |
| AI 演奏工作室 | 4 | 填演奏码 → 查看摘要 → 导入成新曲目；分享列表显示演奏码与被导入次数；拿到演奏计划显示指标 → 点下载触发下载；没选曲子时不发请求 |
| 项目王国 | 3 | 列表渲染出项目名 / 状态 / 成果数字；点项目名跳到详情路由；状态筛选带上查询条件重新请求 |
| Markdown 渲染 | 5 | 标题 / 列表 / 加粗 / 行内代码；**脚本与标签被转义**；空内容不产生标签；标题层级封顶；摘要去掉标记符号 |

`npm run test` 一次跑完：4 个文件 14 个用例。

---

## 七、Phase 7 版本统一

原来版本号散在 8 处手工维护，现在收敛成一条链路：

```
frontend/package.json  ← 唯一需要手改的地方
        │  npm run version:sync  （frontend/scripts/sync-version.mjs）
        ├─→ backend/pom.xml                    项目版本
        ├─→ backend/.../application.yml        info.app.version
        │        ↓  后端运行时读取
        │      /api/health 的 version、Swagger 文档版本、两个扩展的模块自检文案
        └─→ README.md                          版本徽章、当前版本行、模块状态表
```

- 后端不再有硬编码版本号：`HealthService`、`OpenApiConfig`、`MusicService`、`MotionService` 统一读 `${info.app.version}`。
- 前端页脚读 `__APP_VERSION__`（vite.config.ts 从 package.json 注入），模板里通过 `@/config/app` 导入。
  **页面版号、健康检查、接口文档、README 四处现在必然一致**，改版本只需要改 package.json 再跑一次同步脚本。
- 实测：`/api/health` 返回 `v1.2.0`；动效工作台自检文案为「工作台已就绪 · v1.2.0 · 已上线（…）」；页脚显示 v1.2.0。

---

## 八、界面统一（宿主）

原来主站是深红 + 金的重色，扩展是两个深色工具站。这一轮把宿主统一成：

- 底色暖白 `#faf8f4`、面板纯白、浅灰描边 `#ebe5da`，文字用暖黑与两级灰。
- **唯一强调色是金** `#c2963a`：眉标、页脚皇冠、选中态、进度条、星级、关键按钮。
- Element Plus 的主色从默认蓝改成金色（`--el-color-primary` 等一组变量），宿主页面里不再出现蓝色；
  AI 演奏工作室在自己的容器里把主色改回紫色，保持黑 + 紫绿的既有视觉。
- 工作台首页从「说明卡片 + 规划中标签」改成**王国概览**：项目 / 技术记录 / 成长节点 / 代码片段四个数字，
  四张模块卡（带数量与最新动态）、两个扩展入口、最近成长节点与服务状态。

---

## 九、数据库变化

一个脚本：`db/host_modules_v12.sql`（**幂等，可重复执行**，第二次全部打印「已存在，跳过」）。

| 变化 | 内容 |
| --- | --- |
| `project` 加列 | `demo_url`、`highlights`、`code_lines`、`test_count`、`commit_count` |
| `technology` 加列 | `used_projects` |
| `technology` 约束 | 分类 CHECK 放开到 `Java / Spring / Database / AI / Frontend / DevOps` |
| `timeline` 加列 | `event_date`、`related_project` + `idx_timeline_event_date` 索引 |
| 内容补录 | 6 个项目补齐演示地址与 Markdown 亮点（新增 WEDA 博客站）；14 项技术补齐项目应用与 4 项新记录；时间线补 4 个精确节点；代码知识库补 1 条可复用的迁移模板 |

内容只写能对上事实的东西：有部署的才填 `demo_url`（九八 → `jiuba-nu.vercel.app`），
代码量 / 测试数 / 提交数是实测值（Kingdom Studio：30955 行、333 个测试、17 次提交），
其余项目这三项留空，界面上直接不显示 —— 不编数。

---

## 十、接口变化

主站新增 **19 个接口**（后端总计 87 个）：

| 模块 | 接口 |
| --- | --- |
| 项目王国（5） | `GET /api/projects`、`GET /api/projects/{id}`、`POST /api/projects`、`PUT /api/projects/{id}`、`DELETE /api/projects/{id}` |
| 技术图鉴（5） | `GET /api/technologies`（整页数据：分类导航 + 卡片）、`GET /api/technologies/{id}`、`POST`、`PUT`、`DELETE` |
| 成长时间线（4） | `GET /api/timeline`、`POST /api/timeline`、`PUT /api/timeline/{id}`、`DELETE /api/timeline/{id}` |
| 代码知识库（5） | `GET /api/code-snippets`、`GET /api/code-snippets/{id}`、`POST`、`PUT`、`DELETE` |

既有接口与扩展接口**一个都没有删改**（`/api/health` 除版本来源外行为不变）。

---

## 十一、测试报告

| 项目 | 命令 | 结果 |
| --- | --- | --- |
| 后端单元测试 | `mvn -o test` | **333 个用例全部通过**（本次新增 32 个：项目王国 10 / 技术图鉴 9 / 时间线 6 / 代码知识库 7） |
| 前端类型检查 | `npx vue-tsc --noEmit` | 退出码 0 |
| 前端单元测试 | `npm run test` | **4 个文件 14 个用例全部通过** |
| 前端构建 | `npm run build` | `✓ built` |
| 迁移幂等 | `mysql < db/host_modules_v12.sql` 连续两次 | 第二次全部「已存在，跳过」，结果一致 |

接口实测（本机后端 8080）：

| 场景 | 结果 |
| --- | --- |
| 项目 CRUD | 新增 → 修改（状态变「持续开发」、完成度 55%）→ 重名新增被拒（400「已存在同名项目」）→ 查不存在的 id 返回 404 → 删除后查询 404 |
| 技术图鉴 | 14 项，分类导航 `全部 14 / Java 1 / Spring 3 / 数据库 2 / AI 2 / 前端 4 / 工程化 2`；DevOps 过滤返回「Vercel 部署 ★★★☆☆、GitHub Actions ★★☆☆☆」 |
| 成长时间线 | 顺序为 2026-09-27 → 2026-09-27 → 2026-09-24 → 2026-09-22 → 2026 … → 2023 |
| 代码知识库 | 首条「MySQL 幂等 ALTER 模板」8 行，详情返回完整正文 |

界面实测（浏览器，后端 + 前端开发服务器）：

| 页面 | 结果 |
| --- | --- |
| 工作台 | 概览数字 6 项目 / 14 技术 / 8 成长节点 / 4 代码片段；模块卡显示「6 座建筑 · 1 座在建」等；服务状态 v1.2.0、MySQL/Redis 均 UP |
| 项目王国 | 6 张卡片，状态与完成度正确（Kingdom Studio 20%、九八 100%、居家 60%）；成果行显示「30955 行代码 · 333 个测试 · 17 次提交」 |
| 项目详情 | 亮点 Markdown 渲染出「核心能力 / 工程细节」两个小标题与 5 条列表；技术栈按 前端 / 后端 / 数据库 分组；成果面板四项齐全 |
| 技术图鉴 | 左栏分类计数正确，卡片星级与项目应用标签正常 |
| 成长时间线 | 4 个年份分组，精确节点带 ●●●●●，关联项目标签正确 |
| 代码知识库 | 语言标签「全部 / Java / JavaScript / SQL」；点开抽屉显示代码与「复制代码 / 编辑 / 删除 / 关闭」四个动作 |
| 扩展回归 | 动效工作台正常渲染（自检文案 v1.2.0）；AI 演奏工作室仍是黑底 + 紫色主色（`--el-color-primary: #a78bfa`），四个页签在位 |

### 实测中发现并修掉的问题

1. **工作台把「最近 3 个节点」当成了节点总数**：模块卡显示 3 个节点，实际 8 个。已改成读完整列表长度，最近 3 条仍单独展示。
2. **后端跑的是旧包**：改完 `project` 成果字段后只编译没重新打包，详情页成果面板一直是空的。重新打包重启后四项齐全 —— 这类「代码改了但服务没换」的坑以接口回读为准。
3. **模板里的全局常量 vue-tsc 不认**：`__APP_VERSION__` 直接写在模板里会报 TS2339。改成 `src/config/app.ts` 导出常量，模板走 import。
4. **jsdom 不支持真实导航**：下载用到的 `<a download>.click()` 抛 "Not implemented: navigation"。在测试 setup 里把它换成空实现，下载是否发生改由 `URL.createObjectURL` 的调用来断言。

---

## 十二、边界

- **没有编造数据**：项目的代码量 / 测试数 / 提交数只有 Kingdom Studio 有实测值，其余留空，界面不显示空格子。
- **主站没有鉴权**：`SecurityConfig` 仍是骨架，四个模块的写接口目前是开放的；这是既有的安全边界，本轮没有扩大也没有收紧。
- **CI 只是「跑起来」**：要让红灯真正阻止合并，需要在 GitHub 仓库设置里把 CI 设为必需检查。
- **Markdown 是子集**：支持标题 / 列表 / 加粗 / 行内代码，不支持表格、图片、引用与代码块；渲染前先做 HTML 转义。
- **演示数据**：本机库里 `project` 有 6 条（含新增的 WEDA 博客站）、`technology` 14 条、`timeline` 8 条、`code_snippet` 4 条，
  另有接口自测产生的项目 id 11（已逻辑删除）。
