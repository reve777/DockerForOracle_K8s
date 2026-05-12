package com.portfolio.Kafka.controller.demo2;

import com.portfolio.Kafka.service.demo2.OrderChartService;
import lombok.RequiredArgsConstructor;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class KafkaController {

	private final OrderChartService orderChartService;

	/*
	 * 前端呼叫此 API 取得長條圖資料
	 * 使用 AtomicReference 確保多執行緒安全，並透過 @KafkaListener 獲取
	 * >>>
	 * 測試，從中獲取預先獲取cache的資料，未獲取再從DB抓取
	 * 
	 */
	@GetMapping("/chart-data")
	public Map<String, Object> getChartData() {
		return orderChartService.getChartData();
	}
}
