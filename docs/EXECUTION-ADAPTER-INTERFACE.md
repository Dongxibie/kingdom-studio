# 执行层接口（Execution Adapter）· V1.2 说明

> 这一版**不实现真实输入**，只把接口与安全边界定下来。
> 本文说明它与需求里那份 `PerformanceExecutor` 接口的对应关系 —— 已有的抽象不需要推翻，
> 它已经覆盖了计划里的每一个动作，只是命名不同。

---

## 一、需求里的接口 vs 现有实现

需求提出的接口：

```ts
interface PerformanceExecutor {
  execute(plan)
  pause()
  stop()
  status()
}
```

现有实现把它拆成了**两层**，因为「一次派发」与「一段正在进行的演奏」在语义上并不是一回事：

| 需求里的方法 | 现有对应 | 位置 |
| --- | --- | --- |
| `execute(plan)` | `ExecutionAdapter.execute(PlanContext)` | `music/execution/ExecutionAdapter.java` |
| `pause()` | `ExecutionService.pause(taskId)` → `ExecutionSession.pause(reason)` | `music/service/ExecutionService.java`、`music/execution/ExecutionSession.java` |
| `stop()` | `ExecutionService.stop(taskId)` / `emergencyStop(taskId)` | 同上 |
| `status()` | `ExecutionService.snapshot(taskId)` → `ExecutionStatusVO` | 同上 |
| —（不是一次调用能表达的） | `ExecutionService.start(taskId, request)` → 建立会话 | 同上 |

这么分的理由：`execute(plan)` 是**无状态**的（给一份计划，执行或模拟一次，返回结果）；
而 `pause / stop / status` 只有在**存在一个正在跑的会话**时才有意义。
把两者塞进同一个接口，会让「没有会话时调用 pause」变成一种必须先约定好语义的灰色地带。
现在的拆法是：无状态的走适配器，有状态的走会话。

三种适配器（`ExecutionAdapter#mode()` 是它们的标识）：

| 模式 | 实现类 | 行为 |
| --- | --- | --- |
| `PREVIEW` | `PreviewExecutionAdapter` | **只模拟**：把命令流列出来，准备 → 执行 → 完成，不产生任何输入 |
| `MANUAL` | `ManualExecutionAdapter` | 逐条确认；依赖本机执行通道，当前默认关闭 |
| `LOCAL` | `LocalExecutionAdapter` | 本机演奏；有状态会话（开始 / 暂停 / 继续 / 停止 / 状态），默认关闭 |

对应需求里的 `PreviewExecutor`（模拟准备 → 执行 → 完成）就是 `PREVIEW` 模式，已有实现与测试。

---

## 二、安全边界（都是已实现的约束，不是待办）

| 要求 | 实现方式 |
| --- | --- |
| 禁止后台隐藏执行 | 本机演奏默认关闭（`kingdom.music.local-execution.enabled=false`），必须显式开启；执行必须由用户点击「开始」触发 |
| 禁止自动注入未授权输入 | 开始前必须带 `confirm=true`，并要求目标窗口等于当前前台窗口；两者任一不满足直接拒绝 |
| 无提示运行 → 必须显示状态 | 执行监视器显示 曲目 / 进度 / 当前按键 / 剩余时间 / 状态（READY / RUNNING / PAUSED / STOPPED / FINISHED） |
| 支持停止 | `stop()` 与 `emergencyStop()` 同一条路径：停止 + 清空队列 + 释放按住的键 |
| 急停 | PowerShell 只读守门脚本监听 ESC（`tools/ks-performance-guard.ps1`，只读前台窗口标题与 ESC 状态，不记录按键、不写文件、不联网） |

**本阶段没有新增任何真实输入能力**：`PREVIEW` 之外的两种模式在本机默认不可用，
界面上会说明原因而不是降级执行。

---

## 三、未来接 LocalExecutor 时要补的三件事

1. **按键注入通道**：现有 `RobotInjector` 只在开启本机演奏后使用，未来若接入游戏内乐器，
   需要按游戏补充「目标窗口判定」与「按键间隔下限」；
2. **执行回执**：目前会话只回报自己发出的动作，没有来自游戏的确认；
   接入后可以在 `ExecutionStatusVO` 上增加 `confirmedCount` 之类的字段；
3. **录制与回放**：把一次真实演奏记录成可复现的按键流，用于回归验证。

这三件事都属于 V2.0（Local Execution Adapter）的范围，本版不做。
