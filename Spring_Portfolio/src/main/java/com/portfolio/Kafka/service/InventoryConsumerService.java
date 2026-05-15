package com.portfolio.Kafka.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import com.portfolio.Kafka.model.dto.OrderEvent;

@Service
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class InventoryConsumerService {

	/**
	 * 【重要：為什麼移除 @RetryableTopic？】
	 * 
	 * 1. 架構衝突：Spring Kafka 目前的非同步重試機制 (@RetryableTopic) 僅支援單筆 (Record) 模式。
	 * 當配置為 setBatchListener(true) 時，底層適配器 (BatchMessagingMessageListenerAdapter)
	 * 不符合重試機制的型別要求，會導致啟動時拋出 IllegalArgumentException。
	 * 
	 * 2. 效能考量：在處理 100 萬筆數據的高併發場景下，若批次中某一筆失敗導致整批重試，
	 * 會產生嚴重的寫入重複與網路 IO 浪費。
	 * 
	 * 3. 錯誤處理建議：
	 * 改用 try-catch 包裹 jdbcTemplate.batchUpdate，若發生失敗，
	 * 應將整批或錯誤數據記錄至 Error Log Table (Oracle) 或專屬的死信隊列 (DLQ) 手動處理。
	 * 
	 * 
	 * ###此註解與 Batch Listener 衝突，已移除
	 */
	// 設定重試 3 次，每次間隔 2 秒。失敗後送入 DLT。
//	@RetryableTopic(attempts = "3", backoff = @Backoff(delay = 2000), autoCreateTopics = "true")
	@KafkaListener(topics = "order-events", groupId = "inventory-group")
	public void consumeOrder(OrderEvent event,
			@Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {

		System.out.println("📦 收到訂單, OrderID: " + event.getOrderId() +
				" (來自 Partition: " + partition + ")");

		// 模擬業務邏輯或拋出異常
		if (event.getAmount() < 0) {
			throw new IllegalArgumentException("訂單金額異常，觸發重試機制！");
		}

		System.out.println("✅ 庫存扣減成功！");
	}

	// 監聽死信佇列 (DLT) - 處理最終失敗的毒藥訊息
	@KafkaListener(topics = "order-events-dlt", groupId = "inventory-group")
	public void consumeDlt(OrderEvent event) {
		System.err.println("進入死信佇列 (DLQ)，需人工介入處理！OrderID: " + event.getOrderId());
		// 實務上這裡可能會觸發 Slack/Line 警報系統，或將資料存入 DB 狀態改為「異常」
	}
}
