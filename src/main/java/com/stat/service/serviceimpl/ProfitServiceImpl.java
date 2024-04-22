package com.stat.service.serviceimpl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.springframework.stereotype.Service;

import com.stat.service.ProfitService;
import com.stat.user.Profit;
import com.stat.user.ProfitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfitServiceImpl implements ProfitService{
	private final ProfitRepository profitRepo;

	@Override
	public List<Profit> getProfitFromMonth(String month) {
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
		long timeFrom = calendar.getTimeInMillis() / 1000;
		

		// Lấy ngày hiện tại
		calendar.setTime(currentDateTime);

		// Đặt thời gian thành 00:00:01
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 1);

		// Lấy timestamp sau khi đặt thời gian
		
		long timeTo = calendar.getTimeInMillis() / 1000 - 86400;
		
		return profitRepo.findByTimeRange(timeFrom, timeTo);
	}

	@Override
	public double getProfitByExnessIdAndTimeRange(String exnessId, long from, long to) {
		// TODO Auto-generated method stub
		return profitRepo.findByTimeRangeAndExnessId(exnessId, from, to);
	}


}
