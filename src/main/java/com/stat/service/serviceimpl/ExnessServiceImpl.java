package com.stat.service.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stat.service.ExnessService;
import com.stat.user.Exness;
import com.stat.user.ExnessRepository;
import com.stat.user.User;
import com.stat.user.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ExnessServiceImpl implements ExnessService {
	@Autowired
	ExnessRepository exRepo;
	
	@Autowired
	UserRepository userRepo;

	@Override
	public List<Exness> getExnessFromBranchName(String branchName) {
	    return exRepo.findAll().stream()
	            .filter(exness -> exness.getUser().getBranchName().equalsIgnoreCase(branchName))
	            .collect(Collectors.toList());
	}

	@Override
	@Transactional
	public List<Exness> findAllByBranchName(String branchName) {
		// TODO Auto-generated method stub
		List<Exness> results = new ArrayList<>();
		
		List<User> allUsers = userRepo.getUsersByBranchName(branchName);
		
		for (User user : allUsers) {
			if (user.getExnessList().size() > 0) {
				results.addAll(user.getExnessList());
			}
		}
		
		return results;
	}

	@Override
	public List<Exness> findListExnessByRootUser(User rootUser) {
		// TODO Auto-generated method stub
		List<Exness> listExnesses = new ArrayList<>();
		
		return listExnesses;
	}

	@Override
	public List<Exness> getListExnessByBranchName(String branchName) {
		// TODO Auto-generated method stub
		List<Exness> listExness = exRepo.findAll();
		List<Exness> result = new ArrayList<>();
		
		for (Exness exness : listExness) {
			if (exness.getUser().getBranchName().equalsIgnoreCase(branchName)) {
				result.add(exness);
			}
		}
		
		return result;
	}

	@Override
	public double calculateTotalLotFromAlex() {
		// TODO Auto-generated method stub
		double result = 0.0;
		
		List<Exness> listExness = exRepo.findAll();
		List<Exness> listExnessFromAlex = new ArrayList<>();
		
		for (Exness exness : listExness) {
			if (exness.getUser().getBranchName().equalsIgnoreCase("ALEX")
					&& !exness.getExness().equalsIgnoreCase("49151850") 
					&& !exness.getExness().equalsIgnoreCase("49151793")
					&& !exness.getExness().equalsIgnoreCase("49151807")
					&& !exness.getExness().equalsIgnoreCase("24164596")
					&& !exness.getExness().equalsIgnoreCase("24164647")
					) {
				listExnessFromAlex.add(exness);
			}
		}
		
		for (Exness exnessItem : listExnessFromAlex) {
			String lot = exnessItem.getChatId();
			if (exnessItem.getBalance() <= 0) {
				continue;
			}
			result += Double.parseDouble(lot);
		}
		
		return result;
	}

	@Override
	public double calculateAccountLot(String exnessId) {
		// TODO Auto-generated method stub
		Exness exness = exRepo.findByExness(exnessId).get();
		
		if (exness.getBalance() <= 0) {
			return 0;
		}
		String lot = exness.getChatId();
		return Double.parseDouble(lot);
	}
}
