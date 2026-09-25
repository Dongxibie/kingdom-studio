package com.kingdomstudio.controller;

import com.kingdomstudio.common.Result;
import com.kingdomstudio.service.HealthService;
import com.kingdomstudio.vo.HealthVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口：用于确认「应用 + MySQL + Redis」三件套是否都通了。
 *
 * <p>这是 Phase 1 的验收接口：{@code GET http://localhost:8080/api/health}
 *
 * <p>控制器只做接收与转发，探测逻辑在 {@link HealthService}。
 */
@Tag(name = "00. 健康检查", description = "不依赖任何业务表，用于启动自检")
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

	private final HealthService healthService;

	@Operation(summary = "服务健康检查", description = "检查应用、MySQL、Redis 的连通性")
	@GetMapping
	public Result<HealthVO> health() {
		return Result.success(healthService.check());
	}
}
