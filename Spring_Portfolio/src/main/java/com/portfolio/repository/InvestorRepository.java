package com.portfolio.repository;

import com.portfolio.entity.Investor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InvestorRepository extends JpaRepository<Investor, Integer> {

	// 💡 修正重點：使用 JOIN FETCH 一次撈出 investor 與 watchs
	@Query("SELECT i FROM Investor i LEFT JOIN FETCH i.watchs WHERE i.username = :username")
	Optional<Investor> findByUsernameWithWatchs(@Param("username") String username);

	// 使用 JOIN FETCH 強制載入關聯資料，避免 LazyInitializationException
	@Query("SELECT i FROM Investor i LEFT JOIN FETCH i.watchs WHERE i.id = :id")
	Optional<Investor> findByIdWithWatchs(@Param("id") Integer id);

	Optional<Investor> findByUsername(String username);
}