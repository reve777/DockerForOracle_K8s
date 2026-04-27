package com.portfolio.bank.service;

import com.portfolio.bank.dto.SimulationResult;
import com.portfolio.uitls.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
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

        // 統計變數
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
                int maxRetries = 3; 
                int retryCount = 0;

                // 隨機選取帳戶
                String fromId = String.format("A%05d", random.nextInt(100) + 1);
                String toId = String.format("A%05d", random.nextInt(100) + 1);
                while (fromId.equals(toId)) {
                    toId = String.format("A%05d", random.nextInt(100) + 1);
                }

                // 排序 Key 防止分散式死鎖
                String[] ids = { fromId, toId };
                Arrays.sort(ids);
                String lockKey = "lock:transfer:" + ids[0] + ":" + ids[1];
                RLock lock = redisson.getLock(lockKey);

                while (retryCount <= maxRetries && !isDone) {
                    try {
                        // 使用 Watchdog 機制 (不設定 leaseTime)，等待鎖 5 秒
                        if (lock.tryLock(5, TimeUnit.SECONDS)) {
                            try {
                                BigDecimal amount = BigDecimal.valueOf(10);
                                bankService.transfer(fromId, toId, amount);
                                
                                successCount.incrementAndGet();
                                isDone = true; 
                            } finally {
                                // 安全釋放鎖：只釋放自己持有的鎖，避免 IllegalMonitorStateException
                                try {
                                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                                        lock.unlock();
                                    }
                                } catch (Exception e) {
                                    // 即使釋放失敗（如網路中斷），也不影響業務成功統計
                                    LogUtils.bizWarn("釋放鎖異常: {}", e.getMessage());
                                }
                            }
                        } else {
                            // tryLock 回傳 false (在 5 秒內拿不到鎖)
                            retryCount++;
                            if (retryCount > maxRetries) {
                                redisLockFailCount.incrementAndGet();
                                failCount.incrementAndGet();
                                isDone = true;
                            } else {
                                // 隨機避讓等待
                                Thread.sleep(100 + random.nextInt(200));
                            }
                        }
                    } catch (ObjectOptimisticLockingFailureException e) {
                        // DB 樂觀鎖衝突：進行重試
                        retryCount++;
                        if (retryCount > maxRetries) {
                            optimisticLockExCount.incrementAndGet();
                            failCount.incrementAndGet();
                            isDone = true;
                        }
                    } catch (RedisException e) {
                        // 包含 RedisTimeoutException 與其他連線問題
                        LogUtils.bizWarn("Redis 異常 (類別: {}, retry: {}): {}", 
                                         e.getClass().getSimpleName(), retryCount, e.getMessage());
                        retryCount++;
                        if (retryCount > maxRetries) {
                            redisLockFailCount.incrementAndGet(); 
                            failCount.incrementAndGet();
                            isDone = true;
                        } else {
                            // Redis 壓力大時，稍微多等一下再重試
                            try { Thread.sleep(200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        }
                    } catch (IllegalStateException e) {
                        // 業務邏輯錯誤（如餘額不足），不重試
                        if ("餘額不足".equals(e.getMessage())) {
                            insufficientBalanceCount.incrementAndGet();
                        }
                        failCount.incrementAndGet();
                        isDone = true; 
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        failCount.incrementAndGet();
                        isDone = true;
                    } catch (Exception e) {
                        // 真正的未預期異常（例如 NPE 或其他 RuntimeException）
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