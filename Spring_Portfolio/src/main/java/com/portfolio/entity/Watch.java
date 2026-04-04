package com.portfolio.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "watch")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Watch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column
    private String name;

    @ManyToOne
    @JoinColumn(name = "investor_id", referencedColumnName = "id")
    @JsonIgnoreProperties("watchs")
    @ToString.Exclude
    private Investor investor;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinTable(
        name = "watch_tstock",
        joinColumns = @JoinColumn(name = "watch_id"),
        inverseJoinColumns = @JoinColumn(name = "t_stock_id")
    )
    @JsonIgnoreProperties("watchs")
    private Set<TStock> tStocks = new LinkedHashSet<>();

    public Watch(String name, Investor investor) {
        this.name = name;
        this.investor = investor;
    }

    public Set<TStock> addtStock(TStock tStock) {
        tStocks.add(tStock);
        return tStocks;
    }

    public Set<TStock> removetStock(TStock tStock) {
        tStocks.remove(tStock);
        return tStocks;
    }
}
