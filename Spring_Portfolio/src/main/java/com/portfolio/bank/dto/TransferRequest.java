package com.portfolio.bank.dto;

import java.math.BigDecimal;

// 轉帳請求 DTO
public class TransferRequest {
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;

    // Getters and Setters
    public String getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(String fromAccountId) { this.fromAccountId = fromAccountId; }
    public String getToAccountId() { return toAccountId; }
    public void setToAccountId(String toAccountId) { this.toAccountId = toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
