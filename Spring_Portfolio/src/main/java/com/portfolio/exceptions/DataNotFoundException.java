package com.portfolio.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 取無資料錯誤
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DataNotFoundException extends BusinessException {

	@Serial
	private static final long serialVersionUID = 8833291832948880932L;

    private static final String DEFAULT_CODE = "9991";

    // 不需要自定義錯誤代碼的情況
    public DataNotFoundException(String message) {
        super(DEFAULT_CODE, message);
    }

    // 需要自定義錯誤代碼的情況
    public DataNotFoundException(String code, String message) {
        super(code, message);
    }

}
