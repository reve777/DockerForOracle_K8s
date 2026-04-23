package com.portfolio.constants;

public class MessageConstants {

    // 通用訊息
    public static final String 	USER_OPERATE_SUCCESS = "操作成功"; // 新增/修改/刪除...等

    // 系統操作錯誤訊息
    public static final String DATE_FORMAT_ERROR_RANGE = "服務生效日期(起) 不可晚於服務生效(迄) 日期",
    						  BANK_CODE_FORMAT_ERROR = "機構代碼只能包含英文大小寫、數字、減號、底線",
                              BANK_NAME_LENGTH_ERROR = "機構名稱最多40個字元",
                              SEARCH_STATUS_ERROR = "狀態選擇錯誤",
                              BANK_MST_NOT_FOUND_ERROR = "此筆機構主檔不存在,",
                              BANK_TEMP_NOT_FOUND_ERROR = "此筆機構暫存檔不存在,",
                              BANK_SELECT_NOT_FOUND_ERROR = "請選擇正確的機構代號",
                              CERT_NOT_FOUND_ERROR = "此筆機構憑證主檔不存在,",
                              CERT_TEMP_NOT_FOUND_ERROR = "此筆機構憑證暫存檔不存在,",
                              BANK_ALREADY_EXISTS_ERROR = "機構代碼重複 : ",
                              NOTIFY_GROUP_ALREADY_EXISTS_ERROR = "群組名稱重複 : ",
                              BANK_CONTACT_USER_ERROR = "聯繫窗口不可為空",
                              BANK_PARTICIPATION_MODE_ERROR = "參加模式不可為空",
                              START_DATE_ERROR = "生效日期不可為空",
                              NOTIFY_GROUP_NOT_EXISTS= "指定的群組不存在或未啟用",
                              JWE_KEY_NOT_EXISTS= "機構JWE公鑰識別碼不存在或未啟用",
                              JWE_KEY_TYPE_NOT_EXISTS= "金鑰類別不存在",
                              JWE_KEY_REF_SEARCH_INPUT_ERROR = "金鑰識別碼長度最多16碼",
                              CERT_REF_SEARCH_INPUT_ERROR = "憑證識別碼長度最多16碼",
                              LIC_REF_SEARCH_INPUT_ERROR = "License識別碼長度最多16碼",
                              JWE_KEY_REF_FORMAT_ERROR = "金鑰識別碼僅允許英文大小寫、數字及底線",
                              CERT_ID_NOT_EXISTS= "機構簽發客戶端憑證識別碼不存在或未啟用",
                              OVERDUE_ONLY_ERROR = "請確認是否僅顯示逾期項目",
                              CSR_NOT_EXISTS= "該筆CSR不存在",
                              COMMON_TYPE_NOT_EXISTS = "異動類型不存在",
                              NOTIFY_GROUP_NOT_FOUND_ERROR = "此筆群組設定主檔不存在",
                              NOTIFY_GROUP_TEMP_NOT_FOUND_ERROR = "此筆群組設定暫存檔不存在",
                              NOTIFY_GROUP_BANK_IN_USE_ERROR = "此通知群組正被機構使用中，無法刪除",
                              NOTIFY_GROUP_MEMBER_IN_USE_ERROR = "此群組中尚在使用中，不可刪除",
                              NOTIFY_MEMBER_NOT_FOUND_ERROR = "此筆群組人員主檔不存在",
                              NOTIFY_MEMBER_TEMP_NOT_FOUND_ERROR = "此筆群組人員暫存檔不存在",
                          	  ACQUIRER_CODE_INPUT_ERROR = "機構代碼只能包含英文大小寫、數字、減號、底線",
                          	  ACQUIRER_MST_NOT_FOUND_ERROR = "此筆收單機構主檔不存在",
                          	  ACQUIRER_TEMP_NOT_FOUND_ERROR = "此筆收單機構暫存檔不存在",
                          	  ACQUIRER_ALREADY_EXISTS_ERROR = "收單機構代碼重複 : ",
                          	  LICENSE_DATE_FORMAT_ERROR = "請確保日期是現在或未來的時間（不能是過去的時間）",
                          	  LICENSE_MST_NOT_FOUND_ERROR = "此筆License主檔不存在",
                          	  LICENSE_TEMP_NOT_FOUND_ERROR = "此筆License暫存檔不存在",
                          	  JWE_KEY_MST_NOT_FOUND_ERROR = "此JweKey平台識別值主檔不存在",
                          	  BANK_DELETE_ERROR="無法刪除，機構代碼已關聯至 License, 機構代碼 :",
                          	  TEMP_ALREADY_EXISTS_ERROR = "該筆資料已有待覆核的暫存檔，無法重複建立",
                          	  CERT_FILE_EMPTY_ERROR = "憑證檔案不能為空",
                              CSR_FILE_EMPTY_ERROR = "CSR檔案不能為空",
                              CERT_GEN_CSER_ERROR = "產生CSR失敗",
                              CERT_CSR_MISMATCH_ERROR =  "上傳的憑證與我們產生的CSR不匹配，請確認您上傳的是正確的憑證",
                              HSM_ENCRYPTION_ERROR = "HSM加密失敗",
                              HSM_DECRYPTION_ERROR = "HSM解密失敗",
                              HSM_API_CONNECT_ERROR = "HSM API 調用失敗",
                              CERT_FILE_READ_ERROR = "獲取憑證內容時發生錯誤",
                              INVALID_ACTION_TYPE = "無效的異動類型",
                              CERT_TEMP_NOT_FOUND_BY_PK = "找不到憑證暫存檔",
                              CERT_RECORD_NOT_FOUND = "找不到憑證記錄",
                              CERT_CSR_EMPTY = "憑證CSR內容為空",
                              CERT_CONTENT_EMPTY = "憑證內容為空",
                              CERT_EXTENSION_ERROR = "僅接受副檔名為%s的檔案",
                              JWE_UPLOAD_PUB_KEY_INVALID_ERROR = "上傳的公鑰檔案內容不符合有效的格式",
                              JWE_UPLOAD_RSA_KEY_SIZE_ERROR = "上傳的 RSA 公鑰長度不正確，僅接受 4096 位元金鑰",
                              // JWE 相關
                          	  JWEKEY_SEARCH_INPUT_ERROR_1 = "機構代碼只能包含英文大小寫、數字、減號、底線",
                          	  JWEKEY_SEARCH_INPUT_ERROR_2 = "機構名稱長度超過43個字",
                          	  JWEKEY_NOT_FOUND_ERROR = "查無此筆JWE金鑰資料",
                          	  JWEKEY_FILE_EMPTY_ERROR = "金鑰檔案不能為空",
                              // 交易日誌 相關訊息
                    		  TXN_SEARCH_PLAT_TXID_ERROR = "平台交易序號長度太長",
                              TXN_SEARCH_BANK_ID_ERROR = "機構代碼長度不可超過10",
                              TXN_SEARCH_AMOUNT_ERROR = "交易金額(起)不可大於交易金額(迄)",
                              TXN_SEARCH_TX_LOG_TYPE_ERROR = "交易類別不正確",
                              TXN_SEARCH_RTN_CODE_ERROR = "交易結果碼長度不可超過5",
                              TXN_SEARCH_PLAT_REJ_CODE_ERROR = "平台拒絕碼長度不可超過5",
                              TXN_SEARCH_DATE_ERROR = "平台交易時間(起)不可大於平台交易時間(迄)",
                              TXN_SEARCH_MERCHANT_ID_ERROR = "特店代號長度不可超過15",
                              TXN_NOT_FOUND_ERROR = "找不到指定的交易日誌記錄",
                              TXN_NOT_EXISTS = "交易日誌不存在",
                              //
                              SEARCH_DATE_FORMAT_ERROR = "日期格式錯誤",
                              COMMON_EMAIL_FORMAT_ERROR = "請輸入有效的電子郵件格式";

    // 憑證相關
    public static final String 	CERT_FORMAT_ERROR = "無效的憑證格式: ",
    							PROCESSING_ERROR = "憑證處理失敗: ",
    							CERT_FILE_EMPTY = "憑證檔案為空",
    							CERT_INVALID_FORMAT = "無效的憑證格式",
    							CERT_PEM_PARSE_FAILED = "PEM格式解析失敗",
    							CERT_VALIDITY_ERROR = "憑證已過期或尚未生效",
    							CERT_PROCESS_ERROR = "上傳憑證失敗或發生錯誤",
    							CERT_SERIAL_EXTRACT_FAILED = "無法從憑證提取序號",
    							CERT_REF_EXTRACT_FAILED = "從憑證擷取識別碼時發生錯誤",
    							CERT_UPLOAD_FILE_TOO_SHORT = "憑證檔案似乎不完整或已損壞。請確保上傳的是完整有效的憑證檔案。",
    							CERT_UPLOAD_INVALID_FORMAT = "憑證格式不正確。請確保檔案是有效的X.509憑證格式。",
    							CERT_UPLOAD_GENERAL_ERROR = "憑證處理失敗。請確認您上傳的是有效的憑證檔案",
    							CERT_UPLOAD_TOO_SHORT =	"Too short",
    							CERT_UPLOAD_TOO_FORMAT = "format",
    	    					JWE_PUB_KEY_TYPE_CHANGE_ERROR = "金鑰類型不可修改",
    							JWE_UPLOAD_FORMAT_ERROR = "無效的 PEM 公鑰格式，請確認上傳的檔案格式正確",
    							JWE_UPLOAD_PUB_KEY_SIZE_ERROR = "公鑰檔案大小超過限制",
    							JWE_UPLOAD_PUB_KEY_ERROR = "PEM 公鑰解析失敗，請確認上傳的檔案格式正確",
    							JWE_UPLOAD_PUB_KEY_EMPTY_ERROR = "上傳的公鑰檔案不可為空";

    /**
     * 日誌描述常數
     */
    public static final String 	LOG_DESC_DATA_NOT_FOUND = "資料不存在",
							    LOG_DESC_VALIDATION_FAILED = "資料驗證失敗",
							    LOG_DESC_AJAX_REQUEST_FAILED = "AJAX請求處理失敗",
							    LOG_DESC_REQUEST_PARAM_FAILED = "請求參數驗證失敗",
							    LOG_DESC_FORM_BINDING_FAILED = "表單綁定失敗",
							    LOG_DESC_ILLEGAL_ARGUMENT = "參數類型不匹配或非法參數",
							    LOG_DESC_DATA_CONSTRAINT_VIOLATION = "資料約束違規",
							    LOG_DESC_TRANSACTION_FAILED = "資料庫事務處理失敗",
							    LOG_DESC_CONSTRAINT_VIOLATION = "數據約束違規",
							    LOG_DESC_DATA_INTEGRITY_VIOLATION = "數據完整性違規",
							    LOG_DESC_SQL_ERROR = "SQL或資料表格式錯誤",
							    LOG_DESC_UNAUTHORIZED = "權限不足",
							    LOG_DESC_FILE_DOWNLOAD_FAILED = "檔案下載失敗",
							    LOG_DESC_SYSTEM_ERROR = "系統異常",
							    LOG_DESC_ILLEGAL_STATE = "系統狀態異常";

    // 回傳訊息
    public static final String 	SYSTEM_ERROR_MSG = "作業失敗，原因「作業發生異常失敗」",
                              	SYSTEM_ERROR_MSG_NAME = "systemErrorMsg",
                              	NO_PERMISSION_ERROR_MSG = "您無此功能權限。",
                              	VALIDATE_ERROR = "欄位檢核錯誤: ",
                              	DATA_NOT_FOUND_ERROR = "找不到此筆資料",
                              	COMMON_SEARCH_ERROR = "查無此資料，請確認後再執行操作",
                              	NO_DATA_CHANGED_ERROR =	"資料未異動，無需儲存",
                              	REQUEST_HEADER_AUTH_ERROR = "登入時間已逾時，請重新登入",
                              	NO_MORE_DATA_NAME = "noMoreDataMsg",
                              	NO_MORE_DATA_MSG = "已經沒有更多資料了",
                              	NO_DATA_FOUND_MSG = "查無資料",
                              	UPDATE_CERT_ERROR_MSG = "更新憑證內容時發生錯誤",
                                NOT_FOUND_TXN1_MSG = "找不到 TXN1 狀態記錄",
                              	NOT_FOUND_TXN2_MSG = "找不到 TXN2 狀態記錄";

    // 檢核判斷參數
    public static final String 	TYPE_MISMATCH = "typeMismatch",  // 型別轉換錯誤
                              	COLUMN_FORMAT_ERROR = "欄位格式錯誤";


    // 系統使用訊息
    public static final String 	TOOLS_ERROR_STRING = "工具類不應該被實例化",
    							UNKNOWN_STRING = "Unknown",
    							GEN_ID_ERROR_STRING = "無法生成唯一成員ID",
    							OBJECT_CONVERT_ERROR = "物件轉換失敗";


}
