package com.portfolio.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.entity.Investor;
import com.portfolio.entity.Watch;
import com.portfolio.repository.InvestorRepository;
import com.portfolio.repository.WatchRepository;
import com.portfolio.service.InvestorService;

@Service
public class InvestorServiceImpl implements InvestorService {

    private final InvestorRepository investorRepository;
    private final WatchRepository watchRepository;

    public InvestorServiceImpl(InvestorRepository investorRepository, WatchRepository watchRepository) {
        this.investorRepository = investorRepository;
        this.watchRepository = watchRepository;
    }

    @Override
    public List<Investor> getAll() {
        return investorRepository.findAll();
    }

    @Override
    public Investor getById(Integer id) {
        Optional<Investor> opt = investorRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    @Transactional
    public Investor add(Map<String, String> map) {
        Investor investor = new Investor();
        investor.setUsername(map.get("username"));
        investor.setEmail(map.get("email"));
        investor.setBalance(Integer.parseInt(map.get("balance")));
        // 存檔 Investor
        investorRepository.save(investor);

        // 自動建立 Watch
        String watchName = investor.getUsername() + "的投資組合";
        Watch watch = new Watch();
        watch.setName(watchName);
        watch.setInvestor(investor); // 建立關聯
        watchRepository.save(watch);

        return investor;
    }

    @Override
    @Transactional
    public Investor update(Integer id, Map<String, String> map) {
        Investor investor = getById(id);
        if (investor == null) return null;

        investor.setUsername(map.get("username"));
        investor.setEmail(map.get("email"));
        investor.setBalance(Integer.parseInt(map.get("balance")));

        return investorRepository.saveAndFlush(investor);
    }

    @Override
    @Transactional
    public Boolean delete(Integer id) {
        Investor investor = getById(id);
        if (investor == null) return false;

        investorRepository.deleteById(id);
        return true;
    }
}
