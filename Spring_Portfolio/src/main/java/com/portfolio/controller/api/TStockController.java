package com.portfolio.controller.api;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.portfolio.entity.TStock;
import com.portfolio.service.TStockService;

@RestController
@RequestMapping("/tstock")
public class TStockController {

	private final TStockService tStockService;

	public TStockController(TStockService tStockService) {
		this.tStockService = tStockService;
	}

	@GetMapping({ "/", "/query" })
	public List<TStock> query() {
		return tStockService.getAll();
	}

	@GetMapping({ "/{id}", "/get/{id}" })
	public TStock get(@PathVariable("id") Integer id) {
		return tStockService.getById(id);
	}

	@PostMapping({ "/", "/add" })
	public TStock add(@RequestBody Map<String, String> map) {
		return tStockService.add(map);
	}

	@PutMapping({ "/{id}", "/update/{id}" })
	public TStock update(@PathVariable("id") Integer id, @RequestBody Map<String, String> map) {
		return tStockService.update(id, map);
	}

	@DeleteMapping({ "/{id}", "/delete/{id}" })
	public Boolean delete(@PathVariable("id") Integer id) {
		return tStockService.delete(id);
	}
}
