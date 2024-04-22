package com.stat.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.stat.dto.LevelDto;
import com.stat.service.ExnessService;
import com.stat.service.TransactionService;
import com.stat.service.UserService;
import com.stat.user.Exness;
import com.stat.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalibrateIB {
	private final ExnessService exService;
	private final TransactionService tranService;
	private final UserService userService;

	private final List<Double> RATE = new ArrayList<>(Arrays.asList(0.0, 0.2, 0.4, 0.6));

	public Map<String, LevelDto> calibrateIBBracket() {
		Map<String, LevelDto> result = new HashMap<>();
		List<Exness> listExnessFromPixiu = exService.getListExnessByBranchName("PixiuGroup");
		double sales = 0;
		for (Exness exness : listExnessFromPixiu) {
			double totalDeposit = tranService.getTotalDepositByExnessId(exness.getExness());

			LevelDto item = new LevelDto();
			sales += totalDeposit;
			double itemRate = 0;
			if (sales <= 2_500_000) {
				itemRate = RATE.get(0);
			} else if (sales <= 5_000_000) {
				itemRate = RATE.get(1);
			} else if (sales <= 10_000_000) {
				itemRate = RATE.get(2);
			} else {
				itemRate = RATE.get(3);
			}
			item.setExnessId(exness.getExness());
			item.setAmount(totalDeposit);
			item.setRate(itemRate);
			item.setSales(sales);
			result.put(exness.getExness(), item);
		}
		return result;
	}

	public double calculateValue(double totalCapital, double accountIB) {
		double threshold1 = 0;
		double threshold2 = 25_000;
		double threshold3 = 50_000;
		double threshold4 = 100_000;
		double rate1 = 0.0;
		double rate2 = 0.2;
		double rate3 = 0.4;
		double rate4 = 0.6;

		double result = 0.0;

		result += ((totalCapital - threshold1) / totalCapital) * accountIB * rate1
				* Math.pow(0, (totalCapital > threshold2 ? 1 : 0)) * (totalCapital > threshold1 ? 1 : 0);
		result += ((threshold2 - threshold1) / totalCapital) * accountIB * rate1
				* Math.pow(0, (totalCapital > threshold2 ? 1 : 0));
		result += ((totalCapital - threshold2) / totalCapital) * accountIB * rate2
				* Math.pow(0, (totalCapital > threshold3 ? 1 : 0)) * (totalCapital > threshold2 ? 1 : 0);
		result += ((threshold3 - threshold2) / totalCapital) * accountIB * rate2 * (totalCapital > threshold3 ? 1 : 0)
				* (totalCapital > threshold2 ? 1 : 0);
		result += ((totalCapital - threshold3) / totalCapital) * accountIB * rate3
				* (totalCapital > threshold3 ? 1 : 0);
		result += ((threshold4 - threshold3) / totalCapital) * accountIB * rate3 * (totalCapital > threshold4 ? 1 : 0)
				* (totalCapital > threshold3 ? 1 : 0);
		result += ((totalCapital - threshold4) / totalCapital) * accountIB * rate4
				* (totalCapital > threshold4 ? 1 : 0);

		return result;
	}

	public Map<String, List<Double>> calculateDistributedIB(String exnessId, double totalCapital, double accountCapital,
			double dailyIB, String role) {
		Map<String, List<Double>> result = new HashMap<>();

		List<Double> data = new ArrayList<>();

		double threshold2 = 25_000, threshold3 = 50_000, threshold4 = 100_000;

		double rate2 = 0.0, rate3 = 0.0, rate4 = 0.0;

		if (role.equalsIgnoreCase("ADMIN")) {
			rate2 = 0.3;
			rate3 = 0.5;
			rate4 = 0.7;
		} else if (role.equalsIgnoreCase("MANAGER")) {
			rate2 = 0.2;
			rate3 = 0.4;
			rate4 = 0.6;
		}

		if (totalCapital < threshold2 || totalCapital < accountCapital) {
			result.put(exnessId, new ArrayList<>());
			return result;
		}

		data.add(0.0);

		double ibPerDollar = dailyIB / totalCapital;

		double ibShareFromBracket1 = Math.max(0,
				ibPerDollar * (threshold3 - threshold2) * (totalCapital > threshold2 ? 1 : 0));
		double ibShareFromBracket2 = Math.max(0,
				ibPerDollar * (threshold4 - threshold3) * (totalCapital > threshold3 ? 1 : 0));
		double ibShareFromBracket3 = Math.max(0,
				ibPerDollar * (totalCapital - threshold4) * (totalCapital > threshold4 ? 1 : 0));
		double distributableIBFromBracket1 = ibShareFromBracket1 * rate2;
		double distributableIBFromBracket2 = ibShareFromBracket2 * rate3;
		double distributableIBFromBracket3 = ibShareFromBracket3 * rate4;

		double distributedCapitalRate = accountCapital / totalCapital;

		double distributedIBBracket1FromCapitalRate = distributableIBFromBracket1 * distributedCapitalRate;

		double distributedIBBracket2FromCapitalRate = distributableIBFromBracket2 * distributedCapitalRate;
		double distributedIBBracket3FromCapitalRate = distributableIBFromBracket3 * distributedCapitalRate;
		data.add(distributedIBBracket1FromCapitalRate);
		data.add(distributedIBBracket2FromCapitalRate);
		data.add(distributedIBBracket3FromCapitalRate);
		result.put(exnessId, data);

		return result;
	}

	public List<Exness> filterForSubBranch1(List<Exness> listExness, User rootUser) {
		List<Exness> exnessFiltered = new ArrayList<>();
		
		List<User> userLevel1 = userService.findByRefferal(rootUser.getEmail());
		for (User user : userLevel1) {
			findSubBranchExness(user, exnessFiltered);
		}
		
		return exnessFiltered;
	}

	private void findSubBranchExness(User user, List<Exness> exnessFiltered) {
	    if (user != null) {
	        // Kiểm tra xem user có danh sách Exness không
	        List<Exness> userExnessList = user.getExnessList();
	        if (userExnessList != null) {
	            // Thêm Exness của user vào danh sách lọc
	            exnessFiltered.addAll(userExnessList);
	        }

	        // Tìm kiếm chiều sâu với tất cả người giới thiệu của user
	        List<User> refferalUsers = userService.findByRefferal(user.getEmail());
	        for (User userRefferal : refferalUsers) {
	            // Gọi đệ quy cho mỗi người giới thiệu
	            findSubBranchExness(userRefferal, exnessFiltered);
	        }
	    }
	}
}
