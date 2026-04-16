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

	// 查詢所有投資人
	@GetMapping({ "/", "/query" })
	public List<Investor> query() {
		return investorService.getAll();
	}

	@GetMapping("/{id}")
	public Investor get(@PathVariable("id") Integer id) {
	    Investor investor = investorService.getById(id);
	    // 💡 在回傳前，可以在這裡印一下 log 確認 Java 程式內部到底有沒有資料
	    if (investor != null) {
	        System.out.println("偵測到投資組合數量: " + investor.getPortfolios().size());
	    }
	    return investor;
	}

	// 新增投資人
	@PostMapping({ "/", "/add" })
	public Investor add(@RequestBody Map<String, String> map) {
		return investorService.add(map);
	}

	// 修改投資人
	@PutMapping("/{id}")
	public Investor update(@PathVariable("id") Integer id, @RequestBody Map<String, String> map) {
		return investorService.update(id, map);
	}

	// 刪除投資人
	@DeleteMapping({ "/{id}", "/delete/{id}" })
	public Boolean delete(@PathVariable("id") Integer id) {
		return investorService.delete(id);
	}
}
