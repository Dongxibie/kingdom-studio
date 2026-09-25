-- =====================================================================
-- Kingdom Studio · Extensions / Motion Lab 建表脚本
-- MySQL 8.0+ / utf8mb4 / InnoDB
--
-- ⚠️ 本脚本只新增扩展模块的两张表，**不会**触碰原有 5 张业务表，
--     因此可以安全地在已有数据的库上重复执行（CREATE TABLE IF NOT EXISTS +
--     种子数据按 content_hash 唯一键 upsert）。
--
-- 执行方式：
--   mysql -uroot -proot kingdom_studio < db/extensions_motion.sql
-- =====================================================================

USE `kingdom_studio`;

-- ---------------------------------------------------------------------
-- 1. motion_resource 动效资源表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `motion_resource` (
	`id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
	`name`         VARCHAR(160) NOT NULL                COMMENT '动效名称',
	`description`  VARCHAR(600) NOT NULL DEFAULT ''     COMMENT '一句话说明',
	`category`     VARCHAR(32)  NOT NULL                COMMENT '分类：Entrance / Hover / Scroll / Text / Particle / 3D / Glass / Cursor / Background / Loading',
	`technology`   VARCHAR(48)  NOT NULL DEFAULT ''     COMMENT '技术栈：CSS / Canvas / Three.js / GSAP ...',
	`source_url`   VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '来源页面地址',
	`repo_url`     VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '仓库地址（可空）',
	`preview_url`  VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '预览地址或预览标记',
	`tags`         VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '标签，英文逗号分隔',
	`license`      VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '开源许可，展示时必须标注来源',
	`code_path`    VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '代码在仓库中的位置',
	`content_hash` CHAR(40)     NOT NULL DEFAULT ''     COMMENT '内容哈希，用于去重',
	`status`       VARCHAR(16)  NOT NULL DEFAULT 'READY' COMMENT '状态：DRAFT / READY / ARCHIVED',
	`deleted`      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
	`create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uk_motion_resource_hash` (`content_hash`),
	KEY `idx_motion_resource_category` (`category`),
	KEY `idx_motion_resource_technology` (`technology`),
	KEY `idx_motion_resource_status` (`status`),
	CONSTRAINT `chk_motion_resource_category` CHECK (`category` IN
		('Entrance', 'Hover', 'Scroll', 'Text', 'Particle', '3D', 'Glass', 'Cursor', 'Background', 'Loading')),
	CONSTRAINT `chk_motion_resource_status` CHECK (`status` IN ('DRAFT', 'READY', 'ARCHIVED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '动效资源表（Motion Lab）';

-- ---------------------------------------------------------------------
-- 2. motion_code 动效代码表（一条资源对应一条代码记录）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `motion_code` (
	`id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
	`motion_id`   BIGINT   NOT NULL                COMMENT '关联 motion_resource.id',
	`prompt`      TEXT     NULL                    COMMENT '生成该动效的提示词',
	`vue_code`    MEDIUMTEXT NULL                  COMMENT 'Vue 3 代码',
	`react_code`  MEDIUMTEXT NULL                  COMMENT 'React 代码',
	`css_code`    MEDIUMTEXT NULL                  COMMENT '纯 CSS 代码',
	`three_code`  MEDIUMTEXT NULL                  COMMENT 'Three.js 代码',
	`deleted`     TINYINT  NOT NULL DEFAULT 0      COMMENT '逻辑删除',
	`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	KEY `idx_motion_code_motion` (`motion_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '动效代码表（Motion Lab）';
