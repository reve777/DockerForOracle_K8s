package com.portfolio.Kafka.interf;

import com.portfolio.Kafka.model.dto.OrderBatchPayload;
import com.portfolio.Kafka.model.dto.OrderEvent;

public interface MessagePublisher<K, V> {
	// 單筆發送
	void publish(String topic, K key, OrderEvent payload);

	// 批次發送
	void publish(String topic, K key, OrderBatchPayload payloads);
}