package com.portfolio.exceptions;

/**
 * 餘額不足異常
 */
public class InsufficientBalanceException extends BusinessException {
    
    public InsufficientBalanceException(String message) {
        // 呼叫基底類別的 (code, message) 建構子
        super("INSUFFICIENT_BALANCE", message);
    }
}