package com.portfolio.bean;

import java.util.ArrayList;
import java.util.List;

import com.portfolio.entity.AuditedEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class AuditInfo {
	// 資料庫對應欄位
	private String guid; // 識別代號
	private String writeDate; // 寫入日期 YYYYMMDD
	private String writeTime; // 寫入時間 HH24MISS
	private String systemId; // 系統代號
	private String functionId; // 功能代碼
	private String functionName; // 功能名稱
	private String requestURL; // 請求URL
	private String deptId; // 部門ID
	private String teamId; // 團隊ID
	private String userId; // 使用者ID
	private String userName; // 使用者名稱
	private String accessDate; // 存取日期 YYYYMMDD
	private String accessTime; // 存取時間 HH24MISS
	private String accessType; // 存取類型
	private String successFlag = "Y"; // 成功標誌
	private String sourceIp; // 來源IP
	private String targetIp; // 目標IP
	private String queryInput; // 查詢輸入
	private String functionCount; // 功能執行結果筆數

	// 非資料庫欄位 (用於處理業務邏輯)
	private AuditedEntity beforeImage; // 修改前資料 (主檔舊資料)
	private AuditedEntity afterImage; // 修改後資料 (主檔新資料)
	private AuditedEntity tempImage; // 暫存檔資料 (覆核操作的 TEMP 表資料)
	private List<SqlLogEntry> sqlLogEntryList = new ArrayList<>();

	/**
	 * 取得當前時間並設定日期時間欄位
	 */
	public void setCurrentDateTime() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

		this.writeDate = now.format(dateFormatter);
		this.writeTime = now.format(timeFormatter);

		// 如果未設定存取日期和時間，則同時設定
		if (this.accessDate == null) {
			this.accessDate = this.writeDate;
		}
		if (this.accessTime == null) {
			this.accessTime = this.writeTime;
		}
	}

	/**
	 * 添加 SQL 日誌條目
	 */
	public void addSqlLogEntry(SqlLogEntry sqlLogEntry) {
		if (sqlLogEntryList == null) {
			sqlLogEntryList = new ArrayList<>();
		}
		sqlLogEntryList.add(sqlLogEntry);
	}

	/**
	 * 取得 UPDATE 資訊
	 */
	public String getUpdateSqlInfo() {
		StringBuilder sb = new StringBuilder();
		if (sqlLogEntryList != null) {
			sqlLogEntryList.stream()
					.filter(entry -> entry != null && entry.getSql() != null
							&& entry.getSql().toLowerCase().startsWith("update"))
					.forEach(entry -> {
						if (sb.length() > 0) {
							sb.append(" | "); // 使用分隔符
						}
						sb.append("UPDATE Info: ");
						sb.append("SQL: ").append(entry.getSql());
						sb.append(", Params: ").append(entry.getParameters());
					});
		}
		return sb.toString();
	}

	/**
	 * 將查詢條件轉換為 queryInput 欄位 (限制在1000字元以內)
	 */
	public void setQueryInputFromParameters(String parameters) {
		if (parameters != null && !parameters.isEmpty()) {
			// 限制長度在1000字元以內
			this.queryInput = parameters.length() > 1000 ? parameters.substring(0, 1000) : parameters;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("guid:").append(guid).append("\n");
		sb.append("systemId:").append(systemId).append("\n");
		sb.append("functionId:").append(functionId).append("\n");
		sb.append("functionName:").append(functionName).append("\n");
		sb.append("userId:").append(userId).append("\n");
		sb.append("userName:").append(userName).append("\n");
		sb.append("accessDate:").append(accessDate).append("\n");
		sb.append("accessTime:").append(accessTime).append("\n");
		sb.append("accessType:").append(accessType).append("\n");
		sb.append("successFlag:").append(successFlag).append("\n");
		sb.append("sourceIp:").append(sourceIp).append("\n");

		if (sqlLogEntryList != null && !sqlLogEntryList.isEmpty()) {
			sb.append("sqlLogEntryList:").append("[");
			sqlLogEntryList.forEach(sqlLogEntry -> {
				if (sqlLogEntry != null) {
					sb.append(sqlLogEntry.toString()).append(",");
				}
			});
			if (sb.charAt(sb.length() - 1) == ',') {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append("]");
		} else {
			sb.append("sqlLogEntryList:[]");
		}
		return sb.toString();
	}
}