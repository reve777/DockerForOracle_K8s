package com.portfolio.service;

import java.util.List;
import java.util.Map;

import com.portfolio.entity.Watch;

public interface WatchService {
	Watch getById(Integer id);

	List<Watch> getAll();

	Watch addTStock(Integer watchId, Integer tstockId);

	Watch removeTStock(Integer watchId, Integer tstockId);

	Watch update(Integer id, Map<String, String> map);
}
