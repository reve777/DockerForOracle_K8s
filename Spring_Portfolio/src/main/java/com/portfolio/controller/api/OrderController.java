package com.portfolio.controller.api;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.service.OrderService;

@RestController
@RequestMapping("/order")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping("/buy/{tstock_id}/{amount}")
	public String buy(HttpSession session,
			@PathVariable("tstock_id") Integer tstockId,
			@PathVariable("amount") Integer amount) {
		return orderService.buy(session, tstockId, amount);
	}

	@GetMapping("/sell/{id}/{amount}")
	public String sell(HttpSession session,
			@PathVariable("id") Integer portfolioId,
			@PathVariable("amount") Integer amount) {
		return orderService.sell(session, portfolioId, amount);
	}
}
