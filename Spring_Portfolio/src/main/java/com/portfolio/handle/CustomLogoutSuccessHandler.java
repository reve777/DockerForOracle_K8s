package com.portfolio.handle;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            // 移除自訂的 Session 屬性
            session.removeAttribute("investor_id");
            session.removeAttribute("investor_username");
            session.removeAttribute("watch_id");
            session.invalidate(); // 徹底銷毀 Session
        }

        System.out.println("用戶已登出，自訂 Session 屬性已清除");

        // ✅ 使用 Context Path 導向登入頁並帶上 logout 參數
        response.sendRedirect(request.getContextPath() + "/loginpage?logout=true");
    }
}