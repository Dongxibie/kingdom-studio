package com.kingdomstudio.modules.motion.template.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingdomstudio.modules.motion.template.entity.MotionTemplate;
import com.kingdomstudio.modules.motion.template.vo.MotionAssistantVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Motion Assistant 的模型调用层（OpenAI 兼容的 chat completions）。
 *
 * <p><b>职责边界（刻意收窄）</b>：模型只做四件事——理解自然语言需求、判断风格、
 * 从**现有模板**里挑、把它们组合成一个方案并给出参数建议。
 * 它<b>不生成代码</b>：代码永远来自模板库里已经验证过的那份，
 * 这样「模型说得好听但代码跑不起来」这类问题从根上就不会出现。
 *
 * <p><b>为什么是「可替换的一层」</b>：只要服务商兼容 /chat/completions，改三个环境变量即可切换；
 * 没配 key、超时、返回不是合法 JSON、给了不存在的模板 key —— 任何一种情况都回退到
 * {@link MotionAssistantService} 的内置检索，并在返回里说明回退原因。用户不会因为模型不可用而用不了搜索。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotionModelClient {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/** 组装给模型的系统提示：限定职责、限定可选模板、要求严格 JSON */
	private static final String SYSTEM_PROMPT = """
			你是 Motion Assistant，一个前端动效方案的推荐助手。你的职责只有四件事：
			1) 理解用户用自然语言描述的需求；2) 判断它属于什么风格；
			3) 从下面给出的模板清单里挑选合适的模板；4) 把它们按应用顺序组合成一套方案，并给出参数建议。

			硬性规则：
			- 只能使用清单里出现过的 templateKey，绝不杜撰新的 key；
			- 不要生成代码、不要写 CSS/JS 片段，代码由系统按模板自带的实现给出；
			- 参数建议只允许使用模板清单里标注的参数名（形如 --m-duration）；
			- 方案里最多 5 个模板，至少 1 个；顺序要能讲清楚（例如先背景、再内容、最后交互）；
			- 只输出一个 JSON 对象，不要输出解释文字，不要用 markdown 代码块包裹。

			输出结构：
			{
			  "intent": { "scene": "Landing Page|Dashboard|Portfolio|Login|AI SaaS|Game UI 或 null",
			              "style": "Minimal|Luxury|Cyber|Glass|Organic 或 null",
			              "technology": "CSS|Canvas|Three.js|GSAP|Framer Motion 或 null",
			              "keywords": ["命中的动效关键词"],
			              "summary": "一句话复述你理解到的需求" },
			  "matches": [ { "templateKey": "…", "reason": "为什么推荐它" } ],
			  "recipe": { "name": "方案名", "description": "为什么这么组",
			              "bestFor": "官网首页,个人作品集",
			              "steps": [ { "templateKey": "…", "role": "这一步的作用",
			                           "params": { "--m-duration": 0.9 } } ] },
			  "advice": "一句落地的建议"
			}
			""";

	private final RestClient.Builder restClientBuilder;

	/** 模型服务地址（OpenAI 兼容），例如 https://api.deepseek.com/v1；留空表示不启用模型 */
	@Value("${kingdom.motion.assistant.base-url:${MOTION_LLM_BASE_URL:}}")
	private String baseUrl;

	@Value("${kingdom.motion.assistant.api-key:${MOTION_LLM_API_KEY:}}")
	private String apiKey;

	@Value("${kingdom.motion.assistant.model:${MOTION_LLM_MODEL:}}")
	private String model;

	@Value("${kingdom.motion.assistant.timeout-seconds:25}")
	private long timeoutSeconds;

	/** 模型是否可用：三项配置齐了才算 */
	public boolean enabled() {
		return notBlank(baseUrl) && notBlank(apiKey) && notBlank(model);
	}

	public String modelName() {
		return model;
	}

	/**
	 * 让模型分析需求并给方案。
	 *
	 * @param query     用户的自然语言输入
	 * @param templates 全部可选模板（key / 名称 / 场景 / 风格 / 技术 / 难度 / 推荐指数 / 参数名）
	 * @return 解析结果；任何问题都返回 null，由调用方回退到内置检索
	 */
	public ModelAnswer analyze(String query, List<MotionTemplate> templates) {
		if (!enabled()) {
			log.debug("未配置模型，跳过模型分析");
			return null;
		}
		try {
			String payload = OBJECT_MAPPER.writeValueAsString(Map.of(
					"model", model,
					"temperature", 0.2,
					"messages", List.of(
							Map.of("role", "system", "content", SYSTEM_PROMPT + "\n\n可选模板清单：\n" + catalog(templates)),
							Map.of("role", "user", "content", query))));

			String body = restClientBuilder.build()
					.post()
					.uri(trimEnd(baseUrl) + "/chat/completions")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.body(payload)
					.retrieve()
					.body(String.class);

			String content = extractContent(body);
			if (content == null) {
				log.warn("模型没有返回可用的内容");
				return null;
			}
			return parse(content);
		} catch (Exception e) {
			log.warn("模型调用失败，回退到内置检索：{}", e.getMessage());
			return null;
		}
	}

	/** 模板清单：只给模型必要信息，避免把正文代码塞进提示（省 token、也防它照抄代码） */
	String catalog(List<MotionTemplate> templates) {
		StringBuilder builder = new StringBuilder();
		for (MotionTemplate template : templates) {
			builder.append("- ").append(template.getTemplateKey())
					.append(" | ").append(template.getName())
					.append(" | 场景=").append(template.getScene())
					.append(" | 风格=").append(template.getStyle())
					.append(" | 技术=").append(template.getTechnology())
					.append(" | 难度=").append(template.getDifficulty())
					.append(" | 推荐指数=").append(template.getScore())
					.append(" | 参数=").append(paramNames(template.getParams()))
					.append('\n');
		}
		return builder.toString();
	}

	/** 从 params JSON 里取参数名（--m-xxx），给模型做参数建议的白名单 */
	String paramNames(String paramsJson) {
		if (paramsJson == null || paramsJson.isBlank()) {
			return "无";
		}
		try {
			List<Map<String, Object>> raw = OBJECT_MAPPER.readValue(paramsJson,
					OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, Map.class));
			List<String> names = new ArrayList<>();
			for (Map<String, Object> item : raw) {
				Object key = item.get("key");
				if (key != null) {
					names.add(String.valueOf(key));
				}
			}
			return names.isEmpty() ? "无" : String.join(",", names);
		} catch (Exception e) {
			return "无";
		}
	}

	/** 从 chat completions 响应里取出第一个 choice 的正文 */
	String extractContent(String body) {
		if (body == null || body.isBlank()) {
			return null;
		}
		try {
			JsonNode root = OBJECT_MAPPER.readTree(body);
			JsonNode content = root.path("choices").path(0).path("message").path("content");
			return content.isMissingNode() || content.isNull() ? null : content.asText();
		} catch (Exception e) {
			log.warn("模型响应不是合法 JSON：{}", e.getMessage());
			return null;
		}
	}

	/** 解析模型输出的 JSON（容忍 ```json 包裹） */
	ModelAnswer parse(String content) {
		String text = content.trim();
		if (text.startsWith("```")) {
			int start = text.indexOf('{');
			int end = text.lastIndexOf('}');
			if (start >= 0 && end > start) {
				text = text.substring(start, end + 1);
			}
		}
		try {
			JsonNode root = OBJECT_MAPPER.readTree(text);
			JsonNode intentNode = root.path("intent");
			JsonNode recipeNode = root.path("recipe");

			List<Match> matches = new ArrayList<>();
			for (JsonNode node : root.path("matches")) {
				if (node.path("templateKey").isTextual()) {
					matches.add(new Match(node.path("templateKey").asText(), node.path("reason").asText("")));
				}
			}

			List<Step> steps = new ArrayList<>();
			Map<String, Object> params = null;
			if (recipeNode.path("steps").isArray()) {
				for (JsonNode node : recipeNode.path("steps")) {
					if (!node.path("templateKey").isTextual()) {
						continue;
					}
					// 用 for 循环而不是 forEachRemaining：stepParams 在 lambda 里不是有效 final，
					// 而且这里本来就需要一个可变容器
					Map<String, Object> stepParams = new LinkedHashMap<>();
					if (node.path("params").isObject()) {
						var fields = node.path("params").fields();
						while (fields.hasNext()) {
							var entry = fields.next();
							stepParams.put(entry.getKey(), entry.getValue().isNumber()
									? entry.getValue().numberValue() : entry.getValue().asText());
						}
					}
					if (stepParams.isEmpty()) {
						stepParams = null;
					}
					steps.add(new Step(node.path("templateKey").asText(), node.path("role").asText(""), stepParams));
				}
			}

			Recipe recipe = steps.isEmpty() ? null : new Recipe(
					recipeNode.path("name").asText("AI 组合方案"),
					recipeNode.path("description").asText(""),
					recipeNode.path("bestFor").asText(""),
					steps);

			List<String> keywords = new ArrayList<>();
			for (JsonNode node : intentNode.path("keywords")) {
				if (node.isTextual()) {
					keywords.add(node.asText());
				}
			}
			Intent intent = new Intent(textOrNull(intentNode.path("scene")), textOrNull(intentNode.path("style")),
					textOrNull(intentNode.path("technology")), keywords,
					intentNode.path("summary").asText(""));

			return new ModelAnswer(intent, matches, recipe, root.path("advice").asText(""));
		} catch (Exception e) {
			log.warn("模型输出解析失败（按回退处理）：{}", e.getMessage());
			return null;
		}
	}

	/** 把模型给的步骤转成返回值：只保留真实存在的模板，回填名称与参数建议 */
	List<MotionAssistantVO.Step> toSteps(Recipe recipe, Map<String, MotionTemplate> byKey) {
		List<MotionAssistantVO.Step> steps = new ArrayList<>();
		for (Step step : recipe.steps()) {
			MotionTemplate template = byKey.get(step.templateKey());
			if (template == null) {
				log.warn("模型给出了不存在的模板 key，已丢弃：{}", step.templateKey());
				continue;
			}
			steps.add(MotionAssistantVO.Step.builder()
					.templateKey(step.templateKey())
					.templateName(template.getName())
					.role(step.role())
					.params(step.params())
					.build());
		}
		return steps;
	}

	/** 把模型的意图映射成系统里使用的字段（场景 / 风格 / 技术用英文枚举值） */
	String sceneOf(String raw) {
		return enumOrNull(raw, List.of("Landing Page", "Dashboard", "Portfolio", "Login", "AI SaaS", "Game UI"));
	}

	String styleOf(String raw) {
		return enumOrNull(raw, List.of("Minimal", "Luxury", "Cyber", "Glass", "Organic"));
	}

	String technologyOf(String raw) {
		return enumOrNull(raw, List.of("CSS", "Canvas", "Three.js", "GSAP", "Framer Motion"));
	}

	private String enumOrNull(String raw, List<String> allowed) {
		if (raw == null) {
			return null;
		}
		for (String value : allowed) {
			if (value.equalsIgnoreCase(raw.trim())) {
				return value;
			}
		}
		return null;
	}

	private String textOrNull(JsonNode node) {
		return node.isTextual() && !node.asText().isBlank() && !"null".equals(node.asText()) ? node.asText() : null;
	}

	private String trimEnd(String value) {
		String result = value.trim();
		while (result.endsWith("/")) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	private boolean notBlank(String value) {
		return value != null && !value.isBlank();
	}

	/** 模型返回的原始结构（与对外 VO 解耦，便于测试） */
	record ModelAnswer(Intent intent, List<Match> matches, Recipe recipe, String advice) {
	}

	record Intent(String scene, String style, String technology, List<String> keywords, String summary) {
	}

	record Match(String templateKey, String reason) {
	}

	record Recipe(String name, String description, String bestFor, List<Step> steps) {
	}

	record Step(String templateKey, String role, Map<String, Object> params) {
	}
}
