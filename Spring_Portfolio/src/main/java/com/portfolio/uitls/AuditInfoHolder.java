package com.portfolio.uitls;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;

import com.portfolio.bean.AuditInfo;
import com.portfolio.constants.MessageConstants;
import com.portfolio.constants.SystemConstants;

import lombok.extern.log4j.Log4j2;

/*
	 使用 ThreadLocal 來管理每個執行緒各自的 AuditInfo 實例。這樣的設計確保了在多執行緒環境中，
	 每個執行緒都擁有自己獨立的 AuditInfo，避免了不同執行緒之間的資料混淆或競爭條件。
 * */
@Log4j2
public class AuditInfoHolder {
    // 使用 ThreadLocal 保存當前線程的審計信息
    private static final ThreadLocal<AuditInfo> AUDIT_INFO = new ThreadLocal<>();

    // 私有構造函數防止實例化
    private AuditInfoHolder() {
        throw new AssertionError(MessageConstants.TOOLS_ERROR_STRING);
    }

    /**
     * 獲取當前線程的審計信息
     */
    public static AuditInfo getAuditInfo() {
        return AUDIT_INFO.get();
    }

    /**
     * 設置當前線程的審計信息
     */
    public static void setAuditInfo(AuditInfo auditInfo) {
        log.debug("設置審計信息: {}", auditInfo != null ? auditInfo.getGuid() : "null");
        AUDIT_INFO.set(auditInfo);
        // 設置log資訊
        setupMdc(auditInfo);
    }

    /**
     * 清除當前線程的審計信息
     * 注意：在請求結束時必須調用此方法，以防止內存洩漏
     */
    public static void clearAuditInfo() {
        log.debug("清除審計信息");
        AUDIT_INFO.remove();

        // 清除 MDC
        ThreadContext.clearAll();
    }

    /**
     * 設置 MDC 兼容性層
     * 切換日誌框架時，只需修改此方法即可
     */
    private static void setupMdc(AuditInfo auditInfo) {
        if (auditInfo != null && StringUtils.isNotBlank(auditInfo.getUserId())) {
            // 當前使用 Log4j2 的 ThreadContext
            ThreadContext.put(SystemConstants.USER_ID_KEY, StringUtils.defaultString(auditInfo.getUserId()));
            ThreadContext.put(SystemConstants.USER_NAME_KEY, StringUtils.defaultString(auditInfo.getUserName()));
//            ThreadContext.put(SystemConstants.REQUEST_ID_KEY, StringUtils.defaultString(auditInfo.getGuid()));
//            ThreadContext.put(SystemConstants.PATH, StringUtils.defaultString(auditInfo.getRequestURL()));
        }
    }

    /**
     * 獲取用戶 ID
     */
    public static String getUserId() {
        AuditInfo info = AUDIT_INFO.get();
        return info != null ? info.getUserId() : null;
    }

    /**
     * 獲取用戶名稱
     */
    public static String getUserName() {
        AuditInfo info = AUDIT_INFO.get();
        return info != null ? info.getUserName() : null;
    }

    /**
     * 獲取請求 ID
     */
    public static String getRequestId() {
        AuditInfo info = AUDIT_INFO.get();
        return info != null ? info.getGuid() : null;
    }
}
