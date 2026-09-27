# Music Agent · AI 音乐助手变更报告（Phase 2 第一批）

> 目标：把系统从「音乐转换工具」升级为「AI 演奏设计助手」。
> 定位照 spec 走：**AI 不生成音乐**，AI 负责理解已有音乐、优化演奏方案。
> 本批交付：演奏策略（初学 / 标准 / 展示）+ 逐条解释 + 助手服务（模型优先、规则兜底、结果一律过校验）+ 聊天式面板。

---

## 一、这一批做了什么

```
用户一句自然语言
      ↓
MusicAssistantService            ← 模型（有 key 时）或规则判断「这是初学还是展示」
      ↓ 白名单校验（认不出的难度档直接丢，不执行）
PerformanceStrategy              ← BEGINNER / NORMAL / SHOWCASE
      ↓
StrategyApplier                  ← 确定性规则改写：降速、精简、收窄跨度 / 加强重拍与呼吸
      ↓
调整后的曲目 + 逐条原因（AI Explain）
      ↓
点「应用方案」→ 落成一条新曲目（原曲不动，重复应用幂等）
```

## 二、三种演奏策略

| 策略 | 改什么 | 不改什么 |
| --- | --- | --- |
| **初学 Beginner** | 同一时刻挤在一起的音只留一个；过短的经过音合并进前一个；离中心音超过 7 个半音的音移八度收窄；速度按 80% 降（不低于 72 BPM，原速 ≤ 90 时不降） | 音名（移八度不改音名）、旋律顺序 |
| **标准 Normal** | 什么都不改 | 全部原样 |
| **展示 Showcase** | 每小节第一个音力度 ×1.25；长于一拍的音时值 ×1.08（乐句呼吸） | **音高序列与音符数量一个不动**（有单测钉住） |

每条调整都带 `title / detail / before / after`，界面直接当「AI 解释」展示，例如：

> **降低速度**：96 BPM → 77 BPM
> 原速 96 BPM 对初学偏快，按 80% 降到 77 BPM（不低于 72 BPM，再慢会失去乐句感）；音符时值同比拉长，听觉上的节奏比例不变。

## 三、模型边界（与动效助手同一套做法）

| 项 | 做法 |
| --- | --- |
| 模型负责 | 把一句话翻译成 `{difficulty, instrument, suggestion[]}` |
| 模型不负责 | 不写音符、不改时序、不生成音频 —— 改写永远由规则完成 |
| 后端校验 | 难度档必须在白名单（BEGINNER/NORMAL/SHOWCASE/别名）里；认不出来就**丢弃并用规则判断**，同时把原因写进 `fallbackReason` |
| 建议清单 | 最多 5 条、每条最多 60 字，去重截断后才返回 |
| 模型不可用 | 未配置 / 超时 / 非法 JSON / 没有可用字段 → 四种情况全部回退规则，助手照常可用 |
| 环境变量 | `MUSIC_LLM_BASE_URL` / `MUSIC_LLM_API_KEY` / `MUSIC_LLM_MODEL`（兼容 OpenAI `/chat/completions`） |

## 四、接口与界面

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/music/tasks/{id}/assistant/analyze` | 理解一句要求，返回方案 + 建议 + 逐条解释 + 前后对比 |
| POST | `/api/music/tasks/{id}/assistant/apply` | 应用方案，落成一条新曲目（幂等） |

前端：音乐 Agent 右侧新增「AI 音乐助手」面板 —— 聊天式气泡、三个快捷说法、回答里显示
方案徽章（模型分析 / 规则判断）、改动清单与逐条原因、前后对比数字、「应用方案」按钮（生成新版本曲目）。

**幂等**：同一个源任务 + 同一套方案只生成一个版本（`同名 + 同源任务` 做键），不会因为多点了两次就堆出好几条同名曲目。

## 五、验收

| 项 | 结果 |
| --- | --- |
| 后端测试 | **169 个用例全绿**（本批新增 30 个：策略 11 + 模型层 7 + 助手服务 12） |
| 覆盖点 | 三句 spec 原话的规则识别、模型合法/非法/失败三条路径、建议截断、策略的每条规则、空曲子、策略白名单、应用幂等与拒绝路径 |
| 前端类型检查 / 构建 | `vue-tsc --noEmit` 与 `npm run build` 均通过 |
| 接口实跑 | 「简单一点」→ 初学模式 96→77 BPM（时长 10.0→12.5 秒）；「游戏展示」→ 展示模式（音符 14→14）；「15 键口风琴」→ 识别到乐器；标准模式 apply → 明确拒绝 |
| 浏览器实测 | 面板渲染 + 点快捷说法得到回答（含来源徽章、改动 96→77、逐条原因、前后对比）→ 点应用 → 新曲目出现在左侧曲库；控制台无报错 |
| 库内状态 | 曲库多出「小星星（MIDI 文件）（初学模式）」（77 BPM）与「（展示模式）」两条派生版本，原曲三条不动 |

## 六、文件清单

**新增（后端 8 个）**
```
strategy/PerformanceStrategy.java        三档策略 + 白名单解析
strategy/StrategyApplier.java            规则改写 + 逐条解释
assistant/MusicModelClient.java          OpenAI 兼容调用（解析 + 兜底）
service/MusicAssistantService.java       理解 → 校验 → 应用
controller/MusicAssistantController.java analyze / apply 两个接口
dto/MusicPromptDTO.java、dto/StrategyApplyDTO.java
vo/MusicAssistantVO.java
```
**修改（后端 1 个）**：`MusicTaskService.java` 只补两个对外方法（`songOf` 读曲子、`createDerived` 落派生曲目），既有解析与映射一行未动。
**新增（前端 3 个）**：`types/assistant.ts`、`api/assistant.ts`、`components/MusicAssistantPanel.vue`；`MusicAgentView.vue` 加一个面板挂载点与刷新钩子。

## 七、下一批要做的（spec 里已列、本批未做）

1. **§5 智能映射（AI 辅助映射）**：现在超范围只有一条统一策略，spec 要的是「逐音给出方案 1 八度下降 / 方案 2 替换附近音 / 方案 3 跳过 + 原因」。
   既有的 `InstrumentMappingService` 已经有三种策略与未落键原因，需要加的是**逐音级的方案选择与解释**。
2. **模型联通实测**：本轮验证全部在规则模式（没有你的 key）。配好 `MUSIC_LLM_*` 后需要一次真实调用的联通测试。
3. **演奏记录与分享页**（Phase 4 里的「演奏记录」）：曲目 / BPM / 音符数 / 键位 / 难度 + 只读分享页。
