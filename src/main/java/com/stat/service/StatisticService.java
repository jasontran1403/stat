package com.stat.service;

import java.util.List;

import com.stat.user.Statistic;

public interface StatisticService {
	List<Statistic> getStatisticByTime(long from, long to);
	double getTotalBalance();
	void save(Statistic stat);
}
