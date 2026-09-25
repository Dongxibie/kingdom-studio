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
 * 音乐解析任务实体（对应表 music_task）。
 *
 * <p>字段与 {@code db/extensions_music.sql} 一一对应；时间统一存毫秒，
 * 不存 MIDI 的 tick —— tick 的含义依赖 tempo 与每拍分辨率，换首歌就不能直接比了。
 */
@Data
@TableName("music_task")
public class MusicTask {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String name;

	/** 来源：MIDI / JIANPU */
	private String sourceType;

	/** 原始文件名或简谱首行，便于回溯这次解析是从哪来的 */
	private String sourceRef;

	private Integer noteCount;

	private Integer tempoBpm;

	private String timeSignature;

	private Integer durationMs;

	/** 最低音高，用于判断乐器键位够不够用 */
	private Integer pitchLow;

	private Integer pitchHigh;

	private String status;

	@TableLogic
	private Integer deleted;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;
}
