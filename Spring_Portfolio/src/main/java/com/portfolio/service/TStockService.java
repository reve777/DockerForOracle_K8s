package com.portfolio.service;

import java.util.List;
import java.util.Map;

import com.portfolio.entity.TStock;

public interface TStockService {
	List<TStock> getAll();

	TStock getById(Integer id);

	TStock add(Map<String, String> map);

	TStock update(Integer id, Map<String, String> map);

	Boolean delete(Integer id);

}
