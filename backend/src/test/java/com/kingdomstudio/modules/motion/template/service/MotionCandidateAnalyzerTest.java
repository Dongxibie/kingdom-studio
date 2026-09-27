package com.kingdomstudio.modules.motion.template.service;

import com.kingdomstudio.modules.motion.template.entity.MotionCandidate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 候选分析器的单元测试。
 *
 * <p>这一层的价值全在「说得清」：为什么分到这一类、为什么判掉、档位怎么来的。
 * 所以测试不测「返回值非空」，而是把规则本身钉住——改了关键词表，这里必须跟着改。
 */
class MotionCandidateAnalyzerTest {

	private final MotionCandidateAnalyzer analyzer = new MotionCandidateAnalyzer();

	private MotionCandidate candidate(String name, String description, String language, int stars) {
		MotionCandidate candidate = new MotionCandidate();
		candidate.setCandidateKey("demo__repo");
		candidate.setName(name);
		candidate.setFullName("demo/repo");
		candidate.setDescription(description);
		candidate.setLanguage(language);
		candidate.setStars(stars);
		candidate.setTopics("");
		return candidate;
	}

	@Test
	@DisplayName("文字类仓库分到「文字动画」，并留下命中的关键词")
	void classifiesTextMotion() {
		MotionCandidateAnalyzer.Analysis analysis = analyzer.analyze(
				candidate("typewriter", "A pure CSS text animation library for headlines", "CSS", 800));
		assertEquals("文字动画", analysis.getCategory());
		assertTrue(analysis.getHints().contains("text") || analysis.getHints().contains("typewriter"),
				"应留下命中痕迹：" + analysis.getHints());
	}

	@Test
	@DisplayName("shader / webgl 仓库分到「三维 WebGL」，技术栈归到 Canvas，难度最高档")
	void classifiesWebgl() {
		MotionCandidateAnalyzer.Analysis analysis = analyzer.analyze(
				candidate("shader-demo", "WebGL shader playground with GLSL fragment shaders", "GLSL", 3000));
		assertEquals("三维 WebGL", analysis.getCategory());
		assertEquals("Canvas", analysis.getTechnology());
		assertEquals(3, analysis.getDifficulty());
		assertEquals("GPU_ENHANCED", analysis.getPerformanceLevel(), "GPU 类必须标成依赖 GPU 加速");
	}

	@Test
	@DisplayName("关键词一条都不命中时不硬塞分类，返回 null 交给人工")
	void returnsNullWhenNothingMatched() {
		MotionCandidateAnalyzer.Analysis analysis = analyzer.analyze(
				candidate("some-tool", "A build tool for internal services", "TypeScript", 500));
		assertNull(analysis.getCategory(), "命中不了就不该硬塞一个分类");
	}

	@Test
	@DisplayName("触发方式按关键词判：hover / scroll / click / load")
	void detectsTrigger() {
		assertEquals("hover", analyzer.analyze(candidate("hover-cards", "mouse pointer hover effect", "CSS", 500)).getTrigger());
		assertEquals("scroll", analyzer.analyze(candidate("parallax", "parallax scrolling backgrounds", "JS", 900)).getTrigger());
		assertEquals("click", analyzer.analyze(candidate("toggle", "click to toggle theme", "JS", 300)).getTrigger());
		assertEquals("load", analyzer.analyze(candidate("intro", "entrance animation on load", "CSS", 300)).getTrigger());
	}

	@Test
	@DisplayName("运行档位与模板侧同一套口径：纯 CSS 入门档算轻量")
	void performanceFollowsTemplateRules() {
		assertEquals("LIGHTWEIGHT", analyzer.performanceOf("CSS", 1));
		assertEquals("BALANCED", analyzer.performanceOf("CSS", 2));
		assertEquals("GPU_ENHANCED", analyzer.performanceOf("Three.js", 1));
		assertEquals("GPU_ENHANCED", analyzer.performanceOf("Canvas", 2));
	}

	@Test
	@DisplayName("视觉分是排序用的估算值：0 星不崩，十万星也不会顶到 100")
	void visualScoreIsBounded() {
		assertEquals(78, analyzer.visualScore(0));
		assertEquals(78, analyzer.visualScore(null));
		assertTrue(analyzer.visualScore(500) > 78);
		assertTrue(analyzer.visualScore(50000) <= 97, "上限锁在 97，避免所有热门仓库挤在一个分数上");
	}

	@Test
	@DisplayName("自动初审：语言不对、清单类、分类未命中、星数太低，四种都淘汰且写明理由")
	void autoReviewRejectsWithReasons() {
		MotionCandidateAnalyzer.Verdict offLanguage = analyzer.autoReview(
				candidate("ios-anim", "animation for iOS apps", "Swift", 5000));
		assertEquals("REJECTED", offLanguage.getStatus());
		assertTrue(offLanguage.getNote().contains("Swift"));

		MotionCandidateAnalyzer.Verdict list = analyzer.autoReview(
				candidate("awesome-motion", "A curated list of motion resources", "Markdown", 20000));
		assertEquals("REJECTED", list.getStatus());
		assertTrue(list.getNote().contains("清单"));

		MotionCandidateAnalyzer.Verdict unknown = analyzer.autoReview(
				candidate("build-tool", "internal build tool", "TypeScript", 900));
		assertEquals("REJECTED", unknown.getStatus());

		MotionCandidateAnalyzer.Verdict lowStars = analyzer.autoReview(
				candidate("tiny-cards", "hover card effect", "CSS", 20));
		assertEquals("REJECTED", lowStars.getStatus());
		assertTrue(lowStars.getNote().contains("20"));
	}

	@Test
	@DisplayName("正常候选进入「已分析」等待人工，不会自动入库")
	void autoReviewKeepsGoodCandidatesForHuman() {
		MotionCandidateAnalyzer.Verdict verdict = analyzer.autoReview(
				candidate("css-hover-cards", "Hover card effect with glass surface", "CSS", 1200));
		assertEquals("ANALYZED", verdict.getStatus());
		assertTrue(verdict.getNote().contains("人工"), "规则只到「分析」，入库必须有人点头：" + verdict.getNote());
	}
}
