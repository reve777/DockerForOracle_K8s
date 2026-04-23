package com.portfolio.exceptions;

import java.io.Serial;

/**
 * 業務異常基類
 * 所有業務邏輯相關的異常都可以繼承此類
 * 不產生堆疊追蹤，提升效能
 */
public abstract class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 5889323181261628118L;

    protected String code;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
