package com.kingdomstudio.modules.music.share;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 曲谱分享（对应表 performance_share）。
 *
 * <p>它不是「一条指向本机曲目的链接」，而是一份**自包含快照**：{@link #payload} 里带着
 * 音符、乐器、难度与导出事件。所以别人拿到演奏码，即使自己库里没有这首曲子，
 * 也能完整还原出可演奏的一份 —— 这是分享这件事能成立的前提。
 */
@Data
@TableName("performance_share")
public class PerformanceShare {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String shareCode;

	private Long performancePlanId;

	private Long taskId;

	private String creator;

	private String title;

	/** 难度：星级 + 分层，例如 3 星 · 进阶（普通） */
	private String difficulty;

	private String game;

	private String instrument;

	private Integer noteCount;

	private Integer durationMs;

	private Integer importCount;

	/** 自包含快照 JSON */
	private String payload;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
