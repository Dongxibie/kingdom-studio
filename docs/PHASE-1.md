# Phase 1 · 项目初始化

> 目标：搭好前后端与数据库三件套，能启动、能联通、能验证。**不写业务代码**（业务留给 Phase 2/3）。

## 一、完成内容

### 1. 工程与构建
- Maven 单模块工程 `backend`，Java 21 + Spring Boot 3.3.5，可打包成可执行 jar。
- Vite 6 + Vue 3 + TypeScript 工程 `frontend`，Element Plus 全量引入 + 中文语言包。
- 前后端分离：前端统一请求 `/api/**`，开发期由 Vite 代理到 `http://localhost:8080`，无跨域问题。

### 2. 数据库（5 张表 + 测试数据）
`db/kingdom_studio.sql` 可重复执行，包含建库、建表、索引、测试数据与自检查询。

| 表 | 用途 | 关键索引 |
|---|---|---|
| `user` | 用户（V1 单账号，结构按多用户设计） | `uk_user_username` |
| `project` | 项目王国 | `idx_project_status`、`idx_project_create_time` |
| `technology` | 技术图鉴 | `idx_technology_category`、`idx_technology_level` |
| `timeline` | 成长时间线 | `idx_timeline_year` |
| `code_snippet` | 代码知识库 | `idx_code_snippet_language`、`idx_code_snippet_create_time` |

设计约定：统一 `id` 自增主键、`deleted` 逻辑删除、`create_time` / `update_time` 时间字段（带 `ON UPDATE`），字符集 `utf8mb4`。

测试数据：1 个用户、5 个项目（含 Kingdom Studio 自己）、10 条技术、4 条时间线、3 段代码。

### 3. 后端基础设施（Phase 1 的关键部分）
- **统一响应体** `Result<T>`：`{code, message, data}`，配套 `ResultCode` 状态码枚举。
- **全局异常处理** `GlobalExceptionHandler`：业务异常、参数校验、JSON 解析、405、404、403、兜底 500，全部转成统一格式，异常堆栈不外泄。
- **分层骨架**：`controller` / `vo` / `common` / `config` / `handler` 已建立，`entity` `mapper` `service` `dto` 留到 Phase 2。
- **MyBatis-Plus**：分页插件（单页上限 100 条）+ 防全表更新删除插件 + 公共字段自动填充。
- **Redis**：`RedisTemplate`（String key + JSON value，方便 redis-cli 排查）+ `RedisCacheManager`（默认 30 分钟过期，供 `@Cacheable` 使用）。
- **Spring Security**：无状态会话、关闭 CSRF、BCrypt 编码器、CORS 打通；当前接口全部放行，**Phase 2 接入 JWT 后收紧**。
- **SpringDoc**：接口文档 + bearerAuth 安全方案声明。
- **健康检查** `GET /api/health`：一次请求同时验证应用、MySQL、Redis，这是本阶段的验收接口。

### 4. 前端骨架
- 侧边栏 + 顶栏布局（深红金配色，为后续 Husky King IP 留出延展空间）。
- 路由与四个模块入口；模块页先放占位说明，Phase 3 再实现。
- Axios 封装：请求自动带 token、响应自动剥壳、失败统一弹提示。
- 工作台页面调用 `/api/health`，实时显示后端/MySQL/Redis 状态。

## 二、修改文件

```
kingdom-studio/
├── README.md                                    新增
├── docs/PHASE-1.md                              新增（本文件）
├── db/kingdom_studio.sql                        新增
├── backend/
│   ├── pom.xml                                  新增
│   └── src/main/
│       ├── java/com/kingdomstudio/
│       │   ├── KingdomStudioApplication.java    新增
│       │   ├── common/Result.java               新增
│       │   ├── common/ResultCode.java           新增
│       │   ├── common/exception/BusinessException.java        新增
│       │   ├── common/exception/GlobalExceptionHandler.java   新增
│       │   ├── config/MybatisPlusConfig.java    新增
│       │   ├── config/RedisConfig.java          新增
│       │   ├── config/SecurityConfig.java       新增
│       │   ├── config/CorsConfig.java           新增
│       │   ├── config/OpenApiConfig.java        新增
│       │   ├── handler/MyMetaObjectHandler.java 新增
│       │   ├── controller/HealthController.java 新增
│       │   └── vo/HealthVO.java                 新增
│       └── resources/application.yml            新增
└── frontend/
    ├── package.json / vite.config.ts / tsconfig.json / index.html   新增
    └── src/
        ├── main.ts / App.vue / env.d.ts         新增
        ├── styles/main.css                      新增
        ├── router/index.ts                       新增
        ├── api/request.ts                        新增
        ├── api/health.ts                         新增
        ├── layout/BasicLayout.vue                新增
        ├── views/DashboardView.vue               新增
        └── views/ModulePlaceholder.vue           新增
```

## 三、如何运行

前置：JDK 21、Maven 3.9+、Node 18+、MySQL 8、Redis 都已在跑。

```bash
# 1) 初始化数据库
mysql -uroot -proot < db/kingdom_studio.sql

# 2) 后端（新开一个终端）
cd backend
mvn spring-boot:run          # 或用已打好的包：java -jar target/kingdom-studio-backend-1.0.0.jar

# 3) 前端（再开一个终端）
cd frontend
npm install
npm run dev
```

访问地址：

| 服务 | 地址 |
|---|---|
| 前端工作台 | http://localhost:5173 |
| 健康检查接口 | http://localhost:8080/api/health |
| 接口文档 | http://localhost:8080/api/swagger-ui.html |
| Actuator 健康 | http://localhost:8080/api/actuator/health |

> 若本机 MySQL 密码不是 `root`，用环境变量覆盖：`DB_PASSWORD=xxx mvn spring-boot:run`。

## 四、如何测试（Phase 1 验收清单）

**1）后端单独验证**

```bash
curl http://localhost:8080/api/health
```

期望：`code=200`，`database.status=UP`（detail 里带 MySQL 版本）、`redis.status=UP`（detail 为 PONG）。

**2）统一异常处理验证**

```bash
# 不存在的接口 → 404 统一格式
curl http://localhost:8080/api/not-exist
# 期望：{"code":404,"message":"接口不存在：...","data":null}

# 错误的请求方法 → 405
curl -X POST http://localhost:8080/api/health
```

**3）数据库验证**

```bash
mysql -uroot -proot -e "USE kingdom_studio; SELECT COUNT(*) FROM project; SELECT COUNT(*) FROM technology;"
```

**4）前端验证**

打开 http://localhost:5173 —— 工作台的「服务状态」卡片应显示：服务状态 UP、MySQL UP、Redis UP；四个模块入口可点击跳转（页面为 Phase 3 占位说明）。

**5）联调代理验证**

```bash
curl http://localhost:5173/api/health     # 经 Vite 代理访问后端，应返回同样结果
```

## 五、执行过程中修复的问题

| 问题 | 原因 | 处理 |
|---|---|---|
| 编译报错：找不到 `PaginationInnerInterceptor` | MyBatis-Plus 3.5.9 起把依赖 JSqlParser 的插件拆成独立包 | `pom.xml` 增加 `mybatis-plus-jsqlparser` 依赖 |
| 启动日志出现 typeAliases 扫描警告 | Phase 1 还没有 `entity` 包 | `application.yml` 里先注释 `type-aliases-package`，Phase 2 打开 |
| 启动日志提示 "Using generated security password" | 还没有自定义 `UserDetailsService`（Phase 2 才做登录） | 属预期；Phase 2 接入用户表登录后消失 |
| `vue-tsc` 报 TS6305 / TS6310 | tsconfig 的 project reference 与 `noEmit` 冲突 | 去掉 `tsconfig.node.json` 引用，合并为单一 `tsconfig.json`（含 `vite.config.ts`） |

## 六、本阶段的已知边界

- 业务接口（四个模块的 CRUD）**尚未实现**，属 Phase 2 内容。
- 登录接口 `/auth/login` 与 JWT 过滤器**尚未实现**；`SecurityConfig` 中已标了 TODO，Phase 2 收紧为 `authenticated()`。
- 前端四个模块页为占位，Phase 3 实现真实页面。
- Element Plus 目前是**全量引入**（`npm run build` 会提示主包 > 500KB）。个人项目优先清晰，暂不折腾按需引入；若后期在意体积，可换 `unplugin-vue-components` 自动按需引入。
- Redis 目前只做了配置与健康检查，业务缓存（`@Cacheable`）在 Phase 2/3 按需添加。

## 七、需求建议（等你确认，不自行扩范围）

1. **登录范围**：V1 是否只做「单账号 admin 登录 + JWT」，不做注册/找回密码？（建议是）
2. **封面图与头像**：项目封面是要支持上传文件，还是只存外链地址？（建议先只存外链，Upload 模块单独作为可选加分项）
3. **代码片段**：是否需要语法高亮（如 highlight.js）？是否要按语言过滤 + 关键字搜索？（建议高亮与过滤都做，都是前端小改动）
4. **数据可见性**：`user` 表结构支持多用户，但 V1 的四个模块数据是否都视为「公共展示数据」（即不做数据隔离）？（建议是，避免过度设计）
5. **分页**：项目/代码片段列表是否要做分页？（建议做，后端分页插件已就绪，正好体现 MyBatis-Plus 分页能力）
