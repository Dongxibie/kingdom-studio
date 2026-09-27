# Kingdom Studio

> 一个属于开发者自己的数字王国。

个人开发者工作台：管理**我的项目**、**我的技术资产**、**我的成长路线**和**我的代码知识库**。
视觉概念是「个人开发者 = 王国建设者」，后续可结合 Husky King（哈士奇国王）个人 IP 做视觉延展。

这是一个**个人作品项目**，用于展示 Java 后端、Web 开发、数据库设计与工程能力，不是企业级商业系统。

当前版本 **v1.2.0**：主站骨架 + 两个扩展模块（动效基因库 / 音乐 Agent，见下方「扩展模块」）。

---

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 21、Spring Boot 3.3.5、MyBatis-Plus 3.5.9、Spring Security + JWT、Spring Cache |
| 数据库 | MySQL 8.0.16+（utf8mb4 / InnoDB，扩展表使用 CHECK 约束） |
| 缓存 | Redis（采集结果缓存 + 健康检查） |
| 构建 | Maven 3.9、Vite 6 |
| 前端 | Vue 3、TypeScript、Element Plus、Axios、Vue Router |
| 文档 | SpringDoc OpenAPI（Swagger UI） |

## 目录结构

```
kingdom-studio/
├── backend/                     # Spring Boot 后端
│   └── src/main/
│       ├── java/com/kingdomstudio/
│       │   ├── KingdomStudioApplication.java
│       │   ├── common/          # 统一响应体、分页外壳、状态码、异常体系
│       │   ├── config/          # MyBatis-Plus / Redis / 跨域 / Security / OpenAPI
│       │   ├── controller/      # 只做参数接收与结果返回，不写业务逻辑
│       │   ├── handler/         # 公共字段自动填充
│       │   ├── modules/         # 扩展模块：motion（动效）、music（音乐）、desktop（桌面代理）
│       │   └── vo/              # 返回给前端的视图对象
│       └── resources/application.yml
├── frontend/                    # Vue 3 前端
│   └── src/
│       ├── api/                 # Axios 封装（统一响应处理）
│       ├── extensions/          # 扩展模块页面：motion-lab、music-agent、_shared 公共外壳
│       ├── layout/              # 侧边栏 + 顶栏骨架
│       ├── router/              # 路由表
│       └── views/               # 主站页面
├── db/
│   ├── kingdom_studio.sql               # 主站建表 + 种子数据（会重建这 5 张表）
│   ├── extensions_motion.sql            # 扩展：动效资源表 + 代码表
│   ├── extensions_motion_seed.sql       # 扩展：10 条动效示例（含源码，幂等）
│   └── extensions_music.sql             # 扩展：音乐任务 / 音符 / 乐器档案（含 3 套乐器）
├── demo/                        # 演示曲目（scale.mid、twinkle.mid）
└── docs/                        # 阶段记录、完成报告、桌面代理协议、动效示例源码
```

## 快速开始

**前置条件**：JDK 21、Maven 3.9+、Node 18+、MySQL 8.0.16+、Redis。

```bash
# 1. 初始化数据库（顺序不能变：扩展表建在主库之后）
mysql -uroot -p < db/kingdom_studio.sql                          # 主站 5 张表（含种子数据）
mysql -uroot -p kingdom_studio < db/extensions_motion.sql        # 动效资源表
mysql -uroot -p kingdom_studio < db/extensions_motion_seed.sql   # 动效示例（10 条，幂等可重跑）
mysql -uroot -p kingdom_studio < db/extensions_music.sql         # 音乐表 + 3 套乐器档案

# 2. 启动后端（端口 8080，接口前缀 /api）
cd backend
export DB_PASSWORD=你的数据库密码          # Windows PowerShell: $env:DB_PASSWORD="你的数据库密码"
mvn spring-boot:run

# 3. 启动前端（端口 5173，/api 自动代理到后端）
cd ../frontend
npm install
npm run dev
```

打开 <http://localhost:5173> 即可看到工作台；后端接口文档在 <http://localhost:8080/api/swagger-ui.html>。

### 环境变量

仓库里**不留可用的默认密码与密钥**（`DB_PASSWORD`、`JWT_SECRET` 都没有默认值），
本机演示按需提供即可；完整清单与环境变量示例见 [`.env.example`](.env.example)。

| 变量 | 必填 | 默认值 | 说明 |
|---|---|---|---|
| `DB_PASSWORD` | ✅ | 无 | 数据库密码，本地演示通常就是安装 MySQL 时设的那个 |
| `DB_USERNAME` | — | `root` | 数据库账号 |
| `DB_URL` | — | `jdbc:mysql://127.0.0.1:3306/kingdom_studio?...` | 完整连接串 |
| `JWT_SECRET` | ✅（接入鉴权后） | 无 | HS256 至少 32 字节；当前版本接口未启用鉴权，仅本机演示 |
| `REDIS_HOST` / `REDIS_PORT` | — | `127.0.0.1` / `6379` | Redis 连接 |
| `CORS_ORIGINS` | — | `http://localhost:5173,http://127.0.0.1:5173` | 允许的前端来源，逗号分隔 |
| `GITHUB_TOKEN` | — | 空 | 可选。不设也能采集，只是限额较低（未认证 10 次/分钟） |

### 关于本地演示与安全边界

这个版本是**本机演示用**：接口未启用鉴权（`SecurityConfig` 目前全部放行），请勿直接部署到公网。
部署前至少要补三件事：开启 `anyRequest().authenticated()` 并接入 JWT 过滤器、把 `/actuator` 收敛到只暴露 health、
用环境变量提供数据库密码与 JWT 密钥（见上面的清单）。

## 接口约定

统一响应格式（HTTP 状态码恒为 200，业务状态放在 `code`）：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| code | 含义 |
|---|---|
| 200 | 成功 |
| 400 | 请求参数有误（含：上传超限、唯一键冲突、字段超长等可预期的客户端问题） |
| 401 | 未登录或登录过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 600 | 业务处理失败 |

## 进度

两条线分开看：**主站四个业务模块**目前是页面占位（后端接口与页面都还没实现）；**扩展模块**已经可用。

| 线 | 内容 | 状态 |
|---|---|---|
| 主站 · 工程基础 | Spring Boot + Vue 3 + 数据库 + Redis，统一响应体与全局异常处理 | ✅ 已完成 |
| 主站 · 四个业务模块 | 项目王国 / 技术图鉴 / 成长时间线 / 代码知识库 | 规划中（页面为占位） |
| 扩展 · 动效基因库 | 动效工作台（官方 30 + 社区精选 30 双集合 / 七分类 / 推荐组合 / 智能助手 / 运行档位）、候选池流水线（发现 → 分析 → 筛选 → 转 Pattern）、资源增删改查、GitHub 采集与三级去重、代码生成、沙箱预览 | ✅ v1.2.0 |
| 扩展 · 音乐 Agent | MIDI / 简谱解析、乐器按键映射、演奏时间线、Demo 回放、演奏宏导出（TXT / AutoHotkey / JSON）、本机演奏运行时（用户主动开启 + ESC 急停）、AI 音乐助手（初学 / 标准 / 展示三档方案）、演奏难度评估（五星 + 适合人群）、游戏乐器档案与匹配推荐、曲谱分享（演奏码生成 / 查看 / 导入还原） | ✅ v1.2.0 |
| 扩展 · 桌面代理 | WebSocket 协议与派发计划（模拟，不驱动系统输入） | ✅ v1.2.0 |

扩展模块的详细说明见 [docs/EXTENSIONS-COMPLETION-REPORT.md](docs/EXTENSIONS-COMPLETION-REPORT.md)；
2.0 的分阶段计划见 [docs/EXTENSIONS-2.0-PLAN.md](docs/EXTENSIONS-2.0-PLAN.md)，Phase 1 变更报告见 [docs/PHASE-1-REPORT.md](docs/PHASE-1-REPORT.md)；
音乐侧的宏导出、本机演奏与 AI 助手分别见 [docs/MACRO-EXPORT-REPORT.md](docs/MACRO-EXPORT-REPORT.md)、[docs/LOCAL-RUNTIME-REPORT.md](docs/LOCAL-RUNTIME-REPORT.md)、[docs/AI-MUSIC-ASSISTANT-REPORT.md](docs/AI-MUSIC-ASSISTANT-REPORT.md)；
两个扩展的作品集仓库（含截图与演示素材）在 <https://github.com/Dongxibie/kingdom-extensions>。

明确不做：微服务、分布式、复杂权限系统、支付、消息队列。

## 测试

```bash
cd backend && mvn test          # 后端单元测试（不依赖本机 MySQL / Redis）
cd frontend && npx vue-tsc --noEmit && npm run build
```

## 默认账号

| 用户名 | 密码 | 说明 |
|---|---|---|
| admin | admin123 | 登录功能尚未实现，密码已按 BCrypt 存储 |

## 许可

以 [MIT 许可](LICENSE) 发布，著作权归 [Dongxibie](https://github.com/Dongxibie)。
