package com.portfolio.controller.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.entity.Asset;
import com.portfolio.service.ChartService;

@RestController
@RequestMapping("/chart")
public class ChartController {

	@Autowired
	private ChartService chartService;

	@GetMapping("/asset/{invid}")
	public List<Asset> asset(@PathVariable("invid") Integer invid) {
		return chartService.getAssetsByInvid(invid);
	}

	@GetMapping("/profit/{invid}")
	public List<?> profit(@PathVariable("invid") Integer invid) {
		return chartService.getProfitsByInvid(invid);
	}
}
