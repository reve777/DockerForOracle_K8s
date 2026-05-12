package com.portfolio.linepay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.linepay.util.LinePaySignatureUtil;
import com.portfolio.linepay.util.LinePayApiUris;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class LinePayService {

    @Value("${linepay.channel-id}")
    private String channelId;
    @Value("${linepay.channel-secret}")
    private String channelSecret;
    @Value("${linepay.api-url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 建立通用 Header (V4 簽章)
    private HttpHeaders createHeaders(String uri, String body) {
        String nonce = UUID.randomUUID().toString();
        String signature = LinePaySignatureUtil.encrypt(channelSecret, uri, body, nonce);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-LINE-ChannelId", channelId);
        headers.set("X-LINE-Authorization-Nonce", nonce);
        headers.set("X-LINE-Authorization", signature);
        return headers;
    }

    // 封裝 POST 請求
    private Map<String, Object> doPost(String uri, Object payload) throws Exception {
        String body = objectMapper.writeValueAsString(payload);
        HttpEntity<String> entity = new HttpEntity<>(body, createHeaders(uri, body));
        return restTemplate.postForObject(apiUrl + uri, entity, Map.class);
    }

    // 封裝 GET 請求 (GET 的 body 帶空字串)
    private Map<String, Object> doGet(String uri) throws Exception {
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders(uri, ""));
        return restTemplate.getForObject(apiUrl + uri, Map.class, entity);
    }

    // 1. 付款請求
    public Map<String, Object> requestPayment(Map<String, Object> payload) throws Exception {
        return doPost(LinePayApiUris.REQUEST, payload);
    }

    // 2. 查詢付款狀態
    public Map<String, Object> checkRequestStatus(String orderId) throws Exception {
        return doGet(String.format(LinePayApiUris.CHECK_REQUEST_STATUS, orderId));
    }

    // 3. 確認付款 (Confirm)
    public Map<String, Object> confirmPayment(String transactionId, Map<String, Object> payload) throws Exception {
        return doPost(String.format(LinePayApiUris.CONFIRM, transactionId), payload);
    }

    // 4. 請款 (Capture)
    public Map<String, Object> capturePayment(String transactionId, Map<String, Object> payload) throws Exception {
        return doPost(String.format(LinePayApiUris.CAPTURE, transactionId), payload);
    }

    // 5. 取消授權 (Void)
    public Map<String, Object> voidAuthorization(String transactionId) throws Exception {
        return doPost(String.format(LinePayApiUris.VOID_AUTH, transactionId), new HashMap<>());
    }

    // 6. 查詢付款明細 (範例：帶 transactionId 查詢)
    public Map<String, Object> getPaymentDetails(String transactionId) throws Exception {
        return doGet(LinePayApiUris.PAYMENT_DETAILS + "?transactionId=" + transactionId);
    }

    // 7. 退款
    public Map<String, Object> refundPayment(String transactionId, Map<String, Object> payload) throws Exception {
        return doPost(String.format(LinePayApiUris.REFUND, transactionId), payload);
    }

    // --- 預先授權付款 (Preapproved Pay) ---

    // 8. 檢查 regKey 狀態
    public Map<String, Object> checkPreapproved(String regKey) throws Exception {
        return doGet(String.format(LinePayApiUris.PREAPPROVED_CHECK, regKey));
    }

    // 9. 執行預先授權付款
    public Map<String, Object> preapprovedPay(String regKey, Map<String, Object> payload) throws Exception {
        return doPost(String.format(LinePayApiUris.PREAPPROVED_PAY, regKey), payload);
    }

    // 10. 刪除 regKey
    public Map<String, Object> expirePreapproved(String regKey) throws Exception {
        return doPost(String.format(LinePayApiUris.PREAPPROVED_EXPIRE, regKey), new HashMap<>());
    }
}