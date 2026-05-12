package com.portfolio.linepay.util;

public class LinePayApiUris {
    // 1. 付款請求
    public static final String REQUEST = "/v4/payments/request";
    // 2. 查詢付款請求狀態
    public static final String CHECK_REQUEST_STATUS = "/v4/payments/requests/%s/check";
    // 3. 付款授權 (Confirm)
    public static final String CONFIRM = "/v4/payments/%s/confirm";
    // 4. 請款 (Capture - 用於預先授權)
    public static final String CAPTURE = "/v4/payments/authorizations/%s/capture";
    // 5. 取消授權 (Void)
    public static final String VOID_AUTH = "/v4/payments/authorizations/%s/void";
    // 6. 查詢付款明細
    public static final String PAYMENT_DETAILS = "/v4/payments";
    // 7. 退款
    public static final String REFUND = "/v4/payments/%s/refund";
    
    // ==========================================
    // 預先授權付款 (Preapproved Pay) 專區
    // ==========================================
    // 8. 檢查預先授權付款密鑰狀態
    public static final String PREAPPROVED_CHECK = "/v4/payments/preapprovedPay/%s/check";
    // 9. 預先授權付款請求
    public static final String PREAPPROVED_PAY = "/v4/payments/preapprovedPay/%s/payment";
    // 10. 刪除預先授權付款密鑰
    public static final String PREAPPROVED_EXPIRE = "/v4/payments/preapprovedPay/%s/expire";
}
