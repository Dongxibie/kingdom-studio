# Kingdom Studio V1 测试报告

| 项目 | 内容 |
|---|---|
| 被测版本 | v1.0.0（Phase 1 交付物） |
| 测试日期 | 2026-09-22 |
| 测试类型 | 接口测试、数据库测试、前端测试、异常测试、性能基础测试、代码质量检查 |
| 测试方式 | **实际运行测试**（curl 实测接口、MySQL 实跑约束与执行计划、CDP 驱动真实 Chrome 跑前端、Python 并发压测、故障注入） |
| 测试结论 | 基础设施与工程规范达标；**四个业务模块尚未实现，功能验收不通过**（详见第 8 节） |

---

## 1. 测试环境

| 项目 | 实际值 |
|---|---|
| 操作系统 | Windows 11 专业版，版本 10.0.26200，AMD64 |
| Java | JDK 21.0.8 LTS（Oracle HotSpot，`C:\Program Files\Java\jdk-21`） |
| 构建工具 | Apache Maven 3.9.9 |
| Node / npm | Node v25.9.0 / npm 11.12.1 |
| MySQL | MySQL 8.0.26（端口 3306，账号 root） |
| Redis | Redis 3.0.504（端口 6379，Windows 版） |
| 浏览器 | Google Chrome 153.0.8010.53（headless + CDP 驱动） |
| 后端运行 | Spring Boot 3.3.5 可执行 jar，端口 8080，接口前缀 `/api` |
| 前端运行 | Vite 6.4.3 dev server，端口 5173（`/api` 代理到 8080） |
| 测试工具 | curl、Python 3.11（含 bcrypt/socket）、Node 25 + Chrome DevTools Protocol |

服务启动方式：`db/kingdom_studio.sql` 初始化 → `java -jar target/kingdom-studio-backend-1.0.0.jar` → `npm run dev`。

---

## 2. 后端接口测试

### 2.1 基础设施接口（已实现）

| 接口 | 结果 | 说明 |
|---|---|---|
| GET `/api/health` | 通过 | `code=200`，status=UP，MySQL UP(3ms)、Redis UP(1ms) |
| GET `/api/actuator/health` | 通过 | Spring 原生健康端点，db 组件 UP |
| GET `/api/actuator/info` | 通过 | 返回 `{}`；未配置 build 信息，建议 Phase 2 补上 |
| GET `/api/v3/api-docs` | 通过 | OpenAPI 3.0.1，含 `bearerAuth` 安全方案声明 |
| GET `/api/swagger-ui/index.html` | 通过 | Swagger UI 页面可访问 |

### 2.2 业务模块接口（**未实现**）

| 接口 | 结果 | 说明 |
|---|---|---|
| GET `/api/projects` | **未通过** | `code=404 接口不存在：projects` —— 项目列表未实现 |
| GET `/api/projects/1` | **未通过** | 项目详情未实现 |
| POST `/api/projects` | **未通过** | 项目新增未实现 |
| PUT `/api/projects/1` | **未通过** | 项目修改未实现 |
| DELETE `/api/projects/1` | **未通过** | 项目删除未实现 |
| GET `/api/technologies` | **未通过** | 技术列表未实现 |
| POST `/api/technologies` | **未通过** | 技术新增未实现 |
| GET `/api/timelines` | **未通过** | 时间线列表未实现 |
| POST `/api/timelines` | **未通过** | 时间线新增未实现 |
| GET `/api/code-snippets` | **未通过** | 代码片段列表未实现 |
| POST `/api/code-snippets` | **未通过** | 代码片段新增未实现 |
| DELETE `/api/code-snippets/1` | **未通过** | 代码片段删除未实现 |
| GET `/api/users` | **未通过** | 用户列表未实现 |
| GET `/api/users/1` | **未通过** | 用户详情未实现 |
| POST `/api/auth/login` | **未通过** | 登录接口未实现 |

> 结论：Swagger 文档当前只登记了 `GET /health` 一个接口，与上述测试结果一致 —— 四大业务模块属于 Phase 2 范围，本次验收尚未交付。
> 注：未实现的接口返回的是**统一 404 响应体**（`{"code":404,"message":"接口不存在：xxx"}`），说明全局异常处理对这些访问路径是生效的。

### 2.3 异常路径接口

| 接口 | 结果 | 说明 |
|---|---|---|
| POST `/api/health` | 通过 | `code=405`，"不支持的请求方法：POST" |
| GET `/api/not-exist` | 通过 | `code=404`，统一格式，无堆栈泄漏 |
| GET `/api/projects/abc` | 通过 | 非法 ID（非数字）返回统一 404，未出现类型转换异常 |
| OPTIONS `/api/health`（Origin: localhost:5173） | 通过 | 200，返回正确的 `Access-Control-Allow-*` 头 |
| OPTIONS `/api/health`（Origin: evil.example.com） | 通过 | **403 Invalid CORS request**，非法来源被正确拒绝 |

---

## 3. 数据库测试

### 3.1 表结构

| 表 | 引擎 / 字符集 | 字段数 | 索引 |
|---|---|---|---|
| user | InnoDB / utf8mb4_0900_ai_ci | 11 | PRIMARY(id)、UNIQUE uk_user_username(username) |
| project | InnoDB / utf8mb4_0900_ai_ci | 12 | PRIMARY(id)、idx_project_status、idx_project_create_time |
| technology | InnoDB / utf8mb4_0900_ai_ci | 10 | PRIMARY(id)、idx_technology_category、idx_technology_level |
| timeline | InnoDB / utf8mb4_0900_ai_ci | 9 | PRIMARY(id)、idx_timeline_year |
| code_snippet | InnoDB / utf8mb4_0900_ai_ci | 9 | PRIMARY(id)、idx_code_snippet_language、idx_code_snippet_create_time |

字段设计核对通过：统一 `id` 自增主键、`deleted` 逻辑删除、`create_time`/`update_time` 时间字段（`ON UPDATE CURRENT_TIMESTAMP`）、注释与 `COMMENT` 齐全。

### 3.2 约束测试（实测报错码）

| 用例 | 预期 | 实际结果 |
|---|---|---|
| 重复用户名 `admin` | 拒绝 | ✅ ERROR 1062 Duplicate entry for key 'user.uk_user_username' |
| `project.name` 传 NULL | 拒绝 | ✅ ERROR 1048 Column 'name' cannot be null |
| `technology.name` 写入 60 字符（限制 50） | 拒绝 | ✅ ERROR 1406 Data too long for column 'name' |
| 只写 `name` 插入 project | 默认值生效 | ✅ status=PLANNING、progress=0、deleted=0、create_time 自动填充 |
| `project.progress=150` | 拒绝 | ✅ ERROR 1264 Out of range（TINYINT 上限） |
| `project.progress=120` | 拒绝（业务 0-100） | ⚠️ **原先被接受** → 已修复，复测 ERROR 3819 约束拒绝 |
| `project.status='INVALID_STATUS'` | 拒绝 | ⚠️ **原先被接受** → 已修复，复测 ERROR 3819 约束拒绝 |
| `technology.level=99` | 拒绝 | ⚠️ **原先被接受** → 已修复，复测 ERROR 3819 约束拒绝 |
| `technology.category='Other'` | 拒绝 | ⚠️ 已修复，复测 ERROR 3819 约束拒绝 |

### 3.3 初始化数据

| 表 | 记录数 | 逻辑删除 |
|---|---|---|
| user | 1（admin / BCrypt 密码） | 全部 deleted=0 |
| project | 5 | 全部 deleted=0 |
| technology | 10（Java 1 / Spring 3 / Database 2 / Frontend 2 / AI 2） | 全部 deleted=0 |
| timeline | 4（2023–2026） | 全部 deleted=0 |
| code_snippet | 3（Java / SQL / JavaScript） | 全部 deleted=0 |

所有测试均使用事务 + ROLLBACK，测试后复核记录数仍为 1/5/10/4/3，**未污染测试数据**。

### 3.4 查询执行计划（EXPLAIN 实测）

| 查询场景 | 命中索引 | type | 备注 |
|---|---|---|---|
| `WHERE username='admin' AND deleted=0` | uk_user_username | **const** | 最优，唯一索引 |
| `WHERE language='Java' AND deleted=0 ORDER BY create_time DESC` | idx_code_snippet_language | ref | 良好 |
| `WHERE status='DEVELOPING' AND deleted=0 ORDER BY create_time DESC` | idx_project_status | ref | 命中索引但 **Using filesort**（排序未走索引） |
| `GROUP BY category` | idx_technology_category | index | 索引全扫描，符合预期 |

---

## 4. 前端测试

| 用例 | 结果 | 说明 |
|---|---|---|
| 页面加载 `/` | 通过 | 自动重定向到 `/dashboard`，标题"工作台 · Kingdom Studio" |
| 路由 `/dashboard` | 通过 | 主标题、侧边栏高亮、服务状态表均正确 |
| 路由 `/projects` | 通过 | 显示"项目王国"及 Phase 说明 |
| 路由 `/technologies` | 通过 | 显示"技术图鉴"及 Phase 说明 |
| 路由 `/journey` | 通过 | 显示"成长时间线"及 Phase 说明 |
| 路由 `/code-library` | 通过 | 显示"代码知识库"及 Phase 说明 |
| 数据展示 | 通过 | 工作台状态标签由接口数据渲染：`UP`、`UP · 1ms`（后端/MySQL/Redis） |
| 未知路由 `/not-exist-page` | **修复后通过** | 修复前为**完全空白页**；修复后显示 404 页面（"这座建筑还没建成" + 返回工作台按钮） |
| 新增 / 修改 / 删除 | **未通过（无法测试）** | 后端无接口、前端无表单，功能不存在 |
| 浏览器控制台报错 | 通过 | 7 个路由逐一检查，**无任何 JS 报错** |
| 类型检查 | 通过 | `vue-tsc --noEmit` 无错误 |
| 生产构建 | 通过 | `vite build` 成功（19.0s）；主包 1060KB，有体积警告 |
| 窄屏适配（390×844） | **未通过** | 固定 232px 侧边栏挤占 60% 宽度，内容区被压到约 150px，中文逐字换行；未做响应式 |

---

## 5. 异常测试

| 用例 | 结果 | 说明 |
|---|---|---|
| 空参数 | 无法测试 | 无接收参数的已实现接口 |
| 非法 ID | 通过 | `/api/projects/abc` → 统一 404，无 500 |
| 数据库异常 | 通过 | 故障注入：以错误数据库端口启动实例 → `status=DEGRADED`、`database.status=DOWN`、detail 为"Failed to obtain JDBC Connection"，**未返回堆栈** |
| Redis 异常 | 通过 | 同一实例 `redis.status=DOWN`、detail 为"Unable to connect to Redis" |
| 依赖故障时服务可用性 | 通过 | 数据库与 Redis 全不可用时应用**仍能正常启动并响应**，正常实例不受影响 |
| 接口错误（方法不支持） | 通过 | POST `/api/health` → 405 统一格式 |
| 接口路径错误 | 通过 | 统一 404 |
| 跨域非法来源 | 通过 | 403 Invalid CORS request |
| 非法 JSON 请求体 | 无法测试 | 无接收请求体的已实现接口 |

---

## 6. 性能基础测试

### 6.1 接口响应时间

| 场景 | 最小 | 平均 | 中位 | P95 | P99 | 最大 | 失败 |
|---|---|---|---|---|---|---|---|
| 顺序 50 次 `GET /api/health` | 7.7ms | 26.4ms | 30.1ms | 35.2ms | 36.1ms | 40.2ms | 0 |

| 场景 | 请求数 | 总耗时 | 吞吐 | 平均 | P95 | 最大 | 失败 |
|---|---|---|---|---|---|---|---|
| 并发（20 线程 × 10 次） | 200 | 0.33s | **614 req/s** | 27.5ms | 56.3ms | 78.0ms | **0** |

内部构成：MySQL 探测 3ms、Redis 探测 1ms，其余约 25ms 为 HTTP/JSON/Servlet 开销。

### 6.2 SQL 查询

见 3.4 节执行计划：4 类典型查询全部命中索引，其中用户登录查询为最优的 `const`；项目列表按状态过滤 + 时间排序存在 `Using filesort`（当前数据量下无性能影响）。

### 6.3 缓存使用情况

| 项 | 结果 | 说明 |
|---|---|---|
| Redis 连通性 | 通过 | PING → PONG，健康检查 1ms |
| RedisTemplate | 已配置 | String key + JSON value，便于 redis-cli 排查 |
| RedisCacheManager | 已配置 | 默认 TTL 30 分钟，key 前缀 `kingdom:` |
| 业务缓存命中 | **未验证** | 当前代码尚无 `@Cacheable` 标注的业务查询（无业务代码），缓存链路未实际投入使用 |

---

## 7. 代码质量检查

### 7.1 规模与复杂度

| 项 | 数量 | 说明 |
|---|---|---|
| 后端 Java 文件 | 13 个 / 671 行 | 最大文件 `GlobalExceptionHandler` 102 行，无超长类 |
| 前端源文件 | 10 个 / 585 行 | 最大文件 `DashboardView.vue` 173 行 |

### 7.2 分层结构

| 检查项 | 结果 | 说明 |
|---|---|---|
| Controller 是否含业务逻辑 | **修复后通过** | 修复前 `HealthController` 内含约 50 行探测逻辑（违反"业务逻辑不进 Controller"要求），已下沉到 `HealthService` |
| 分层包结构 | 通过 | `common / config / controller / service / handler / vo` 职责清晰；`entity / mapper / dto` 待 Phase 2 |
| 统一响应与异常集中 | 通过 | 响应体只有一处实现；异常处理集中在一个 `@RestControllerAdvice` |

### 7.3 命名规范

| 检查项 | 结果 |
|---|---|
| 包名全小写分层 | 通过 |
| 类名后缀符合分层语义（`XxxController` / `XxxService` / `XxxVO` / `XxxConfig`） | 通过 |
| 常量与字段命名（`APP_VERSION`、`DEFAULT_PAGE_SIZE` 风格） | 通过 |
| 前端命名（组件 PascalCase、目录小写、组合式 API `useXxx`） | 通过 |

### 7.4 重复代码

未发现明显重复：统一响应、异常映射、公共字段填充、序列化配置各自只有一处实现，无复制粘贴痕迹。

### 7.5 安全问题

| 检查项 | 结果 | 说明 |
|---|---|---|
| 密码存储 | 通过 | 种子用户密码为 BCrypt 哈希，应用使用 `BCryptPasswordEncoder` |
| 敏感信息硬编码 | **需处理** | `application.yml` 中 JWT 密钥与数据库密码有**可被环境变量覆盖的默认值**，直接开源会暴露（`${JWT_SECRET:...}`、`${DB_PASSWORD:root}`） |
| 接口鉴权 | 预期内未启用 | Phase 1 全部放行，`SecurityConfig` 已留 TODO，Phase 2 收紧为 `authenticated()` |
| 异常信息泄漏 | 通过 | 500/404/405 均返回统一格式，堆栈只进日志；健康检查异常信息截断到 120 字符 |
| 内部信息暴露 | 需注意 | `/api/health` 返回 MySQL 版本；Actuator、Swagger 对外开放，生产环境建议关闭或加权限 |
| 版本控制卫生 | 通过 | `.gitignore` 已覆盖 `target/`、`node_modules/`、`dist/`、`.env` |
| SQL 注入 | 通过 | 无字符串拼接 SQL（MyBatis-Plus 参数绑定；当前仅 `SELECT VERSION()`） |

---

## 8. 最终结论

### 8.1 本次测试中发现并修复的缺陷（均已复测通过）

| 编号 | 缺陷 | 级别 | 修复方式 | 复测结果 |
|---|---|---|---|---|
| D1 | 未知路由渲染**完全空白页**，无 404 兜底 | 中 | 新增 `NotFoundView.vue` + 通配路由 `/:pathMatch(.*)*` | ✅ 显示 404 页面，标题正确，无控制台报错 |
| D2 | 数据库缺少业务约束：`progress` 可写 120、`status` 可写非法枚举、`level` 可写 99、`category` 可写非五类 | 中 | 为 4 张表补 6 个 CHECK 约束（同步更新初始化脚本） | ✅ 4 个非法写入全部被拒（ERROR 3819），正常数据不受影响 |
| D3 | 健康探测逻辑写在 Controller 中，违反分层要求 | 中 | 抽出 `HealthService`，Controller 只做转发 | ✅ 接口返回不变，Swagger 文档正常，异常路径正常 |

### 8.2 三大标准达标评估

| 标准 | 结论 | 依据 |
|---|---|---|
| **个人作品展示标准** | **未达标（基础设施达标，功能未达标）** | 架构分层、统一响应、全局异常、数据库设计、索引、接口文档、性能表现均达标；但四个业务模块的**全部 CRUD 接口与页面不存在**，无法演示完整功能 |
| **GitHub 开源标准** | **暂不建议直接开源** | 优点：结构清晰、README 与阶段文档齐全、`.gitignore` 规范、依赖版本明确。<br>待补：① 业务功能（否则 star 无意义）② 密钥/密码默认值处理 ③ 自动化测试 ④ LICENSE 文件 |
| **简历项目标准** | **未达标** | 简历项目需要"能演示的功能闭环"。当前无法演示"新增项目 → 列表展示 → 编辑 → 删除"这一条主链路，也无法体现参数校验、分页、缓存等后端能力（相关配置虽已就绪但未被业务使用） |

### 8.3 达标的核心能力（可用于答辩/自我陈述的部分）

- 后端分层架构 + 统一响应体 + 全局异常处理（含 8 类异常的统一转换）
- 数据库设计：5 表、主键/索引/时间字段/逻辑删除/业务约束齐备，典型查询全部命中索引
- Redis 接入（模板 + 缓存管理器）、Swagger 接口文档、CORS 精细化控制（非法来源 403）
- 故障降级能力：数据库与 Redis 同时不可用时服务仍可启动，并准确报告依赖状态
- 性能：614 req/s、200 并发零失败、P95 56ms（健康检查接口）
- 前端工程化：Vue3 + TS 严格模式、类型检查与生产构建通过、Axios 统一封装、7 个路由零控制台报错

### 8.4 V1.1 建议优化列表

**P0 · 功能闭环（完成前无法作为作品展示）**
1. 实现四个模块的后端 CRUD：Entity / Mapper / Service（接口 + impl）/ Controller / DTO / VO，含参数校验与分页
2. 前端四模块页面：项目卡片墙（技术栈 + 完成度 + GitHub 链接）、技能卡片墙、时间轴、代码片段管理（含语法高亮）
3. 实现登录：`POST /auth/login` 返回 JWT + `JwtAuthenticationFilter`，`SecurityConfig` 收紧为 `authenticated()`

**P1 · 开源前必做**
4. 敏感信息处理：`application.yml` 改为不提供默认密钥（或提供 `application-example.yml`），README 增加环境变量说明
5. 增加 `LICENSE`（MIT）与 `docs/API.md`（导出 OpenAPI 或手写接口文档）
6. 补充自动化测试：`@SpringBootTest` + MockMvc 覆盖 Service/Controller（目标：核心接口 20 条用例以上）

**P2 · 质量提升**
7. 前端响应式适配：侧边栏在窄屏折叠为抽屉，卡片网格自适应（当前 390px 下布局不可用）
8. 统一响应与 HTTP 语义：可选方案 —— 保持 `code` 恒 200（现有约定），或让 HTTP 状态与 `code` 对齐（便于监控与网关识别）
9. 生产环境收敛：Actuator 仅暴露 health、Swagger 加开关、健康检查不返回数据库版本
10. Element Plus 改为按需引入（`unplugin-vue-components`），主包体积从 1060KB 降到约 400KB

**P3 · 体验与性能**
11. `project` 表增加复合索引 `(status, create_time)`，消除列表查询的 `Using filesort`
12. 列表接口接入 `@Cacheable`（Redis 已就绪），体现"缓存扩展"的实际收益
13. 统一分页返回体 `PageResult<T>`（MyBatis-Plus 分页插件已配置，等待业务接入）
14. 前端补充 `PageLoading`/空状态组件与统一错误页，提升演示观感

---

### 附：测试可复现命令

```bash
# 后端接口清单测试
bash _gh/api_test.sh

# 性能测试（顺序 50 次 + 并发 200 次）
python _gh/perf_test.py

# 前端路由与渲染测试（需先启动 headless Chrome 的 CDP 端口）
node _gh/frontend_test.mjs 9333

# 数据库约束复测
mysql -uroot -proot -e "USE kingdom_studio; INSERT INTO project(name,progress) VALUES('越界',120);"
# 预期：ERROR 3819 Check constraint 'chk_project_progress' is violated
```
