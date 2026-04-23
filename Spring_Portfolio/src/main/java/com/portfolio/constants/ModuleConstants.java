package com.portfolio.constants;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 模組相關常量，包含模組定義、URL路徑、權限前綴等
 */
public class ModuleConstants {

    // 各種管理模組的字串(畫面左側的功能清單)
    public static final String	
    							MODULE_INDEX		 		= "index",		   				// 首頁
                              	HOME_PAGE    				= "page",
                              	MODULE_CLASSIFY 			="classify";

    /*
     * 放入所有功能
     * 順序不可隨意變動，會影響權限讀取結果!
     */
    public static final List<String> FUNCTION_LIST = Arrays.asList(
    		HOME_PAGE,
    		MODULE_CLASSIFY
    );

    // function name
    public static final String 	FUNC_B			= "/b",
                              	FUNC_REMINDER 		= "/reminder";
    // 路徑模式後綴
    public static final String PATH_PATTERN_SUFFIX = "/**";

    // action 前綴（用於權限判斷與 bitIndex）
    public static final String 	PREFIX_MAIN        = "main",
                              	PREFIX_GET         = "get",
                              	PREFIX_ADD         = "add",
                              	PREFIX_DELETE      = "delete",
                              	PREFIX_UPDATE      = "update",
                              	PREFIX_UPLOAD 	   = "upload",
                              	PREFIX_DOWNLOAD    = "download",
                              	PREFIX_CONFIRM     = "confirm",
                      			PREFIX_CONFIRM_RECORD_QUERY = "confirmRecordQuery",
                      					classify	="classify";

    // urls
    public static final String URL_INDEX = "/index",
                              URL_MAIN = "/main",
                              URL_GET_MAIN = "/getMain",
                              URL_ADD = "/add",
                              URL_ERROR = "/errorPage",
                              URL_UNCONFIRMED = "/confirm",
                              URL_UPDATE = "/update",
                              URL_DELETE = "/delete";

    // AJAX
    public static final String API = "/api",
            				   API_GET_TITLE = "/getTitle";

    // 權限錯誤回傳JSON路徑
    public static final List<String> JSON_ERROR_LIST = Arrays.asList(
            URL_ADD, "/update", "/delete", "/confirm", "/getBankDetail", "/unconfirmedDetail",
            API
    );

    // fragement
    public static final String TEMPLATE_NAME = "templateName";


    // titles
    public static final String 	TITLE = "title",
                              		TITLE__LOG_MAIN = "交易日誌";

    // 機構管理_連線方式
    public static final String 	CONN_MTHD_INTERNET = "I", // Internet
                              	CONN_MTHD_PURPOSE = "P"; // 專線

    // 機構管理_參加模式
    public static final String 	PARTICIPATION_MODE_1 = "1", // 參加模式1
                              	PARTICIPATION_MODE_2 = "2", // 參加模式2
                              	PARTICIPATION_MODE_3 = "3"; // 參加模式3

    // HTML使用
    public static final String 	HTML_COLUMN_ID = "id", // 指定id
    							HTML_APPROVE_DETAIL	= "approveDetail", // 覆核明細
    							HTML_SEARCH_STATUS = "validStatus", // 狀態搜尋下拉
    							HTML_UNCONFIRMED_COUNT = "count"; // 待辦事項待覆核筆數

    // 加密相關
    public static final int 	RSA_KEY_SIZE = 4096;
    public static final String 	X509_TYPE = "X.509",
								ALGORITHM_AES = "AES",
    							ALGORITHM_RSA = "RSA",
    							ALGORITHM_SHA256 = "SHA-256",
    							PUBLIC_KEY_BEGIN_NEW_LINE = "-----BEGIN PUBLIC KEY-----\n",
    							PUBLIC_KEY_END_NEW_LINE = "\n-----END PUBLIC KEY-----",
    							PUBLIC_KEY_BEGIN = "-----BEGIN PUBLIC KEY-----",
    							PUBLIC_KEY_END = "-----END PUBLIC KEY-----",
    							CERT_REF_PREFIX = "CERT-",
    							CERT_CN = "CN",
    							CERT_BEGIN_MARKER = "-----BEGIN CERTIFICATE-----",
    							CERT_END_MARKER = "-----END CERTIFICATE-----",
    							CERT_BEGIN_REQUEST_MARKER ="-----BEGIN CERTIFICATE REQUEST-----",
    	    					CERT_END_REQUEST_MARKER ="-----END CERTIFICATE REQUEST-----",
    							CERT_CN_PREFIX = "CN=",
    							CERT_CN_PREFIX_LOWER = "cn=",
    							CERT_REF_SN = "SN",
    							CERT_REF_T = "T",
    						    MODULUS_PREFIX = "Modulus=",
    						    FILE_EXTENSION_CSR = "csr",
    	    					FILE_EXTENSION_CER = "cer";

    // 日期相關
    public static final String 	  DATE_FORMAT_PATTERN = "yyyy/MM/dd",
    							  DATE_TIME_FORMAT_PATTERN = "yyyy/MM/dd HH:mm:ss";
    public static final LocalDate MAX_END_DATE = LocalDate.of(9999, 12, 31);

    
    // 交易日誌交易類別
    public static final String 	TXN_LOG_TYPE_A = "AAA",
    							TXN_LOG_TYPE_C = "CCC",
    							TXN_LOG_TYPE_R = "RRR"; 

    
    // 新增路徑標題映射常量
    private static final Map<String, String> PATH_TO_TITLE_MAP = new LinkedHashMap<>();

    static {
        // 初始化路徑到標題的映射
        // 根路徑特殊處理 + 待辦事項
        PATH_TO_TITLE_MAP.put("/portfolio", "A");
        PATH_TO_TITLE_MAP.put("/portfolio/", "B");
    }


    // 返回不可修改的映射，防止外部修改
    public static Map<String, String> getPathToTitleMap() {
        return Collections.unmodifiableMap(PATH_TO_TITLE_MAP);
    }

    // 通過路徑獲取標題的便捷方法
    public static String getTitleByPath(String path) {
        return PATH_TO_TITLE_MAP.getOrDefault(path, "TITLE_DEFAULT");
    }

}
