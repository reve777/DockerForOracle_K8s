package com.portfolio.entity;

import java.util.Date;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "portfolio")
@Data
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private Double cost;

    @Column
    private Integer amount;

    @Column(name = "watch_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();

    @ManyToOne
    @JoinColumn(name = "investor_id")
    @JsonIgnoreProperties("portfolios")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // 💡 避免與 Investor 產生循環雜湊
    private Investor investor;

    @ManyToOne
    @JoinColumn(name = "t_stock_id") // 💡 務必對齊資料庫有底線的欄位
    private TStock tStock;
}