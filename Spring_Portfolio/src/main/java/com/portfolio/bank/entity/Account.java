package com.portfolio.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "ACCOUNTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @Id
    @Column(name = "ACCOUNT_ID") // 明確對應 SQL 中的 ACCOUNT_ID
    private String accountId;
    
    @Column(name = "BALANCE")    // 明確對應 SQL 中的 BALANCE
    private BigDecimal balance;

    @Version
    @Column(name = "VERSION")    // 明確對應 SQL 中的 VERSION
    private Long version;

    public void addBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void subtractBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }
}