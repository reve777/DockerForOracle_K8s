package com.portfolio.Kafka.model;

import jakarta.persistence.*;
import lombok.*;

// 1. 商品 Table
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private Double price;
}
