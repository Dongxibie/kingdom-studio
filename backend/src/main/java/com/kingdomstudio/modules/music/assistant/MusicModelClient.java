package com.kingdomstudio.modules.music.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 音乐助手的模型调用层（OpenAI 兼容的 chat completions）。
 *
 * <p><b>职责边界</b>：模型只做「理解」—— 把一句自然语言要求翻译成
 * {难度档 / 目标乐器 / 建议清单}；它<b>不生成音乐</b>，也不直接执行任何东西。
 * 真正的改写由 {@code StrategyApplier} 的确定性规则完成，
 * 模型给的结果还要先过服务端的白名单校验（见 MusicAssistantService）。
 *
 * <p>没配密钥、超时、返回不是合法 JSON —— 任何一种情况都回退到规则模式，
 * 界面上如实说明这次是模型给的还是规则给的。
 */
@Slf4j
@Service
public class MusicModelClient {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final String SYSTEM_PROMPT = """
			你是音乐演奏助手，负责把使用者的自然语言要求翻译成结构化的「演奏方案选择」。你不作曲、不生成音频、
			不输出乐谱，只做三件事：判断难度档、判断目标乐器、给出可执行的调整建议。

			可选难度档（只能从这三个里选，写英文）：
			- BEGINNER：初学、想简单一点、想先顺下来
			- NORMAL：保持原曲
			- SHOWCASE：演出、展示、想更有表现力

			只输出一个 JSON 对象，不要解释文字，不要用 markdown 代码块包裹：
			{
			  "difficulty": "BEGINNER|NORMAL|SHOWCASE",
			  "instrument": "使用者提到的乐器，例如 15-key kalimba / 口风琴；没提到就写 null",
			  "suggestion": ["3-5 条具体建议，每条一句话，中文"]
			}
			""";

	private final RestClient.Builder restClientBuilder;

	@Value("${kingdom.music.assistant.base-url:${MUSIC_LLM_BASE_URL:}}")
	private String baseUrl;

	@Value("${kingdom.music.assistant.api-key:${MUSIC_LLM_API_KEY:}}")
	private String apiKey;

	@Value("${kingdom.music.assistant.model:${MUSIC_LLM_MODEL:}}")
	private String model;

	@Value("${kingdom.music.assistant.timeout-seconds:25}")
	private long timeoutSeconds;

	public MusicModelClient(RestClient.Builder restClientBuilder) {
		this.restClientBuilder = restClientBuilder;
	}

	/** 三项配置齐了才算启用 */
	public boolean enabled() {
		return notBlank(baseUrl) && notBlank(apiKey) && notBlank(model);
	}

	public String modelName() {
		return model;
	}

	/**
	 * 让模型理解使用者的要求。
	 *
	 * @param prompt      使用者原话
	 * @param songSummary 曲子概况（音数、音域、速度），让模型知道在谈什么曲子
	 * @return 解析结果；任何问题都返回 null，由调用方回退到规则模式
	 */
	public Answer analyze(String prompt, String songSummary) {
		if (!enabled()) {
			return null;
		}
		try {
			String payload = OBJECT_MAPPER.writeValueAsString(Map.of(
					"model", model,
					"temperature", 0.2,
					"messages", List.of(
							Map.of("role", "system", "content", SYSTEM_PROMPT),
							Map.of("role", "user", "content", "曲子概况：" + songSummary + "\n要求：" + prompt))));
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
				log.warn("音乐助手：模型没有返回可用内容");
				return null;
			}
			return parse(content);
		} catch (Exception error) {
			log.warn("音乐助手：模型调用失败，回退到规则模式：{}", error.getMessage());
			return null;
		}
	}

	String extractContent(String body) {
		if (body == null || body.isBlank()) {
			return null;
		}
		try {
			JsonNode content = OBJECT_MAPPER.readTree(body).path("choices").path(0).path("message").path("content");
			return content.isMissingNode() || content.isNull() ? null : content.asText();
		} catch (Exception error) {
			log.warn("音乐助手：模型响应不是合法 JSON：{}", error.getMessage());
			return null;
		}
	}

	/** 解析模型输出（容忍 ```json 包裹） */
	Answer parse(String content) {
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
			List<String> suggestions = new ArrayList<>();
			for (JsonNode node : root.path("suggestion")) {
				if (node.isTextual() && !node.asText().isBlank()) {
					suggestions.add(node.asText().trim());
				}
			}
			JsonNode difficulty = root.path("difficulty");
			JsonNode instrument = root.path("instrument");
			// 模型有时会回一段自然语言（不是 JSON 对象），那种情况当作「没返回」，
			// 否则会得到一个全是 null 的 Answer，让上层以为模型真的分析了
			if (!root.isObject() || (!difficulty.isTextual() && suggestions.isEmpty())) {
				log.warn("音乐助手：模型返回里没有任何可用字段，按回退处理");
				return null;
			}
			return new Answer(
					difficulty.isTextual() ? difficulty.asText() : null,
					instrument.isTextual() && !"null".equalsIgnoreCase(instrument.asText())
							? instrument.asText() : null,
					suggestions);
		} catch (Exception error) {
			log.warn("音乐助手：模型输出解析失败（按回退处理）：{}", error.getMessage());
			return null;
		}
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

	/** 模型给出的原始结构（与对外 VO 解耦，便于测试） */
	public record Answer(String difficulty, String instrument, List<String> suggestions) {

		public Map<String, Object> toMap() {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("difficulty", difficulty);
			map.put("instrument", instrument);
			map.put("suggestion", suggestions);
			return map;
		}
	}
}
