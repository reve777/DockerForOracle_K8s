package com.portfolio.Kafka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.Kafka.model.dto.OrderEvent;
import com.portfolio.Kafka.service.OrderProducerService;

@RestController
@RequestMapping("/api/test")
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaTestController {

    // 注入你寫好的 Producer Service
    @Autowired
    private OrderProducerService orderProducerService;

    @PostMapping("/simulate-pay-success")
    public ResponseEntity<String> simulatePayment() {
        // 1. 產生假資料
        String fakeOrderId = "ORD-" + System.currentTimeMillis();
        OrderEvent event = new OrderEvent(
            fakeOrderId,
            "USER_9527",
            "iPhone 15 Pro",
            36900.0,
            "PAID"
        );

        // 2. 透過 Service 發送至 Kafka
        orderProducerService.publishOrderEvent(event);

        return ResponseEntity.ok("模擬支付成功！已發送 Kafka 訊息。OrderID: " + fakeOrderId);
    }
}