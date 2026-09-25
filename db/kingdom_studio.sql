-- =====================================================================
-- Kingdom Studio · 数据库初始化脚本
-- MySQL 8.0+ / utf8mb4 / InnoDB
--
-- ⚠️ 说明：本脚本用于「首次初始化」。它会先 DROP 掉 kingdom_studio 库内的
--          这 5 张表再重建，因此**会清空这些表里的既有数据**。
--          如果只想追加数据，请只执行文件末尾的 INSERT 部分。
--
-- 执行方式：
--   mysql -uroot -proot < db/kingdom_studio.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `kingdom_studio`
	DEFAULT CHARACTER SET utf8mb4
	DEFAULT COLLATE utf8mb4_general_ci;

USE `kingdom_studio`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------
-- 1. user 用户表（V1 只做单账号登录，结构上仍按多用户设计，方便扩展）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
	`id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
	`username`    VARCHAR(50)  NOT NULL                COMMENT '登录名',
	`password`    VARCHAR(100) NOT NULL                COMMENT '密码（BCrypt 加密）',
	`nickname`    VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '昵称',
	`avatar`      VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '头像地址',
	`email`       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
	`role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN / USER',
	`status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1 启用，0 禁用',
	`deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 未删，1 已删',
	`create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uk_user_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ---------------------------------------------------------------------
-- 2. project 项目表（Module 1 项目王国）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
	`id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
	`name`             VARCHAR(100) NOT NULL                COMMENT '项目名称',
	`description`      VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '项目简介',
	`cover_image`      VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '封面图地址',
	`technology_stack` VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '技术栈，英文逗号分隔，如 Java,Spring Boot,MySQL',
	`github_url`       VARCHAR(255) NOT NULL DEFAULT ''     COMMENT 'GitHub 地址',
	`status`           VARCHAR(20)  NOT NULL DEFAULT 'PLANNING' COMMENT '状态：PLANNING 规划中 / DEVELOPING 开发中 / COMPLETED 已完成',
	`progress`         TINYINT      NOT NULL DEFAULT 0      COMMENT '完成度百分比 0-100',
	`sort_order`       INT          NOT NULL DEFAULT 0      COMMENT '排序值，越小越靠前',
	`deleted`          TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
	`create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	KEY `idx_project_status` (`status`),
	KEY `idx_project_create_time` (`create_time`),
	CONSTRAINT `chk_project_status` CHECK (`status` IN ('PLANNING', 'DEVELOPING', 'COMPLETED')),
	CONSTRAINT `chk_project_progress` CHECK (`progress` BETWEEN 0 AND 100)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '项目表';

-- ---------------------------------------------------------------------
-- 3. technology 技术表（Module 2 技术图鉴）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `technology`;
CREATE TABLE `technology` (
	`id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
	`name`        VARCHAR(50)  NOT NULL                COMMENT '技术名称',
	`category`    VARCHAR(20)  NOT NULL                COMMENT '分类：Java / Spring / Database / AI / Frontend',
	`level`       TINYINT      NOT NULL DEFAULT 1      COMMENT '掌握程度 1-5 星',
	`description` VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '说明 / 学习心得',
	`learn_date`  DATE         DEFAULT NULL            COMMENT '开始学习日期',
	`icon`        VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '图标地址或标识',
	`deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
	`create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	KEY `idx_technology_category` (`category`),
	KEY `idx_technology_level` (`level`),
	CONSTRAINT `chk_technology_category` CHECK (`category` IN ('Java', 'Spring', 'Database', 'AI', 'Frontend')),
	CONSTRAINT `chk_technology_level` CHECK (`level` BETWEEN 1 AND 5)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '技术图鉴表';

-- ---------------------------------------------------------------------
-- 4. timeline 成长时间线（Module 3 Royal Journey）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `timeline`;
CREATE TABLE `timeline` (
	`id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
	`year`        SMALLINT     NOT NULL                COMMENT '年份',
	`title`       VARCHAR(100) NOT NULL                COMMENT '节点标题',
	`description` VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '节点描述',
	`level`       TINYINT      NOT NULL DEFAULT 1      COMMENT '阶段等级 1-5（对应王国晋升）',
	`sort_order`  INT          NOT NULL DEFAULT 0      COMMENT '同一年内的排序值',
	`deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
	`create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	KEY `idx_timeline_year` (`year`),
	CONSTRAINT `chk_timeline_level` CHECK (`level` BETWEEN 1 AND 5)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '成长时间线表';

-- ---------------------------------------------------------------------
-- 5. code_snippet 代码片段表（Module 4 Code Library）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `code_snippet`;
CREATE TABLE `code_snippet` (
	`id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
	`title`        VARCHAR(100) NOT NULL                COMMENT '片段标题',
	`language`     VARCHAR(20)  NOT NULL                COMMENT '语言：Java / SQL / JavaScript',
	`description`  VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '说明',
	`code_content` TEXT         NOT NULL                COMMENT '代码内容',
	`tags`         VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '标签，英文逗号分隔',
	`deleted`      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
	`create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	KEY `idx_code_snippet_language` (`language`),
	KEY `idx_code_snippet_create_time` (`create_time`),
	CONSTRAINT `chk_code_snippet_language` CHECK (`language` IN ('Java', 'SQL', 'JavaScript'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '代码片段表';

-- =====================================================================
-- 测试数据
-- =====================================================================

-- 默认账号：admin / admin123（密码为 BCrypt 哈希，登录功能在 Phase 2 实现）
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `role`, `status`)
VALUES ('admin', '$2b$10$fbqHphbN2OBtuqq1Bj7KuuL71LYNrSqRegzPFS/aRCBMXwAFw3Zg6', '王国主人', '1337815858@qq.com', 'ADMIN', 1);

INSERT INTO `project` (`name`, `description`, `technology_stack`, `github_url`, `status`, `progress`, `sort_order`) VALUES
('Kingdom Studio', '个人开发者工作台 —— 管理项目、技术资产、成长路线与代码知识库，属于开发者自己的数字王国。', 'Java,Spring Boot,MyBatis Plus,MySQL,Redis,Vue3,TypeScript', 'https://github.com/Dongxibie', 'DEVELOPING', 20, 1),
('九八 · 校园快递代取订单管理系统', '校园快递代取业务的订单管理系统，覆盖下单、接单、配送、结算全流程。', 'Java,Spring Boot,MyBatis Plus,MySQL,Vue3', 'https://github.com/Dongxibie/jiuba', 'COMPLETED', 100, 2),
('Offer Hunter AI · AI 求职助手', '面向大学生的一站式求职平台：简历解析、岗位匹配、简历优化、模拟面试与成长规划。', 'Java,Spring Boot,Spring AI,Vue3,DeepSeek', 'https://github.com/Dongxibie/Offer-hunter-ai', 'COMPLETED', 100, 3),
('AI Builder Studio', 'Prompt 工程生命周期平台：构建、可解释分析、优化、本地版本与试跑。', 'TypeScript,Next.js,Supabase,Vitest', 'https://github.com/Dongxibie/ai-builder-studio', 'COMPLETED', 100, 4),
('居家环境感知预警监测系统', '居家环境数据采集与预警监测，含实时看板与阈值告警。', 'Java,Spring Boot,MySQL,Vue3,ECharts', 'https://github.com/Dongxibie', 'DEVELOPING', 60, 5);

INSERT INTO `technology` (`name`, `category`, `level`, `description`, `learn_date`) VALUES
('Java 21', 'Java', 4, '熟悉集合、并发基础、Stream 与 JDK 新特性，日常主力语言。', '2024-03-01'),
('Spring Boot 3', 'Spring', 4, '掌握自动装配、分层架构、统一异常处理与接口规范。', '2024-04-01'),
('Spring Security + JWT', 'Spring', 3, '能实现基于 JWT 的无状态登录与接口鉴权。', '2024-09-01'),
('MyBatis Plus', 'Spring', 4, '熟练使用条件构造器、分页插件、逻辑删除与字段自动填充。', '2024-05-01'),
('MySQL 8', 'Database', 4, '掌握表结构设计、索引与执行计划初步分析、常见 SQL 优化。', '2024-03-15'),
('Redis', 'Database', 3, '掌握缓存读写、过期策略与 Spring Cache 集成。', '2024-10-01'),
('Vue 3 + TypeScript', 'Frontend', 4, '组合式 API、组件化开发、Axios 封装与前后端联调。', '2024-06-01'),
('Vite', 'Frontend', 4, '前端工程化构建工具，熟悉代理与打包配置。', '2024-06-10'),
('Spring AI / LLM 应用', 'AI', 3, '接入大模型完成简历解析、问答与 Prompt 工程实践。', '2025-02-01'),
('Prompt Engineering', 'AI', 4, '能设计结构化提示词并做多轮评测与优化。', '2025-01-10');

INSERT INTO `timeline` (`year`, `title`, `description`, `level`, `sort_order`) VALUES
(2023, '起点 · The Curious Pup', '第一次打开 IDE，写下第一行代码；从 C 语言与数据结构开始，什么都想试一试。', 1, 1),
(2024, 'Java Apprentice', '系统学习 Java 与面向对象，完成第一个 Spring Boot 项目，理解了分层与接口。', 2, 1),
(2025, 'Backend Knight', '独立完成校园快递代取系统，跑通「需求 → 设计 → 编码 → 部署」的完整链路。', 3, 1),
(2026, 'AI Kingdom Builder', '把 AI 能力接入自己的项目，用工程化的方式沉淀技术资产与知识库。', 4, 1);

INSERT INTO `code_snippet` (`title`, `language`, `description`, `code_content`, `tags`) VALUES
('Stream 分组统计', 'Java', '按分类对集合分组并统计数量，替代手写循环。', 'Map<String, Long> countByCategory = items.stream()\n        .collect(Collectors.groupingBy(Item::getCategory, Collectors.counting()));', 'Stream,Collectors,Java21'),
('按分类统计项目数', 'SQL', '统计每类技术的条目数量，并按数量倒序。', 'SELECT category, COUNT(*) AS total\nFROM technology\nWHERE deleted = 0\nGROUP BY category\nORDER BY total DESC;', 'GROUP BY,聚合,索引'),
('防抖函数', 'JavaScript', '限制高频事件的触发频率，常用于搜索输入。', 'function debounce(fn, delay = 300) {\n  let timer = null\n  return (...args) => {\n    clearTimeout(timer)\n    timer = setTimeout(() => fn.apply(this, args), delay)\n  }\n}', 'debounce,性能,前端');

SET FOREIGN_KEY_CHECKS = 1;

-- 执行结果自检
SELECT 'user' AS table_name, COUNT(*) AS rows_inserted FROM `user`
UNION ALL SELECT 'project', COUNT(*) FROM `project`
UNION ALL SELECT 'technology', COUNT(*) FROM `technology`
UNION ALL SELECT 'timeline', COUNT(*) FROM `timeline`
UNION ALL SELECT 'code_snippet', COUNT(*) FROM `code_snippet`;
