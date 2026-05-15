package com.portfolio.bank.component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
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

	/**
	 * kafka 單次寫入
	 **/
//	@KafkaListener(topics = "ivan-transfer-topic", groupId = "txn_group")
//	public void listen(TransactionMessage msg) {
//		try {
//			String sql = "INSERT INTO ivan_txn_log (txn_id, from_account, to_account, amount, status, created_at) VALUES (?, ?, ?, ?, ?, ?)";
//			jdbcTemplate.update(sql,
//					msg.getTxnId(),
//					msg.getFromAccount(),
//					msg.getToAccount(),
//					msg.getAmount(),
//					msg.getStatus(),
//					new Timestamp(msg.getTimestamp()));
//		} catch (Exception e) {
//			log.error("寫入交易流水失敗: {}", e.getMessage());
//		}
//	}
	/**
	 * kafka 批次寫入
	 */
	@KafkaListener(topics = "ivan-transfer-topic", groupId = "txn_group")
	public void listen(List<TransactionMessage> messages) {
		if (messages == null || messages.isEmpty()) {
			return;
		}

		long startTime = System.currentTimeMillis();
		log.info(">>> 開始處理 Kafka 批次，筆數: {}", messages.size());

		try {
			// 使用 Oracle 語法的 INSERT
			String sql = "INSERT INTO ivan_txn_log (txn_id, from_account, to_account, amount, status, created_at) " +
					"VALUES (?, ?, ?, ?, ?, ?)";

			// 執行批次更新
			int[] updateCounts = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					TransactionMessage msg = messages.get(i);
					ps.setString(1, msg.getTxnId());
					ps.setString(2, msg.getFromAccount());
					ps.setString(3, msg.getToAccount());
					ps.setBigDecimal(4, msg.getAmount());
					ps.setString(5, msg.getStatus());
					// 建議檢查 timestamp 是否為 null
					ps.setTimestamp(6, msg.getTimestamp() != 0 ? new Timestamp(msg.getTimestamp())
							: new Timestamp(System.currentTimeMillis()));
				}

				@Override
				public int getBatchSize() {
					return messages.size();
				}
			});

			long endTime = System.currentTimeMillis();
			log.info(">>> 批次寫入成功，耗時: {} ms，成功筆數: {}", (endTime - startTime), updateCounts.length);

		} catch (Exception e) {
			// 在生產環境，若批次失敗，通常會需要紀錄哪些 txn_id 失敗，或進行重試
			log.error("!!! Oracle 批次寫入失敗，原因: {}", e.getMessage(), e);

			// 建議這裡可以加入備份邏輯 (如寫入 Error Log Table)，避免 100 萬筆數據因某一筆異常而整批遺失
		}
	}
}
