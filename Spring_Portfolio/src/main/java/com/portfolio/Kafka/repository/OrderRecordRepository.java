package com.portfolio.Kafka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.portfolio.Kafka.model.OrderRecord;

import java.util.List;
import java.util.Map;

@Repository
public interface OrderRecordRepository extends JpaRepository<OrderRecord, Long> {

	// 聚合查詢：統計每個商品的銷售數量，回傳給 Highcharts
	@Query("SELECT p.name AS productName, COUNT(o.id) AS orderCount " +
			"FROM OrderRecord o JOIN Product p ON o.productId = p.id " +
			"GROUP BY p.name")
	List<Map<String, Object>> getSalesCountPerProduct();
}