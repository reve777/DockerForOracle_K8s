package com.portfolio.filter;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.portfolio.repository.InvestorRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LoginFilter extends OncePerRequestFilter {

    @Autowired
    private InvestorRepository investorRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String investorIdParam = request.getParameter("investor_id");

        if (investorIdParam != null && !investorIdParam.isEmpty()) {
            try {
                int id = Integer.parseInt(investorIdParam);

                // 💡 修正重點：方法名稱改為 findByIdWithWatches (對齊 Repository)
                investorRepository.findByIdWithWatches(id).ifPresent(investor -> {
                    session.setAttribute("investor_id", investor.getId());
                    session.setAttribute("investor_username", investor.getUsername());

                    // 💡 修正重點：getWatchs() 改為 getWatches() (對齊 Entity)
                    if (investor.getWatches() != null && !investor.getWatches().isEmpty()) {
                        session.setAttribute("watch_id", investor.getWatches().iterator().next().getId());
                    }

                    session.setAttribute("investor", investor);
                    System.out.println("✅ [Filter] 成功切換身份為: " + investor.getUsername());
                });
            } catch (NumberFormatException e) {
                System.err.println("❌ 投資人 ID 格式錯誤: " + investorIdParam);
            }
        }

        chain.doFilter(request, response);
    }
}