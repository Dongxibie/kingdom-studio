-- =====================================================================
-- Kingdom Studio · Motion Lab 升级（v1.1.0）建表脚本
-- MySQL 8.0.16+ / utf8mb4 / InnoDB
--
-- 三张新表：
--   motion_template  官方模板（含五类产物 + 推荐指数四项子分 + 可调参数）
--   motion_recipe    动效组合方案（把多个模板拼成一个场景级方案）
--   motion_rating    评分与理由（人对模板/组合的复核记录）
--
-- 与既有 motion_resource / motion_code 并存，互不影响：
--   motion_resource 是「采集与手建资源」，motion_template 是「官方模板与组合素材」。
--   本脚本可重复执行（CREATE TABLE IF NOT EXISTS + 种子按 template_key 唯一键 upsert）。
-- =====================================================================

USE `kingdom_studio`;

-- 连接字符集按 utf8mb4 显式设置：Windows 下 mysql 客户端默认可能是 GBK，
-- 那样导入含中文的脚本会报 Incorrect string value，这里先把它定死，脚本换台机器也能直接跑。
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 1. motion_template 官方动效模板
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `motion_template` (
	`id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
	`template_key`  VARCHAR(64)  NOT NULL                COMMENT '稳定标识（英文短横线），组合方案与前端用它引用，不随 id 变化',
	`name`          VARCHAR(120) NOT NULL                COMMENT '模板名',
	`name_en`       VARCHAR(120) NOT NULL DEFAULT ''     COMMENT '英文名，展示与检索用',
	`description`   VARCHAR(400) NOT NULL DEFAULT ''     COMMENT '一句话说明这个动效是什么、什么时候用',
	`category`      VARCHAR(24)  NOT NULL                COMMENT '分组：基础交互 / 产品页面 / 高级效果',
	`scene`         VARCHAR(32)  NOT NULL                COMMENT '主要适用场景：Landing Page / Dashboard / Portfolio / Login / AI SaaS / Game UI',
	`style`         VARCHAR(24)  NOT NULL                COMMENT '风格：Minimal / Luxury / Cyber / Glass / Organic',
	`technology`    VARCHAR(32)  NOT NULL                COMMENT '技术：CSS / GSAP / Framer Motion / Three.js / Canvas',
	`difficulty`    TINYINT      NOT NULL DEFAULT 1      COMMENT '难度 1 入门 / 2 进阶 / 3 高阶',
	`best_for`      VARCHAR(160) NOT NULL DEFAULT ''     COMMENT '适合场景，逗号分隔（官网首页、后台系统、个人作品集…）',
	`score_visual`  TINYINT      NOT NULL DEFAULT 80     COMMENT '视觉效果 0-100（权重 30%）',
	`score_code`    TINYINT      NOT NULL DEFAULT 80     COMMENT '代码质量 0-100（权重 25%）',
	`score_reuse`   TINYINT      NOT NULL DEFAULT 80     COMMENT '复用价值 0-100（权重 25%）',
	`score_perf`    TINYINT      NOT NULL DEFAULT 80     COMMENT '性能表现 0-100（权重 20%）',
	`score`         TINYINT      NOT NULL DEFAULT 80     COMMENT '推荐指数 = 加权合成（由四项子分算出，不在代码里手写）',
	`runtime_tier`  VARCHAR(16)  NOT NULL DEFAULT 'BALANCED' COMMENT '运行档位：LIGHTWEIGHT 轻量 / BALANCED 均衡 / GPU_ENHANCED 依赖 GPU 加速',
	`runtime_note`  VARCHAR(300) NOT NULL DEFAULT ''     COMMENT '运行建议：这一档的代价在哪、怎么降级（空则按档位给通用建议）',
	`params`        VARCHAR(1200) NOT NULL DEFAULT '[]'  COMMENT '可调参数 JSON：[{key,label,unit,min,max,step,default}]，右侧「参数」页签据此生成控件',
	`preview_url`   VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '外部预览地址（官方模板留空，用内置预览）',
	`preview_html`  MEDIUMTEXT   NOT NULL                COMMENT '预览用的 DOM 结构（模板自带，代码生成也复用它）',
	`preview_js`    MEDIUMTEXT   NOT NULL                COMMENT '预览用的脚本（仅需要交互的模板非空）',
	`css_code`      MEDIUMTEXT   NOT NULL                COMMENT '样式实现（可调参数以 CSS 变量暴露）',
	`vue_code`      MEDIUMTEXT   NOT NULL                COMMENT 'Vue 3 单文件组件',
	`react_code`    MEDIUMTEXT   NOT NULL                COMMENT 'React 组件',
	`three_code`    MEDIUMTEXT   NOT NULL                COMMENT 'Three.js 实现（仅三维/粒子类模板非空）',
	`prompt`        MEDIUMTEXT   NOT NULL                COMMENT '给 AI 的描述（Prompt），用于在别的项目里复现',
	`tags`          VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '检索标签，逗号分隔',
	`source`        VARCHAR(16)  NOT NULL DEFAULT 'OFFICIAL' COMMENT '来源：OFFICIAL 官方模板 / IMPORTED 采集导入',
	`status`        VARCHAR(16)  NOT NULL DEFAULT 'READY' COMMENT '状态：READY / DRAFT / ARCHIVED',
	`deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 正常 / 1 已删',
	`create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uk_motion_template_key` (`template_key`),
	KEY `idx_motion_template_scene` (`scene`, `deleted`),
	KEY `idx_motion_template_style` (`style`, `deleted`),
	KEY `idx_motion_template_technology` (`technology`, `deleted`),
	KEY `idx_motion_template_score` (`score`),
	CONSTRAINT `chk_motion_template_difficulty` CHECK (`difficulty` BETWEEN 1 AND 3),
	CONSTRAINT `chk_motion_template_score` CHECK (`score` BETWEEN 0 AND 100),
	CONSTRAINT `chk_motion_template_status` CHECK (`status` IN ('READY', 'DRAFT', 'ARCHIVED')),
	CONSTRAINT `chk_motion_template_source` CHECK (`source` IN ('OFFICIAL', 'IMPORTED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Motion Lab：官方动效模板';

-- ---------------------------------------------------------------------
-- 1.1 老库补列：运行档位（v1.1 起新增）
--     CREATE TABLE IF NOT EXISTS 不会改动已存在的表，所以这里单独补一次。
--     MySQL 8 没有 ADD COLUMN IF NOT EXISTS，用 information_schema 判断后执行，
--     保证本脚本仍然可以重复跑。
-- ---------------------------------------------------------------------
SET @ks_schema := DATABASE();

SET @ks_sql := (
	SELECT IF(COUNT(*) = 0,
		'ALTER TABLE `motion_template` ADD COLUMN `runtime_tier` VARCHAR(16) NOT NULL DEFAULT ''BALANCED'' COMMENT ''运行档位：LIGHTWEIGHT 轻量 / BALANCED 均衡 / GPU_ENHANCED 依赖 GPU 加速'' AFTER `score`',
		'DO 0')
	FROM information_schema.COLUMNS
	WHERE TABLE_SCHEMA = @ks_schema AND TABLE_NAME = 'motion_template' AND COLUMN_NAME = 'runtime_tier');
PREPARE ks_stmt FROM @ks_sql;
EXECUTE ks_stmt;
DEALLOCATE PREPARE ks_stmt;

SET @ks_sql := (
	SELECT IF(COUNT(*) = 0,
		'ALTER TABLE `motion_template` ADD COLUMN `runtime_note` VARCHAR(300) NOT NULL DEFAULT '''' COMMENT ''运行建议：这一档的代价在哪、怎么降级'' AFTER `runtime_tier`',
		'DO 0')
	FROM information_schema.COLUMNS
	WHERE TABLE_SCHEMA = @ks_schema AND TABLE_NAME = 'motion_template' AND COLUMN_NAME = 'runtime_note');
PREPARE ks_stmt FROM @ks_sql;
EXECUTE ks_stmt;
DEALLOCATE PREPARE ks_stmt;

SET @ks_sql := (
	SELECT IF(COUNT(*) = 0,
		'ALTER TABLE `motion_template` ADD COLUMN `preview_html` MEDIUMTEXT NOT NULL COMMENT ''预览用的 DOM 结构（模板自带，代码生成也复用它）'' AFTER `preview_url`',
		'DO 0')
	FROM information_schema.COLUMNS
	WHERE TABLE_SCHEMA = @ks_schema AND TABLE_NAME = 'motion_template' AND COLUMN_NAME = 'preview_html');
PREPARE ks_stmt FROM @ks_sql;
EXECUTE ks_stmt;
DEALLOCATE PREPARE ks_stmt;

SET @ks_sql := (
	SELECT IF(COUNT(*) = 0,
		'ALTER TABLE `motion_template` ADD COLUMN `preview_js` MEDIUMTEXT NOT NULL COMMENT ''预览用的脚本（仅需要交互的模板非空）'' AFTER `preview_html`',
		'DO 0')
	FROM information_schema.COLUMNS
	WHERE TABLE_SCHEMA = @ks_schema AND TABLE_NAME = 'motion_template' AND COLUMN_NAME = 'preview_js');
PREPARE ks_stmt FROM @ks_sql;
EXECUTE ks_stmt;
DEALLOCATE PREPARE ks_stmt;

-- 按运行档位筛选（左栏「按性能成本」）走这条索引
SET @ks_sql := (
	SELECT IF(COUNT(*) = 0,
		'ALTER TABLE `motion_template` ADD KEY `idx_motion_template_runtime_tier` (`runtime_tier`, `deleted`)',
		'DO 0')
	FROM information_schema.STATISTICS
	WHERE TABLE_SCHEMA = @ks_schema AND TABLE_NAME = 'motion_template' AND INDEX_NAME = 'idx_motion_template_runtime_tier');
PREPARE ks_stmt FROM @ks_sql;
EXECUTE ks_stmt;
DEALLOCATE PREPARE ks_stmt;

-- ---------------------------------------------------------------------
-- 2. motion_recipe 动效组合方案
--    一个方案 = 若干模板按顺序叠出来的效果，例如 Hero Entrance = 渐变背景 + 粒子 + 文字揭示 + 按钮
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `motion_recipe` (
	`id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
	`recipe_key`    VARCHAR(64)  NOT NULL                COMMENT '稳定标识',
	`name`          VARCHAR(120) NOT NULL                COMMENT '方案名，如 Premium Hero',
	`description`   VARCHAR(600) NOT NULL DEFAULT ''     COMMENT '这个组合在表达什么、为什么这么搭',
	`scene`         VARCHAR(32)  NOT NULL                COMMENT '主要场景',
	`style`         VARCHAR(24)  NOT NULL DEFAULT ''     COMMENT '风格',
	`best_for`      VARCHAR(160) NOT NULL DEFAULT ''     COMMENT '适合场景，逗号分隔',
	`score`         TINYINT      NOT NULL DEFAULT 80     COMMENT '推荐指数 = 组成模板的加权平均（由服务端算出）',
	`template_keys` VARCHAR(600) NOT NULL DEFAULT '[]'   COMMENT '包含的模板 template_key JSON 数组，按应用顺序排列',
	`steps`         LONGTEXT     NULL                   COMMENT '组合步骤 JSON：[{templateKey, stage, role}]，按应用顺序；为空时回退到 template_keys',
	`prompt`        MEDIUMTEXT   NOT NULL               COMMENT '整套组合的 Prompt（比单模板更完整的一句话需求）',
	`status`        VARCHAR(16)  NOT NULL DEFAULT 'READY' COMMENT '状态：READY / DRAFT / ARCHIVED',
	`deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
	`create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uk_motion_recipe_key` (`recipe_key`),
	KEY `idx_motion_recipe_scene` (`scene`, `deleted`),
	CONSTRAINT `chk_motion_recipe_score` CHECK (`score` BETWEEN 0 AND 100),
	CONSTRAINT `chk_motion_recipe_status` CHECK (`status` IN ('READY', 'DRAFT', 'ARCHIVED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Motion Lab：动效组合方案';

-- ---------------------------------------------------------------------
-- 3. motion_rating 评分与理由
--    模板的推荐指数是系统按四项子分算的；这张表记录「人对它的复核评分与理由」，
--    两者分开：一个是算法口径，一个是判断依据，避免互相污染。
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `motion_rating` (
	`id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
	`target_type` VARCHAR(16) NOT NULL                COMMENT '评分对象类型：TEMPLATE / RECIPE',
	`target_key`  VARCHAR(64) NOT NULL                COMMENT '对象的 template_key / recipe_key',
	`score`       TINYINT     NOT NULL                COMMENT '人工评分 1-5 星',
	`reason`      VARCHAR(400) NOT NULL DEFAULT ''    COMMENT '打分理由（为什么是这个分）',
	`deleted`     TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除',
	`create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uk_motion_rating_target` (`target_type`, `target_key`),
	CONSTRAINT `chk_motion_rating_score` CHECK (`score` BETWEEN 1 AND 5),
	CONSTRAINT `chk_motion_rating_type` CHECK (`target_type` IN ('TEMPLATE', 'RECIPE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Motion Lab：评分与理由';
