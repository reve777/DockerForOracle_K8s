package com.portfolio.exceptions;

public class AccountException {

    public static class NotFound extends BusinessException {
        public NotFound(String message) { super("ACCOUNT_NOT_FOUND", message); }
    }

    public static class InsufficientBalance extends BusinessException {
        public InsufficientBalance(String message) { super("INSUFFICIENT_BALANCE", message); }
    }
}

// 使用時：throw new AccountException.InsufficientBalance("餘額不足");