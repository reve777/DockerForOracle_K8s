package com.portfolio.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Calendar;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.portfolio.entity.TStock;
import com.portfolio.entity.TwseQuote; 
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

    @Override
    @Transactional
    public List<TStock> refreshPrices() {
        List<TStock> list = tStockRepository.findAll();

        for (TStock ts : list) {
            String symbol = ts.getSymbol();
            try {
                TwseResponse twseResponse = fetchTwseQuote(symbol);

                if (twseResponse != null && twseResponse.getQuotes() != null && !twseResponse.getQuotes().isEmpty()) {
                    TwseQuote twseQuote = twseResponse.getQuotes().get(0);

                    mapQuoteToTStock(twseQuote, ts);

                    tStockRepository.updatePrice(
                            ts.getId(),
                            ts.getChangePrice(),
                            ts.getChangeInPercent(),
                            ts.getPreClosed(),
                            ts.getPrice(),
                            ts.getTransactionDate(),
                            ts.getVolumn());
                }
            } catch (Exception e) {
                System.err.println("更新股票 " + symbol + " 報價失敗: " + e.getMessage());
            }
        }
        return list;
    }

    @Override
    public List<HistoricalQuote> getHistoricalQuotes(String symbol) {
        // Yahoo Finance 目前對台股 API 較不穩定，如果是 0050 等台股請確保代號後綴正確 (如 0050.TW)
        if (symbol.contains(".TW") || symbol.contains(".tw")) {
            return fetchTaiwanStockHistory(symbol.toUpperCase());
        } else {
            return fetchForeignStockHistory(symbol);
        }
    }

    private List<HistoricalQuote> fetchTaiwanStockHistory(String symbol) {
        try {
            yahoofinance.Stock stock = yahoofinance.YahooFinance.get(symbol);
            if (stock == null) return Collections.emptyList();

            Calendar from = Calendar.getInstance();
            from.add(Calendar.YEAR, -1);
            
            List<yahoofinance.histquotes.HistoricalQuote> history = stock.getHistory(from, yahoofinance.histquotes.Interval.DAILY);
            if (history == null) return Collections.emptyList();

            List<HistoricalQuote> result = new ArrayList<>();
            for (yahoofinance.histquotes.HistoricalQuote hq : history) {
                HistoricalQuote quote = new HistoricalQuote();
                quote.date = new SimpleDateFormat("yyyy-MM-dd").format(hq.getDate().getTime());
                quote.open = hq.getOpen();
                quote.high = hq.getHigh();
                quote.low = hq.getLow();
                quote.close = hq.getClose();
                quote.volume = hq.getVolume() != null ? new BigDecimal(hq.getVolume()) : BigDecimal.ZERO;
                result.add(quote);
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<HistoricalQuote> fetchForeignStockHistory(String symbol) {
        try {
            String apiKey = "deb3e1449b34481f8e6d7e0e5366b217"; 
            String url = String.format(
                    "https://api.twelvedata.com/time_series?symbol=%s&interval=1day&outputsize=100&apikey=%s",
                    symbol, apiKey);

            Request request = new Request.Builder().url(url).build();
            try (Response response = CLIENT.newCall(request).execute()) {
                String jsonStr = response.body().string();
                TwelveDataResponse tdResp = objectMapper.readValue(jsonStr, TwelveDataResponse.class);
                if (tdResp.values == null) return Collections.emptyList();
                
                Collections.reverse(tdResp.values); 
                return tdResp.values;
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private TwseResponse fetchTwseQuote(String symbol) throws Exception {
        String twseSymbol = "tse_" + symbol + ".tw";
        String url = "https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=" + twseSymbol + "&json=1";

        Request request = new Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build();
        try (Response response = CLIENT.newCall(request).execute()) {
            return objectMapper.readValue(response.body().string(), TwseResponse.class);
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
            tStock.setChangeInPercent(changePrice.divide(prevClose, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
        } else {
            tStock.setChangePrice(BigDecimal.ZERO);
            tStock.setChangeInPercent(BigDecimal.ZERO);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TwseResponse {
        @JsonProperty("msgArray")
        private List<TwseQuote> quotes;
        public List<TwseQuote> getQuotes() { return quotes; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TwelveDataResponse {
        public List<HistoricalQuote> values;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoricalQuote {
        @JsonProperty("datetime")
        public String date;
        public BigDecimal open;
        public BigDecimal high;
        public BigDecimal low;
        public BigDecimal close;
        public BigDecimal volume;
    }
}