package com.kingdomstudio.modules.music.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 音符明细实体（对应表 music_note）。 */
@Data
@TableName("music_note")
public class MusicNote {

	@TableId(type = IdType.AUTO)
	private Long id;

	private Long taskId;

	/** 从 1 开始；按起始时间排序后重新编号 */
	private Integer seqNo;

	private Integer startMs;

	private Integer durationMs;

	/** MIDI 音高号：60 = 中央 C */
	private Integer pitch;

	/** 音名，如 C4 / #F5；由 Service 统一算好存下来，前端不必再算一遍 */
	private String noteName;

	private Integer velocity;

	private Integer trackNo;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
