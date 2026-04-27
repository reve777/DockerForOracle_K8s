package com.portfolio.advice;

import com.portfolio.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全域例外處理器 統一攔截各類例外，回傳一致的錯誤格式，防止 Stack Trace 直接暴露給前端。
 *
 * 處理優先順序（@Order(3)）： 
 * 1. GlobalSessionAdvice (@Order 預設) 
 * 2. GlobalControllerAdvice (@Order(2)) 
 * 3. GlobalExceptionHandler (@Order(3)) ← 此類
 * 4. GlobalResponseBodyAdvice (@Order(4))
 */
@Slf4j
@Order(3)
@ControllerAdvice
public class GlobalExceptionHandler {

    // ─────────────────────────────────────────────
    // 業務層例外
    // ─────────────────────────────────────────────

    /**
     * 資源不存在（404）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ApiResponse handleNotFound(NoResourceFoundException ex) {
        log.warn("[GlobalExceptionHandler] 資源不存在: {}", ex.getMessage());
        return ApiResponse.error("404", "查無此資源，請確認後再試");
    }

    /**
     * 存取被拒絕（403）
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ApiResponse handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("[GlobalExceptionHandler] 權限不足 [url={}, 原因={}]", request.getRequestURI(), ex.getMessage());
        return ApiResponse.error("403", "您沒有執行此操作的權限");
    }

    /**
     * 非法參數（400）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiResponse handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("[GlobalExceptionHandler] 非法參數: {}", ex.getMessage());
        return ApiResponse.error("400", "請求參數有誤：" + ex.getMessage());
    }

    // ─────────────────────────────────────────────
    // Spring Validation 驗證例外
    // ─────────────────────────────────────────────

    /**
     * @Valid / @Validated 驗證失敗
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                String field = ((FieldError) error).getField();
                String message = error.getDefaultMessage();
                fieldErrors.put(field, message);
            } else {
                fieldErrors.put(error.getObjectName(), error.getDefaultMessage());
            }
        });

        String summary = fieldErrors.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("；"));

        log.warn("[GlobalExceptionHandler] 請求參數驗證失敗: {}", summary);
        return ApiResponse.error("400", "輸入資料驗證失敗：" + summary);
    }

    // ─────────────────────────────────────────────
    // 資料庫例外
    // ─────────────────────────────────────────────

    /**
     * 資料庫完整性衝突（409）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ApiResponse handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("[GlobalExceptionHandler] 資料庫完整性衝突: {}", ex.getMostSpecificCause().getMessage());
        return ApiResponse.error("409", "資料衝突或違反唯一約束（例如重複名稱），請確認後再試");
    }

    // ─────────────────────────────────────────────
    // 兜底處理
    // ─────────────────────────────────────────────

    /**
     * 所有未處理的 Exception
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiResponse handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("[GlobalExceptionHandler] 未預期的系統例外 [url={}]", request.getRequestURI(), ex);
        return ApiResponse.error("500", "系統內部錯誤，請聯絡管理員");
    }

    /**
     * 💡 額外處理 Throwable (包含 java.lang.Error)
     * 用於捕捉如剛才遇到的 Unresolved compilation problem 等嚴重系統錯誤。
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiResponse handleThrowable(Throwable ex, HttpServletRequest request) {
        log.error("[GlobalExceptionHandler] 嚴重系統錯誤 (Throwable) [url={}]", request.getRequestURI(), ex);
        return ApiResponse.error("500", "系統底層發生嚴重錯誤，請檢查伺服器日誌");
    }
    
    
 // ─────────────────────────────────────────────
    // 業務層例外 (新增 IllegalStateException)
    // ─────────────────────────────────────────────

    /**
     * 處理業務邏輯狀態錯誤（如：餘額不足）
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 或者用 BAD_REQUEST (400)
    @ResponseBody
    public ApiResponse handleIllegalState(IllegalStateException ex) {
        log.warn("[GlobalExceptionHandler] 業務邏輯衝突: {}", ex.getMessage());
        // 這樣前端就能看到 "餘額不足"
        return ApiResponse.error("409", ex.getMessage()); 
    }

    // ─────────────────────────────────────────────
    // 資料庫併發例外 (新增 PessimisticLockingFailureException)
    // ─────────────────────────────────────────────

    /**
     * 捕捉悲觀鎖/死鎖失敗 (當 @Retryable 次數用盡時會進到這裡)
     */
    /**
     * 捕捉悲觀鎖、死鎖、鎖取得超時失敗
     */
    @ExceptionHandler({
        org.springframework.dao.PessimisticLockingFailureException.class,
        org.springframework.dao.CannotAcquireLockException.class,
        org.springframework.dao.QueryTimeoutException.class // 鎖等待超時
    })
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ResponseBody
    public ApiResponse handleLockingFailure(Exception ex) {
        log.error("[GlobalExceptionHandler] 併發存取失敗 (重試已耗盡): {}", ex.getMessage());
        return ApiResponse.error("503", "系統繁忙（併發競爭過大），請稍候再試");
    }
}