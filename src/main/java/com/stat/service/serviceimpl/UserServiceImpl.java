package com.stat.service.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stat.service.UserService;
import com.stat.user.Exness;
import com.stat.user.ExnessRepository;
import com.stat.user.User;
import com.stat.user.UserRepository;

@Service
public class UserServiceImpl implements UserService{
	@Autowired
	ExnessRepository exRepo;
	
	@Autowired
	UserRepository userRepo;

	@Override
	public boolean isFromBranch(String branchName, String exnessId) {
		// TODO Auto-generated method stub
		Exness exness = exRepo.findByExness(exnessId).get();
		if (exness.getUser().getBranchName().equalsIgnoreCase(branchName)) {
			return true;
		}
		return false;
	}

	@Override
	public List<User> findByRefferal(String refferalEmail) {
		// TODO Auto-generated method stub
		return userRepo.findByRefferal(refferalEmail);
	}
}
