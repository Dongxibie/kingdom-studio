package com.kingdomstudio.modules.music.share;

/**
 * 演奏码（Share Code）。
 *
 * <p>格式：{@code KS-MUSIC-<年>-<字母><三位序号>}，例如 {@code KS-MUSIC-2026-A001}。
 *
 * <p>为什么不用纯随机码：演奏码是要靠嘴念、靠手抄、贴在聊天窗口里的东西，
 * 随机串抄错一位就找不回，而「A001」这种有序编号念一遍就能确认。序号按 id 递增，
 * 每满 999 换一个字母（A/B/C…），所以既能一眼看出大致先后，也不会有歧义字符。
 */
public final class ShareCode {

	private static final String PREFIX = "KS-MUSIC-";

	private ShareCode() {
	}

	/** 由自增 id 与年份生成演奏码 */
	public static String of(long id, int year) {
		long safe = Math.max(1, id);
		long block = (safe - 1) / 999;
		long index = (safe - 1) % 999 + 1;
		char letter = (char) ('A' + (block % 26));
		return PREFIX + year + "-" + letter + String.format("%03d", index);
	}

	/** 校验是不是本系统的演奏码（用户输入时先挡掉明显不对的） */
	public static boolean matches(String code) {
		return code != null && code.trim().toUpperCase(java.util.Locale.ROOT)
				.matches("KS-MUSIC-\\d{4}-[A-Z]\\d{3}");
	}

	/** 规整用户输入：去空格、转大写，允许只输入后半段（A001 → KS-MUSIC-<今年>-A001） */
	public static String normalize(String raw, int year) {
		if (raw == null) {
			return "";
		}
		String value = raw.trim().toUpperCase(java.util.Locale.ROOT).replace(" ", "");
		if (value.isEmpty()) {
			return "";
		}
		if (value.startsWith(PREFIX)) {
			return value;
		}
		if (value.matches("[A-Z]\\d{3}")) {
			return PREFIX + year + "-" + value;
		}
		if (value.matches("\\d{4}-[A-Z]\\d{3}")) {
			return PREFIX + value;
		}
		return value;
	}
}
