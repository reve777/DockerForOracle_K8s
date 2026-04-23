package com.portfolio.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.portfolio.entity.TStock;
import com.portfolio.entity.Watch;
import com.portfolio.repository.TStockRepository;
import com.portfolio.repository.WatchRepository;
import com.portfolio.service.WatchService;

@Service
public class WatchServiceImpl implements WatchService {

    private final WatchRepository watchRepository;
    private final TStockRepository tStockRepository;

    public WatchServiceImpl(WatchRepository watchRepository, TStockRepository tStockRepository) {
        this.watchRepository = watchRepository;
        this.tStockRepository = tStockRepository;
    }

    @Override
    @Transactional
    public Watch getById(Integer id) {
        Watch watch = watchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch not found"));
        // 初始化 tStocks 避免 null
        if (watch.getTStocks() == null) {
            watch.setTStocks(new HashSet<>());
        }
        return watch;
    }

    @Override
    @Transactional
    public List<Watch> getAll() {
        List<Watch> list = watchRepository.findAll();
        list.forEach(w -> {
            if (w.getTStocks() == null) {
                w.setTStocks(new HashSet<>());
            }
        });
        return list;
    }

    @Override
    @Transactional
    public Watch addTStock(Integer watchId, Integer tstockId) {
        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch not found"));
        TStock ts = tStockRepository.findById(tstockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TStock not found"));

        watch.addTStock(ts);
        watchRepository.saveAndFlush(watch);

        return getById(watchId); // 確保 tStocks 已初始化
    }

    @Override
    @Transactional
    public Watch removeTStock(Integer watchId, Integer tstockId) {
        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch not found"));
        TStock ts = tStockRepository.findById(tstockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TStock not found"));

        watch.removeTStock(ts);
        watchRepository.saveAndFlush(watch);

        return getById(watchId); // 確保 tStocks 已初始化
    }

    @Override
    @Transactional
    public Watch update(Integer id, Map<String, String> map) {
        Watch watch = watchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch not found"));

        if (map.containsKey("name")) {
            watch.setName(map.get("name"));
        }

        return watchRepository.saveAndFlush(watch);
    }
}
