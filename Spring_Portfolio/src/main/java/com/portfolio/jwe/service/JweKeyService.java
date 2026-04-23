package com.portfolio.jwe.service;

import com.portfolio.jwe.dto.JweKeyRequest;
import com.portfolio.jwe.dto.JweKeyResponse;

/**
 * Service interface for JWE key operations
 */
public interface JweKeyService {
    JweKeyResponse create(JweKeyRequest request) throws Exception;
}
