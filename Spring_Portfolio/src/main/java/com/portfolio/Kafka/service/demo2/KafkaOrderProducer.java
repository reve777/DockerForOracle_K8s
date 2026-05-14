package com.portfolio.Kafka.service.demo2;

import com.portfolio.Kafka.model.Product;
import com.portfolio.Kafka.interf.MessagePublisher;
import com.portfolio.Kafka.model.OrderRecord;
import com.portfolio.Kafka.model.dto.OrderBatchPayload;
import com.portfolio.Kafka.model.dto.OrderEvent;
import com.portfolio.Kafka.repository.ProductRepository;
import com.portfolio.uitls.IdGenerator;
import com.portfolio.Kafka.repository.OrderRecordRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaOrderProducer {

	// 修改點：使用自定義的接口，不再直接操作 KafkaTemplate
	private final MessagePublisher<String, Object> messagePublisher;

	private final ProductRepository productRepository;
	private final OrderRecordRepository orderRecordRepository;
	private final TransactionTemplate transactionTemplate;
	private final Random random = new Random();
	private final IdGenerator idGenerator;

	@PostConstruct
	public void initProducts() {
		if (productRepository.count() == 0) {
			log.info("--- [INIT] 初始化商品資料庫 (10筆資料) ---");
			List<Product> initialProducts = List.of(
					new Product(null, "iPhone 15 Pro", 36900.0),
					new Product(null, "iPhone 15", 29900.0),
					new Product(null, "MacBook Pro M3", 54900.0),
					new Product(null, "MacBook Air M2", 35900.0),
					new Product(null, "iPad Pro", 27900.0),
					new Product(null, "iPad Air", 19900.0),
					new Product(null, "Apple Watch S9", 13500.0),
					new Product(null, "Apple Watch Ultra 2", 27900.0),
					new Product(null, "AirPods Pro 2", 7490.0),
					new Product(null, "AirPods Max", 17990.0));
			productRepository.saveAll(initialProducts);
			productRepository.flush(); // 確保資料立即寫入以供後續查詢
			log.info("--- [INIT] 10 種商品初始化完成 ---");
		}
	}

	/**
	 * 每 60 分鐘執行一次。
	 * 使用 TransactionTemplate 確保資料庫寫入完成後再發送訊息
	 */
	@Scheduled(fixedRate = 3600000, initialDelay = 10000)
	public void generateStoreAndSend() {
		List<Product> products = productRepository.findAll();

		if (products == null || products.isEmpty()) {
			log.warn("目前無商品資料，跳過執行。");
			return;
		}

		Map<Long, String> productMap = products.stream()
				.collect(Collectors.toMap(Product::getId, Product::getName));

		String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

		// --- 階段 1：存入資料庫 (獨立事務) ---
		log.info("Step 1: 開始產生並存入 1000 筆訂單至 DB...");

		List<OrderRecord> savedOrders = transactionTemplate.execute(status -> {
			List<OrderRecord> newOrders = new ArrayList<>();
			for (int i = 0; i < 1000; i++) {
				Product randomProduct = products.get(random.nextInt(products.size()));
				String orderId = idGenerator.generateCompactUniqueId();
				String kafkaKey = "ORDER_" + currentTime + "_INDEX_" + i;

				OrderRecord record = OrderRecord.builder()
						.orderId(orderId)
						.productId(randomProduct.getId())
						.amount(randomProduct.getPrice())
						.status("PENDING")
						.kafkaKey(kafkaKey)
						.build();
				newOrders.add(record);
			}
			List<OrderRecord> result = orderRecordRepository.saveAll(newOrders);
			orderRecordRepository.flush();
			return result;
		});

		log.info("成功儲存 1000 筆訂單至 DB (事務已提交)。");

		// --- 階段 2：發送到 Kafka ---
		if (savedOrders == null || savedOrders.isEmpty()) {
			log.warn("無儲存的訂單可發送。");
			return;
		}

		log.info("Step 2: 開始打包並發送至 Kafka...");

		List<OrderEvent> eventList = savedOrders.stream()
				.map(savedRecord -> OrderEvent.builder()
						.orderId(savedRecord.getOrderId())
						.productName(productMap.getOrDefault(savedRecord.getProductId(), "Unknown"))
						.amount(savedRecord.getAmount())
						.status(savedRecord.getStatus())
						.build())
				.collect(Collectors.toList());

		try {
			String batchKey = "BATCH_" + currentTime;

			// 👉 新增這行：將 List 包裝進 DTO
			OrderBatchPayload payload = new OrderBatchPayload(
					eventList);

			// 👉 修改這行：發送包裝好的 payload，而不是 eventList
			messagePublisher.publish("product", batchKey, payload);

			log.info("成功發送批次訊息至 Kafka，包含 {} 筆訂單。", eventList.size());
		} catch (Exception e) {
			log.error("Kafka 批次發送失敗，原因: {}", e.getMessage());
		}
	}
}