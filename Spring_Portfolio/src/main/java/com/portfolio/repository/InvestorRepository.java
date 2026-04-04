package com.portfolio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.portfolio.entity.Investor;

@Repository
public interface InvestorRepository extends JpaRepository<Investor, Integer> {

	Optional<Investor> findByUsername(String username);

}
