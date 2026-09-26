package com.kingdomstudio.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 接口文档。
 *
 * <p>访问地址：{@code http://localhost:8080/api/swagger-ui.html}
 *
 * <p>这里只声明 bearerAuth 这个安全方案，没有给全部接口加上全局鉴权要求；
 * Phase 2 接入 JWT 后，需要鉴权的接口用 {@code @SecurityRequirement(name = "bearerAuth")} 单独标注。
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI kingdomStudioOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Kingdom Studio API")
						.description("个人开发者工作台 —— 项目王国 / 技术图鉴 / 成长时间线 / 代码知识库")
						.version("v1.0.1")
						.license(new License().name("MIT"))
						.contact(new Contact().name("Dongxibie").url("https://github.com/Dongxibie")))
				.components(new Components().addSecuritySchemes("bearerAuth",
						new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("登录接口返回的 token，填入时不需要自己加 Bearer 前缀")));
	}
}
