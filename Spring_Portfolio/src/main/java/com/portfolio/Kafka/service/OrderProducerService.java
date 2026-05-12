package com.portfolio.Kafka.service;

import org.springframework.stereotype.Service;

import com.portfolio.Kafka.interf.MessagePublisher;
import com.portfolio.Kafka.model.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor // 自動生成建構子，注入 MessagePublisher
public class OrderProducerService {

    // ✅ 改用統一接口，泛型與 KafkaConfig 裡的 Object 匹配
    private final MessagePublisher<String, Object> messagePublisher;
    
    // 將 Topic 名稱統一定義
    private static final String TOPIC = "order-events";

    /**
     * 發布單筆訂單事件
     */
    public void publishOrderEvent(OrderEvent event) {
        try {
            // 使用 userId 作為 Kafka Key，確保同用戶的訊息進入同一個 Partition
            // 呼叫接口的 publish 方法
            messagePublisher.publish(TOPIC, event.getUserId(), event);
            
            log.info("✅ 訂單事件已發布 (Producer), OrderID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("❌ 訂單事件發布失敗, OrderID: {}, Error: {}", event.getOrderId(), e.getMessage());
        }
    }
}