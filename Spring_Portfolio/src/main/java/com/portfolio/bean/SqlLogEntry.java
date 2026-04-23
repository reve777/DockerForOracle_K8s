package com.portfolio.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.portfolio.constants.EntityConstants;
import com.portfolio.constants.SystemConstants;
import com.portfolio.uitls.LogUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlLogEntry {
	private String sql;
	private String parameters;
	private String targetEntity;

	// SQL 類型枚舉
	public enum SqlType {
		INSERT, UPDATE, DELETE, SELECT, OTHER
	}

	// 表名提取正則表達式
	private static final Map<SqlType, Pattern> SQL_PATTERNS = new HashMap<>();
	// 用於表名到實體名的映射
	private static final Map<String, String> TABLE_TO_ENTITY_MAP = new HashMap<>();

	static {
		// 初始化 SQL 模式
		SQL_PATTERNS.put(SqlType.INSERT, Pattern.compile("INSERT\\s+INTO\\s+([\\w_\\.]+)", Pattern.CASE_INSENSITIVE));
		SQL_PATTERNS.put(SqlType.UPDATE, Pattern.compile("UPDATE\\s+([\\w_\\.]+)", Pattern.CASE_INSENSITIVE));
		SQL_PATTERNS.put(SqlType.DELETE, Pattern.compile("DELETE\\s+FROM\\s+([\\w_\\.]+)", Pattern.CASE_INSENSITIVE));
		SQL_PATTERNS.put(SqlType.SELECT,
				Pattern.compile("SELECT\\b.*?\\bFROM\\s+([\\w_\\.]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));

		// 初始化表名映射
		// 信託機構
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_BANK_MST, EntityConstants.ENTITY_BANK_MST);
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_BANK_TEMP, EntityConstants.ENTITY_BANK_TEMP);

		// 機構簽發憑證
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_CERTIFICATE_MST, EntityConstants.ENTITY_CERTIFICATE_MST);
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_CERTIFICATE_TEMP, EntityConstants.ENTITY_CERTIFICATE_TEMP);

		// JWE 金鑰
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_JWE_KEY_MST, EntityConstants.ENTITY_JWE_KEY_MST);
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_JWE_KEY_TEMP, EntityConstants.ENTITY_JWE_KEY_TEMP);

		// 通知群組
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_NOTIFY_GROUP, EntityConstants.ENTITY_NOTIFY_GROUP);
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_NOTIFY_GROUP_TEMP, EntityConstants.ENTITY_NOTIFY_GROUP_TEMP);

		// 通知人員
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_NOTIFY_MEMBER, EntityConstants.ENTITY_NOTIFY_MEMBER);
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_NOTIFY_MEMBER_TEMP, EntityConstants.ENTITY_NOTIFY_MEMBER_TEMP);

		// 收單機構
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_ACQUIRER_MST, EntityConstants.ENTITY_ACQUIRER_MST);
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_ACQUIRER_TEMP, EntityConstants.ENTITY_ACQUIRER_TEMP);

		// License
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_LICENSE_MST, EntityConstants.ENTITY_LICENSE_MST);
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_LICENSE_TEMP, EntityConstants.ENTITY_LICENSE_TEMP);

		// 系統監控
		TABLE_TO_ENTITY_MAP.put(EntityConstants.TABLE_TXN_STATUS, EntityConstants.ENTITY_TXN_STATUS);
	}

	public SqlLogEntry(String sql, String parameters) {
		this.sql = sql;
		this.parameters = parameters;
		detectTargetEntity();
	}

	public SqlType getSqlType() {
		if (sql == null)
			return SqlType.OTHER;

		String upperSql = sql.toUpperCase();
		if (upperSql.startsWith(SystemConstants.INSERT_PREFIX))
			return SqlType.INSERT;
		if (upperSql.startsWith(SystemConstants.UPDATE_PREFIX))
			return SqlType.UPDATE;
		if (upperSql.startsWith(SystemConstants.DELETE_PREFIX))
			return SqlType.DELETE;
		if (upperSql.startsWith(SystemConstants.SELECT_PREFIX))
			return SqlType.SELECT;

		return SqlType.OTHER;
	}

	public void detectTargetEntity() {
		if (sql == null)
			return;

		SqlType sqlType = getSqlType();
		Pattern pattern = SQL_PATTERNS.get(sqlType);

		if (pattern == null) {
			LogUtils.debug("不支持的 SQL 類型: {}", sqlType);
			return;
		}

		Matcher matcher = pattern.matcher(sql);
		if (matcher.find()) {
			String tableName = matcher.group(1);

			// 移除可能的 schema 前綴, ex:SCHEMA_NAME.TABLE_NAME
			if (tableName.contains(".")) {
				tableName = tableName.substring(tableName.lastIndexOf(".") + 1);
			}

			// 從映射表中查找實體名
			this.targetEntity = TABLE_TO_ENTITY_MAP.get(tableName.toUpperCase());

			if (this.targetEntity != null) {
				LogUtils.debug("已關聯到實體: {} -> {}", tableName, this.targetEntity);
			} else {
				LogUtils.debug("未找到表名 {} 對應的實體", tableName);
			}
		} else {
			LogUtils.debug("無法從 SQL 中提取表名: {}", sql);
		}
	}

	@Override
	public String toString() {
		return String.format("SQL=%s, TYPE=%s, ENTITY=%s",
				sql.length() > 50 ? sql.substring(0, 50) + "..." : sql,
				getSqlType(),
				targetEntity != null ? targetEntity : "N/A");
	}

}