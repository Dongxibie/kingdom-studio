# 音乐 Agent · 产品化增强报告（曲库 / 分析 / 优化 / 方案 / 演示 / 导出）

> 目标：不增加复杂底层能力，把「解析 → 映射 → 导出」这条已经能跑的链路，做成一个**能演示、能复用、说得清**的产品。
> 六件事：曲目管理系统、音乐分析卡、自动优化、演奏方案、演示模式、导出中心升级。

---

## 一、修改文件

### 后端（新增 12 个，修改 7 个）

| 文件 | 类型 | 说明 |
| --- | --- | --- |
| `db/extensions_music_preset.sql` | 新增 | 收藏列 + 演奏方案表（幂等，可重复执行） |
| `entity/MusicPerformancePreset.java` | 新增 | 演奏方案实体 |
| `mapper/MusicPerformancePresetMapper.java` | 新增 | 方案 Mapper |
| `analysis/DifficultyRule.java` | 新增 | 难度规则：跨度 / 速度 / 密度三轴 → 1-5 星 |
| `analysis/SongAnalysisService.java` | 新增 | 曲目分析卡：难度、音域、预计演奏、推荐键位（真实试算） |
| `analysis/PerformanceOptimizerService.java` | 新增 | 演奏优化器：连按冲突 / 超范围 / 和弦 / 密度 / 长按 |
| `service/PerformanceTempo.java` | 新增 | 速度倍率与同键最小间隔（都在序列层做，仍走同一条校验） |
| `service/PerformancePresetService.java` | 新增 | 方案 CRUD + 内置 A/B/C 生成 + 解析 |
| `vo/SongAnalysisVO.java`、`vo/OptimizationVO.java`、`vo/PerformancePresetVO.java` | 新增 | 三个新接口的返回结构 |
| `dto/PerformancePresetDTO.java` | 新增 | 方案入参（带速度 / 间隔的区间校验） |
| `controller/MusicInsightController.java` | 新增 | 分析 / 优化 / 方案共 7 个端点 |
| `entity/MusicTask.java` | 修改 | 加 `favorite` |
| `service/MusicTaskService.java` | 修改 | 收藏开关、只看收藏、列表项补难度、`require()` |
| `service/PerformanceMacroService.java` | 修改 | 生成计划时按方案执行（档案 / 策略 / 速度 / 间隔） |
| `vo/MusicTaskListItemVO.java` | 修改 | 列表项补收藏与难度 |
| `controller/MusicTaskController.java` | 修改 | 分页加 `favorite` 参数、收藏端点 |
| `controller/PerformanceMacroController.java` | 修改 | 生成计划加可选 `presetId` |

### 后端测试（新增 4 个，修改 1 个）

`DifficultyRuleTest`（7）· `PerformanceOptimizerServiceTest`（10）· `PerformanceTempoTest`（7）· `PerformancePresetServiceTest`（12）· `PerformanceMacroServiceTest`（构造参数同步）

### 前端（新增 4 个，修改 9 个）

| 文件 | 类型 | 说明 |
| --- | --- | --- |
| `music-studio/types/studio.ts` | 新增 | 分析 / 优化 / 方案类型与展示常量 |
| `music-studio/components/SongAnalysisCard.vue` | 新增 | 音乐分析卡（星级 / 速度 / 音域 / 预计演奏 / 推荐键位 / 计分依据 / 备选方案） |
| `music-studio/components/PerformanceOptimizerPanel.vue` | 新增 | 优化建议列表 + 一键应用 |
| `music-studio/components/PerformancePresetBar.vue` | 新增 | 方案卡（A/B/C…），可应用、另存、删除 |
| `music-studio/components/ShowModeOverlay.vue` | 新增 | 演示模式：选曲 → 全屏演示（时间线 / 键盘 / 音频 / 当前按键 / 循环） |
| `music-agent/api/music.ts` | 修改 | 收藏、分析、优化、方案共 8 个函数；分页加收藏参数 |
| `music-agent/api/macro.ts` | 修改 | 生成计划支持 `presetId` |
| `music-agent/types/music.ts` | 修改 | 列表项补 `favorite` / `difficultyStars` / `difficultyLabel` |
| `music-studio/composables/useStudioSession.ts` | 修改 | 收藏、洞察、方案、应用建议（内置方案不动，另存优化版） |
| `music-studio/components/ExportCenter.vue` | 修改 | 播放计划事件数 / 秒数、支持格式勾选、按方案生成 |
| `music-studio/views/StudioDashboardView.vue` | 修改 | 收藏星、难度、创建时间、只看收藏、分析卡、演示模式入口 |
| `music-studio/views/StudioComposerView.vue` | 修改 | 方案栏、分析卡、优化器、演示模式入口 |
| `music-studio/views/StudioExportView.vue` | 修改 | 方案栏 + 当前方案随导出一起变 |

---

## 二、数据库变化

| 变更 | 内容 |
| --- | --- |
| `music_task.favorite` | `TINYINT NOT NULL DEFAULT 0`，并加索引 `idx_music_task_favorite (favorite, deleted, create_time)` |
| `music_performance_preset` | 新表：`id / task_id / name / profile_id / strategy / speed_scale / min_gap_ms / builtin / note / deleted / create_time / update_time`，唯一键 `(task_id, name)`，两个 CHECK（速度 0.50-2.00、间隔 0-500） |
| 幂等性 | 迁移脚本重放两次验证通过（第二次输出「已存在，跳过」）；没有删除或重命名任何既有列 |

---

## 三、接口变化

**新增 8 个：**

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/music/tasks/{id}/favorite` | 收藏 / 取消收藏，返回切换后的状态 |
| GET | `/api/music/tasks/{id}/analysis` | 曲目分析卡（可带 `profileId`） |
| GET | `/api/music/tasks/{id}/optimize` | 优化报告（`profileId` 必填，`strategy` / `presetId` 可选） |
| GET | `/api/music/tasks/{id}/presets` | 方案列表（首次访问补齐内置三套） |
| POST | `/api/music/tasks/{id}/presets` | 新建方案 |
| PUT | `/api/music/tasks/{id}/presets/{presetId}` | 修改方案 |
| DELETE | `/api/music/tasks/{id}/presets/{presetId}` | 删除方案 |
| GET | `/api/music/tasks/{id}/presets/{presetId}/keys` | 按方案跑一遍按键映射 |

**扩展 2 个（向后兼容）：**

| 接口 | 变化 |
| --- | --- |
| `GET /api/music/tasks` | 新增可选参数 `favorite`（只看收藏）；返回项新增 `favorite / difficultyStars / difficultyLabel` |
| `POST /api/music/tasks/{id}/performance-plan` | 新增可选参数 `presetId`；**不传时行为与之前完全一致**（实测同为 20016ms） |

---

## 四、六项功能怎么落地的

**1 · 曲目管理（曲库）**：收藏星（点击即翻转，本地先亮再以后端为准）、只看收藏、按曲名搜索、删除；每张卡片按需求列全：名称 / 来源 / 类型 / BPM / 音符数 / 音域 / 难度 / 创建时间。

**2 · 音乐分析卡**：难度星级（★☆☆☆☆ + 文字 + 一句话说明）、速度、音域（含半音跨度）、预计演奏时长、推荐键位。
推荐不是猜的：**把每个乐器档案都真实试算一遍**，谁能把所有音落下、谁用的键最少就推谁，另外两张备选方案一键可切。

**3 · 自动优化（Performance Optimizer）**：五条检查——同键重按太密、超出音域、多键和弦、按键过密、长按音。
每条都给出「在哪、为什么、怎么办」，并且**怎么办是可应用的参数**。实测在「两只老虎」上抓到了真实问题：
有 4 处同一个键在上一次松开后 0ms 就被再次按下 —— 正是需求里举的「连续重复按键 Z Z → 增加间隔 60ms 避免输入冲突」。

**4 · 演奏方案（Performance Preset）**：内置三套按库里的档案现算 —— 原版（默认档案 / 原速）、简单版（键数最少的档案 + 超范围移八度）、快速版（原速 ×1.35）；
也可以「另存为方案」把自己的设置存下来，或删掉不要的。方案是**唯一的速度与间隔载体**：生成计划与导出都按它执行。
实测：原版 20016ms → 快速版 14827ms（正好 ÷1.35）。

**5 · 演示模式（Show Mode）**：两步 —— 选歌（卡片带难度与时长）→ 开始演示。
演示页全屏铺开：大号当前时间 / 当前音符 / 当前按键、键盘实时点亮、时间线走针、音频播放、接下来几组按键预告、循环开关。

**6 · 导出中心升级**：顶部直接给「64 events / 14.8 秒 / 32 组按键 / 6 个键」；
下面一行 `✓ AutoHotkey 脚本 3.8 KB · ✓ TXT 按键时间线 1.3 KB · ✓ JSON 演奏计划 5.3 KB`；
保留逐格式预览与复制，并新增「复制用代码块右上角按钮」的提示。切换方案时事件数不变、时长跟着变（20.0 秒 → 14.8 秒）。

---

## 五、测试报告

| 项 | 结果 |
| --- | --- |
| 后端全量测试 | **274 个用例全绿**（0 失败 / 0 错误），比上一阶段 238 个新增 **36 个** |
| 新增测试分布 | `DifficultyRuleTest` 7 · `PerformanceOptimizerServiceTest` 10 · `PerformanceTempoTest` 7 · `PerformancePresetServiceTest` 12 |
| 前端类型检查 | `vue-tsc --noEmit` 通过 |
| 前端构建 | `npm run build` 通过（曲目台 7.1 kB、编排台 17.2 kB、导出中心 9.0 kB，均按页分包） |
| 接口实测 | 收藏切换 True→False、只看收藏过滤生效；分析卡给出星级 / 预计 20 秒 / 推荐 8 键模式；三套内置方案字段完整；优化器报出 4 处连按冲突 |
| 方案实测 | 原版 / 简单版 / 快速版生成计划分别为 20016 / 20016 / 14827 ms；不带 `presetId` 仍为 20016ms（行为未变） |
| 优化闭环实测 | 点「应用建议」→ 生成「优化版」（间隔 60ms），内置三套未被改动；再用优化版复查，冲突消失且报告注明「已按当前方案留出 60ms 同键间隔后重新检查」 |
| 浏览器实测 | 曲库收藏（toast + 星标变金）、难度与创建时间上卡；编排台方案栏 / 分析卡 / 优化器渲染正常；演示模式两步流程走通（时钟 1.69s → 3.92s、键盘高亮、当前按键与预告同步）；导出中心显示 64 events / 14.8 秒与三种格式勾选 |

### 实测中发现并修掉的问题

1. **星级公式到不了 5 星**：三条规则各贡献 2 分、满分 6 分，而 `1 + ceil(分/2)` 最多只能算出 4 星。
   改成一张明确的「分数 → 星级」对照表，并补测试钉住 5 分 = 4 星、6 分 = 5 星。
2. **「间隔」的判定口径**：最初按「两次按下之间」算，会出现「上一组还没松开就再按」被漏判的情况。
   统一改成 **「上一次松开到这一次按下」的空档**，优化器判定与 `PerformanceTempo` 应用两端同口径。
3. **优化器没看方案**：应用「间隔 60ms」后，优化器仍按原始序列报同一处冲突，像是建议没生效。
   现在优化器按当前方案（速度 + 间隔）调好序列再检查，复查结论会写明这一点。
4. **应用建议会改掉内置方案**：原版被改过之后就回不到原样了。改为：内置方案不动，自动另存一份「优化版」承接调整。

---

## 六、边界

- **难度只用库里已有的四个字段**（音符数 / 时长 / 速度 / 音域跨度），和弦比例不参与星级 ——
  这样曲库列表与详情卡上的星级永远一致，不会出现同一首歌两个难度。
- **速度与间隔在「序列」这一层生效**，之后仍走原来那条 `DesktopAgentService.plan` 校验：
  最短按住 40ms、命令数上限、单键校验都不绕开。
- **优化器只建议、不自动改**：每条建议都要用户点一下才落到方案上，避免「它自己把谱子改了」。
- **演示模式不改任何数据**：只播放与高亮，不写库、不触发本机按键。
