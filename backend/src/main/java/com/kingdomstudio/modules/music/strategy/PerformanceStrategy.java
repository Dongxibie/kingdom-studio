package com.kingdomstudio.modules.music.strategy;

import java.util.Locale;
import java.util.Map;

/**
 * 演奏策略：同一首曲子，按使用者的水平与场合换一种「演奏方案」。
 *
 * <p>三种策略改的都是**演奏方式**，不是音乐内容：
 * 初学模式允许删繁就简（去装饰音、合并密集音、收窄跨度），展示模式只调力度与呼吸（不加音、不改旋律）。
 * 换句话说：AI 与规则都不在这里「作曲」。
 */
public enum PerformanceStrategy {

	/** 初学模式：简化节奏、降低跨度、删掉复杂音符 */
	BEGINNER("初学模式", "简化节奏、降低跨度、删掉复杂音符，先能顺下来"),
	/** 标准模式：保持原曲 */
	NORMAL("标准模式", "保持原曲，不做任何改动"),
	/** 展示模式：保留特色、增强节奏、优化表现 */
	SHOWCASE("展示模式", "保留旋律与特色，只调力度与呼吸，让演奏更有起伏");

	private static final Map<String, String> ALIASES = Map.ofEntries(
			Map.entry("BEGINNER", "BEGINNER"),
			Map.entry("EASY", "BEGINNER"),
			Map.entry("SIMPLE", "BEGINNER"),
			Map.entry("NORMAL", "NORMAL"),
			Map.entry("STANDARD", "NORMAL"),
			Map.entry("DEFAULT", "NORMAL"),
			Map.entry("SHOWCASE", "SHOWCASE"),
			Map.entry("SHOW", "SHOWCASE"),
			Map.entry("PERFORMANCE", "SHOWCASE"));

	private final String label;
	private final String description;

	PerformanceStrategy(String label, String description) {
		this.label = label;
		this.description = description;
	}

	public String label() {
		return label;
	}

	public String description() {
		return description;
	}

	/**
	 * 解析策略。
	 *
	 * <p>只认白名单里的值：模型或前端给什么字符串都一样，认不出来就退回 {@link #NORMAL}，
	 * 绝不把一段自由文本当策略用。
	 */
	public static PerformanceStrategy of(String value) {
		if (value == null || value.isBlank()) {
			return NORMAL;
		}
		String key = value.trim().toUpperCase(Locale.ROOT);
		String mapped = ALIASES.get(key);
		if (mapped != null) {
			return valueOf(mapped);
		}
		for (PerformanceStrategy strategy : values()) {
			if (strategy.label.equals(value.trim())) {
				return strategy;
			}
		}
		return NORMAL;
	}

	/** 认不认得出来：给助手做校验用 */
	public static boolean recognizes(String value) {
		if (value == null || value.isBlank()) {
			return false;
		}
		String key = value.trim().toUpperCase(Locale.ROOT);
		return ALIASES.containsKey(key) || value.trim().equals(BEGINNER.label)
				|| value.trim().equals(NORMAL.label) || value.trim().equals(SHOWCASE.label);
	}
}
