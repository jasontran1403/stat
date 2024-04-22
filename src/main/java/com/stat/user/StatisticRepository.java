package com.stat.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StatisticRepository extends JpaRepository<Statistic, Long> {
	@Query(value="select * from statictis where time >= ?1 and time <= ?2 order by time desc", nativeQuery = true)
	List<Statistic> findStatisticByTime(long from, long to);
	
	@Query(value = "SELECT COALESCE(SUM(total_ib), 0) FROM statictis", nativeQuery = true)
	double getTotalBalance();
	
	@Query(value = "SELECT * FROM statictis where time = ?1", nativeQuery = true)
	Optional<Statistic> findByTimeAndNameAndAmount(long time);
}
