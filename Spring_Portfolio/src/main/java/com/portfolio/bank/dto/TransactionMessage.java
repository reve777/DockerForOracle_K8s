package com.portfolio.bank.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionMessage {
    private String txnId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String status;
    private long timestamp;
}
