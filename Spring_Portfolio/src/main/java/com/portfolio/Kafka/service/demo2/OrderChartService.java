package com.portfolio.Kafka.service.demo2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.Kafka.interf.MessagePublisher;
import com.portfolio.Kafka.model.OrderRecord;
import com.portfolio.Kafka.model.dto.OrderBatchPayload;
import com.portfolio.Kafka.model.dto.OrderEvent;
import com.portfolio.Kafka.repository.OrderRecordRepository;
import com.portfolio.Kafka.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderChartService {

	private final OrderRecordRepository orderRecordRepository;
	private final ProductRepository productRepository;
	private final ObjectMapper objectMapper = new ObjectMapper(); // 用於解析 JSON

	/**
	 * 使用 AtomicReference 確保多執行緒安全。
	 * 當 Kafka 監聽器更新快取時，Controller 讀取的執行緒能立即看到最新結果。
	 */
	private final AtomicReference<List<OrderEvent>> latestBatchCache = new AtomicReference<>(new ArrayList<>());

	/**
	 * 監聽器 1：負責同步至資料庫 (單筆處理)
	 */
	@KafkaListener(topics = "order-topic", groupId = "order-group")
	public void consumeOrder(
			OrderEvent event,
			@Header(KafkaHeaders.RECEIVED_KEY) String key) {

		productRepository.findAll().stream()
				.filter(p -> p.getName().equals(event.getProductName()))
				.findFirst()
				.ifPresent(product -> {
					OrderRecord record = OrderRecord.builder()
							.orderId(event.getOrderId())
							.productId(product.getId())
							.amount(event.getAmount())
							.status(event.getStatus())
							.kafkaKey(key)
							.build();
					orderRecordRepository.save(record);
					log.info("--- [DB] 訂單 {} 已同步至資料庫 ---", event.getOrderId());
				});
	}

	/**
	 * 監聽器 2：專門監聽發送給 "product" topic 的整個 List 批次訊息
	 * 此處直接更新記憶體快取，供前端圖表即時讀取。
	 */
	@KafkaListener(topics = "product", groupId = "chart-cache-group")
	public void listenProductBatch(OrderBatchPayload payload) { // 直接接收 DTO
		log.info("--- [KAFKA] 成功接收並自動轉換批次數據 ({} 筆) ---", payload.getEvents().size());

		try {
			if (payload.getEvents() != null) {
				// 直接更新快取，不需要手動 readValue 了
				this.latestBatchCache.set(payload.getEvents());
				log.info("--- [SUCCESS] 快取已更新，目前內含 {} 筆資料 ---", payload.getEvents().size());
			}
		} catch (Exception e) {
			log.error("--- [ERROR] 快取更新失敗: {} ---", e.getMessage());
		}
	}

	/**
	 * 提供給 Controller 的圖表資料邏輯
	 */
	public Map<String, Object> getChartData() {
		// 1. 直接從快取中讀取最新數據
		List<OrderEvent> kafkaData = latestBatchCache.get();

		if (kafkaData != null && !kafkaData.isEmpty()) {
			log.info("--- [CACHE] 使用 Kafka 即時快取數據產生圖表 ---");
			return convertToChartFormat(processKafkaStats(kafkaData));
		}

		// 2. 如果快取為空（剛啟動或尚未有訊息），走 DB 邏輯
		log.info("--- [DB] 無即時快取，從資料庫讀取歷史統計 ---");
		List<Map<String, Object>> stats = orderRecordRepository.getSalesCountPerProduct();
		return convertToChartFormat(stats);
	}

	/**
	 * 將 Kafka 的原始 List 轉換為統計格式 (產品名稱 -> 數量)
	 */
	private List<Map<String, Object>> processKafkaStats(List<OrderEvent> events) {
		return events.stream()
				.collect(Collectors.groupingBy(OrderEvent::getProductName, Collectors.counting()))
				.entrySet().stream()
				.map(e -> {
					Map<String, Object> map = new HashMap<>();
					map.put("productName", e.getKey());
					map.put("orderCount", e.getValue());
					return map;
				})
				.collect(Collectors.toList());
	}

	/**
	 * 將統計結果轉換為 Echarts 或前端易讀的 categories/data 格式
	 */
	private Map<String, Object> convertToChartFormat(List<Map<String, Object>> stats) {
		List<String> categories = stats.stream()
				.map(s -> s.get("productName").toString())
				.collect(Collectors.toList());

		List<Long> data = stats.stream()
				.map(s -> ((Number) s.get("orderCount")).longValue())
				.collect(Collectors.toList());

		return Map.of(
				"categories", categories,
				"data", data);
	}
}