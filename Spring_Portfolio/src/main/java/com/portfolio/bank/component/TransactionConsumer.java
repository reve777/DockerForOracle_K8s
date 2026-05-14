package com.portfolio.bank.component;

import java.sql.Timestamp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.portfolio.bank.dto.TransactionMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class TransactionConsumer {

	private final JdbcTemplate jdbcTemplate; // 或使用你的 Repository

	@KafkaListener(topics = "ivan-transfer-topic", groupId = "txn_group")
	public void listen(TransactionMessage msg) {
		try {
			String sql = "INSERT INTO ivan_txn_log (txn_id, from_account, to_account, amount, status, created_at) VALUES (?, ?, ?, ?, ?, ?)";
			jdbcTemplate.update(sql,
					msg.getTxnId(),
					msg.getFromAccount(),
					msg.getToAccount(),
					msg.getAmount(),
					msg.getStatus(),
					new Timestamp(msg.getTimestamp()));
		} catch (Exception e) {
			log.error("寫入交易流水失敗: {}", e.getMessage());
		}
	}
}
