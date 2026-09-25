package com.kingdomstudio.modules.motion.crawler;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 去重工具：URL 哈希、内容哈希、README 文本相似度。
 *
 * <p>第一版刻意不做视觉相似（像素级感知哈希）：纯 Java 实现代价高、效果还不稳定，
 * 先把「同一来源 / 同一内容 / 高度相似文本」这三类最常见的重复挡住。
 */
@Component
public class MotionDedupe {

	/** 文本相似度阈值：3-gram 集合的 Jaccard 系数，超过即判为重复 */
	public static final double SIMILARITY_THRESHOLD = 0.8;

	/** 包含率阈值：短文本的 3-gram 几乎全被长文本覆盖时，视为「抄了主干又追加内容」 */
	public static final double CONTAINMENT_THRESHOLD = 0.95;

	/** 长度量级下限：两侧 3-gram 数量比值低于此值时只看 Jaccard，防止短语碰巧出现在长文里被误判 */
	public static final double MIN_LENGTH_RATIO = 0.5;

	/** 来源地址哈希：两个仓库地址只差大小写或末尾斜杠时视为同一个 */
	public String urlHash(String url) {
		return sha1(normalizeUrl(url));
	}

	/** 内容哈希：与 MotionService 里新增资源时的算法保持一致，唯一键 uk_motion_resource_hash 兜底 */
	public String contentHash(String name, String sourceUrl, String category) {
		return sha1((name == null ? "" : name.trim().toLowerCase(Locale.ROOT))
				+ "|" + (sourceUrl == null ? "" : sourceUrl.trim().toLowerCase(Locale.ROOT))
				+ "|" + (category == null ? "" : category));
	}

	public String normalizeUrl(String url) {
		if (url == null) {
			return "";
		}
		String value = url.trim().toLowerCase(Locale.ROOT);
		while (value.endsWith("/")) {
			value = value.substring(0, value.length() - 1);
		}
		return value;
	}

	/**
	 * README 文本相似度：取 3-gram 词元集合算 Jaccard 系数。
	 *
	 * <p>用 3-gram 而不是整词集合：README 里常出现同一句话换行、去标点的差异，
	 * 3-gram 对这类改写更稳，也天然忽略语序。
	 */
	public double similarity(String left, String right) {
		Set<String> a = shingles(left);
		Set<String> b = shingles(right);
		if (a.isEmpty() || b.isEmpty()) {
			return 0d;
		}
		return (double) overlap(a, b) / union(a, b).size();
	}

	public boolean isSimilar(String left, String right) {
		Set<String> a = shingles(left);
		Set<String> b = shingles(right);
		if (a.isEmpty() || b.isEmpty()) {
			return false;
		}
		int shared = overlap(a, b);
		if ((double) shared / union(a, b).size() >= SIMILARITY_THRESHOLD) {
			return true;
		}
		// Jaccard 用并集做分母，遇上「README 抄过来又追加了几段」会被稀释到阈值以下；
		// 这里补一条包含率判据：短的一侧几乎被长的一侧完整覆盖，且两侧长度量级相当，
		// 才算重复 —— 长度比值下限是为了不让「一句话恰好出现在长文里」被误判为重复
		// （误判的代价是这个资源被静默跳过，漏采比误采更糟）。
		double containment = (double) shared / Math.min(a.size(), b.size());
		double lengthRatio = (double) Math.min(a.size(), b.size()) / Math.max(a.size(), b.size());
		return containment >= CONTAINMENT_THRESHOLD && lengthRatio >= MIN_LENGTH_RATIO;
	}

	private int overlap(Set<String> a, Set<String> b) {
		Set<String> intersection = new HashSet<>(a);
		intersection.retainAll(b);
		return intersection.size();
	}

	private Set<String> union(Set<String> a, Set<String> b) {
		Set<String> union = new HashSet<>(a);
		union.addAll(b);
		return union;
	}

	/** 归一化 + 切 3-gram */
	Set<String> shingles(String text) {
		Set<String> set = new HashSet<>();
		if (text == null || text.isBlank()) {
			return set;
		}
		String normalized = text.toLowerCase(Locale.ROOT).replaceAll("[\\p{Punct}\\s]+", " ").trim();
		if (normalized.length() < 3) {
			set.add(normalized);
			return set;
		}
		for (int i = 0; i + 3 <= normalized.length(); i++) {
			set.add(normalized.substring(i, i + 3));
		}
		return set;
	}

	private String sha1(String raw) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder(bytes.length * 2);
			for (byte b : bytes) {
				builder.append(String.format("%02x", b));
			}
			return builder.toString();
		} catch (NoSuchAlgorithmException e) {
			return String.format("%040x", Math.abs(Arrays.hashCode(raw.getBytes(StandardCharsets.UTF_8))));
		}
	}
}
