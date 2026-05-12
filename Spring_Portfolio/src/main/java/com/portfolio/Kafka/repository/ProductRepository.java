package com.portfolio.Kafka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.portfolio.Kafka.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
