package com.portfolio.repository;

import com.portfolio.entity.Investor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 💡 務必 import
import org.springframework.data.repository.query.Param; // 💡 務必 import
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InvestorRepository extends JpaRepository<Investor, Integer> {

    /**
     * 💡 方案：加上 @Query
     * 這樣 Spring 就不會去拆解 "WithWatches" 這個字眼，而是執行你寫的 JPQL。
     * 同時保留 @EntityGraph 來處理關聯抓取。
     */
    @EntityGraph(attributePaths = { "watches" })
    @Query("SELECT i FROM Investor i WHERE i.username = :username")
    Optional<Investor> findByUsernameWithWatches(@Param("username") String username);

    @EntityGraph(attributePaths = { "watches" })
    @Query("SELECT i FROM Investor i WHERE i.id = :id")
    Optional<Investor> findByIdWithWatches(@Param("id") Integer id);

    // 一般查詢（維持原樣，這是符合規範的命名）
    Optional<Investor> findByUsername(String username);
}