package com.portfolio.bank.entity;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// 帳戶模型
public class Account {
    private String accountId;
    private BigDecimal balance;
    // 每個帳戶獨立的鎖，用於高併發控制
    private final Lock lock = new ReentrantLock();

    public Account(String accountId, BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public String getAccountId() { return accountId; }
    public BigDecimal getBalance() { return balance; }
    
    public void addBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
    
    public void subtractBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public Lock getLock() { return lock; }
}
