package com.portfolio.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.portfolio.example.User;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConditionalOnProperty(value = "spring.redis.enabled", havingValue = "true", matchIfMissing = false)
public class RedisConfig {

	// 🔥 自動抓取 properties 裡的設定，如果沒設就用預設值
	@Value("${spring.redis.host:127.0.0.1}")
	private String host;

	@Value("${spring.redis.port:6379}")
	private int port;

	@Value("${spring.redis.database:0}")
	private int database;

	@Value("${spring.redis.password:}")
	private String password;

	// 通用 Object RedisTemplate，如果有其他對象可以用這個
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());

		Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		serializer.setObjectMapper(mapper);

		template.setValueSerializer(serializer);
		template.setHashValueSerializer(serializer);
		template.afterPropertiesSet();
		return template;
	}

	// 專門處理 User 的 RedisTemplate
	@Bean
	public RedisTemplate<String, User> userRedisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, User> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());

		Jackson2JsonRedisSerializer<User> serializer = new Jackson2JsonRedisSerializer<>(User.class);
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		serializer.setObjectMapper(mapper);

		template.setValueSerializer(serializer);
		template.setHashValueSerializer(serializer);
		template.afterPropertiesSet();
		return template;
	}

//	@Bean
//	public RedissonClient redissonClient() {
//		Config config = new Config();
//
//		// 使用單機模式
//		config.useSingleServer()
//				.setAddress("redis://" + host + ":" + port)
//				.setDatabase(database)
//				.setTimeout(3000) // 稍微縮短連線超時，避免死等
//				.setConnectionPoolSize(200) // 🔥 與 HikariCP 同步
//				.setConnectionMinimumIdleSize(50) // 提高常駐連線數
//				.setRetryAttempts(3) // 增加重試次數
//				.setRetryInterval(1500); // 重試間隔
//
//		if (password != null && !password.isEmpty()) {
//			config.useSingleServer().setPassword(password);
//		}
//
//		return Redisson.create(config);
//	}
	@Bean
	public RedissonClient redissonClient() {
	    Config config = new Config();

	    // 1. 設定序列化方式 (建議加在這裡)
	    // 使用 StringCodec 可以解決日誌中的 ClassCastException (String cannot be cast to Long)
	    config.setCodec(new org.redisson.client.codec.StringCodec());

	    // 2. 設定 Netty 線程數 (針對 200 併發進行優化)
	    config.setNettyThreads(64); 

	    // 3. 設定單機模式與連線池
	    config.useSingleServer()
	            .setAddress("redis://" + host + ":" + port)
	            .setDatabase(database)
	            .setTimeout(10000) // 💡 延長至 10 秒，避免高負載時 Response Timeout
	            .setConnectionPoolSize(300) // 🚀 高於執行緒數 (200)，確保不排隊
	            .setConnectionMinimumIdleSize(100)
	            .setRetryAttempts(5)
	            .setRetryInterval(2000)
	            .setPingConnectionInterval(30000);

	    if (password != null && !password.isEmpty()) {
	        config.useSingleServer().setPassword(password);
	    }

	    // 4. 最後才建立實例
	    return Redisson.create(config);
	}
}
