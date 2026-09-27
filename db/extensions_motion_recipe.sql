-- =====================================================================
-- Motion Recipe 2.0 结构变更：组合方案支持「步骤」
--
--   motion_recipe.steps  组合步骤 JSON 数组，按应用顺序：
--     [{"templateKey":"mesh-gradient","stage":"背景","role":"网格渐变打底，科技感来自结构"}, ...]
--
--   为什么要加这一列：v1.1 的组合方案只记了「包含哪些模板」，界面能做的只有列出成员；
--   真实网页的组合是有顺序、有分工的（谁铺底、谁给纵深、谁收尾），所以把「作用」也变成数据。
--
-- 幂等：本脚本可重复执行。用 information_schema 判断列是否已存在，
--       已存在时不重复 ALTER（MySQL 没有 ADD COLUMN IF NOT EXISTS）。
-- 全新安装：db/extensions_motion_template.sql 的建表语句里已经包含这一列，本脚本对新库是空操作。
-- =====================================================================

USE `kingdom_studio`;

SET NAMES utf8mb4;

SET @col_exists = (
	SELECT COUNT(*)
	FROM `information_schema`.`COLUMNS`
	WHERE `TABLE_SCHEMA` = DATABASE()
	  AND `TABLE_NAME` = 'motion_recipe'
	  AND `COLUMN_NAME` = 'steps'
);

SET @ddl = IF(@col_exists = 0,
	'ALTER TABLE `motion_recipe` ADD COLUMN `steps` LONGTEXT NULL COMMENT ''组合步骤 JSON：[{templateKey, stage, role}]，按应用顺序；为空时回退到 template_keys''',
	'SELECT ''motion_recipe.steps 已存在，跳过'' AS note');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
