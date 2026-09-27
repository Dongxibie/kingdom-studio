package com.kingdomstudio.modules.music.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 演奏计划（对应表 performance_plan）。
 *
 * <p>一条计划 = 一份「按键事件流」：每个事件是「某个键在某个时刻按下 / 松开」。
 * 三种导出（TXT / AutoHotkey / JSON）与本机执行通道都从这同一份事件流生成，
 * 时序只有一处定义，不会出现「txt 一个节奏、脚本又一个节奏」的情况。
 *
 * <p>与既有的 {@code music_note} 分开：音符是「音乐事实」（音高、时值），
 * 事件流是「演奏动作」（哪个键、什么时候按/松）—— 一个音可能对应一次按键，
 * 也可能因为乐器音域被挪八度、跳过，甚至一个和弦对应多次同时按下。
 */
@Data
@TableName("performance_plan")
public class PerformancePlan {

	@TableId(type = IdType.AUTO)
	private Long id;

	/** 来源音乐任务 */
	private Long taskId;

	/** 生成时使用的乐器档案 */
	private Long profileId;

	/** 冗余存一份曲名与档案名：列表展示不必再联表 */
	private String taskName;
	private String profileName;

	/** 生成时的超范围策略 */
	private String strategy;

	/** 整首计划时长（ms，取最后一个事件的时间） */
	private Integer duration;

	/** 事件数（按下 + 松开） */
	private Integer noteCount;

	/** 按键次数（和弦算一次） */
	private Integer strokeCount;

	/** 用到的不同按键数量 */
	private Integer keyCount;

	/** 命令流校验的提示，逐条留痕（换行分隔） */
	private String warnings;

	/** 事件流 JSON：[{key, action, timestamp, strokeSeq}] */
	private String notes;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
