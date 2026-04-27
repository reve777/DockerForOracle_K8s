package com.portfolio.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/*
 * 【併發衝突處理機制說明】
 * ----------------------------------------------------------------------
 * 1. 觸發條件：當資料庫發生「鎖競爭失敗」或「無法取得鎖」的異常時觸發。
 * 2. 重試次數：maxAttempts = 10 (包含初始執行)，適合高壓力測試場景。
 * 3. 退避策略 (Backoff)：
 * - delay (100ms): 首次失敗後等待 0.1 秒。
 * - multiplier (1.5): 每次失敗後等待時間增加 50%，實現「碰撞規避」。
 * - maxDelay (1000ms): 單次重試前的等待時間上限為 1 秒。
 * 4. 注意事項：
 * - 必須由外部類別呼叫此方法，Self-invocation (內部呼叫) 會導致重試失效。
 * - 每次重試都會開啟一個全新的 @Transactional 事務。
 * ----------------------------------------------------------------------
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
    retryFor = { 
        PessimisticLockingFailureException.class, 
        org.springframework.dao.CannotAcquireLockException.class,
        org.springframework.dao.QueryTimeoutException.class // 增加超時異常處理
    },
    maxAttempts = 10,
    backoff = @Backoff(delay = 100, multiplier = 1.5, maxDelay = 1000)
)
public @interface RetryOnLockConflict {}