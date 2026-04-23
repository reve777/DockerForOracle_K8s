package com.portfolio.eunms;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.portfolio.constants.ModuleConstants;

/**
 * 功能代碼列舉 用於將 URL 路徑對應到功能代碼和功能名稱
 */
public enum FunctionEnum {

	REMINDER("001", ModuleConstants.FUNC_REMINDER),

	B("002", ModuleConstants.FUNC_B);


	private static final String URL_PREFIX = "/portfolio/";
	private static final String DEFAULT_FUNCTION_ID = "999";
	private static final String DEFAULT_FUNCTION_NAME = "unknown_function";

	// 使用 Map 快取提升效能
	private static final Map<String, String> MODULE_TO_FUNCTION_ID = Arrays.stream(values())
			.collect(Collectors.toMap(FunctionEnum::getModuleName, FunctionEnum::getFunctionId));

	private final String functionId;
	private final String moduleName;

	FunctionEnum(String functionId, String moduleName) {
		this.functionId = functionId;
		this.moduleName = moduleName.substring(1).toLowerCase(); // 移除開頭的 "/"
	}

	public String getFunctionId() {
		return functionId;
	}

	public String getModuleName() {
		return moduleName;
	}

	/**
	 * 根據 URL 路徑解析功能代碼
	 * 
	 * @param requestUrl 完整的請求 URL 路徑
	 * @return 對應的功能代碼，若找不到則回傳 "999"
	 */
	public static String parseFunctionId(String requestUrl) {
		return extractModuleName(requestUrl).map(MODULE_TO_FUNCTION_ID::get).orElse(DEFAULT_FUNCTION_ID);
	}

	/**
	 * 根據 URL 路徑產生功能名稱
	 * 
	 * @param requestUrl 完整的請求 URL 路徑
	 * @return 功能名稱,路徑分隔符替換為底線
	 */
	public static String parseFunctionName(String requestUrl) {
		if (StringUtils.isBlank(requestUrl)) {
			return DEFAULT_FUNCTION_NAME;
		}

		String cleanUrl = removeUrlPrefix(requestUrl);
		return removeQueryParams(cleanUrl).replace("/", "_");
	}

	/**
	 * 提取模組名稱的核心邏輯
	 * 
	 * @param requestUrl 完整的請求 URL 路徑
	 * @return 模組名稱 (Optional)
	 */
	private static Optional<String> extractModuleName(String requestUrl) {
		if (StringUtils.isBlank(requestUrl)) {
			return Optional.empty();
		}

		String cleanUrl = removeUrlPrefix(requestUrl);
		String moduleName = removeQueryParams(cleanUrl).split("/")[0].toLowerCase();

		return StringUtils.isNotBlank(moduleName) ? Optional.of(moduleName) : Optional.empty();
	}

	/**
	 * 移除 URL 前綴 統一處理前綴移除邏輯
	 */
	private static String removeUrlPrefix(String url) {
		if (url.startsWith(URL_PREFIX)) {
			return url.substring(URL_PREFIX.length());
		}
		return url;
	}

	/**
	 * 移除查詢參數 將 URL 中 "?" 之後的部分移除
	 */
	private static String removeQueryParams(String url) {
		if (StringUtils.isBlank(url)) {
			return "";
		}
		int queryIndex = url.indexOf('?');
		return queryIndex >= 0 ? url.substring(0, queryIndex) : url;
	}

}
