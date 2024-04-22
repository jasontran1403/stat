package com.stat.service.serviceimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stat.service.StatisticService;
import com.stat.user.Statistic;
import com.stat.user.StatisticRepository;

@Service
public class StatisticServiceImpl implements StatisticService{
	@Autowired
	StatisticRepository statRepo;

	@Override
	public List<Statistic> getStatisticByTime(long from, long to) {
		// TODO Auto-generated method stub
		return statRepo.findStatisticByTime(from, to);
	}

	@Override
	public double getTotalBalance() {
		// TODO Auto-generated method stub
		return statRepo.getTotalBalance();
	}

	@Override
	public void save(Statistic stat) {
		// TODO Auto-generated method stub
		Optional<Statistic> checkStat = statRepo.findByTimeAndNameAndAmount(stat.getTime());
		if (checkStat.isPresent()) {
			return;
		}
		statRepo.save(stat);
	}
}
