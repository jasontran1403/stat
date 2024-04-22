package com.stat.service;

import java.util.List;

import com.stat.user.User;

public interface UserService {
	boolean isFromBranch(String branchName, String exnessId);
	List<User> findByRefferal(String refferalEmail);
}
