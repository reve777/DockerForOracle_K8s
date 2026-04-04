package com.portfolio.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import jakarta.persistence.ForeignKey;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table
@Data
public class Portfolio {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column
	private Double cost;// 買進成本
	@Column
	private Integer amount;// 買進股數
//	@Column
	@Column(name = "watch_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date = new Date();// 交易時間

	@ManyToOne
	@JoinColumn(name = "investor_id", referencedColumnName = "id")

	@JsonIgnoreProperties("portfolios")
	private Investor investor;// 投資人

	@OneToOne
	@JoinColumn(name = "tStock_id", foreignKey = @ForeignKey(name = "tStock_fk", value = ConstraintMode.CONSTRAINT))
	private TStock tStock;// 對應股票ID

}
