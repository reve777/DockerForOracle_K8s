package com.portfolio.Kafka.model.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 訂單事件 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private String orderId;
	private String userId;
	private String productName;
	private Double amount;
	private String status;
}