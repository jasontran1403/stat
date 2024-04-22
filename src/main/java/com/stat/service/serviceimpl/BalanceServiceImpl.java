package com.stat.service.serviceimpl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.springframework.stereotype.Service;

import com.stat.service.BalanceService;
import com.stat.user.Balance;
import com.stat.user.BalanceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService{
	private final BalanceRepository balanceRepo;

	@Override
	public List<Balance> getBeginBalanceFromMonth(String month) {
		// TODO Auto-generated method stub
		Date currentDateTime = new Date();

		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDateTime);

		// Set thời gian thành 00:00:01
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 1);

		// Giảm giá trị tháng đi 1 để có tháng trước đó
		calendar.add(Calendar.MONTH, -1);

		// Lấy timestamp sau khi đặt thời gian
		long time = calendar.getTimeInMillis() / 1000 - 86400;

		return balanceRepo.findBalanceByTime(time);
	}

	@Override
	public List<Balance> getEndBalanceFromMonth(String month) {
		// TODO Auto-generated method stub
		Date currentDateTime = new Date();

		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDateTime);

		// Đặt thời gian thành 00:00:01
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 1);

		// Lấy timestamp sau khi đặt thời gian
		
		long time = calendar.getTimeInMillis() / 1000 - 86400;
		
		return balanceRepo.findBalanceByTime(time);
	}

	@Override
	public double getBalanceByTime(String exnessId, long time) {
		// TODO Auto-generated method stub
		return balanceRepo.getBalanceByExnessAndTime(exnessId, time);
	}

	
}
