package com.kingdomstudio.modules.music.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 乐器按键档案实体（对应表 instrument_profile）。
 *
 * <p>{@code keyLayout} 存按键顺序的 JSON 数组（从低音到高音），长度就是键数；
 * 相邻两键差半音还是差一个音级，由 {@code mappingMode} + {@code scale} 决定。
 */
@Data
@TableName("instrument_profile")
public class InstrumentProfile {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String name;

	private String instrument;

	/** 所属游戏：通用 / 光遇 Sky / Minecraft / 三角洲行动 / 自定义 */
	private String game;

	/** CHROMATIC / DIATONIC / CUSTOM */
	private String mappingMode;

	/** MAJOR / MINOR / PENTATONIC / CHROMATIC */
	private String scale;

	/** 按键顺序 JSON 数组，如 ["Z","X","C"] */
	private String keyLayout;

	/** 最低键对应音高 */
	private Integer basePitch;

	/** 移调（半音） */
	private Integer transpose;

	/** 整体升降八度 */
	private Integer octaveShift;

	/** 超范围策略：SKIP / NEAREST / SHIFT_OCTAVE */
	private String unmappedStrategy;

	/** 覆盖音域，例如 F#3–F#5；留空表示按键位与基准音推导 */
	private String octaveRange;

	private String description;

	/** 这个游戏乐器的特殊规则：长按是否支持、和弦上限、切换方式等 */
	private String specialRules;

	private String status;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
