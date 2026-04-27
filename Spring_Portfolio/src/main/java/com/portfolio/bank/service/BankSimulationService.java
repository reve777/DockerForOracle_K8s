package com.portfolio.bank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.portfolio.bank.dto.SimulationResult;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankSimulationService {

	// 🔥 注入核心的 BankService
	private final BankService bankService;

	public SimulationResult simulateHighConcurrency(int totalRequests, int threadPoolSize) {
		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		CountDownLatch latch = new CountDownLatch(totalRequests);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();
		AtomicInteger optimisticLockExCount = new AtomicInteger();
		AtomicInteger pessimisticLockExCount = new AtomicInteger();
		AtomicInteger insufficientBalanceCount = new AtomicInteger();

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

					BigDecimal amount = BigDecimal.valueOf(random.nextInt(50) + 1);

					// 🔥 跨類別呼叫，保證會觸發 Spring 的 @Transactional 代理
					bankService.transfer(fromId, toId, amount);
					successCount.incrementAndGet();

				} catch (ObjectOptimisticLockingFailureException e) {
					optimisticLockExCount.incrementAndGet();
					failCount.incrementAndGet();
				} catch (PessimisticLockingFailureException e) {
					pessimisticLockExCount.incrementAndGet();
					failCount.incrementAndGet();
				} catch (IllegalStateException e) {
					if ("餘額不足".equals(e.getMessage())) {
						insufficientBalanceCount.incrementAndGet();
					}
					failCount.incrementAndGet();
				} catch (Exception e) {
					log.error("未預期的錯誤", e);
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
				optimisticLockExCount.get(),
				pessimisticLockExCount.get(),
				insufficientBalanceCount.get(),
				(endTime - startTime));
	}
}