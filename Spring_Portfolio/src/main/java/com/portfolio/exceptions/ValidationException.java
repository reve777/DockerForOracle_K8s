package com.portfolio.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 驗證錯誤
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends BusinessException {
	@Serial
	private static final long serialVersionUID = 5889323181261628998L;

    private static final String DEFAULT_CODE = "9992";

    // 不需要自定義錯誤代碼的情況
    public ValidationException(String message) {
        super(DEFAULT_CODE, message);
    }

    // 需要自定義錯誤代碼的情況
    public ValidationException(String code, String message) {
        super(code, message);
    }

}
