package com.portfolio.constants;

import java.util.Arrays;
import java.util.List;

/**
 * 系統級別常量，包含基本配置、錯誤訊息、通用狀態等
 */
public class SystemConstants {
    // 系統代號
    public static final String 	SYSTEM_ID = "portal",
    							HOSTNAME_TXN1 = "TXN1",
    							HOSTNAME_TXN2 = "TXN2";

    // 全域參數
    public static final String 	DEFAULT_ERROR_VIEW = "error",
                              	UNKNOWN_EXCEPTION = "Unknown Exception",
                              	DOT_STRING = "...";

    // 使用者資訊
    public static final String  IV_CREDS = "iv-creds",
    							PERMISSION_BEAN = "permissionBean",
          						PERMISSION_USER_ID = "user_id",
  								PERMISSION_USER_CN = "user_cn",
  								PERMISSION_USER_EMAIL = "user_email",
  								PERMISSION_DEPT_ID = "user_deptid",
  								PERMISSION_DEPT_NAME = "user_deptname",
  								PERMISSION_TEL_NUMBER = "user_telNumber",
  		  						PERMISSION_SYSTEM_ID = "NCCC_AP039",
  								SYSTEM_ID_STR = "AP039";

    // 分頁參數
    public static final String 	PAGINATION_NUMBER_0 = "0",
                              	PAGINATION_NUMBER_10 = "10",
                              	PAGINATION_PAGE = "page",
                              	PAGINATION_SIZE = "size",
                              	PAGINATION_CONTENT = "content",
                              	PAGINATION_NUMBER = "number",
                              	PAGINATION_TOTALELEMENTS = "totalElements",
                              	PAGINATION_TOTALPAGES = "totalPages";

    // 通用狀態
    public static final String 	STRING_Y = "Y",
                              	STRING_N = "N",
                              	STRING_NEW_LINE = "\n";

    // 覆核狀態: 未覆核/已覆核
    public static final String 	APPROVED_STATUS_W = "W", // 未覆核
                              	APPROVED_STATUS_Y = "Y"; // 已覆核

    // 稱謂
    public static final String 	TITLE_MAN = "M", // 先生
                              	TITLE_LADY = "F"; // 小姐

    // License生成參數
    public static final String 	SALT_PARAM = "NcccTdep", // 加鹽參數
                              	AES_MODE_CBC = "AES/CBC/PKCS5Padding"; // AES_CBC演算法

    // 異動類型
    public static final String 	ACT_TYPE_Q = "Q", // 查詢
                              	ACT_TYPE_A = "A", // 新增
                              	ACT_TYPE_U = "U", // 異動
                              	ACT_TYPE_D = "D", // 刪除
                              	ACT_TYPE_C = "C", // 覆核
  								ACT_TYPE_O = "O"; // 匯出下載

    // SQL 類型常量
    public static final String 	INSERT_PREFIX = "INSERT",
    							UPDATE_PREFIX = "UPDATE",
    							DELETE_PREFIX = "DELETE",
    							SELECT_PREFIX = "SELECT";

    // 前端回傳定義
    public static final String 	JSON_RETURN_STR_VALUE = "value",
    							JSON_RETURN_STR_LABEL = "label";

    // 敏感數據參數名稱常數
    public static final List<String> SENSITIVE_PARAM_NAMES = Arrays.asList(
        "cardNo", "idNo", "password", "bankAccount", "phone", "email", "address"
    );

    // 系統參數
    public static final int MAX_QUERY_PARAM_LENGTH = 2000, 	// 查詢參數最大長度常數
    						GEN_ID_MAX_RETRY = 3, 			// 產生PK可配置參數
    						READ_COMMITTED_TIMEOUT = 5,		// 產生PK重試次數
    						AUDIT_URL_TRUNCATE = 5;

    // Log 相關參數
    public static final int 	SINGLE_RECORD_COUNT = 1; // 單筆查詢紀錄筆數
    public static final String 	USER_ID_KEY = "userId",
    							USER_NAME_KEY = "userName",
    							REQUEST_ID_KEY = "requestId",
    	    					PATH = "path";

    /**
     * VARCHAR2 最大長度
     * 使用 3996 而非 4000 作為安全值，避免因字元編碼轉換（如 UTF-8 多位元組字元）
     * 或 JDBC/Hibernate 內部處理時可能產生的額外開銷導致實際長度超出限制
     * 同時保留3個字元，用以適時添加 ... 以表示資料長度超出上限
     */
    public static final String MAX_VARCHAR2_LENGTH = "3996";

    /**
     * 事件類型常數
     */
    public static final String EVENT_TYPE_EXCEPTION = "EXCEPTION";

    /**
     * 事件代碼常數
     */
    // 不需要處理
    public static final String EVENT_CODE_SYSTEM_WARN_000 = "000";
    // 需要處理
    public static final String EVENT_CODE_SYSTEM_ERROR_900 = "900";
    public static final String EVENT_CODE_SYSTEM_ERROR_999 = "999"; // 未定義錯誤

    // 識別碼長度
	public static final int KEY_REF_MAX_LENGTH = 13, // JWE 金鑰識別碼 (從公鑰雜湊取得) 長度
							MAX_CERT_REF_LENGTH = 13, // 憑證識別碼 (從公鑰雜湊取得) 長度
							MAX_JWE_CERT_LIC_REF_LENGTH = 16; // 資料庫限制最大長度

    /**
     * 警示常數
     */
    public static final String 	ALERT_EVENT_TYPE = "PORTAL",
    							ALERT_LEVEL_WARN = "WARN",
    							ALERT_LEVEL_ERROR = "ERROR";

    // 系統參數相關
    public static final String 	LOOKUP_TYPE_TXNIP = "TXNIP",
								LOOKUP_CODE_TXN1 = "TXN1",
								LOOKUP_CODE_TXN2 = "TXN2",
								DEFAULT_TXN1_NAME = "TXN1",
								DEFAULT_TXN2_NAME = "TXN2";

    // HSM 加解密相關
    public static final String 	LOOKUP_TYPE_SECURITY = "SECURITY",
    							LOOKUP_CODE_MASTER_KEY = "MASTER_KEY";

}

