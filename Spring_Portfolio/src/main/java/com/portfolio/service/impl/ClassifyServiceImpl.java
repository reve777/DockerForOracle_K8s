package com.portfolio.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.entity.Classify;
import com.portfolio.repository.ClassifyRepository;
import com.portfolio.service.ClassifyService;

@Service
public class ClassifyServiceImpl implements ClassifyService {

	private final ClassifyRepository classifyRepository;

	// 建構子注入
	public ClassifyServiceImpl(ClassifyRepository classifyRepository) {
		this.classifyRepository = classifyRepository;
	}

	@Override
	public List<Classify> getAll() {
		return classifyRepository.findAll();
	}

	@Override
	public Classify getById(Integer id) {
		Optional<Classify> opt = classifyRepository.findById(id);
		return opt.orElse(null);
	}

	@Override
	@Transactional
	public Classify add(Map<String, String> map) {
		Classify classify = new Classify();
		classify.setName(map.get("name"));
		classify.setTx(map.get("tx") != null);
		return classifyRepository.saveAndFlush(classify);
	}

	@Override
	@Transactional
	public Classify update(Integer id, Map<String, String> map) {
		Classify classify = getById(id);
		if (classify == null)
			return null;
		classify.setName(map.get("name"));
		classify.setTx(map.get("tx") != null);
		return classifyRepository.saveAndFlush(classify);
	}

	@Override
	public Boolean delete(Integer id) {
		Classify classify = getById(id);
		if (classify == null)
			return false;
		classifyRepository.deleteById(id);
		return true;
	}
}
