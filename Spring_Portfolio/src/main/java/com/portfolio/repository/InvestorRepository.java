package com.portfolio.repository;

import com.portfolio.entity.Investor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InvestorRepository extends JpaRepository<Investor, Integer> {

	/**
	 * 💡 修正重點 1：JPQL 路徑改為 i.watches (對齊 Entity 欄位名)
	 * 💡 修正重點 2：方法名稱改為 findByUsernameWithWatches (對齊各處呼叫端)
	 */
	@Query("SELECT i FROM Investor i LEFT JOIN FETCH i.watches WHERE i.username = :username")
	Optional<Investor> findByUsernameWithWatches(@Param("username") String username);

	/**
	 * 💡 修正重點 1：JPQL 路徑改為 i.watches
	 * 💡 修正重點 2：方法名稱改為 findByIdWithWatches
	 */
	@Query("SELECT i FROM Investor i LEFT JOIN FETCH i.watches WHERE i.id = :id")
	Optional<Investor> findByIdWithWatches(@Param("id") Integer id);

	// 一般查詢（不帶關聯資料）
	Optional<Investor> findByUsername(String username);
}