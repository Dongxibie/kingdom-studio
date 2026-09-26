package com.kingdomstudio.modules.motion.template.service;

import com.kingdomstudio.modules.motion.template.dto.MotionSearchDTO;
import com.kingdomstudio.modules.motion.template.entity.MotionTemplate;
import com.kingdomstudio.modules.motion.template.mapper.MotionRecipeMapper;
import com.kingdomstudio.modules.motion.template.mapper.MotionRatingMapper;
import com.kingdomstudio.modules.motion.template.mapper.MotionTemplateMapper;
import com.kingdomstudio.modules.motion.template.vo.MotionAssistantVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Motion Assistant（模型优先 + 回退）测试。
 *
 * <p>最要紧的两条：模型**没有**生成代码的自由（只能挑现成模板），
 * 以及模型出任何问题都不能让搜索不可用（必须回退且如实说明）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MotionAssistantModelTest {

	private static final String MODEL_BASE = "https://api.example.com/v1";

	@Mock
	private MotionTemplateMapper templateMapper;
	@Mock
	private MotionRecipeMapper recipeMapper;
	@Mock
	private MotionRatingMapper ratingMapper;

	private RestClient.Builder builder;
	private MockRestServiceServer server;
	private MotionModelClient client;
	private MotionAssistantService service;

	private MotionTemplate template(String key, String name, String scene, String style, String technology) {
		MotionTemplate item = new MotionTemplate();
		item.setId(1L);
		item.setTemplateKey(key);
		item.setName(name);
		item.setNameEn(key);
		item.setDescription(name);
		item.setCategory("产品页面");
		item.setScene(scene);
		item.setStyle(style);
		item.setTechnology(technology);
		item.setDifficulty(2);
		item.setBestFor("官网首页");
		item.setScoreVisual(90);
		item.setScoreCode(90);
		item.setScoreReuse(90);
		item.setScorePerf(90);
		item.setScore(90);
		item.setTags(key);
		item.setParams("[{\"key\":\"--m-duration\",\"label\":\"时长\",\"unit\":\"s\",\"min\":0.2,\"max\":3,\"step\":0.1,\"default\":1}]");
		item.setPreviewHtml("<div class=\"motion-root\"></div>");
		item.setCssCode(".motion-root{}");
		return item;
	}

	@BeforeEach
	void setUp() {
		builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		client = new MotionModelClient(builder);
		ReflectionTestUtils.setField(client, "baseUrl", MODEL_BASE);
		ReflectionTestUtils.setField(client, "apiKey", "test-key");
		ReflectionTestUtils.setField(client, "model", "test-model");

		MotionTemplateService templateService = new MotionTemplateService(templateMapper, recipeMapper, ratingMapper);
		MotionRecipeService recipeService = new MotionRecipeService(recipeMapper, templateMapper, templateService);
		service = new MotionAssistantService(templateMapper, recipeMapper, templateService, recipeService, client);

		when(templateMapper.selectList(any())).thenReturn(List.of(
				template("hero-entrance", "首屏入场", "Landing Page", "Luxury", "CSS"),
				template("aurora-background", "极光背景", "AI SaaS", "Cyber", "CSS"),
				template("three-scene", "三维场景", "Game UI", "Cyber", "Three.js")));
		when(recipeMapper.selectList(any())).thenReturn(List.of());
	}

	private String chatResponse(String content) {
		return "{\"choices\":[{\"message\":{\"content\":" + escapeJson(content) + "}}]}";
	}

	private String escapeJson(String raw) {
		return "\"" + raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
	}

	private MotionSearchDTO request(String query) {
		MotionSearchDTO dto = new MotionSearchDTO();
		dto.setQuery(query);
		dto.setLimit(5);
		return dto;
	}

	@Test
	@DisplayName("模型可用：返回模型给的意图、推荐与组合方案，并标注来源为 MODEL")
	void shouldUseModelWhenAvailable() {
		String modelJson = """
				{"intent":{"scene":"Landing Page","style":"Luxury","technology":"CSS",
				  "keywords":["hero","首屏"],"summary":"你要一个高级感的网站首屏动画"},
				 "matches":[{"templateKey":"hero-entrance","reason":"正是首屏入场"},
				            {"templateKey":"aurora-background","reason":"可做背景氛围"}],
				 "recipe":{"name":"高级首屏组合","description":"背景铺氛围 + 首屏入场",
				   "bestFor":"官网首页",
				   "steps":[{"templateKey":"aurora-background","role":"铺背景","params":{"--m-duration":12}},
				            {"templateKey":"hero-entrance","role":"主视觉入口","params":{"--m-duration":0.9}}]},
				 "advice":"先看组合方案，再按需微调时长。"}""";
		server.expect(requestTo(containsString("/chat/completions")))
				.andRespond(withSuccess(chatResponse(modelJson), MediaType.APPLICATION_JSON));

		MotionAssistantVO vo = service.assist(request("做一个苹果官网风格的首屏动画"));

		assertEquals("MODEL", vo.getSource());
		assertEquals("test-model", vo.getModelName());
		assertEquals("网站首页", vo.getIntent().getSceneLabel());
		assertEquals("高级", vo.getIntent().getStyleLabel());
		assertEquals(2, vo.getMatches().size());
		assertNotNull(vo.getRecipeSuggestion());
		assertEquals("高级首屏组合", vo.getRecipeSuggestion().getName());
		assertEquals(List.of("aurora-background", "hero-entrance"),
				vo.getRecipeSuggestion().getSteps().stream().map(MotionAssistantVO.Step::getTemplateKey).toList());
		assertEquals("极光背景", vo.getRecipeSuggestion().getSteps().get(0).getTemplateName(), "模板名由服务端按 key 回填");
		assertEquals(12, ((Number) vo.getRecipeSuggestion().getSteps().get(0).getParams().get("--m-duration")).intValue());
		server.verify();
	}

	@Test
	@DisplayName("模型杜撰模板 key：直接丢弃，不污染推荐结果")
	void shouldDropInventedTemplateKeys() {
		String modelJson = """
				{"intent":{"scene":"Landing Page","style":"Luxury","technology":"CSS","keywords":[],"summary":"测试"},
				 "matches":[{"templateKey":"hero-entrance","reason":"存在"},
				            {"templateKey":"nonexistent-effect","reason":"模型编出来的"}],
				 "recipe":{"name":"组合","description":"","bestFor":"",
				   "steps":[{"templateKey":"nonexistent-effect","role":"不存在"}]},
				 "advice":""}""";
		server.expect(requestTo(containsString("/chat/completions")))
				.andRespond(withSuccess(chatResponse(modelJson), MediaType.APPLICATION_JSON));

		MotionAssistantVO vo = service.assist(request("随便试试"));

		assertEquals(1, vo.getMatches().size());
		assertEquals("hero-entrance", vo.getMatches().get(0).getKey());
		assertNull(vo.getRecipeSuggestion(), "方案里只剩不存在的成员，应整条作废");
		server.verify();
	}

	@Test
	@DisplayName("模型返回不是合法 JSON：回退到内置检索，并说明原因")
	void shouldFallbackWhenModelReturnsGarbage() {
		server.expect(requestTo(containsString("/chat/completions")))
				.andRespond(withSuccess(chatResponse("这个需求我建议你先想清楚"), MediaType.APPLICATION_JSON));

		MotionAssistantVO vo = service.assist(request("首屏动画"));

		assertEquals("RULE", vo.getSource());
		assertTrue(vo.getFallbackReason().contains("模型"), vo.getFallbackReason());
		assertFalse(vo.getMatches().isEmpty(), "回退后仍然要给出结果");
		server.verify();
	}

	@Test
	@DisplayName("模型服务 5xx：回退到内置检索，不让搜索整体失败")
	void shouldFallbackWhenModelFails() {
		server.expect(requestTo(containsString("/chat/completions"))).andRespond(withServerError());

		MotionAssistantVO vo = service.assist(request("首屏动画"));

		assertEquals("RULE", vo.getSource());
		assertFalse(vo.getMatches().isEmpty());
		server.verify();
	}

	@Test
	@DisplayName("模型返回 401：同样回退，并如实说明是模型侧的问题")
	void shouldFallbackWhenUnauthorized() {
		server.expect(requestTo(containsString("/chat/completions"))).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

		MotionAssistantVO vo = service.assist(request("首屏动画"));

		assertEquals("RULE", vo.getSource());
		assertFalse(vo.getMatches().isEmpty());
		server.verify();
	}

	@Test
	@DisplayName("没配模型：不发请求，直接用内置检索并说明未配置")
	void shouldUseRuleSearchWhenModelNotConfigured() {
		ReflectionTestUtils.setField(client, "apiKey", "");

		MotionAssistantVO vo = service.assist(request("首屏动画"));

		assertEquals("RULE", vo.getSource());
		assertTrue(vo.getFallbackReason().contains("未配置模型"), vo.getFallbackReason());
		server.verify();
	}

	@Test
	@DisplayName("提示词里带上了模板清单与参数名白名单，且明确不许生成代码")
	void promptShouldCarryCatalogAndBanCode() {
		String catalog = client.catalog(List.of(template("hero-entrance", "首屏入场", "Landing Page", "Luxury", "CSS")));

		assertTrue(catalog.contains("hero-entrance"));
		assertTrue(catalog.contains("场景=Landing Page"));
		assertTrue(catalog.contains("--m-duration"), "参数名要作为白名单给模型：" + catalog);
	}

	@Test
	@DisplayName("运行档位规则：Three.js / Canvas 一律 GPU Enhanced，CSS 按性能分档")
	void shouldGradeRuntimeTier() {
		MotionTemplateService templateService = new MotionTemplateService(templateMapper, recipeMapper, ratingMapper);

		MotionTemplate three = template("three-scene", "三维场景", "Game UI", "Cyber", "Three.js");
		three.setScorePerf(74);
		assertEquals("GPU_ENHANCED", templateService.runtimeTier(three));
		assertEquals("依赖 GPU 加速", templateService.runtimeTierLabel(templateService.runtimeTier(three)));

		MotionTemplate canvas = template("shader-background", "着色器波纹", "Game UI", "Cyber", "Canvas");
		canvas.setScorePerf(76);
		assertEquals("GPU_ENHANCED", templateService.runtimeTier(canvas));

		MotionTemplate lightCss = template("noise-texture", "噪点质感", "Portfolio", "Organic", "CSS");
		lightCss.setScorePerf(94);
		assertEquals("LIGHTWEIGHT", templateService.runtimeTier(lightCss));

		MotionTemplate balancedCss = template("aurora-background", "极光背景", "AI SaaS", "Cyber", "CSS");
		balancedCss.setScorePerf(82);
		assertEquals("BALANCED", templateService.runtimeTier(balancedCss));

		MotionTemplate heavyCss = template("heavy-blur", "多层模糊", "Portfolio", "Glass", "CSS");
		heavyCss.setScorePerf(70);
		assertEquals("GPU_ENHANCED", templateService.runtimeTier(heavyCss));
	}

	@Test
	@DisplayName("运行建议：库里存过就用库里的，没存过按档位给通用建议")
	void shouldExplainRuntimeCost() {
		MotionTemplateService templateService = new MotionTemplateService(templateMapper, recipeMapper, ratingMapper);

		MotionTemplate withNote = template("three-scene", "三维场景", "Game UI", "Cyber", "Three.js");
		withNote.setRuntimeNote("自定义说明");
		assertEquals("自定义说明", templateService.runtimeNote(withNote));

		MotionTemplate withoutNote = template("shader-background", "着色器波纹", "Game UI", "Cyber", "Canvas");
		assertTrue(templateService.runtimeNote(withoutNote).contains("GPU"), templateService.runtimeNote(withoutNote));
	}
}
