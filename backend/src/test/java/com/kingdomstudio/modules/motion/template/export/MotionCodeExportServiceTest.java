package com.kingdomstudio.modules.motion.template.export;

import com.kingdomstudio.modules.motion.template.service.MotionRecipeService;
import com.kingdomstudio.modules.motion.template.service.MotionTemplateService;
import com.kingdomstudio.modules.motion.template.vo.MotionExportVO;
import com.kingdomstudio.modules.motion.template.vo.MotionRecipeVO;
import com.kingdomstudio.modules.motion.template.vo.MotionTemplateDetailVO;
import com.kingdomstudio.modules.motion.template.vo.RecipeStepVO;
import com.kingdomstudio.modules.motion.template.vo.TemplateParamVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 代码导出的单元测试。
 *
 * <p>导出最容易出的两个问题：产物里带着别的模板的样式（作用域没做好）、
 * 以及参数值没带单位导致复制过去跑不起来。这两条都钉在这里。
 */
class MotionCodeExportServiceTest {

	private MotionCodeExportService service;

	@BeforeEach
	void setUp() {
		MotionRecipeService recipeService = mock(MotionRecipeService.class);
		MotionTemplateService templateService = mock(MotionTemplateService.class);

		when(recipeService.detail("cyber-launch")).thenReturn(MotionRecipeVO.builder()
				.recipeKey("cyber-launch").name("Cyber Launch").scene("Landing Page").style("Cyber")
				.steps(List.of(
						RecipeStepVO.builder().order(1).templateKey("galaxy-background").name("星系背景")
								.stage("背景").role("星系渐变打底").technology("CSS").triggerLabel("加载时")
								.performanceGrade("B").params(List.of()).build(),
						RecipeStepVO.builder().order(2).templateKey("headline-clip-wipe").name("标题擦除入场")
								.stage("内容").role("标题擦除出现").technology("CSS").triggerLabel("加载时")
								.performanceGrade("A").params(List.of()).build()))
				.build());
		when(templateService.detail(anyString())).thenAnswer(invocation -> {
			String key = invocation.getArgument(0);
			if ("galaxy-background".equals(key)) {
				return detail(key, "星系背景", "<div class=\"motion-root motion-root--bleed\"><div class=\"m-galaxy\"></div></div>",
						":root { --m-duration: 18s; }\n.m-galaxy { position: relative; height: 100%; }\n"
								+ "@keyframes m-drift { 50% { opacity: 0.6; } }\n.m-galaxy::before { animation: m-drift 18s linear infinite; }",
						List.of(param("--m-duration", "穿越周期", "s", 18),
								param("--m-distance", "星密度", "%", 14)));
			}
			return detail(key, "标题擦除入场",
					"<div class=\"motion-root\"><div class=\"m-wipe\"><h2>MOTION INTELLIGENCE</h2></div></div>",
					".motion-root { display: grid; place-items: center; }\n.m-wipe h2 { animation: m-wipe-text var(--m-duration); }\n"
							+ "@keyframes m-wipe-text { from { clip-path: inset(0 100% 0 0); } }",
					List.of(param("--m-duration", "擦除时长", "s", 1.1)));
		});

		service = new MotionCodeExportService(recipeService, templateService);
	}

	private TemplateParamVO param(String key, String label, String unit, double def) {
		return TemplateParamVO.builder().key(key).label(label).unit(unit).min(0.2).max(40d).defaultValue(def).build();
	}

	private MotionTemplateDetailVO detail(String key, String name, String html, String css, List<TemplateParamVO> params) {
		return MotionTemplateDetailVO.builder()
				.templateKey(key).name(name).description(name + " 的说明")
				.category("背景效果").scene("Landing Page").sceneLabel("网站首页").style("Cyber").styleLabel("赛博")
				.technology("CSS").triggerType("load").triggerLabel("加载时")
				.runtimeTier("BALANCED").runtimeTierLabel("均衡").grade("B")
				.previewHtml(html).previewJs("").cssCode(css).vueCode("").reactCode("").threeCode("").prompt("")
				.params(params).bestFor(List.of("官网首页")).tags(List.of()).usedByRecipes(List.of())
				.build();
	}

	@Test
	@DisplayName("Vue 导出：方案组件 + 每步一个 SFC + 配置文件")
	void shouldExportVueProject() {
		MotionExportVO vo = service.export("cyber-launch", "科技公司首页", "VUE", null, null);

		assertEquals("VUE", vo.getFormat());
		assertEquals(2, vo.getStepCount());
		assertEquals(4, vo.getFiles().size(), "主组件 + 2 个步骤组件 + 配置");
		assertEquals("src/components/MotionPlan.vue", vo.getFiles().get(0).getPath());
		assertTrue(vo.getFiles().get(0).getContent().contains("import Step1GalaxyBackground"),
				vo.getFiles().get(0).getContent());
		assertTrue(vo.getFiles().get(1).getContent().contains("<style scoped>"), "Vue 用 scoped 隔离");
		assertTrue(vo.getFiles().get(1).getContent().contains("'--m-duration': '18s'"),
				"参数以 CSS 变量下发且带单位");
		assertTrue(vo.getFiles().get(3).getPath().endsWith("motion.config.ts"));
		assertTrue(vo.getFiles().get(3).getContent().contains("'galaxy-background'"));
	}

	@Test
	@DisplayName("React 导出：逐步一个 TSX + 一份作用域化 CSS，且两份 CSS 不互相污染")
	void shouldExportReactProjectWithScopedCss() {
		MotionExportVO vo = service.export("cyber-launch", "科技公司首页", "REACT", null, null);

		List<MotionExportVO.FileVO> cssFiles = vo.getFiles().stream()
				.filter(file -> file.getPath().endsWith(".css") && file.getPath().contains("steps")).toList();
		assertEquals(2, cssFiles.size());
		assertTrue(cssFiles.get(0).getContent().contains(".mlab-step-1 .m-galaxy"), cssFiles.get(0).getContent());
		assertTrue(cssFiles.get(1).getContent().contains(".mlab-step-2 .motion-root"), cssFiles.get(1).getContent());
		assertTrue(cssFiles.get(0).getContent().contains("@keyframes mlab-step-1-m-drift"), cssFiles.get(0).getContent());
		assertFalse(cssFiles.get(1).getContent().contains("m-galaxy"), "第二步的样式里不该出现第一步的类名");
		assertTrue(vo.getFiles().stream().anyMatch(file -> file.getPath().endsWith("motion.css")), "方案级布局样式");
		assertTrue(vo.getFiles().stream().anyMatch(file -> file.getPath().endsWith("MotionPlan.tsx")));
	}

	@Test
	@DisplayName("HTML 导出：单页 + 样式表，每一步是带参数与作用域类的 section")
	void shouldExportHtmlPage() {
		MotionExportVO vo = service.export("cyber-launch", "科技公司首页", "HTML", null, null);

		assertEquals(2, vo.getFiles().size());
		String html = vo.getFiles().get(0).getContent();
		assertTrue(html.contains("<section class=\"motion-step mlab-step-1\" style=\"--m-duration: 18s; --m-distance: 14%;\""),
				html);
		assertTrue(html.contains("data-step=\"2\""), html);
		String css = vo.getFiles().get(1).getContent();
		assertTrue(css.contains(".mlab-step-1 .m-galaxy"));
		assertTrue(css.contains(".mlab-step-2 .m-wipe h2"));
	}

	@Test
	@DisplayName("组合预览：与导出的 HTML 同构，两步都在，样式已作用域化")
	void shouldBuildPreviewDocument() {
		MotionExportVO vo = service.export("cyber-launch", "科技公司首页", "HTML", null, null);

		String preview = vo.getPreviewHtml();
		assertTrue(preview.startsWith("<!DOCTYPE html>"));
		assertTrue(preview.contains("m-galaxy"));
		assertTrue(preview.contains("m-wipe"));
		assertTrue(preview.contains(".mlab-step-1 .m-galaxy"));
		assertTrue(preview.contains(".mlab-step-2 .motion-root"));
		assertTrue(preview.contains("<main class=\"motion-plan\">"));
	}

	@Test
	@DisplayName("参数覆盖：传了值就用传的，没传用默认值，单位照旧补上")
	void shouldApplyParamOverrides() {
		MotionExportVO vo = service.export("cyber-launch", "轻量版", "HTML", null,
				Map.of("galaxy-background", Map.<String, Double>of("--m-duration", 6.0)));

		String html = vo.getFiles().get(0).getContent();
		assertTrue(html.contains("--m-duration: 6s"), html);
		assertTrue(html.contains("--m-distance: 14%"), "没覆盖的用默认值");
	}

	@Test
	@DisplayName("临时组合：不给方案 key 时按模板清单导出（推荐结果直接导出用这条路）")
	void shouldExportFromTemplateKeys() {
		MotionExportVO vo = service.export(null, "临时组合", "HTML",
				List.of("galaxy-background", "headline-clip-wipe"), null);

		assertEquals(2, vo.getStepCount());
		assertEquals("临时组合", vo.getPlanName());
		assertTrue(vo.getFiles().get(0).getContent().contains("mlab-step-2"));
	}

	@Test
	@DisplayName("格式缺省或写错时按 Vue 处理，不报错")
	void shouldNormalizeFormat() {
		assertEquals("VUE", service.normalizeFormat(null));
		assertEquals("VUE", service.normalizeFormat("  "));
		assertEquals("VUE", service.normalizeFormat("svelte"));
		assertEquals("REACT", service.normalizeFormat("react"));
		assertEquals("HTML", service.normalizeFormat("Html"));
	}

	@Test
	@DisplayName("组件名与包装类名可预测：Step1GalaxyBackground / mlab-step-1")
	void shouldBuildNames() {
		RecipeStepVO step = RecipeStepVO.builder().order(3).templateKey("parallax-depth-3").build();
		assertEquals("mlab-step-3", service.stepClass(step));
		assertEquals("Step1GalaxyBackground", service.componentName(1, "galaxy-background"));
		assertEquals("Step2ParallaxDepth3", service.componentName(2, "parallax-depth-3"));
	}

	@Test
	@DisplayName("导出说明把「代码来自模板库」与「参数怎么调」写清楚")
	void shouldExplainWhatWasDone() {
		MotionExportVO vo = service.export("cyber-launch", "科技公司首页", "REACT", null, null);

		assertTrue(vo.getNotes().stream().anyMatch(note -> note.contains("没有生成新代码")), vo.getNotes().toString());
		assertTrue(vo.getNotes().stream().anyMatch(note -> note.contains("作用域化")), vo.getNotes().toString());
		assertTrue(vo.getNotes().stream().anyMatch(note -> note.contains("同一份数据")), vo.getNotes().toString());
	}
}
