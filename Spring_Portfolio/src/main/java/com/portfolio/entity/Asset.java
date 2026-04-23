package com.portfolio.entity;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@Getter // 取代 @Data
@Setter // 取代 @Data
@ToString // 取代 @Data
public class Asset {

	@Id
	private Integer id;

	@Column
	private Integer invid;

	@Column
	private String name;

	@Column
	private Double subtotal;

	// 不要使用 Lombok 自動生成的 hashCode/equals
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Asset asset))
			return false;
		return id != null && id.equals(asset.getId());
	}

	@Override
	public int hashCode() {
		// 對於 Immutable 或 View 類型的 Entity，使用固定值或單一 ID
		return getClass().hashCode();
	}
}
