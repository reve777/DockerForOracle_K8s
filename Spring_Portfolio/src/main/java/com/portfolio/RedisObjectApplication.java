package com.portfolio;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.portfolio.example.User;

@SpringBootApplication
@ConditionalOnProperty(value = "spring.redis.enabled", havingValue = "true")
public class RedisObjectApplication implements CommandLineRunner {

	// 使用專門的 User RedisTemplate
	@Autowired
	private RedisTemplate<String, User> userRedisTemplate;

	public static void main(String[] args) {
		SpringApplication.run(RedisObjectApplication.class, args);
		System.out.println("Run Redis OK!");
	}

	@Override
	public void run(String... args) throws Exception {
		// 建立 User 物件
		User user = new User("u001", "Alice", 25);

		// 存到 Redis
		userRedisTemplate.opsForValue().set("user:u001", user);
		System.out.println("Saved user to Redis: " + user);

		// 從 Redis 取出
		User cachedUser = userRedisTemplate.opsForValue().get("user:u001");
		System.out.println("Retrieved user from Redis: " + cachedUser);

		// 刪除
		userRedisTemplate.delete("user:u001");
		System.out.println("Deleted user from Redis");
	}
}
