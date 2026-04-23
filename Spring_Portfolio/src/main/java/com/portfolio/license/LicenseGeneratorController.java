package com.portfolio.license;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/license")
@RequiredArgsConstructor
public class LicenseGeneratorController {

    private final LicenseGeneratorService service;

    /**
     * Generate license and return JSON containing base64 license and meta info.
     *
     * 中文說明:
     * 此 POST API 用於產生一組 license，回傳內容為 JSON 格式，包含:
     * - licenseBase64: 產生的 license 二進位經 Base64 編碼後的字串
     * - activationCode: 啟用碼 (8 個十六進位字元)
     * - licenseRefBase64: licenseRef (8 bytes) 的 Base64 表示
     * - privateKeyBase64: 私鑰的 Base64（用於備份或後續使用，請妥善保管）
     *
     * 注意: 此 API 預設不需要 CSRF token (已在 SecurityConfiguration 排除)，
     * 若要限制存取請改為加入 API key 或其他認證機制。
     */
    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LicenseResult> generate() throws Exception {
        LicenseResult result = service.generateLicense();
        return ResponseEntity.ok(result);
    }

}
