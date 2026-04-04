package com.portfolio.controller.api;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.portfolio.entity.Classify;
import com.portfolio.service.ClassifyService;

@RestController
@RequestMapping("/classify")
public class ClassifyController {

	private final ClassifyService classifyService;

	// 建構子注入
	public ClassifyController(ClassifyService classifyService) {
		this.classifyService = classifyService;
	}

	@GetMapping({ "/", "/query" })
	public List<Classify> query() {
		return classifyService.getAll();
	}

	@GetMapping({ "/{id}", "/get/{id}" })
	public Classify get(@PathVariable("id") Integer id) {
		return classifyService.getById(id);
	}

	@PostMapping({ "/", "/add" })
	public Classify add(@RequestBody Map<String, String> map) {
		return classifyService.add(map);
	}

	@PutMapping({ "/{id}", "/update/{id}" })
	public Classify update(@PathVariable("id") Integer id, @RequestBody Map<String, String> map) {
		return classifyService.update(id, map);
	}

	@DeleteMapping({ "/{id}", "/delete/{id}" })
	public Boolean delete(@PathVariable("id") Integer id) {
		return classifyService.delete(id);
	}
}
