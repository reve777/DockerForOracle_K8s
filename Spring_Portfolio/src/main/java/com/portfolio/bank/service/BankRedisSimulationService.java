package com.portfolio.bank.service;

import com.portfolio.bank.dto.SimulationResult;
import com.portfolio.uitls.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模擬銀行轉帳服務 - 使用 Redis 分散式鎖解決併發衝突
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "spring.redis.enabled", havingValue = "true")
public class BankRedisSimulationService {

	private final BankService bankService;
	private final RedissonClient redisson;

	public SimulationResult simulateWithRedis(int totalRequests, int threadPoolSize) {
		// 使用固定大小的執行緒池模擬併發用戶
		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		// 使用 CountDownLatch 確保主執行緒等待所有模擬請求完成後再計算結果
		CountDownLatch latch = new CountDownLatch(totalRequests);

		// 使用 AtomicInteger 確保在多執行緒環境下統計數據的原子性（執行緒安全）
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
				boolean isDone = false;
				// ⭐ 實務考量：設定最大重試次數。高併發下「碰撞」是常態，適度重試可提高成功率
				int maxRetries = 6;
				int retryCount = 0;

				// 隨機產生轉出 ID 與 轉入 ID
				String fromId = String.format("A%05d", random.nextInt(100) + 1);
				String toId = String.format("A%05d", random.nextInt(100) + 1);
				while (fromId.equals(toId)) { // 避免自己轉給自己
					toId = String.format("A%05d", random.nextInt(100) + 1);
				}

				/*
				 * * ⭐【鎖的排序策略 - 預防死鎖】
				 * 死鎖發生於：執行緒 1 鎖住 A 想鎖 B，同時執行緒 2 鎖住 B 想鎖 A。
				 * 透過排序，我們強制「不論轉帳方向，永遠先鎖 ID 小的，再鎖 ID 大的」，
				 * 這樣所有執行緒都會按相同順序排隊，從物理上消滅死鎖循環。
				 */
				String[] ids = { fromId, toId };
				Arrays.sort(ids);
				String lockKey = "lock:transfer:" + ids[0] + ":" + ids[1];
				RLock lock = redisson.getLock(lockKey);

				// 進入重試迴圈
				while (retryCount <= maxRetries && !isDone) {
					try {
						/*
						 * * ⭐【Redisson tryLock 阻塞式等待】
						 * 參數1 (8): 等待時間 (waitTime)。執行緒會在這裡等待/訂閱鎖的釋放，最多等 8 秒。
						 * 這比不斷寫 while(true) 去搶鎖更節省 CPU 與 Redis 性能。
						 */
						if (lock.tryLock(8, TimeUnit.SECONDS)) {
							try {
								// 成功取得鎖，進入執行業務邏輯（此時該對帳號已被鎖定）
								bankService.transfer(fromId, toId, BigDecimal.valueOf(10));
								successCount.incrementAndGet();
								isDone = true; // 標記完成，退出 while 迴圈
							} finally {
								/*
								 * * ⭐【安全釋放鎖】
								 * 必須放在 finally 確保異常時也能解鎖。
								 * isHeldByCurrentThread() 檢查至關重要，防止自己執行太久導致鎖過期後，
								 * 誤把「別人新取得的鎖」給釋放掉。
								 */
								try {
									if (lock.isLocked() && lock.isHeldByCurrentThread()) {
										lock.unlock();
									}
								} catch (Exception e) {
									LogUtils.bizWarn("釋放鎖異常: {}", e.getMessage());
								}
							}
						} else {
							/*
							 * * ⭐【獲取鎖超時 - Jitter 避讓機制】
							 * 8 秒內都沒排到隊，代表併發極高。
							 * 透過 Thread.sleep 加上隨機毫秒，打散執行緒再次搶鎖的時間點。
							 */
							retryCount++;
							if (retryCount > maxRetries) {
								redisLockFailCount.incrementAndGet();
								failCount.incrementAndGet();
								isDone = true;
								log.debug("Redis 鎖重試耗盡，放棄請求: {}", lockKey);
							} else {
								Thread.sleep(100 + random.nextInt(200));
							}
						}
					} catch (ObjectOptimisticLockingFailureException e) {
						/*
						 * * ⭐【資料庫樂觀鎖衝突處理】
						 * 即使外層有 Redis 鎖，但若 DB 層有 Version 控制，仍可能發生衝突。
						 * 這裡同樣採取「退避重試」，讓系統韌性更強。
						 */
						retryCount++;
						if (retryCount > maxRetries) {
							optimisticLockExCount.incrementAndGet();
							failCount.incrementAndGet();
							isDone = true;
						} else {
							try {
								Thread.sleep(50 + random.nextInt(100));
							} catch (InterruptedException ie) {
								Thread.currentThread().interrupt();
							}
						}
					} catch (RedisException e) {
						// 處理 Redis 連線或網路異常
						LogUtils.bizWarn("Redis 異常 (類別: {}, retry: {}): {}",
								e.getClass().getSimpleName(), retryCount, e.getMessage());
						retryCount++;
						if (retryCount > maxRetries) {
							redisLockFailCount.incrementAndGet();
							failCount.incrementAndGet();
							isDone = true;
						} else {
							try {
								Thread.sleep(200);
							} catch (InterruptedException ie) {
								Thread.currentThread().interrupt();
							}
						}
					} catch (IllegalStateException e) {
						// 業務邏輯拋出的餘額不足，無需重試
						if ("餘額不足".equals(e.getMessage())) {
							insufficientBalanceCount.incrementAndGet();
						}
						failCount.incrementAndGet();
						isDone = true;
					} catch (InterruptedException e) {
						// 執行緒被中斷處理
						Thread.currentThread().interrupt();
						failCount.incrementAndGet();
						isDone = true;
					} catch (Exception e) {
						// 捕捉未預期的其他異常
						LogUtils.bizWarn("未預期異常: {}, 訊息: {}", e.getClass().getSimpleName(), e.getMessage());
						otherExCount.incrementAndGet();
						failCount.incrementAndGet();
						isDone = true;
					}
				}
				latch.countDown();
			});
		}

		try {
			latch.await(); // 等待所有子執行緒結束
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			executor.shutdown(); // 關閉執行緒池
		}

		long endTime = System.currentTimeMillis();
		// 返回統計結果
		return new SimulationResult(
				totalRequests,
				successCount.get(),
				failCount.get(),
				optimisticLockExCount.get(),
				redisLockFailCount.get(),
				insufficientBalanceCount.get(),
				(endTime - startTime));
	}
}