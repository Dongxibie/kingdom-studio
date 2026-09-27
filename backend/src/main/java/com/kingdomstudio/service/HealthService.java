package com.kingdomstudio.service;

import com.kingdomstudio.vo.HealthVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 健康检查业务逻辑。
 *
 * <p>探测逻辑放在 Service 层，Controller 只负责接收请求与包装响应，
 * 符合项目「禁止业务逻辑写在 Controller」的分层要求。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthService {

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final String APP_NAME = "kingdom-studio";
	private static final int DETAIL_MAX_LENGTH = 120;

	/**
	 * 版本号唯一来源是 application.yml 的 info.app.version，
	 * 而那个值由 {@code npm run version:sync} 从 frontend/package.json 同步过来 ——
	 * 全项目只有 package.json 需要手改版本号。
	 */
	@Value("${info.app.version:}")
	private String appVersion;

	private final JdbcTemplate jdbcTemplate;
	private final StringRedisTemplate stringRedisTemplate;

	/** 汇总应用、MySQL、Redis 的可用状态 */
	public HealthVO check() {
		HealthVO.Component database = checkDatabase();
		HealthVO.Component redis = checkRedis();

		boolean allUp = "UP".equals(database.getStatus()) && "UP".equals(redis.getStatus());

		return HealthVO.builder()
				.application(APP_NAME)
				.status(allUp ? "UP" : "DEGRADED")
				.version(appVersion)
				.javaVersion(System.getProperty("java.version"))
				.serverTime(LocalDateTime.now().format(TIME_FORMATTER))
				.database(database)
				.redis(redis)
				.build();
	}

	private HealthVO.Component checkDatabase() {
		long start = System.currentTimeMillis();
		try {
			String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
			return HealthVO.Component.up("MySQL " + version, System.currentTimeMillis() - start);
		} catch (Exception e) {
			log.error("MySQL 健康检查失败", e);
			return HealthVO.Component.down(shortMessage(e), System.currentTimeMillis() - start);
		}
	}

	private HealthVO.Component checkRedis() {
		long start = System.currentTimeMillis();
		try {
			String pong = stringRedisTemplate.execute(RedisConnection::ping);
			return HealthVO.Component.up("Redis " + pong, System.currentTimeMillis() - start);
		} catch (Exception e) {
			log.error("Redis 健康检查失败", e);
			return HealthVO.Component.down(shortMessage(e), System.currentTimeMillis() - start);
		}
	}

	/** 只把异常摘要返回给前端，避免把堆栈暴露出去 */
	private String shortMessage(Exception e) {
		String message = e.getMessage();
		if (message == null || message.isBlank()) {
			return e.getClass().getSimpleName();
		}
		return message.length() > DETAIL_MAX_LENGTH ? message.substring(0, DETAIL_MAX_LENGTH) + "..." : message;
	}
}
