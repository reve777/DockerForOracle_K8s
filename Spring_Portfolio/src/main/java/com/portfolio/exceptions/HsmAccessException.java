package com.portfolio.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class HsmAccessException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 8833291832948880932L;

	private String code = "9994";

    // 不需要自定義錯誤代碼的情況
    public HsmAccessException(String message) {
        super(message);
    }

    // 需要自定義錯誤代碼的情況
    public HsmAccessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
