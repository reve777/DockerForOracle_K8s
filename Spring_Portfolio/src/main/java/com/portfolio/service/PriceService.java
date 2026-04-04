package com.portfolio.service;

import java.util.List;

import com.portfolio.entity.TStock;
// 移除: import yahoofinance.histquotes.HistoricalQuote;

public interface PriceService {
	List<TStock> refreshPrices();
	
	/**
	 * 獲取指定股票的歷史報價。
	 * 由於 yahoofinance.HistoricalQuote 類別已移除，回傳類型變更為 List<?>。
	 * 在整合新的歷史數據 API 時，請將此處的 List<?> 替換為您定義的新歷史報價實體。
	 * @param symbol 股票代號
	 * @return 歷史報價列表
	 */
	List<?> getHistoricalQuotes(String symbol);
}