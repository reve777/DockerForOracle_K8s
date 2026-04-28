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
import com.portfolio.uitls.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "spring.redis.enabled", havingValue = "true")
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
                totalRequests, successCount.get(), failCount.get(),
                optimisticLockExCount.get(), redisLockFailCount.get(),
                insufficientBalanceCount.get(), (endTime - startTime));
    }
}