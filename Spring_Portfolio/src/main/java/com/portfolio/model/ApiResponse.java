package com.portfolio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 統一 API 回應格式 所有 REST API 的回應皆包裝成此格式： { "code": "200", "message": "操作成功",
 * "data": { ... } }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

	private String code;
	private String message;
	private Object data;

	// ── 靜態工廠方法 ──────────────────────────────

	public static ApiResponse success(Object data) {
		return new ApiResponse("200", "操作成功", data);
	}

	public static ApiResponse success(String message, Object data) {
		return new ApiResponse("200", message, data);
	}

	public static ApiResponse error(String code, String message) {
		return new ApiResponse(code, message, null);
	}
}