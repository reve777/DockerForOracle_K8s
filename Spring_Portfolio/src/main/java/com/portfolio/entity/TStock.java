package com.portfolio.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tstock")
@Data
public class TStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String symbol;

    @Column
    private BigDecimal preClosed;

    @Column
    private BigDecimal price;

    @Column
    private BigDecimal changePrice;

    @Column
    private BigDecimal changeInPercent;

    @Column
    private Long volumn;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;

    @ManyToOne
    @JoinColumn(name = "classify_id", referencedColumnName = "id")
    @JsonIgnoreProperties("tStocks") // 這裡通常 Classify 也要加 Ignore
    private Classify classify;

    @ManyToMany(mappedBy = "tStocks")
    @JsonIgnore // 💡 終極方案：徹底切斷
    private Set<Watch> watchs = new LinkedHashSet<>();
}