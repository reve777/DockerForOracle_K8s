package com.portfolio.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Calendar;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 引入 OkHttp 和 Jackson
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // 【保留】用於 TwseResponse

import com.portfolio.entity.TStock;
import com.portfolio.entity.TwseQuote; // 使用外部 TwseQuote 實體
import com.portfolio.repository.TStockRepository;
import com.portfolio.service.PriceService;

@Service
public class PriceServiceImpl implements PriceService {

	private final TStockRepository tStockRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private static final OkHttpClient CLIENT = new OkHttpClient();

	public PriceServiceImpl(TStockRepository tStockRepository) {
		this.tStockRepository = tStockRepository;
	}

	// ----------------------------------------------------------------------
	// 1. 即時報價更新 (取代 YahooFinance.get())
	// ----------------------------------------------------------------------

	@Override
	@Transactional
	public List<TStock> refreshPrices() {
		List<TStock> list = tStockRepository.findAll();

		for (TStock ts : list) {
			String symbol = ts.getSymbol();
			try {
				// 1. 呼叫 TWSE 接口獲取報價
				TwseResponse twseResponse = fetchTwseQuote(symbol);

				if (twseResponse != null && twseResponse.getQuotes() != null && !twseResponse.getQuotes().isEmpty()) {
					// 使用外部定義的 TwseQuote
					TwseQuote twseQuote = twseResponse.getQuotes().get(0);

					// 2. 映射數據並計算衍生欄位
					mapQuoteToTStock(twseQuote, ts);

					// 3. 儲存更新後的報價
					tStockRepository.updatePrice(
							ts.getId(),
							ts.getChangePrice(),
							ts.getChangeInPercent(),
							ts.getPreClosed(),
							ts.getPrice(),
							ts.getTransactionDate(),
							ts.getVolumn());
				} else {
					System.err.println("TWSE MIS 接口未回傳股票代號 " + symbol + " 的報價數據。");
				}
			} catch (Exception e) {
				System.err.println("更新股票代號 " + symbol + " 報價失敗: " + e.getMessage());
				// e.printStackTrace();
			}
		}
		return list;
	}

	// ----------------------------------------------------------------------
	// 2. 歷史報價獲取 (目前為空實作，待整合其他 API)
	// ----------------------------------------------------------------------

	@Override
	public List<HistoricalQuote> getHistoricalQuotes(String symbol) {
		// 判斷是否為台股（以 .TW 結尾）
		if (symbol.endsWith(".TW")) {
			return fetchTaiwanStockHistory(symbol);
		} else {
			return fetchForeignStockHistory(symbol);
		}
	}

	// --------------------
	// 1. 台股歷史資料 (Yahoo Finance)
	private List<HistoricalQuote> fetchTaiwanStockHistory(String symbol) {
		try {
			// YahooFinance API Java 套件
			yahoofinance.Stock stock = yahoofinance.YahooFinance.get(symbol);
			if (stock == null)
				return Collections.emptyList();

			// 取得最近一年歷史資料
			Calendar from = Calendar.getInstance();
			from.add(Calendar.YEAR, -1);
			Calendar to = Calendar.getInstance();

			List<yahoofinance.histquotes.HistoricalQuote> history = stock.getHistory(from, to,
					yahoofinance.histquotes.Interval.DAILY);
			if (history == null || history.isEmpty())
				return Collections.emptyList();

			List<HistoricalQuote> result = new ArrayList<>();
			for (yahoofinance.histquotes.HistoricalQuote hq : history) {
				HistoricalQuote quote = new HistoricalQuote();
				quote.date = new SimpleDateFormat("yyyy-MM-dd").format(hq.getDate().getTime());
				quote.open = hq.getOpen();
				quote.high = hq.getHigh();
				quote.low = hq.getLow();
				quote.close = hq.getClose();
				quote.adjClose = hq.getAdjClose() != null ? hq.getAdjClose() : hq.getClose();
				quote.volume = hq.getVolume() != null ? new BigDecimal(hq.getVolume()) : BigDecimal.ZERO;
				result.add(quote);
			}
			return result;
		} catch (Exception e) {
			System.err.println("抓取台股歷史資料失敗: " + e.getMessage());
			return Collections.emptyList();
		}
	}

	// --------------------
	// 2. 國外股歷史資料 (Twelve Data)
	private List<HistoricalQuote> fetchForeignStockHistory(String symbol) {
		try {
			String apiKey = "deb3e1449b34481f8e6d7e0e5366b217"; // 你的 API Key
			String interval = "1day";

			Calendar from = Calendar.getInstance();
			from.add(Calendar.YEAR, -1);
			Calendar to = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			String startDate = sdf.format(from.getTime());
			String endDate = sdf.format(to.getTime());

			String url = String.format(
					"https://api.twelvedata.com/time_series?symbol=%s&interval=%s&start_date=%s&end_date=%s&apikey=%s",
					symbol, interval, startDate, endDate, apiKey);

			Request request = new Request.Builder()
					.url(url)
					.header("User-Agent", "Mozilla/5.0")
					.build();

			try (Response response = CLIENT.newCall(request).execute()) {
				if (!response.isSuccessful()) {
					System.err.println("HTTP request failed: " + response.code());
					return Collections.emptyList();
				}

				String jsonStr = response.body().string();
				TwelveDataResponse tdResp = objectMapper.readValue(jsonStr, TwelveDataResponse.class);

				if (tdResp.values == null || tdResp.values.isEmpty())
					return Collections.emptyList();

				Collections.reverse(tdResp.values); // 由最早日期到最新
				for (HistoricalQuote hq : tdResp.values) {
					hq.adjClose = hq.close; // 前端 K 線圖需要
				}
				return tdResp.values;
			}
		} catch (Exception e) {
			System.err.println("抓取國外股歷史資料失敗: " + e.getMessage());
			return Collections.emptyList();
		}
	}

	// ----------------------------------------------------------------------
	// 3. 輔助方法
	// ----------------------------------------------------------------------

	private TwseResponse fetchTwseQuote(String symbol) throws Exception {
		String twseSymbol = "tse_" + symbol + ".tw";
		String url = String.format(
				"https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=%s&json=1&delay=0",
				twseSymbol);

		Request request = new Request.Builder()
				.url(url)
				.header("User-Agent", "Mozilla/5.0")
				.build();

		try (Response response = CLIENT.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new Exception("HTTP 請求失敗: " + response.code());
			}
			String jsonString = response.body().string();
			return objectMapper.readValue(jsonString, TwseResponse.class);
		}
	}

	private void mapQuoteToTStock(TwseQuote twseQuote, TStock tStock) {
		BigDecimal currentPrice = twseQuote.getPrice();
		BigDecimal prevClose = twseQuote.getPreviousClose();

		tStock.setPreClosed(prevClose);
		tStock.setPrice(currentPrice);
		tStock.setVolumn(twseQuote.getVolume());
		tStock.setTransactionDate(twseQuote.getTransactionDate());

		if (currentPrice.compareTo(BigDecimal.ZERO) > 0 && prevClose.compareTo(BigDecimal.ZERO) > 0) {

			BigDecimal changePrice = currentPrice.subtract(prevClose);
			tStock.setChangePrice(changePrice);

			BigDecimal changeInPercent = changePrice.divide(prevClose, 4, RoundingMode.HALF_UP)
					.multiply(new BigDecimal("100"));
			tStock.setChangeInPercent(changeInPercent);
		} else {
			tStock.setChangePrice(BigDecimal.ZERO);
			tStock.setChangeInPercent(BigDecimal.ZERO);
		}
	}

	// ----------------------------------------------------------------------
	// 4. 內部類別：JSON 數據結構定義
	// ----------------------------------------------------------------------

	// 1. 外層：對應整個 JSON 回應
	@JsonIgnoreProperties(ignoreUnknown = true) // 【重要修正】忽略最外層如 "referer" 等未定義欄位
	private static class TwseResponse {
		@JsonProperty("msgArray")
		private List<TwseQuote> quotes;

		public List<TwseQuote> getQuotes() {
			return quotes;
		}
	}

	// 2. 內層：TwseQuote 類別已移至 com.portfolio.entity 套件
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class TwelveDataResponse {
		@JsonProperty("values")
		public List<HistoricalQuote> values;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class HistoricalQuote {
		@JsonProperty("datetime")
		public String date; // 格式 yyyy-MM-dd
		public BigDecimal open;
		public BigDecimal high;
		public BigDecimal low;
		public BigDecimal close;
		public BigDecimal volume;

		@JsonProperty("close")
		public BigDecimal adjClose; // 為前端 K 線圖需要
	}

}
