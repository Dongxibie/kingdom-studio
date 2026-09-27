-- =====================================================================
-- Kingdom Studio · Motion Lab 2.0（AI Creator Workbench）结构与迁移脚本
-- MySQL 8.0.16+ / utf8mb4 / InnoDB
--
-- 这一版新增两件事：
--   1. 模板分来源：Official Collection（30 个官方模板）+ Community Collection（30 个社区精选）；
--      社区模板要能标出「灵感来源地址」与「来源许可」，所以 motion_template 补三列；
--   2. 候选池 motion_candidate：GitHub 发现 → 规则分析 → 人工筛选 → 转成 Motion Pattern 的四步都记在这张表里。
--
-- 纪律：只存元数据，不存源码。候选池里永远不会有任何第三方代码，
-- 进资源库的代码一律是 Kingdom Studio 自己实现的 Motion Pattern。
--
-- 本脚本可重复执行（IF NOT EXISTS + information_schema 判断后补列/补约束）。
-- =====================================================================

USE `kingdom_studio`;

SET NAMES utf8mb4;

SET @ks_schema := DATABASE();

-- ---------------------------------------------------------------------
-- 1. motion_template 补三列：触发方式 / 来源地址 / 来源许可
-- ---------------------------------------------------------------------
SET @ks_sql := (
	SELECT IF(COUNT(*) = 0,
		'ALTER TABLE `motion_template` ADD COLUMN `trigger_type` VARCHAR(16) NOT NULL DEFAULT ''load'' COMMENT ''触发方式：load / hover / scroll / click'' AFTER `best_for`',
		'DO 0')
	FROM information_schema.COLUMNS
	WHERE TABLE_SCHEMA = @ks_schema AND TABLE_NAME = 'motion_template' AND COLUMN_NAME = 'trigger_type');
PREPARE ks_stmt FROM @ks_sql;
EXECUTE ks_stmt;
DEALLOCATE PREPARE ks_stmt;

SET @ks_sql := (
	SELECT IF(COUNT(*) = 0,
		'ALTER TABLE `motion_template` ADD COLUMN `source_url` VARCHAR(500) NOT NULL DEFAULT '''' COMMENT ''灵感来源地址（社区精选标注用；官方模板留空）'' AFTER `tags`',
		'DO 0')
	FROM information_schema.COLUMNS
	WHERE TABLE_SCHEMA = @ks_schema AND TABLE_NAME = 'motion_template' AND COLUMN_NAME = 'source_url');
PREPARE ks_stmt FROM @ks_sql;
EXECUTE ks_stmt;
DEALLOCATE PREPARE ks_stmt;

SET @ks_sql := (
	SELECT IF(COUNT(*) = 0,
		'ALTER TABLE `motion_template` ADD COLUMN `source_license` VARCHAR(32) NOT NULL DEFAULT '''' COMMENT ''来源许可（仅记录来源项目的许可，实现代码为 Kingdom Studio 原创）'' AFTER `source_url`',
		'DO 0')
	FROM information_schema.COLUMNS
	WHERE TABLE_SCHEMA = @ks_schema AND TABLE_NAME = 'motion_template' AND COLUMN_NAME = 'source_license');
PREPARE ks_stmt FROM @ks_sql;
EXECUTE ks_stmt;
DEALLOCATE PREPARE ks_stmt;

-- ---------------------------------------------------------------------
-- 2. source 约束放开 COMMUNITY（原来只允许 OFFICIAL / IMPORTED）
-- ---------------------------------------------------------------------
SET @ks_sql := (
	SELECT IF(COUNT(*) > 0,
		'ALTER TABLE `motion_template` DROP CHECK `chk_motion_template_source`',
		'DO 0')
	FROM information_schema.CHECK_CONSTRAINTS
	WHERE CONSTRAINT_SCHEMA = @ks_schema AND CONSTRAINT_NAME = 'chk_motion_template_source'
	  AND CHECK_CLAUSE NOT LIKE '%COMMUNITY%');
PREPARE ks_stmt FROM @ks_sql;
EXECUTE ks_stmt;
DEALLOCATE PREPARE ks_stmt;

SET @ks_sql := (
	SELECT IF(COUNT(*) = 0,
		'ALTER TABLE `motion_template` ADD CONSTRAINT `chk_motion_template_source` CHECK (`source` IN (''OFFICIAL'', ''COMMUNITY'', ''IMPORTED''))',
		'DO 0')
	FROM information_schema.CHECK_CONSTRAINTS
	WHERE CONSTRAINT_SCHEMA = @ks_schema AND CONSTRAINT_NAME = 'chk_motion_template_source');
PREPARE ks_stmt FROM @ks_sql;
EXECUTE ks_stmt;
DEALLOCATE PREPARE ks_stmt;

-- 按来源筛选（官方 / 社区）走这条索引
SET @ks_sql := (
	SELECT IF(COUNT(*) = 0,
		'ALTER TABLE `motion_template` ADD KEY `idx_motion_template_source` (`source`, `deleted`)',
		'DO 0')
	FROM information_schema.STATISTICS
	WHERE TABLE_SCHEMA = @ks_schema AND TABLE_NAME = 'motion_template' AND INDEX_NAME = 'idx_motion_template_source');
PREPARE ks_stmt FROM @ks_sql;
EXECUTE ks_stmt;
DEALLOCATE PREPARE ks_stmt;

-- ---------------------------------------------------------------------
-- 3. motion_candidate 候选池
--    一条候选 = 一个被发现的仓库/案例（只有元数据），以及它被如何处置。
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `motion_candidate` (
	`id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
	`candidate_key`         VARCHAR(120) NOT NULL                COMMENT '稳定标识：owner__repo（归一化小写）',
	`name`                  VARCHAR(120) NOT NULL                COMMENT '仓库/案例名',
	`full_name`             VARCHAR(160) NOT NULL                COMMENT 'owner/repo',
	`source_url`            VARCHAR(500) NOT NULL                COMMENT '来源地址',
	`description`           VARCHAR(600) NOT NULL DEFAULT ''     COMMENT '仓库描述（发现时抓到什么就存什么）',
	`category`              VARCHAR(24)  NOT NULL                COMMENT '建议分类（规则分析得出，可人工改）：七类之一',
	`technology`            VARCHAR(32)  NOT NULL DEFAULT ''     COMMENT '技术线索：CSS / Canvas / Three.js / WebGL / JS 库',
	`trigger_type`          VARCHAR(16)  NOT NULL DEFAULT 'load' COMMENT '触发方式：load / hover / scroll / click',
	`difficulty`            TINYINT      NOT NULL DEFAULT 1      COMMENT '难度 1 入门 / 2 进阶 / 3 高阶',
	`performance_level`     VARCHAR(16)  NOT NULL DEFAULT 'BALANCED' COMMENT '运行档位：LIGHTWEIGHT / BALANCED / GPU_ENHANCED',
	`visual_score`          TINYINT      NOT NULL DEFAULT 80     COMMENT '视觉潜力分（按星数与关键词估算）',
	`license`               VARCHAR(32)  NOT NULL DEFAULT 'NO-LICENSE' COMMENT '来源项目许可（SPDX）',
	`stars`                 INT          NOT NULL DEFAULT 0      COMMENT '发现时的星数',
	`language`              VARCHAR(24)  NOT NULL DEFAULT ''     COMMENT '主要语言',
	`topics`                VARCHAR(400) NOT NULL DEFAULT ''     COMMENT '主题标签，逗号分隔',
	`matched_hints`         VARCHAR(200) NOT NULL DEFAULT ''     COMMENT '命中的分类关键词（留痕，便于解释为什么分到这一类）',
	`prompt`                VARCHAR(600) NOT NULL DEFAULT ''     COMMENT '转换说明：如果要做成 Pattern，该怎么描述（选中后由 Pattern 覆盖）',
	`status`                VARCHAR(16)  NOT NULL DEFAULT 'NEW'  COMMENT 'NEW 待看 / ANALYZED 已分析 / SELECTED 已选入 / PROMOTED 已入库 / REJECTED 已淘汰',
	`review_note`           VARCHAR(300) NOT NULL DEFAULT ''     COMMENT '人工筛选意见（尤其是淘汰理由）',
	`pattern_key`           VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '转成哪个内置 Motion Pattern（Kingdom Studio 自己实现的那份）',
	`promoted_template_key` VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '入库后的模板 key',
	`deleted`               TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 正常 / 1 已删',
	`create_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uk_motion_candidate_key` (`candidate_key`),
	KEY `idx_motion_candidate_status` (`status`, `deleted`),
	KEY `idx_motion_candidate_category` (`category`, `deleted`),
	KEY `idx_motion_candidate_stars` (`stars`),
	CONSTRAINT `chk_motion_candidate_difficulty` CHECK (`difficulty` BETWEEN 1 AND 3),
	CONSTRAINT `chk_motion_candidate_status` CHECK (`status` IN ('NEW', 'ANALYZED', 'SELECTED', 'PROMOTED', 'REJECTED')),
	CONSTRAINT `chk_motion_candidate_performance` CHECK (`performance_level` IN ('LIGHTWEIGHT', 'BALANCED', 'GPU_ENHANCED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Motion Lab 2.0：动效候选池（只存元数据，不存源码）';
