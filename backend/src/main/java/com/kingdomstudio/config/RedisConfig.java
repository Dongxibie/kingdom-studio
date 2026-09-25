package com.kingdomstudio.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 配置。
 *
 * <p>两件事：
 * <ol>
 *   <li>{@link RedisTemplate}：key 用字符串、value 用 JSON，方便在 redis-cli 里直接看；
 *       用 XML 序列化（JDK 序列化）会导致存进去的是乱码二进制。</li>
 *   <li>{@link RedisCacheManager}：给 {@code @Cacheable} 用的默认策略（30 分钟过期），
 *       即「Redis 作为缓存扩展」的落点，Phase 2 之后按需在查询上加。</li>
 * </ol>
 */
@Configuration
@EnableCaching
public class RedisConfig {

	private static final String KEY_PREFIX = "kingdom:";

	/**
	 * 反序列化类型白名单。
	 *
	 * <p>这里刻意不用 {@code LaissezFaireSubTypeValidator}（放行任意类）：JSON 里带
	 * {@code @class} 字段，一旦 Redis 中的数据被篡改，反序列化就可能被引导到 JDK 的
	 * gadget 链上执行代码。把可还原的类型收窄到「本项目 + 常用 JDK 类型」即可。
	 *
	 * <p>Phase 2 若要在缓存里放新的类型（尤其是第三方库的类型），需要往这里补包名，
	 * 否则读缓存时会抛 {@code InvalidTypeIdException}。
	 */
	private PolymorphicTypeValidator redisTypeValidator() {
		return BasicPolymorphicTypeValidator.builder()
				.allowIfSubType("com.kingdomstudio.")
				.allowIfSubType("java.util.")
				.allowIfSubType("java.time.")
				.allowIfSubType("java.lang.")
				.allowIfSubType("java.math.")
				.build();
	}

	private ObjectMapper redisObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		// 写入类型信息，反序列化时才能还原成原来的 Java 类型
		mapper.activateDefaultTyping(redisTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
		return mapper;
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		StringRedisSerializer keySerializer = new StringRedisSerializer();
		GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());

		template.setKeySerializer(keySerializer);
		template.setHashKeySerializer(keySerializer);
		template.setValueSerializer(valueSerializer);
		template.setHashValueSerializer(valueSerializer);
		template.afterPropertiesSet();
		return template;
	}

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofMinutes(30))
				.prefixCacheNameWith(KEY_PREFIX)
				.disableCachingNullValues()
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper())));

		return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
	}
}
