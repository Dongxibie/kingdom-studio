# 音乐 Agent V1.2 · AI 驱动的游戏音乐演奏工作台

> 目标：在已有的「解析 → 映射 → 时间线 → 演出计划 → 宏导出」之上，补齐**难度分层、游戏乐器生态、曲谱分享**三件事，
> 并把工作台界面统一成黑色 + 深灰 + 紫绿强调的一版。
> 解析器、映射引擎、时间线、演奏计划、宏导出全部原样保留，新能力都建在 `Performance Plan` 之上，没有再造第二套时间线。

---

## 一、修改文件

### 后端（新增 12 个，修改 15 个）

| 文件 | 类型 | 说明 |
| --- | --- | --- |
| `analysis/ProfileMatchService.java` | 新增 | 游戏乐器匹配：按「落键覆盖率 + 键数距离 + 游戏/乐器命中」打分并给出理由 |
| `analysis/DifficultyRule.java` | 修改 | 在五星之上补分层（简单 / 普通 / 高级）与适合人群（新手 / 有基础 / 熟练） |
| `analysis/SongAnalysisService.java` | 修改 | 分析卡带上分层与适合人群 |
| `share/ShareCode.java` | 新增 | 演奏码：`KS-MUSIC-2026-A001` 生成 / 解析 / 归一化（`A007` 这种简写也认） |
| `share/PerformanceShare.java` | 新增 | 分享实体（`performance_share`） |
| `share/PerformanceShareMapper.java` | 新增 | 分享 Mapper |
| `share/PerformanceShareService.java` | 新增 | 生成快照、查看摘要、导入还原（含导入计数） |
| `share/PerformanceShareController.java` | 新增 | 分享的四个接口 |
| `share/dto/PerformanceShareDTO.java`、`share/vo/PerformanceShareVO.java` | 新增 | 分享入参 / 出参 |
| `vo/ProfileMatchVO.java` | 新增 | 匹配结果的出参（分数、覆盖率、理由清单） |
| `controller/MusicInsightController.java` | 修改 | 增加 `GET /music/tasks/{taskId}/profiles/match` |
| `entity/InstrumentProfile.java`、`dto/InstrumentProfileSaveDTO.java`、`vo/InstrumentProfileVO.java`、`service/InstrumentProfileService.java` | 修改 | 乐器档案补 `game` / `octaveRange` / `specialRules` 三个字段并落库 |
| `service/MusicTaskService.java` | 修改 | `createDerived(song, sourceRef)`：分享导入时按快照还原成一条新曲目 |
| `vo/SongAnalysisVO.java`、`vo/MusicTaskListItemVO.java` | 修改 | 分析卡带 `difficultyTier` / `difficultyTierLabel` / `audience`；曲库列表带 `difficultyTier` |
| `vo/MusicModuleVO.java`、`service/MusicService.java` | 修改 | 模块自检的能力清单补上难度评估、游戏乐器匹配、曲谱分享 |

### 后端测试（新增 3 个，修改 1 个）

| 文件 | 类型 | 用例 | 覆盖点 |
| --- | --- | --- | --- |
| `share/ShareCodeTest.java` | 新增 | 5 | 按 id 生成、字母段进位、`A007` 简写归一化、非法码拒绝 |
| `share/PerformanceShareServiceTest.java` | 新增 | 12 | 快照自包含、摘要文案、导入还原成新曲目、导入计数、同码重复导入、快照缺字段时的容错 |
| `analysis/ProfileMatchServiceTest.java` | 新增 | 7 | 全覆盖优先、键数距离、游戏 / 乐器命中加分、理由文案、空档案兜底 |
| `analysis/DifficultyRuleTest.java` | 修改 | +3 | 分层与适合人群的取值边界 |

### 前端（新增 2 个，修改 11 个）

| 文件 | 类型 | 说明 |
| --- | --- | --- |
| `music-studio/components/MusicLibraryRail.vue` | 新增 | 左侧音乐库：我的歌曲 / 最近演奏 / 收藏 / 分享四个页签，含演奏码生成、查看、导入 |
| `music-studio/components/GameInstrumentPicker.vue` | 新增 | 游戏 · 乐器 · 键数三条件筛选 + 匹配理由 + 档案新建 / 编辑 / 删除 |
| `music-studio/styles/studio.css` | 修改 | 配色整体换成黑色 + 深灰 + 紫 / 绿强调 |
| `music-studio/composables/useStudioSession.ts` | 修改 | 最近演奏记录、换曲子后重置方案、AI 建议落成方案 |
| `music-studio/components/SongAnalysisCard.vue` | 修改 | 展示分层与适合人群；预计演奏改为「按推荐方案落键」 |
| `music-studio/types/studio.ts`、`music-agent/types/music.ts`、`music-agent/api/music.ts` | 修改 | 补匹配 / 分享的类型与接口；曲目来源补 `SHARE` 与统一的中文来源标签 |
| `music-studio/views/StudioComposerView.vue`、`views/StudioDashboardView.vue` | 修改 | 左栏换成音乐库 + 游戏乐器选择；曲目台展示分层与来源 |
| `music-agent/components/MusicTaskList.vue` | 修改 | 来源标签走同一套文字 |
| `layout/BasicLayout.vue`、`motion-lab/views/MotionResourceView.vue`、`music-agent/views/MusicAgentView.vue` | 修改 | 版本号统一到 v1.2.0 |

### 文档与数据库

| 文件 | 类型 | 说明 |
| --- | --- | --- |
| `db/extensions_music_game.sql` | 新增 | 乐器档案三字段 + 索引 + 内置档案补游戏信息 + `performance_share` 表 + 曲目来源放开 SHARE（幂等） |
| `db/extensions_music.sql` | 修改 | 全新安装时 `chk_music_task_source` 直接允许 `MIDI / JIANPU / SHARE` |
| `docs/EXECUTION-ADAPTER-INTERFACE.md` | 新增 | 自动演奏准备层：把 `execute / pause / stop / status` 对应到已有执行层，列出安全门禁 |
| `README.md` | 修改 | 版本与两个扩展的能力清单更新到 v1.2.0 |

---

## 二、数据库变化

| 变化 | 内容 |
| --- | --- |
| `instrument_profile` 加三列 | `game`（所属游戏）、`octave_range`（可用音域，如 `C4–C6`）、`special_rules`（演奏注意事项） |
| `instrument_profile` 加索引 | `idx_instrument_game`，按游戏筛选走索引 |
| 内置档案补信息 | 光遇式 15 键 → `光遇 Sky` / `C4–C6`；键盘式 12 键 → `C4–B4`；八音盒 8 键 → `C4–C5` |
| 新增档案 | `Minecraft 音符盒 25 键`（`Minecraft` / `F#3–F#5`，附示例布局说明） |
| 新表 `performance_share` | `share_code`(唯一) / `performance_plan_id` / `task_id` / `creator` / `title` / `difficulty` / `game` / `instrument` / `note_count` / `duration_ms` / `import_count` / `payload`(自包含快照) / `deleted` / 时间戳 |
| `chk_music_task_source` | 由 `MIDI / JIANPU` 放开为 `MIDI / JIANPU / SHARE` |

迁移脚本用 `information_schema` 判断后执行，**连续执行两遍结果一致**（第二遍全部打印「已存在，跳过」），
加约束时按 `CONSTRAINT_SCHEMA + CONSTRAINT_NAME` 关联 `TABLE_CONSTRAINTS` 定位到表，不依赖库名写死。

---

## 三、接口变化

音乐模块接口由 33 个到 38 个，本次新增 5 个：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/music/tasks/{taskId}/profiles/match?game=&instrument=&keyCount=&limit=` | 游戏乐器匹配：返回分数、覆盖率、键数差与命中理由 |
| POST | `/api/music/shares?taskId=` | 把曲目 + 当前方案 + 最近一次计划打包成演奏码 |
| GET | `/api/music/shares` | 分享列表（演奏码、曲名、难度、被导入次数） |
| GET | `/api/music/shares/{shareCode}` | 按码查看摘要（不导入也能知道是什么曲子） |
| POST | `/api/music/shares/{shareCode}/import` | 导入还原：在本库生成一条来源为 `SHARE` 的新曲目 |

既有接口的变化都是加字段，不改变原语义：

- `GET /api/music/tasks/{id}/analysis`：增加 `difficultyTier` / `difficultyTierLabel` / `audience`。
- `GET /api/music/tasks`：列表项增加 `difficultyTier`。
- `GET /api/music/instruments` 与增改接口：增加 `game` / `octaveRange` / `specialRules`。

---

## 四、六件事怎么落地的

### 1 AI 曲谱生成助手：模型只给建议，落成方案由规则决定

助手返回的仍然是 `{难度档, 乐器, 建议[]}`，白名单之外的值一律丢弃；点「应用方案」走的是
「AI 建议 → 后端重新映射 → 存成演奏方案」这条链路：谱子本身不变，方案里记下键位、超范围策略与间隔。
在编排台点一次「让它听起来简单一点」再点应用，会得到一条「（初学模式）」的衍生曲目和一套
`AI 建议 · 简单版` 方案（方案名带 AI 建议前缀，与内置 A/B/C 三套并存）。

### 2 难度分层：星级之上给出「分层 + 适合谁」

沿用原来的三轴计分（音域跨度 / 速度 / 密度），在星级之上再归纳成三档分层，
并说明适合谁、为什么这么判：

| 星级 | 文字 | 分层 | 适合谁 |
| --- | --- | --- | --- |
| ★ | 入门 | 简单 | 新手 |
| ★★ | 简单 | 简单 | 新手 |
| ★★★ | 进阶 | 普通 | 有基础 |
| ★★★★ | 较难 | 普通 | 有基础 |
| ★★★★★ | 挑战 | 高级 | 熟练 |

分析卡上星级、分层、适合人群分三个位置展示，下面挂「凭什么这么判」的计分依据，每条都标出计分与否。

### 3 游戏乐器生态：档案带游戏信息，匹配靠试算

档案新增游戏、音域、注意事项三个字段后，选择「游戏 + 乐器 + 键数」就能得到一份排序结果：
先真实跑一遍映射看有多少音落得下来，全部落下的排前面；再比键数与目标的距离，越接近越靠前；
游戏与乐器命中额外加分。每个候选都写明理由，例如
`所有音都能落键，不需要改动曲谱`、`键数正好是你要的 15 键`、`键数与目标差 10 个（实际 25 键）`。
档案本身可以在界面上新建 / 编辑 / 删除，新增一种乐器不需要改代码。

### 4 曲谱分享：演奏码 + 自包含快照

演奏码形如 `KS-MUSIC-2026-A001`（年份 + 字母段 + 三位流水，单字母段满 999 递增到下一段），
快照里带曲名、来源、BPM、拍号、难度（星级 / 分层 / 适合谁）、方案（键位 + 策略 + 速度 + 间隔）、
演出计划（时长、音符数、按键数、事件列表）与完整音符表 —— 别人导入时不需要你有这份数据库，
导入后会在本库生成一条 **来源标注为「分享」** 的新曲目，可以直接演奏、导出脚本，被导入次数同步 +1。

### 5 工作台界面：黑色 + 深灰 + 紫绿

- 底色 `#07080b`、面板 `#0f1115` 一级层叠，文本三级灰阶；强调色只有两支：紫 `#a78bfa`、绿 `#6ede9a`，蓝色系全部退出。
- 三栏结构：左 = 音乐库（我的歌曲 / 最近演奏 / 收藏 / 分享），中 = 时间线与虚拟键盘，右 = AI 助手与演奏优化；游戏乐器选择在左栏下段。
- 音符块用绿色、按键高亮用紫色；星级、难度分层、匹配理由都走同一套强调色，不再出现传统后台式的表格与表单堆叠。

### 6 自动演奏准备层：只完善接口，不驱动真实输入

这一层没有新增抽象，而是把用户侧的四个动作对应到已有执行层，并写清安全门禁：

| 用户动作 | 对应实现 | 行为 |
| --- | --- | --- |
| 开始 | `ExecutionAdapter.execute(PlanContext)` 经 `ExecutionService.start` | 必须有演出计划、用户已勾选确认、目标窗口已切到前台 |
| 暂停 / 继续 | `ExecutionService.pause / resume` | 窗口一换自动暂停 |
| 停止 | `ExecutionService.stop / emergencyStop` | 立刻停止并松开所有按下的键 |
| 状态 | `ExecutionService.snapshot` | 回报进度、当前事件、开关状态 |

预览执行器（`PreviewExecutor`）只做模拟：时间线走、键盘亮、状态回报，不向系统发送任何按键。
模块仍然默认关闭，需要后端显式开关；界面上常驻三条提示（需主动开启 / 执行前确认窗口 / ESC 随时急停），
不存在后台隐藏执行、也不注入任何进程。

---

## 五、测试报告

| 项目 | 命令 | 结果 |
| --- | --- | --- |
| 后端测试 | `mvn -o test` | **301 个用例全部通过**（音乐模块 160 个，其中本次新增 27 个：分享码 5 / 分享服务 12 / 乐器匹配 7 / 难度分层 3） |
| 前端类型检查 | `npx vue-tsc --noEmit` | 退出码 0，无错误 |
| 前端构建 | `npm run build` | `✓ built`，产物正常 |
| 迁移幂等 | `mysql < db/extensions_music_game.sql` 连续两次 | 第二次全部「已存在，跳过」，结果一致 |

接口与界面实测（本机后端 8080 + 前端 5173）：

| 场景 | 结果 |
| --- | --- |
| 难度分层 | 两只老虎（演示）→ 1 星 · 入门 · 分层简单 · 适合新手；音阶练习 → 2 星 · 简单 |
| 乐器匹配（游戏=Minecraft，键数=15） | 分数依次 140 / 120 / 117 / 113，Minecraft 音符盒 25 键排第一，理由为「所有音都能落键」「键数与目标差 10 个」 |
| 演奏码生成 | `KS-MUSIC-2026-A001`，摘要「32 个音符 · 15 秒 / 乐器：光遇式 15 键 / 难度：1 星 · 入门（简单）/ 64 条按键动作」 |
| 演奏码导入 | 生成新曲目（`source_type=SHARE`，来源标注「演奏码 KS-MUSIC-2026-A001 ← 李泽龙」），被导入次数 +1，界面提示「已导入为「两只老虎（演示）」，可以直接去编排台演奏或导出」 |
| AI 建议落成方案 | 点「让它听起来简单一点」→ 应用 → 生成「（初学模式）」衍生曲目，方案面板出现 `AI 建议 · 简单版` 并标为正在使用，时长 20 秒 → 25 秒 |
| 曲目来源标签 | 导入的曲子在各处显示为「分享」，不再混进「简谱」 |

### 实测中发现并修掉的问题

1. **换曲子后方案没有跟着换**：曲库切歌后仍带着上一首的方案 id，导致分析与计划请求被后端拒绝（界面反复弹「方案与曲目不匹配」）。已在读取方案列表时按当前曲目重置选择。
2. **导入演奏码后分享列表不刷新**：被导入次数还显示旧值。已在导入成功后一并刷新分享列表。
3. **分析卡上「入门」与「简单」并列容易误读**：两个词分别来自星级文字与分层，已给分层加前缀并统一展示位置。
4. **预计演奏的数值与副标题重复**（都是「20 秒」）：副标题改为「按推荐方案落键」，说明这个时长是按推荐键位算出来的。
5. **迁移脚本的约束查询用错表**：`information_schema.CHECK_CONSTRAINTS` 没有表名，改为关联 `TABLE_CONSTRAINTS` 定位，脚本可重复执行。

### 环境说明

本轮界面验证以 DOM 快照与接口实测为主。内置浏览器的截图面在验证过程中停止响应（先是 3 秒准备超时，随后报 guest 未挂载），
因此只留下了曲目台的一张截图；配色与布局的事实依据是样式表中的色板变量与页面结构，不含深蓝色系。截图通道恢复后可以补一轮纯视觉截图。

---

## 六、边界

- 分享快照是**静态复制**：导入后对方的改动不会回流到原作者，这一点写在界面的说明文字里。
- 演奏码按年份 + 字母段生成，同一字母段满 999 自动进位，不做全局唯一性抢占。
- 难度分层是规则计算的结论，不做「人人都觉得难」的主观校准。
- 自动演奏只到接口与状态回报为止，真实输入仍由本机运行时在用户主动开启、勾选确认、窗口在前台时才执行。
- 本机开发库里留了演示数据：演奏码 `KS-MUSIC-2026-A001` 及其一条导入副本、`AI 建议 · 简单版` 方案与「（初学模式）」衍生曲目。
