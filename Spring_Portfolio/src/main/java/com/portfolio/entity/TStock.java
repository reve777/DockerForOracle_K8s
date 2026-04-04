package com.portfolio.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

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

    // 報價資訊
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
    @JsonIgnoreProperties("tStocks")
    private Classify classify;

    @ManyToMany(mappedBy = "tStocks")
    @JsonIgnoreProperties("tStocks")
    private Set<Watch> watchs = new LinkedHashSet<>();
}
