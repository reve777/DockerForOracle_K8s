package com.portfolio.linepay.controller;

import com.portfolio.linepay.service.LinePayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/linepay") // 回歸簡單路徑，避免 404 錯誤
public class LinePayController {

    @Autowired
    private LinePayService linePayService;

    @Value("${linepay.return-url}")
    private String returnUrl;

    @Value("${linepay.cancel-url}")
    private String cancelUrl;

    // 1. 前端發起結帳 (Request)
    @PostMapping("/checkout")
    @ResponseBody
    public Map<String, Object> checkout(@RequestBody(required = false) Map<String, Object> customData) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", 100);
        payload.put("currency", "TWD");
        payload.put("orderId", "ORD_" + System.currentTimeMillis());

        Map<String, Object> product = new HashMap<>();
        product.put("name", "V4測試商品");
        product.put("quantity", 1);
        product.put("price", 100);

        Map<String, Object> pkg = new HashMap<>();
        pkg.put("id", "PKG_1");
        pkg.put("amount", 100);
        pkg.put("products", Collections.singletonList(product));
        payload.put("packages", Collections.singletonList(pkg));

        Map<String, Object> redirectUrls = new HashMap<>();
        redirectUrls.put("confirmUrl", returnUrl);
        redirectUrls.put("cancelUrl", cancelUrl);
        payload.put("redirectUrls", redirectUrls);

        Map<String, Object> options = new HashMap<>();
        Map<String, Object> payment = new HashMap<>();
        payment.put("payType", "NORMAL"); 
        options.put("payment", payment);
        payload.put("options", options);

        // 直接回傳 Service 結果，不要包 successResponse
        return linePayService.requestPayment(payload);
    }

    // 2. 查詢付款請求狀態
    @GetMapping("/requests/{orderId}/check")
    @ResponseBody
    public Map<String, Object> checkRequestStatus(@PathVariable String orderId) throws Exception {
        return linePayService.checkRequestStatus(orderId);
    }

    // 3. LINE Pay 導回處理 (Confirm)
    @GetMapping("/confirm")
    public String confirm(@RequestParam String transactionId, @RequestParam String orderId, Model model) throws Exception {
        Map<String, Object> confirmPayload = new HashMap<>();
        confirmPayload.put("amount", 100);
        confirmPayload.put("currency", "TWD");

        Map<String, Object> response = linePayService.confirmPayment(transactionId, confirmPayload);

        // 確保讀取的是 LINE Pay 原始的 returnCode
        if ("0000".equals(response.get("returnCode"))) {
            model.addAttribute("status", "success");
            model.addAttribute("message", "付款成功");
        } else {
            model.addAttribute("status", "fail");
            model.addAttribute("message", "付款失敗：" + response.get("returnMessage"));
        }
        model.addAttribute("orderId", orderId);
        model.addAttribute("transactionId", transactionId);
        System.out.println("transactionId ==== "+ transactionId);
        return "linepay_result"; 
    }

    // 4. 請款 (Capture)
    @PostMapping("/capture/{transactionId}")
    @ResponseBody
    public Map<String, Object> capture(@PathVariable String transactionId, @RequestBody Map<String, Object> payload) throws Exception {
        return linePayService.capturePayment(transactionId, payload);
    }

    // 5. 取消授權 (Void)
    @PostMapping("/void/{transactionId}")
    @ResponseBody
    public Map<String, Object> voidAuth(@PathVariable String transactionId) throws Exception {
        return linePayService.voidAuthorization(transactionId);
    }

    // 6. 查詢付款明細
    @GetMapping("/details")
    @ResponseBody
    public Map<String, Object> getDetails(@RequestParam String transactionId) throws Exception {
        return linePayService.getPaymentDetails(transactionId);
    }

    // 7. 退款 (Refund)
    @PostMapping("/refund/{transactionId}")
    @ResponseBody
    public Map<String, Object> refund(@PathVariable String transactionId, @RequestBody Map<String, Object> payload) throws Exception {
        return linePayService.refundPayment(transactionId, payload);
    }

    // --- 預先授權付款 (Preapproved Pay) ---

    // 8. 檢查 regKey 狀態
    @GetMapping("/preapproved/{regKey}/check")
    @ResponseBody
    public Map<String, Object> checkRegKey(@PathVariable String regKey) throws Exception {
        return linePayService.checkPreapproved(regKey);
    }

    // 9. 執行預先授權付款
    @PostMapping("/preapproved/{regKey}/payment")
    @ResponseBody
    public Map<String, Object> preapprovedPay(@PathVariable String regKey, @RequestBody Map<String, Object> payload) throws Exception {
        return linePayService.preapprovedPay(regKey, payload);
    }

    // 10. 刪除 regKey
    @PostMapping("/preapproved/{regKey}/expire")
    @ResponseBody
    public Map<String, Object> expireRegKey(@PathVariable String regKey) throws Exception {
        return linePayService.expirePreapproved(regKey);
    }
}