package com.portfolio.service.impl;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.entity.Investor;
import com.portfolio.entity.Portfolio;
import com.portfolio.entity.TStock;
import com.portfolio.repository.InvestorRepository;
import com.portfolio.repository.PortfolioRepository;
import com.portfolio.repository.TStockRepository;
import com.portfolio.service.OrderService;

import jakarta.servlet.http.HttpSession;

@Service
public class OrderServiceImpl implements OrderService {

	private final InvestorRepository investorRepository;
	private final TStockRepository tStockRepository;
	private final PortfolioRepository portfolioRepository;

	public OrderServiceImpl(InvestorRepository investorRepository,
			TStockRepository tStockRepository,
			PortfolioRepository portfolioRepository) {
		this.investorRepository = investorRepository;
		this.tStockRepository = tStockRepository;
		this.portfolioRepository = portfolioRepository;
	}

	@Override
	@Transactional
	public String buy(HttpSession session, Integer tstockId, Integer amount) {
		Integer investorId = (Integer) session.getAttribute("investor_id");
		Investor investor = investorRepository.findById(investorId).orElse(null);
		if (investor == null)
			return "Investor None";

		TStock ts = tStockRepository.findById(tstockId).orElse(null);
		if (ts == null)
			return "TStock None";

		int buyTotalCost = (int) (ts.getPrice().doubleValue() * amount);
		int balance = investor.getBalance() - buyTotalCost;
		if (balance < 0)
			return "Insufficient balance";

		investor.setBalance(balance);

		Portfolio po = new Portfolio();
		po.setInvestor(investor);
		po.setTStock(ts);
		po.setCost(ts.getPrice().doubleValue());
		po.setAmount(amount);
		po.setDate(new Date());

		investorRepository.saveAndFlush(investor);
		portfolioRepository.saveAndFlush(po);

		return po.getId() + "";
	}

	@Override
	@Transactional
	public String sell(HttpSession session, Integer portfolioId, Integer amount) {
		Integer investorId = (Integer) session.getAttribute("investor_id");
		Investor investor = investorRepository.findById(investorId).orElse(null);
		if (investor == null)
			return "Investor None";

		Portfolio po = portfolioRepository.findById(portfolioId).orElse(null);
		if (po == null)
			return "Portfolio None";

		if (po.getAmount() < amount)
			return "Insufficient stock amount";

		po.setAmount(po.getAmount() - amount);
		int profit = (int) (amount * po.getTStock().getPrice().doubleValue());
		investor.setBalance(investor.getBalance() + profit);

		investorRepository.saveAndFlush(investor);
		portfolioRepository.saveAndFlush(po);

		return "OK";
	}
}
