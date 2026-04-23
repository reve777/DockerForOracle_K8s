package com.portfolio.controller.api;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import com.portfolio.entity.Investor;
import com.portfolio.service.InvestorService;

@RestController
@RequestMapping("/investor")
public class InvestorController {

    private final InvestorService investorService;

    public InvestorController(InvestorService investorService) {
        this.investorService = investorService;
    }

    @GetMapping({ "/", "/query" })
    public List<Investor> query() {
        return investorService.getAll();
    }

    @GetMapping("/{id}")
    public Investor get(@PathVariable("id") Integer id) {
        Investor investor = investorService.getById(id);
        // 💡 這裡因為 Service 層用了 findByIdWithWatches，所以不會再報 no Session 錯誤
        if (investor != null) {
            System.out.println("✅ [API] 投資人: " + investor.getUsername() + "，觀測名單筆數: " + 
                (investor.getWatches() != null ? investor.getWatches().size() : 0));
        }
        return investor;
    }

    @PostMapping({ "/", "/add" })
    public Investor add(@RequestBody Map<String, String> map) {
        return investorService.add(map);
    }

    @PutMapping("/{id}")
    public Investor update(@PathVariable("id") Integer id, @RequestBody Map<String, String> map) {
        return investorService.update(id, map);
    }

    @DeleteMapping({ "/{id}", "/delete/{id}" })
    public Boolean delete(@PathVariable("id") Integer id) {
        return investorService.delete(id);
    }
}