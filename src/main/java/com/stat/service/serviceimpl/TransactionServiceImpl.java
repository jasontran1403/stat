package com.stat.service.serviceimpl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.stereotype.Service;

import com.stat.service.ExnessService;
import com.stat.service.TransactionService;
import com.stat.user.Exness;
import com.stat.user.Transaction;
import com.stat.user.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
	private final TransactionRepository transactionRepo;
	private final ExnessService exService;

	@Override
	public List<Transaction> getDepositFromMonth(String month) {
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
		calendar.set(Calendar.SECOND, 0);

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
		calendar.set(Calendar.SECOND, 0);

		// Lấy timestamp sau khi đặt thời gian
		
		long timeTo = calendar.getTimeInMillis() / 1000 - 1;
		String type = "Deposit";
		return transactionRepo.findTransactionByTypeAndTimeRange(type, timeFrom, timeTo);
	}

	@Override
	public List<Transaction> getWithdrawFromMonth(String month) {
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
		calendar.set(Calendar.SECOND, 0);

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
		calendar.set(Calendar.SECOND, 0);

		// Lấy timestamp sau khi đặt thời gian
		
		long timeTo = calendar.getTimeInMillis() / 1000 - 1;
		String type = "Withdraw";
		return transactionRepo.findTransactionByTypeAndTimeRange(type, timeFrom, timeTo);
	}

	@Override
	public Optional<Transaction> getLatestTopupByExnessId(String exnessID, String type, long from, long to) {
		// TODO Auto-generated method stub
		Optional<Transaction> transaction = transactionRepo.findLatestTransactionByExnessId(exnessID, type, from, to);
		return transaction;
	}
	
	@Override
	public double getTotalDepositByExnessId(String exnessId) {
		// TODO Auto-generated method stub
		double totalDeposit = transactionRepo.getTotalDepositByExnessId(exnessId);
		double totalWithdraw = transactionRepo.getTotalWithdrawByExnessId(exnessId);
		return totalDeposit - totalWithdraw;
	}

	@Override
	public double getTotalDepositFromAlex(long timestamp) {
		// TODO Auto-generated method stub
		List<Exness> listExnessFromPixiu = exService.findAllByBranchName("ALEX");
		
		double result = 0.0;
		
		for (Exness exness : listExnessFromPixiu) {
			double deposit = transactionRepo.getTotalDepositPixiuByExnessId(exness.getExness(), timestamp);
			double withdraw = transactionRepo.getTotalWithdrawPixiuByExnessId(exness.getExness(), timestamp);
			
			result = result + deposit - withdraw;
		}
		
		return result;
	}

	@Override
	public double getTotalDepositByExnessIdAndTime(String exnessId, long time) {
		// TODO Auto-generated method stub
		double totalDeposit = transactionRepo.getTotalDepositPixiuByExnessId(exnessId, time);
		double totalWithdraw = transactionRepo.getTotalWithdrawPixiuByExnessId(exnessId, time);
		return totalDeposit - totalWithdraw;
	}
}
