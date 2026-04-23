package com.portfolio.exceptions;
import java.io.Serial;

/**
 * 資源未找到異常類
 */
public class ResourceNotFoundException extends RuntimeException {

	@Serial
    private static final long serialVersionUID = 1L;

	private String code = "9996";

    // 不需要自定義錯誤代碼的情況
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // 需要自定義錯誤代碼的情況
    public ResourceNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}