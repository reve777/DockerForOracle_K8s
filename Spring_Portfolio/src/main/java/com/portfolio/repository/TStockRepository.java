package com.portfolio.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.portfolio.entity.TStock;

import jakarta.transaction.Transactional;

@Repository
public interface TStockRepository extends JpaRepository<TStock, Integer> {

    // 💡 解決 findAll() 時 classify 為 null 的問題
    @Override
    @EntityGraph(attributePaths = {"classify"})
    List<TStock> findAll();

    @Modifying(clearAutomatically = true) // 💡 更新後自動清除快取，確保下次查詢抓到最新關聯
    @Transactional
    @Query(value = "UPDATE tstock SET name=:name, symbol=:symbol, classify_id=:classifyId WHERE id=:id", nativeQuery = true)
    void update(@Param("id") Integer id,
            @Param("name") String name,
            @Param("symbol") String symbol,
            @Param("classifyId") Integer classifyId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE tstock SET change_price=:changePrice, change_in_percent=:changeInPercent, pre_closed=:preClosed, price=:price, transaction_date=:transactionDate, volumn=:volumn WHERE id=:id", nativeQuery = true)
    void updatePrice(@Param("id") Integer id,
            @Param("changePrice") BigDecimal changePrice,
            @Param("changeInPercent") BigDecimal changeInPercent,
            @Param("preClosed") BigDecimal preClosed,
            @Param("price") BigDecimal price,
            @Param("transactionDate") Date transactionDate,
            @Param("volumn") Long volumn);
}