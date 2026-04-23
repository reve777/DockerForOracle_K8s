package com.portfolio.jwe.controller;

import com.portfolio.jwe.dto.JweKeyRequest;
import com.portfolio.jwe.dto.JweKeyResponse;
import com.portfolio.jwe.service.JweKeyService;
import com.portfolio.uitls.CustomMultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/jwekey")
public class JweKeyController {

    private final JweKeyService jweKeyService;
    private final ResourceLoader resourceLoader; // 💡 用來讀取靜態資源

    public JweKeyController(JweKeyService jweKeyService, ResourceLoader resourceLoader) {
        this.jweKeyService = jweKeyService;
        this.resourceLoader = resourceLoader;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestParam("keyType") String keyType,
            @RequestParam(value = "bankId", required = false) String bankId,
            @RequestParam(value = "keyRef", required = false) String keyRef,
            @RequestParam(value = "expireDate", required = false) String expireDate,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            // 💡 邏輯判斷：如果是 OGN 且沒傳檔案，自動抓取 resources/static/sample/default_pub.pem
            if ("OGN".equalsIgnoreCase(keyType) && (file == null || file.isEmpty())) {
                Resource resource = resourceLoader.getResource("classpath:static/sample/default_pub.pem");
                if (resource.exists()) {
                    byte[] fileContent = StreamUtils.copyToByteArray(resource.getInputStream());
                    file = new CustomMultipartFile(fileContent, "default_pub.pem");
                    System.out.println("偵測到 OGN 模式且無檔案，已自動加載預設 PEM");
                }
            }

            JweKeyRequest req = new JweKeyRequest();
            req.setKeyType(keyType);
            req.setBankId(bankId);
            req.setKeyRef(keyRef);
            req.setExpireDate(expireDate);
            req.setFile(file);

            JweKeyResponse resp = jweKeyService.create(req);

            Map<String, Object> map = new HashMap<>();
            map.put("code", "200"); // 配合你前端顯示的 code
            map.put("message", "操作成功");
            map.put("data", resp);
            return ResponseEntity.ok(map);

        } catch (Exception ex) {
            ex.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "建立失敗: " + ex.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/new")
    public ResponseEntity<?> redirectToPageController() {
        return ResponseEntity.status(302).header("Location", "/portfolio/page/jwekey/new").build();
    }
}