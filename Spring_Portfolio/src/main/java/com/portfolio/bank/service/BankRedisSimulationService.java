//package com.portfolio.bank.service;
//
//import com.portfolio.bank.dto.SimulationResult;
//import com.portfolio.uitls.LogUtils;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.redisson.api.RLock;
//import org.redisson.api.RedissonClient;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.orm.ObjectOptimisticLockingFailureException;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.Arrays;
//import java.util.Random;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@ConditionalOnProperty(value = "spring.redis.enabled", havingValue = "true")
//public class BankRedisSimulationService {
//
//    private final BankService bankService;
//    private final RedissonClient redisson;
//
//    public SimulationResult simulateWithRedis(int totalRequests, int threadPoolSize) {
//        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
//        CountDownLatch latch = new CountDownLatch(totalRequests);
//
//        AtomicInteger successCount = new AtomicInteger();
//        AtomicInteger failCount = new AtomicInteger();
//        AtomicInteger optimisticLockExCount = new AtomicInteger();
//        AtomicInteger redisLockFailCount = new AtomicInteger();
//        AtomicInteger insufficientBalanceCount = new AtomicInteger();
//        AtomicInteger otherExCount = new AtomicInteger();
//
//        Random random = new Random();
//        long startTime = System.currentTimeMillis();
//
//        for (int i = 0; i < totalRequests; i++) {
//            executor.submit(() -> {
//                try {
//                    // 1. 隨機產生帳號
//                    String fromId = String.format("A%05d", random.nextInt(10000) + 1);
//                    String toId = String.format("A%05d", random.nextInt(10000) + 1);
//                    while (fromId.equals(toId)) {
//                        toId = String.format("A%05d", random.nextInt(10000) + 1);
//                    }
//
//                    // 2. 分散式鎖 Key 排序 (預防 Redis 層級死結)
//                    String[] ids = { fromId, toId };
//                    Arrays.sort(ids);
//                    String lockKey = "lock:transfer:" + ids[0] + ":" + ids[1];
//                    RLock lock = redisson.getLock(lockKey);
//
//                    // 3. 嘗試取得 Redis 鎖 (等待 8 秒，租約 10 秒)
//                    if (lock.tryLock(8, TimeUnit.SECONDS, 10, TimeUnit.SECONDS)) {
//                        try {
//                            // 調用具備 @Retryable 自我修復能力的業務方法
//                            bankService.transfer(fromId, toId, BigDecimal.valueOf(10));
//                            successCount.incrementAndGet();
//                        } finally {
//                            if (lock.isHeldByCurrentThread()) {
//                                lock.unlock();
//                            }
//                        }
//                    } else {
//                        redisLockFailCount.incrementAndGet();
//                        failCount.incrementAndGet();
//                    }
//
//                } catch (ObjectOptimisticLockingFailureException e) {
//                    // 雖然有重試，但若超過 maxAttempts 仍失敗則記錄
//                    optimisticLockExCount.incrementAndGet();
//                    failCount.incrementAndGet();
//                } catch (IllegalStateException e) {
//                    if ("餘額不足".equals(e.getMessage())) {
//                        insufficientBalanceCount.incrementAndGet();
//                    }
//                    failCount.incrementAndGet();
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    failCount.incrementAndGet();
//                } catch (Exception e) {
//                    LogUtils.bizWarn("模擬執行異常: {}", e.getMessage());
//                    otherExCount.incrementAndGet();
//                    failCount.incrementAndGet();
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        } finally {
//            executor.shutdown();
//        }
//
//        long endTime = System.currentTimeMillis();
//        return new SimulationResult(
//                totalRequests, successCount.get(), failCount.get(),
//                optimisticLockExCount.get(), redisLockFailCount.get(),
//                insufficientBalanceCount.get(), (endTime - startTime));
//    }
//}
package com.portfolio.bank.service;

import com.portfolio.bank.dto.SimulationResult;
import com.portfolio.bank.dto.TransactionMessage;
import com.portfolio.uitls.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "spring.redis.enabled", havingValue = "true")
//@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class BankRedisSimulationService {

//	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final ObjectProvider<KafkaTemplate<String, Object>> kafkaProvider;
	private final BankService bankService;
	private final RedissonClient redisson;

	public SimulationResult simulateWithRedis(int totalRequests, int threadPoolSize) {
		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		CountDownLatch latch = new CountDownLatch(totalRequests);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();
		AtomicInteger optimisticLockExCount = new AtomicInteger();
		AtomicInteger redisLockFailCount = new AtomicInteger();
		AtomicInteger insufficientBalanceCount = new AtomicInteger();
		AtomicInteger otherExCount = new AtomicInteger();

		Random random = new Random();
		long startTime = System.currentTimeMillis();

		for (int i = 0; i < totalRequests; i++) {
			executor.submit(() -> {
				try {
					String fromId = String.format("A%05d", random.nextInt(10000) + 1);
					String toId = String.format("A%05d", random.nextInt(10000) + 1);
					while (fromId.equals(toId)) {
						toId = String.format("A%05d", random.nextInt(10000) + 1);
					}

					String[] ids = { fromId, toId };
					Arrays.sort(ids);
					String lockKey = "lock:transfer:" + ids[0] + ":" + ids[1];
					RLock lock = redisson.getLock(lockKey);

					// ⭐ 修正點：Redisson tryLock 參數為 (waitTime, leaseTime, unit)
					if (lock.tryLock(8, 10, TimeUnit.SECONDS)) {
						try {
							bankService.transfer(fromId, toId, BigDecimal.valueOf(10));
							successCount.incrementAndGet();
						} finally {
							if (lock.isHeldByCurrentThread()) {
								lock.unlock();
							}
						}
					} else {
						redisLockFailCount.incrementAndGet();
						failCount.incrementAndGet();
					}

				} catch (ObjectOptimisticLockingFailureException e) {
					optimisticLockExCount.incrementAndGet();
					failCount.incrementAndGet();
				} catch (IllegalStateException e) {
					if ("餘額不足".equals(e.getMessage())) {
						insufficientBalanceCount.incrementAndGet();
					}
					failCount.incrementAndGet();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					failCount.incrementAndGet();
				} catch (Exception e) {
					LogUtils.bizWarn("模擬執行異常: {}", e.getMessage());
					otherExCount.incrementAndGet();
					failCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			executor.shutdown();
		}

		long endTime = System.currentTimeMillis();
		return new SimulationResult(
				totalRequests,
				successCount.get(),
				failCount.get(),
//				optimisticLockExCount.get(), 
				0,
				redisLockFailCount.get(),
				insufficientBalanceCount.get(),
				(endTime - startTime));
	}

//    ============================use Lua=============================
	/**
	 * Redis Lua 腳本：原子性轉帳操作
	 * 
	 * 作用：保證「扣款」與「入帳」在 Redis 中是一次性完成的原子操作，避免併發時的數據不一致。
	 * 
	 * [參數說明]
	 * KEYS[1]: 轉出帳戶的 Key (fromKey)
	 * KEYS[2]: 轉入帳戶的 Key (toKey)
	 * ARGV[1]: 轉帳金額 (amount)
	 * 
	 * [回傳值]
	 * 1: 轉帳成功
	 * -1: 餘額不足
	 * -2: 帳戶不存在（fromKey 或 toKey 其中之一不存在）
	 */
//	private static final String TRANSFER_LUA = "local fromKey = KEYS[1]; " + // 1. 取得轉出帳戶 Key
//			"local toKey = KEYS[2]; " + // 2. 取得轉入帳戶 Key
//			"local amount = tonumber(ARGV[1]); " + // 3. 轉帳金額轉為數字
//			"local fromBalance = redis.call('GET', fromKey); " + // 4. 查詢轉出帳戶餘額
//			"local toBalance = redis.call('GET', toKey); " + // 5. 查詢轉入帳戶餘額
//			"if not fromBalance or not toBalance then return -2 end; " + // 6. 若帳戶不存在則回傳 -2
//			"if tonumber(fromBalance) < amount then return -1 end; " + // 7. 若餘額不足則回傳 -1
//			"redis.call('DECRBY', fromKey, amount); " + // 8. 扣除轉出帳戶金額
//			"redis.call('INCRBY', toKey, amount); " + // 9. 增加轉入帳戶金額
//			"return 1;"; // 10. 全部成功，回傳 1
	private static final String TRANSFER_LUA = "local amount = tonumber(ARGV[1]); " +
			"local fromKey = ARGV[2]; " + // 顯式指定轉出帳號
			"local toKey; " +
//		    -- 透過判斷找出哪一個是轉入帳號
			"if KEYS[1] == fromKey then toKey = KEYS[2] else toKey = KEYS[1] end; " +

			"local fromBalance = redis.call('GET', fromKey); " +
			"local toBalance = redis.call('GET', toKey); " +
			"if not fromBalance or not toBalance then return -2 end; " +
			"if tonumber(fromBalance) < amount then return -1 end; " +

			"redis.call('DECRBY', fromKey, amount); " +
			"redis.call('INCRBY', toKey, amount); " +
			"return 1;";

	/**
	 * 預熱資料：模擬開始前，將 DB 資料同步至 Redis
	 * 這是解決「為什麼 DB 有錢但 Redis 報錯」的關鍵步驟
	 */
	public void preheatAccountData() {
		log.info(">>> 執行 Redis 資料預熱...");
		// 模擬從 DB 讀取 10000 個帳號並寫入 Redis
		for (int i = 1; i <= 10000; i++) {
			String accountId = String.format("A%05d", i);
			// 這裡假設從 DB 查出的初始餘額為 10000 (請依據你 DB 實際狀況修改)
			redisson.getBucket("account:" + accountId).set(10000);
		}
		log.info(">>> 預熱完成，10000 個帳號已載入 Redis。");
	}

	public SimulationResult simulateWithRedisLua(int totalRequests, int threadPoolSize) {
		// 建議在外部手動呼叫預熱，或在測試開始前執行一次
//		preheatAccountData();

		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		CountDownLatch latch = new CountDownLatch(totalRequests);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();
		AtomicInteger insufficientBalanceCount = new AtomicInteger();
		AtomicInteger redisKeyMissingCount = new AtomicInteger(); // 改為記錄 Key 缺失
		AtomicInteger otherExCount = new AtomicInteger();

		Random random = new Random();
		long startTime = System.currentTimeMillis();

		for (int i = 0; i < totalRequests; i++) {
			executor.submit(() -> {
				String fromId = String.format("account:A%05d", random.nextInt(10000) + 1);
				String toId = String.format("account:A%05d", random.nextInt(10000) + 1);
				while (fromId.equals(toId)) {
					toId = String.format("account:A%05d", random.nextInt(10000) + 1);
				}
				// 對 Redis KEYS 進行排序：確保不論轉帳方向為何，資源鎖定順序一致，從而徹底防止分散式死結 (Deadlock)
				List<Object> sortedKeys = Stream.of(fromId, toId)
						.sorted()
						.map(id -> (Object) id) // 強制轉為 Object
						.toList();
				String status = "UNKNOWN";
				try {
					// 執行 Lua 原子轉帳
					Long result = redisson.getScript().eval(
							RScript.Mode.READ_WRITE,
							TRANSFER_LUA,
							RScript.ReturnType.INTEGER,
							sortedKeys, // 現在這是 List<Object>
							10, // ARGV[1] (會被自動裝箱為 Integer)
							fromId); // ARGV[2] (String)

					if (result == 1) {
						successCount.incrementAndGet();
						status = "SUCCESS";
					} else if (result == -1) {
						insufficientBalanceCount.incrementAndGet();
						failCount.incrementAndGet();
						status = "FAIL_INSUFFICIENT_BALANCE";
					} else if (result == -2) {
						redisKeyMissingCount.incrementAndGet();
						failCount.incrementAndGet();
						status = "FAIL_KEY_MISSING";
					}

					// --- 關鍵修改：發送交易日誌到 Kafka (包含失敗狀態) ---
					sendTransactionLog(fromId, toId, status);

				} catch (Exception e) {
					LogUtils.bizWarn("模擬執行異常: {}", e.getMessage());
					otherExCount.incrementAndGet();
					failCount.incrementAndGet();
					// 系統異常也記錄下來
					sendTransactionLog(fromId, toId, "ERROR_SYSTEM_EXCEPTION");
				} finally {
					latch.countDown();
				}
			});
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			executor.shutdown();
		}

		long endTime = System.currentTimeMillis();

		return new SimulationResult(
				totalRequests,
				successCount.get(),
				failCount.get(),
				0, // Lua 模式不使用樂觀鎖
				redisKeyMissingCount.get(), // 借用此欄位顯示 Redis 找不到 Key 的次數
				insufficientBalanceCount.get(),
				(endTime - startTime));
	}

	/**
	 * kafka 單次寫入
	 **/
	// 提取成獨立方法增加可讀性
//	private void sendTransactionLog(String fromId, String toId, String status) {
//		TransactionMessage msg = new TransactionMessage(
//				UUID.randomUUID().toString(),
//				fromId,
//				toId,
//				BigDecimal.valueOf(10),
//				status,
//				System.currentTimeMillis());
//		kafkaTemplate.send("ivan-transfer-topic", msg);
//	}
	// 提取成獨立方法增加可讀性
//	private void sendTransactionLog(String fromId, String toId, String status) {
//		kafkaProvider.ifAvailable(template -> {
//			TransactionMessage msg = new TransactionMessage(
//					UUID.randomUUID().toString(),
//					fromId,
//					toId,
//					BigDecimal.valueOf(10),
//					status,
//					System.currentTimeMillis());
//			template.send("ivan-transfer-topic", msg);
//		});
//	}
	/**
	 * kafka 批次寫入
	 */
	private void sendTransactionLog(String fromId, String toId, String status) {
		kafkaProvider.ifAvailable(template -> {
			TransactionMessage msg = new TransactionMessage(
					UUID.randomUUID().toString(),
					fromId,
					toId,
					BigDecimal.valueOf(10),
					status,
					System.currentTimeMillis());

			// 修改點：傳入 fromId 作為 Key
			// template.send(topic, key, data)
			template.send("ivan-transfer-topic", fromId, msg);
		});
	}

}