package com.portfolio.bank.controller;

import com.portfolio.bank.dto.TransferRequest;
import com.portfolio.bank.entity.Account;
import com.portfolio.bank.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bank")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    // 優化前：public ApiResponse<BigDecimal> ... return ApiResponse.success(balance);
    // 優化後：直接回傳 BigDecimal，Advice 會幫你包成 {"code":200, "data": 100.0}
    @GetMapping("/balance/{accountId}")
    public BigDecimal getBalance(@PathVariable String accountId) {
        Account acc = bankService.getAccount(accountId);
        if (acc == null) throw new IllegalArgumentException("找不到該帳戶");
        return acc.getBalance();
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferRequest request) {
        bankService.transfer(request.getFromAccountId(), request.getToAccountId(), request.getAmount());
        return "轉帳成功"; // Advice 會處理 String 的轉 JSON 邏輯，前端收到 {"code":200, "data":"轉帳成功"}
    }

    /**
     * 壓力測試接口
     * 注意：這裡故意不加 @Transactional 與 @Retryable
     * 目的是觀察 Service 層在極端併發下的重試表現
     */
    @PostMapping("/stress-test")
    public String stressTest(@RequestBody TransferRequest request) {
        // 1. 不要自己 catch，讓異常往外拋給 GlobalExceptionHandler
        // 2. 移除 try-catch 後，代碼會變得非常乾淨
        bankService.transfer(request.getFromAccountId(), request.getToAccountId(), request.getAmount());
        
        // 3. 如果能執行到這行，代表成功，回傳統一格式
        return "壓測單筆執行成功";
    }
}