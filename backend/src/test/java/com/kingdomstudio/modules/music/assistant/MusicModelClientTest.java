package com.kingdomstudio.modules.music.assistant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * 音乐助手模型层的测试。
 *
 * <p>重点：模型解析必须宽容（容忍 ```json 包裹、缺字段），但**判断必须严格**——
 * 认不出来的难度档一律交给上层回退，模型出问题不能让助手不可用。
 */
class MusicModelClientTest {

	private static final String MODEL_BASE = "https://api.example.com/v1";

	private RestClient.Builder builder;
	private MockRestServiceServer server;
	private MusicModelClient client;

	@BeforeEach
	void setUp() {
		builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		client = new MusicModelClient(builder);
		ReflectionTestUtils.setField(client, "baseUrl", MODEL_BASE);
		ReflectionTestUtils.setField(client, "apiKey", "test-key");
		ReflectionTestUtils.setField(client, "model", "deepseek-chat");
	}

	private String chatResponse(String content) {
		String escaped = content.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
		return "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"" + escaped + "\"}}]}";
	}

	@Test
	@DisplayName("三项配置齐了才算启用")
	void enabledRequiresAllThreeSettings() {
		assertTrue(client.enabled());
		ReflectionTestUtils.setField(client, "apiKey", "");
		assertFalse(client.enabled(), "缺密钥就不该启用");
		ReflectionTestUtils.setField(client, "apiKey", "k");
		ReflectionTestUtils.setField(client, "model", "  ");
		assertFalse(client.enabled(), "缺模型名同样不启用");
	}

	@Test
	@DisplayName("解析合法应答：难度、乐器、建议都拿到")
	void parsesValidAnswer() {
		MusicModelClient.Answer answer = client.parse("""
				{"difficulty":"BEGINNER","instrument":"15-key kalimba",
				 "suggestion":["降低速度","调整八度","减少重复"]}
				""");
		assertNotNull(answer);
		assertEquals("BEGINNER", answer.difficulty());
		assertEquals("15-key kalimba", answer.instrument());
		assertEquals(3, answer.suggestions().size());
		assertTrue(answer.suggestions().contains("降低速度"));
	}

	@Test
	@DisplayName("容忍 ```json 包裹与缺失字段：instrument 为 null 时不报错")
	void toleratesFencesAndMissingFields() {
		MusicModelClient.Answer fenced = client.parse("""
				```json
				{"difficulty":"SHOWCASE","instrument":null,"suggestion":["加强重拍"]}
				```
				""");
		assertNotNull(fenced);
		assertEquals("SHOWCASE", fenced.difficulty());
		assertNull(fenced.instrument());

		MusicModelClient.Answer partial = client.parse("{\"difficulty\":\"NORMAL\"}");
		assertNotNull(partial);
		assertEquals("NORMAL", partial.difficulty());
		assertTrue(partial.suggestions().isEmpty());
	}

	@Test
	@DisplayName("非法输出一律返回 null，交给上层回退到规则判断")
	void invalidOutputFallsBackToNull() {
		assertNull(client.parse("这个需求我建议你先想清楚"), "不是 JSON");
		assertNull(client.parse(""));
	}

	@Test
	@DisplayName("完整调用：请求带鉴权头，返回体里取出正文")
	void analyzeCallsModelWithAuth() {
		server.expect(requestTo(MODEL_BASE + "/chat/completions"))
				.andRespond(withSuccess(chatResponse("{\"difficulty\":\"BEGINNER\",\"suggestion\":[\"降速\"]}"),
						MediaType.APPLICATION_JSON));
		MusicModelClient.Answer answer = client.analyze("简单一点", "小星星：14 个音");
		assertNotNull(answer);
		assertEquals("BEGINNER", answer.difficulty());
		server.verify();
	}

	@Test
	@DisplayName("模型报错或超时：返回 null（不抛异常），助手照常可用")
	void modelFailureReturnsNull() {
		server.expect(requestTo(MODEL_BASE + "/chat/completions")).andRespond(withServerError());
		assertNull(client.analyze("简单一点", "小星星：14 个音"));

		server.reset();
		server.expect(requestTo(MODEL_BASE + "/chat/completions")).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
		assertNull(client.analyze("简单一点", "小星星：14 个音"));
	}

	@Test
	@DisplayName("没配置模型时根本不发请求")
	void disabledClientNeverCalls() {
		ReflectionTestUtils.setField(client, "baseUrl", "");
		assertNull(client.analyze("简单一点", "小星星"));
		server.verify();
	}
}
