-- =====================================================================
-- Kingdom Studio · Music Agent 演奏宏导出（Performance Macro Export）
-- MySQL 8.0.16+ / utf8mb4 / InnoDB
--
-- 只做一件事：新增 performance_plan 表，存「每种乐器的演奏计划」。
-- 既有的 music_task / music_note / instrument_profile 三张表一个字段都不动。
--
-- 计划里的 notes 是按键事件流（按下 / 松开 + 相对时间），它是宏脚本（TXT / AutoHotkey / JSON）
-- 与未来的本机执行通道共同的输入 —— 三个导出格式都由同一份事件流生成，避免各写一套时序。
--
-- 本脚本可重复执行。
-- =====================================================================

USE `kingdom_studio`;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `performance_plan` (
	`id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
	`task_id`      BIGINT       NOT NULL                COMMENT '来源音乐任务（music_task.id）',
	`profile_id`   BIGINT       NOT NULL                COMMENT '生成时使用的乐器档案（instrument_profile.id）',
	`task_name`    VARCHAR(160) NOT NULL DEFAULT ''     COMMENT '冗余存一份曲名：列表展示不必再联表',
	`profile_name` VARCHAR(120) NOT NULL DEFAULT ''     COMMENT '冗余存一份档案名',
	`strategy`     VARCHAR(24)  NOT NULL DEFAULT ''     COMMENT '超范围策略（SINGLE_OCTAVE / NEAREST / SKIP 等）',
	`duration`     INT          NOT NULL DEFAULT 0      COMMENT '整首计划时长（ms，取最后一个事件的时间）',
	`note_count`   INT          NOT NULL DEFAULT 0      COMMENT '事件数（按下 + 松开）',
	`stroke_count` INT          NOT NULL DEFAULT 0      COMMENT '按键次数（和弦算一次）',
	`key_count`    INT          NOT NULL DEFAULT 0      COMMENT '用到的不同按键数量',
	`warnings`     VARCHAR(2000) NOT NULL DEFAULT ''    COMMENT '命令流校验产生的提示（截断 / 顺延 / 跳过），逐条留痕',
	`notes`        LONGTEXT     NOT NULL                COMMENT '按键事件流 JSON：[{key, action:DOWN|UP, timestamp, strokeSeq}]',
	`create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	KEY `idx_performance_plan_task` (`task_id`, `create_time`),
	KEY `idx_performance_plan_profile` (`profile_id`),
	CONSTRAINT `chk_performance_plan_duration` CHECK (`duration` >= 0),
	CONSTRAINT `chk_performance_plan_note_count` CHECK (`note_count` >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Music Agent：演奏计划（按键事件流，供宏导出与本机执行使用）';
