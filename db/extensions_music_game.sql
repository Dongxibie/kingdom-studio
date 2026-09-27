-- =====================================================================
-- 游戏乐器生态 + 曲谱分享（V1.2）
--
--   instrument_profile 扩字段：game / octave_range / special_rules
--     —— 把「乐器配置」升级成「游戏乐器档案」：同一套键位在不同游戏里含义不同
--        （光遇的 15 键、Minecraft 的音符盒、三角洲行动的口风琴……），
--        游戏名与特殊规则必须能落库、能筛选、能在界面上讲清楚。
--
--   performance_share  曲谱分享：把一份演奏方案变成一串可转述的分享码
--     —— payload 里存**自包含快照**（音符、乐器、难度、导出事件），
--        别人拿到码就能在自己的库里还原出一首可演奏的曲子，不依赖原作者数据库。
--
-- 幂等：ALTER 用 information_schema 判断；建表用 CREATE TABLE IF NOT EXISTS；
--       档案补字段用按 name 的 upsert。
-- =====================================================================

USE `kingdom_studio`;

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 1. instrument_profile 扩三个字段
-- ---------------------------------------------------------------------
SET @col = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
	WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'instrument_profile' AND `COLUMN_NAME` = 'game');
SET @ddl = IF(@col = 0,
	'ALTER TABLE `instrument_profile` ADD COLUMN `game` VARCHAR(48) NOT NULL DEFAULT ''通用'' COMMENT ''所属游戏：通用 / 光遇 Sky / Minecraft / 三角洲行动 / 自定义'' AFTER `instrument`',
	'SELECT ''instrument_profile.game 已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
	WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'instrument_profile' AND `COLUMN_NAME` = 'octave_range');
SET @ddl = IF(@col = 0,
	'ALTER TABLE `instrument_profile` ADD COLUMN `octave_range` VARCHAR(24) NOT NULL DEFAULT '''' COMMENT ''覆盖音域，例如 F#3–F#5；留空表示按键位与基准音推导'' AFTER `octave_shift`',
	'SELECT ''instrument_profile.octave_range 已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
	WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'instrument_profile' AND `COLUMN_NAME` = 'special_rules');
SET @ddl = IF(@col = 0,
	'ALTER TABLE `instrument_profile` ADD COLUMN `special_rules` VARCHAR(300) NOT NULL DEFAULT '''' COMMENT ''这个游戏乐器的特殊规则：长按是否支持、和弦上限、切换方式等'' AFTER `description`',
	'SELECT ''instrument_profile.special_rules 已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS`
	WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'instrument_profile' AND `INDEX_NAME` = 'idx_instrument_game');
SET @ddl = IF(@idx = 0,
	'ALTER TABLE `instrument_profile` ADD INDEX `idx_instrument_game` (`game`, `deleted`)',
	'SELECT ''idx_instrument_game 已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 2. 给既有三套档案补上游戏归属与特殊规则
--    光遇的 15 键是真实存在的游戏乐器，这里如实标注；
--    键盘式与八音盒是通用工具，game 保持「通用」。
-- ---------------------------------------------------------------------
UPDATE `instrument_profile`
SET `game` = '光遇 Sky',
	`octave_range` = 'C4–C6',
	`special_rules` = '只有白键：遇到变化音只能就近落到音阶音上；同一时刻支持多键（和弦），但连续同键要留间隔。'
WHERE `name` = '光遇式 15 键' AND `deleted` = 0;

UPDATE `instrument_profile`
SET `octave_range` = 'C4–B4',
	`special_rules` = '一个八度内的十二个半音，任何音都能原样落键；超出范围直接跳过并在导出里标出来。'
WHERE `name` = '键盘式 12 键' AND `deleted` = 0;

UPDATE `instrument_profile`
SET `octave_range` = 'C4–C5',
	`special_rules` = '只有一个八度：旋律超范围时自动挪八度，尽量让整首曲子完整弹下来。'
WHERE `name` = '八音盒 8 键' AND `deleted` = 0;

-- ---------------------------------------------------------------------
-- 3. 新增一个游戏乐器档案：Minecraft 音符盒（25 键，示例布局）
--    「按键」是音符盒的编号顺序（从低音到高音），实际触发方式交给执行层；
--    special_rules 里写明这是示例布局，可以按自己的世界改。
-- ---------------------------------------------------------------------
INSERT INTO `instrument_profile`
	(`name`, `instrument`, `game`, `mapping_mode`, `scale`, `key_layout`, `base_pitch`, `transpose`,
	 `octave_shift`, `octave_range`, `unmapped_strategy`, `description`, `special_rules`)
VALUES
	('Minecraft 音符盒 25 键', '音符盒（Note Block）', 'Minecraft', 'CHROMATIC', 'CHROMATIC',
	 '["1","2","3","4","5","6","7","8","9","0","Q","W","E","R","T","Y","U","I","O","P","A","S","D","F","G"]',
	 54, 0, 0, 'F#3–F#5', 'NEAREST',
	 '音符盒每升高一个音需要一层方块，25 个音正好覆盖两个八度：键位表按音高从低到高排列。',
	 '示例布局：这里的「按键」是音符盒的音高编号顺序，实际触发方式（点击 / 指令 / 红石）由执行层决定，可以按自己的世界改键位。')
ON DUPLICATE KEY UPDATE
	`instrument` = VALUES(`instrument`),
	`game` = VALUES(`game`),
	`mapping_mode` = VALUES(`mapping_mode`),
	`scale` = VALUES(`scale`),
	`key_layout` = VALUES(`key_layout`),
	`base_pitch` = VALUES(`base_pitch`),
	`octave_range` = VALUES(`octave_range`),
	`unmapped_strategy` = VALUES(`unmapped_strategy`),
	`description` = VALUES(`description`),
	`special_rules` = VALUES(`special_rules`);

-- ---------------------------------------------------------------------
-- 4. performance_share 曲谱分享
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `performance_share` (
	`id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
	`share_code`          VARCHAR(32)  NOT NULL                COMMENT '分享码，例如 KS-MUSIC-2026-A001',
	`performance_plan_id` BIGINT       NULL                    COMMENT '来源演奏计划 id（原作者的库，可为空）',
	`task_id`             BIGINT       NULL                    COMMENT '来源曲目 id（原作者的库，可为空）',
	`creator`             VARCHAR(48)  NOT NULL DEFAULT '匿名' COMMENT '分享者署名',
	`title`               VARCHAR(120) NOT NULL DEFAULT ''     COMMENT '曲目名',
	`difficulty`          VARCHAR(24)  NOT NULL DEFAULT ''     COMMENT '难度：星级 + 分层文字',
	`game`                VARCHAR(48)  NOT NULL DEFAULT ''     COMMENT '目标游戏乐器（便于检索）',
	`instrument`          VARCHAR(96)  NOT NULL DEFAULT ''     COMMENT '乐器名',
	`note_count`          INT          NOT NULL DEFAULT 0      COMMENT '音符数',
	`duration_ms`         INT          NOT NULL DEFAULT 0      COMMENT '演奏时长（毫秒）',
	`import_count`        INT          NOT NULL DEFAULT 0      COMMENT '被导入次数',
	`payload`             LONGTEXT     NOT NULL                COMMENT '自包含快照：音符 + 乐器 + 难度 + 事件，别人导入即可还原',
	`deleted`             TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
	`create_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uk_performance_share_code` (`share_code`),
	KEY `idx_performance_share_created` (`deleted`, `create_time`),
	KEY `idx_performance_share_game` (`game`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '音乐 Agent：曲谱分享';

-- ---------------------------------------------------------------------
-- 5. 曲目来源放开第三种：SHARE（由演奏码导入）
--    分享导入是「复制一份到我的库里」，来源类型如实标成 SHARE，
--    界面上就能看出这首是别人分享来的，而不是自己上传的。
-- ---------------------------------------------------------------------
-- 注意：CHECK_CONSTRAINTS 里没有表名，要 JOIN TABLE_CONSTRAINTS 才能按表定位
SET @src_def = (
	SELECT cc.`CHECK_CLAUSE`
	FROM `information_schema`.`CHECK_CONSTRAINTS` cc
	JOIN `information_schema`.`TABLE_CONSTRAINTS` tc
	  ON tc.`CONSTRAINT_SCHEMA` = cc.`CONSTRAINT_SCHEMA`
	 AND tc.`CONSTRAINT_NAME` = cc.`CONSTRAINT_NAME`
	WHERE cc.`CONSTRAINT_SCHEMA` = DATABASE()
	  AND tc.`TABLE_NAME` = 'music_task'
	  AND cc.`CONSTRAINT_NAME` = 'chk_music_task_source'
	LIMIT 1
);

SET @ddl = IF(@src_def IS NOT NULL AND @src_def NOT LIKE '%SHARE%',
	'ALTER TABLE `music_task` DROP CHECK `chk_music_task_source`',
	'SELECT ''source_type 约束已放开，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has = (
	SELECT COUNT(*)
	FROM `information_schema`.`TABLE_CONSTRAINTS`
	WHERE `CONSTRAINT_SCHEMA` = DATABASE()
	  AND `TABLE_NAME` = 'music_task'
	  AND `CONSTRAINT_NAME` = 'chk_music_task_source'
);
SET @ddl = IF(@has = 0,
	'ALTER TABLE `music_task` ADD CONSTRAINT `chk_music_task_source` CHECK (`source_type` IN (''MIDI'', ''JIANPU'', ''SHARE''))',
	'SELECT ''source_type 约束已存在，跳过'' AS note');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
