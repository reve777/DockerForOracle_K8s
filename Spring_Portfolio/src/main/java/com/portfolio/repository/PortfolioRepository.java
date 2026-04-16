package com.portfolio.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.portfolio.entity.Portfolio;
import com.portfolio.entity.Investor;
import com.portfolio.entity.TStock;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Integer> {

    // 使用 @Query 強制指定查詢路徑，解決命名解析失敗的問題
    @Query("SELECT p FROM Portfolio p WHERE p.investor = :investor AND p.tStock = :tStock")
    Optional<Portfolio> findByInvestorAndTStock(
        @Param("investor") Investor investor, 
        @Param("tStock") TStock tStock
    );
}