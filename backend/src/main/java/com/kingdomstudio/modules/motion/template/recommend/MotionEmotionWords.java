package com.kingdomstudio.modules.motion.template.recommend;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 情绪词表：把「高级感 / 科技感 / 克制」这类说法，换成可扫描的词。
 *
 * <p>情绪不进数据库：模板里没有「emotion」这一列，也不该为了推荐算法再加一列。
 * 所以情绪轴的做法是拿这张词表去比对模板（或组合方案）**已有的文本字段**
 * —— 风格、分组、标签、名称与说明。命不中就这一轴不加分，绝不猜。
 *
 * <p>推荐（模板）与设计（组合方案）两层共用这一份词表，免得同样的说法在两边得到不同的判定。
 */
public final class MotionEmotionWords {

	private static final Map<String, List<String>> KEYWORDS = Map.of(
			"premium", List.of("luxury", "gold", "高级", "premium", "elegant", "精致", "金"),
			"tech", List.of("cyber", "neon", "glow", "科技", "未来", "赛博", "shader", "particle", "three", "mesh"),
			"calm", List.of("minimal", "soft", "极简", "柔和", "克制", "fade", "smooth"),
			"playful", List.of("organic", "bounce", "活泼", "有趣", "跳"),
			"warm", List.of("organic", "warm", "温暖", "治愈", "噪点", "noise"),
			"bold", List.of("particle", "3d", "tilt", "shader", "冲击", "强", "粒子", "三维"));

	private MotionEmotionWords() {
	}

	/** 这段文本里命中了哪些情绪词（空列表表示没命中） */
	public static List<String> hits(String text, String emotion) {
		List<String> words = KEYWORDS.get(emotion);
		if (words == null || text == null || text.isBlank()) {
			return List.of();
		}
		String lower = text.toLowerCase(Locale.ROOT);
		List<String> hits = new ArrayList<>();
		for (String word : words) {
			if (lower.contains(word)) {
				hits.add(word);
			}
		}
		return hits;
	}
}
