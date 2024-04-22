package com.stat.service.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stat.service.ExnessCommissionService;
import com.stat.service.ExnessService;
import com.stat.user.Exness;
import com.stat.user.ExnessCommission;
import com.stat.user.ExnessCommissionRepository;

@Service
public class ExnessCommissionServiceImpl implements ExnessCommissionService{
	@Autowired
	ExnessCommissionRepository exCommissionRepo;
	
	@Autowired
	ExnessService exService;

	@Override
	public void saveExnessCommission(ExnessCommission ex) {
		// TODO Auto-generated method stub
		exCommissionRepo.save(ex);
	}

	@Override
	public List<ExnessCommission> getExnessCommissionByTimeRange(long fromDate, long toDate) {
		// TODO Auto-generated method stub
		return exCommissionRepo.getByTimeRange(fromDate, toDate);
	}

	@Override
	public double calculateIBFromTimeRange(long fromDate, long toDate) {
		// TODO Auto-generated method stub
		List<Exness> exnessList = exService.findAllByBranchName("ALEX");
		
		List<ExnessCommission> listCommission = exCommissionRepo.getByTimeRange(fromDate, toDate);
		
		double result = 0.0;
		
		for (Exness exness : exnessList) {
			for (ExnessCommission commission : listCommission) {
				if (exness.getExness().equalsIgnoreCase(commission.getClientAccount())) {
					result += commission.getAmount();
				}
			}
		}
		return result;
	}

}
