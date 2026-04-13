package com.portfolio.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/**
 * 全域 Controller Advice 為每個 Thymeleaf 頁面請求自動注入共通 Model 屬性， 例如頁面標題、目前登入使用者名稱。
 */
@Slf4j
@Order(2)
@ControllerAdvice
public class GlobalControllerAdvice {

	/**
	 * 自動將 Session 中的投資人資訊注入每個頁面的 Model
	 */
	@ModelAttribute
	public void addSessionAttributes(HttpSession session, Model model) {
		model.addAttribute("investorId", session.getAttribute("investor_id"));
		model.addAttribute("username", session.getAttribute("investor_username"));
		model.addAttribute("watchId", session.getAttribute("watch_id"));
	}

	@ModelAttribute("currentUser")
	public String addCurrentUser(Principal principal) {
		return (principal != null) ? principal.getName() : null;
	}

	/**
	 * 處理帶有 /page 前綴的標題邏輯
	 */
	@ModelAttribute("pageTitle")
	public String addPageTitle(HttpServletRequest request) {
		String uri = request.getRequestURI();

		// 如果是純根路徑或首頁
		if (uri == null || uri.equals("/") || uri.equals("/page/home")) {
			return "首頁";
		}

		// 使用 contains 或 endsWith 判斷，避免被 /page/ 影響
		if (uri.contains("/portfolio"))
			return "投資組合";
		if (uri.contains("/watch"))
			return "自選股清單";
		if (uri.contains("/stock"))
			return "股票查詢";
		if (uri.contains("/asset"))
			return "資產概況";
		if (uri.contains("/profile"))
			return "個人設定";
		if (uri.contains("/login"))
			return "登入";
		if (uri.contains("/investor"))
			return "投資人管理";

		log.debug("[GlobalControllerAdvice] 未定義路徑: {}", uri);
		return "投資人入口";
	}
}