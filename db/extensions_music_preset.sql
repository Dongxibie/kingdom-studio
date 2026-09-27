-- =====================================================================
-- 曲库与演奏方案（产品化增强）
--
--   music_task.favorite               收藏标记（曲库可以只看收藏）
--   music_performance_preset          演奏方案：同一首曲子可以有多套打法
--
--   方案是什么、为什么要有它：同一首曲子在不同场景下的最优打法并不一样 ——
--   演示要原版、教学要简单版（键少、能落下）、短视频要快速版（节奏更紧）。
--   方案把「用哪个乐器档案 + 超范围怎么处理 + 速度倍率 + 最小间隔」存成一条记录，
--   选它就等于把这四个旋钮一次性调好，而不是每次重新点一遍。
--
-- 幂等：可重复执行。ALTER 用 information_schema 判断列是否存在；
--       建表用 CREATE TABLE IF NOT EXISTS。
-- =====================================================================

USE `kingdom_studio`;

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 1. 曲目收藏
-- ---------------------------------------------------------------------
SET @col_exists = (
	SELECT COUNT(*)
	FROM `information_schema`.`COLUMNS`
	WHERE `TABLE_SCHEMA` = DATABASE()
	  AND `TABLE_NAME` = 'music_task'
	  AND `COLUMN_NAME` = 'favorite'
);

SET @ddl = IF(@col_exists = 0,
	'ALTER TABLE `music_task` ADD COLUMN `favorite` TINYINT NOT NULL DEFAULT 0 COMMENT ''收藏标记：1 收藏 / 0 普通'' AFTER `status`',
	'SELECT ''music_task.favorite 已存在，跳过'' AS note');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
	SELECT COUNT(*)
	FROM `information_schema`.`STATISTICS`
	WHERE `TABLE_SCHEMA` = DATABASE()
	  AND `TABLE_NAME` = 'music_task'
	  AND `INDEX_NAME` = 'idx_music_task_favorite'
);

SET @ddl = IF(@idx_exists = 0,
	'ALTER TABLE `music_task` ADD INDEX `idx_music_task_favorite` (`favorite`, `deleted`, `create_time`)',
	'SELECT ''idx_music_task_favorite 已存在，跳过'' AS note');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 2. 演奏方案
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `music_performance_preset` (
	`id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
	`task_id`       BIGINT       NOT NULL                COMMENT '所属曲目',
	`name`          VARCHAR(64)  NOT NULL                COMMENT '方案名，如 原版 / 简单版 / 快速版',
	`profile_id`    BIGINT       NOT NULL                COMMENT '乐器档案 id',
	`strategy`      VARCHAR(24)  NULL                    COMMENT '超范围策略覆盖：SKIP / NEAREST / SHIFT_OCTAVE；为空用档案默认',
	`speed_scale`   DECIMAL(4,2) NOT NULL DEFAULT 1.00   COMMENT '速度倍率：1.00 原速，>1 更快，0.50-2.00',
	`min_gap_ms`    INT          NOT NULL DEFAULT 0      COMMENT '同键重按的最小间隔（毫秒），0 表示不额外加间隔',
	`builtin`       TINYINT      NOT NULL DEFAULT 0      COMMENT '是否内置方案（系统为每首曲子预置的 A/B/C）',
	`note`          VARCHAR(200) NOT NULL DEFAULT ''     COMMENT '一句话说明这套方案适合什么场景',
	`deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
	`create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uk_music_preset_task_name` (`task_id`, `name`),
	KEY `idx_music_preset_task` (`task_id`, `deleted`),
	CONSTRAINT `chk_music_preset_speed` CHECK (`speed_scale` BETWEEN 0.50 AND 2.00),
	CONSTRAINT `chk_music_preset_gap` CHECK (`min_gap_ms` BETWEEN 0 AND 500)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '演奏方案：乐器档案 + 策略 + 速度 + 间隔的组合';
