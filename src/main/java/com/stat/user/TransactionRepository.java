package com.stat.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{
	@Query(value="select * from transaction where type = ?1 and time between ?2 and ?3 order by time desc", nativeQuery = true)
	List<Transaction> findTransactionByTypeAndTimeRange(String type, long timeFrom, long timeTo);
	
	@Query(value="select * from transaction where exness_id = ?1 and type = ?2 and time between ?3 and ?4 order by time desc limit 1", nativeQuery = true)
	Optional<Transaction> findLatestTransactionByExnessId(String exnessId, String type, long timeFrom, long timeTo);
	
	@Query(value="select COALESCE(SUM(amount), 0) from transaction where exness_id = ?1 and type = 'Deposit'", nativeQuery = true)
	double getTotalDepositByExnessId(String exnessId);
	
	@Query(value="select COALESCE(SUM(amount), 0) from transaction where exness_id = ?1 and type = 'Withdraw'", nativeQuery = true)
	double getTotalWithdrawByExnessId(String exnessId);
	
	@Query(value="select COALESCE(SUM(amount), 0) from transaction where exness_id = ?1 and type = 'Deposit' and time <= ?2", nativeQuery = true)
	double getTotalDepositPixiuByExnessId(String exnessId, long time);
	
	@Query(value="select COALESCE(SUM(amount), 0) from transaction where exness_id = ?1 and type = 'Withdraw' and time <= ?2", nativeQuery = true)
	double getTotalWithdrawPixiuByExnessId(String exnessId, long time);
}
