//package com.portfolio.bank.service;
//
//import com.portfolio.bank.dto.SimulationResult;
//import com.portfolio.bank.entity.Account;
//import com.portfolio.bank.repository.AccountRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//
//import org.springframework.dao.CannotAcquireLockException;
//import org.springframework.dao.PessimisticLockingFailureException;
//import org.springframework.orm.ObjectOptimisticLockingFailureException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Log4j2
//@Service
//@RequiredArgsConstructor
//public class BankService {
//
//	private final AccountRepository accountRepository;
//
//	public Account getAccount(String accountId) {
//		return accountRepository.findById(accountId).orElse(null);
//	}
//
//	@Transactional
//	public boolean transfer(String fromId, String toId, BigDecimal amount) {
//		if (fromId.equals(toId))
//			throw new IllegalArgumentException("不能轉帳給自己");
//
//		// --- 關鍵：決定鎖定順序 ---
//		String firstId = fromId.compareTo(toId) < 0 ? fromId : toId;
//		String secondId = fromId.compareTo(toId) < 0 ? toId : fromId;
//
//		// 1. 依照順序鎖定，避免循環等待 (Deadlock)
//		Account firstAccount = accountRepository.findByIdForUpdate(firstId)
//				.orElseThrow(() -> new IllegalArgumentException("帳戶不存在: " + firstId));
//
//		Account secondAccount = accountRepository.findByIdForUpdate(secondId)
//				.orElseThrow(() -> new IllegalArgumentException("帳戶不存在: " + secondId));
//
//		// 2. 找出誰是來源、誰是目的（因為順序可能被我們調換了）
//		Account fromAccount = fromId.equals(firstAccount.getAccountId()) ? firstAccount : secondAccount;
//		Account toAccount = toId.equals(firstAccount.getAccountId()) ? firstAccount : secondAccount;
//
//		// 3. 檢查餘額
//		if (fromAccount.getBalance().compareTo(amount) < 0) {
//			throw new IllegalStateException("餘額不足");
//		}
//
//		// 4. 執行金額異動
//		fromAccount.subtractBalance(amount);
//		toAccount.addBalance(amount);
//
//		// JPA 會在事務結束時自動 flush，或者手動呼叫 save
//		accountRepository.save(fromAccount);
//		accountRepository.save(toAccount);
//
//		return true;
//	}
//	/** 創建資料*/
//	@Transactional
//	public void generateMassiveData(int count) {
//		// 1. 先清空現有帳戶，確保測試環境一致
//		accountRepository.deleteAllInBatch();
//		accountRepository.flush();
//
//		List<Account> accounts = new ArrayList<>();
//		Random random = new Random();
//
//		for (int i = 1; i <= count; i++) {
//			// ID 格式統一為 A00001, A00002...
//			String accountId = String.format("A%05d", i);
//
//			// 隨機餘額 10,000 ~ 15,000
//			BigDecimal initialBalance = new BigDecimal("10000.00")
//					.add(BigDecimal.valueOf(random.nextInt(5001)));
//
//			// version 設為 null 或 0 均可，因為 isNew() 已強制設為 true
//			accounts.add(new Account(accountId, initialBalance, null));
//
//			// 每 1,000 筆批次寫入
//			if (i % 1000 == 0) {
//				accountRepository.saveAll(accounts);
//				accounts.clear();
//				log.info("已處理 {} 筆...", i);
//			}
//		}
//
//		if (!accounts.isEmpty()) {
//			accountRepository.saveAll(accounts);
//		}
//	}
//	
////	public SimulationResult simulateHighConcurrency(int totalRequests, int threadPoolSize) {
////        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
////        CountDownLatch latch = new CountDownLatch(totalRequests);
////
////        // 使用 AtomicInteger 確保多執行緒下計數準確
////        AtomicInteger successCount = new AtomicInteger();
////        AtomicInteger failCount = new AtomicInteger();
////        AtomicInteger optimisticLockExCount = new AtomicInteger();
////        AtomicInteger pessimisticLockExCount = new AtomicInteger();
////        AtomicInteger insufficientBalanceCount = new AtomicInteger();
////
////        Random random = new Random();
////        long startTime = System.currentTimeMillis();
////
////        for (int i = 0; i < totalRequests; i++) {
////            executor.submit(() -> {
////                try {
////                    // 隨機產生 A00001 ~ A10000 的帳號
////                    String fromId = String.format("A%05d", random.nextInt(10000) + 1);
////                    String toId = String.format("A%05d", random.nextInt(10000) + 1);
////
////                    // 確保轉出跟轉入不是同一個帳號
////                    while (fromId.equals(toId)) {
////                        toId = String.format("A%05d", random.nextInt(10000) + 1);
////                    }
////
////                    // 隨機轉帳金額 1 ~ 50
////                    BigDecimal amount = BigDecimal.valueOf(random.nextInt(50) + 1);
////
////                    // 執行轉帳
////                    this.transfer(fromId, toId, amount);
////                    successCount.incrementAndGet();
////
////                } catch (ObjectOptimisticLockingFailureException e) {
////                    // 樂觀鎖衝突 (Version 不符)
////                    optimisticLockExCount.incrementAndGet();
////                    failCount.incrementAndGet();
////                } catch (PessimisticLockingFailureException e) {
////                    // 悲觀鎖超時或死結 (DB Row Lock Timeout)
////                    pessimisticLockExCount.incrementAndGet();
////                    failCount.incrementAndGet();
////                } catch (IllegalStateException e) {
////                    if ("餘額不足".equals(e.getMessage())) {
////                        insufficientBalanceCount.incrementAndGet();
////                    }
////                    failCount.incrementAndGet();
////                } catch (Exception e) {
////                    log.error("未預期的錯誤", e);
////                    failCount.incrementAndGet();
////                } finally {
////                    latch.countDown(); // 任務執行完畢，計數減一
////                }
////            });
////        }
////
////        try {
////            latch.await(); // 主執行緒等待所有子執行緒執行完畢
////        } catch (InterruptedException e) {
////            Thread.currentThread().interrupt();
////        } finally {
////            executor.shutdown();
////        }
////
////        long endTime = System.currentTimeMillis();
////
////        return new SimulationResult(
////                totalRequests,
////                successCount.get(),
////                failCount.get(),
////                optimisticLockExCount.get(),
////                pessimisticLockExCount.get(),
////                insufficientBalanceCount.get(),
////                (endTime - startTime)
////        );
////    }
//}
package com.portfolio.bank.service;

import com.portfolio.bank.entity.Account;
import com.portfolio.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Log4j2
@Service
@RequiredArgsConstructor
public class BankService {

    private final AccountRepository accountRepository;

    public Account getAccount(String accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    /**
     * 執行轉帳
     * @Retryable: 當發生樂觀鎖、悲觀鎖或無法取得鎖定時自動重試
     * backoff: 初始等 100ms，最高 500ms，隨機抖動(random=true)避免集體碰撞
     */
    @Transactional
    @Retryable(
        value = { 
            ObjectOptimisticLockingFailureException.class, 
            PessimisticLockingFailureException.class,
            CannotAcquireLockException.class 
        }, 
        maxAttempts = 5, 
        backoff = @Backoff(delay = 100, maxDelay = 500, multiplier = 1.5, random = true)
    )
    public boolean transfer(String fromId, String toId, BigDecimal amount) {
        if (fromId.equals(toId))
            throw new IllegalArgumentException("不能轉帳給自己");

        // --- 關鍵：決定鎖定順序預防死結 ---
        String firstId = fromId.compareTo(toId) < 0 ? fromId : toId;
        String secondId = fromId.compareTo(toId) < 0 ? toId : fromId;

        // 1. 依照順序鎖定 (SELECT ... FOR UPDATE)
        Account firstAccount = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new IllegalArgumentException("帳戶不存在: " + firstId));

        Account secondAccount = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new IllegalArgumentException("帳戶不存在: " + secondId));

        // 2. 辨識來源與目的帳戶
        Account fromAccount = fromId.equals(firstAccount.getAccountId()) ? firstAccount : secondAccount;
        Account toAccount = toId.equals(firstAccount.getAccountId()) ? firstAccount : secondAccount;

        // 3. 檢查餘額
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("餘額不足");
        }

        // 4. 執行金額異動
        fromAccount.subtractBalance(amount);
        toAccount.addBalance(amount);

        // 5. 儲存變更
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return true;
    }

    @Transactional
    public void generateMassiveData(int count) {
        accountRepository.deleteAllInBatch();
        accountRepository.flush();

        List<Account> accounts = new ArrayList<>();
        Random random = new Random();

        for (int i = 1; i <= count; i++) {
            String accountId = String.format("A%05d", i);
            BigDecimal initialBalance = new BigDecimal("10000.00")
                    .add(BigDecimal.valueOf(random.nextInt(5001)));
            accounts.add(new Account(accountId, initialBalance, null));

            if (i % 1000 == 0) {
                accountRepository.saveAll(accounts);
                accounts.clear();
                log.info("已處理 {} 筆...", i);
            }
        }
        if (!accounts.isEmpty()) {
            accountRepository.saveAll(accounts);
        }
    }
}