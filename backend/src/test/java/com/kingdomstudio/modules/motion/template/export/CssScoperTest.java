package com.kingdomstudio.modules.motion.template.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CSS 作用域化的单元测试。
 *
 * <p>这段逻辑是「导出可运行代码」的关键：做错了不会报错，只会让拼起来的页面样式互相串。
 * 所以每条规则都单独钉一遍。
 */
class CssScoperTest {

	@Test
	@DisplayName("普通规则：选择器逐个加前缀，逗号选择器各自处理")
	void shouldPrefixSelectors() {
		String scoped = CssScoper.scope(".m-wipe h2 { color: red; }\n.a, .b { opacity: 0.5; }", ".mlab-step-1");

		assertTrue(scoped.contains(".mlab-step-1 .m-wipe h2 {"), scoped);
		assertTrue(scoped.contains(".mlab-step-1 .a, .mlab-step-1 .b {"), scoped);
	}

	@Test
	@DisplayName(":root / html / body 换成包装元素：参数变量只落在这一个步骤里")
	void shouldScopeRootVariables() {
		String scoped = CssScoper.scope(":root { --m-duration: 1.2s; }\nbody { margin: 0; }", ".mlab-step-2");

		assertTrue(scoped.contains(".mlab-step-2 { --m-duration: 1.2s; }"), scoped);
		assertTrue(scoped.contains(".mlab-step-2 { margin: 0; }"), scoped);
		assertFalse(scoped.contains(":root"), scoped);
	}

	@Test
	@DisplayName("关键帧重命名：定义与引用一起改，避免两个模板的动画互相覆盖")
	void shouldRenameKeyframes() {
		String css = "@keyframes m-wipe-text { from { clip-path: inset(0 100% 0 0); } to { clip-path: inset(0); } }\n"
				+ ".m-wipe h2 { animation: m-wipe-text var(--m-duration) forwards; }";
		String scoped = CssScoper.scope(css, ".mlab-step-1");

		assertTrue(scoped.contains("@keyframes mlab-step-1-m-wipe-text {"), scoped);
		assertTrue(scoped.contains("animation: mlab-step-1-m-wipe-text var(--m-duration)"), scoped);
		assertFalse(scoped.contains("@keyframes m-wipe-text"), scoped);
	}

	@Test
	@DisplayName("关键帧内部不加前缀：from / to / 百分比不是选择器")
	void shouldNotPrefixInsideKeyframes() {
		String scoped = CssScoper.scope("@keyframes spin { 0% { transform: rotate(0); } 100% { transform: rotate(360deg); } }",
				".mlab-step-3");

		assertTrue(scoped.contains("0% { transform: rotate(0); }"), scoped);
		assertTrue(scoped.contains("100% { transform: rotate(360deg); }"), scoped);
		assertFalse(scoped.contains(".mlab-step-3 0%"), scoped);
	}

	@Test
	@DisplayName("@media 递归处理：外层保留，里面的规则照样加前缀")
	void shouldScopeInsideMediaQueries() {
		String scoped = CssScoper.scope("@media (max-width: 640px) { .m-card { padding: 8px; } }", ".mlab-step-4");

		assertTrue(scoped.startsWith("@media (max-width: 640px) {"), scoped);
		assertTrue(scoped.contains(".mlab-step-4 .m-card { padding: 8px; }"), scoped);
	}

	@Test
	@DisplayName("同名的关键帧在两个步骤里被改成不同的名字")
	void shouldRenamePerStep() {
		String css = "@keyframes pulse { 50% { opacity: 0.4; } }\n.a { animation: pulse 2s infinite; }";

		assertTrue(CssScoper.scope(css, ".mlab-step-1").contains("mlab-step-1-pulse"));
		assertTrue(CssScoper.scope(css, ".mlab-step-2").contains("mlab-step-2-pulse"));
	}

	@Test
	@DisplayName("空 CSS 与单行 at-rule 不炸")
	void shouldHandleEdgeCases() {
		assertEquals("", CssScoper.scope(null, ".mlab-step-1"));
		assertEquals("", CssScoper.scope("   ", ".mlab-step-1"));
		assertFalse(CssScoper.scope("@import url('x.css'); .a { color: red; }", ".mlab-step-1").contains("import url"));
		assertTrue(CssScoper.scope("@import url('x.css'); .a { color: red; }", ".mlab-step-1")
				.contains(".mlab-step-1 .a { color: red; }"));
	}

	@Test
	@DisplayName("前缀不会漏进声明里：内容里的同名文本不动")
	void shouldOnlyRenameInDeclarations() {
		String scoped = CssScoper.scope(".a { content: \"pulse\"; }\n@keyframes pulse { 50% { opacity: 0.5; } }",
				".mlab-step-1");

		assertTrue(scoped.contains("@keyframes mlab-step-1-pulse"), scoped);
		assertTrue(scoped.contains(".mlab-step-1 .a"), scoped);
	}
}
