package com.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

@Entity
@Immutable
@Subselect("SELECT ROW_NUMBER() OVER() AS id, p.investor_id as invid, c.name as name, SUM(p.amount * (s.price-p.cost)) as subtotal "
		+ "FROM Classify c, Portfolio p, TStock s "
		+ "WHERE p.t_stock_id = s.id AND s.classify_id = c.id "
		+ "GROUP BY p.investor_id, c.name")
@Data
public class Profit {// 進賺
	@Id

	private Integer id;
	@Column
	private Integer invid;
	@Column
	private String name;
	@Column
	private Double subtotal;
}
