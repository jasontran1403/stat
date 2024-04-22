package com.stat.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stat.dto.AuthResponse;
import com.stat.dto.DataItem;
import com.stat.dto.LoginRequest;
import com.stat.dto.Response;
import com.stat.dto.ResponseIBToAlex;
import com.stat.service.BalanceService;
import com.stat.service.ExnessCommissionService;
import com.stat.service.ExnessService;
import com.stat.service.ProfitService;
import com.stat.service.TransactionService;
import com.stat.service.UserService;
import com.stat.user.Balance;
import com.stat.user.Exness;
import com.stat.user.ExnessCommission;
import com.stat.user.ExnessRepository;
import com.stat.user.Profit;
import com.stat.user.Transaction;

@Component
public class ExcelGenerator {
	@Autowired
	private BalanceService balanceService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private ProfitService profitService;

	@Autowired
	UserService userService;

	@Autowired
	ExnessRepository exRepo;

	@Autowired
	ExnessService exService;
	
	@Autowired
	ExnessCommissionService exCommissionService;

	public byte[] generateExcel() throws IOException {
		List<Balance> balanceBegin = balanceService.getBeginBalanceFromMonth("11");
		List<Balance> balanceEnd = balanceService.getEndBalanceFromMonth("11");
		List<Transaction> deposits = transactionService.getDepositFromMonth("11");
		List<Transaction> withdraws = transactionService.getWithdrawFromMonth("11");

		Map<String, Double> depositMap = new HashMap<>();

		for (Transaction deposit : deposits) {
			String exnessId = deposit.getExnessId();
			double amount = deposit.getAmount();

			// Nếu đã có trong map, cộng thêm amount vào amount hiện tại
			if (depositMap.containsKey(exnessId)) {
				depositMap.put(exnessId, depositMap.get(exnessId) + amount);
			} else {
				// Nếu chưa có trong map, đưa vào map với giá trị amount
				depositMap.put(exnessId, amount);
			}
		}

		Map<String, Double> withdrawMap = new HashMap<>();

		for (Transaction withdraw : withdraws) {
			String exnessId = withdraw.getExnessId();
			double amount = withdraw.getAmount();

			// Nếu đã có trong map, cộng thêm amount vào amount hiện tại
			if (withdrawMap.containsKey(exnessId)) {
				withdrawMap.put(exnessId, withdrawMap.get(exnessId) + amount);
			} else {
				// Nếu chưa có trong map, đưa vào map với giá trị amount
				withdrawMap.put(exnessId, amount);
			}
		}

		List<Profit> profits = profitService.getProfitFromMonth("11");
		Map<String, Response> responseMap = new HashMap<>();

		for (Profit profit : profits) {
			String exnessId = profit.getExnessId();

			// Nếu đã có trong map, thì cộng thêm amount vào Response hiện tại
			if (responseMap.containsKey(exnessId)) {
				Response existingResponse = responseMap.get(exnessId);
				existingResponse.setLoiNhuan(
						String.valueOf(Double.parseDouble(existingResponse.getLoiNhuan()) + profit.getAmount()));
			} else {
				// Nếu chưa có trong map, tạo mới một Response và đặt giá trị amount
				for (Balance balance : balanceBegin) {
					if (balance.getExnessId().equals(String.valueOf(exnessId))) {

					}
				}
				for (Balance balance : balanceEnd) {
					if (balance.getExnessId().equals(String.valueOf(exnessId))) {

					}
				}

				Response newResponse = new Response();
				newResponse.setStt(String.valueOf(responseMap.size() + 1));
				newResponse.setNgayKhoiTao("");
				newResponse.setTen("");
				newResponse.setServer("");
				newResponse.setId(String.valueOf(exnessId));
				newResponse.setSoTienKhoiTao("");
				newResponse.setChiSoDanh("");
				newResponse.setPhanTram("");
				if (depositMap.containsKey(String.valueOf(exnessId))) {
					double amount = depositMap.get(String.valueOf(exnessId));
					newResponse.setSoTienNapThem(String.valueOf(amount));
				} else {
					newResponse.setSoTienNapThem("0");
				}

				newResponse.setSoTienDaRut("");
				if (withdrawMap.containsKey(String.valueOf(exnessId))) {
					double amount = withdrawMap.get(String.valueOf(exnessId));
					newResponse.setSoTienDaRut(String.valueOf(amount));
				} else {
					newResponse.setSoTienDaRut("0");
				}
				newResponse.setLoiNhuan(String.valueOf(profit.getAmount()));
				for (Balance balance : balanceBegin) {
					if (balance.getExnessId().equals(String.valueOf(exnessId))) {
						newResponse.setSoDuTruoc(String.valueOf(balance.getAmount()));
					}
				}
				if (newResponse.getSoDuTruoc() == null) {
					newResponse.setSoDuTruoc("N/A");
				}
				for (Balance balance : balanceEnd) {
					if (balance.getExnessId().equals(String.valueOf(exnessId))) {
						newResponse.setSoDuSau(String.valueOf(balance.getAmount()));
					}
				}
				if (newResponse.getSoDuSau() == null) {
					newResponse.setSoDuSau("N/A");
				}

				responseMap.put(exnessId, newResponse);
			}
		}

		try (Workbook workbook = new XSSFWorkbook()) {

			Sheet sheet = workbook.createSheet("Sheet 1");

			// Dòng 1: BẢNG THỐNG KÊ LỢI NHUẬN
			Row titleRow = sheet.createRow(0);
			Cell titleCell = titleRow.createCell(0);
			titleCell.setCellValue("BẢNG THỐNG KÊ LỢI NHUẬN");
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));
			titleRow.setHeight((short) 1_000);

			// Dòng 2: Tiêu đề cột
			Row headerRow = sheet.createRow(1);
			String[] headers = { "STT", "Ngày khởi tạo", "Tên", "Server", "ID", "Số tiền khởi tạo", "Chỉ số đánh",
					"Phần trăm", "Số tiền nạp thêm (USC)", "Số tiền đã rút (USC)", "Lợi nhuận", "Số dư trước",
					"Số dư sau" };

			CellStyle headerStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setBold(true);
			headerStyle.setFont(font);

			for (int i = 0; i < headers.length; i++) {
				Cell headerCell = headerRow.createCell(i);
				headerCell.setCellValue(headers[i]);
				headerCell.setCellStyle(headerStyle);
				sheet.setColumnWidth(i, 6_000);
			}

			int rowCount = 0;
			int index = 1;

			for (Map.Entry<String, Response> entry : responseMap.entrySet()) {
				Response value = entry.getValue();
				Row dataRow = sheet.createRow(rowCount + 2); // Bắt đầu từ dòng 3

				// Thiết lập giá trị cho từng ô trong dòng
				dataRow.createCell(0).setCellValue(index);
				dataRow.createCell(1).setCellValue(value.getNgayKhoiTao());
				dataRow.createCell(2).setCellValue(value.getTen());
				dataRow.createCell(3).setCellValue(value.getServer());
				dataRow.createCell(4).setCellValue(value.getId());
				dataRow.createCell(5).setCellValue(value.getSoTienKhoiTao());
				dataRow.createCell(6).setCellValue(value.getChiSoDanh());
				dataRow.createCell(7).setCellValue(value.getPhanTram());
				dataRow.createCell(8).setCellValue(formatIfNumber(value.getSoTienNapThem()));
				dataRow.createCell(9).setCellValue(formatIfNumber(value.getSoTienDaRut()));
				dataRow.createCell(10).setCellValue(formatIfNumber(value.getLoiNhuan()));
				dataRow.createCell(11).setCellValue(formatIfNumber(value.getSoDuTruoc()));
				dataRow.createCell(12).setCellValue(formatIfNumber(value.getSoDuSau()));

				rowCount++;
				index++;
			}

			// Canh giữa các ô trong sheet
			for (int i = 0; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row != null) {
					row.setHeight((short) 1_000); // Điều chỉnh độ cao của mỗi dòng
					for (int j = 0; j < row.getLastCellNum(); j++) {
						Cell cell = row.getCell(j);
						if (cell != null) {
							cellCellStyle(cell, workbook);
						}
					}
				}
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			return outputStream.toByteArray();
		}
	}

	public byte[] generateExcelAlex(long from, long to) throws IOException {
		System.out.println("Bắt đầu chức năng: " + System.currentTimeMillis() / 1000);

		System.out.println("Bắt đầu lấy dữ liệu: " + System.currentTimeMillis() / 1000);
		List<Balance> balanceBegin = balanceService.getBeginBalanceFromMonth("11");
		List<Balance> balanceEnd = balanceService.getEndBalanceFromMonth("11");
		List<Transaction> deposits = transactionService.getDepositFromMonth("11");
		List<Transaction> withdraws = transactionService.getWithdrawFromMonth("11");

		long fromProfit = from + 1;
		long toProfit = to + 1 - 86400;

		Map<String, Double> depositMap = new HashMap<>();

		for (Transaction deposit : deposits) {
			String exnessId = deposit.getExnessId();
			double amount = deposit.getAmount();

			// Nếu đã có trong map, cộng thêm amount vào amount hiện tại
			if (depositMap.containsKey(exnessId)) {
				depositMap.put(exnessId, depositMap.get(exnessId) + amount);
			} else {
				// Nếu chưa có trong map, đưa vào map với giá trị amount
				depositMap.put(exnessId, amount);
			}
		}

		Map<String, Double> withdrawMap = new HashMap<>();

		for (Transaction withdraw : withdraws) {
			String exnessId = withdraw.getExnessId();
			double amount = withdraw.getAmount();

			// Nếu đã có trong map, cộng thêm amount vào amount hiện tại
			if (withdrawMap.containsKey(exnessId)) {
				withdrawMap.put(exnessId, withdrawMap.get(exnessId) + amount);
			} else {
				// Nếu chưa có trong map, đưa vào map với giá trị amount
				withdrawMap.put(exnessId, amount);
			}

		}

		List<Exness> exnessesFromAlex = exService.getExnessFromBranchName("Alex");
		List<Exness> exnessesFromHhl = exService.getExnessFromBranchName("HHL");
		List<Exness> exnessesFromQueen = exService.getExnessFromBranchName("LISA");
		List<Exness> exnessesFromPixiu = exService.getExnessFromBranchName("PixiuGroup");
		Map<String, Response> responseMapAlex = new HashMap<>();
		Map<String, Response> responseMapHhl = new HashMap<>();
		Map<String, Response> responseMapQueen = new HashMap<>();
		Map<String, Response> responseMapPixiu = new HashMap<>();

		System.out.println("Bắt đầu lọc dữ liệu cho nhánh Alex: " + System.currentTimeMillis() / 1000);

		for (Exness exness : exnessesFromAlex) {
			String exnessId = exness.getExness();

			Response newResponse = new Response();

			double profitLastMonth = profitService.getProfitByExnessIdAndTimeRange(exnessId, fromProfit, toProfit);
			newResponse.setLoiNhuan(String.valueOf(profitLastMonth));
			newResponse.setStt(String.valueOf(responseMapAlex.size() + 1));
			newResponse.setNgayKhoiTao(exness.getMessage());
			newResponse.setTen(exness.getName());
			newResponse.setServer(exness.getServer());
			newResponse.setRefferal(exness.getRefferal());
			newResponse.setId(String.valueOf(exnessId));
			newResponse.setSoTienKhoiTao("");
			newResponse.setPassword(exness.getPassview());
			newResponse.setChiSoDanh(exness.getReason());
			newResponse.setPhanTram(exness.getReason());
			Optional<Transaction> transaction = transactionService.getLatestTopupByExnessId(exness.getExness(),
					"Deposit", from, to);
			if (transaction.isPresent()) {
				long thoiGianLenhNapGanNhat = transaction.get().getTime() - 25200;
				Date dateLatest = new Date(thoiGianLenhNapGanNhat * 1000);
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				String formattedDate = sdf.format(dateLatest);
				newResponse.setLenhNapGanNhat(formattedDate);
			} else {
				newResponse.setLenhNapGanNhat("");
			}

			if (depositMap.containsKey(String.valueOf(exnessId))) {
				double amount = depositMap.get(String.valueOf(exnessId));
				newResponse.setSoTienNapThem(String.valueOf(amount));
			} else {
				newResponse.setSoTienNapThem("0");
			}

			newResponse.setSoTienDaRut("");
			if (withdrawMap.containsKey(String.valueOf(exnessId))) {
				double amount = withdrawMap.get(String.valueOf(exnessId));
				newResponse.setSoTienDaRut(String.valueOf(amount));
			} else {
				newResponse.setSoTienDaRut("0");
			}
			for (Balance balance : balanceBegin) {
				if (balance.getExnessId().equals(String.valueOf(exnessId))) {
					newResponse.setSoDuTruoc(String.valueOf(balance.getAmount()));
				}
			}
			if (newResponse.getSoDuTruoc() == null) {
				newResponse.setSoDuTruoc("");
			}
			for (Balance balance : balanceEnd) {
				if (balance.getExnessId().equals(String.valueOf(exnessId))) {
					newResponse.setSoDuSau(String.valueOf(balance.getAmount()));
				}
			}
			if (newResponse.getSoDuSau() == null) {
				newResponse.setSoDuSau("");
			}

			responseMapAlex.put(exnessId, newResponse);

		}

		System.out.println("Bắt đầu lọc dữ liệu cho nhánh Hhl: " + System.currentTimeMillis() / 1000);

		for (Exness exness : exnessesFromHhl) {
			String exnessId = exness.getExness();

			Response newResponse = new Response();

			double profitLastMonth = profitService.getProfitByExnessIdAndTimeRange(exnessId, fromProfit, toProfit);
			newResponse.setLoiNhuan(String.valueOf(profitLastMonth));
			newResponse.setStt(String.valueOf(responseMapHhl.size() + 1));
			newResponse.setNgayKhoiTao(exness.getMessage());
			newResponse.setTen(exness.getName());
			newResponse.setServer(exness.getServer());
			newResponse.setRefferal(exness.getRefferal());
			newResponse.setId(String.valueOf(exnessId));
			newResponse.setSoTienKhoiTao("");
			newResponse.setPassword(exness.getPassview());
			newResponse.setChiSoDanh(exness.getReason());
			newResponse.setPhanTram(exness.getReason());
			newResponse.setLot(String.valueOf(exness.getLot()));
			Optional<Transaction> transaction = transactionService.getLatestTopupByExnessId(exness.getExness(),
					"Deposit", from, to);
			if (transaction.isPresent()) {
				long thoiGianLenhNapGanNhat = transaction.get().getTime() - 25200;
				Date dateLatest = new Date(thoiGianLenhNapGanNhat * 1000);
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				String formattedDate = sdf.format(dateLatest);
				newResponse.setLenhNapGanNhat(formattedDate);
			} else {
				newResponse.setLenhNapGanNhat("");
			}

			if (depositMap.containsKey(String.valueOf(exnessId))) {
				double amount = depositMap.get(String.valueOf(exnessId));
				newResponse.setSoTienNapThem(String.valueOf(amount));
			} else {
				newResponse.setSoTienNapThem("0");
			}

			newResponse.setSoTienDaRut("");
			if (withdrawMap.containsKey(String.valueOf(exnessId))) {
				double amount = withdrawMap.get(String.valueOf(exnessId));
				newResponse.setSoTienDaRut(String.valueOf(amount));
			} else {
				newResponse.setSoTienDaRut("0");
			}
			for (Balance balance : balanceBegin) {
				if (balance.getExnessId().equals(String.valueOf(exnessId))) {
					newResponse.setSoDuTruoc(String.valueOf(balance.getAmount()));
				}
			}
			if (newResponse.getSoDuTruoc() == null) {
				newResponse.setSoDuTruoc("");
			}
			for (Balance balance : balanceEnd) {
				if (balance.getExnessId().equals(String.valueOf(exnessId))) {
					newResponse.setSoDuSau(String.valueOf(balance.getAmount()));
				}
			}
			if (newResponse.getSoDuSau() == null) {
				newResponse.setSoDuSau("");
			}

			responseMapHhl.put(exnessId, newResponse);

		}

		System.out.println("Bắt đầu lọc dữ liệu cho nhánh Queen: " + System.currentTimeMillis() / 1000);

		for (Exness exness : exnessesFromQueen) {
			String exnessId = exness.getExness();

			Response newResponse = new Response();

			double profitLastMonth = profitService.getProfitByExnessIdAndTimeRange(exnessId, fromProfit, toProfit);
			newResponse.setLoiNhuan(String.valueOf(profitLastMonth));
			newResponse.setStt(String.valueOf(responseMapQueen.size() + 1));
			newResponse.setNgayKhoiTao(exness.getMessage());
			newResponse.setTen(exness.getName());
			newResponse.setServer(exness.getServer());
			newResponse.setRefferal(exness.getRefferal());
			newResponse.setId(String.valueOf(exnessId));
			newResponse.setSoTienKhoiTao("");
			newResponse.setPassword(exness.getPassview());
			newResponse.setChiSoDanh(exness.getReason());
			newResponse.setPhanTram(exness.getReason());
			newResponse.setLot(String.valueOf(exness.getLot()));
			Optional<Transaction> transaction = transactionService.getLatestTopupByExnessId(exness.getExness(),
					"Deposit", from, to);
			if (transaction.isPresent()) {
				long thoiGianLenhNapGanNhat = transaction.get().getTime() - 25200;
				Date dateLatest = new Date(thoiGianLenhNapGanNhat * 1000);
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				String formattedDate = sdf.format(dateLatest);
				newResponse.setLenhNapGanNhat(formattedDate);
			} else {
				newResponse.setLenhNapGanNhat("");
			}

			if (depositMap.containsKey(String.valueOf(exnessId))) {
				double amount = depositMap.get(String.valueOf(exnessId));
				newResponse.setSoTienNapThem(String.valueOf(amount));
			} else {
				newResponse.setSoTienNapThem("0");
			}

			newResponse.setSoTienDaRut("");
			if (withdrawMap.containsKey(String.valueOf(exnessId))) {
				double amount = withdrawMap.get(String.valueOf(exnessId));
				newResponse.setSoTienDaRut(String.valueOf(amount));
			} else {
				newResponse.setSoTienDaRut("0");
			}
			for (Balance balance : balanceBegin) {
				if (balance.getExnessId().equals(String.valueOf(exnessId))) {
					newResponse.setSoDuTruoc(String.valueOf(balance.getAmount()));
				}
			}
			if (newResponse.getSoDuTruoc() == null) {
				newResponse.setSoDuTruoc("");
			}
			for (Balance balance : balanceEnd) {
				if (balance.getExnessId().equals(String.valueOf(exnessId))) {
					newResponse.setSoDuSau(String.valueOf(balance.getAmount()));
				}
			}
			if (newResponse.getSoDuSau() == null) {
				newResponse.setSoDuSau("");
			}

			responseMapQueen.put(exnessId, newResponse);

		}

		System.out.println("Bắt đầu lọc dữ liệu cho nhánh Pixiu Group: " + System.currentTimeMillis() / 1000);

		for (Exness exness : exnessesFromPixiu) {
			String exnessId = exness.getExness();

			Response newResponse = new Response();

			double profitLastMonth = profitService.getProfitByExnessIdAndTimeRange(exnessId, fromProfit, toProfit);
			newResponse.setLoiNhuan(String.valueOf(profitLastMonth));
			newResponse.setStt(String.valueOf(responseMapPixiu.size() + 1));
			newResponse.setNgayKhoiTao(exness.getMessage());
			newResponse.setTen(exness.getName());
			newResponse.setServer(exness.getServer());
			newResponse.setRefferal(exness.getRefferal());
			newResponse.setId(String.valueOf(exnessId));
			newResponse.setSoTienKhoiTao("");
			newResponse.setPassword(exness.getPassview());
			newResponse.setChiSoDanh(exness.getReason());
			newResponse.setPhanTram(exness.getReason());
			newResponse.setLot(String.valueOf(exness.getLot()));
			Optional<Transaction> transaction = transactionService.getLatestTopupByExnessId(exness.getExness(),
					"Deposit", from, to);
			if (transaction.isPresent()) {
				long thoiGianLenhNapGanNhat = transaction.get().getTime() - 25200;
				Date dateLatest = new Date(thoiGianLenhNapGanNhat * 1000);
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				String formattedDate = sdf.format(dateLatest);
				newResponse.setLenhNapGanNhat(formattedDate);
			} else {
				newResponse.setLenhNapGanNhat("");
			}

			if (depositMap.containsKey(String.valueOf(exnessId))) {
				double amount = depositMap.get(String.valueOf(exnessId));
				newResponse.setSoTienNapThem(String.valueOf(amount));
			} else {
				newResponse.setSoTienNapThem("0");
			}

			newResponse.setSoTienDaRut("");
			if (withdrawMap.containsKey(String.valueOf(exnessId))) {
				double amount = withdrawMap.get(String.valueOf(exnessId));
				newResponse.setSoTienDaRut(String.valueOf(amount));
			} else {
				newResponse.setSoTienDaRut("0");
			}
			for (Balance balance : balanceBegin) {
				if (balance.getExnessId().equals(String.valueOf(exnessId))) {
					newResponse.setSoDuTruoc(String.valueOf(balance.getAmount()));
				}
			}
			if (newResponse.getSoDuTruoc() == null) {
				newResponse.setSoDuTruoc("");
			}
			for (Balance balance : balanceEnd) {
				if (balance.getExnessId().equals(String.valueOf(exnessId))) {
					newResponse.setSoDuSau(String.valueOf(balance.getAmount()));
				}
			}
			if (newResponse.getSoDuSau() == null) {
				newResponse.setSoDuSau("");
			}

			responseMapPixiu.put(exnessId, newResponse);

		}

		System.out.println("Bắt đầu ghi file: " + System.currentTimeMillis() / 1000);

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheetAlex = workbook.createSheet("Alex");
			// Dòng 1: BẢNG THỐNG KÊ LỢI NHUẬN
			Row titleRowAlex = sheetAlex.createRow(0);
			Cell titleCellAlex = titleRowAlex.createCell(0);
			titleCellAlex.setCellValue("BẢNG THỐNG KÊ");
			sheetAlex.addMergedRegion(new CellRangeAddress(0, 0, 0, 18));
			titleRowAlex.setHeight((short) 500);

			// Dòng 2: Tiêu đề cột
			Row headerRowAlex = sheetAlex.createRow(1);
			String[] headersAlex = { "STT", "Ngày khởi tạo", "ID (Level1)", "Tên", "ID (Level2)", "Tên", "ID (Level3)",
					"Tên", "Vai trò", "Server", "Passview", "Tổng vốn đầu tháng", "Tổng Topup tháng",
					"Thời gian topup mới nhất", "Tổng rút trong tháng", "Tổng lợi nhuận trong tháng",
					"Tổng vốn cuối tháng", "% Kích", "Phí VPS", "NĐT chuyển BOT", "BOT chuyển IB" };

			CellStyle headerStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setBold(true);
			headerStyle.setFont(font);

			for (int i = 0; i < headersAlex.length; i++) {
				Cell headerCell = headerRowAlex.createCell(i);
				headerCell.setCellValue(headersAlex[i]);
				headerCell.setCellStyle(headerStyle);
				sheetAlex.setColumnWidth(i, 6_000);
			}

			int rowCount = 0;
			int index = 1;

			for (Map.Entry<String, Response> entry : responseMapAlex.entrySet()) {
				Response value = entry.getValue();
				Row dataRow = sheetAlex.createRow(rowCount + 2); // Bắt đầu từ dòng 3

				// Thiết lập giá trị cho từng ô trong dòng
				dataRow.createCell(0).setCellValue(index);
				dataRow.createCell(1).setCellValue(value.getNgayKhoiTao());
				// level1
				dataRow.createCell(2).setCellValue("52151575");
				dataRow.createCell(3).setCellValue("Alex");
				// level2
				dataRow.createCell(4).setCellValue(value.getRefferal());
				Exness exnessRef = exRepo.findByExness(value.getRefferal()).get();
				dataRow.createCell(5).setCellValue(exnessRef.getName());
				// level3
				dataRow.createCell(6).setCellValue(Long.parseLong(value.getId()));
				Exness exnessLevel3 = exRepo.findByExness(value.getId()).get();
				dataRow.createCell(7).setCellValue(exnessLevel3.getName());
				dataRow.createCell(8).setCellValue("Vai tro");
				dataRow.createCell(9).setCellValue(value.getServer());
				dataRow.createCell(10).setCellValue(value.getPassword());
				dataRow.createCell(11).setCellValue(value.getSoDuTruoc().replace(".", ","));
				dataRow.createCell(12).setCellValue(value.getSoTienNapThem().replace(".", ","));
				dataRow.createCell(13).setCellValue(formatIfNumber(value.getLenhNapGanNhat()));
				dataRow.createCell(14).setCellValue(value.getSoTienDaRut().replace(".", ","));
				dataRow.createCell(15).setCellValue(value.getLoiNhuan().replace(".", ","));
				dataRow.createCell(16).setCellValue(value.getSoDuSau().replace(".", ","));
				dataRow.createCell(17).setCellValue(formatIfNumber(value.getPhanTram()));
				dataRow.createCell(18).setCellValue(formatIfNumber(""));
				dataRow.createCell(19).setCellValue(formatIfNumber(""));
				dataRow.createCell(20).setCellValue(formatIfNumber(""));

				rowCount++;
				index++;
			}

			Sheet sheetHhl = workbook.createSheet("HHL");
			// Dòng 1: BẢNG THỐNG KÊ LỢI NHUẬN
			Row titleRowHhl = sheetHhl.createRow(0);
			Cell titleCellHhl = titleRowHhl.createCell(0);
			titleCellHhl.setCellValue("BẢNG THỐNG KÊ");
			sheetHhl.addMergedRegion(new CellRangeAddress(0, 0, 0, 18));
			titleRowHhl.setHeight((short) 500);

			// Dòng 2: Tiêu đề cột
			Row headerRowHhl = sheetHhl.createRow(1);
			String[] headersHhl = { "STT", "Ngày khởi tạo", "ID", "Tên", "Server", "Passview", "Tổng vốn đầu tháng",
					"Tổng Topup tháng", "Thời gian topup mới nhất", "Tổng rút trong tháng",
					"Tổng lợi nhuận trong tháng", "Tổng vốn cuối tháng", "% Kích", "Số LOT", "Phí VPS",
					"NĐT chuyển BOT", "BOT chuyển IB" };

			CellStyle headerStyleHhl = workbook.createCellStyle();
			Font fontHhl = workbook.createFont();
			fontHhl.setBold(true);
			headerStyleHhl.setFont(fontHhl);

			for (int i = 0; i < headersHhl.length; i++) {
				Cell headerCell = headerRowHhl.createCell(i);
				headerCell.setCellValue(headersHhl[i]);
				headerCell.setCellStyle(headerStyleHhl);
				sheetHhl.setColumnWidth(i, 6_000);
			}

			int rowCountHhl = 0;
			int indexHhl = 1;

			for (Map.Entry<String, Response> entry : responseMapHhl.entrySet()) {
				Response value = entry.getValue();
				if (value.getId().equals("1")) {
					continue;
				}
				Row dataRow = sheetHhl.createRow(rowCountHhl + 2); // Bắt đầu từ dòng 3

				// Thiết lập giá trị cho từng ô trong dòng
				dataRow.createCell(0).setCellValue(indexHhl);
				dataRow.createCell(1).setCellValue(value.getNgayKhoiTao());
				dataRow.createCell(2).setCellValue(Long.parseLong(value.getId()));
				dataRow.createCell(3).setCellValue(value.getTen());
				dataRow.createCell(4).setCellValue(value.getServer());
				dataRow.createCell(5).setCellValue(value.getPassword());
				dataRow.createCell(6).setCellValue(value.getSoDuTruoc().replace(".", ","));
				dataRow.createCell(7).setCellValue(value.getSoTienNapThem().replace(".", ","));
				dataRow.createCell(8).setCellValue(formatIfNumber(value.getLenhNapGanNhat()));
				dataRow.createCell(9).setCellValue(value.getSoTienDaRut().replace(".", ","));
				dataRow.createCell(10).setCellValue(value.getLoiNhuan().replace(".", ","));
				dataRow.createCell(11).setCellValue(value.getSoDuSau().replace(".", ","));
				dataRow.createCell(12).setCellValue(formatIfNumber(value.getPhanTram()));
				dataRow.createCell(13).setCellValue(formatIfNumber(value.getLot()));
				dataRow.createCell(14).setCellValue(formatIfNumber(""));
				dataRow.createCell(15).setCellValue(formatIfNumber(""));
				dataRow.createCell(16).setCellValue(formatIfNumber(""));

				rowCountHhl++;
				indexHhl++;
			}

			Sheet sheetQueen = workbook.createSheet("Queen");
			// Dòng 1: BẢNG THỐNG KÊ LỢI NHUẬN
			Row titleRowQueen = sheetQueen.createRow(0);
			Cell titleCellQueen = titleRowQueen.createCell(0);
			titleCellQueen.setCellValue("BẢNG THỐNG KÊ");
			sheetQueen.addMergedRegion(new CellRangeAddress(0, 0, 0, 18));
			titleRowQueen.setHeight((short) 500);

			// Dòng 2: Tiêu đề cột
			Row headerRowQueen = sheetQueen.createRow(1);
			String[] headersQueen = { "STT", "Ngày khởi tạo", "ID", "Tên", "Server", "Passview", "Tổng vốn đầu tháng",
					"Tổng Topup tháng", "Thời gian topup mới nhất", "Tổng rút trong tháng",
					"Tổng lợi nhuận trong tháng", "Tổng vốn cuối tháng", "% Kích", "Số LOT", "Phí VPS",
					"NĐT chuyển BOT", "BOT chuyển IB" };

			CellStyle headerStyleQueen = workbook.createCellStyle();
			Font fontQueen = workbook.createFont();
			fontQueen.setBold(true);
			headerStyleQueen.setFont(fontQueen);

			for (int i = 0; i < headersQueen.length; i++) {
				Cell headerCell = headerRowQueen.createCell(i);
				headerCell.setCellValue(headersQueen[i]);
				headerCell.setCellStyle(headerStyleQueen);
				sheetQueen.setColumnWidth(i, 6_000);
			}

			int rowCountQueen = 0;
			int indexQueen = 1;

			for (Map.Entry<String, Response> entry : responseMapQueen.entrySet()) {
				Response value = entry.getValue();
				Row dataRow = sheetQueen.createRow(rowCountQueen + 2); // Bắt đầu từ dòng 3

				// Thiết lập giá trị cho từng ô trong dòng
				dataRow.createCell(0).setCellValue(indexQueen);
				dataRow.createCell(1).setCellValue(value.getNgayKhoiTao());
				dataRow.createCell(2).setCellValue(Long.parseLong(value.getId()));
				dataRow.createCell(3).setCellValue(value.getTen());
				dataRow.createCell(4).setCellValue(value.getServer());
				dataRow.createCell(5).setCellValue(value.getPassword());
				dataRow.createCell(6).setCellValue(value.getSoDuTruoc().replace(".", ","));
				dataRow.createCell(7).setCellValue(value.getSoTienNapThem().replace(".", ","));
				dataRow.createCell(8).setCellValue(formatIfNumber(value.getLenhNapGanNhat()));
				dataRow.createCell(9).setCellValue(value.getSoTienDaRut().replace(".", ","));
				dataRow.createCell(10).setCellValue(value.getLoiNhuan().replace(".", ","));
				dataRow.createCell(11).setCellValue(value.getSoDuSau().replace(".", ","));
				dataRow.createCell(12).setCellValue(formatIfNumber(value.getPhanTram()));
				dataRow.createCell(13).setCellValue(formatIfNumber(value.getLot()));
				dataRow.createCell(14).setCellValue(formatIfNumber(""));
				dataRow.createCell(15).setCellValue(formatIfNumber(""));
				dataRow.createCell(16).setCellValue(formatIfNumber(""));

				rowCountQueen++;
				indexQueen++;
			}

			Sheet sheetPixiu = workbook.createSheet("PixiuGroup");
			// Dòng 1: BẢNG THỐNG KÊ LỢI NHUẬN
			Row titleRowPixiu = sheetPixiu.createRow(0);
			Cell titleCellPixiu = titleRowPixiu.createCell(0);
			titleCellPixiu.setCellValue("BẢNG THỐNG KÊ");
			sheetPixiu.addMergedRegion(new CellRangeAddress(0, 0, 0, 18));
			titleRowPixiu.setHeight((short) 500);

			// Dòng 2: Tiêu đề cột
			Row headerRowPixiu = sheetPixiu.createRow(1);
			String[] headersPixiu = { "STT", "Ngày khởi tạo", "ID", "Tên", "Server", "Passview", "Tổng vốn đầu tháng",
					"Tổng Topup tháng", "Thời gian topup mới nhất", "Tổng rút trong tháng",
					"Tổng lợi nhuận trong tháng", "Tổng vốn cuối tháng", "% Kích", "Số LOT", "Phí VPS",
					"NĐT chuyển BOT", "BOT chuyển IB" };

			CellStyle headerStylePixiu = workbook.createCellStyle();
			Font fontPixiu = workbook.createFont();
			fontPixiu.setBold(true);
			headerStylePixiu.setFont(fontPixiu);

			for (int i = 0; i < headersPixiu.length; i++) {
				Cell headerCell = headerRowPixiu.createCell(i);
				headerCell.setCellValue(headersPixiu[i]);
				headerCell.setCellStyle(headerStylePixiu);
				sheetPixiu.setColumnWidth(i, 6_000);
			}

			int rowCountPixiu = 0;
			int indexPixiu = 1;

			for (Map.Entry<String, Response> entry : responseMapPixiu.entrySet()) {
				Response value = entry.getValue();
				Row dataRow = sheetPixiu.createRow(rowCountPixiu + 2); // Bắt đầu từ dòng 3

				// Thiết lập giá trị cho từng ô trong dòng
				dataRow.createCell(0).setCellValue(indexPixiu);
				dataRow.createCell(1).setCellValue(value.getNgayKhoiTao());
				dataRow.createCell(2).setCellValue(Long.parseLong(value.getId()));
				dataRow.createCell(3).setCellValue(value.getTen());
				dataRow.createCell(4).setCellValue(value.getServer());
				dataRow.createCell(5).setCellValue(value.getPassword());
				dataRow.createCell(6).setCellValue(value.getSoDuTruoc().replace(".", ","));
				dataRow.createCell(7).setCellValue(value.getSoTienNapThem().replace(".", ","));
				dataRow.createCell(8).setCellValue(formatIfNumber(value.getLenhNapGanNhat()));
				dataRow.createCell(9).setCellValue(value.getSoTienDaRut().replace(".", ","));
				dataRow.createCell(10).setCellValue(value.getLoiNhuan().replace(".", ","));
				dataRow.createCell(11).setCellValue(value.getSoDuSau().replace(".", ","));
				dataRow.createCell(12).setCellValue(formatIfNumber(value.getPhanTram()));
				dataRow.createCell(13).setCellValue(formatIfNumber(value.getLot()));
				dataRow.createCell(14).setCellValue(formatIfNumber(""));
				dataRow.createCell(15).setCellValue(formatIfNumber(""));
				dataRow.createCell(16).setCellValue(formatIfNumber(""));

				rowCountPixiu++;
				indexPixiu++;
			}

			// Canh giữa các ô trong sheet
			for (int i = 0; i <= sheetAlex.getLastRowNum(); i++) {
				Row row = sheetAlex.getRow(i);
				if (row != null) {
					row.setHeight((short) 1_000); // Điều chỉnh độ cao của mỗi dòng
					for (int j = 0; j < row.getLastCellNum(); j++) {
						Cell cell = row.getCell(j);
						if (cell != null) {
							cellCellStyle(cell, workbook);
						}
					}
				}
			}

			for (int i = 0; i <= sheetHhl.getLastRowNum(); i++) {
				Row row = sheetHhl.getRow(i);
				if (row != null) {
					row.setHeight((short) 1_000); // Điều chỉnh độ cao của mỗi dòng
					for (int j = 0; j < row.getLastCellNum(); j++) {
						Cell cell = row.getCell(j);
						if (cell != null) {
							cellCellStyle(cell, workbook);
						}
					}
				}
			}

			for (int i = 0; i <= sheetQueen.getLastRowNum(); i++) {
				Row row = sheetQueen.getRow(i);
				if (row != null) {
					row.setHeight((short) 1_000); // Điều chỉnh độ cao của mỗi dòng
					for (int j = 0; j < row.getLastCellNum(); j++) {
						Cell cell = row.getCell(j);
						if (cell != null) {
							cellCellStyle(cell, workbook);
						}
					}
				}
			}

			for (int i = 0; i <= sheetPixiu.getLastRowNum(); i++) {
				Row row = sheetPixiu.getRow(i);
				if (row != null) {
					row.setHeight((short) 1_000); // Điều chỉnh độ cao của mỗi dòng
					for (int j = 0; j < row.getLastCellNum(); j++) {
						Cell cell = row.getCell(j);
						if (cell != null) {
							cellCellStyle(cell, workbook);
						}
					}
				}
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			System.out.println("Kết thúc chức năng: " + System.currentTimeMillis() / 1000);
			return outputStream.toByteArray();
		}
	}

	public void test() throws IOException {
        long initDateTime = 1678665600;
        long targetDateTime = 1708300800;
        int delaySeconds = 15;

        while (initDateTime < targetDateTime) {
            Instant instant = Instant.ofEpochSecond(initDateTime);
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = dateTime.format(formatter);

            Map<Long, DataItem> result = getDataIB(formattedDate, formattedDate);
            
            result.forEach((key, value) -> {
                System.out.println(value);
            });
            
            // Increment initDateTime by 86400 (number of seconds in a day)
            initDateTime += 86400;

            try {
                // Sleep for 15 seconds
                Thread.sleep(delaySeconds * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


	public byte[] generateoIBToExcel(String fromDate, String toDate) throws IOException {
		System.out.println(System.currentTimeMillis() / 1000);

		Map<Long, DataItem> result = getDataIBFromDatabase(fromDate, toDate);
		
		System.out.println(result);

		Map<String, ResponseIBToAlex> responseMap = new HashMap<>();

		Map<String, ResponseIBToAlex> responseMapToHHL = new HashMap<>();

		Map<String, ResponseIBToAlex> responseMapToQueen = new HashMap<>();

		Map<String, ResponseIBToAlex> responseMapToPixiu = new HashMap<>();

		List<Exness> listExnessAlex = exService.getExnessFromBranchName("Alex");

		List<Exness> listExnessHHL = exService.getExnessFromBranchName("HHL");

		List<Exness> listExnessQueen = exService.getExnessFromBranchName("Lisa");

		List<Exness> listExnessPixiu = exService.getExnessFromBranchName("PixiuGroup");
		
		long fromDateConverted = convertToUnixTimestamp(fromDate, "GMT+7");
		long toDateConverted = convertToUnixTimestamp(toDate, "GMT+7");
		
		double totalLotFromAlex = exService.calculateTotalLotFromAlex();
		double totalIbFromTimeRange = exCommissionService.calculateIBFromTimeRange(fromDateConverted, toDateConverted);
		
		System.out.println(totalLotFromAlex + " - " + totalIbFromTimeRange);
		for (Exness exness : listExnessAlex) {
			ResponseIBToAlex newResponse = new ResponseIBToAlex();
			newResponse.setStt(String.valueOf(responseMap.size() + 1));
			newResponse.setNgayKhoiTao(exness.getMessage());
			newResponse.setIdLevel1("52151575");
			newResponse.setNameLevel1("Alex");
			newResponse.setIdLevel2(exness.getRefferal());
			Exness exRef = exRepo.findByExness(exness.getRefferal()).get();
			newResponse.setNameLevel2(exRef.getName());
			newResponse.setId(exness.getExness());
			Exness exLevel3 = exRepo.findByExness(exness.getExness()).get();
			newResponse.setNameLevel3(exLevel3.getName());
			newResponse.setServer(exness.getServer());
			newResponse.setPassword(exness.getPassword());
			newResponse.setCurrentBalance(exness.getBalance());
			newResponse.setPhanTram(exness.getReason());
			if (result.containsKey(Long.parseLong(exness.getExness()))) {
				newResponse.setIb(Double.parseDouble(result.get(Long.parseLong(exness.getExness())).getReward()));
			} else {
				newResponse.setIb(0);
			}

			responseMap.put(exness.getExness(), newResponse);
		}

		for (Exness exness : listExnessHHL) {
			ResponseIBToAlex newResponse = new ResponseIBToAlex();

			newResponse.setStt(String.valueOf(responseMap.size() + 1));
			newResponse.setNgayKhoiTao(exness.getMessage());
			newResponse.setId(exness.getExness());
			newResponse.setNameLevel1(exness.getName());
			newResponse.setServer(exness.getServer());
			newResponse.setPassword(exness.getPassword());
			newResponse.setLot(exness.getLot());
			newResponse.setCurrentBalance(exness.getBalance());
			newResponse.setPhanTram(exness.getReason());
			if (result.containsKey(Long.parseLong(exness.getExness()))) {
				newResponse.setIb(Double.parseDouble(result.get(Long.parseLong(exness.getExness())).getReward()));
			} else {
				newResponse.setIb(0);
			}

			responseMapToHHL.put(exness.getExness(), newResponse);
		}

		for (Exness exness : listExnessQueen) {
			ResponseIBToAlex newResponse = new ResponseIBToAlex();

			newResponse.setStt(String.valueOf(responseMap.size() + 1));
			newResponse.setNgayKhoiTao(exness.getMessage());
			newResponse.setId(exness.getExness());
			newResponse.setNameLevel1(exness.getName());
			newResponse.setServer(exness.getServer());
			newResponse.setPassword(exness.getPassword());
			newResponse.setCurrentBalance(exness.getBalance());
			newResponse.setPhanTram(exness.getReason());
			newResponse.setLot(exness.getLot());
			if (result.containsKey(Long.parseLong(exness.getExness()))) {
				newResponse.setIb(Double.parseDouble(result.get(Long.parseLong(exness.getExness())).getReward()));
			} else {
				newResponse.setIb(0);
			}

			responseMapToQueen.put(exness.getExness(), newResponse);
		}

		for (Exness exness : listExnessPixiu) {
			ResponseIBToAlex newResponse = new ResponseIBToAlex();

			newResponse.setStt(String.valueOf(responseMap.size() + 1));
			newResponse.setNgayKhoiTao(exness.getMessage());
			newResponse.setId(exness.getExness());
			newResponse.setNameLevel1(exness.getName());
			newResponse.setServer(exness.getServer());
			newResponse.setPassword(exness.getPassword());
			newResponse.setCurrentBalance(exness.getBalance());
			newResponse.setPhanTram(exness.getReason());
			newResponse.setLot(exness.getLot());
			if (result.containsKey(Long.parseLong(exness.getExness()))) {
				newResponse.setIb(Double.parseDouble(result.get(Long.parseLong(exness.getExness())).getReward()));
			} else {
				newResponse.setIb(0);
			}

			responseMapToPixiu.put(exness.getExness(), newResponse);
		}

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Alex");
			// Dòng 1: BẢNG THỐNG KÊ LỢI NHUẬN
			Row titleRow = sheet.createRow(0);
			Cell titleCell = titleRow.createCell(0);
			titleCell.setCellValue("BẢNG THỐNG KÊ TỪ IB NGÀY " + fromDate + " ĐẾN NGÀY " + toDate);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 18));
			titleRow.setHeight((short) 500);

			// Dòng 2: Tiêu đề cột
			Row headerRow = sheet.createRow(1);
			String[] headers = { "STT", "Ngày khởi tạo", "ID (Level1)", "Tên", "ID (Level2)", "Tên", "ID (Level3)",
					"Tên", "Vai trò", "Server", "Passview", "Vốn GD hiện tại", "% Kích", "IB" };

			CellStyle headerStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setBold(true);
			headerStyle.setFont(font);

			for (int i = 0; i < headers.length; i++) {
				Cell headerCell = headerRow.createCell(i);
				headerCell.setCellValue(headers[i]);
				headerCell.setCellStyle(headerStyle);
				sheet.setColumnWidth(i, 6_000);
			}

			int rowCount = 0;
			int index = 1;

			for (Map.Entry<String, ResponseIBToAlex> entry : responseMap.entrySet()) {
				ResponseIBToAlex value = entry.getValue();
				double accountCapital = transactionService.getTotalDepositByExnessIdAndTime(value.getId(), toDateConverted) / 100;
				double accountLot = exService.calculateAccountLot(value.getId());
				
				if (value.getIb() <= 0 || accountCapital <= 0) {
					continue;
				}
				
				Row dataRow = sheet.createRow(rowCount + 2); // Bắt đầu từ dòng 3

				// Thiết lập giá trị cho từng ô trong dòng
				dataRow.createCell(0).setCellValue(index);
				dataRow.createCell(1).setCellValue(value.getNgayKhoiTao());
				// level1
				dataRow.createCell(2).setCellValue(value.getIdLevel1());
				dataRow.createCell(3).setCellValue(value.getNameLevel1());
				// level2
				dataRow.createCell(4).setCellValue(value.getIdLevel2().equals("1") ? "52151575" : value.getIdLevel2());
				dataRow.createCell(5).setCellValue(value.getNameLevel2());
				// level3
				dataRow.createCell(6).setCellValue(Long.parseLong(value.getId()));
				dataRow.createCell(7).setCellValue(value.getNameLevel3());
				dataRow.createCell(8).setCellValue(value.getRole());

				dataRow.createCell(9).setCellValue(value.getServer());
				dataRow.createCell(10).setCellValue(value.getPassword());

				dataRow.createCell(11).setCellValue(value.getCurrentBalance());
				dataRow.createCell(12).setCellValue(value.getPhanTram());
				// xu ly ib
				
				double ibShared = calculateDistributedIB(value.getId(), totalLotFromAlex, accountLot, value.getIb(), totalIbFromTimeRange);
				dataRow.createCell(13).setCellValue(ibShared);

				rowCount++;
				index++;
			}

			// Canh giữa các ô trong sheet
			for (int i = 0; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row != null) {
					row.setHeight((short) 1_000); // Điều chỉnh độ cao của mỗi dòng
					for (int j = 0; j < row.getLastCellNum(); j++) {
						Cell cell = row.getCell(j);
						if (cell != null) {
							cellCellStyle(cell, workbook);
						}
					}
				}
			}

			// Tạo sheets cho HHL
			Sheet sheet2 = workbook.createSheet("HHL");
			// Dòng 1: BẢNG THỐNG KÊ LỢI NHUẬN
			Row titleRow2 = sheet2.createRow(0);
			Cell titleCell2 = titleRow2.createCell(0);
			titleCell2.setCellValue("BẢNG THỐNG KÊ TỪ IB NGÀY " + fromDate + " ĐẾN NGÀY " + toDate);
			sheet2.addMergedRegion(new CellRangeAddress(0, 0, 0, 18));
			titleRow2.setHeight((short) 500);

			// Dòng 2: Tiêu đề cột
			Row headerRow2 = sheet2.createRow(1);
			String[] headers2 = { "STT", "Tên", "Server", "ID", "Phần Trăm", "Vốn", "Hệ Số", "IB" };

			for (int i = 0; i < headers2.length; i++) {
				Cell headerCell2 = headerRow2.createCell(i);
				headerCell2.setCellValue(headers2[i]);
				headerCell2.setCellStyle(headerStyle);
				sheet2.setColumnWidth(i, 6_000);
			}

			int rowCount2 = 0;
			int index2 = 1;

			for (Map.Entry<String, ResponseIBToAlex> entry : responseMapToHHL.entrySet()) {
				ResponseIBToAlex value = entry.getValue();
				Row dataRow = sheet2.createRow(rowCount2 + 2); // Bắt đầu từ dòng 3
				if (value.getId().equals("1") || value.getIb() <= 0) {
					continue;
				}

				// Thiết lập giá trị cho từng ô trong dòng
				dataRow.createCell(0).setCellValue(index2);
				dataRow.createCell(1).setCellValue(value.getNameLevel1());
				dataRow.createCell(2).setCellValue(value.getServer());
				dataRow.createCell(3).setCellValue(Long.parseLong(value.getId()));
				dataRow.createCell(4).setCellValue(value.getPhanTram());
				dataRow.createCell(5).setCellValue(value.getCurrentBalance());
				dataRow.createCell(6).setCellValue(value.getLot());
				dataRow.createCell(7).setCellValue(value.getIb() * 0.8);

				rowCount2++;
				index2++;

			}

			// Canh giữa các ô trong sheet
			for (int i = 0; i <= sheet2.getLastRowNum(); i++) {
				Row row = sheet2.getRow(i);
				if (row != null) {
					row.setHeight((short) 1_000); // Điều chỉnh độ cao của mỗi dòng
					for (int j = 0; j < row.getLastCellNum(); j++) {
						Cell cell = row.getCell(j);
						if (cell != null) {
							cellCellStyle(cell, workbook);
						}
					}
				}
			}

			// Tạo sheets cho Queen
			Sheet sheet4 = workbook.createSheet("Queen");
			// Dòng 1: BẢNG THỐNG KÊ LỢI NHUẬN
			Row titleRow4 = sheet4.createRow(0);
			Cell titleCell4 = titleRow4.createCell(0);
			titleCell4.setCellValue("BẢNG THỐNG KÊ TỪ IB NGÀY " + fromDate + " ĐẾN NGÀY " + toDate);
			sheet4.addMergedRegion(new CellRangeAddress(0, 0, 0, 18));
			titleRow4.setHeight((short) 500);

			// Dòng 2: Tiêu đề cột
			Row headerRow4 = sheet4.createRow(1);
			String[] headers4 = { "STT", "Tên", "Server", "ID", "Phần Trăm", "Vốn", "Hệ Số", "IB" };

			for (int i = 0; i < headers4.length; i++) {
				Cell headerCell4 = headerRow4.createCell(i);
				headerCell4.setCellValue(headers4[i]);
				headerCell4.setCellStyle(headerStyle);
				sheet4.setColumnWidth(i, 6_000);
			}

			int rowCount4 = 0;
			int index4 = 1;

			for (Map.Entry<String, ResponseIBToAlex> entry : responseMapToQueen.entrySet()) {
				ResponseIBToAlex value = entry.getValue();
				Row dataRow = sheet4.createRow(rowCount4 + 2); // Bắt đầu từ dòng 3
				
				if (value.getIb() <= 0) {
					continue;
				}

				// Thiết lập giá trị cho từng ô trong dòng
				dataRow.createCell(0).setCellValue(index4);
				dataRow.createCell(1).setCellValue(value.getNameLevel1());
				dataRow.createCell(2).setCellValue(value.getServer());
				dataRow.createCell(3).setCellValue(Long.parseLong(value.getId()));
				dataRow.createCell(4).setCellValue(value.getPhanTram());
				dataRow.createCell(5).setCellValue(value.getCurrentBalance());
				dataRow.createCell(6).setCellValue(value.getLot());
				dataRow.createCell(7).setCellValue(value.getIb() * 0.8);

				rowCount4++;
				index4++;

			}

			// Canh giữa các ô trong sheet
			for (int i = 0; i <= sheet4.getLastRowNum(); i++) {
				Row row = sheet4.getRow(i);
				if (row != null) {
					row.setHeight((short) 1_000); // Điều chỉnh độ cao của mỗi dòng
					for (int j = 0; j < row.getLastCellNum(); j++) {
						Cell cell = row.getCell(j);
						if (cell != null) {
							cellCellStyle(cell, workbook);
						}
					}
				}
			}

			// Tạo sheets cho Pixiu
			Sheet sheet5 = workbook.createSheet("Pixiu Group");
			// Dòng 1: BẢNG THỐNG KÊ LỢI NHUẬN
			Row titleRow5 = sheet5.createRow(0);
			Cell titleCell5 = titleRow5.createCell(0);
			titleCell5.setCellValue("BẢNG THỐNG KÊ TỪ IB NGÀY " + fromDate + " ĐẾN NGÀY " + toDate);
			sheet5.addMergedRegion(new CellRangeAddress(0, 0, 0, 18));
			titleRow5.setHeight((short) 500);

			// Dòng 2: Tiêu đề cột
			Row headerRow5 = sheet5.createRow(1);
			String[] headers5 = { "STT", "Tên", "Server", "ID", "Phần Trăm", "Vốn", "Hệ Số", "IB" };

			for (int i = 0; i < headers5.length; i++) {
				Cell headerCell5 = headerRow5.createCell(i);
				headerCell5.setCellValue(headers5[i]);
				headerCell5.setCellStyle(headerStyle);
				sheet5.setColumnWidth(i, 6_000);
			}

			int rowCount5 = 0;
			int index5 = 1;

			for (Map.Entry<String, ResponseIBToAlex> entry : responseMapToPixiu.entrySet()) {
				ResponseIBToAlex value = entry.getValue();
				Row dataRow = sheet5.createRow(rowCount5 + 2); // Bắt đầu từ dòng 3
				
				if (value.getIb() <= 0) {
					continue;
				}

				// Thiết lập giá trị cho từng ô trong dòng
				dataRow.createCell(0).setCellValue(index5);
				dataRow.createCell(1).setCellValue(value.getNameLevel1());
				dataRow.createCell(2).setCellValue(value.getServer());
				dataRow.createCell(3).setCellValue(Long.parseLong(value.getId()));
				dataRow.createCell(4).setCellValue(value.getPhanTram());
				dataRow.createCell(5).setCellValue(value.getCurrentBalance());
				dataRow.createCell(6).setCellValue(value.getLot());
				dataRow.createCell(7).setCellValue(value.getIb() * 0.8);

				rowCount5++;
				index5++;
			}

			// Canh giữa các ô trong sheet
			for (int i = 0; i <= sheet5.getLastRowNum(); i++) {
				Row row = sheet5.getRow(i);
				if (row != null) {
					row.setHeight((short) 1_000); // Điều chỉnh độ cao của mỗi dòng
					for (int j = 0; j < row.getLastCellNum(); j++) {
						Cell cell = row.getCell(j);
						if (cell != null) {
							cellCellStyle(cell, workbook);
						}
					}
				}
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			System.out.println(System.currentTimeMillis() / 1000);
			return outputStream.toByteArray();
		}
	}

	private static String formatIfNumber(String input) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setGroupingSeparator('.');
		symbols.setDecimalSeparator(',');

		// Tạo DecimalFormat với ký hiệu mới
		DecimalFormat decimalFormat = new DecimalFormat("#.##", symbols);
		decimalFormat.setGroupingUsed(true);
		decimalFormat.setGroupingSize(3);

		try {
			double number = Double.parseDouble(input);
			return decimalFormat.format(number);
		} catch (NumberFormatException e) {
			return input;
		}
	}

	private static void cellCellStyle(Cell cell, Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		cell.setCellStyle(style);
	}

	private static String authentication() {
		String token = "";
		String url = "https://my.exnessaffiliates.com/api/v2/auth/";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("Accept", "application/json");

		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setLogin("Long_phan@ymail.com");
		loginRequest.setPassword("Xitrum11");

		HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

		ResponseEntity<AuthResponse> responseEntity = new RestTemplate().exchange(url, HttpMethod.POST, request,
				AuthResponse.class);
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			AuthResponse authResponse = responseEntity.getBody();
			token = authResponse.getToken();
		}
		return token;
	}
	
	private Map<Long, DataItem> getDataIBFromDatabase(String fromDate, String toDate) {
		Map<Long, DataItem> clientAccountMap = new HashMap<>();
		long fromDateConverted = convertToUnixTimestamp(fromDate, "GMT+7");
		long DateConverted = convertToUnixTimestamp(toDate, "GMT+7");
		
		
		List<ExnessCommission> listCommissions = exCommissionService.getExnessCommissionByTimeRange(fromDateConverted, DateConverted);
		for (ExnessCommission item : listCommissions) {
			if (clientAccountMap.containsKey(Long.parseLong(item.getClientAccount()))) {
				// Client_account exists, update the reward
				DataItem existingItem = clientAccountMap.get(Long.parseLong(item.getClientAccount()));
				existingItem.setReward(String.valueOf(Double.parseDouble(existingItem.getReward())
						+ item.getAmount()));
			} else {
				// Client_account doesn't exist, add new DataItem to the map
				DataItem newData = new DataItem();
				newData.setClient_account(Long.parseLong(item.getClientAccount()));
				newData.setReward(String.valueOf(item.getAmount()));
				newData.setId(Long.parseLong(item.getTransactionId()));
				clientAccountMap.put(Long.parseLong(item.getClientAccount()), newData);
			}
		}
		
		
		
		return clientAccountMap;
	}

	private static Map<Long, DataItem> getDataIB(String fromDate, String toDate)
			throws JsonMappingException, JsonProcessingException {
		Map<Long, DataItem> clientAccountMap = new HashMap<>();

		String token = authentication();

		// Gọi API khác với token
		// Ví dụ: Gửi yêu cầu GET đến một API sử dụng token
		String apiUrl = "https://my.exaffiliates.com/api/reports/rewards/?reward_date_from=" + fromDate
				+ "&reward_date_to=" + toDate + "&limit=1000";

		HttpHeaders headersWithToken = new HttpHeaders();
		headersWithToken.set("Authorization", "JWT " + token);

		HttpEntity<String> requestWithToken = new HttpEntity<>(headersWithToken);

		ResponseEntity<String> apiResponse = new RestTemplate().exchange(apiUrl, HttpMethod.GET, requestWithToken,
				String.class);

		String json = apiResponse.getBody();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(json); // Convert JSON to JsonNode

		double sum = 0;
		if (rootNode.has("totals")) {
			JsonNode totalsNode = rootNode.get("totals");
			if (totalsNode.has("reward")) {
				JsonNode dataNode = rootNode.get("data");
				if (dataNode.isArray()) {
					
					for (JsonNode itemNode : dataNode) {
						DataItem dataItem = objectMapper.treeToValue(itemNode, DataItem.class);
						sum += Double.parseDouble(dataItem.getReward());
						// Check if client_account already exists in the map
						if (clientAccountMap.containsKey(dataItem.getClient_account())) {
							// Client_account exists, update the reward
							DataItem existingItem = clientAccountMap.get(dataItem.getClient_account());
							existingItem.setReward(String.valueOf(Double.parseDouble(existingItem.getReward())
									+ Double.parseDouble(dataItem.getReward())));
						} else {
							// Client_account doesn't exist, add new DataItem to the map
							clientAccountMap.put(dataItem.getClient_account(), dataItem);
						}
					}

					// Add the total item to the map
//					DataItem total = new DataItem();
//					total.setClient_account(0L); // Assuming the client_account is of type Long
//					total.setClient_account_type("Total");
//					total.setReward(totalsNode.get("reward").asText());
//					clientAccountMap.put(total.getClient_account(), total);
				}
			}
		}

		return clientAccountMap;
	}
	
	public void readData() {
		String desktopPath = System.getProperty("user.home") + "/Desktop/";
		String fileName = "Report.xlsx";
		String filePath = desktopPath + fileName;
		String sheetName = "Sheet1";

		try (FileInputStream fis = new FileInputStream(new File(filePath));
				XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

			// Lấy sheet theo tên
			Sheet sheet = workbook.getSheet(sheetName);

			if (sheet != null) {
				// Lấy iterator cho các dòng
				Iterator<Row> rowIterator = sheet.iterator();

				// Bỏ qua dòng đầu tiên (header)
				if (rowIterator.hasNext()) {
					rowIterator.next();
				}

				// Đọc dữ liệu từ các dòng còn lại
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					
					if (isRowEmpty(row)) {
	                    continue; // Nếu dòng không có dữ liệu, bỏ qua và đọc dòng tiếp theo
	                }


					// Đọc các ô theo chỉ số cột
					String transactionId = row.getCell(0).getStringCellValue();
					String rewardDate = row.getCell(1).getStringCellValue();
					double amountReward = row.getCell(10).getNumericCellValue();
					String clientAccount = row.getCell(15).getStringCellValue();
					String currencyName = row.getCell(7).getStringCellValue();

					ExnessCommission item = new ExnessCommission();
					long unixTimestamp = convertToUnixTimestamp(rewardDate, "GMT+7");
					item.setTransactionId(transactionId);
					item.setDate(unixTimestamp);
					item.setAmount(amountReward);
					item.setCurrency(currencyName);
					item.setClientAccount(clientAccount);
					exCommissionService.saveExnessCommission(item);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static boolean isRowEmpty(Row row) {
	    if (row == null) {
	        return true;
	    }

	    Iterator<Cell> cellIterator = row.iterator();
	    while (cellIterator.hasNext()) {
	        Cell cell = cellIterator.next();
	        if (cell.getCellType() != CellType.BLANK) {
	            return false; // Nếu ô không trống, dòng có dữ liệu
	        }
	    }

	    return true; // Nếu tất cả các ô đều trống, dòng không có dữ liệu
	}
	
	public static long convertToUnixTimestamp(String dateString, String timeZoneId) {
        try {
            // Định dạng của chuỗi ngày
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            // Đặt múi giờ cho đối tượng DateFormat
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            
            // Chuyển đổi chuỗi ngày thành đối tượng Date
            Date date = dateFormat.parse(dateString);
            
            // Sử dụng Calendar để thiết lập giờ, phút và giây
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            
            // Lấy giá trị Unix timestamp
            long unixTimestamp = calendar.getTimeInMillis() / 1000; // Chia cho 1000 để chuyển từ mili giây sang giây
            return unixTimestamp;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Trả về giá trị âm để chỉ ra lỗi
        }
    }

	public double calculateDistributedIB(String exnessId, double totalLot, double accountLot, double accountIb,
			double dailyIB) {
		double threshold = 50_000;

		double totalSalesAmount = totalLot/0.01*200;
		
		double rate = 0.5;

		if (totalSalesAmount < threshold) {
			return 0.0;
		}

		double ibPerDollar = dailyIB / totalSalesAmount;

		double ibShareFromBracket = Math.max(0, ibPerDollar * (totalSalesAmount - threshold));

		double ibRateConverted = ibShareFromBracket / dailyIB * accountIb * rate;

		return ibRateConverted;
	}

}
