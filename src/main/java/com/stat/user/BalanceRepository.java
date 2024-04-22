package com.stat.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BalanceRepository extends JpaRepository<Balance, Long>{
	@Query(value="select * from balance where time = ?1", nativeQuery = true)
	List<Balance> findBalanceByTime(long time);
	
	@Query(value ="select amount from balance where exness_id = ?1 and time = ?2", nativeQuery = true)
	double getBalanceByExnessAndTime(String exnessId, long time);
}
