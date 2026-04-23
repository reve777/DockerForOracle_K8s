package com.portfolio.hanlder;

import org.springframework.core.annotation.Order;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.bean.PermissionBean;
import com.portfolio.constants.ModuleConstants;
import com.portfolio.constants.SystemConstants;
import com.portfolio.uitls.LogUtils;
import com.portfolio.uitls.PermissionUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * 全域的 Model Advice，用於在渲染 Thymeleaf 頁面時，
 * 自動將 permissionBean 放到 Model。
 * 作用域在controller層
 */
@Order(1)
@ControllerAdvice(basePackages = "com.portfolio.controller")
@RequiredArgsConstructor
public class GlobalModelAdviceHandler {

    private final ObjectMapper objectMapper;

    /**
     * 為每個頁面請求自動建立並注入權限物件
     * 在 Controller 執行前先準備好權限資料
     * @param model Spring MVC Model 物件
     * @param request HTTP 請求物件
     */
    @ModelAttribute
    public void addPermissionBean(Model model, HttpServletRequest request) {
        LogUtils.debug("=== GlobalModelAdviceHandler 權限處理開始 ===");

//        String ivCreds = request.getHeader(SystemConstants.IV_CREDS);
//        String myAuth = PermissionUtils.extractPermissionFromIvCreds(ivCreds);
        String myAuth = request.getHeader(SystemConstants.PERMISSION_SYSTEM_ID);

        // 測試Code
        myAuth = "8,8,15,32,15,32,15,32,15,32,15,32,15,32,15,32,9"; // 全開(看Excel: Portal權限表)

        PermissionBean permissionBean = null;
        String permissionJson = null;
        try {
            String uri = request.getRequestURI();
            LogUtils.debug("GlobalModelAdviceHandler start");
            LogUtils.debug("URI: {}, myAuth: {}", uri, myAuth);

            String[] parts = uri.split("/");
            String function = (parts.length > 2) ? parts[2] : ModuleConstants.MODULE_INDEX;
            String action = (parts.length > 3) ? parts[3] : ModuleConstants.PREFIX_MAIN;

            LogUtils.debug("function={}, action={}", function, action);

            if (ModuleConstants.MODULE_INDEX.equals(function) || function.isEmpty()) {
                // 首頁
				permissionBean = PermissionUtils.buildIndexPermissionBean(myAuth);
            } else {
                // 一般功能權限處理
                permissionBean = PermissionUtils.buildPermissionBean(myAuth, function, action);

                // 對於未知功能發出警告
                if (permissionBean != null && !ModuleConstants.API.equals(function)) {
                    int permissionIndex = PermissionUtils.findFunctionIndex(function, action);
                    if (permissionIndex < 0) {
                        LogUtils.bizWarn(String.format("功能名稱 '%s' 不存在", function));
                    }
                }
            }

            // JSON 序列化
            permissionJson = objectMapper.writeValueAsString(permissionBean);

        } catch (Exception e) {
            LogUtils.error("GlobalModelAdviceHandler.addPermissionBean() error", e);
            permissionBean = PermissionUtils.buildDefaultPermissionBean();
            permissionJson = "{}";  // 設定預設值
        }

        LogUtils.debug("最終 permissionBean: {}", permissionBean);
        LogUtils.debug("=== GlobalModelAdviceHandler 權限處理結束 ===");

        model.addAttribute("permissionJson", permissionJson);
        model.addAttribute(SystemConstants.PERMISSION_BEAN, permissionBean);
        request.setAttribute(SystemConstants.PERMISSION_BEAN, permissionBean);
    }

}
