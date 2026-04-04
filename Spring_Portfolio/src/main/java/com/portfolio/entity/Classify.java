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

//商品分類表
@Entity
@Table
@Data
public class Classify {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column
	private String name;// 分類名稱
	@Column
	private Boolean tx;// 該商品是否支援交易(transaction)

	@OneToMany(mappedBy = "classify")
	@JsonIgnoreProperties("classify")
	private Set<TStock> tStocks;
}
