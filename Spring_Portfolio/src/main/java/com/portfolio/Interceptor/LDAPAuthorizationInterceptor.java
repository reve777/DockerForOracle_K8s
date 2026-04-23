package com.portfolio.Interceptor;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.portfolio.constants.MessageConstants;
import com.portfolio.constants.SystemConstants;
import com.portfolio.exceptions.UnauthorizedException;
import com.portfolio.uitls.LogUtils;
import com.portfolio.uitls.PermissionUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LDAPAuthorizationInterceptor implements HandlerInterceptor {

	// URI 必須包含4個字串才能檢查權限 (含空字串), 如: /tdep_portal/bank/main
	public static final int MIN_URI_SEGMENTS_FOR_PERMISSION_CHECK = 4;

	/**
	 * 請求前置處理：驗證用戶是否有權限存取指定功能
	 * 
	 * @param request  HTTP 請求
	 * @param response HTTP 回應
	 * @param handler  處理器
	 * @return true=允許繼續, false=阻止請求
	 * @throws IOException           當 網路中斷, 客戶端連線異常, 伺服器資源不足 拋出
	 * @throws UnauthorizedException 當用戶權限驗證失敗時拋出
	 */
	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull Object handler) throws UnauthorizedException, IOException {
		// 印出所有 header
//    	Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//        	String headerName = headerNames.nextElement();
//        	LogUtils.system("{} / {} ", headerName, request.getHeader(headerName));
//        }

//        String ivCreds = request.getHeader(SystemConstants.IV_CREDS);
//        String myAuth = PermissionUtils.extractPermissionFromIvCreds(ivCreds);
		String myAuth = request.getHeader(SystemConstants.PERMISSION_SYSTEM_ID);
		String userId = request.getHeader(SystemConstants.PERMISSION_USER_ID);

//		LogUtils.system("myAuth="+myAuth);
//		LogUtils.system("userId="+userId);

		// 測試Code
		myAuth = "8,8,15,32,15,32,15,32,15,32,15,32,15,32,15,32,9"; // 全開(看Excel: Portal權限表)

		// 讀取 HttpHeader 'my-auth'
		if (myAuth == null) {
			LogUtils.bizWarn("權限驗證失敗：無法從 request 中提取權限資訊 [userId={}, uri={}]", userId, request.getRequestURI());
			throw new UnauthorizedException("權限驗證失敗：缺少必要的權限資訊");
		}

		// 讀取 URI (僅路徑，不包括主機,ex:"/reminder/list")
		String uri = request.getRequestURI();

		// ***讀取權限
		boolean isGranted = processPermission(uri, myAuth);
		LogUtils.debug("isGranted=" + isGranted);

		// 沒有權限返回401
		if (!isGranted) {
			throw new UnauthorizedException(MessageConstants.NO_PERMISSION_ERROR_MSG);
		}

		return true;
	}

	/**
	 * 處理權限驗證邏輯 解析 URI 並檢查用戶是否有對應功能的操作權限
	 * 
	 * @param uri              請求的 URI 路徑
	 * @param permissionValues 用戶權限字串
	 * @return 是否有權限
	 * @throws Exception 處理過程中的例外
	 */
	private boolean processPermission(String uri, String permissionValues) {
		LogUtils.debug("=== 處理權限開始 ===");
		LogUtils.debug("URI: {}", uri);
		LogUtils.debug("權限值: {}", permissionValues);

		String[] pathSegments = uri.split("/");
		if (pathSegments.length < MIN_URI_SEGMENTS_FOR_PERMISSION_CHECK) {
			LogUtils.debug("URI格式不足以進行權限檢查: {}, 允許訪問", uri);
			return true;
		}

		String domain = pathSegments[1];
		String function = pathSegments[2];
		String action = pathSegments[3];

		LogUtils.debug("domain={}, function={}, action={}", domain, function, action);

		// 檢查指定操作的權限
		boolean result = PermissionUtils.hasPermission(permissionValues, function, action);

		LogUtils.debug("最終權限檢查結果: {}", result);
		LogUtils.debug("=== 處理權限結束 ===");

		return result;
	}

}
