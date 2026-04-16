package com.portfolio.controller.api;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.entity.Investor;
import com.portfolio.entity.Portfolio;
import com.portfolio.entity.TStock;
import com.portfolio.entity.User;
import com.portfolio.repository.PortfolioRepository;
import com.portfolio.repository.TStockRepository;
import com.portfolio.service.OrderService;

@RestController
@RequestMapping("/order")
public class OrderController {

	private final OrderService orderService;

	private final PortfolioRepository portfolioRepository;

	private final TStockRepository tStockRepository;

	public OrderController(OrderService orderService, PortfolioRepository portfolioRepository,
			TStockRepository tStockRepository) {
		this.orderService = orderService;
		this.portfolioRepository = portfolioRepository;
		this.tStockRepository = tStockRepository;
	}

//	@GetMapping("/buy/{tstock_id}/{amount}")
//	public String buy(HttpSession session,
//			@PathVariable("tstock_id") Integer tstockId,
//			@PathVariable("amount") Integer amount) {
//		return orderService.buy(session, tstockId, amount);
//	}
	@Transactional
	@GetMapping("/buy/{tstock_id}/{amount}")
	public String buy(HttpSession session, Integer tstockId, Integer amount) {
		Investor investor = (Investor) session.getAttribute("investor");
		if (investor == null)
			return "請先登入";

		TStock tStock = tStockRepository.findById(tstockId)
				.orElseThrow(() -> new RuntimeException("找不到股票"));

		// 檢查是否已持有
		Optional<Portfolio> opt = portfolioRepository.findByInvestorAndTStock(investor, tStock);

		if (opt.isPresent()) {
			// --- 情況 A：更新 (UPDATE) ---
			Portfolio existing = opt.get();
			existing.setAmount(existing.getAmount() + amount);
			// 這裡不寫 setDate()，所以資料庫會維持「第一次買入」的時間
			// 如果你需要更新時間，才需要手動 setDate(new Date())

			portfolioRepository.save(existing);
			return "已更新數量";
		} else {
			// --- 情況 B：新增 (INSERT) ---
			Portfolio p = new Portfolio();
			p.setInvestor(investor);
			p.setTStock(tStock);
			p.setAmount(amount);
			// p.setDate(...) 不需要寫，因為 Entity 宣告時已經 = new Date()

			portfolioRepository.save(p);
			return "下單成功";
		}
	}

	@GetMapping("/sell/{id}/{amount}")
	public String sell(HttpSession session,
			@PathVariable("id") Integer portfolioId,
			@PathVariable("amount") Integer amount) {
		return orderService.sell(session, portfolioId, amount);
	}
}
