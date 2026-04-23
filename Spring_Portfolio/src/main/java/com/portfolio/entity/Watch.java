package com.portfolio.entity;

import java.util.LinkedHashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "watch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"investor", "tStocks"})
@EqualsAndHashCode(of = "id")
public class Watch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id")
    // 💡 當從 Watch 查 Investor 時，不要再抓 Investor 下的 watches 造成遞迴
    @JsonIgnoreProperties({"watches", "portfolios"}) 
    private Investor investor;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY) // 改為 LAZY
    @JoinTable(
        name = "watch_tstock",
        joinColumns = @JoinColumn(name = "watch_id"),
        inverseJoinColumns = @JoinColumn(name = "t_stock_id")
    )
    // 💡 進入 TStock 後，禁止再抓回 watches
    @JsonIgnoreProperties("watches") 
    private Set<TStock> tStocks = new LinkedHashSet<>();

    public Watch(String name, Investor investor) {
        this.name = name;
        this.investor = investor;
    }

    public void addTStock(TStock tStock) {
        this.tStocks.add(tStock);
    }

    public void removeTStock(TStock tStock) {
        this.tStocks.remove(tStock);
    }
}