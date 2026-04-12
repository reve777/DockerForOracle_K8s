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
    // 保留顯示 investor，但剛才我們在 Investor 類別已經把它的 watchs 給 @JsonIgnore 了
    @ToString.Exclude
    private Investor investor;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinTable(
        name = "watch_tstock",
        joinColumns = @JoinColumn(name = "watch_id"),
        inverseJoinColumns = @JoinColumn(name = "t_stock_id")
    )
    // 這裡不用改，因為 TStock 端已經 @JsonIgnore 了 watchs
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