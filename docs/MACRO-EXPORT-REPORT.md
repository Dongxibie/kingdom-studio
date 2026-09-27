# Music Agent · 演奏宏导出层变更报告（Performance Macro Export）

> 目标：让用户上传音乐后，能**带走一份可用于本机自动演奏测试的按键执行脚本**。
> 做法：在既有链路末端加两层（Macro Export + Local Execution Adapter 的边界），
> 解析 / 音符时间线 / 键位映射 / 时序展示 / 回放**一处未动**。

---

## 一、新增的两层

```
MIDI / 简谱 → Music Parser → Note Timeline → Instrument Mapping → Key Sequence   （既有，未改）
                                                                    ↓
                                                        演奏计划 PerformancePlan   ← 新增
                                                                    ↓
                                            宏导出 TXT / AutoHotkey / JSON        ← 新增
                                                                    ↓
                                        Local Execution Adapter（仅模拟 · 真实通道未开启）← 新增边界
```

| 层 | 做了什么 | 没做什么 |
| --- | --- | --- |
| 演奏计划 | 把按键序列编译成「按下 / 松开 + 相对时间」的事件流并落库，三种导出与执行共用这一份 | 不重算音高、不改策略、不动音符表 |
| 宏导出 | 同一份事件流导出 TXT 时间线 / AutoHotkey 脚本 / JSON 计划 | 不生成代码以外的任何副作用 |
| 执行适配层 | 抽象出 `ExecutionAdapter`，把「谁去发按键」隔离成一层；本轮只有**仅模拟**可用 | **不做任何真实按键注入** |

## 二、三种导出

以「小星星（MIDI 文件）· 光遇式 15 键」实跑为例：14 个音、28 条事件、6 个键、时长 10.0 秒。

**① TXT 按键时间线**（给人看）

```
   0.00s  按下 Z
   0.63s  松开 Z
   0.69s  按下 Z
   1.25s  按下 B
```

**② AutoHotkey 脚本**（给机器执行，v1.1）

```ahk
F9::
    Send, {Z down}
    Wait(625)
    Send, {Z up}   ; 按住 625ms
    Wait(60)
    Send, {Z down}
    ...

F8::paused := !paused          ; 暂停 / 继续
Esc::                          ; 急停：松开所有键并退出
    running := false
    Sleep, 40
    for index, key in keys
        Send, {%key% up}
    ExitApp
return
```

- 支持**按下 / 释放 / 持续时间 / 延迟**：按下与松开之间是按住时长，事件之间是间隔，都由事件流算出来；
- 支持**暂停**：`Wait()` 与 `Sleep` 的区别是每 20ms 检查一次状态，所以暂停期间按 ESC 依然有效；
- **不自动开始**：主流程写在 `F9::` 标签下面，脚本跑起来只会待命；
- 头部注释里带曲目、乐器、键数、时长、校验提示，以及「只对某个窗口生效」的 `#IfWinActive` 写法。

**③ JSON 演奏计划**（给程序读）

```json
{
  "taskId": 3, "profileId": 1, "duration": 10000,
  "planVersion": "1.0",
  "notes": [
    { "key": "Z", "action": "DOWN", "timestamp": 0, "strokeSeq": 1 },
    { "key": "Z", "action": "UP", "timestamp": 625, "strokeSeq": 1 }
  ],
  "warnings": ["..."],
  "safety": ["急停随时可用：按 ESC 立即松开所有已按下的键并退出", "..."]
}
```

## 三、演奏计划模型

新增表 `performance_plan`（既有三张音乐表一个字段没动）：

| 字段 | 说明 |
| --- | --- |
| `id` / `task_id` / `profile_id` | 主键与来源（音乐任务 + 乐器档案） |
| `task_name` / `profile_name` / `strategy` | 冗余存一份，列表展示不必联表 |
| `duration` | 整首时长（ms） |
| `note_count` / `stroke_count` / `key_count` | 事件数 / 按键次数 / 用到的不同键数 |
| `warnings` | 命令流校验提示，逐条留痕 |
| `notes` | 事件流 JSON（按下 / 松开 + 时间 + 序号） |
| `create_time` / `update_time` | 时间戳 |

**时序只有一处定义**：事件流来自 `DesktopAgentService.plan()` 已经校验过的命令流 ——
最短 / 最长按住截断、同键未松开不得再按（顺延到最小间隔之后）、单键校验都在那一层，
本层不另写一套，所以**导出脚本的节奏与桌面代理的派发计划永远一致**。
实跑时那 6 条「已顺延到 xxx」的提示就是这么来的（相邻同音正好首尾相接，被顺延 60ms）。

## 四、执行层：边界与安全

| 模式 | 当前状态 | 说明 |
| --- | --- | --- |
| `PREVIEW` 仅模拟 | **可用** | 按时间列出全部命令（含每条按住时长），明确返回「本次不会产生任何真实按键输入」 |
| `MANUAL` 逐条确认 | 未开启 | 依赖本机执行通道，界面点它会给出一段说明而不是静默失败 |
| `LOCAL` 本机执行 | 未开启 | 需要先具备「随时可用的急停」「只对前台窗口生效」「显式开启开关」三条 |

抽象成 `ExecutionAdapter` 接口的三个好处：单测注入假实现就能覆盖时序判断；界面能如实说"没执行"；
将来接真实输入只换这一层实现，导出与计划模型都不用动。

## 五、接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/music/tasks/{id}/performance-plan` | 生成（或重新生成）演奏计划 |
| GET | `/api/music/tasks/{id}/performance-plan` | 最近一次计划（事件流可能截断，完整内容走导出） |
| GET | `/api/music/tasks/{id}/performance-plan/export?format=AHK\|TXT\|JSON` | 导出脚本 |
| POST | `/api/music/tasks/{id}/performance-plan/execute` | 执行（默认仅模拟） |
| GET | `/api/music/tasks/{id}/performance-plan/modes` | 三种模式是否可用、不可用的原因 |

## 六、验收

| 项 | 结果 |
| --- | --- |
| 后端编译 | BUILD SUCCESS |
| 后端测试 | **124 个用例全绿**（本层新增 18 个：`MacroScriptGeneratorTest` 9 + `PerformanceMacroServiceTest` 9） |
| 前端类型检查 / 构建 | `vue-tsc --noEmit` 与 `npm run build` 均通过 |
| 真实数据实跑 | 用「小星星（MIDI 文件）」生成计划：28 条事件 / 6 个键 / 10.0 秒；三种格式导出内容逐一核对；`PREVIEW` 返回 28 条命令；`LOCAL` 如实拒绝 |
| 浏览器实测 | 曲库选项 → 按键序列面板出现「导出演奏脚本」→ 弹窗生成计划（含 6 条校验提示）→ 切换三种格式 → 仅模拟执行列出前 40 条命令 → 点未开启模式弹出说明；控制台无报错 |

## 七、文件清单

**新增（后端 9 个）**
```
entity/PerformancePlan.java          mapper/PerformancePlanMapper.java
macro/MacroScriptGenerator.java      宏脚本生成器（三种格式）
macro/ExecutionAdapter.java          执行通道抽象
macro/ExecutionAdapters.java         三种模式实现（仅模拟可用）
dto/ExecutionRequestDTO.java
vo/PerformancePlanVO / MacroExportVO / ExecutionResultVO.java
service/PerformanceMacroService.java controller/PerformanceMacroController.java
db/extensions_music_macro.sql        performance_plan 建表（可重复执行）
```
**新增（前端 3 个）**：`types/macro.ts`、`api/macro.ts`、`components/MacroExportPanel.vue`
**修改（1 个）**：`components/KeySequencePanel.vue`（加「导出演奏脚本」入口，其余不动）

## 八、已知边界

1. **本机执行通道未开启**：`LOCAL` / `MANUAL` 只到「如实拒绝」这一步。真实执行需要先定下急停方式、
   前台窗口限制与开启开关（用户给出的规格第五节缺失，未实现）。
2. **AutoHotkey 只支持 v1.1**：脚本用了 `#NoEnv`、`%key%` 这类 v1 语法，装 v2 不能直接跑，头部注释里已写明。
3. **相邻同音会被顺延 60ms**：这是桌面代理的既有规则（同一个键没松开不能再按），导出里如实列出提示，
   不是精度问题而是物理约束。
4. **按键字符必须是单键**：多字符键（如 `F5`）在协议语义之外，会在命令流阶段被跳过并给出提示。
