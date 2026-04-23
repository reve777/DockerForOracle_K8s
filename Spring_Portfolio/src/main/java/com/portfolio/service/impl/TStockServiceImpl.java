package com.portfolio.service.impl;

import java.math.BigDecimal;
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
        // 1. 驗證分類是否存在
        Optional<Classify> optClassify = classifyRepository.findById(Integer.parseInt(map.get("classify_id")));
        if (!optClassify.isPresent()) {
            return null;
        }

        TStock ts = new TStock();
        ts.setName(map.get("name"));
        ts.setSymbol(map.get("symbol"));
        ts.setClassify(optClassify.get());

        // 2. 處理價格 (從 String 轉 BigDecimal)
        // 預防前端傳入空值，給予預設值 0
        String priceStr = map.getOrDefault("price", "0");
        ts.setPrice(new BigDecimal(priceStr));

        // 3. 初始化其他報價欄位 (避免報價程式跑之前為 null)
        ts.setChangePrice(BigDecimal.ZERO);
        ts.setChangeInPercent(BigDecimal.ZERO);
        ts.setPreClosed(BigDecimal.ZERO);
        ts.setVolumn(0L);

        return tStockRepository.save(ts);
    }

    @Override
    @Transactional
    public TStock update(Integer id, Map<String, String> map) {
        // 1. 驗證分類與股票是否存在
        Optional<Classify> optClassify = classifyRepository.findById(Integer.parseInt(map.get("classify_id")));
        Optional<TStock> optTStock = tStockRepository.findById(id);
        
        if (!optClassify.isPresent() || !optTStock.isPresent()) {
            return null;
        }

        TStock tStock = optTStock.get();
        tStock.setName(map.get("name"));
        tStock.setSymbol(map.get("symbol"));
        tStock.setClassify(optClassify.get());

        // 2. 更新價格
        if (map.containsKey("price")) {
            tStock.setPrice(new BigDecimal(map.get("price")));
        }

        return tStockRepository.saveAndFlush(tStock);
    }

    @Override
    @Transactional
    public Boolean delete(Integer id) {
        if (!tStockRepository.existsById(id)) {
            return false;
        }
        // 注意：如果這檔股票已經在某些人的 WatchList 裡面，直接刪除可能會觸發外鍵約束錯誤
        // 建議在實際開發中確認資料庫有無設定 Cascade
        tStockRepository.deleteById(id);
        return true;
    }
}