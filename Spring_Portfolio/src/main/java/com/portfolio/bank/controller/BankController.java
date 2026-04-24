package com.portfolio.bank.controller;


import org.springframework.web.bind.annotation.*;

import com.portfolio.bank.dto.ApiResponse;
import com.portfolio.bank.dto.TransferRequest;
import com.portfolio.bank.entity.Account;
import com.portfolio.bank.service.BankService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/bank")
@CrossOrigin(origins = "*") // 允許前端呼叫
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    // 取得帳戶餘額 API
    @GetMapping("/balance/{accountId}")
    public ApiResponse<BigDecimal> getBalance(@PathVariable String accountId) {
        Account acc = bankService.getAccount(accountId);
        if (acc == null) {
            return new ApiResponse<>(false, "找不到該帳戶", null);
        }
        return new ApiResponse<>(true, "查詢成功", acc.getBalance());
    }

    // 執行轉帳 API
    @PostMapping("/transfer")
    public ApiResponse<String> transfer(@RequestBody TransferRequest request) {
        try {
            bankService.transfer(request.getFromAccountId(), request.getToAccountId(), request.getAmount());
            return new ApiResponse<>(true, "轉帳成功", "交易完成");
        } catch (Exception e) {
            return new ApiResponse<>(false, "轉帳失敗: " + e.getMessage(), null);
        }
    }
}

