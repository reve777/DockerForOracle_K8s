package com.portfolio.handle;

import com.portfolio.repository.InvestorRepository;
import com.portfolio.entity.Investor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true) // 💡 確保在讀取 LAZY 集合時 Session 是開啟的
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
                                        throws IOException, ServletException {

        System.out.println("✅ [SuccessHandler] 認證成功，正在初始化 Session...");

        String username = authentication.getName();
        HttpSession session = request.getSession();
        
        // 基本資訊設定
        session.setAttribute("investor_username", username);
        session.setMaxInactiveInterval(30 * 60); // 30 分鐘

        // 💡 使用我們優化過的 JOIN FETCH 方法一次撈出關聯資料
        investorRepository.findByUsernameWithWatches(username).ifPresentOrElse(investor -> {
            
            // 存入必要的識別 ID
            session.setAttribute("investor_id", investor.getId());

            // 處理預設顯示的觀察名單 ID (取第一筆)
            if (investor.getWatches() != null && !investor.getWatches().isEmpty()) {
                Integer firstWatchId = investor.getWatches().iterator().next().getId();
                session.setAttribute("watch_id", firstWatchId);
                System.out.println("📌 Watch ID 已設置: " + firstWatchId);
            }

            // ⚠️ 備註：業界通常不建議將整個 Entity 塞進 Session
            // 如果後端 JSP/Thymeleaf 真的需要整個物件，才保留這一行：
            session.setAttribute("investor", investor);

            System.out.println("👤 使用者數據已載入: ID=" + investor.getId());
            
        }, () -> {
            System.err.println("⚠️ 警告：Spring Security 帳號 (" + username + ") 在 Investor 表中不存在！");
        });

        // 權限導向邏輯
        String redirectUrl = determineTargetUrl(authentication);
        
        // 額外檢查：如果還是沒有 investor_id，可能需要導向建立資料頁面
        if (session.getAttribute("investor_id") == null) {
             System.out.println("ℹ️ 資訊：缺少 Investor 關聯資料，準備跳轉...");
             // 可視需求調整導向至 /profile/setup 等
        }

        response.sendRedirect(request.getContextPath() + redirectUrl);
    }

    /**
     * 💡 提取導向邏輯，讓代碼更整潔
     */
    private String determineTargetUrl(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"))
                ? "/page/home" 
                : "/page/home"; // 目前你的邏輯兩者相同，保留彈性可隨時修改
    }
}