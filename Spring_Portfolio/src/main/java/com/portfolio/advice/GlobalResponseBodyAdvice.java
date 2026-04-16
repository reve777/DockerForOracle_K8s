package com.portfolio.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.model.ApiResponse;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全域 REST 回應封裝處理器 自動將 @RestController 的回傳值統一包裝為 ApiResponse 格式， 讓前端接收到一致的 JSON
 * 結構：{ code, message, data }
 *
 * 排除以下情況（不做包裝）： - 已是 ApiResponse 類型（避免雙重包裝） - ResponseEntity 類型（開發者自行控制格式） -
 * byte[] 二進位資料（檔案下載等）
 */
@Slf4j
@Order(4)
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {

	private final ObjectMapper objectMapper;

	@Override
	public boolean supports(@NonNull MethodParameter returnType,
			@NonNull Class<? extends HttpMessageConverter<?>> converterType) {

		// 1. 原有的排除條件
		boolean isNotWrapped = !ResponseEntity.class.isAssignableFrom(returnType.getParameterType())
				&& !ApiResponse.class.isAssignableFrom(returnType.getParameterType());

		// 2. 額外排除：如果是 String 且回傳的是視圖名稱（Thymeleaf/JSP），通常不包裝
		// 或者你可以根據是否有特定 Annotation 來決定要不要包裝
		return !returnType.getParameterType().equals(void.class) &&
				!returnType.getParameterType().isAssignableFrom(Resource.class);
	}

	@Override
	public Object beforeBodyWrite(@Nullable Object body, @NonNull MethodParameter returnType,
			@NonNull MediaType mediaType, @NonNull Class<? extends HttpMessageConverter<?>> converterType,
			@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {

		// 1. 如果 body 已經是 ApiResponse 類型或二進位資料，直接放行不做二次包裝
		if (body instanceof ApiResponse || body instanceof byte[]) {
			return body;
		}

		// 2. 針對 String 類型進行特殊處理
		if (body instanceof String) {
			// 如果 MediaType 是 TEXT_HTML，通常代表是 Thymeleaf 回傳的頁面路徑，直接回傳不包裝
			if (mediaType.isCompatibleWith(MediaType.TEXT_HTML)) {
				return body;
			}

			try {
				// 強制將回傳 Header 的 Content-Type 改為 application/json
				response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

				// 將字串包裝進 ApiResponse，並透過 ObjectMapper 轉為 JSON 格式的字串
				// 這樣前端接收到的會是 {"code": 200, "message": "成功", "data": "你的字串內容"}
				return objectMapper.writeValueAsString(buildApiResponse(body));
			} catch (JsonProcessingException e) {
				log.error("[GlobalResponseBodyAdvice] String 類型包裝 JSON 失敗", e);
				// 萬一轉換失敗，退而求其次回傳原始字串
				return body;
			}
		}

		// 3. 其他非 String 類型（如 POJO, List, Map 等），統一呼叫封裝方法
		return buildApiResponse(body);
	}

	/**
	 * 建立統一的 ApiResponse 物件
	 * 若為 Spring Data 的 Page 物件，則拆解分頁資訊
	 */
	private ApiResponse buildApiResponse(Object body) {
		if (body instanceof Page<?> page) {
			Map<String, Object> pageData = new HashMap<>();
			pageData.put("content", page.getContent());
			pageData.put("number", page.getNumber());
			pageData.put("size", page.getSize());
			pageData.put("totalElements", page.getTotalElements());
			pageData.put("totalPages", page.getTotalPages());
			return ApiResponse.success(pageData);
		}
		// 一般物件包裝
		return ApiResponse.success(body);
	}
}