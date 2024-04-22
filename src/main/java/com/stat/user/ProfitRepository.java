package com.stat.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProfitRepository extends JpaRepository<Profit, Long>{
	@Query(value="select * from profit where time between ?1 and ?2 order by time asc", nativeQuery=true)
	List<Profit> findByTimeRange(long timeForm, long timeTo);
	
	@Query(value="select COALESCE(SUM(amount), 0) from profit where exness_id = ?1 and time between ?2 and ?3", nativeQuery=true)
	double findByTimeRangeAndExnessId(String exnessId, long from, long to);
}
