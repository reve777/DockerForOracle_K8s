package com.portfolio.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.entity.TStock;
import com.portfolio.service.PriceService;

import yahoofinance.histquotes.HistoricalQuote;

@RestController
@RequestMapping("/price")
public class PriceController {

	private final PriceService priceService;

	public PriceController(PriceService priceService) {
		this.priceService = priceService;
	}

	@GetMapping("/refresh")
	public List<TStock> refresh() {
		return priceService.refreshPrices();
	}

	@GetMapping("/histquotes/{symbol:.+}")
	// 【重要修改】：將回傳類型從 List<HistoricalQuote> 改為 List<?>
	public List<?> queryHistoricalQuotes(@PathVariable("symbol") String symbol) {
		System.out.println("symbol==== : " +symbol);
		return priceService.getHistoricalQuotes(symbol);
	}
}