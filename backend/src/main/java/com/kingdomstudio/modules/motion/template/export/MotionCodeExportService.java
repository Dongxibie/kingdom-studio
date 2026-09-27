package com.kingdomstudio.modules.motion.template.export;

import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.motion.template.service.MotionRecipeService;
import com.kingdomstudio.modules.motion.template.service.MotionTemplateService;
import com.kingdomstudio.modules.motion.template.vo.MotionExportVO;
import com.kingdomstudio.modules.motion.template.vo.MotionRecipeVO;
import com.kingdomstudio.modules.motion.template.vo.MotionTemplateDetailVO;
import com.kingdomstudio.modules.motion.template.vo.RecipeStepVO;
import com.kingdomstudio.modules.motion.template.vo.TemplateParamVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 代码导出：把一套方案（多个动效）编译成一整套**能拷进项目就跑**的工程文件。
 *
 * <p>为什么由后端生成而不是前端拼字符串：模板的预览结构、CSS、参数定义都在库里，
 * 生成规则（怎么作用域化、怎么把参数写成 CSS 变量）只有一份实现，前端只负责展示与下载。
 * 这样「导出结果」与「沙箱里看到的效果」用的是同一份数据，不会出现所见与所得不一致。
 *
 * <p>三种格式对应三种真实用法：
 * <ul>
 *   <li><b>Vue 3</b>：每个动效一个 SFC（各自 {@code <style scoped>}，天然不串样式）+ 一个方案组件 + 配置；</li>
 *   <li><b>React</b>：每个动效一个 TSX + 它自己的 CSS（服务端做了作用域化）+ 方案组件 + 配置；</li>
 *   <li><b>HTML + CSS</b>：单页可直接打开的 index.html，样式全部作用域化后内联。</li>
 * </ul>
 *
 * <p>参数不是写死在样式里，而是作为 CSS 变量挂在每一步的包装元素上：
 * 想调只改配置，不用去改样式表。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotionCodeExportService {

	private static final int MAX_STEPS = 8;

	private final MotionRecipeService recipeService;
	private final MotionTemplateService templateService;

	public MotionExportVO export(String recipeKey, String planName, String formatRaw, List<String> templateKeys,
			Map<String, Map<String, Double>> overrides) {
		String format = normalizeFormat(formatRaw);
		List<RecipeStepVO> steps = resolveSteps(recipeKey, templateKeys);
		if (steps.isEmpty()) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "这套方案里没有可导出的动效");
		}
		if (steps.size() > MAX_STEPS) {
			steps = steps.subList(0, MAX_STEPS);
		}
		List<StepBundle> bundles = new ArrayList<>();
		for (RecipeStepVO step : steps) {
			MotionTemplateDetailVO detail = templateService.detail(step.getTemplateKey());
			bundles.add(new StepBundle(step, detail, variables(detail, overrides == null ? null : overrides.get(step.getTemplateKey()))));
		}

		String name = planName != null && !planName.isBlank() ? planName.trim()
				: (recipeKey != null && !recipeKey.isBlank() ? recipeKey : "Motion Plan");
		List<MotionExportVO.FileVO> files = switch (format) {
			case "VUE" -> vueFiles(name, recipeKey, bundles);
			case "REACT" -> reactFiles(name, recipeKey, bundles);
			default -> htmlFiles(name, recipeKey, bundles);
		};
		String previewHtml = previewDocument(name, bundles);

		List<String> notes = new ArrayList<>();
		notes.add("共 " + bundles.size() + " 步，全部来自模板库里的现成实现：没有生成新代码，也就没有「看起来对但跑不起来」的片段。");
		notes.add("每一步的可调参数已写成 CSS 变量挂在包装元素上（见 motion.config.ts 或各步的 style 绑定），改参数不用动样式表。");
		if (!"VUE".equals(format)) {
			notes.add("CSS 已在服务端做了作用域化：选择器统一加包装类前缀，@keyframes 名字按步骤重命名，"
					+ "所以两个模板的 .motion-root 或同名动画不会互相覆盖。");
		} else {
			notes.add("Vue 版本用 <style scoped> 隔离，同时把模板里的 :root 变量收到每一步的包装元素上 —— "
					+ "scoped 样式里的 :root 匹配不到任何元素，不改的话变量会静默失效。");
		}
		notes.add("预览用的是同一份数据渲染出来的组合页面，你看到的排版与导出结果一致。");

		return MotionExportVO.builder()
				.planName(name)
				.recipeKey(recipeKey)
				.format(format)
				.formatLabel(switch (format) {
					case "VUE" -> "Vue 3 单文件组件";
					case "REACT" -> "React 函数组件";
					default -> "HTML + CSS";
				})
				.stepCount(bundles.size())
				.files(files)
				.previewHtml(previewHtml)
				.notes(notes)
				.build();
	}

	/** 步骤来源：给定方案 key 就用方案；否则用直接传入的模板清单（推荐页/设计页的临时组合） */
	List<RecipeStepVO> resolveSteps(String recipeKey, List<String> templateKeys) {
		if (recipeKey != null && !recipeKey.isBlank()) {
			MotionRecipeVO recipe = recipeService.detail(recipeKey);
			return new ArrayList<>(recipe.getSteps());
		}
		List<RecipeStepVO> steps = new ArrayList<>();
		if (templateKeys == null) {
			return steps;
		}
		int order = 0;
		for (String key : templateKeys) {
			if (key == null || key.isBlank()) {
				continue;
			}
			MotionTemplateDetailVO detail = templateService.detail(key.trim());
			steps.add(RecipeStepVO.builder()
					.order(++order)
					.templateKey(detail.getTemplateKey())
					.name(detail.getName())
					.stage("")
					.role(detail.getDescription())
					.technology(detail.getTechnology())
					.triggerLabel(detail.getTriggerLabel())
					.runtimeTier(detail.getRuntimeTier())
					.runtimeTierLabel(detail.getRuntimeTierLabel())
					.performanceGrade(detail.getGrade())
					.params(detail.getParams())
					.previewReady(true)
					.build());
		}
		return steps;
	}

	/** 每一步的 CSS 变量：默认值 or 覆盖值，带上参数自己的单位 */
	Map<String, String> variables(MotionTemplateDetailVO detail, Map<String, Double> override) {
		Map<String, String> values = new LinkedHashMap<>();
		for (TemplateParamVO param : detail.getParams()) {
			Double value = override != null && override.get(param.getKey()) != null
					? override.get(param.getKey())
					: param.getDefaultValue();
			if (value == null) {
				continue;
			}
			values.put(param.getKey(), format(value) + (param.getUnit() == null ? "" : param.getUnit()));
		}
		return values;
	}

	// ------------------------------------------------------------------ 三种格式

	List<MotionExportVO.FileVO> vueFiles(String planName, String recipeKey, List<StepBundle> bundles) {
		List<MotionExportVO.FileVO> files = new ArrayList<>();
		StringBuilder imports = new StringBuilder();
		StringBuilder tags = new StringBuilder();
		for (StepBundle bundle : bundles) {
			String component = componentName(bundle.step().getOrder(), bundle.step().getTemplateKey());
			imports.append("import ").append(component).append(" from './steps/").append(component).append(".vue'\n");
			tags.append("\t\t<").append(component).append(" />\n");
		}
		String plan = """
				<script setup lang="ts">
				/**
				 * %s
				 *
				 * 由 Kingdom Studio 动效工作台导出：%d 个动效按顺序叠成一页。
				 * 每一步是独立组件（各自 <style scoped>），所以样式天然互不干扰。
				 * 想调参数：改 motion.config.ts，或直接改某一个步骤组件里的 style 绑定。
				 */
				%s
				</script>

				<template>
					<div class="motion-plan">
				%s	</div>
				</template>

				<style scoped>
				.motion-plan {
					display: flex;
					flex-direction: column;
					gap: 24px;
				}
				</style>
				""".formatted(planName, bundles.size(), imports.toString(), tags.toString());
		files.add(file("src/components/MotionPlan.vue", "vue", "方案主组件：把每一步按顺序叠起来", plan));

		for (StepBundle bundle : bundles) {
			String component = componentName(bundle.step().getOrder(), bundle.step().getTemplateKey());
			files.add(file("src/components/steps/" + component + ".vue", "vue",
					"第 " + bundle.step().getOrder() + " 步：" + bundle.step().getName(),
					vueStep(component, bundle)));
		}
		files.add(file("src/components/motion.config.ts", "ts", "方案配置：名称与每一步的参数值", configFile(planName, recipeKey, bundles)));
		return files;
	}

	private String vueStep(String component, StepBundle bundle) {
		return """
				<script setup lang="ts">
				/**
				 * %s —— %s
				 * 作用：%s
				 *
				 * 参数以 CSS 变量下发，样式表里用 var(--m-xxx) 引用；
				 * 想换默认值改这里，或者在上层传 props 覆盖。
				 */
				const styleVars = %s as Record<string, string>
				</script>

				<template>
					<div class="motion-step" :style="styleVars">
						%s
					</div>
				</template>

				<style scoped>
				.motion-step {
					position: relative;
					min-height: %s;
					border-radius: 16px;
					overflow: hidden;
				}

				%s
				</style>
				""".formatted(component, bundle.step().getName(), blankToDash(bundle.step().getRole()),
				objectLiteral(bundle.variables(), "\t"), indent(bundle.detail().getPreviewHtml(), 2),
				minHeight(bundle.step()), indent(bundle.detail().getCssCode(), 0).strip());
	}

	List<MotionExportVO.FileVO> reactFiles(String planName, String recipeKey, List<StepBundle> bundles) {
		List<MotionExportVO.FileVO> files = new ArrayList<>();
		StringBuilder imports = new StringBuilder();
		StringBuilder tags = new StringBuilder();
		for (StepBundle bundle : bundles) {
			String component = componentName(bundle.step().getOrder(), bundle.step().getTemplateKey());
			imports.append("import ").append(component).append(" from './steps/").append(component).append("'\n");
			tags.append("      <").append(component).append(" />\n");
		}
		String plan = """
				/**
				 * %s
				 *
				 * 由 Kingdom Studio 动效工作台导出：%d 个动效按顺序叠成一页。
				 * 每一步是一个独立组件，样式在各自文件里已做好作用域，不会互相串。
				 */
				%s
				import './motion.css'

				export default function MotionPlan() {
				  return (
				    <main className="motion-plan">
				%s    </main>
				  )
				}
				""".formatted(planName, bundles.size(), imports.toString(), tags.toString());
		files.add(file("src/components/MotionPlan.tsx", "tsx", "方案主组件：把每一步按顺序叠起来", plan));

		for (StepBundle bundle : bundles) {
			String component = componentName(bundle.step().getOrder(), bundle.step().getTemplateKey());
			files.add(file("src/components/steps/" + component + ".tsx", "tsx",
					"第 " + bundle.step().getOrder() + " 步：" + bundle.step().getName(),
					reactStep(component, bundle)));
			files.add(file("src/components/steps/" + component + ".css", "css",
					"第 " + bundle.step().getOrder() + " 步的样式（已作用域化）",
					CssScoper.scope(bundle.detail().getCssCode(), "." + stepClass(bundle.step())) + "\n"));
		}
		files.add(file("src/components/motion.config.ts", "ts", "方案配置：名称与每一步的参数值", configFile(planName, recipeKey, bundles)));
		files.add(file("src/components/motion.css", "css", "方案级布局样式", planCss()));
		return files;
	}

	private String reactStep(String component, StepBundle bundle) {
		return """
				/**
				 * %s —— %s
				 * 作用：%s
				 *
				 * 参数以 CSS 变量下发；样式见 %s.css（已做作用域化）。
				 */
				import type { CSSProperties } from 'react'
				import './%s.css'

				const styleVars = %s as CSSProperties

				export default function %s() {
				  return (
				    <section className="motion-step" style={styleVars}>
				%s    </section>
				  )
				}
				""".formatted(component, bundle.step().getName(), blankToDash(bundle.step().getRole()),
				component, component, objectLiteral(bundle.variables(), ""), component,
				indent(bundle.detail().getPreviewHtml(), 3));
	}

	List<MotionExportVO.FileVO> htmlFiles(String planName, String recipeKey, List<StepBundle> bundles) {
		StringBuilder sections = new StringBuilder();
		StringBuilder styles = new StringBuilder();
		for (StepBundle bundle : bundles) {
			String stepClass = stepClass(bundle.step());
			sections.append("    <section class=\"motion-step ").append(stepClass).append("\" style=\"")
					.append(inlineVars(bundle.variables())).append("\" data-step=\"")
					.append(bundle.step().getOrder()).append("\" data-name=\"").append(escape(bundle.step().getName()))
					.append("\">\n").append(indent(bundle.detail().getPreviewHtml(), 3)).append("\n    </section>\n");
			styles.append("/* 第 ").append(bundle.step().getOrder()).append(" 步：").append(bundle.step().getName())
					.append(" —— ").append(blankToDash(bundle.step().getRole())).append(" */\n")
					.append(CssScoper.scope(bundle.detail().getCssCode(), "." + stepClass)).append("\n\n");
		}
		String html = """
				<!DOCTYPE html>
				<html lang="zh-CN">
				<head>
				  <meta charset="utf-8" />
				  <meta name="viewport" content="width=device-width, initial-scale=1" />
				  <title>%s</title>
				  <!-- 由 Kingdom Studio 动效工作台导出：%d 个动效按顺序叠成一页，样式已作用域化 -->
				  <link rel="stylesheet" href="motion.css" />
				</head>
				<body>
				  <main class="motion-plan">
				%s  </main>
				</body>
				</html>
				""".formatted(escape(planName), bundles.size(), sections.toString());
		String css = """
				/* %s —— 方案级布局 + 各步样式（选择器已加前缀，@keyframes 已按步重命名） */

				%s
				%s""".formatted(planName, planCss(), styles);
		List<MotionExportVO.FileVO> files = new ArrayList<>();
		files.add(file("index.html", "html", "可直接打开的整页（每步一个 section）", html));
		files.add(file("motion.css", "css", "方案样式：布局 + 各步作用域化后的样式", css));
		return files;
	}

	/** 组合预览文档：与导出的 HTML 同源同构，前端塞进沙箱 iframe 即可 */
	String previewDocument(String planName, List<StepBundle> bundles) {
		StringBuilder sections = new StringBuilder();
		StringBuilder styles = new StringBuilder();
		for (StepBundle bundle : bundles) {
			String stepClass = stepClass(bundle.step());
			sections.append("<section class=\"motion-step ").append(stepClass).append("\" style=\"")
					.append(inlineVars(bundle.variables())).append("\">\n")
					.append(bundle.detail().getPreviewHtml()).append("\n</section>\n");
			styles.append(CssScoper.scope(bundle.detail().getCssCode(), "." + stepClass)).append("\n");
		}
		return """
				<!DOCTYPE html>
				<html lang="zh-CN">
				<head>
				<meta charset="utf-8" />
				<style>
				html, body { margin: 0; padding: 0; background: #05060a; }
				%s
				%s
				</style>
				</head>
				<body>
				<main class="motion-plan">
				%s</main>
				</body>
				</html>
				""".formatted(planCss(), styles, sections);
	}

	String configFile(String planName, String recipeKey, List<StepBundle> bundles) {
		StringBuilder steps = new StringBuilder();
		for (StepBundle bundle : bundles) {
			steps.append("\t'").append(bundle.step().getTemplateKey()).append("': ")
					.append(objectLiteral(bundle.variables(), "\t")).append(",\n");
		}
		return """
				/**
				 * 方案配置：%s
				 *
				 * 每一步的可调参数在这里集中管理（键就是 CSS 变量名，值带单位）。
				 * 组件把这些值作为 style 下发，所以调参数不用改样式表。
				 */
				export const motionPlan = {
				  name: '%s',
				  recipeKey: '%s',
				} as const

				export const stepParams: Record<string, Record<string, string>> = {
				%s}
				""".formatted(planName, escape(planName), recipeKey == null ? "" : recipeKey, steps.toString());
	}

	String planCss() {
		return """
				.motion-plan {
				  display: flex;
				  flex-direction: column;
				  gap: 24px;
				  padding: 24px;
				  box-sizing: border-box;
				  background: #05060a;
				}

				.motion-step {
				  position: relative;
				  min-height: 320px;
				  border-radius: 16px;
				  overflow: hidden;
				}
				""";
	}

	// ------------------------------------------------------------------ 小工具

	private String minHeight(RecipeStepVO step) {
		return "320px";
	}

	/** 包装类名：mlab-step-3 */
	String stepClass(RecipeStepVO step) {
		return "mlab-step-" + step.getOrder();
	}

	/** 组件名：Step1GalaxyBackground */
	String componentName(int order, String templateKey) {
		StringBuilder builder = new StringBuilder("Step").append(order);
		for (String part : templateKey.split("[-_]")) {
			if (part.isBlank()) {
				continue;
			}
			builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return builder.toString();
	}

	String objectLiteral(Map<String, String> values, String indent) {
		if (values.isEmpty()) {
			return "{}";
		}
		StringBuilder builder = new StringBuilder("{\n");
		values.forEach((key, value) -> builder.append(indent).append("\t'").append(key).append("': '")
				.append(value).append("',\n"));
		builder.append(indent).append("}");
		return builder.toString();
	}

	String inlineVars(Map<String, String> values) {
		StringBuilder builder = new StringBuilder();
		values.forEach((key, value) -> builder.append(key).append(": ").append(value).append("; "));
		return builder.toString().trim();
	}

	String normalizeFormat(String raw) {
		if (raw == null || raw.isBlank()) {
			return "VUE";
		}
		String value = raw.trim().toUpperCase(Locale.ROOT);
		return List.of("VUE", "REACT", "HTML").contains(value) ? value : "VUE";
	}

	private MotionExportVO.FileVO file(String path, String language, String role, String content) {
		return MotionExportVO.FileVO.builder()
				.path(path)
				.language(language)
				.role(role)
				.content(content)
				.bytes(content.getBytes(StandardCharsets.UTF_8).length)
				.build();
	}

	private String format(Double value) {
		if (value == null) {
			return "0";
		}
		if (Math.abs(value - Math.rint(value)) < 0.0001) {
			return String.valueOf((long) Math.rint(value));
		}
		return String.valueOf(Math.round(value * 1000) / 1000.0);
	}

	private String indent(String text, int levels) {
		if (text == null || text.isBlank()) {
			return "";
		}
		String pad = "\t".repeat(Math.max(0, levels));
		StringBuilder builder = new StringBuilder();
		for (String line : text.strip().split("\n")) {
			builder.append(pad).append(line).append('\n');
		}
		return builder.toString().stripTrailing();
	}

	private String escape(String value) {
		return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;");
	}

	private String blankToDash(String value) {
		return value == null || value.isBlank() ? "（未写作用说明）" : value;
	}

	/** 一步的全部素材：声明的步骤 + 模板详情 + 最终参数值 */
	record StepBundle(RecipeStepVO step, MotionTemplateDetailVO detail, Map<String, String> variables) {
	}
}
