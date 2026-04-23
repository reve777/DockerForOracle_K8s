package com.portfolio.jwe.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.portfolio.jwe.config.JweKeyProperties;
import com.portfolio.jwe.dto.JweKeyRequest;
import com.portfolio.jwe.dto.JweKeyResponse;
import com.portfolio.jwe.service.JweKeyService;

@Service
public class JweKeyServiceImpl implements JweKeyService {

    private static final String ALG_RSA = "RSA";
    private static final int RSA_KEY_SIZE = 4096;
    private static final String ALG_SHA256 = "SHA-256";

    private final JweKeyProperties props;

    public JweKeyServiceImpl(JweKeyProperties props) {
        this.props = props;
    }

    @Override
    public JweKeyResponse create(JweKeyRequest request) throws Exception {
        if (request == null || StringUtils.isBlank(request.getKeyType())) {
            throw new IllegalArgumentException("keyType is required");
        }

        String keyType = request.getKeyType().trim();
        JweKeyResponse resp = new JweKeyResponse();
        resp.setKeyId(UUID.randomUUID().toString());

        // --- SELF 模式 ---
        if (props.isSelfCode(keyType)) {
            KeyPair keyPair = generateRSAKeyPair();
            String pubBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String priBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            resp.setPubKeyBase64(pubBase64);
            resp.setPriKeyBase64(priBase64);
            resp.setKeyRef(generateKeyRef(pubBase64));
            return resp;
        }

        // --- ORG 模式 (處理 PEM 檔案) ---
        if (props.isOrgCode(keyType)) {
            MultipartFile file = request.getFile();
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("PEM file is required");
            }

            // 1. 讀取原始字串
            String rawContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            
            // 2. 進行徹底清洗：只允許 Base64 合法字元
            String sanitized = sanitizeBase64(rawContent);

            try {
                // 3. 解碼 (使用 MimeDecoder 增加對不規則換行的容忍度)
                byte[] keyBytes = Base64.getMimeDecoder().decode(sanitized);
                
                KeyFactory kf = KeyFactory.getInstance(ALG_RSA);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                PublicKey publicKey = kf.generatePublic(keySpec);

                // 4. 驗證長度
                if (!(publicKey instanceof RSAPublicKey rsaKey) || rsaKey.getModulus().bitLength() != RSA_KEY_SIZE) {
                    throw new IllegalArgumentException("The RSA key size must be " + RSA_KEY_SIZE + " bits.");
                }

                // 5. 轉回標準 Base64 字串
                String finalPubBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
                resp.setPubKeyBase64(finalPubBase64);
                resp.setKeyRef(StringUtils.defaultIfBlank(request.getKeyRef(), generateKeyRef(finalPubBase64)));

                return resp;
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse Public Key: " + e.getMessage());
            }
        }
        throw new IllegalArgumentException("Unsupported keyType: " + keyType);
    }

    /**
     * 終極清洗：只保留 Base64 字典字元，並自動處理長度問題
     */
    private String sanitizeBase64(String raw) {
        if (raw == null) return "";
        
        // 移除 PEM 標籤行
        String noTags = raw.replaceAll("-+[^-]+-+", "");
        
        // 移除所有非 Base64 字元 (只保留 A-Z, a-z, 0-9, +, /)
        // 注意：先不保留 '='，因為後面會根據長度統一補齊
        String pureBase64 = noTags.replaceAll("[^A-Za-z0-9+/]", "");
        
        // 自動補齊 '=' Padding，確保長度是 4 的倍數
        int mod = pureBase64.length() % 4;
        if (mod > 0) {
            pureBase64 += "====".substring(mod);
        }
        
        return pureBase64;
    }

    private KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ALG_RSA);
        keyPairGen.initialize(RSA_KEY_SIZE);
        return keyPairGen.generateKeyPair();
    }

    private String generateKeyRef(String publicKeyBase64) {
        try {
            String jwkJson = String.format("{\"kty\":\"RSA\",\"n\":\"%s\"}", publicKeyBase64);
            MessageDigest digest = MessageDigest.getInstance(ALG_SHA256);
            byte[] hash = digest.digest(jwkJson.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            String result = hex.toString().toUpperCase();
            return result.length() > 13 ? result.substring(0, 13) : result;
        } catch (Exception e) {
            return String.valueOf(System.currentTimeMillis()).substring(0, 13);
        }
    }
}