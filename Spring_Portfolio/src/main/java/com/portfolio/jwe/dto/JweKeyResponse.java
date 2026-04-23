package com.portfolio.jwe.dto;

/**
 * Minimal response for created key
 */
public class JweKeyResponse {
    private String keyId;
    private String keyRef;
    private String pubKeyBase64;
    private String priKeyBase64; // encrypted or encoded private key

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getKeyRef() {
        return keyRef;
    }

    public void setKeyRef(String keyRef) {
        this.keyRef = keyRef;
    }

    public String getPubKeyBase64() {
        return pubKeyBase64;
    }

    public void setPubKeyBase64(String pubKeyBase64) {
        this.pubKeyBase64 = pubKeyBase64;
    }

    public String getPriKeyBase64() {
        return priKeyBase64;
    }

    public void setPriKeyBase64(String priKeyBase64) {
        this.priKeyBase64 = priKeyBase64;
    }
}
