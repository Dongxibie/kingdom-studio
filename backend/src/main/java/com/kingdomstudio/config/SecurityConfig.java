package com.kingdomstudio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 配置。
 *
 * <p><b>Phase 1 的状态</b>：接口全部放行，只把「无状态 + 关闭 CSRF + BCrypt 编码器 + CORS」
 * 这些骨架立好，方便后面直接接 JWT。
 *
 * <p><b>Phase 2 要做的事</b>：
 * <ol>
 *   <li>删掉下面的 {@code anyRequest().permitAll()}，改成 {@code anyRequest().authenticated()}</li>
 *   <li>在 UsernamePasswordAuthenticationFilter 之前插入 JwtAuthenticationFilter</li>
 *   <li>加上 {@code exceptionHandling} 把 401/403 也转成统一响应格式</li>
 * </ol>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	/** 无需登录即可访问的路径 */
	private static final String[] PUBLIC_ENDPOINTS = {
			"/health",
			"/auth/login",
			"/swagger-ui.html",
			"/swagger-ui/**",
			"/v3/api-docs/**",
			"/actuator/**"
	};

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource))
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(PUBLIC_ENDPOINTS).permitAll()
						// TODO Phase 2：改成 .anyRequest().authenticated()
						.anyRequest().permitAll());

		return http.build();
	}

	/** 密码编码器：种子数据里的密码就是用这个算法生成的 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
