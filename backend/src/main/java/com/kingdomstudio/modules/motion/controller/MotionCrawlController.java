package com.kingdomstudio.modules.motion.controller;

import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.motion.dto.CrawlRequestDTO;
import com.kingdomstudio.modules.motion.service.MotionCrawlerService;
import com.kingdomstudio.modules.motion.vo.CrawlResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 动效采集接口（Phase 3）。
 *
 * <p>单独一个 Controller 而不是塞进 MotionController：采集依赖外部网络、耗时以秒计，
 * 与常规 CRUD 的职责与超时策略都不一样。
 */
@Tag(name = "10.1 动效采集", description = "GitHub 搜索采集：限流处理、重试、缓存、去重与自动分类")
@RestController
@RequestMapping("/motion/crawl")
@RequiredArgsConstructor
public class MotionCrawlController {

	private final MotionCrawlerService crawlerService;

	@Operation(summary = "默认关键词", description = "采集时若未指定关键词使用的默认值")
	@GetMapping("/keywords")
	public Result<List<String>> keywords() {
		return Result.success(crawlerService.defaultKeywords());
	}

	@Operation(summary = "执行采集",
			description = "dryRun=true 时只试算不写库。/ 未配置 GITHUB_TOKEN 时限额较低（10 次/分钟），触发限流会立即停止并如实汇报")
	@PostMapping
	public Result<CrawlResultVO> crawl(@RequestBody(required = false) CrawlRequestDTO request) {
		return Result.success(crawlerService.crawl(request == null ? new CrawlRequestDTO() : request));
	}
}
