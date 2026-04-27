package com.portfolio.bank.service;

import com.portfolio.bank.entity.Account;
import com.portfolio.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BankService {

    private final AccountRepository accountRepository;

    public Account getAccount(String accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    @Transactional
    public boolean transfer(String fromId, String toId, BigDecimal amount) {
        if (fromId.equals(toId)) throw new IllegalArgumentException("不能轉帳給自己");
        
        // --- 關鍵：決定鎖定順序 ---
        String firstId = fromId.compareTo(toId) < 0 ? fromId : toId;
        String secondId = fromId.compareTo(toId) < 0 ? toId : fromId;

        // 1. 依照順序鎖定，避免循環等待 (Deadlock)
        Account firstAccount = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new IllegalArgumentException("帳戶不存在: " + firstId));
        
        Account secondAccount = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new IllegalArgumentException("帳戶不存在: " + secondId));

        // 2. 找出誰是來源、誰是目的（因為順序可能被我們調換了）
        Account fromAccount = fromId.equals(firstAccount.getAccountId()) ? firstAccount : secondAccount;
        Account toAccount = toId.equals(firstAccount.getAccountId()) ? firstAccount : secondAccount;

        // 3. 檢查餘額
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("餘額不足");
        }

        // 4. 執行金額異動
        fromAccount.subtractBalance(amount);
        toAccount.addBalance(amount);

        // JPA 會在事務結束時自動 flush，或者手動呼叫 save
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return true;
    }
}