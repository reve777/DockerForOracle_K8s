package com.portfolio.service;

import java.util.List;
import java.util.Map;

import com.portfolio.entity.Investor;

public interface InvestorService {
    List<Investor> getAll();
    Investor getById(Integer id);
    Investor add(Map<String, String> map);
    Investor update(Integer id, Map<String, String> map);
    Boolean delete(Integer id);
}
