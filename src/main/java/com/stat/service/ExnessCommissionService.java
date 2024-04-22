package com.stat.service;

import java.util.List;

import com.stat.user.ExnessCommission;

public interface ExnessCommissionService {
	void saveExnessCommission(ExnessCommission ex);
	
	List<ExnessCommission> getExnessCommissionByTimeRange(long fromDate, long toDate);
	
	double calculateIBFromTimeRange(long fromDate, long toDate);
}
