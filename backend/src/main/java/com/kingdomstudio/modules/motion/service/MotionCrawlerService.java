package com.kingdomstudio.modules.motion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingdomstudio.modules.motion.crawler.MotionClassifier;
import com.kingdomstudio.modules.motion.crawler.MotionDedupe;
import com.kingdomstudio.modules.motion.dto.CrawlRequestDTO;
import com.kingdomstudio.modules.motion.entity.MotionResource;
import com.kingdomstudio.modules.motion.mapper.MotionResourceMapper;
import com.kingdomstudio.modules.motion.vo.CrawlResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GitHub 动效采集。
 *
 * <p>三条纪律：
 * <ol>
 *   <li><b>不疯狂请求</b>：每个关键词之间固定间隔，读响应头里的 X-RateLimit-Remaining，
 *       剩余不足时直接停下并如实汇报，而不是继续撞限流。</li>
 *   <li><b>重试</b>：只有 5xx 与网络异常才重试，退避 1s / 2s；403/429 不重试（重试也没用）。</li>
 *   <li><b>缓存</b>：同一关键词的原始响应在 Redis 里缓存 30 分钟，重复点采集不会重复打接口。</li>
 * </ol>
 *
 * <p>令牌通过环境变量 {@code GITHUB_TOKEN} 注入；不带令牌也能跑，只是限额低（10 次/分钟）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotionCrawlerService {

	private static final String SEARCH_URL = "https://api.github.com/search/repositories";
	private static final List<String> DEFAULT_KEYWORDS = List.of(
			"css animation hover", "web animation particles", "three.js animation", "scroll animation library");
	private static final int MAX_PER_KEYWORD = 30;
	private static final int MAX_RETRY = 2;

	private final MotionResourceMapper motionResourceMapper;
	private final MotionClassifier classifier;
	private final MotionDedupe dedupe;
	private final StringRedisTemplate redisTemplate;
	/** 用注入的 Builder 而不是 RestClient.create()：测试里可以挂 MockRestServiceServer 验证限流与重试 */
	private final RestClient.Builder restClientBuilder;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Value("${kingdom.motion.crawl.token:${GITHUB_TOKEN:}}")
	private String token;

	@Value("${kingdom.motion.crawl.interval-ms:1200}")
	private long intervalMs;

	@Value("${kingdom.motion.crawl.cache-minutes:30}")
	private long cacheMinutes;

	public CrawlResultVO crawl(CrawlRequestDTO request) {
		List<String> keywords = (request.getKeywords() == null || request.getKeywords().isEmpty())
				? DEFAULT_KEYWORDS : request.getKeywords();
		int limit = Math.min(request.getLimitPerKeyword() == null ? 10 : request.getLimitPerKeyword(), MAX_PER_KEYWORD);
		boolean dryRun = Boolean.TRUE.equals(request.getDryRun());

		Map<String, Integer> byCategory = new LinkedHashMap<>();
		List<String> notes = new ArrayList<>();
		int[] counters = new int[5]; // scanned / created / byUrl / byContent / bySimilarity
		int failed = 0;
		List<MotionResource> existing = motionResourceMapper.selectList(new LambdaQueryWrapper<>());
		List<String> existingReadmes = new ArrayList<>();

		for (String keyword : keywords) {
			try {
				JsonNode payload = searchWithCache(keyword, limit, notes);
				if (payload == null) {
					continue;
				}
				JsonNode items = payload.path("items");
				for (JsonNode item : items) {
					counters[0]++;
					String repoUrl = item.path("html_url").asText("");
					String name = item.path("name").asText("");
					String description = item.path("description").asText("");
					String license = item.path("license").path("spdx_id").asText("");
					int stars = item.path("stargazers_count").asInt(0);
					String topics = item.path("topics").toString();

					MotionClassifier.Result classified = classifier.classify(name + " " + description + " " + topics);
					String sourceUrl = repoUrl;
					String hash = dedupe.contentHash(name, sourceUrl, classified.category());

					// 三级去重，从便宜到贵：同一个仓库地址 → 同一个内容指纹 → README 文本高度相似
					if (existing.stream().anyMatch(r -> sourceUrl.equalsIgnoreCase(r.getSourceUrl()))) {
						counters[2]++;
						continue;
					}
					if (existing.stream().anyMatch(r -> hash.equals(r.getContentHash()))) {
						counters[3]++;
						continue;
					}
					String readmeKey = name + " " + description;
					if (existingReadmes.stream().anyMatch(seen -> dedupe.isSimilar(seen, readmeKey))) {
						counters[4]++;
						continue;
					}

					if (!dryRun) {
						MotionResource resource = new MotionResource();
						resource.setName(name);
						resource.setDescription(trim(description, 600));
						resource.setCategory(classified.category());
						resource.setTechnology(item.path("language").asText(""));
						resource.setSourceUrl(sourceUrl);
						resource.setRepoUrl(repoUrl);
						resource.setPreviewUrl("");
						resource.setTags(String.join(",", topicsArray(item)));
						resource.setLicense("NOASSERTION".equals(license) || license.isBlank() ? "未标注" : license);
						resource.setCodePath("");
						resource.setContentHash(hash);
						// 没有命中任何关键词时降级成 DRAFT，交给人来确认分类，而不是硬塞
						resource.setStatus(classified.confident() ? "READY" : "DRAFT");
						try {
							motionResourceMapper.insert(resource);
							counters[1]++;
							existing.add(resource);
							existingReadmes.add(readmeKey);
							byCategory.merge(classified.category(), 1, Integer::sum);
						} catch (Exception e) {
							// 唯一键冲突等：算作已存在，不算失败
							counters[3]++;
							log.debug("插入被跳过：{}", e.getMessage());
						}
					} else {
						counters[1]++;
						byCategory.merge(classified.category(), 1, Integer::sum);
					}
				}
				notes.add(keyword + "：取回 " + items.size() + " 条");
			} catch (RateLimitedException e) {
				notes.add(keyword + "：触发限流，已停止（" + e.getMessage() + "）");
				break;
			} catch (Exception e) {
				failed++;
				notes.add(keyword + "：失败（" + e.getClass().getSimpleName() + "）");
				log.warn("采集失败 keyword={}", keyword, e);
			}
			sleep(intervalMs);
		}

		return CrawlResultVO.builder()
				.scanned(counters[0])
				.created(counters[1])
				.skippedByUrl(counters[2])
				.skippedByContent(counters[3])
				.skippedBySimilarity(counters[4])
				.failed(failed)
				.dryRun(dryRun)
				.byCategory(byCategory)
				.notes(notes)
				.build();
	}

	/** 带 Redis 缓存的搜索：同关键词 30 分钟内不重复打接口 */
	private JsonNode searchWithCache(String keyword, int limit, List<String> notes) throws Exception {
		String cacheKey = "kingdom:motion:crawl:" + dedupe.urlHash(keyword + ":" + limit);
		try {
			String cached = redisTemplate.opsForValue().get(cacheKey);
			if (cached != null && !cached.isBlank()) {
				notes.add(keyword + "：命中缓存");
				return objectMapper.readTree(cached);
			}
		} catch (Exception e) {
			// Redis 不可用不该阻断采集，降级为直连
			log.debug("缓存读取失败，降级直连：{}", e.getMessage());
		}

		JsonNode payload = requestSearch(keyword, limit);
		try {
			redisTemplate.opsForValue().set(cacheKey, payload.toString(), Duration.ofMinutes(cacheMinutes));
		} catch (Exception e) {
			log.debug("缓存写入失败：{}", e.getMessage());
		}
		return payload;
	}

	/** 真正发请求：重试只针对 5xx 与网络异常，403/429 直接抛限流异常 */
	private JsonNode requestSearch(String keyword, int limit) throws Exception {
		String url = SEARCH_URL + "?q=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8)
				+ "&sort=stars&order=desc&per_page=" + limit;
		Exception lastError = null;
		for (int attempt = 0; attempt <= MAX_RETRY; attempt++) {
			try {
				RestClient.RequestHeadersSpec<?> spec = restClientBuilder.build()
						.get()
						.uri(url)
						.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
						.header(HttpHeaders.USER_AGENT, "kingdom-studio-motion-lab");
				if (token != null && !token.isBlank()) {
					spec = spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token.trim());
				}
				var response = spec.retrieve().toEntity(String.class);
				String remaining = response.getHeaders().getFirst("X-RateLimit-Remaining");
				if (remaining != null && "0".equals(remaining.trim())) {
					throw new RateLimitedException("GitHub 限额已用尽，重置时间 " + response.getHeaders().getFirst("X-RateLimit-Reset"));
				}
				return objectMapper.readTree(response.getBody());
			} catch (RateLimitedException e) {
				throw e;
			} catch (RestClientResponseException e) {
				// 按状态码分流，不去猜异常消息里有没有「403」这种字样
				int status = e.getStatusCode().value();
				if (status == 403 || status == 429) {
					throw new RateLimitedException("GitHub 拒绝请求（HTTP " + status + "），可能是未配置令牌或调用过快");
				}
				if (status >= 500 && attempt < MAX_RETRY) {
					lastError = e;
					sleep(1000L * (attempt + 1));
					continue;
				}
				// 其余 4xx 是请求本身的问题（查询串非法等），重试没有意义
				throw e;
			} catch (Exception e) {
				// 网络层异常值得重试
				lastError = e;
				if (attempt < MAX_RETRY) {
					sleep(1000L * (attempt + 1));
				}
			}
		}
		throw lastError == null ? new IllegalStateException("采集失败") : lastError;
	}

	private List<String> topicsArray(JsonNode item) {
		List<String> topics = new ArrayList<>();
		item.path("topics").forEach(node -> topics.add(node.asText("")));
		return topics;
	}

	private static String trim(String value, int max) {
		if (value == null) {
			return "";
		}
		return value.length() <= max ? value : value.substring(0, max);
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/** 命中限流时用的专用异常：区分「网络抖动可以重试」和「限额用尽不要重试」 */
	public static class RateLimitedException extends RuntimeException {
		public RateLimitedException(String message) {
			super(message);
		}
	}

	/** 供 Controller 展示的默认关键词 */
	public List<String> defaultKeywords() {
		return DEFAULT_KEYWORDS;
	}
}
