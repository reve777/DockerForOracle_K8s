package com.portfolio.jwe.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * DTO for create request
 */
public class JweKeyRequest {
    private String keyType; // "SELF" or "ORG"
    private String bankId;
    private String keyRef;
    private String expireDate; // yyyy-MM-dd optional
    private MultipartFile file; // optional (for ORG)

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getKeyRef() {
        return keyRef;
    }

    public void setKeyRef(String keyRef) {
        this.keyRef = keyRef;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
