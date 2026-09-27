package com.kingdomstudio.modules.motion.template.export;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 把一份模板 CSS 收进一个作用域里。
 *
 * <p>为什么要这一步：每个模板都是**独立设计**的，用的类名会重名（三十多个模板都有 {@code .motion-root}，
 * 参数变量也都叫 {@code --m-duration}）。把几个模板拼进同一张页面时，不隔离就会互相串样式。
 * Vue 的 {@code <style scoped>} 能自己解决，但导出的 HTML 与 React 版本没有这个机制，所以在服务端做一次作用域化。
 *
 * <p>规则（都有测试兜着）：
 * <ol>
 *   <li>普通规则的选择器逐个加前缀：{@code .m-wipe} → {@code .mlab-step-1 .m-wipe}；</li>
 *   <li>{@code :root} / {@code html} / {@code body} 换成包装元素本身，参数变量才会落在这一步里，
 *       而不是落到整页（否则第二步会把第一步的参数覆盖掉）；</li>
 *   <li>{@code @keyframes} 的名字重命名为 {@code mlab-step-1-原名}，并在同一份 CSS 里把引用一起改掉 ——
 *       不同模板的 keyframes 重名会互相覆盖，动画会跳；</li>
 *   <li>{@code @media} / {@code @supports} 递归处理内部规则；</li>
 *   <li>{@code @import} 这类单行 at-rule 原样保留。</li>
 * </ol>
 */
public final class CssScoper {

	private CssScoper() {
	}

	/** 作用域前缀：传 {@code .mlab-step-3} 这样的包装类名 */
	public static String scope(String css, String wrapper) {
		if (css == null || css.isBlank()) {
			return "";
		}
		Map<String, String> renamed = keyframeNames(css, wrapper);
		StringBuilder out = new StringBuilder();
		write(css, wrapper, renamed, out, 0);
		return out.toString().trim();
	}

	/** 收集 @keyframes 名字 → 重命名后的名字（前缀把模板之间隔开） */
	private static Map<String, String> keyframeNames(String css, String wrapper) {
		Map<String, String> names = new LinkedHashMap<>();
		for (Block block : blocks(css)) {
			String prelude = block.prelude().trim();
			if (prelude.startsWith("@keyframes") || prelude.startsWith("@-webkit-keyframes")) {
				String name = prelude.substring(prelude.indexOf(' ')).trim();
				if (!name.isEmpty() && !names.containsKey(name)) {
					names.put(name, prefix(wrapper) + "-" + name);
				}
			}
		}
		return names;
	}

	private static void write(String css, String wrapper, Map<String, String> renamed, StringBuilder out, int depth) {
		for (Block block : blocks(css)) {
			String prelude = block.prelude().trim();
			if (prelude.isEmpty()) {
				continue;
			}
			if (prelude.startsWith("@keyframes") || prelude.startsWith("@-webkit-keyframes")) {
				// 关键帧内部不能加前缀（from / to / 百分比不是选择器），只重命名名字
				String name = prelude.substring(prelude.indexOf(' ')).trim();
				String newName = renamed.getOrDefault(name, name);
				out.append("@keyframes ").append(newName).append(" {").append(block.body()).append("}\n");
				continue;
			}
			if (prelude.startsWith("@media") || prelude.startsWith("@supports") || prelude.startsWith("@layer")) {
				out.append(prelude).append(" {\n");
				StringBuilder inner = new StringBuilder();
				write(block.body(), wrapper, renamed, inner, depth + 1);
				out.append(inner);
				out.append("}\n");
				continue;
			}
			if (prelude.startsWith("@")) {
				// @import / @charset / @font-face 之类：原样保留
				out.append(prelude).append(" {").append(block.body()).append("}\n");
				continue;
			}
			out.append(String.join(", ", prefixed(prelude, wrapper)))
					.append(" {").append(rename(block.body(), renamed)).append("}\n");
		}
	}

	/** 选择器加前缀：:root / html / body 换成包装元素，其余前置包装元素 */
	static List<String> prefixed(String prelude, String wrapper) {
		List<String> result = new ArrayList<>();
		for (String raw : prelude.split(",")) {
			String selector = raw.trim().replaceAll("\\s+", " ");
			if (selector.isEmpty()) {
				continue;
			}
			if (selector.startsWith(":root")) {
				result.add(wrapper + selector.substring(":root".length()));
				continue;
			}
			if (selector.equals("html") || selector.equals("body") || selector.equals("*")
					|| selector.startsWith("html ") || selector.startsWith("body ")) {
				String rest = selector.equals("html") || selector.equals("body") ? "" : selector.substring(selector.indexOf(' '));
				result.add(wrapper + rest);
				continue;
			}
			result.add(wrapper + " " + selector);
		}
		return result;
	}

	/** 把动画名换成重命名后的（只在声明里替换，避免误伤选择器） */
	private static String rename(String declarations, Map<String, String> renamed) {
		String result = declarations;
		for (Map.Entry<String, String> entry : renamed.entrySet()) {
			result = result.replaceAll("(?<![\\w-])" + java.util.regex.Pattern.quote(entry.getKey()) + "(?![\\w-])",
					entry.getValue());
		}
		return result;
	}

	private static String prefix(String wrapper) {
		return wrapper.startsWith(".") ? wrapper.substring(1) : wrapper;
	}

	/** 把 CSS 切成「前导 + 花括号内容」的块，能正确处理嵌套（@media 里面还有规则） */
	static List<Block> blocks(String css) {
		List<Block> list = new ArrayList<>();
		int index = 0;
		int length = css.length();
		while (index < length) {
			int brace = css.indexOf('{', index);
			int semicolon = css.indexOf(';', index);
			if (brace < 0) {
				break;
			}
			// 单行 at-rule（@import …;）没有花括号，跳过它
			if (semicolon >= 0 && semicolon < brace) {
				index = semicolon + 1;
				continue;
			}
			String prelude = css.substring(index, brace);
			int depth = 1;
			int cursor = brace + 1;
			while (cursor < length && depth > 0) {
				char ch = css.charAt(cursor);
				if (ch == '{') {
					depth++;
				} else if (ch == '}') {
					depth--;
					if (depth == 0) {
						break;
					}
				}
				cursor++;
			}
			list.add(new Block(prelude, css.substring(brace + 1, Math.min(cursor, length))));
			index = cursor + 1;
		}
		return list;
	}

	/** 一个 CSS 块：前导（选择器或 at-rule）与花括号里的内容 */
	record Block(String prelude, String body) {
	}
}
