package com.portfolio.bank.service;

import org.springframework.stereotype.Service;

import com.portfolio.bank.entity.Account;

import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BankService {
    // 模擬資料庫，使用 ConcurrentHashMap 支援高併發存取
    private final Map<String, Account> accountDB = new ConcurrentHashMap<>();

    @PostConstruct
    public void initAccounts() {
        // 初始化兩個測試帳戶，各有一萬塊
        accountDB.put("A001", new Account("A001", new BigDecimal("10000.00")));
        accountDB.put("A002", new Account("A002", new BigDecimal("10000.00")));
    }

    public Account getAccount(String accountId) {
        return accountDB.get(accountId);
    }

    /**
     * 高併發轉帳核心邏輯
     */
    public boolean transfer(String fromId, String toId, BigDecimal amount) {
        if (fromId.equals(toId)) throw new IllegalArgumentException("不能轉帳給自己");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("轉帳金額必須大於 0");

        Account fromAccount = accountDB.get(fromId);
        Account toAccount = accountDB.get(toId);

        if (fromAccount == null || toAccount == null) {
            throw new IllegalArgumentException("帳戶不存在");
        }

        // 【高併發死鎖預防機制】
        // 永遠先鎖定 ID 字典序較小的帳戶。這保證了即使發生 A轉B 與 B轉A 的併發情況，
        // 兩個執行緒也會以相同的順序取得鎖，完美避免 Deadlock。
        Account firstLock = fromId.compareTo(toId) < 0 ? fromAccount : toAccount;
        Account secondLock = fromId.compareTo(toId) < 0 ? toAccount : fromAccount;

        firstLock.getLock().lock();
        try {
            secondLock.getLock().lock();
            try {
                // 再次檢查餘額是否充足 (在鎖定狀態下檢查，確保資料一致性)
                if (fromAccount.getBalance().compareTo(amount) < 0) {
                    throw new IllegalStateException("餘額不足");
                }
                
                // 執行扣款與入帳
                fromAccount.subtractBalance(amount);
                toAccount.addBalance(amount);
                
                return true;
            } finally {
                secondLock.getLock().unlock();
            }
        } finally {
            firstLock.getLock().unlock();
        }
    }
}
