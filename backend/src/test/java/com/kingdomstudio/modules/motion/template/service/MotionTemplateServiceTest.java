package com.kingdomstudio.modules.motion.template.service;

import com.kingdomstudio.modules.motion.template.entity.MotionRecipe;
import com.kingdomstudio.modules.motion.template.entity.MotionTemplate;
import com.kingdomstudio.modules.motion.template.mapper.MotionRecipeMapper;
import com.kingdomstudio.modules.motion.template.mapper.MotionRatingMapper;
import com.kingdomstudio.modules.motion.template.mapper.MotionTemplateMapper;
import com.kingdomstudio.modules.motion.template.vo.MotionFacetVO;
import com.kingdomstudio.modules.motion.template.vo.MotionRecipeVO;
import com.kingdomstudio.modules.motion.template.vo.MotionTemplateDetailVO;
import com.kingdomstudio.modules.motion.template.vo.MotionTemplateItemVO;
import com.kingdomstudio.modules.motion.template.vo.TemplateParamVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 模板与组合方案的单元测试。
 *
 * <p>重点在两件容易走偏的事：推荐指数只能有一个算法（不要库里存一个、代码里算一个），
 * 以及组合方案的分数必须由成员实时算出，不能各自维护。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MotionTemplateServiceTest {

	@Mock
	private MotionTemplateMapper templateMapper;
	@Mock
	private MotionRecipeMapper recipeMapper;
	@Mock
	private MotionRatingMapper ratingMapper;

	private MotionTemplateService service;
	private MotionRecipeService recipeService;

	@BeforeEach
	void setUp() {
		service = new MotionTemplateService(templateMapper, recipeMapper, ratingMapper);
		recipeService = new MotionRecipeService(recipeMapper, templateMapper, service);
	}

	private MotionTemplate template(String key, String name, String category, String scene, String style,
			String technology, int difficulty, int visual, int code, int reuse, int perf) {
		MotionTemplate template = new MotionTemplate();
		template.setId(1L);
		template.setTemplateKey(key);
		template.setName(name);
		template.setNameEn(key);
		template.setDescription(name + " 的说明");
		template.setCategory(category);
		template.setScene(scene);
		template.setStyle(style);
		template.setTechnology(technology);
		template.setDifficulty(difficulty);
		template.setBestFor("官网首页,个人作品集");
		template.setScoreVisual(visual);
		template.setScoreCode(code);
		template.setScoreReuse(reuse);
		template.setScorePerf(perf);
		template.setScore(1); // 故意写一个错的值：服务必须重算，而不是读这一列
		template.setTags(key.replace('-', ' '));
		template.setParams("[{\"key\":\"--m-duration\",\"label\":\"时长\",\"unit\":\"s\",\"min\":0.3,\"max\":3,\"step\":0.1,\"default\":1.2}]");
		template.setPreviewHtml("<div class=\"motion-root\"></div>");
		template.setCssCode(".motion-root{}");
		return template;
	}

	@Test
	@DisplayName("推荐指数：四项子分加权（视觉30 / 代码25 / 复用25 / 性能20），不读库里缓存的那一列")
	void shouldComputeRecommendScore() {
		MotionTemplate item = template("hero-entrance", "首屏入场", "产品页面", "Landing Page", "Luxury", "CSS", 2,
				100, 80, 60, 40);

		// 100*0.3 + 80*0.25 + 60*0.25 + 40*0.2 = 30 + 20 + 15 + 8 = 73
		assertEquals(73, service.recommendScore(item));
		assertEquals("B", service.grade(73));
		assertEquals(3.5, service.stars(73));
	}

	@Test
	@DisplayName("等级与星级：90 分是 S、满星；89 分只差半星")
	void shouldMapGradeAndStars() {
		assertEquals("S", service.grade(92));
		assertEquals(4.5, service.stars(92));
		assertEquals("A", service.grade(89));
		assertEquals(4.5, service.stars(89));
		assertEquals("C", service.grade(60));
		assertEquals(3.0, service.stars(60));
	}

	@Test
	@DisplayName("详情：场景与风格带中文标签，参数解析成控件定义")
	void shouldBuildDetailWithLabelsAndParams() {
		when(templateMapper.selectOne(any())).thenReturn(
				template("glass-card-hover", "玻璃卡片悬停", "基础交互", "Portfolio", "Glass", "CSS", 2, 89, 92, 94, 84));
		when(recipeMapper.selectList(any())).thenReturn(List.of());

		MotionTemplateDetailVO detail = service.detail("glass-card-hover");

		assertEquals("个人主页", detail.getSceneLabel());
		assertEquals("玻璃", detail.getStyleLabel());
		assertEquals("进阶", detail.getDifficultyLabel());
		assertEquals(List.of("官网首页", "个人作品集"), detail.getBestFor());
		// 89*0.3 + 92*0.25 + 94*0.25 + 84*0.2 = 26.7 + 23 + 23.5 + 16.8 = 90.0
		assertEquals(90, detail.getScore());

		assertEquals(1, detail.getParams().size());
		TemplateParamVO param = detail.getParams().get(0);
		assertEquals("--m-duration", param.getKey());
		assertEquals("时长", param.getLabel());
		assertEquals(0.3, param.getMin());
		assertEquals(1.2, param.getDefaultValue());
	}

	@Test
	@DisplayName("详情：模板不存在时明确报 404，而不是返回空对象")
	void shouldRejectUnknownTemplate() {
		when(templateMapper.selectOne(any())).thenReturn(null);

		var error = assertThrows(RuntimeException.class, () -> service.detail("not-exists"));
		assertTrue(error.getMessage().contains("动效模板不存在"), error.getMessage());
	}

	@Test
	@DisplayName("发现方式：场景分面带数量与平均推荐指数，并给出中文标签")
	void shouldBuildFacets() {
		when(templateMapper.selectList(any())).thenReturn(List.of(
				template("a", "甲", "基础交互", "Landing Page", "Minimal", "CSS", 1, 90, 90, 90, 90),
				template("b", "乙", "基础交互", "Landing Page", "Luxury", "CSS", 2, 80, 80, 80, 80),
				template("c", "丙", "高级效果", "Portfolio", "Cyber", "Canvas", 3, 70, 70, 70, 70)));
		when(recipeMapper.selectCount(any())).thenReturn(5L);

		MotionFacetVO facets = service.facets();

		assertEquals(3L, facets.getTotal());
		assertEquals(5L, facets.getRecipeTotal());
		MotionFacetVO.FacetOption landing = facets.getScenes().stream()
				.filter(option -> "Landing Page".equals(option.getValue())).findFirst().orElseThrow();
		assertEquals("网站首页", landing.getLabel());
		assertEquals(2L, landing.getCount());
		assertEquals(85, landing.getAverageScore());
		assertEquals(2, facets.getScenes().size());
	}

	@Test
	@DisplayName("组合方案：推荐指数由成员实时平均，成员找不到时按剩余成员算并跳过")
	void shouldAverageRecipeScoreFromMembers() {
		MotionRecipe recipe = new MotionRecipe();
		recipe.setId(1L);
		recipe.setRecipeKey("premium-hero");
		recipe.setName("Premium Hero");
		recipe.setScene("Landing Page");
		recipe.setStyle("Luxury");
		recipe.setBestFor("官网首页");
		recipe.setScore(1); // 同样故意写错，服务必须重算
		recipe.setTemplateKeys("[\"a\",\"b\",\"missing\"]");
		recipe.setPrompt("做一个高级感首屏");
		when(recipeMapper.selectOne(any())).thenReturn(recipe);
		when(templateMapper.selectList(any())).thenReturn(List.of(
				template("b", "乙", "基础交互", "Landing Page", "Luxury", "CSS", 2, 100, 100, 100, 100),
				template("a", "甲", "基础交互", "Landing Page", "Luxury", "CSS", 1, 80, 80, 80, 80)));

		MotionRecipeVO vo = recipeService.detail("premium-hero");

		assertEquals(List.of("a", "b"), vo.getMemberKeys().subList(0, 2));
		assertEquals(2, vo.getMembers().size(), "不存在的成员被跳过");
		assertEquals("甲", vo.getMembers().get(0).getName(), "成员按方案里声明的顺序排列，而不是库里的顺序");
		assertEquals(90, vo.getScore(), "(80+100)/2 = 90");
		assertEquals("S", vo.getGrade());
		assertEquals("网站首页", vo.getSceneLabel());
	}

	@Test
	@DisplayName("组合方案：不存在时报 404")
	void shouldRejectUnknownRecipe() {
		when(recipeMapper.selectOne(any())).thenReturn(null);

		var error = assertThrows(RuntimeException.class, () -> recipeService.detail("nope"));
		assertTrue(error.getMessage().contains("组合方案不存在"), error.getMessage());
	}

	@Test
	@DisplayName("列表项：难度与场景都有中文标签，适合场景拆成数组")
	void shouldBuildListItem() {
		MotionTemplateItemVO item = service.toItem(
				template("smooth-fade", "柔和淡入", "基础交互", "Landing Page", "Minimal", "CSS", 1, 78, 96, 95, 98));

		assertEquals("入门", item.getDifficultyLabel());
		assertEquals("网站首页", item.getSceneLabel());
		assertEquals("极简", item.getStyleLabel());
		assertEquals(List.of("官网首页", "个人作品集"), item.getBestFor());
		assertEquals(91, item.getScore());
		assertEquals(4.5, item.getStars());
	}
}
