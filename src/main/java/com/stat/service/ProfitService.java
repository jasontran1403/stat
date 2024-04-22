package com.stat.service;

import java.util.List;

import com.stat.user.Profit;

public interface ProfitService {
	List<Profit> getProfitFromMonth(String month);
	double getProfitByExnessIdAndTimeRange(String exnessId, long from, long to);
}
