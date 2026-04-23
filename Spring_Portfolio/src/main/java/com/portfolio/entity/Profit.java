package com.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

@Entity
@Immutable
@Subselect("SELECT ROW_NUMBER() OVER() AS id, p.investor_id as invid, c.name as name, SUM(p.amount * (s.price-p.cost)) as subtotal "
		+ "FROM Classify c, Portfolio p, TStock s "
		+ "WHERE p.t_stock_id = s.id AND s.classify_id = c.id "
		+ "GROUP BY p.investor_id, c.name")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Profit {
    @Id
    private Integer id;
    private Integer invid;
    private String name;
    private Double subtotal;
}
