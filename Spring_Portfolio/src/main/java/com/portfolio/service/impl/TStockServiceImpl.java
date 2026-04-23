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
    @Transactional(readOnly = true)
    public List<TStock> getAll() {
        return tStockRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public TStock getById(Integer id) {
        return tStockRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public TStock add(Map<String, String> map) {
        Optional<Classify> optClassify = classifyRepository.findById(Integer.parseInt(map.get("classify_id")));
        if (!optClassify.isPresent()) {
            return null;
        }

        TStock ts = new TStock();
        ts.setName(map.get("name"));
        ts.setSymbol(map.get("symbol"));
        ts.setClassify(optClassify.get());

        String priceStr = map.getOrDefault("price", "0");
        ts.setPrice(new BigDecimal(priceStr));

        ts.setChangePrice(BigDecimal.ZERO);
        ts.setChangeInPercent(BigDecimal.ZERO);
        ts.setPreClosed(BigDecimal.ZERO);
        ts.setVolumn(0L);

        return tStockRepository.save(ts);
    }

    @Override
    @Transactional
    public TStock update(Integer id, Map<String, String> map) {
        Optional<Classify> optClassify = classifyRepository.findById(Integer.parseInt(map.get("classify_id")));
        Optional<TStock> optTStock = tStockRepository.findById(id);
        
        if (!optClassify.isPresent() || !optTStock.isPresent()) {
            return null;
        }

        TStock tStock = optTStock.get();
        tStock.setName(map.get("name"));
        tStock.setSymbol(map.get("symbol"));
        tStock.setClassify(optClassify.get());

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
        tStockRepository.deleteById(id);
        return true;
    }
}