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
 * 處理優先順序（@Order(3)）： 1. GlobalSessionAdvice (@Order 預設) 2.
 * GlobalControllerAdvice (@Order(2)) 3. GlobalExceptionHandler (@Order(3)) ← 此類
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
	 * 資源不存在（例如查無此投資人、查無此股票） 建議自行新增
	 * com.portfolio.exception.ResourceNotFoundException
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public ApiResponse handleNotFound(NoResourceFoundException ex) {
		log.warn("[GlobalExceptionHandler] 資源不存在: {}", ex.getMessage());
		return ApiResponse.error("404", "查無此資源，請確認後再試");
	}

	/**
	 * 存取被拒絕（無權限操作）
	 */
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ResponseBody
	public ApiResponse handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		log.warn("[GlobalExceptionHandler] 權限不足 [url={}, 原因={}]", request.getRequestURI(), ex.getMessage());
		return ApiResponse.error("403", "您沒有執行此操作的權限");
	}

	/**
	 * 非法參數（例如傳入負數 ID、格式不符）
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ApiResponse handleIllegalArgument(IllegalArgumentException ex) {
		log.warn("[GlobalExceptionHandler] 非法參數: {}", ex.getMessage());
		return ApiResponse.error("400", "請求參數有誤：" + ex.getMessage());
	}

	// ─────────────────────────────────────────────
	// Spring Validation 例外
	// ─────────────────────────────────────────────

	/**
	 * @Valid / @Validated 驗證失敗（@RequestBody）
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ApiResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		Map<String, String> fieldErrors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String field = ((FieldError) error).getField();
			String message = error.getDefaultMessage();
			fieldErrors.put(field, message);
		});

		String summary = fieldErrors.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue())
				.collect(Collectors.joining("；"));

		log.warn("[GlobalExceptionHandler] 請求參數驗證失敗: {}", summary);
		return ApiResponse.error("400", "輸入資料驗證失敗：" + summary);
	}

	// ─────────────────────────────────────────────
	// 資料庫例外
	// ─────────────────────────────────────────────

	/**
	 * 資料庫完整性約束違反 例如：重複的 email、外鍵約束失敗
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	@ResponseBody
	public ApiResponse handleDataIntegrity(DataIntegrityViolationException ex) {
		log.error("[GlobalExceptionHandler] 資料庫完整性衝突: {}", ex.getMostSpecificCause().getMessage());
		return ApiResponse.error("409", "資料已存在或違反唯一約束，請確認後再試");
	}

	// ─────────────────────────────────────────────
	// 兜底例外（所有未預期的錯誤）
	// ─────────────────────────────────────────────

	/**
	 * 所有未處理的例外最終都在這裡攔截 僅回傳通用錯誤訊息，不暴露內部細節
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ApiResponse handleGeneral(Exception ex, HttpServletRequest request) {
		log.error("[GlobalExceptionHandler] 未預期的系統例外 [url={}]", request.getRequestURI(), ex);
		return ApiResponse.error("500", "系統發生錯誤，請稍後再試");
	}
}