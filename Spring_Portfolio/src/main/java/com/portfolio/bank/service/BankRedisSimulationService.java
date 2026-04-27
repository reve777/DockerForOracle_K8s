package com.portfolio.bank.service;

import com.portfolio.bank.dto.SimulationResult;
import com.portfolio.uitls.LogUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class BankRedisSimulationService {

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
        // 在統計變數區增加
        AtomicInteger otherExCount = new AtomicInteger();

        Random random = new Random();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                boolean isDone = false;
                int maxRetries = 3; // 🔥 最大重試次數
                int retryCount = 0;

                // 準備隨機帳戶
                String fromId = String.format("A%05d", random.nextInt(100) + 1);
                String toId = String.format("A%05d", random.nextInt(100) + 1);
                while (fromId.equals(toId)) {
                    toId = String.format("A%05d", random.nextInt(100) + 1);
                }

                // 分散式鎖 Key 排序防止死鎖
                String[] ids = { fromId, toId };
                Arrays.sort(ids);
                String lockKey = "lock:transfer:" + ids[0] + ":" + ids[1];
                RLock lock = redisson.getLock(lockKey);

                // 🔥 進入重試迴圈
                while (retryCount <= maxRetries && !isDone) {
                    try {
                        // 嘗試加鎖 (等 5 秒，鎖 10 秒)
                        if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                            try {
                                BigDecimal amount = BigDecimal.valueOf(10);
                                bankService.transfer(fromId, toId, amount);
                                
                                successCount.incrementAndGet();
                                isDone = true; // 成功執行，標記完成
                            } finally {
                                if (lock.isHeldByCurrentThread()) {
                                    lock.unlock();
                                }
                            }
                        } else {
                            // 獲取鎖失敗，準備重試
                            retryCount++;
                            if (retryCount <= maxRetries) {
                                // 隨機等待 100~300ms 避免蜂擁效應
                                Thread.sleep(100 + random.nextInt(200));
                            } else {
                                redisLockFailCount.incrementAndGet();
                                failCount.incrementAndGet();
                                isDone = true; // 達到最大重試次數，標記結束
                            }
                        }
                    } catch (ObjectOptimisticLockingFailureException e) {
                        // 即使有 Redis 鎖，萬一還是發生 DB 樂觀鎖衝突，也進行重試
                        retryCount++;
                        if (retryCount > maxRetries) {
                            optimisticLockExCount.incrementAndGet();
                            failCount.incrementAndGet();
                            isDone = true;
                        }
                    } catch (IllegalStateException e) {
                        if ("餘額不足".equals(e.getMessage())) {
                            insufficientBalanceCount.incrementAndGet();
                        }
                        failCount.incrementAndGet();
                        isDone = true; // 餘額不足不需要重試
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        failCount.incrementAndGet();
                        isDone = true;
                    } catch (Exception e) {
//                        log.error("轉帳異常: {}", e.getMessage());
//                        failCount.incrementAndGet();
//                        isDone = true;
                    	LogUtils.bizWarn("未預期異常類型: {}", e.getClass().getSimpleName()); // 看看 Log 噴出什麼
                        otherExCount.incrementAndGet();
                        failCount.incrementAndGet();
                        isDone = true;
                    }
                }
                
                // 無論成功失敗，每個請求最後都要 countDown
                latch.countDown();
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
                optimisticLockExCount.get(), 
                redisLockFailCount.get(), 
                insufficientBalanceCount.get(), 
                (endTime - startTime)
        );
    }
}