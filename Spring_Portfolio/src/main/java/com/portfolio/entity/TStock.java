package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "tstock")
@Getter
@Setter
@ToString(exclude = {"classify", "watches"}) 
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column(unique = true)
    private String symbol;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "change_price", precision = 10, scale = 2)
    private BigDecimal changePrice;

    @Column(name = "change_in_percent", precision = 10, scale = 2)
    private BigDecimal changeInPercent;

    @Column(name = "pre_closed", precision = 10, scale = 2)
    private BigDecimal preClosed;

    @Column
    private Long volumn; 

    @Column(name = "transaction_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;

    @ManyToOne(fetch = FetchType.LAZY) // 改為 LAZY
    @JoinColumn(name = "classify_id")
    @JsonIgnoreProperties("tStocks") 
    private Classify classify;

    @ManyToMany(mappedBy = "tStocks", fetch = FetchType.LAZY) // 改為 LAZY
    @JsonIgnore // 💡 股票通常不需要反向查有哪些觀測清單，直接 Ignore 斷開最安全
    private Set<Watch> watches = new LinkedHashSet<>();
}