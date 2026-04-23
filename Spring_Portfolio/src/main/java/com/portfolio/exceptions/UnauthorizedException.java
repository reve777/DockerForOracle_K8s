package com.portfolio.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 沒有權限
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -3426905549855455425L;

	private String code = "9993";

	public UnauthorizedException(String message) {
        super(message);
    }

    // 需要自定義錯誤代碼的情況
    public UnauthorizedException(String code, String message) {
        super(message);
        this.code = code;
    }

	public String getCode() {
	    return code;
	}

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

}
