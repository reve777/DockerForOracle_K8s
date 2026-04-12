package com.portfolio.controller.pay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EcpayService {

    @Value("${ecpay.url}")
    private String ecpayUrl;

    public String createOrder() {
    	//3002607	pwFHCqoQZGmho4w6	EkRm7iFT261dpevs一般特店
    	
        // ⚠️ 目前使用綠界官方測試帳號，正式上線請換成自己的帳號資訊
        String cleanMid = "3002607";
        String cleanKey = "pwFHCqoQZGmho4w6";
        String cleanIv  = "EkRm7iFT261dpevs";

        Map<String, String> params = new HashMap<>();

        // TP(2) + 毫秒後9碼(9) + 4位隨機(4) = 15碼，不超過綠界上限20碼
        long ms = System.currentTimeMillis();
        String tradeNo = String.format("TP%09d%04d",
                ms % 1_000_000_000L,
                (int)(Math.random() * 9999));

        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

        params.put("MerchantID", cleanMid);
        params.put("MerchantTradeNo", tradeNo);
        params.put("MerchantTradeDate", date);
        params.put("PaymentType", "aio");
        params.put("TotalAmount", "100");
        params.put("TradeDesc", "test");
        params.put("ItemName", "item");
        params.put("ReturnURL", "https://www.ecpay.com.tw/");
        params.put("OrderResultURL", "https://www.ecpay.com.tw/");
        params.put("ChoosePayment", "ALL");
        params.put("EncryptType", "1");

        String checkMacValue = generateCheckMacValue(params, cleanKey, cleanIv);
        params.put("CheckMacValue", checkMacValue);

        return generateAutoSubmitForm(params, "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5");
    }

    private String generateCheckMacValue(Map<String, String> params, String k, String iv) {
        TreeMap<String, String> sorted = new TreeMap<>(params);

        // 只對 value 做 URL Encode，= 和 & 本身不能被 encode
        StringBuilder sb = new StringBuilder();
        sb.append("HashKey=").append(k);
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            sb.append("&")
              .append(entry.getKey())
              .append("=")
              .append(urlEncodeValue(entry.getValue()));
        }
        sb.append("&HashIV=").append(iv);

        // 整串轉小寫 -> SHA256 -> 大寫
        String finalToHash = sb.toString().toLowerCase();

        return sha256(finalToHash).toUpperCase();
    }

    /**
     * 模擬 PHP urlencode 行為：
     * - 空格 -> +（不換成 %20，與綠界 PHP SDK 一致）
     * - 其餘特殊字元正常 encode
     */
    private String urlEncodeValue(String value) {
        try {
            String result = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
            return result
                .replace("%21", "!")
                .replace("%28", "(")
                .replace("%29", ")")
                // 不把 + 換成 %20，保留 + 與綠界 PHP SDK 一致
                .replace("%2A", "*")
                .replace("%2a", "*");
        } catch (Exception e) {
            return value;
        }
    }

    private String sha256(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                String h = Integer.toHexString(0xFF & b);
                if (h.length() == 1) hex.append("0");
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String generateAutoSubmitForm(Map<String, String> params, String actionUrl) {
        StringBuilder form = new StringBuilder();
        form.append("<html><head><meta charset='UTF-8'></head><body>");
        form.append("<form id='ecpayForm' method='post' action='").append(actionUrl).append("'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            form.append("<input type='hidden' name='").append(entry.getKey())
                .append("' value='").append(entry.getValue()).append("'/>");
        }
        form.append("</form>");
        form.append("<script>document.getElementById('ecpayForm').submit();</script>");
        form.append("</body></html>");
        return form.toString();
    }
}