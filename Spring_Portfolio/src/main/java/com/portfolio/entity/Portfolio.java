package com.portfolio.entity;

import java.util.Date;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "portfolio")
@Getter
@Setter
@ToString(exclude = {"investor", "tStock"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id")
    @JsonIgnoreProperties("portfolios")
    private Investor investor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "t_stock_id")
    private TStock tStock;
}