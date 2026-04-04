package com.portfolio.service;

import jakarta.servlet.http.HttpSession;

public interface OrderService {
    String buy(HttpSession session, Integer tstockId, Integer amount);
    String sell(HttpSession session, Integer portfolioId, Integer amount);
}
