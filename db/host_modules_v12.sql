-- =====================================================================
-- 主站四个业务模块 · 产品化补强（V1.2）
--
--   项目王国 / 技术图鉴 / 成长时间线 / 代码知识库 四张表在 V1 就有骨架，
--   这次把「作品展示」需要的信息补齐，不重建表、不动既有列：
--
--   project.demo_url            演示地址（有部署的才填）
--   project.highlights          项目亮点（Markdown）
--   technology.used_projects    这套技术用在哪些项目里
--   technology.category         放开到 DevOps（补 CI 与部署能力）
--   timeline.event_date         精确到日的时间点（年度节点留空）
--   timeline.related_project    这个节点属于哪个项目
--
--   代码知识库的表结构已经够用（title / language / description / code_content / tags），
--   所以 Phase 4 只做展示与接口，不新建表。
--
-- 幂等：可重复执行。ALTER 先查 information_schema，内容用名称做存在性判断。
-- =====================================================================

USE `kingdom_studio`;

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 1. project：演示地址 + 项目亮点
-- ---------------------------------------------------------------------
SET @col = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
	WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'project' AND `COLUMN_NAME` = 'demo_url');
SET @ddl = IF(@col = 0,
	'ALTER TABLE `project` ADD COLUMN `demo_url` VARCHAR(255) NOT NULL DEFAULT '''' COMMENT ''演示地址（Demo）'' AFTER `github_url`',
	'SELECT ''project.demo_url 已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
	WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'project' AND `COLUMN_NAME` = 'highlights');
SET @ddl = IF(@col = 0,
	'ALTER TABLE `project` ADD COLUMN `highlights` TEXT NULL COMMENT ''项目亮点，Markdown 文本'' AFTER `progress`',
	'SELECT ''project.highlights 已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 2. technology：项目应用 + 放开 DevOps 分类
-- ---------------------------------------------------------------------
SET @col = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
	WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'technology' AND `COLUMN_NAME` = 'used_projects');
SET @ddl = IF(@col = 0,
	'ALTER TABLE `technology` ADD COLUMN `used_projects` VARCHAR(255) NOT NULL DEFAULT '''' COMMENT ''项目应用，英文逗号分隔'' AFTER `description`',
	'SELECT ''technology.used_projects 已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- CHECK_CONSTRAINTS 里没有表名，要 JOIN TABLE_CONSTRAINTS 才能按表定位
SET @cat_def = (
	SELECT cc.`CHECK_CLAUSE`
	FROM `information_schema`.`CHECK_CONSTRAINTS` cc
	JOIN `information_schema`.`TABLE_CONSTRAINTS` tc
	  ON tc.`CONSTRAINT_SCHEMA` = cc.`CONSTRAINT_SCHEMA`
	 AND tc.`CONSTRAINT_NAME` = cc.`CONSTRAINT_NAME`
	WHERE cc.`CONSTRAINT_SCHEMA` = DATABASE()
	  AND tc.`TABLE_NAME` = 'technology'
	  AND cc.`CONSTRAINT_NAME` = 'chk_technology_category'
	LIMIT 1
);
SET @ddl = IF(@cat_def IS NOT NULL AND @cat_def NOT LIKE '%DevOps%',
	'ALTER TABLE `technology` DROP CHECK `chk_technology_category`',
	'SELECT ''分类约束已放开，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has = (SELECT COUNT(*) FROM `information_schema`.`TABLE_CONSTRAINTS`
	WHERE `CONSTRAINT_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'technology'
	  AND `CONSTRAINT_NAME` = 'chk_technology_category');
SET @ddl = IF(@has = 0,
	'ALTER TABLE `technology` ADD CONSTRAINT `chk_technology_category` CHECK (`category` IN (''Java'', ''Spring'', ''Database'', ''AI'', ''Frontend'', ''DevOps''))',
	'SELECT ''分类约束已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 3. timeline：时间点精确到日 + 关联项目
-- ---------------------------------------------------------------------
SET @col = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
	WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'timeline' AND `COLUMN_NAME` = 'event_date');
SET @ddl = IF(@col = 0,
	'ALTER TABLE `timeline` ADD COLUMN `event_date` DATE DEFAULT NULL COMMENT ''精确到日的时间点，年度节点留空'' AFTER `year`',
	'SELECT ''timeline.event_date 已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
	WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'timeline' AND `COLUMN_NAME` = 'related_project');
SET @ddl = IF(@col = 0,
	'ALTER TABLE `timeline` ADD COLUMN `related_project` VARCHAR(100) NOT NULL DEFAULT '''' COMMENT ''关联项目名'' AFTER `description`',
	'SELECT ''timeline.related_project 已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS`
	WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'timeline' AND `INDEX_NAME` = 'idx_timeline_event_date');
SET @ddl = IF(@idx = 0,
	'ALTER TABLE `timeline` ADD INDEX `idx_timeline_event_date` (`deleted`, `year`, `event_date`)',
	'SELECT ''idx_timeline_event_date 已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 4. 内容：项目亮点与演示地址
--    只写能对上事实的内容：有部署的才填 demo_url，亮点写清「做了什么」。
-- ---------------------------------------------------------------------
UPDATE `project` SET
	`demo_url` = '',
	`highlights` = '### 核心能力\n- 两个扩展模块：**动效工作台** 与 **AI 演奏工作室**，与主站共用统一响应体、全局异常处理与分层结构\n- 后端 301 个单元测试，数据库迁移脚本可重复执行\n- AI 能力定位为「模型可选」：未配置 key 时走规则判断，配置模型后输出仍由后端校验\n\n### 工程细节\n- 解析（MIDI / 简谱）→ 键位映射 → 演奏计划 → 宏导出，每一段都能单独单测\n- 动效模板 60 个、组合方案 30 套，推荐按五轴加权并给出理由'
WHERE `name` = 'Kingdom Studio' AND `deleted` = 0;

UPDATE `project` SET
	`demo_url` = 'https://jiuba-nu.vercel.app',
	`highlights` = '### 交付内容\n- 源码包与答辩注释版分开发布，注释版按模块标注负责人\n- 建表脚本、种子数据与演示数据脚本齐备，可重复初始化\n\n### 工程要点\n- Controller / Service / Mapper 分层，接口按业务模块划分，统一响应体\n- 当前版本 v1.8（答辩注释版）'
WHERE `name` = '九八 · 校园快递代取订单管理系统' AND `deleted` = 0;

UPDATE `project` SET
	`highlights` = '### 核心能力\n- 简历解析与问答：把非结构化简历转成可检索的结构化信息\n- Prompt 工程实践：结构化提示词 + 多轮评测后再上线\n\n### 技术选型\n- Spring AI + DeepSeek，Vue3 前端，后端保持 Java 技术栈一致'
WHERE `name` = 'Offer Hunter AI · AI 求职助手' AND `deleted` = 0;

UPDATE `project` SET
	`highlights` = '### 核心创新\n- Prompt 生命周期管理：草稿、版本、发布三段式管理\n- AI 辅助优化：用模型对自己的提示词提出改进建议\n- 本地版本管理：不依赖云端即可回滚到任意历史版本\n\n### 技术选型\n- Next.js + React + Supabase，Vitest 做单元测试'
WHERE `name` = 'AI Builder Studio' AND `deleted` = 0;

UPDATE `project` SET
	`highlights` = '### 界面原型\n- Bento 风格信息宫格，中文化规格与文案\n- 手机与宽屏两套视图，同一份数据换布局\n\n### 业务场景\n- 居家环境数据的采集、阈值预警与历史趋势查看'
WHERE `name` = '居家环境感知预警监测系统' AND `deleted` = 0;

INSERT INTO `project` (`name`, `description`, `technology_stack`, `github_url`, `demo_url`, `status`, `progress`, `highlights`, `sort_order`)
SELECT 'WEDA 博客站', '个人博客站：在 Weida Blog 基础上做个人化改造，站点结构、主题配置与部署全部自己维护。',
	'Next.js,TypeScript,Vercel', 'https://github.com/Dongxibie/weida', '', 'COMPLETED', 100,
	'### 已完成\n- 站点结构与主题配置按个人习惯重做，文章与页面数据自己托管\n- 部署链路：GitHub 仓库直连 Vercel，推送即发布', 6
WHERE NOT EXISTS (SELECT 1 FROM `project` WHERE `name` = 'WEDA 博客站' AND `deleted` = 0);

-- ---------------------------------------------------------------------
-- 5. 内容：技术图鉴的项目应用与 DevOps 补录
-- ---------------------------------------------------------------------
UPDATE `technology` SET `used_projects` = 'Kingdom Studio,九八 · 校园快递代取订单管理系统' WHERE `name` = 'Java 21' AND `deleted` = 0;
UPDATE `technology` SET `used_projects` = 'Kingdom Studio,九八 · 校园快递代取订单管理系统,居家环境感知预警监测系统' WHERE `name` = 'Spring Boot 3' AND `deleted` = 0;
UPDATE `technology` SET `used_projects` = 'Kingdom Studio' WHERE `name` = 'Spring Security + JWT' AND `deleted` = 0;
UPDATE `technology` SET `used_projects` = 'Kingdom Studio,九八 · 校园快递代取订单管理系统' WHERE `name` = 'MyBatis Plus' AND `deleted` = 0;
UPDATE `technology` SET `used_projects` = '全部项目' WHERE `name` = 'MySQL 8' AND `deleted` = 0;
UPDATE `technology` SET `used_projects` = 'Kingdom Studio' WHERE `name` = 'Redis' AND `deleted` = 0;
UPDATE `technology` SET `used_projects` = 'Kingdom Studio,九八 · 校园快递代取订单管理系统,居家环境感知预警监测系统' WHERE `name` = 'Vue 3 + TypeScript' AND `deleted` = 0;
UPDATE `technology` SET `used_projects` = 'Kingdom Studio,AI Builder Studio' WHERE `name` = 'Vite' AND `deleted` = 0;
UPDATE `technology` SET `used_projects` = 'Offer Hunter AI · AI 求职助手' WHERE `name` = 'Spring AI / LLM 应用' AND `deleted` = 0;
UPDATE `technology` SET `used_projects` = 'Offer Hunter AI · AI 求职助手,AI Builder Studio' WHERE `name` = 'Prompt Engineering' AND `deleted` = 0;

INSERT INTO `technology` (`name`, `category`, `level`, `description`, `used_projects`, `learn_date`)
SELECT 'Next.js', 'Frontend', 3, 'App Router 与数据获取方式，用于 AI Builder Studio 与博客站的页面开发。', 'AI Builder Studio,WEDA 博客站', '2025-06-01'
WHERE NOT EXISTS (SELECT 1 FROM `technology` WHERE `name` = 'Next.js' AND `deleted` = 0);

INSERT INTO `technology` (`name`, `category`, `level`, `description`, `used_projects`, `learn_date`)
SELECT 'GitHub Actions', 'DevOps', 2, '用工作流把「后端跑测试 + 前端类型检查与构建」变成推送即执行的自动结论。', 'Kingdom Studio', '2026-09-27'
WHERE NOT EXISTS (SELECT 1 FROM `technology` WHERE `name` = 'GitHub Actions' AND `deleted` = 0);

INSERT INTO `technology` (`name`, `category`, `level`, `description`, `used_projects`, `learn_date`)
SELECT 'Vercel 部署', 'DevOps', 3, '静态站点与前端应用的部署链路：仓库直连、推送即发布、按分支预览。', '九八 · 校园快递代取订单管理系统,WEDA 博客站', '2026-09-01'
WHERE NOT EXISTS (SELECT 1 FROM `technology` WHERE `name` = 'Vercel 部署' AND `deleted` = 0);

INSERT INTO `technology` (`name`, `category`, `level`, `description`, `used_projects`, `learn_date`)
SELECT 'Vitest 前端测试', 'Frontend', 2, '给关键流程补组件测试：模板出码、分享码导入、项目详情跳转。', 'Kingdom Studio,AI Builder Studio', '2026-09-27'
WHERE NOT EXISTS (SELECT 1 FROM `technology` WHERE `name` = 'Vitest 前端测试' AND `deleted` = 0);

-- ---------------------------------------------------------------------
-- 6. 内容：成长时间线的精确节点（年度节点保持原样，event_date 留空）
-- ---------------------------------------------------------------------
INSERT INTO `timeline` (`year`, `event_date`, `title`, `description`, `related_project`, `level`, `sort_order`)
SELECT 2026, '2026-09-22', 'Kingdom Studio 立起骨架', '统一响应体、全局异常处理、分层结构与健康检查跑通，主站从零到可访问。', 'Kingdom Studio', 4, 10
WHERE NOT EXISTS (SELECT 1 FROM `timeline` WHERE `title` = 'Kingdom Studio 立起骨架' AND `deleted` = 0);

INSERT INTO `timeline` (`year`, `event_date`, `title`, `description`, `related_project`, `level`, `sort_order`)
SELECT 2026, '2026-09-24', '两个扩展模块落地', '动效工作台与音乐 Agent 从模块自检起步，各自带独立数据表、接口与页面。', 'Kingdom Studio', 4, 20
WHERE NOT EXISTS (SELECT 1 FROM `timeline` WHERE `title` = '两个扩展模块落地' AND `deleted` = 0);

INSERT INTO `timeline` (`year`, `event_date`, `title`, `description`, `related_project`, `level`, `sort_order`)
SELECT 2026, '2026-09-27', 'AI 演奏工作台 v1.2', '难度分层、游戏乐器匹配与曲谱分享上线，演奏工作台统一成黑色 + 紫绿界面。', 'Kingdom Studio', 5, 30
WHERE NOT EXISTS (SELECT 1 FROM `timeline` WHERE `title` = 'AI 演奏工作台 v1.2' AND `deleted` = 0);

INSERT INTO `timeline` (`year`, `event_date`, `title`, `description`, `related_project`, `level`, `sort_order`)
SELECT 2026, '2026-09-27', '主站四个模块补齐', '项目王国、技术图鉴、成长时间线、代码知识库从占位页变成可用的真实模块。', 'Kingdom Studio', 5, 40
WHERE NOT EXISTS (SELECT 1 FROM `timeline` WHERE `title` = '主站四个模块补齐' AND `deleted` = 0);

-- ---------------------------------------------------------------------
-- 7. 内容：代码知识库补一条真实可复用的迁移模板
-- ---------------------------------------------------------------------
INSERT INTO `code_snippet` (`title`, `language`, `description`, `code_content`, `tags`)
SELECT 'MySQL 幂等 ALTER 模板', 'SQL',
	'加列前先查 information_schema，存在就跳过，让迁移脚本可以重复执行。',
	'SET @col = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`\n\tWHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = ''music_task'' AND `COLUMN_NAME` = ''favorite'');\nSET @ddl = IF(@col = 0,\n\t''ALTER TABLE `music_task` ADD COLUMN `favorite` TINYINT NOT NULL DEFAULT 0'',\n\t''SELECT ''''已存在，跳过'''' AS note'');\nPREPARE stmt FROM @ddl;\nEXECUTE stmt;\nDEALLOCATE PREPARE stmt;',
	'MySQL,迁移,幂等,DDL'
WHERE NOT EXISTS (SELECT 1 FROM `code_snippet` WHERE `title` = 'MySQL 幂等 ALTER 模板' AND `deleted` = 0);

-- ---------------------------------------------------------------------
-- 8. project：项目成果三个可选数字
--    代码量 / 测试数量 / Git 提交这三项只在能如实统计出来时才填，
--    不编数：没有的就留 NULL，界面上直接不显示那一格。
-- ---------------------------------------------------------------------
SET @col = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
	WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'project' AND `COLUMN_NAME` = 'code_lines');
SET @ddl = IF(@col = 0,
	'ALTER TABLE `project` ADD COLUMN `code_lines` INT NULL COMMENT ''代码行数（非空行）'' AFTER `progress`',
	'SELECT ''project.code_lines 已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
	WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'project' AND `COLUMN_NAME` = 'test_count');
SET @ddl = IF(@col = 0,
	'ALTER TABLE `project` ADD COLUMN `test_count` INT NULL COMMENT ''自动化测试数量'' AFTER `code_lines`',
	'SELECT ''project.test_count 已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
	WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'project' AND `COLUMN_NAME` = 'commit_count');
SET @ddl = IF(@col = 0,
	'ALTER TABLE `project` ADD COLUMN `commit_count` INT NULL COMMENT ''Git 提交数'' AFTER `test_count`',
	'SELECT ''project.commit_count 已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Kingdom Studio 的三个数字是实测值（2026-09-27）：
-- 后端主代码 15280 行 + 前端 15675 行 = 30955 行（不含空行），测试 333 个，仓库提交 17 次。
UPDATE `project` SET
	`code_lines` = 30955,
	`test_count` = 333,
	`commit_count` = 17
WHERE `name` = 'Kingdom Studio' AND `deleted` = 0;
