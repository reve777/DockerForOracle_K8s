package com.portfolio.license;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseResult {
    // Base64 encoded license file bytes
    // 中文: license 二進位內容經 Base64 編碼，用戶端可以 decode 後寫入檔案
    private String licenseBase64;
    // Activation code (8 hex chars)
    // 中文: 啟用碼 (8 個十六進位字元)，用於派生 AES key 的一部分
    private String activationCode;
    // Base64 encoded licenseRef (8 bytes)
    // 中文: licenseRef 的 Base64 表示（來源為私鑰的 SHA-256 前 8 bytes）
    private String licenseRefBase64;
    // Base64 encoded private key (for safekeeping) - unencrypted here
    // 中文: 私鑰的 Base64 表示（未加密），僅作備份用途，請妥善保管，避免外洩
    private String privateKeyBase64;
}
