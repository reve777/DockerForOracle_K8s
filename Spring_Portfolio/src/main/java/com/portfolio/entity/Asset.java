package com.portfolio.entity;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Immutable
@Subselect("""
    SELECT ROW_NUMBER() OVER() AS id,
           p.investor_id AS invid,
           c.name AS name,
           SUM(p.amount * s.price) AS subtotal
    FROM Classify c, Portfolio p, TStock s
    WHERE p.t_stock_id = s.id AND s.classify_id = c.id
    GROUP BY p.investor_id, c.name
""")
@Data
public class Asset {  // 資產

    @Id
    private Integer id;

    @Column
    private Integer invid;

    @Column
    private String name;

    @Column
    private Double subtotal;
}
