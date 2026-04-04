package com.portfolio.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.entity.Classify;
import com.portfolio.entity.TStock;
import com.portfolio.repository.ClassifyRepository;
import com.portfolio.repository.TStockRepository;
import com.portfolio.service.TStockService;

@Service
public class TStockServiceImpl implements TStockService {

	private final TStockRepository tStockRepository;
	private final ClassifyRepository classifyRepository;

	public TStockServiceImpl(TStockRepository tStockRepository, ClassifyRepository classifyRepository) {
		this.tStockRepository = tStockRepository;
		this.classifyRepository = classifyRepository;
	}

	@Override
	public List<TStock> getAll() {
		return tStockRepository.findAll();
	}

	@Override
	public TStock getById(Integer id) {
		return tStockRepository.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public TStock add(Map<String, String> map) {
		Optional<Classify> optClassify = classifyRepository.findById(Integer.parseInt(map.get("classify_id")));
		if (!optClassify.isPresent())
			return null;

		TStock ts = new TStock();
		ts.setName(map.get("name"));
		ts.setSymbol(map.get("symbol"));
		ts.setClassify(optClassify.get());

		return tStockRepository.save(ts);
	}

	@Override
	@Transactional
	public TStock update(Integer id, Map<String, String> map) {
		Optional<Classify> optClassify = classifyRepository.findById(Integer.parseInt(map.get("classify_id")));
		if (!optClassify.isPresent())
			return null;

		Optional<TStock> optTStock = tStockRepository.findById(id);
		if (!optTStock.isPresent())
			return null;

		TStock tStock = optTStock.get();
		tStock.setName(map.get("name"));
		tStock.setSymbol(map.get("symbol"));
		tStock.setClassify(optClassify.get());

		return tStockRepository.saveAndFlush(tStock);
	}

	@Override
	@Transactional
	public Boolean delete(Integer id) {
		Optional<TStock> optTStock = tStockRepository.findById(id);
		if (!optTStock.isPresent())
			return false;

		tStockRepository.deleteById(id);
		return true;
	}
}
