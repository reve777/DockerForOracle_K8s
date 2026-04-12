package com.portfolio.handle;

import com.portfolio.repository.InvestorRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.security.Principal;

@ControllerAdvice
public class GlobalSessionAdvice {

    @Autowired
    private InvestorRepository investorRepository;

    @ModelAttribute
    public void handleRememberMeSession(Principal principal, HttpSession session) {
        // 💡 如果已經認證(Principal) 但 Session 裡沒有 ID，就去資料庫補抓
        if (principal != null && session.getAttribute("investor_id") == null) {
            String username = principal.getName();
            
            investorRepository.findByUsernameWithWatchs(username).ifPresent(investor -> {
                session.setAttribute("investor_id", investor.getId());
                session.setAttribute("investor_username", investor.getUsername());
                
                if (investor.getWatchs() != null && !investor.getWatchs().isEmpty()) {
                    session.setAttribute("watch_id", investor.getWatchs().iterator().next().getId());
                }
                System.out.println("🔄 [GlobalAdvice] 已補填 Remember-Me 用戶之 Session: " + username);
            });
        }
    }
}