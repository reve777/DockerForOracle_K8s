package com.portfolio.Kafka.interf;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.portfolio.Kafka.model.dto.OrderBatchPayload;
import com.portfolio.Kafka.model.dto.OrderEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaPublisherImpl implements MessagePublisher<String, Object> {

	// 建議將 KafkaTemplate 宣告為 Object，這樣一個 Template 就能發送任何類型的 Payload
	private final KafkaTemplate<String, Object> kafkaTemplate;

	/**
	 * 發送單筆訂單
	 */
	@Override
	public void publish(String topic, String key, OrderEvent payload) {
		kafkaTemplate.send(topic, key, payload);
	}

	/**
	 * 發送批次訂單 (List)
	 */
	@Override
	public void publish(String topic, String key, OrderBatchPayload payloads) {
		// 注意：這裡直接將 List 作為一條訊息的 Value 發出
		kafkaTemplate.send(topic, key, payloads);
	}
}