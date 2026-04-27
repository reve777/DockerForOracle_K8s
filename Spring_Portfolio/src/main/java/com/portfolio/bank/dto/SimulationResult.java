package com.portfolio.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimulationResult {
    private int totalRequests;           // 總請求數
    private int successCount;            // 成功數
    private int failCount;               // 失敗總數
    private int optimisticLockExCount;   // 樂觀鎖例外次數
    private int pessimisticLockExCount;  // 悲觀鎖/鎖超時例外次數
    private int insufficientBalanceCount;// 餘額不足次數
    private long executionTimeMs;        // 總執行時間 (毫秒)
}
