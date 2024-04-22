package com.stat.service;

import java.util.List;

import com.stat.user.Exness;
import com.stat.user.User;

public interface ExnessService {
	List<Exness> getExnessFromBranchName(String branchName);
	List<Exness> getListExnessByBranchName(String branchName);
	List<Exness> findAllByBranchName(String branchName);
	List<Exness> findListExnessByRootUser(User rootUser);
	double calculateTotalLotFromAlex();
	double calculateAccountLot(String exnessId);
}
