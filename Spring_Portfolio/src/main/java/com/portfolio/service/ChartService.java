package com.portfolio.service;

import java.util.List;
import com.portfolio.entity.Asset;

public interface ChartService {
	List<Asset> getAssetsByInvid(Integer invid);

	List<?> getProfitsByInvid(Integer invid);
}
