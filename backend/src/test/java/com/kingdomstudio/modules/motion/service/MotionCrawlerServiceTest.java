package com.kingdomstudio.modules.motion.service;

import com.kingdomstudio.modules.motion.crawler.MotionClassifier;
import com.kingdomstudio.modules.motion.crawler.MotionDedupe;
import com.kingdomstudio.modules.motion.dto.CrawlRequestDTO;
import com.kingdomstudio.modules.motion.entity.MotionResource;
import com.kingdomstudio.modules.motion.mapper.MotionResourceMapper;
import com.kingdomstudio.modules.motion.vo.CrawlResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * 采集服务单元测试：走 MockRestServiceServer 而不打真实 GitHub，
 * 重点验证「不疯狂请求」这条纪律能不能守住 —— 限额用尽立刻停、403 不重试、5xx 才退避重试。
 */
@ExtendWith(MockitoExtension.class)
class MotionCrawlerServiceTest {

	@Mock
	private MotionResourceMapper motionResourceMapper;
	@Mock
	private StringRedisTemplate redisTemplate;

	private MotionDedupe dedupe;
	private RestClient.Builder builder;
	private MockRestServiceServer server;
	private MotionCrawlerService service;

	@BeforeEach
	void setUp() {
		dedupe = new MotionDedupe();
		// 分类器用真实现（纯函数），去重也用真实现，只把网络与数据库换成替身
		MotionClassifier classifier = new MotionClassifier();
		builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		service = new MotionCrawlerService(motionResourceMapper, classifier, dedupe, redisTemplate, builder);
	}

	private static final String ONE_ITEM = """
			{"total_count":1,"items":[{"name":"hover-shine","description":"css hover glow shine sweep",
			"html_url":"https://github.com/demo/hover-shine","language":"CSS",
			"stargazers_count":42,"topics":["animation"],"license":{"spdx_id":"MIT"}}]}
			""";

	@Test
	@DisplayName("dryRun：只试算不落库，按分类汇总")
	void dryRunShouldNotInsert() {
		server.expect(requestTo(containsString("search/repositories")))
				.andRespond(withSuccess(ONE_ITEM, MediaType.APPLICATION_JSON));

		CrawlResultVO result = service.crawl(request(List.of("css animation hover"), 3, true));

		assertEquals(1, result.getScanned());
		assertEquals(1, result.getCreated());
		assertTrue(result.getDryRun());
		assertEquals(1, result.getByCategory().get("Hover"));
		verify(motionResourceMapper, never()).insert(any(MotionResource.class));
		server.verify();
	}

	@Test
	@DisplayName("正式采集：落库一条，状态为 READY")
	void realRunShouldInsert() {
		server.expect(requestTo(containsString("search/repositories")))
				.andRespond(withSuccess(ONE_ITEM, MediaType.APPLICATION_JSON));

		CrawlResultVO result = service.crawl(request(List.of("css animation hover"), 3, false));

		assertEquals(1, result.getCreated());
		assertEquals(0, result.getFailed());
		verify(motionResourceMapper).insert(any(MotionResource.class));
		server.verify();
	}

	@Test
	@DisplayName("限额用尽：立即停止，不再打下一个关键词")
	void exhaustedQuotaShouldStop() {
		server.expect(requestTo(containsString("search/repositories")))
				.andRespond(withSuccess(ONE_ITEM, MediaType.APPLICATION_JSON)
						.header("X-RateLimit-Remaining", "0")
						.header("X-RateLimit-Reset", "1800000000"));
		// 刻意不给第二个关键词准备期望：真去请求就会因缺少期望而抛错、被计成失败
		CrawlResultVO result = service.crawl(request(List.of("css animation hover", "three.js animation"), 3, true));

		assertEquals(0, result.getCreated(), "限额用尽的响应体不应被采信");
		assertEquals(0, result.getFailed(), "第二个关键词不应再发请求");
		assertTrue(result.getNotes().stream().anyMatch(note -> note.contains("触发限流")),
				"notes 里应如实说明限流：" + result.getNotes());
		server.verify();
	}

	@Test
	@DisplayName("403：按限流处理且不重试")
	void forbiddenShouldNotRetry() {
		server.expect(requestTo(containsString("search/repositories"))).andRespond(withStatus(HttpStatus.FORBIDDEN));
		// 同样不给第二次期望：403 若被重试，重试请求会撞上「没有更多期望」而变成 failed

		CrawlResultVO result = service.crawl(request(List.of("css animation hover"), 3, true));

		assertTrue(result.getNotes().stream().anyMatch(note -> note.contains("触发限流")), result.getNotes().toString());
		assertEquals(0, result.getFailed(), "限流不算关键词失败，也说明没有重试");
		server.verify();
	}

	@Test
	@DisplayName("5xx：退避后重试并最终成功")
	void serverErrorShouldRetry() {
		server.expect(requestTo(containsString("search/repositories"))).andRespond(withServerError());
		server.expect(requestTo(containsString("search/repositories")))
				.andRespond(withSuccess(ONE_ITEM, MediaType.APPLICATION_JSON));

		CrawlResultVO result = service.crawl(request(List.of("css animation hover"), 3, true));

		assertEquals(1, result.getScanned(), "第二次成功，扫描计数应为 1");
		assertEquals(0, result.getFailed());
		server.verify();
	}

	@Test
	@DisplayName("422：请求本身有问题，重试没有意义，计入失败")
	void badRequestShouldFailFast() {
		server.expect(requestTo(containsString("search/repositories"))).andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

		CrawlResultVO result = service.crawl(request(List.of("css animation hover"), 3, true));

		assertEquals(1, result.getFailed());
		assertTrue(result.getNotes().stream().anyMatch(note -> note.contains("失败")), result.getNotes().toString());
		server.verify();
	}

	private CrawlRequestDTO request(List<String> keywords, int limit, boolean dryRun) {
		CrawlRequestDTO dto = new CrawlRequestDTO();
		dto.setKeywords(keywords);
		dto.setLimitPerKeyword(limit);
		dto.setDryRun(dryRun);
		return dto;
	}
}
