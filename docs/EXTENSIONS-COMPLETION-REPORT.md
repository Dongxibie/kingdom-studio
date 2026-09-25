# Kingdom Studio 扩展模块 · 完成报告

> 范围：`Motion Lab`（动效基因库）与 `Music Agent`（音乐 Agent），外加 `Desktop Agent` 的协议设计。
> 原则：**只新增，不改动既有业务代码**。既有的四个业务模块（项目王国 / 技术图鉴 / 成长时间线 / 代码知识库）与页面一行业务逻辑都没动。

## 一、总览

| 阶段 | 内容 | 状态 | 验证方式 |
| --- | --- | --- | --- |
| Phase 0 | 项目扫描与方案（找出可扩展点、列出风险） | 已完成 | `docs/PHASE-0-EXTENSIONS-SCAN.md` |
| Phase 1 | 扩展架构：目录约定、深色工具风主题、三栏外壳、路由与菜单、模块自检接口 | 已完成 | 前端构建 + 两个 `/ping` 接口 |
| Phase 2 | Motion Lab：两张表、完整 CRUD、三种语言代码生成、沙箱预览 | 已完成 | 单测 + 浏览器端到端 + 落库核对 |
| Phase 3 | Motion Crawler：GitHub 采集、三级去重、关键词分类、限流与重试 | 已完成 | 单测 + 真实采集 + 界面入口 |
| Phase 4 | Music Agent：三张表、MIDI / 简谱解析、映射引擎、按键序列导出 | 已完成 | 单测 + 真实 MIDI 与简谱实跑 |
| Phase 5 | 演奏时间线：钢琴卷帘、按键轨、虚拟键盘、Demo 回放与按键动画 | 已完成 | 浏览器实测（含回放与定位） |
| Phase 6 | Desktop Agent：WebSocket 协议 + 派发计划（模拟） | 已完成 | 单测 + 接口与界面实测 |

**代码量**：后端扩展新增 66 个 Java 文件（57 个主代码 + 9 个测试类）、前端 `extensions/` 下 33 个文件、4 个 SQL 脚本、3 份文档。
**测试**：后端 70 个单元测试全绿（0 失败 / 0 错误），前端 `vue-tsc --noEmit` 与 `npm run build` 均通过。

## 二、Motion Lab（动效基因库）

### 功能

| 功能 | 说明 |
| --- | --- |
| 动效资源管理 | 十个分类（入场 / 悬停 / 滚动 / 文字 / 粒子 / 三维 / 玻璃 / 光标 / 背景 / 加载），支持关键词搜索、分类筛选、分页、增删改 |
| 代码产物 | 每条资源可保存 Prompt、Vue 3、React、CSS、Three.js 五份内容；可从模板一键生成再手工调整 |
| 实景预览 | 预览走 `sandbox` iframe（仅 `allow-scripts`，不开 `allow-same-origin`），代码不越出沙箱 |
| GitHub 采集 | 按关键词搜索仓库并入库；命中限流立即停止并如实汇报，不硬撞 |
| 自动分类 | 关键词权重打分，命中不了就落 `DRAFT` 交给人确认，不硬塞一个分类 |
| 三级去重 | 同一仓库地址 → 同一内容指纹 → README 文本相似（3-gram Jaccard + 包含率兜底） |

### 采集器的三条纪律（已用测试固化）

1. **不疯狂请求**：每个关键词之间固定间隔；响应头 `X-RateLimit-Remaining: 0` 或 HTTP 403/429 立刻停止并在 `notes` 里说明原因。
2. **重试只针对值得重试的**：5xx 与网络异常退避 1s / 2s 重试，403/429 与其余 4xx 直接失败，不做无意义重试。
3. **缓存**：同一关键词的原始响应在 Redis 缓存 30 分钟，重复点采集不会重复打接口。

### 实测数据（本机）

- 采集 `css animation hover`：取回 3 条，分类全部落到 `Hover`。
- 第二次采集同一关键词：`created 0 / skippedByUrl 4`，命中缓存，库内数量不变（去重生效）。
- 界面「采集 GitHub」弹窗：默认打开「试算」开关，试算不写库；关掉后正式采集，14 → 16 条，分类 `3D 2`。

## 三、Music Agent（音乐 Agent）

### 功能

| 功能 | 说明 |
| --- | --- |
| MIDI 解析 | JDK 自带的 `javax.sound.midi`，纯 Java、不引入 Python 服务；支持多轨、变速（tempo 分段换算）、力度 0 的 Note On 当作松开、同音重叠按栈配对 |
| 简谱输入 | 支持 `1=C 4/4 BPM=96` 头行、音级 1-7 与休止 0、八度记号 `'` `,`、变化音 `#` `b`、延长线 `-`、附点 `.`、减时线 `_`、小节线 |
| 音符时间线 | 钢琴卷帘：位置 = 时间、高度 = 音高、宽度 = 时值；黑键与白键分色；点击或拖动可定位 |
| 按键轨 | 每次按键动作单独标记，和弦显示为 `Z+C`，被调整过的用虚线框标出 |
| Instrument Profile 映射 | 档案存在数据库里（三套预置：光遇式 15 键 / 键盘式 12 键 / 八音盒 8 键），新增乐器只加数据不改代码 |
| 超出音域的处理 | 三种策略：跳过（SKIP）/ 就近落键（NEAREST）/ 移八度（SHIFT_OCTAVE），每种都会在导出里说明 |
| 按键序列导出 | 文本可直接复制去练：速度与拍号、键位图例（`Z=C4`）、逐行按键与音名、未落键清单 |
| Demo 回放 | 网页内用 WebAudio 合成发声，时间线走针 + 虚拟键盘同步点亮；**不驱动系统输入** |

### 映射为什么不是「减一下当下标」

相邻键之间差的可能是半音（半音排列），也可能是一个音级（只有白键的乐器）：C 后面直接是 D，没有 C#。
所以引擎按 `mappingMode` + `scale` 展开一张「第几个键发声什么音」的表，再拿音符去查，
而不是用「音高 − 基准音高」当按键下标。三套预置档案里，同一首 C 大调音阶的落键结果分别是：

| 档案 | 键数 | 结果 |
| --- | --- | --- |
| 光遇式 15 键（音阶排列） | 15 | 8 个音全部落键，0 调整：`Z X C V B N M A` |
| 键盘式 12 键（半音排列） | 12 | 7 个落键 + 1 个未落键（C5 超出 B4 上限，策略为跳过） |
| 八音盒 8 键（移八度） | 8 | 8 个音全部落键：`A S D F G H J K` |

### 实测数据（本机）

- 上传一份 8 个音的 C 大调 MIDI：解析为 `C4 D4 E4 F4 G4 A4 B4 C5`，120 BPM、4/4、时长 4000ms、音域 C4–C5。
- 粘贴简谱「小星星」片段：14 个音符、96 BPM、时长 10000ms、音域 C4–A4。
- 从界面点「Demo 回放」：走针从 0:00.000 推进到 0:03.857，按键高亮与按键轨同步（C → … → A），停止后高亮清空。

## 四、Desktop Agent（协议与模拟）

**这一部分不会按下任何一个键。** 交付的是通道设计与命令流：

| 内容 | 位置 |
| --- | --- |
| 协议文档 | `docs/desktop-agent-protocol.md`（消息信封、5 种消息类型、错误码、安全约束） |
| 协议接口 | `GET /api/desktop/protocol` 返回同一份内容（文档会过期，接口返回的能被测试直接断言） |
| 状态接口 | `GET /api/desktop/ping`：`available=false`、`mode=MOCK`、端点与协议版本 |
| 派发计划 | `POST /api/desktop/dispatch`：返回按下 / 松开成对的命令流与调整说明 |
| 界面面板 | 音乐 Agent 右侧「桌面代理（模拟）」：一键生成派发计划并查看命令流 |

派发前的安全规则（写死在代码里，不放进配置）：

| 规则 | 处理 |
| --- | --- |
| 同一时刻多个键（和弦） | 各自成对发送，时间点相同 |
| 同一个键没松开又要按下 | 顺延到上次松开之后 60ms，并说明 |
| 按住时长 < 40ms / > 2000ms | 按 40ms 发送 / 截断到 2000ms，并说明 |
| 多字符的键（如 `F5`） | 跳过并说明（协议只传单键） |
| 命令总数 > 4000 条 | 直接拒绝，要求先裁短曲子 |
| 前端想直接提交按键列表 | 不接受：请求只带 `taskId` + `profileId`，按键由服务端按档案重算 |

实测：任务「scale」+ 档案「光遇式 15 键」→ 派发计划 16 条命令（8 个音 × 按下/松开），无警告，模式 `MOCK`。

## 五、交付物清单

### 新增（后端）

- `modules/motion/**`：实体 2、Mapper 2、DTO 4、VO 4、Service 2、Controller 2、采集器 2
- `modules/music/**`：实体 3、Mapper 3、DTO 3、VO 5、解析器 3、Service 3、Controller 3
- `modules/desktop/**`：DTO 1、VO 3、Service 1、Controller 1
- `common/PageVO.java`：分页返回外壳
- 测试 9 个类（`motion` 4 / `music` 4 / `desktop` 1）

### 新增（前端 `src/extensions/`）

- `_shared/`：三栏外壳、空状态、代码块、状态标签、深色工具风主题、模块自检接口
- `motion-lab/`：视图 1、组件 6、API 2、类型 2、工具 2
- `music-agent/`：视图 1、组件 7、API 1、类型 1、工具 2

### 新增（数据库与文档）

- `db/extensions_motion.sql`、`db/extensions_motion_seed.sql`（10 条带代码的种子资源）
- `db/extensions_music.sql`（3 张表 + 3 套乐器档案）
- `docs/PHASE-0-EXTENSIONS-SCAN.md`、`docs/desktop-agent-protocol.md`、本报告

### 修改（既有文件，均为追加）

| 文件 | 改动 |
| --- | --- |
| `frontend/src/router/index.ts` | 追加 2 条扩展路由 |
| `frontend/src/layout/BasicLayout.vue` | 追加「扩展」菜单分组（2 条菜单） |
| `.gitignore` | 追加 `*.tsbuildinfo`、`Thumbs.db` |

## 六、测试与验收

| 项目 | 命令 | 结果 |
| --- | --- | --- |
| 后端单测 | `mvn -o test` | 70 个测试，0 失败 0 错误 |
| 前端类型检查 | `npx vue-tsc --noEmit` | 通过 |
| 前端构建 | `npm run build` | 通过 |

后端测试分布（全部为 Mockito 单元测试，不依赖本机 MySQL / Redis 也能跑）：

| 测试类 | 用例数 | 覆盖点 |
| --- | --- | --- |
| `MotionServiceTest` | 9 | 分类校验、哈希去重、级联删除、分页收敛、代码 upsert |
| `MotionClassifierTest` | 5 | 关键词权重、命中留痕、未命中降级 |
| `MotionDedupeTest` | 6 | URL 归一化、内容哈希稳定、格式差异容忍、包含率与长度比值 |
| `MotionCrawlerServiceTest` | 6 | 试算不写库、限额用尽即停、403 不重试、5xx 才重试、422 快速失败 |
| `MidiParserTest` | 6 | tick→毫秒、中途变速、力度 0 当松开、同音重叠配对、截断收尾、字节流解析 |
| `JianpuParserTest` | 9 | 音级、头行、八度、时值、休止、变化音、斜杠断拍、行对齐 |
| `InstrumentMappingServiceTest` | 11 | 键位展开、三种策略、和弦归组、移调、导出文本、坏配置报错 |
| `MusicTaskServiceTest` | 8 | 落库与音符数、空简谱拒绝、坏 MIDI 拒绝、404、级联删除、映射入参 |
| `DesktopAgentServiceTest` | 10 | 命令成对、和弦同刻、重复按键顺延、时长上下限、非法键、空序列拒绝、排序 |

## 七、怎么跑起来

```bash
# 1. 建表（幂等，可重复执行；只新增扩展模块的表）
mysql -uroot -proot kingdom_studio < db/extensions_motion.sql
mysql -uroot -proot kingdom_studio < db/extensions_motion_seed.sql
mysql -uroot -proot kingdom_studio < db/extensions_music.sql

# 2. 后端
cd backend && mvn spring-boot:run            # http://localhost:8080/api

# 3. 前端
cd frontend && npm install && npm run dev    # http://localhost:5173
```

界面入口：主站左侧菜单「扩展」→「动效基因库」/「音乐 Agent」。
接口文档：`http://localhost:8080/api/swagger-ui.html`（扩展模块的标签为 10 / 11 / 12 开头的四组）。

## 八、可选后续

1. **采集量上去之后**：现在的分类是关键词规则，命中不了会落 `DRAFT`；等库里资源多了可以接一个模型来分类，规则继续作为兜底。
2. **真实演奏通道**：要实现 Desktop Agent 的真实执行，需要先具备急停（按住 ESC 立即松开所有键）、只对前台窗口生效、显式申请输入权限这三件事，协议文档第 6 节已列出。
3. **MP3 音频分析**：需要引入音频分析（librosa / music21），属于引入 Python 服务的范围，本期未做。
4. **乐器档案**：想加新乐器只需在 `instrument_profile` 里插一条数据（按键 JSON + 基准音高 + 音阶 + 超范围策略），不需要改代码。
