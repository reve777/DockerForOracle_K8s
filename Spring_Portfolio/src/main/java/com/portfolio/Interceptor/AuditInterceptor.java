package com.portfolio.Interceptor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import org.springframework.web.servlet.HandlerInterceptor;

import com.portfolio.bean.AuditInfo;
import com.portfolio.bean.SqlLogEntry;
import com.portfolio.constants.EntityConstants;
import com.portfolio.constants.MessageConstants;
import com.portfolio.constants.SystemConstants;
import com.portfolio.eunms.DeptIdEnum;
import com.portfolio.eunms.FunctionEnum;
import com.portfolio.exceptions.UnauthorizedException;
import com.portfolio.uitls.AuditFormatter;
import com.portfolio.uitls.AuditInfoHolder;
import com.portfolio.uitls.IdGenerator;
import com.portfolio.uitls.LogUtils;
import com.portfolio.uitls.StringHelper;
import com.portfolio.uitls.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditInterceptor implements HandlerInterceptor {

	private final DataSource auditDataSource;
	private final IdGenerator idGenerator;

	private final static String TABLE_TEMP = "TEMP"; // 暫存表判斷字串
	private final static String QUERY_CHANGE_COUNT = "0"; // 查詢異動筆數

	/*
	 * 執行時機： 在請求到達控制器（Controller）之前執行。 用途： 適合用於前置處理，例如權限驗證、日誌記錄、參數檢查等。 如果此方法返回
	 * false，請求將被中斷，後續的攔截器和控制器將不會被執行。 Here: preHandle 方法：
	 * 在請求處理之前，記錄請求的相關資訊，如標頭、路徑、方法和遠端地址，並建立審計資訊物件。
	 * 
	 * @NonNull 是文檔作用，並不影響程式運作。
	 */
	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull Object handler) throws Exception {
		// 印出所有 header
//        Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            LogUtils.system("{} / {} ", headerName, request.getHeader(headerName));
//        }

		String userId = request.getHeader(SystemConstants.PERMISSION_USER_ID);
		String userName = Utils.decodeHeader(request.getHeader(SystemConstants.PERMISSION_USER_CN));
		String deptId = request.getHeader(SystemConstants.PERMISSION_DEPT_ID);
		String deptName = Utils.decodeHeader(request.getHeader(SystemConstants.PERMISSION_DEPT_NAME));

		// 測試Code
		userId = "testId";
		userName = "testName";

		String sourceIp = getClientIp(request);

		if (StringUtils.isBlank(userId) && StringUtils.isBlank(userName)) {
			LogUtils.bizWarn("請求缺少必要的使用者識別資訊 [uri={}, sourceIp={}]", request.getRequestURI(), sourceIp);
			throw new UnauthorizedException(MessageConstants.REQUEST_HEADER_AUTH_ERROR);
		}

		AuditInfo auditInfo = new AuditInfo();
		auditInfo.setUserName(userName);
		auditInfo.setUserId(userId);
		auditInfo.setTeamId(deptId);
		auditInfo.setDeptId(DeptIdEnum.convertDeptId(deptId, deptName));
		auditInfo.setRequestURL(request.getRequestURI());
		auditInfo.setGuid(idGenerator.generateCompactUniqueId());
		auditInfo.setSystemId(SystemConstants.SYSTEM_ID_STR);
		auditInfo.setSourceIp(sourceIp);
		auditInfo.setSuccessFlag(SystemConstants.STRING_Y); // 設置成功標誌（預設為Y）

		// 設置日期時間相關欄位，按照 YYYYMMDD 和 HH24MISS 格式
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

		auditInfo.setWriteDate(now.format(dateFormatter));
		auditInfo.setWriteTime(now.format(timeFormatter));
		auditInfo.setAccessDate(now.format(dateFormatter));
		auditInfo.setAccessTime(now.format(timeFormatter));

		// 設置至 ThreadLocal
		AuditInfoHolder.setAuditInfo(auditInfo);

		// 記錄請求信息
		LogUtils.operate("開始處理: " + request.getRequestURI());

		return true;
	}

	/*
	 * 執行時機： 在整個請求完成之後，亦即視圖渲染結束後執行。 用途： 適合用於資源清理、最終日誌記錄、異常處理等操作。
	 * 
	 * Here: 在請求處理完成後，記錄審計資訊，包括請求處理時間，並將這些資訊保存到資料庫中。
	 */
	@Override
	public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull Object handler, @Nullable Exception ex) throws Exception {

		AuditInfo auditInfo = AuditInfoHolder.getAuditInfo();
		if (ex != null) {
			auditInfo.setSuccessFlag(SystemConstants.STRING_N); // 處理失敗
			LogUtils.error("請求處理異常", ex);
		}

		auditInfo.setFunctionId(FunctionEnum.parseFunctionId(auditInfo.getRequestURL()));
		auditInfo.setFunctionName(FunctionEnum.parseFunctionName(auditInfo.getRequestURL()));
		auditInfo.setTargetIp(getLocalHostAddress());

		LogUtils.operate("audit information : guid={}, path={}, userId={}", auditInfo.getGuid(),
				auditInfo.getRequestURL(), auditInfo.getUserId());

		// 根據 SQL 日誌條目判斷 ActType
		determineMasterActType(auditInfo);
		saveAPLog(auditInfo);

		AuditInfoHolder.clearAuditInfo();

		// 清除 MDC 上下文，防止線程池重用導致的上下文污染
		LogUtils.clear();
	}

	/**
	 * 根據 SQL 日誌條目決定 LOG_MASTER 的 ACT_TYPE 優先順序: A (新增) > D (刪除) > U (修改) > Q (查詢)
	 */
	private void determineMasterActType(AuditInfo auditInfo) {
		// 優先使用已設定的 actType
		if (StringUtils.isNotBlank(auditInfo.getAccessType())) {
			return;
		}

		List<SqlLogEntry> entries = auditInfo.getSqlLogEntryList();
		if (entries == null || entries.isEmpty()) {
			// 如果沒有 SQL 日誌條目，預設為查詢
			auditInfo.setAccessType(SystemConstants.ACT_TYPE_Q);
			LogUtils.operate("無 SQL 日誌條目，預設 Master ActType: {}", SystemConstants.ACT_TYPE_Q);
			return;
		}

		// 找出最高優先級的操作類型
		// 優先級: INSERT(3) > DELETE(2) > UPDATE(1) > SELECT/OTHER(0)
		String actType = entries.stream().map(entry -> {
			SqlLogEntry.SqlType sqlType = entry.getSqlType();
			switch (sqlType) {
			case INSERT:
				return new Object[] { 3, SystemConstants.ACT_TYPE_A };
			case DELETE:
				return new Object[] { 2, SystemConstants.ACT_TYPE_D };
			case UPDATE:
				return new Object[] { 1, SystemConstants.ACT_TYPE_U };
			default:
				return new Object[] { 0, SystemConstants.ACT_TYPE_Q };
			}
		}).max((a, b) -> Integer.compare((Integer) a[0], (Integer) b[0])).map(pair -> (String) pair[1])
				.orElse(SystemConstants.ACT_TYPE_Q); // 預設為查詢

		auditInfo.setAccessType(actType);
		LogUtils.operate("確定的 Master ActType: {}", actType);
	}

	private void saveAPLog(AuditInfo auditInfo) {
		saveAPLogMaster(auditInfo);
		saveAPLogDetail(auditInfo);
	}

	private void saveAPLogMaster(AuditInfo auditInfo) {
		String sqlStatement = "INSERT INTO LOG_MASTER (GUID, WRITE_DATE, WRITE_TIME, SYSTEM_ID, FUNCTION_ID, "
				+ "FUNCTION_NAME, REQUEST_URL, DEPT_ID, TEAM_ID, USER_ID, USER_NAME, "
				+ "ACCESS_DATE, ACCESS_TIME, ACCESS_TYPE, SUCCESS_FLAG, SOURCE_IP, TARGET_IP, "
				+ "QUERY_INPUT, FUNCTION_COUNT) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection connection = auditDataSource.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
			preparedStatement.setString(1, auditInfo.getGuid()); // GUID
			preparedStatement.setString(2, auditInfo.getWriteDate()); // WRITE_DATE
			preparedStatement.setString(3, auditInfo.getWriteTime()); // WRITE_TIME
			preparedStatement.setString(4, auditInfo.getSystemId()); // SYSTEM_ID
			preparedStatement.setString(5, auditInfo.getFunctionId()); // FUNCTION_ID
			preparedStatement.setString(6, auditInfo.getFunctionName()); // FUNCTION_NAME
			preparedStatement.setString(7, auditInfo.getRequestURL()); // REQUEST_URL
			preparedStatement.setString(8, auditInfo.getDeptId()); // DEPT_ID
			preparedStatement.setString(9, auditInfo.getTeamId()); // TEAM_ID
			preparedStatement.setString(10, auditInfo.getUserId()); // USER_ID
			preparedStatement.setString(11, auditInfo.getUserName()); // USER_NAME
			preparedStatement.setString(12, auditInfo.getAccessDate()); // ACCESS_DATE
			preparedStatement.setString(13, auditInfo.getAccessTime()); // ACCESS_TIME
			preparedStatement.setString(14, auditInfo.getAccessType()); // ACCESS_TYPE
			preparedStatement.setString(15, auditInfo.getSuccessFlag()); // SUCCESS_FLAG
			preparedStatement.setString(16, auditInfo.getSourceIp()); // SOURCE_IP
			preparedStatement.setString(17, auditInfo.getTargetIp()); // TARGET_IP
			preparedStatement.setString(18, auditInfo.getQueryInput()); // QUERY_INPUT
			preparedStatement.setString(19,
					auditInfo.getFunctionCount() != null ? String.valueOf(auditInfo.getFunctionCount()) : ""); // FUNCTION_COUNT

			preparedStatement.execute();
		} catch (Exception e) {
			LogUtils.error("儲存審計日誌主檔信息時發生錯誤", e);
		}
	}

	private void saveAPLogDetail(AuditInfo auditInfo) {
		final String SQL_INSERT = "INSERT INTO LOG_DETAIL (GUID, WRITE_DATE, WRITE_TIME, SQL_STATEMENT_1, "
				+ "SQL_STATEMENT_2, BEFORE_IMAGE, AFTER_IMAGE, SQL_CODE, QUERY_COUNT) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		final int MAX_SQL_LENGTH = Integer.valueOf(SystemConstants.MAX_VARCHAR2_LENGTH);
		final int COLUMN_GUID = 1;
		final int COLUMN_WRITE_DATE = 2;
		final int COLUMN_WRITE_TIME = 3;
		final int COLUMN_SQL_STATEMENT_1 = 4;
		final int COLUMN_SQL_STATEMENT_2 = 5;
		final int COLUMN_BEFORE_IMAGE = 6;
		final int COLUMN_AFTER_IMAGE = 7;
		final int COLUMN_SQL_CODE = 8;
		final int COLUMN_QUERY_COUNT = 9;

		List<SqlLogEntry> sqlLogEntries = auditInfo.getSqlLogEntryList();
		if (sqlLogEntries == null || sqlLogEntries.isEmpty()) {
			LogUtils.warn("沒有 SQL 日誌條目可記錄");
			return;
		}

		// 建立實體到最後一個 DML 操作的映射
		Map<String, SqlLogEntry> entityToSqlMap = buildEntityToSqlMap(sqlLogEntries);

		try (Connection connection = auditDataSource.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT)) {
			connection.setAutoCommit(false); // 確保整個過程在同一事務中
			// 處理並批次添加所有SQL日誌條目
			processSqlLogEntries(preparedStatement, auditInfo, sqlLogEntries, entityToSqlMap, MAX_SQL_LENGTH,
					COLUMN_GUID, COLUMN_WRITE_DATE, COLUMN_WRITE_TIME, COLUMN_SQL_STATEMENT_1, COLUMN_SQL_STATEMENT_2,
					COLUMN_BEFORE_IMAGE, COLUMN_AFTER_IMAGE, COLUMN_SQL_CODE, COLUMN_QUERY_COUNT);

			preparedStatement.executeBatch();
			connection.commit();
		} catch (Exception e) {
			LogUtils.error(" 儲存審計日誌詳細信息時發生錯誤", e);
		}
	}

	/**
	 * 建立實體到最後一個 DML 操作的映射
	 */
	private Map<String, SqlLogEntry> buildEntityToSqlMap(List<SqlLogEntry> sqlLogEntries) {
		Map<String, SqlLogEntry> entityToSqlMap = new HashMap<>();

		for (SqlLogEntry entry : sqlLogEntries) {
			SqlLogEntry.SqlType sqlType = entry.getSqlType();
			if (entry.getTargetEntity() != null && (sqlType == SqlLogEntry.SqlType.INSERT
					|| sqlType == SqlLogEntry.SqlType.UPDATE || sqlType == SqlLogEntry.SqlType.DELETE)) {
				entityToSqlMap.put(entry.getTargetEntity(), entry);
			}
		}

		return entityToSqlMap;
	}

	/**
	 * 處理並批次添加所有SQL日誌條目
	 */
	private void processSqlLogEntries(PreparedStatement preparedStatement, AuditInfo auditInfo,
			List<SqlLogEntry> sqlLogEntries, Map<String, SqlLogEntry> entityToSqlMap, int maxSqlLength, int columnGuid,
			int columnWriteDate, int columnWriteTime, int columnSqlStatement1, int columnSqlStatement2,
			int columnBeforeImage, int columnAfterImage, int columnSqlCode, int columnFunctionCount) throws Exception {

		// 使用 auditInfo 中已設定的日期時間，而不是重新產生
		final String currentDate = auditInfo.getWriteDate();
		final String currentTime = auditInfo.getWriteTime();

		for (SqlLogEntry sqlLogEntry : sqlLogEntries) {
			// 跳過審計表相關的操作
			if (sqlLogEntry.getSql().toUpperCase().contains(EntityConstants.TABLE_LOG_MASTER)
					|| sqlLogEntry.getSql().toUpperCase().contains(EntityConstants.TABLE_LOG_DETAIL)) {
				continue;
			}
			// 設置基本信息
			preparedStatement.setString(columnGuid, auditInfo.getGuid());
			preparedStatement.setString(columnWriteDate, currentDate);
			preparedStatement.setString(columnWriteTime, currentTime);

			String sql = sqlLogEntry.getSql();
			int sqlByteLength = sql.getBytes(StandardCharsets.UTF_8).length;
			String sqlStatement1;
			String sqlStatement2 = null;

			if (sqlByteLength <= maxSqlLength) {
				sqlStatement1 = sql;
			} else {
				sqlStatement1 = StringHelper.truncateBytesWithoutEllipsis(sql, maxSqlLength);
				int splitIndex = sqlStatement1.length();
				String remaining = sql.substring(splitIndex);

				if (remaining.getBytes(StandardCharsets.UTF_8).length > maxSqlLength) {
					sqlStatement2 = StringHelper.truncateBytesWithEllipsis(remaining, maxSqlLength);
				} else {
					sqlStatement2 = remaining;
				}
			}

			preparedStatement.setString(columnSqlStatement1, sqlStatement1);
			preparedStatement.setString(columnSqlStatement2, sqlStatement2);

			// 處理 BEFORE_IMAGE 和 AFTER_IMAGE
			String beforeImageStr = null;
			String afterImageStr = null;

			// 只有當這個 SQL 操作的實體與實體變更匹配時，才使用變更信息
			if (hasRegisteredEntity(sqlLogEntry)) {
				String targetEntity = sqlLogEntry.getTargetEntity();
				SqlLogEntry.SqlType sqlType = sqlLogEntry.getSqlType();

				// 匹配 Image:
				// 1. TEMP 表 INSERT → 使用 tempImage (記錄到 AFTER_IMAGE)
				// 2. TEMP 表 DELETE → 使用 tempImage (記錄到 BEFORE_IMAGE，用於覆核操作)
				// 3. MST/MASTER 表 → 使用 beforeImage/afterImage (主檔資料)
				try {
					boolean isTempOperation = targetEntity != null && targetEntity.toUpperCase().contains(TABLE_TEMP);

					if (isTempOperation && auditInfo.getTempImage() != null) {
						// TEMP 表操作：使用 tempImage
						if (sqlType == SqlLogEntry.SqlType.INSERT) {
							// TEMP 表新增：記錄到 AFTER_IMAGE
							afterImageStr = AuditFormatter.formatEntity(auditInfo.getTempImage());
						} else if (sqlType == SqlLogEntry.SqlType.DELETE) {
							// TEMP 表刪除（覆核操作）：記錄到 BEFORE_IMAGE
							beforeImageStr = AuditFormatter.formatEntity(auditInfo.getTempImage());
						}
					} else {
						// MST/MASTER 表操作：使用 beforeImage/afterImage
						if (auditInfo.getBeforeImage() != null
								&& (sqlType == SqlLogEntry.SqlType.UPDATE || sqlType == SqlLogEntry.SqlType.DELETE)) {
							beforeImageStr = AuditFormatter.formatEntity(auditInfo.getBeforeImage());
						}
						if (auditInfo.getAfterImage() != null
								&& (sqlType == SqlLogEntry.SqlType.UPDATE || sqlType == SqlLogEntry.SqlType.INSERT)) {
							afterImageStr = AuditFormatter.formatEntity(auditInfo.getAfterImage());
						}
					}
				} catch (Exception e) {
					LogUtils.warn("無法獲取實體 {} 的 Image: {}", targetEntity, e.getMessage());
				}
			}

			// 截斷超過 maxSqlLength 的 beforeImageStr
			if (beforeImageStr != null && beforeImageStr.getBytes(StandardCharsets.UTF_8).length > maxSqlLength) {
				LogUtils.warn("BEFORE_IMAGE 內容超過 {} 字節，將被截斷。GUID: {}, 實體: {}, 原字節長度: {}", maxSqlLength,
						auditInfo.getGuid(), sqlLogEntry.getTargetEntity(),
						beforeImageStr.getBytes(StandardCharsets.UTF_8).length);
				beforeImageStr = StringHelper.truncateBytesWithEllipsis(beforeImageStr, maxSqlLength);
			}

			// 先設置 BEFORE_IMAGE
			preparedStatement.setString(columnBeforeImage, beforeImageStr);

			// 設置 AFTER_IMAGE
			String afterImage = null;
			SqlLogEntry.SqlType sqlType = sqlLogEntry.getSqlType();
			if (sqlType == SqlLogEntry.SqlType.INSERT) {
				afterImage = StringUtils.isNotBlank(afterImageStr) ? afterImageStr : sqlLogEntry.getParameters();
			} else if (sqlType == SqlLogEntry.SqlType.DELETE) {
				afterImage = null;
			} else if (sqlType == SqlLogEntry.SqlType.UPDATE) {
				afterImage = StringUtils.isNotBlank(afterImageStr) ? afterImageStr : auditInfo.getUpdateSqlInfo();
			} else { // SELECT or OTHER
				afterImage = null;
			}

			// 截斷超過 maxSqlLength 的 afterImage
			if (afterImage != null && afterImage.getBytes(StandardCharsets.UTF_8).length > maxSqlLength) {
				LogUtils.warn("AFTER_IMAGE 內容超過 {} 字節，將被截斷。GUID: {}, 實體: {}, 原字節長度: {}", maxSqlLength,
						auditInfo.getGuid(), sqlLogEntry.getTargetEntity(),
						afterImage.getBytes(StandardCharsets.UTF_8).length);
				afterImage = StringHelper.truncateBytesWithEllipsis(afterImage, maxSqlLength);
			}

			preparedStatement.setString(columnAfterImage, afterImage);

			// 設置 SQL_CODE 和 QUERY_COUNT
			preparedStatement.setString(columnSqlCode, QUERY_CHANGE_COUNT);

			// 設置 QUERY_COUNT (如果是查詢操作)
			String functionCount = QUERY_CHANGE_COUNT;
			// 從 auditInfo 中獲取查詢計數
			if (sqlType == SqlLogEntry.SqlType.SELECT) {
				functionCount = String.valueOf(auditInfo.getFunctionCount());
			}
			preparedStatement.setString(columnFunctionCount, functionCount);

			preparedStatement.addBatch();
		}
	}

	// 是否為註冊的實體
	private boolean hasRegisteredEntity(SqlLogEntry sqlLogEntry) {
		if (sqlLogEntry.getTargetEntity() == null) {
			return false;
		}

		// 檢查是否是審計表操作
		String sql = sqlLogEntry.getSql().toUpperCase();
		return !sql.contains(EntityConstants.TABLE_LOG_MASTER) && !sql.contains(EntityConstants.TABLE_LOG_DETAIL);
	}

	// 取得主機IP
	private String getLocalHostAddress() {
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			return localHost.getHostAddress();
		} catch (UnknownHostException e) {
			LogUtils.error("無法獲取本機IP地址", e);
			return "unknown";
		}
	}

	/**
	 * 獲取客戶端真實IP地址
	 */
	private String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");

		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}

		// 如果是多級代理，取第一個IP地址
		if (ip != null && ip.contains(",")) {
			ip = ip.split(",")[0].trim();
		}

		return ip;
	}

}
