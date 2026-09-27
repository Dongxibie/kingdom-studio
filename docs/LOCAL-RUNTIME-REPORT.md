# Music Agent · 本机演奏运行时变更报告（Local Performance Runtime）

> 目标：让 Kingdom Studio 从「演奏计划生成器」升级为「本地演奏代理」——
> 生成一份计划，静帧预览确认后，在**用户主动开启**的前提下于本机把这份计划弹出来。
> 架构要求照做：`PerformancePlan → ExecutionService → ExecutionAdapter → 具体实现`，
> 没有把系统输入逻辑写进 MusicService。

---

## 一、结构

```
com.kingdomstudio.modules.music
├── execution/                     ← 本阶段新增（执行层，一眼能看全）
│   ├── ExecutionAdapter           执行通道抽象（PREVIEW / MANUAL / LOCAL）
│   ├── PreviewExecutionAdapter    仅模拟：列命令，不发输入
│   ├── ManualExecutionAdapter     逐条确认：未实现，如实说明
│   ├── LocalExecutionAdapter      本机演奏：指向有状态会话
│   ├── ExecutionSession           会话：状态机 + 执行线程 + 随时可停
│   ├── ExecutionStatus            状态枚举 READY/RUNNING/PAUSED/STOPPED/FINISHED
│   ├── KeyInjector                按键注入通道（Robot 实现 / 记录实现）
│   └── WindowGuard                前台窗口 + ESC 急停通道
├── service/ExecutionService       四道闸门与会话生命周期
└── service/PerformanceMacroService 既有的导出层（只改了引用，逻辑未动）
tools/ks-performance-guard.ps1     只读守门脚本（读前台窗口 + 查 ESC）
```

## 二、四道闸门（本机执行的开启条件）

| # | 闸门 | 落点 |
| --- | --- | --- |
| 1 | 配置开关，默认关闭 | `kingdom.music.local-execution.enabled`（`MUSIC_LOCAL_EXECUTION_ENABLED=true`） |
| 2 | 请求必须显式确认 | `POST /runtime/start` 的 `confirm=true`，缺失一律拒绝 |
| 3 | 目标窗口必须已在前台 | 默认取当前前台窗口并要求确认；传了别的窗口且不是前台 → 拒绝启动 |
| 4 | 真实注入必须有窗口守护 | 守门脚本起不来时：真实模式拒绝启动（宁可不弹，也不要在看不见窗口时乱发按键） |

另外还有一条自检期就暴露的：**一次只允许一个会话**（两个会话同时发按键，急停说不清该松哪个）。

## 三、安全行为（每一条都有单测钉住）

| 行为 | 实现方式 |
| --- | --- |
| ESC 随时急停 | 守门脚本查 ESC → 上报 → `ExecutionService.emergencyStop` → 走**与手动停止同一条** `stop()` 路径 |
| 停止即释放 | `stop()` 立刻置位并 `releaseAll(heldKeys)`；执行线程 finally 里再释放一次（幂等） |
| 暂停即释放 | `pause()` 同步把状态改成「已暂停」并松开按住的键，不等工作线程走到检查点 |
| 只对确认过的窗口生效 | 每条命令前比对前台窗口标题，不一致 → **自动暂停** 并写明原因，切回去可继续 |
| 异常也释放 | 注入过程中抛异常 → 状态「已停止」+ 警告 + 释放按住的键（finally） |
| 状态全程可见 | 进度 / 当前按键 / 剩余时间 / 按住的键 / 目标窗口 / 此刻前台窗口 / 守护是否在线 |
| 不做的事 | 不识别游戏、不注入进程、不绕过检测、不后台隐藏执行（只发按键，连鼠标都不动） |

守护脚本 `tools/ks-performance-guard.ps1` 是**只读**的：只查 ESC 一个虚拟键、只读前台窗口标题，
输出只有 `READY` / `WINDOW <标题>` / `ESC` 三类行，不写文件、不联网，随仓库提交可自行核对。

## 四、接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/music/tasks/{id}/performance-plan/runtime/preflight` | 环境自检：开关 / 此刻前台窗口 / 注入方式 / 必须确认的条件 |
| POST | `/api/music/tasks/{id}/performance-plan/runtime/start` | 开始（需 `confirm=true` 与目标窗口） |
| GET | `/api/music/tasks/{id}/performance-plan/runtime/status` | 状态 / 进度 / 当前按键 / 剩余时间 / 按住的键 |
| POST | `/api/music/tasks/{id}/performance-plan/runtime/pause` | 暂停（立刻松键） |
| POST | `/api/music/tasks/{id}/performance-plan/runtime/resume` | 继续 |
| POST | `/api/music/tasks/{id}/performance-plan/runtime/stop` | 停止（等同急停） |

前端：音乐 Agent 右侧新增「演奏控制（本机演奏）」面板 —— 三条 ⚠ 提示、环境自检、
「我知道这会在本机发送按键」确认框、开始/暂停/继续/停止（急停）按钮、以及执行监视器。

## 五、验收

| 项 | 结果 |
| --- | --- |
| 后端测试 | **140 个用例全绿**（本阶段新增 16 个：`ExecutionSessionTest` 8 + `ExecutionServiceTest` 8） |
| 覆盖的用例 | 计划执行顺序、暂停恢复、停止释放、异常退出、空计划、ESC 急停、窗口切换自动暂停、四道闸门的拒绝路径 |
| 前端类型检查 / 构建 | `vue-tsc --noEmit` 与 `npm run build` 均通过 |
| 守门脚本 | 单独试跑：正确读出前台窗口标题并持续上报（只读，无副作用） |
| 接口实跑（记录模式） | 自检 → 开始（28 条命令）→ 状态轮询（进度 7%→39%、当前键、剩余时间、按住的键）→ 暂停（进度冻住、按键清空）→ 继续 → 停止（原因与空按键列表） |
| 浏览器实测 | 面板渲染警告与自检；未勾确认时「开始」禁用，勾选后开始；监视器进度从 18% 走到 39%；停止后状态「已停止」、按住的键「无」；控制台无报错 |

**没有实测的一件事**：真实的 `java.awt.Robot` 注入路径没有在你机器上跑过 ——
我不想在你不看着的时候往桌面发按键。验证用的是记录模式（`injector=record`，一个键都不发）。
真实模式怎么试写在下面第六节，30 秒就能确认。

## 六、怎么开真实本机演奏

```bash
# 1) 打开开关（默认关闭）
cd backend
DB_PASSWORD=root MUSIC_LOCAL_EXECUTION_ENABLED=true mvn spring-boot:run
#    或用 jar：java -DDB_PASSWORD=root -Dkingdom.music.local-execution.enabled=true -jar ks.jar

# 2) 页面：音乐 Agent → 选曲目 → 生成按键序列 → 演奏控制面板 → 勾选确认 → 开始本地演奏
#    开始前把目标窗口（游戏/乐器窗口）切到前台：面板上的「当前前台窗口」就是它

# 3) ESC 随时急停；面板上的「停止（急停）」等效
```

可选参数：`MUSIC_LOCAL_EXECUTION_INJECTOR=record`（记录模式，自检用）、
`kingdom.music.local-execution.guard-script=<绝对路径>`（守门脚本不在默认位置时指定）、
`MUSIC_LOCAL_EXECUTION_GUARD=none`（关掉窗口判断，此时真实模式不允许启动）。

## 七、已知边界

1. **逐条确认（MANUAL）未实现**：三种模式里最小的一种，当前只提供「仅模拟」与「本机演奏」；
   界面上会说明，不会静默失败。
2. **按键间隔**：真实注入时每条命令之间留 12ms（`KeyInjector.keyDelayMs`），
   目标程序反应慢的话可以把间隔调大——这是刻意留的余量，不是精度极限。
3. **只支持单键**：与桌面代理协议一致，多字符键（如 `F5`）在命令流阶段就被跳过并提示。
4. **窗口匹配按标题**：标题完全一致才算同一个窗口；同名窗口（例如两个同名浏览器标签）区分不了，
   这类情况建议把目标窗口标题改得可辨识一些。
