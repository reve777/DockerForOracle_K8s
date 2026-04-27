package com.portfolio.bank.repository;

import com.portfolio.bank.entity.Account;

import jakarta.persistence.LockModeType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.accountId in :ids")
    List<Account> findAllByIdInForUpdate(Collection<String> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.accountId = :id")
    Optional<Account> findByIdForUpdate(String id);
}