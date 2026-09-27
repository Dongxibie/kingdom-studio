package com.kingdomstudio.modules.music.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 演奏方案（对应表 music_performance_preset）。
 *
 * <p>同一首曲子在不同场景下的最优打法并不一样：演示要原版、教学要简单版（键少、都能落下）、
 * 短视频要快速版（节奏更紧）。方案把四个旋钮存成一条记录 ——
 * 用哪个乐器档案、超范围怎么处理、速度倍率、同键重按的最小间隔。
 */
@Data
@TableName("music_performance_preset")
public class MusicPerformancePreset {

	@TableId(type = IdType.AUTO)
	private Long id;

	private Long taskId;

	private String name;

	private Long profileId;

	/** 超范围策略覆盖：SKIP / NEAREST / SHIFT_OCTAVE；为空用档案默认 */
	private String strategy;

	/** 速度倍率：1.00 原速，>1 更快 */
	private BigDecimal speedScale;

	/** 同键重按的最小间隔（毫秒），0 表示不额外加 */
	private Integer minGapMs;

	/** 是否系统为每首曲子预置的方案 */
	private Integer builtin;

	private String note;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
