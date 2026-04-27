package com.portfolio.bank.service;

import com.portfolio.bank.entity.Account;
import com.portfolio.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

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

	@Transactional
	public boolean transfer(String fromId, String toId, BigDecimal amount) {
		if (fromId.equals(toId))
			throw new IllegalArgumentException("不能轉帳給自己");

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

	@Transactional
	public void generateMassiveData(int count) {
		// 1. 先清空現有帳戶，確保測試環境一致
		accountRepository.deleteAllInBatch();
		accountRepository.flush();

		List<Account> accounts = new ArrayList<>();
		Random random = new Random();

		for (int i = 1; i <= count; i++) {
			// ID 格式統一為 A00001, A00002...
			String accountId = String.format("A%05d", i);

			// 隨機餘額 10,000 ~ 15,000
			BigDecimal initialBalance = new BigDecimal("10000.00")
					.add(BigDecimal.valueOf(random.nextInt(5001)));

			// version 設為 null 或 0 均可，因為 isNew() 已強制設為 true
			accounts.add(new Account(accountId, initialBalance, null));

			// 每 1,000 筆批次寫入
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