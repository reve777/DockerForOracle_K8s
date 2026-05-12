package com.portfolio.Kafka.model;

import jakarta.persistence.*;
import lombok.*;

//2. 訂單紀錄 Table (Kafka 存入資料庫用)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ORDER_RECORD")
public class OrderRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ORDER_ID")
    private String orderId;

    @Column(name = "PRODUCT_ID") // 關鍵！修正 ORA-00904
    private Long productId;

    @Column(name = "AMOUNT")
    private Double amount;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "KAFKA_KEY") // 建議也明確指定，Oracle 比較嚴格
    private String kafkaKey;
}
