package com.portfolio.bank.controller;

import com.portfolio.bank.dto.SimulationResult;
import com.portfolio.bank.dto.TransferRequest;
import com.portfolio.bank.entity.Account;
import com.portfolio.bank.service.BankRedisSimulationService;
import com.portfolio.bank.service.BankService;
import com.portfolio.bank.service.BankSimulationService;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/bank")
@CrossOrigin(origins = "*")
public class BankController {

    private final BankService bankService;
    private final BankSimulationService simulationService;
    private final Optional<BankRedisSimulationService> redisSimulationService;

    // Spring 4.3+ 只要是單一建構子，會自動進行依賴注入，無須加上 @Autowired
    public BankController(
            BankService bankService,
            BankSimulationService simulationService,
            Optional<BankRedisSimulationService> redisSimulationService) {
        this.bankService = bankService;
        this.simulationService = simulationService;
        this.redisSimulationService = redisSimulationService;
    }

    @GetMapping("/balance/{accountId}")
    public BigDecimal getBalance(@PathVariable String accountId) {
        Account acc = bankService.getAccount(accountId);
        if (acc == null) {
            // 建議將錯誤訊息加上變數，方便日後 Debug 排查
            throw new IllegalArgumentException("找不到該帳戶: " + accountId);
        }
        return acc.getBalance();
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferRequest request) {
        bankService.transfer(request.getFromAccountId(), request.getToAccountId(), request.getAmount());
        return "轉帳成功"; 
    }

    /**
     * 壓力測試接口
     * 故意不加 @Transactional 與 @Retryable 以觀察 Service 層在極端併發下的重試表現
     */
    @PostMapping("/stress-test")
    public String stressTest(@RequestBody TransferRequest request) {
        bankService.transfer(request.getFromAccountId(), request.getToAccountId(), request.getAmount());
        return "壓測單筆執行成功";
    }

    @PostMapping("/batch-init")
    public String initMassiveData(@RequestParam(defaultValue = "10000") int count) {
        bankService.generateMassiveData(count);
        return "成功初始化 " + count + " 筆帳戶資料，餘額皆大於 10,000";
    }

    /**
     * 觸發高併發轉帳測試
     */
    @PostMapping("/concurrent-transfers")
    public SimulationResult runConcurrentTransfers(
            @RequestParam(defaultValue = "10000") int totalRequests,
            @RequestParam(defaultValue = "100") int threadPoolSize) {
        
        // 變數宣告後直接回傳，可簡化為一行
        return simulationService.simulateHighConcurrency(totalRequests, threadPoolSize);
    }

    /**
     * 增加 redis 緩存，觸發高併發轉帳測試
     */
    @PostMapping("/redis-transfers")
    public SimulationResult runConcurrentTransfersByRedis(
            @RequestParam(defaultValue = "10000") int totalRequests,
            @RequestParam(defaultValue = "100") int threadPoolSize) {

        return redisSimulationService
                .map(service -> service.simulateWithRedis(totalRequests, threadPoolSize))
                .orElseThrow(() -> new IllegalStateException("目前環境未啟用 Redis 功能 (spring.redis.enabled=false)"));
    }

    /**
     * 觸發 Redis 資料預熱
     */
    @PostMapping("/preheat")
    public String preheat() {
        // 修復: redisSimulationService 是 Optional，需拆箱或使用 map 處理
        return redisSimulationService.map(service -> {
            service.preheatAccountData();
            return "預熱執行成功：10000 個帳號已載入 Redis。";
        }).orElseThrow(() -> new IllegalStateException("目前環境未啟用 Redis 功能 (spring.redis.enabled=false)"));
    }

    /**
     * 改用 Lua + @KafkaListener SavaLog >> ivan_txn_log Table
     */
    @PostMapping("/redis-transfersLua")
    public SimulationResult runConcurrentTransfersByRedisLua(
            @RequestParam(defaultValue = "10000") int totalRequests,
            @RequestParam(defaultValue = "100") int threadPoolSize) {

        return redisSimulationService
                .map(service -> service.simulateWithRedisLua(totalRequests, threadPoolSize))
                .orElseThrow(() -> new IllegalStateException("目前環境未啟用 Redis 功能 (spring.redis.enabled=false)"));
    }
}