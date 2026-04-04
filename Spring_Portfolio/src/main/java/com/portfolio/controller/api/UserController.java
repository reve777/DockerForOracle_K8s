package com.portfolio.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import com.portfolio.example.User;

@RestController
@RequestMapping("/user")
@ConditionalOnProperty(value = "spring.redis.enabled", havingValue = "true")
public class UserController {

	@Autowired
	private RedisTemplate<String, User> userRedisTemplate;

	// 新增 User 到 Redis
	@PostMapping("/add")
	public String addUser(@RequestBody User user) {
		if (user == null || user.getId() == null) {
			return "User ID cannot be null!";
		}
		String key = "user:" + user.getId();
		userRedisTemplate.opsForValue().set(key, user);
		return "Saved user to Redis: " + user;
	}

	@GetMapping("/get/{id}")
	public Object getUser(@PathVariable("id") String id) {
		String key = "user:" + id;
		User user = userRedisTemplate.opsForValue().get(key);
		return user != null ? user : "User with id " + id + " does not exist.";
	}

	@DeleteMapping("/delete/{id}")
	public String deleteUser(@PathVariable("id") String id) {
		String key = "user:" + id;
		Boolean existed = userRedisTemplate.hasKey(key);
		if (existed != null && existed) {
			userRedisTemplate.delete(key);
			return "Deleted user with id: " + id;
		}
		return "User with id " + id + " does not exist.";
	}

}
