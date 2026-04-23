package com.portfolio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.portfolio.entity.Watch;

@Repository
public interface WatchRepository extends JpaRepository<Watch, Integer> {

	// 💡 一次撈出名單及其包含的股票，避免迴圈內重複查詢資料庫
	@EntityGraph(attributePaths = { "tStocks" })
	Optional<Watch> findById(Integer id);
}