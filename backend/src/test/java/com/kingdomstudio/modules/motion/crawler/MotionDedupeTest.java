package com.kingdomstudio.modules.motion.crawler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 去重工具单元测试：URL 归一化、内容哈希稳定性、README 相似度。 */
class MotionDedupeTest {

	private final MotionDedupe dedupe = new MotionDedupe();

	@Test
	@DisplayName("URL 归一化：大小写与末尾斜杠不影响哈希")
	void urlHashShouldIgnoreCaseAndSlash() {
		assertEquals(
				dedupe.urlHash("https://GitHub.com/Foo/Bar/"),
				dedupe.urlHash("https://github.com/foo/bar"));
	}

	@Test
	@DisplayName("内容哈希：同样的名称 / 来源 / 分类得到同样的哈希，改分类则不同")
	void contentHashShouldBeStable() {
		String a = dedupe.contentHash("Glass Card", "https://a.com/x", "Glass");
		String b = dedupe.contentHash(" glass card ", "https://a.com/x", "Glass");
		assertEquals(a, b, "名称首尾空格与大小写应被忽略");
		assertEquals(40, a.length());
		assertNotEquals(a, dedupe.contentHash("Glass Card", "https://a.com/x", "Hover"));
	}

	@Test
	@DisplayName("相似度：同一段文字换行、去标点后仍判为相似")
	void similarityShouldTolerateFormatting() {
		double score = dedupe.similarity(
				"Glass card hover animation with shine sweep effect",
				"glass-card, hover animation with shine sweep effect!!");
		assertTrue(score >= MotionDedupe.SIMILARITY_THRESHOLD, "实际相似度 " + score);
		assertTrue(dedupe.isSimilar("loading skeleton shimmer", "loading skeleton shimmer effect"));
	}

	@Test
	@DisplayName("相似度：不相关文本判为不重复")
	void similarityShouldRejectDifferent() {
		assertFalse(dedupe.isSimilar("three.js particle constellation",
				"mysql index optimization guide"));
		assertEquals(0d, dedupe.similarity("", "anything"));
	}

	@Test
	@DisplayName("相似度：Jaccard 被并集稀释、但短侧被完整包含时仍判为重复")
	void containmentShouldCatchAppendedText() {
		String base = "loading skeleton shimmer";
		String extended = "loading skeleton shimmer for dashboards";
		double jaccard = dedupe.similarity(base, extended);
		assertTrue(jaccard < MotionDedupe.SIMILARITY_THRESHOLD,
				"该样例的 Jaccard 应低于阈值，用于证明是包含率判据在兜底，实际 " + jaccard);
		assertTrue(dedupe.isSimilar(base, extended));
	}

	@Test
	@DisplayName("相似度：短句被长文完整覆盖但长度量级差太多时不算重复")
	void containmentShouldRespectLengthRatio() {
		String shortText = "hover glow";
		String longText = "hover glow effect on cards for landing page with parallax scroll"
				+ " and lazy loading images plus responsive grid layout demo";

		Set<String> shortGrams = dedupe.shingles(shortText);
		Set<String> longGrams = dedupe.shingles(longText);
		Set<String> covered = new HashSet<>(shortGrams);
		covered.retainAll(longGrams);
		assertEquals(shortGrams.size(), covered.size(), "短句 3-gram 确实被长文完整覆盖（包含率为 1）");

		assertFalse(dedupe.isSimilar(shortText, longText), "长度量级差距过大，长度比值下限应拦下误判");
	}
}
