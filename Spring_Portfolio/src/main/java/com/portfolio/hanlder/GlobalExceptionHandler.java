//package com.portfolio.hanlder;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import org.springframework.core.annotation.Order;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.dao.InvalidDataAccessResourceUsageException;
//import org.springframework.http.HttpStatus;
//import org.springframework.transaction.TransactionSystemException;
//import org.springframework.validation.BindException;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.ResponseStatus;
//
//
//
//import jakarta.annotation.PreDestroy;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.validation.ConstraintViolationException;
//import lombok.RequiredArgsConstructor;
//
///**
// * 全局異常處理器
// * 統一處理系統中的各種異常，提供一致的錯誤回應格式
// */
//@Order(3)
//@ControllerAdvice
//@RequiredArgsConstructor
//public class GlobalExceptionHandler {
//
//    private final SysEventLogService sysEventLogService;
//
//    /**
//     * 處理 DataNotFoundException
//     * 讓前端畫面不跳轉，但能取得錯誤訊息
//     * EX: 此筆機構主檔不存在
//     */
//    @ExceptionHandler(DataNotFoundException.class)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    @ResponseBody
//    public ApiResponse handleNotFound(DataNotFoundException ex) {
//        LogUtils.bizWarn("資料不存在 [錯誤=" + ex.getMessage() + "]");
//
//        // 記錄到系統事件日誌
//        sysEventLogService.logException(
//            SystemConstants.EVENT_CODE_SYSTEM_WARN_000,
//            ex,
//            MessageConstants.LOG_DESC_DATA_NOT_FOUND,
//            SystemConstants.ALERT_LEVEL_WARN
//        );
//
//        return ApiResponse.error(ResponseCode.DATA_NOT_FOUND.getCode(),
//        						MessageConstants.DATA_NOT_FOUND_ERROR);
//    }
//
//    /**
//     * 處理資料驗證異常
//     */
//    @ExceptionHandler(ValidationException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ResponseBody
//    public ApiResponse handleValidationException(ValidationException ex) {
//        LogUtils.bizWarn("驗證錯誤：" + ex.getMessage());
//
//        // 記錄到系統事件日誌
//        sysEventLogService.logException(
//            SystemConstants.EVENT_CODE_SYSTEM_WARN_000,
//            ex,
//            MessageConstants.LOG_DESC_VALIDATION_FAILED,
//            SystemConstants.ALERT_LEVEL_WARN
//        );
//
//        return ApiResponse.error(ResponseCode.VALIDATION_ERROR.getCode(),
//        						ex.getMessage()); // 回傳驗證錯誤訊息
//    }
//
//    /**
//     * 處理參數類型不匹配錯誤
//     * 注意：這裡使用 TypeMismatchException 的父類 IllegalArgumentException 來處理
//     * 因為 MethodArgumentTypeMismatchException 在某些 Spring 版本中可能不可用
//     */
//    @ExceptionHandler(IllegalArgumentException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ResponseBody
//    public ApiResponse handleIllegalArgumentException(IllegalArgumentException ex) {
//        LogUtils.error("參數類型不匹配或非法參數 [錯誤=" + ex.getMessage() + "]", ex);
//
//        // 記錄到系統事件日誌
//        sysEventLogService.logException(
//            SystemConstants.EVENT_CODE_SYSTEM_WARN_000,
//            ex,
//            MessageConstants.LOG_DESC_ILLEGAL_ARGUMENT,
//            SystemConstants.ALERT_LEVEL_ERROR
//        );
//
//        return ApiResponse.error(ResponseCode.BAD_REQUEST.getCode(),
//        						MessageConstants.SYSTEM_ERROR_MSG);
//    }
//
//    /**
//     * 處理未授權異常
//     * 根據請求類型返回不同的響應格式
//     */
//    @ExceptionHandler(UnauthorizedException.class)
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    @ResponseBody
//    public ApiResponse handleUnauthorizedException(HttpServletRequest request, UnauthorizedException ex) {
//        String url = request.getRequestURI();
//        LogUtils.bizWarn("權限不足 [url=" + url + ", 錯誤=" + ex.getMessage() + "]");
//
//        // 記錄到系統事件日誌
//        sysEventLogService.logException(
//            SystemConstants.EVENT_CODE_SYSTEM_WARN_000,
//            ex,
//            MessageConstants.LOG_DESC_UNAUTHORIZED + ", URL: " + url,
//            SystemConstants.ALERT_LEVEL_WARN
//        );
//
//        return ApiResponse.error(ResponseCode.UNAUTHORIZED.getCode(),
//        						MessageConstants.NO_PERMISSION_ERROR_MSG);
//    }
//
//    /**
//     * 處理 Spring Validation 框架的參數驗證錯誤
//     */
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ResponseBody
//    public ApiResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach(error -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//
//        String errorMessage = errors.entrySet().stream()
//                .map(entry -> entry.getKey() + ": " + entry.getValue())
//                .collect(Collectors.joining(", "));
//
//        LogUtils.error("請求參數驗證失敗 [錯誤=" + errors + "]", ex);
//
//        // 記錄到系統事件日誌
//        sysEventLogService.logException(
//            SystemConstants.EVENT_CODE_SYSTEM_ERROR_900,
//            ex,
//            MessageConstants.LOG_DESC_REQUEST_PARAM_FAILED + ": " + errorMessage,
//            SystemConstants.ALERT_LEVEL_ERROR
//        );
//
//        return ApiResponse.error(ResponseCode.VALIDATION_ERROR.getCode(),
//        						MessageConstants.SYSTEM_ERROR_MSG);
//    }
//
//    /**
//     * 處理表單綁定錯誤
//     */
//    @ExceptionHandler(BindException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ResponseBody
//    public ApiResponse handleBindException(BindException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach(error -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//
//        LogUtils.error("表單綁定失敗 [錯誤=" + errors + "]", ex);
//
//        // 記錄到系統事件日誌
//        sysEventLogService.logException(
//            SystemConstants.EVENT_CODE_SYSTEM_ERROR_900,
//            ex,
//            MessageConstants.LOG_DESC_FORM_BINDING_FAILED + ": " + errors,
//            SystemConstants.ALERT_LEVEL_ERROR
//        );
//
//        return ApiResponse.error(ResponseCode.VALIDATION_ERROR.getCode(),
//        						MessageConstants.SYSTEM_ERROR_MSG);
//    }
//
//    /*
//     * 處理物件定義錯誤回傳
//     */
//    @ExceptionHandler(TransactionSystemException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ResponseBody
//    public ApiResponse handleTransactionSystemException(TransactionSystemException ex) {
//        Throwable cause = ex.getRootCause();
//        if (cause instanceof ConstraintViolationException) {
//            ConstraintViolationException cve = (ConstraintViolationException) cause;
//            // 將所有驗證錯誤訊息合併
//            Map<String, String> errors = new HashMap<>();
//            cve.getConstraintViolations().forEach(cv -> {
//                String path = cv.getPropertyPath().toString();
//                String message = cv.getMessage();
//                errors.put(path, message);
//            });
//
//            String errorMessage = errors.entrySet().stream()
//                    .map(entry -> entry.getKey() + ": " + entry.getValue())
//                    .collect(Collectors.joining(", "));
//
//            return ApiResponse.error(ResponseCode.VALIDATION_ERROR.getCode(), errorMessage);
//        }
//
//        LogUtils.error("資料庫事務處理失敗 [錯誤=" + ex.getMessage() + "]", ex);
//
//        // 記錄到系統事件日誌
//        sysEventLogService.logException(
//            SystemConstants.EVENT_CODE_SYSTEM_ERROR_900,
//            ex,
//            MessageConstants.LOG_DESC_TRANSACTION_FAILED,
//            SystemConstants.ALERT_LEVEL_ERROR
//        );
//
//        return ApiResponse.error(ResponseCode.SYSTEM_ERROR.getCode(),
//        						MessageConstants.SYSTEM_ERROR_MSG);
//    }
//
//    /**
//     * 處理 Bean Validation 的約束違反異常
//     */
//    @ExceptionHandler(ConstraintViolationException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ResponseBody
//    public ApiResponse handleConstraintViolation(ConstraintViolationException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getConstraintViolations().forEach(cv -> {
//            String path = cv.getPropertyPath().toString();
//            String message = cv.getMessage();
//            errors.put(path, message);
//        });
//
//        LogUtils.error("數據約束違規 [錯誤=" + errors + "]", ex);
//
//        // 記錄到系統事件日誌
//        sysEventLogService.logException(
//            SystemConstants.EVENT_CODE_SYSTEM_ERROR_900,
//            ex,
//            MessageConstants.LOG_DESC_CONSTRAINT_VIOLATION + ": " + errors,
//            SystemConstants.ALERT_LEVEL_ERROR
//        );
//
//        return ApiResponse.error(ResponseCode.VALIDATION_ERROR.getCode(),
//        						MessageConstants.SYSTEM_ERROR_MSG);
//    }
//
//    /**
//     * 處理DB 更新錯誤，
//     * 插入、更新、刪除資料時違反了資料庫的完整性約束時會拋出這個異常。
//     */
//    @ExceptionHandler(DataIntegrityViolationException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ResponseBody
//    public ApiResponse handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
//        LogUtils.error("數據完整性違規 [錯誤=" + ex.getMessage() + "]", ex);
//
//        // 記錄到系統事件日誌
//        sysEventLogService.logException(
//            SystemConstants.EVENT_CODE_SYSTEM_ERROR_900,
//            ex,
//            MessageConstants.LOG_DESC_DATA_INTEGRITY_VIOLATION,
//            SystemConstants.ALERT_LEVEL_ERROR
//        );
//
//        return ApiResponse.error(ResponseCode.SYSTEM_ERROR.getCode(),
//        						MessageConstants.SYSTEM_ERROR_MSG);
//    }
//
//    /**
//     * 處理 SQL/資料表格式錯誤
//     */
//    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ResponseBody
//    public ApiResponse handleInvalidDataAccessResourceUsageException(InvalidDataAccessResourceUsageException ex) {
//        LogUtils.error("SQL或資料表格式錯誤 [錯誤=" + ex.getMessage() + "]", ex);
//
//        // 記錄到系統事件日誌
//        sysEventLogService.logException(
//            SystemConstants.EVENT_CODE_SYSTEM_ERROR_900,
//            ex,
//            MessageConstants.LOG_DESC_SQL_ERROR,
//            SystemConstants.ALERT_LEVEL_ERROR
//        );
//
//        return ApiResponse.error(ResponseCode.SYSTEM_ERROR.getCode(),
//        						MessageConstants.SYSTEM_ERROR_MSG);
//    }
//
//    /**
//     * 處理檔案下載異常
//     */
//    @ExceptionHandler(FileDownloadException.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ResponseBody
//    public ApiResponse handleFileDownloadException(FileDownloadException ex) {
//        LogUtils.error("檔案下載失敗 [錯誤=" + ex.getMessage() + "]", ex);
//
//        // 記錄到系統事件日誌
//        sysEventLogService.logException(
//            SystemConstants.EVENT_CODE_SYSTEM_ERROR_900,
//            ex,
//            MessageConstants.LOG_DESC_FILE_DOWNLOAD_FAILED + ": " + ex.getMessage(),
//            SystemConstants.ALERT_LEVEL_ERROR
//        );
//
//        return ApiResponse.error(ex.getCode(), ex.getMessage());
//    }
//
//    /**
//     * 處理資料庫交易異常
//     */
//    @ExceptionHandler({
//        org.springframework.dao.RecoverableDataAccessException.class,
//        org.springframework.transaction.TransactionException.class,
//        org.springframework.dao.DataAccessException.class
//    })
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ResponseBody
//    public ApiResponse handleTransactionException(Exception ex) {
//        // 找出根本原因
//        Throwable cause = ex;
//        while (cause.getCause() != null) {
//            cause = cause.getCause();
//        }
//
//        LogUtils.error("資料庫交易處理異常: " + cause.getClass().getName(), ex);
//
//        // 記錄到系統事件日誌
//        sysEventLogService.logException(
//            SystemConstants.EVENT_CODE_SYSTEM_ERROR_900,
//            ex,
//            "資料庫交易異常: " + cause.getMessage(),
//            SystemConstants.ALERT_LEVEL_ERROR
//        );
//
//        return ApiResponse.error(ResponseCode.DATABASE_ERROR.getCode(),
//        						MessageConstants.SYSTEM_ERROR_MSG);
//    }
//
//    /**
//     * 處理系統狀態異常
//     */
//    @ExceptionHandler(IllegalStateException.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ResponseBody
//    public ApiResponse handleIllegalStateException(IllegalStateException ex) {
//        LogUtils.error("系統狀態異常 [錯誤=" + ex.getMessage() + "]", ex);
//
//        // 記錄到系統事件日誌
//        sysEventLogService.logException(
//            SystemConstants.EVENT_CODE_SYSTEM_ERROR_900,
//            ex,
//            MessageConstants.LOG_DESC_ILLEGAL_STATE + ": " + ex.getMessage(),
//            SystemConstants.ALERT_LEVEL_ERROR
//        );
//
//        return ApiResponse.error(ResponseCode.SYSTEM_ERROR.getCode(),
//        						MessageConstants.SYSTEM_ERROR_MSG);
//    }
//
//    /**
//     * 處理所有其他未處理的異常
//     */
//    @ExceptionHandler(value = Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ResponseBody
//    public ApiResponse defaultErrorHandler(HttpServletRequest req, Exception e) {
//        String url = req.getRequestURL().toString();
//        LogUtils.error("系統異常 [url=" + url + ", 錯誤=" + e.getMessage() + "]", e);
//
//        // 記錄到系統事件日誌
//        sysEventLogService.logException(
//            SystemConstants.EVENT_CODE_SYSTEM_ERROR_999,
//            e,
//            MessageConstants.LOG_DESC_SYSTEM_ERROR + ", URL: " + url,
//            SystemConstants.ALERT_LEVEL_ERROR
//        );
//
//        return ApiResponse.error(ResponseCode.SYSTEM_ERROR.getCode(),
//        						MessageConstants.SYSTEM_ERROR_MSG);
//    }
//
//    // 防禦性機制，確保在 bean 銷毀前清理任何剩餘的上下文
//    @PreDestroy
//    public void cleanup() {
//        LogUtils.clear();
//    }
//
//}
