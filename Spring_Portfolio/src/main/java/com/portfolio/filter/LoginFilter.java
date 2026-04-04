package com.portfolio.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.portfolio.entity.Investor;
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

        if (investorIdParam != null) {
            try {
                int id = Integer.parseInt(investorIdParam);
                investorRepository.findById(id).ifPresent(investor -> {
                    session.setAttribute("investor_username", investor.getUsername());
                    session.setAttribute("investor_id", investor.getId());

                    if (investor.getWatchs() != null && !investor.getWatchs().isEmpty()) {
                        session.setAttribute("watch_id", investor.getWatchs().iterator().next().getId());
                    }

                    session.setAttribute("investor", investor);
                    System.out.println("session investor = " + investor);
                });
            } catch (NumberFormatException e) {
                System.err.println("投資人 ID 格式錯誤: " + investorIdParam);
            }
        }

        chain.doFilter(request, response);
    }
}
