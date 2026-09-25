package com.kingdomstudio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Kingdom Studio 启动类。
 *
 * <p>V1 采用单体分层架构：Controller → Service → Mapper → Entity，
 * 统一响应体 {@code Result}，统一异常处理 {@code GlobalExceptionHandler}。
 *
 * <p>注意：{@code @MapperScan("com.kingdomstudio.mapper")} 会在 Phase 2
 * 添加 Mapper 接口时一起加上，当前还没有 Mapper 包。
 */
@EnableCaching
@SpringBootApplication
public class KingdomStudioApplication {

	public static void main(String[] args) {
		SpringApplication.run(KingdomStudioApplication.class, args);
	}
}
