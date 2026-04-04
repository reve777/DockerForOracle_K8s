package com.portfolio.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.ToString;

@Entity
@Table
@Data
public class Investor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String username;

    @Column
    private String email;

    @Column
    private Integer balance;

    @OneToMany(mappedBy = "investor")
    @JsonIgnoreProperties("investor")
    @ToString.Exclude // 排除 watchs 避免遞迴
    private Set<Watch> watchs;

    @OneToMany(mappedBy = "investor")
    @JsonIgnoreProperties("investor")
    @ToString.Exclude
    private Set<Portfolio> portfolios;
}
