-- =====================================================================
-- Kingdom Studio · Extensions / Music Agent 建表脚本
-- MySQL 8.0+ / utf8mb4 / InnoDB
--
-- ⚠️ 本脚本只新增扩展模块的三张表，**不会**触碰原有业务表；可以安全地在已有数据的库上
--     重复执行（CREATE TABLE IF NOT EXISTS + 种子乐器档案按 name 唯一键 upsert）。
--
-- 执行方式：
--   mysql -uroot -proot kingdom_studio < db/extensions_music.sql
-- =====================================================================

USE `kingdom_studio`;

-- ---------------------------------------------------------------------
-- 1. music_task 解析任务
--    一次 MIDI 上传或一次简谱粘贴 = 一条任务，音符明细挂在 music_note 上
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `music_task` (
	`id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
	`name`           VARCHAR(160) NOT NULL                COMMENT '任务名称：文件名或用户给的曲子名',
	`source_type`    VARCHAR(16)  NOT NULL                COMMENT '来源：MIDI / JIANPU',
	`source_ref`     VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '来源信息：原始文件名 / 简谱首行',
	`note_count`     INT          NOT NULL DEFAULT 0      COMMENT '音符数量（不含休止）',
	`tempo_bpm`      INT          NOT NULL DEFAULT 120    COMMENT '速度 BPM',
	`time_signature` VARCHAR(16)  NOT NULL DEFAULT '4/4'  COMMENT '拍号',
	`duration_ms`    INT          NOT NULL DEFAULT 0      COMMENT '总时长（毫秒）',
	`pitch_low`      INT          NOT NULL DEFAULT 0      COMMENT '最低音高（MIDI 音高号）',
	`pitch_high`     INT          NOT NULL DEFAULT 0      COMMENT '最高音高（MIDI 音高号）',
	`status`         VARCHAR(16)  NOT NULL DEFAULT 'READY' COMMENT '状态：READY / ARCHIVED',
	`deleted`        TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 正常 / 1 已删',
	`create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	KEY `idx_music_task_source` (`source_type`),
	KEY `idx_music_task_status` (`status`),
	CONSTRAINT `chk_music_task_source` CHECK (`source_type` IN ('MIDI', 'JIANPU', 'SHARE')),
	CONSTRAINT `chk_music_task_status` CHECK (`status` IN ('READY', 'ARCHIVED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '音乐 Agent：解析任务';

-- ---------------------------------------------------------------------
-- 2. music_note 音符明细
--    时间统一换算成毫秒存储：MIDI 的 tick 依赖 tempo 与 PPQ，存 tick 反而不好用
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `music_note` (
	`id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
	`task_id`     BIGINT      NOT NULL                COMMENT '所属任务',
	`seq_no`      INT         NOT NULL DEFAULT 0      COMMENT '序号，从 1 开始',
	`start_ms`    INT         NOT NULL DEFAULT 0      COMMENT '起始时间（毫秒）',
	`duration_ms` INT         NOT NULL DEFAULT 0      COMMENT '持续时长（毫秒）',
	`pitch`       INT         NOT NULL                COMMENT 'MIDI 音高号：60 = C4',
	`note_name`   VARCHAR(8)  NOT NULL DEFAULT ''     COMMENT '音名：C4 / #F5',
	`velocity`    INT         NOT NULL DEFAULT 90     COMMENT '力度 1-127',
	`track_no`    INT         NOT NULL DEFAULT 0      COMMENT '来源轨道序号',
	`deleted`     TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 正常 / 1 已删',
	`create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	KEY `idx_music_note_task` (`task_id`, `seq_no`),
	CONSTRAINT `chk_music_note_pitch` CHECK (`pitch` BETWEEN 0 AND 127)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '音乐 Agent：音符明细';

-- ---------------------------------------------------------------------
-- 3. instrument_profile 乐器按键档案
--    key_layout：按键顺序（从低音到高音）的 JSON 数组，长度即键数
--    base_pitch：第一个键对应的音高；scale 决定相邻键之间是半音还是音阶级进
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `instrument_profile` (
	`id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
	`name`              VARCHAR(96)  NOT NULL                COMMENT '档案名称',
	`instrument`        VARCHAR(96)  NOT NULL DEFAULT ''     COMMENT '乐器/场景说明',
	`mapping_mode`      VARCHAR(16)  NOT NULL                COMMENT '映射方式：CHROMATIC 半音 / DIATONIC 音阶 / CUSTOM 自定义',
	`scale`             VARCHAR(16)  NOT NULL DEFAULT 'MAJOR' COMMENT '音阶：MAJOR / MINOR / PENTATONIC / CHROMATIC',
	`key_layout`        VARCHAR(512) NOT NULL                COMMENT '按键顺序 JSON 数组，如 ["Z","X","C"]',
	`base_pitch`        INT          NOT NULL DEFAULT 60     COMMENT '最低键对应音高',
	`transpose`         INT          NOT NULL DEFAULT 0      COMMENT '移调（半音）',
	`octave_shift`      INT          NOT NULL DEFAULT 0      COMMENT '整体升降八度',
	`unmapped_strategy` VARCHAR(24)  NOT NULL DEFAULT 'SKIP' COMMENT '超范围策略：SKIP 跳过 / NEAREST 就近 / SHIFT_OCTAVE 移八度',
	`description`       VARCHAR(400) NOT NULL DEFAULT ''     COMMENT '说明与使用建议',
	`status`            VARCHAR(16)  NOT NULL DEFAULT 'READY' COMMENT '状态：READY / ARCHIVED',
	`deleted`           TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 正常 / 1 已删',
	`create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uk_instrument_profile_name` (`name`),
	CONSTRAINT `chk_instrument_mode` CHECK (`mapping_mode` IN ('CHROMATIC', 'DIATONIC', 'CUSTOM')),
	CONSTRAINT `chk_instrument_strategy` CHECK (`unmapped_strategy` IN ('SKIP', 'NEAREST', 'SHIFT_OCTAVE')),
	CONSTRAINT `chk_instrument_status` CHECK (`status` IN ('READY', 'ARCHIVED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '音乐 Agent：乐器按键档案';

-- ---------------------------------------------------------------------
-- 4. 种子数据：三个常见场景的按键档案（按 name upsert，重复执行不会产生副本）
-- ---------------------------------------------------------------------
INSERT INTO `instrument_profile`
	(`name`, `instrument`, `mapping_mode`, `scale`, `key_layout`, `base_pitch`, `transpose`, `octave_shift`, `unmapped_strategy`, `description`)
VALUES
	('光遇式 15 键', '手机游戏虚拟乐器（两排十五键）', 'DIATONIC', 'MAJOR',
	 '["Z","X","C","V","B","N","M","A","S","D","F","G","H","J","K"]', 60, 0, 0, 'NEAREST',
	 '两排十五键按自然音阶排列：上排七个音、下排八个音，只有白键没有黑键，所以遇到变化音只能就近落到音阶音上。'),
	('键盘式 12 键', '电脑键盘半音排列', 'CHROMATIC', 'CHROMATIC',
	 '["Z","S","X","D","C","V","G","B","H","N","J","M"]', 60, 0, 0, 'SKIP',
	 '十二个键对应一个八度内的十二个半音，任何音都能原样落键；超出范围就跳过并在导出里标出来。'),
	('八音盒 8 键', '单八度小乐器', 'CUSTOM', 'MAJOR',
	 '["A","S","D","F","G","H","J","K"]', 60, 0, 0, 'SHIFT_OCTAVE',
	 '只有一个八度的八个音，旋律超范围时自动往上或往下挪八度，尽量让整首曲子能完整弹下来。')
ON DUPLICATE KEY UPDATE
	`instrument` = VALUES(`instrument`),
	`mapping_mode` = VALUES(`mapping_mode`),
	`scale` = VALUES(`scale`),
	`key_layout` = VALUES(`key_layout`),
	`base_pitch` = VALUES(`base_pitch`),
	`transpose` = VALUES(`transpose`),
	`octave_shift` = VALUES(`octave_shift`),
	`unmapped_strategy` = VALUES(`unmapped_strategy`),
	`description` = VALUES(`description`);
