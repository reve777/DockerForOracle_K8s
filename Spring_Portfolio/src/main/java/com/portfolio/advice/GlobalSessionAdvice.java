package com.portfolio.advice;

import com.portfolio.repository.InvestorRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.Optional;

/**
 * 全域 Session 補填處理器
 * 當使用者透過 Remember-Me 自動登入時，Principal 存在但 Session 尚未初始化，
 * 此 Advice 負責從資料庫補抓使用者資料並填入 Session。
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalSessionAdvice {

    private final InvestorRepository investorRepository;

    @ModelAttribute
    public void handleRememberMeSession(Principal principal, HttpSession session) {
        // 如果已認證但 Session 尚未填入 investor_id，表示是 Remember-Me 登入
        if (principal == null || session.getAttribute("investor_id") != null) {
            return;
        }

        String username = principal.getName();
        try {
            investorRepository.findByUsernameWithWatchs(username).ifPresent(investor -> {
                session.setAttribute("investor_id", investor.getId());
                session.setAttribute("investor_username", investor.getUsername());

                // 安全取得第一個 Watch ID（避免 null 或空集合）
                Optional.ofNullable(investor.getWatchs())
                        .filter(w -> !w.isEmpty())
                        .map(w -> w.iterator().next().getId())
                        .ifPresent(watchId -> session.setAttribute("watch_id", watchId));

                log.info("[GlobalSessionAdvice] Session 補填完成，使用者: {}", username);
            });
        } catch (Exception e) {
            // Session 補填失敗不應中斷請求流程，僅記錄警告
            log.warn("[GlobalSessionAdvice] Session 補填失敗，使用者: {}，原因: {}", username, e.getMessage(), e);
        }
    }
}