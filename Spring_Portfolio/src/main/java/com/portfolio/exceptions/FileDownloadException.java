package com.portfolio.exceptions;
import java.io.Serial;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 檔案下載異常類
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileDownloadException extends RuntimeException {

	@Serial
    private static final long serialVersionUID = 1L;

	private String code = "9995";

    // 不需要自定義錯誤代碼的情況
    public FileDownloadException(String message) {
        super(message);
    }

    // 需要自定義錯誤代碼的情況
    public FileDownloadException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
