package com.portfolio.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;

@Entity
@Table(name = "ACCOUNTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Persistable<String> {
    
    @Id
    @Column(name = "ACCOUNT_ID")
    private String accountId;
    
    @Column(name = "BALANCE")
    private BigDecimal balance;

    @Version
    @Column(name = "VERSION")
    private Long version;

    /**
     * 實作 Persistable 介面的 getId()
     */
    @Override
    public String getId() {
        return this.accountId;
    }

    /**
     * 關鍵邏輯：強制讓 JPA 認為這是一筆新資料。
     * 當呼叫 repository.save() 時，JPA 會直接執行 INSERT 而非先 SELECT。
     */
    @Override
    @Transient // 此欄位不對應資料庫
    public boolean isNew() {
        return true; 
    }

    public void addBalance(BigDecimal amount) {
        if (this.balance == null) this.balance = BigDecimal.ZERO;
        this.balance = this.balance.add(amount);
    }

    public void subtractBalance(BigDecimal amount) {
        if (this.balance == null) this.balance = BigDecimal.ZERO;
        this.balance = this.balance.subtract(amount);
    }
}