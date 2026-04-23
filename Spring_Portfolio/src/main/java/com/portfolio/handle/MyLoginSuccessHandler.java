package com.portfolio.handle;

import com.portfolio.repository.InvestorRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class MyLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final InvestorRepository investorRepository;

    public MyLoginSuccessHandler(InvestorRepository investorRepository) {
        this.investorRepository = investorRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        System.out.println("✅ MyLoginSuccessHandler (onAuthenticationSuccess) 被呼叫！");

        String username = authentication.getName();
        HttpSession session = request.getSession();
        session.setAttribute("investor_username", username);
        session.setMaxInactiveInterval(30 * 60); // 30 分鐘

        // 使用 FETCH JOIN 方法一次撈出相關資料
        investorRepository.findByUsernameWithWatches(username).ifPresentOrElse(investor -> {
            session.setAttribute("investor_id", investor.getId());

            if (investor.getWatches() != null && !investor.getWatches().isEmpty()) {
                session.setAttribute("watch_id", investor.getWatches().iterator().next().getId());
                System.out.println("Watch ID 已設置: " + session.getAttribute("watch_id"));
            }

            System.out.println("Session 已設置: investor_id=" + investor.getId() +
                    ", username=" + investor.getUsername());
        }, () -> {
            System.out.println("⚠️ 警告：使用者 (" + username + ") 尚未在 Investor 表建立資料。");
        });

        // 導向邏輯
        String redirectUrl = "/portfolio/page/home";

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            redirectUrl = "/portfolio/page/home";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
            redirectUrl = "/portfolio/page/home";
        }

        // 如果沒有 investor_id，可以在此處理導向完善資料頁面
        if (session.getAttribute("investor_id") == null) {
            System.out.println("ℹ️ 資訊：缺少 Investor ID，維持原導向。");
        }

        response.sendRedirect(redirectUrl);
    }
}