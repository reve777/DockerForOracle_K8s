package com.portfolio.service;

import java.util.List;
import java.util.Map;

import com.portfolio.entity.Classify;

public interface ClassifyService {
	List<Classify> getAll();

	Classify getById(Integer id);

	Classify add(Map<String, String> map);

	Classify update(Integer id, Map<String, String> map);

	Boolean delete(Integer id);
}
