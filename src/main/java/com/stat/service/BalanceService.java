package com.stat.service;

import java.util.List;

import com.stat.user.Balance;

public interface BalanceService {
	List<Balance> getBeginBalanceFromMonth(String month);
	List<Balance> getEndBalanceFromMonth(String month);
	double getBalanceByTime(String exnessId, long time);
}
