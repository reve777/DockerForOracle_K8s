package com.portfolio.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; 
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略 TWSE JSON 中所有未定義的欄位
public class TwseQuote {

	// --- JSON 欄位對應 ---
	@JsonProperty("z") // 'z' = 最新成交價 (Price)
	private String priceStr;

	@JsonProperty("y") // 'y' = 昨日收盤價 (Previous Close)
	private String previousCloseStr;

	@JsonProperty("v") // 'v' = 總成交量 (Volume)
	private String volumeStr;

	@JsonProperty("t") // 't' = 最後成交時間 (e.g., "10:11:59")
	private String tradeTime;

	@JsonProperty("d") // 'd' = 交易日期 (e.g., "20251126")
	private String tradeDate;

	// --- 輔助方法：處理 String 轉 BigDecimal/Long/Date ---

	private BigDecimal safeParseBigDecimal(String value) {
		if (value == null || value.equals("-") || value.trim().isEmpty()) { 
			return BigDecimal.ZERO; 
		}
		try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
	}

	public BigDecimal getPrice() {
		return safeParseBigDecimal(priceStr);
	}

	public BigDecimal getPreviousClose() {
		return safeParseBigDecimal(previousCloseStr);
	}

	public Long getVolume() {
		if (volumeStr == null || volumeStr.equals("-") || volumeStr.trim().isEmpty()) {
			return 0L;
		}
		try {
            // TWSE MIS 的成交量 V 欄位是張數，需要乘以 1000 才是股數
            return Long.parseLong(volumeStr) * 1000L;
        } catch (NumberFormatException e) {
            return 0L;
        }
	}

	public Date getTransactionDate() {
		// 只有 priceStr 非 "-" 且日期時間都存在時，時間才有意義
		if (tradeDate == null || tradeTime == null || priceStr == null || priceStr.equals("-"))
			return null;
            
		try {
			// 【修正點】：將格式字串從 "yyyyMMddHH:mm:ss" 改為 "yyyyMMddHHmmss"
            // 以匹配 tradeDate + tradeTime.replaceAll(":", "") 產生的無冒號字串 (e.g., "20251126104634")
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); 
			
			// 設定時區為臺北時區 (CST)
			sdf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
			
			return sdf.parse(tradeDate + tradeTime.replaceAll(":", ""));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}