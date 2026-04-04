package com.portfolio.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.portfolio.entity.Asset;
import com.portfolio.repository.AssetRepository;
import com.portfolio.repository.ProfitRepository;
import com.portfolio.service.ChartService;

@Service
public class ChartServiceImpl implements ChartService {

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private ProfitRepository profitRepository;

	@Override
	public List<Asset> getAssetsByInvid(Integer invid) {
		return assetRepository.findByInvid(invid);
	}

	@Override
	public List<?> getProfitsByInvid(Integer invid) {
		return profitRepository.findByInvid(invid);
	}
}
