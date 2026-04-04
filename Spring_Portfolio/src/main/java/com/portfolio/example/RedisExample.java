package com.portfolio.example;

import redis.clients.jedis.Jedis;

public class RedisExample {
	public static void main(String[] args) {
		// 連線到本地 Redis 服務
		Jedis jedis = new Jedis("localhost", 6379);

		// 驗證連線
		String pingResponse = jedis.ping();
		System.out.println("Redis PING: " + pingResponse); // PONG

		// 設定 key-value
		jedis.set("name", "Alice");
		System.out.println("Set key 'name' to Alice");

		// 取得 key
		String value = jedis.get("name");
		System.out.println("Get key 'name': " + value);

		// 刪除 key
		jedis.del("name");
		System.out.println("Deleted key 'name'");

		// 關閉連線
		jedis.close();
	}
}
