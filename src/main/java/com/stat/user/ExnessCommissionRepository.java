package com.stat.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExnessCommissionRepository extends JpaRepository<ExnessCommission, Long>{
	@Query(value="select * from exness_commission where date >= ?1 and date <= ?2", nativeQuery=true)
	List<ExnessCommission> getByTimeRange(long fromDate, long toDate);
}
