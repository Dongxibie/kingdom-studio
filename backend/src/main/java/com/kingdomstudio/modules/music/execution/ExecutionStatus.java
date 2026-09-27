package com.kingdomstudio.modules.music.execution;

import java.util.Locale;
import java.util.Map;

/**
 * 演奏会话的状态机。
 *
 * <p>只允许这几条转移：READY → RUNNING →（PAUSED ↔ RUNNING）→ FINISHED / STOPPED。
 * STOPPED 与 FINISHED 都是终态，且**只有停下来的会话才允许释放按键**——
 * 状态清楚，是因为「有没有按键还按着」这件事必须一眼看得见。
 */
public enum ExecutionStatus {

	/** 已就绪：计划有了，但还没开始 */
	READY("就绪"),
	/** 演奏中 */
	RUNNING("演奏中"),
	/** 已暂停：计时停住，按键全部释放 */
	PAUSED("已暂停"),
	/** 已停止：人工停止或急停触发 */
	STOPPED("已停止"),
	/** 已完成 */
	FINISHED("已完成");

	private static final Map<String, String> LABELS = Map.of(
			"READY", "就绪", "RUNNING", "演奏中", "PAUSED", "已暂停", "STOPPED", "已停止", "FINISHED", "已完成");

	private final String label;

	ExecutionStatus(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	public boolean terminal() {
		return this == STOPPED || this == FINISHED;
	}

	/** 从字符串解析，认不出来就按就绪处理 */
	public static ExecutionStatus of(String value) {
		if (value == null || value.isBlank()) {
			return READY;
		}
		try {
			return valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ignored) {
			return READY;
		}
	}
}
