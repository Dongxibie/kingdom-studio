# Motion Lab 2.0 · Phase 1 变更报告（Motion 资源扩展）

> 目标：把「30 个官方模板」扩成 **Official Collection + Community Collection** 的双集合资源库，
> 并把「GitHub 发现 → 规则分析 → 人工筛选 → 转成 Motion Pattern」做成一条真实可走的流水线。
> 本阶段**只新增**：候选池表、三个来源字段、30 个社区模板、一条流水线与界面入口；既有 30 个官方模板的功能一处没减。

---

## 一、这一阶段交付了什么

| # | 交付物 | 说明 |
| --- | --- | --- |
| 1 | **七分类体系** | 文字动画 / 卡片交互 / 按钮交互 / 滚动动画 / 首屏动画 / 背景效果 / 三维 WebGL；原有 30 个模板一并迁移过来 |
| 2 | **Official Collection（30）** | 既有官方模板，分类迁移 + 补上「触发方式」 |
| 3 | **Community Collection（30）** | 全部为本项目原创实现的 Motion Pattern，带灵感来源地址与来源许可标注 |
| 4 | **候选池 `motion_candidate`（118 条）** | 真实 GitHub 仓库元数据：名称 / 地址 / 说明 / 语言 / 星数 / 许可 / 主题 + 分析结论 + 处置状态 |
| 5 | **发现脚本 `_gh/mc_discover.py`** | 调 GitHub 搜索 API，19 个查询，带限流纪律与增量发现 |
| 6 | **规则分析器** | 分类 / 触发 / 难度 / 运行档位 / 视觉分，全部可解释、可复现，无模型依赖 |
| 7 | **人工筛选 + 转换接口** | 状态机（待看 / 已分析 / 已选入 / 已入库 / 已淘汰）、淘汰必须写理由、转换必须指定承载 Pattern |
| 8 | **候选池界面** | 概览、筛选、逐条分析依据、一键筛选与转换；工作台卡片与详情页显示「官方 / 社区」与来源标注 |

---

## 二、两条刻意的边界

**一、候选池只存元数据，不存源码。**
`motion_candidate` 里没有任何一行第三方代码：只有仓库名、地址、说明、语言、星数、许可与主题。
GitHub 在这个项目里只承担「发现案例、学习做法」的角色。

**二、进资源库的代码必须是我们自己的实现。**
转换接口 `POST /motion/candidates/{id}/promote` 强制要求指定 `patternTemplateKey`，
它把候选的名字、来源、许可贴到一个**现有的内置 Pattern** 上；模板的 CSS / Vue / React / Three.js / 预览结构与参数**全部来自 Pattern**。
所以社区集合里 30 个模板的实现都是本项目原创，`source_url` + `source_license` 只用于标注灵感出处。

---

## 三、数据

| 项 | 数量 | 明细 |
| --- | --- | --- |
| 模板总数 | **60** | OFFICIAL 30 + COMMUNITY 30 |
| 七分类分布 | 60 | 卡片交互 12 / 背景效果 11 / 首屏动画 10 / 文字动画 8 / 按钮交互 7 / 滚动动画 7 / 三维 WebGL 5 |
| 触发方式分布 | 60 | 加载时 38 / 悬停 12 / 滚动 6 / 点击 4 |
| 候选池 | **118** | 已入库 30 / 已分析 50 / 已淘汰 38 |
| 发现查询 | 19 | 每个查询带星数下限，命中限流即等窗口或停止 |
| 来源许可分布 | 118 | MIT 51 / 未标注 16 / Apache-2.0 10 / NOASSERTION 6 / GPL-3.0 3 / BSD-2 与 BSD-3 各 2 … |

**候选池的处置是真实的**：38 条被自动初审淘汰，理由逐条写进 `review_note`（语言不对 / 清单类仓库 / 分类未命中 / 星数过低），
50 条留作人工筛选，30 条已转为社区模板。

**种子可重复执行且不覆盖人的决定**：候选的 upsert 只刷新「发现到的事实」（名称、说明、星数、许可、主题），
不动 `status` / `review_note` / `pattern_key` / `promoted_template_key` —— 重跑种子不会把人工筛选的结果冲掉（已实测）。

---

## 四、验收

| 项 | 结果 |
| --- | --- |
| 后端编译 | `mvn -o -DskipTests package` BUILD SUCCESS |
| 后端测试 | **106 个用例全绿**（0 失败 / 0 错误），其中本阶段新增 16 个：`MotionCandidateAnalyzerTest` 8 + `MotionCandidateServiceTest` 8 |
| 前端类型检查 | `vue-tsc --noEmit` 通过 |
| 前端构建 | `npm run build` 通过 |
| 数据校验 | 建表脚本 + 两个种子在库上重放通过；60 / 118 的数量与状态分布与报告一致 |
| 浏览器实测 | ① 新手引导 → 选场景 → 推荐模板与组合方案；②「按来源」筛出社区精选并显示来源地址与许可；③ 卡片同时出现「官方 / 社区」标识（首屏 6 : 6）；④ 候选池筛选 / 分析依据 / 选入 / 转换全流程走通，且转换后右侧停留在同一条候选上 |
| 预览冒烟 | **30 / 30 社区模板渲染正常**：节点数、根尺寸、动画数、Canvas 数逐一核对，无 JS 报错 |

浏览器实测中修掉的两个问题：评审后右侧面板被列表刷新顶掉（改为保留当前候选），以及 `Map.of()` 不接受 null 键导致的 NPE。

---

## 五、文件清单

**新增（后端 9 个）**
```
entity/MotionCandidate.java          mapper/MotionCandidateMapper.java
service/MotionCandidateAnalyzer.java service/MotionCandidateService.java
controller/MotionCandidateController.java
dto/CandidateQueryDTO / CandidateReviewDTO / CandidatePromoteDTO.java
vo/MotionCandidateVO / CandidateStatsVO.java
```

**修改（后端 6 个）**
```
entity/MotionTemplate.java（+ trigger_type / source_url / source_license）
dto/TemplateQueryDTO.java（+ source / trigger）
vo/MotionFacetVO.java（+ sources / triggers）
vo/MotionTemplateItemVO.java、MotionTemplateDetailVO.java（+ 来源与触发字段）
service/MotionTemplateService.java（筛选、分面、来源与触发映射、null 键兜底）
```

**新增（前端 2 个 + 路由）**
```
types/candidate.ts、views/MotionCandidateView.vue、router/index.ts（/extensions/motion-lab/candidates）
```

**修改（前端 5 个）**
```
api/template.ts（候选池 5 个接口）、types/workbench.ts（来源 / 触发 / 分面）、
components/MotionDiscoverPanel.vue（+ 按来源 / 按触发方式）、
components/MotionTemplateCards.vue（官方 / 社区标识）、
views/MotionLabView.vue（候选池入口 + 来源标注）、styles/workbench.css
```

**SQL 与生成器**
```
db/extensions_motion_community.sql        结构：motion_candidate 建表 + 模板补三列 + source 放开 COMMUNITY
db/extensions_motion_community_seed.sql   生成物：30 个社区模板 + 118 条候选
_gh/mc_discover.py                        发现脚本（19 个查询、限流纪律、增量复用）
_gh/mc_build.py + mc_data1-3.py           社区模板与候选池的生成器（30 个 Pattern 的原创实现数据）
```

---

## 六、已知的取舍

1. **候选池 118 条**：spec 的目标是「至少 100 候选、60 入库」。当前 118 候选满足下限；「60 入库」由官方 30 + 社区 30 达成
   （社区集合目前是 30 条，若要做到「社区 60」需要再产 30 个 Pattern，属于下一轮工作量）。
2. **分析用的是规则不是模型**：分类、触发、难度、档位都是确定性的关键词规则，可复现可解释；模型留在动效助手里做语义理解。
3. **转换后的人工筛选结果不会回写种子**：种子只负责发现事实，人做的判断留在库里（已在 upsert 里显式排除）。
4. **JS 驱动的 Pattern 在预览里不受播放控制**：暂停 / 变速走的是 Web Animations API，Canvas 与指针驱动的效果不受影响，
   与既有官方模板（粒子星网、三维场景）行为一致。
