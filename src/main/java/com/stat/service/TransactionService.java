package com.stat.service;

import java.util.List;
import java.util.Optional;

import com.stat.user.Transaction;

public interface TransactionService {
	List<Transaction> getDepositFromMonth(String month);
	List<Transaction> getWithdrawFromMonth(String month);
	Optional<Transaction> getLatestTopupByExnessId(String exnessID, String type, long from, long to);
	double getTotalDepositByExnessId(String exnessId);
	double getTotalDepositByExnessIdAndTime(String exnessId, long time);
	double getTotalDepositFromAlex(long timestamp);
}
