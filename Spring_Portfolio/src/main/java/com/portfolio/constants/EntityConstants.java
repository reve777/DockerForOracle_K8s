package com.portfolio.constants;

/**
 * 實體與數據表相關常量
 */
public class EntityConstants {

    /**
     * 資料表名稱
     */
    // 審計日誌相關
    public static final String TABLE_LOG_MASTER = "LOG_MASTER";
    public static final String TABLE_LOG_DETAIL = "LOG_DETAIL";

    // 機構相關
    public static final String TABLE_BANK_MST = "BANK_MST";
    public static final String TABLE_BANK_TEMP = "BANK_TEMP";

    // 憑證相關
    public static final String TABLE_CERTIFICATE_MST = "CERTIFICATE_MST";
    public static final String TABLE_CERTIFICATE_TEMP = "CERTIFICATE_TEMP";

    // JWE 密鑰相關
    public static final String TABLE_JWE_KEY_MST = "JWE_KEY_MST";
    public static final String TABLE_JWE_KEY_TEMP = "JWE_KEY_TEMP";

    // 通知群組相關
    public static final String TABLE_NOTIFY_GROUP = "NOTIFY_GROUP";
    public static final String TABLE_NOTIFY_GROUP_TEMP = "NOTIFY_GROUP_TEMP";

    // 收單機構相關
    public static final String TABLE_ACQUIRER_MST = "ACQUIRER_MST";
    public static final String TABLE_ACQUIRER_TEMP = "ACQUIRER_TEMP";

    // License相關
    public static final String TABLE_LICENSE_MST = "LICENSE_MST";
    public static final String TABLE_LICENSE_TEMP = "LICENSE_TEMP";

    // 通知人員相關
    public static final String TABLE_NOTIFY_MEMBER = "NOTIFY_MEMBER";
    public static final String TABLE_NOTIFY_MEMBER_TEMP = "NOTIFY_MEMBER_TEMP";
    
    // 系統監控_TXN主機狀態
    public static final String TABLE_TXN_STATUS = "TXN_STATUS";

    /**
     * 實體名稱
     */
    // 機構相關
    public static final String ENTITY_BANK_MST = "BankMst";
    public static final String ENTITY_BANK_TEMP = "BankTemp";

    // 收單機構相關
    public static final String ENTITY_ACQUIRER_MST	="AcquirerMst";
    public static final String ENTITY_ACQUIRER_TEMP	="AcquirerTemp";

    // License相關
    public static final String ENTITY_LICENSE_MST	="LicenseMst";
    public static final String ENTITY_LICENSE_TEMP	="LicenseTemp";

    // 憑證相關
    public static final String ENTITY_CERTIFICATE_MST = "CertificateMst";
    public static final String ENTITY_CERTIFICATE_TEMP = "CertificateTemp";

    // JWE 金鑰相關
    public static final String ENTITY_JWE_KEY_MST = "JweKeyMst";
    public static final String ENTITY_JWE_KEY_TEMP = "JweKeyTemp";
	public static final String ENTITY_JWE_FILE= "jweFile";

    // 通知群組相關
    public static final String ENTITY_NOTIFY_GROUP = "NotifyGroup";
    public static final String ENTITY_NOTIFY_GROUP_TEMP = "NotifyGroupTemp";

    // 通知人員
	public static final String ENTITY_NOTIFY_MEMBER = "NotifyMember";
	public static final String ENTITY_NOTIFY_MEMBER_TEMP = "NotifyMemberTemp";

    // 交易日誌
	public static final String ENTITY_TDEP_TXN_LOG = "TdepTxnLog";

    // 系統監控_TXN主機狀態
	public static final String ENTITY_TXN_STATUS = "TxnStatus";
	
	// 自定義區
	public static final String 	TRANSIENT_GROUP_NAME = "groupName", // 群組名稱
								TRANSIENT_KEY_TYPE_NAME = "keyTypeName"; // 金鑰類型名稱

    /**
     * TABLE欄位
     */
    public static final String 	TABLE_COLUMN_BANK_ID = "bankId",
    							TABLE_COLUMN_BANK_CODE = "bankCode", // 機構代碼
    							TABLE_COLUMN_BANK_NAME = "bankName", // 機構名稱
    	    					TABLE_COLUMN_PARTITION_MODE = "partitionMode", // 參加模式
    	    					TABLE_COLUMN_CONN_MTHD = "connMthd", // 連線方式
    	    					TABLE_COLUMN_JWE_ENABLE = "jweEnable", // JWE啟用
    							TABLE_COLUMN_STATUS = "status", // 狀態
    	    					TABLE_COLUMN_TYPE = "type", // 異動類型
    							TABLE_COLUMN_MODIFY_DATE = "modifyDate", // 修改時間
    	    					TABLE_COLUMN_MODIFY_USER = "modifyUser", // 修改人員
    							TABLE_COLUMN_CONFIRM_DATE = "confirmDate", // 覆核時間
    	    					TABLE_COLUMN_CONFIRM_USER = "confirmUser", // 覆核人員
    							TABLE_COLUMN_GROUP_ID = "groupId", // 群組ID
    							TABLE_COLUMN_MEM_ID = "memId", // 人員ID
    							TABLE_COLUMN_ACQ_ID = "acqId", // 機構平台識別碼
    	    					TABLE_COLUMN_ACQ_CODE = "acqCode", // 收單機構代碼
    							TABLE_COLUMN_ACQ_NAME = "acqName", // 收單機構名稱
    							TABLE_COLUMN_LICENSE_ID = "licenseId", // 平台識別值
    							TABLE_COLUMN_LICENSE_REF = "licenseRef", // License識別碼
    							TABLE_COLUMN_CERT_ID = "certId", // 機構簽發憑證 平台識別碼
    	    					TABLE_COLUMN_EXPIRE_DATE = "expireDate", // 有效期限
    							TABLE_COLUMN_CERT_REF = "certRef",
    							TABLE_COLUMN_KEY_ID = "keyId", // JWE金鑰ID
								TABLE_COLUMN_KEY_REF = "keyRef", // 金鑰識別碼
								TABLE_COLUMN_KEY_TYPE = "keyType", // 公鑰類別 ORG:機構公鑰，SELF:平台公鑰
    							TABLE_COLUMN_PLAT_TXID = "platTxid", // 交易平台序號
    							TABLE_COLUMN_RTN_CODE = "rtnCode", // 交易結果碼
    							TABLE_COLUMN_PLAT_REJ_CODE = "platRejCode", // 平台拒絕碼
    	    					TABLE_COLUMN_AMOUNT = "amount", // 交易金額(沖銷時為空)
    	    					TABLE_COLUMN_TX_LOG_TYPE = "txnLogType", // 交易類型，A:授權(Auth)、C:取消(Cancel)、R:沖銷(Reversal)
    	    	    			TABLE_COLUMN_PLAT_TX_DATE = "platTxdate", // 平台交易時間
    	    	    			TABLE_COLUMN_HOST_NAME = "hostName", // 主機名稱
    	    	    			TABLE_COLUMN_LOOKUP_TYPE = "lookup_type", // 參數類型
    	    	    			TABLE_COLUMN_LOOKUP_CODE = "lookup_code", // 參數代碼
    	    	    	    	TABLE_COLUMN_MERCHANT_ID = "merchantId"; // 特店代號

    /**
     * 欄位長度限制
     */
    public static final int 	TABLE_COLUMN_PLAT_TXID_LENGTH = 36,
    	 						TABLE_COLUMN_RESULT_CODE_LENGTH = 5,
    	 						TABLE_COLUMN_PLAT_REJ_CODE_LENGTH = 5,
    	 						TABLE_COLUMN_MERCHANT_ID_LENGTH = 15;

    // 正規化規則
    public static final String 	BANK_CODE_FORMAT = "^[A-Za-z0-9_-]+$", 	// 機構代碼格式
    							ACQUIRER_CODE_FORMAT = "^[A-Za-z0-9_-]+$", 	// 收單機構代碼格式
    							KEY_REF_FORMAT = "^[A-Za-z0-9_]+$", 	// JWE金鑰識別碼格式: 英文數字及(_)
    							// 通用EMAIL格式, 收單機構/機構/群組人員
    							COMMON_EMAIL_FORMAT = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    
    
}
