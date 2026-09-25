# Kingdom Studio

> 一个属于开发者自己的数字王国。

个人开发者工作台：管理**我的项目**、**我的技术资产**、**我的成长路线**和**我的代码知识库**。
视觉概念是「个人开发者 = 王国建设者」，后续可结合 Husky King（哈士奇国王）个人 IP 做视觉延展。

这是一个**个人作品项目**，用于展示 Java 后端、Web 开发、数据库设计与工程能力，不是企业级商业系统。

---

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 21、Spring Boot 3.3.5、MyBatis-Plus 3.5.9、Spring Security + JWT、Spring Cache |
| 数据库 | MySQL 8.0（utf8mb4 / InnoDB） |
| 缓存 | Redis（缓存扩展 + 健康检查） |
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
│       │   ├── common/          # 统一响应体、状态码、异常体系
│       │   ├── config/          # MyBatis-Plus / Redis / 跨域 / Security / OpenAPI
│       │   ├── controller/      # 只做参数接收与结果返回，不写业务逻辑
│       │   ├── handler/         # 公共字段自动填充
│       │   └── vo/              # 返回给前端的视图对象（Phase 2 还会加 entity/mapper/service/dto）
│       └── resources/application.yml
├── frontend/                    # Vue 3 前端
│   └── src/
│       ├── api/                 # Axios 封装（统一响应处理）
│       ├── layout/              # 侧边栏 + 顶栏骨架
│       ├── router/              # 路由表
│       └── views/               # 页面
└── db/kingdom_studio.sql        # 数据库初始化脚本（建表 + 测试数据）
```

## 快速开始

**前置条件**：JDK 21、Maven 3.9+、Node 18+、MySQL 8（默认 `root/root`）、Redis（默认 `127.0.0.1:6379`）。

```bash
# 1. 初始化数据库（会重建 kingdom_studio 库内的 5 张表）
mysql -uroot -proot < db/kingdom_studio.sql

# 2. 启动后端（端口 8080，接口前缀 /api）
cd backend
mvn spring-boot:run

# 3. 启动前端（端口 5173，/api 自动代理到后端）
cd frontend
npm install
npm run dev
```

打开 <http://localhost:5173> 即可看到工作台；后端接口文档在 <http://localhost:8080/api/swagger-ui.html>。

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
| 400 | 请求参数有误 |
| 401 | 未登录或登录过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 600 | 业务处理失败 |

## 开发阶段

| 阶段 | 内容 | 状态 |
|---|---|---|
| Phase 1 | 项目初始化：Spring Boot + Vue3 + 数据库，完成启动 | ✅ 已完成 |
| Phase 2 | 后端四大模块 Entity / Mapper / Service / Controller + 接口测试 | ⏳ 待开始 |
| Phase 3 | 前端页面（项目卡片、技能卡片、时间轴、代码库） | ⏳ 待开始 |
| Phase 4 | 前后端联调 | ⏳ 待开始 |
| Phase 5 | 测试与总结 | ⏳ 待开始 |

V1 范围内的四个模块：**项目王国**、**技术图鉴**、**成长时间线**、**代码知识库**。

明确不做：微服务、分布式、复杂权限系统、支付、消息队列。

## 默认账号

| 用户名 | 密码 | 说明 |
|---|---|---|
| admin | admin123 | 登录功能在 Phase 2 实现，密码已按 BCrypt 存储 |
