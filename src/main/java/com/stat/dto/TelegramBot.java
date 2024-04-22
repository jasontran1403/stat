package com.stat.dto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stat.service.StatisticService;
import com.stat.user.Exness;
import com.stat.user.ExnessRepository;
import com.stat.user.Statistic;
import com.stat.user.User;
import com.stat.user.UserRepository;
import com.stat.utils.ChartGenerator;
import com.stat.utils.ExcelGenerator;

public class TelegramBot extends TelegramLongPollingBot {
	@Autowired
	ExcelGenerator excelGenerator;

	@Autowired
	UserRepository userRepo;

	@Autowired
	ExnessRepository exRepo;

	@Autowired
	StatisticService statService;

	@Override
	public String getBotUsername() {
		return "trading_stat_bot";
	}

	@Override
	public String getBotToken() {
		return "6655823795:AAHmc7kfVNeRYjvw3wmp3p2Mkh5BdZ6hwCY";
	}

	public TelegramBot() {
//		extractExnessIds(input, setAvoidPayIB);
	}

	private static Set<String> setAvoidPayIB = new HashSet<>();

	private enum BotState {
		NONE, WAITING_FOR_EXNESS_LIST, WAITING_FOR_ADD, WAITING_FOR_RETURN_LIST_EXNESS, WAITING_FOR_EXNESS_ID,
		WAITING_FOR_EXNESS_CHECK, WAITING_FOR_DATE_INPUT, WAITING_FOR_TIME_INPUT_IB, WAITING_FOR_TIME_INPUT_PROFIT
	}

	private BotState botState = BotState.NONE;

	// Phương thức gửi tin nhắn
	public void sendMessageToChat(String chatId, String message) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId);
		sendMessage.setText(message);
		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			System.out.println(e.getMessage());
		}
	}

	public void sendPhoto(String chatId, File photo) {
		SendPhoto sendPhoto = new SendPhoto();
		sendPhoto.setChatId(chatId); // Replace with your Telegram chat ID

		InputFile inputFile = new InputFile(photo);
		sendPhoto.setPhoto(inputFile);

		try {
			execute(sendPhoto);
		} catch (TelegramApiException e) {
			System.out.println(e.getMessage());
		}
	}

	public void sendExcelDocument(String chatId, byte[] excelData) {
		SendDocument sendDocument = new SendDocument();
		sendDocument.setChatId(chatId);

		InputStream inputStream = new ByteArrayInputStream(excelData);
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
		String formattedDate = dateFormat.format(date);
		InputFile inputFile = new InputFile(inputStream, "Report-" + formattedDate + ".xlsx");
		sendDocument.setDocument(inputFile);

		try {
			execute(sendDocument);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageText = update.getMessage().getText();
			String chatId = String.valueOf(update.getMessage().getChatId());
			if (messageText.equals("/start")) { // Ví dụ: Khi người dùng gửi "/start"
				sendMenu(String.valueOf(chatId));
			} else if (messageText.equals("Xuất file thu chi")) {

				Date currentDateTime = new Date();

				// Lấy ngày hiện tại
				TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
				Calendar calendar = Calendar.getInstance(timeZone);
				calendar.setTime(currentDateTime);

				// Lấy timestamp sau khi đặt thời gian
				long timestamp = calendar.getTimeInMillis() / 1000;
				List<Statistic> listData = statService.getStatisticByTime(0, timestamp);

				double totalBalance = statService.getTotalBalance();
				ChartGenerator chartGenerator = new ChartGenerator();
				try {
					sendExcelDocument(chatId, chartGenerator.generatePieChart(listData, totalBalance));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				botState = BotState.NONE;
				sendMenu(chatId);
			} else if (messageText.equals("Xuất file báo cáo IB")) {
				sendMessageToChat(chatId, "Nhập ngày cần xuất báo cáo: ");
				botState = BotState.WAITING_FOR_TIME_INPUT_IB;
			} else if (botState == BotState.WAITING_FOR_TIME_INPUT_IB) {
				String fromDate = "", toDate = "";
				if (messageText.equalsIgnoreCase("thoát")) {
					botState = BotState.NONE;
					sendMenu(chatId);
				} else {
					String[] dateRange = messageText.split(" ");
					if (dateRange.length == 2) {
						fromDate = dateRange[0];
						toDate = dateRange[1];
					} else {
						sendMessageToChat(chatId, "Vui lòng nhập ngày cần kiểm tra IB.");
						botState = BotState.WAITING_FOR_DATE_INPUT;
						sendMenu(chatId);
					}
				}
				try {
					String fromDateConverted = convertParamToValid(fromDate);
					String toDateConverted = convertParamToValid(toDate);

					sendExcelDocument(chatId, excelGenerator.generateoIBToExcel(fromDateConverted, toDateConverted));
					
					botState = BotState.NONE;
					sendMenu(chatId);
				} catch (ParseException pae) {
					sendMessageToChat(chatId, "Ngày không hợp lệ!");
					botState = BotState.NONE;
					sendMenu(chatId);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					sendMessageToChat(chatId, "Lỗi trong quá trình đọc/ghi file!");
					botState = BotState.NONE;
					sendMenu(chatId);
				}
			} else if (messageText.equals("Xuất file báo cáo lợi nhuận")) {
				sendMessageToChat(chatId, "Nhập tháng cần xuất báo cáo: ");
				botState = BotState.WAITING_FOR_TIME_INPUT_PROFIT;
			} else if (botState == BotState.WAITING_FOR_TIME_INPUT_PROFIT) {
				if (messageText.equalsIgnoreCase("thoát")) {
					botState = BotState.NONE;
					sendMenu(chatId);
				} else {
					if (messageText.length() >  0) {
					} else {
						sendMessageToChat(chatId, "Vui lòng nhập tháng cần kiểm tra Profit.");
						botState = BotState.WAITING_FOR_DATE_INPUT;
						sendMenu(chatId);
					}
				}
				try {
					long[] dateConverted = convertDateToUnix(messageText);
					System.out.println(dateConverted[0] + " - " + dateConverted[1]);
					sendExcelDocument(chatId, excelGenerator.generateExcelAlex(dateConverted[0], dateConverted[1]));
					botState = BotState.NONE;
					sendMenu(chatId);
				} catch (ParseException pae) {
					sendMessageToChat(chatId, "Ngày không hợp lệ!");
					botState = BotState.NONE;
					sendMenu(chatId);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					sendMessageToChat(chatId, "Lỗi trong quá trình đọc/ghi file!");
					botState = BotState.NONE;
					sendMenu(chatId);
				}
			} else if (messageText.equals("Kiểm tra IB")) {
				sendMenuChosseDate(chatId);
				botState = BotState.WAITING_FOR_DATE_INPUT;
			} else if (botState == BotState.WAITING_FOR_DATE_INPUT) {
				if (messageText.equalsIgnoreCase("thoát")) {
					botState = BotState.NONE;
					sendMenu(chatId);
				} else {
					String fromDate = "";
					String toDate = "";
					if (messageText.equalsIgnoreCase("Hôm qua")) {
						// Chuyển đối tượng Date thành chuỗi với định dạng "yyyy-MM-dd"
						fromDate = getDate(1);
						toDate = getDate(1);
					} else if (messageText.equalsIgnoreCase("Hôm nay")) {
						fromDate = getDate(0);
						toDate = getDate(0);
					} else {
						// Trường hợp còn lại: Xử lý ngày bắt đầu và ngày kết thúc từ messageText
						String[] dateRange = messageText.split(" ");
						if (dateRange.length == 2) {
							fromDate = dateRange[0];
							toDate = dateRange[1];
						} else {
							sendMessageToChat(chatId, "Vui lòng nhập ngày cần kiểm tra IB.");
							botState = BotState.WAITING_FOR_DATE_INPUT;
							sendMenu(chatId);
						}
					}

					if (isValidDateFormat(fromDate) && isValidDateFormat(toDate)) {
						if (!isValidDateRange(fromDate, toDate)) {
							sendMessageToChat(chatId, "Ngày bắt đầu phải trước hoặc bằng với ngày kết thúc.");
							botState = BotState.NONE;
							sendMenu(chatId);
						} else {
							String message = "";
							try {
								message = processData(fromDate, toDate);
							} catch (JsonProcessingException e) {
								message = e.getMessage();
							} catch (ResourceAccessException e) {
								// TODO Auto-generated catch block
								message = e.getMessage();
							} catch (ConnectException e) {
								// TODO Auto-generated catch block
								message = e.getMessage();
							}

							sendMessageToChat(chatId, message);
							botState = BotState.NONE;
							sendMenu(chatId);
						}
					} else {
						sendMessageToChat(chatId, "Định dạng ngày không hợp lệ. Sử dụng định dạng yyyy-MM-dd.");
						botState = BotState.WAITING_FOR_DATE_INPUT;
						sendMenu(chatId);
					}
				}
			} else if (messageText.equals("Kiểm tra tài khoản Exness")) {
				sendFormExness(chatId);
				botState = BotState.WAITING_FOR_EXNESS_ID;
			} else if (botState == BotState.WAITING_FOR_EXNESS_ID) {
				if (messageText.equalsIgnoreCase("thoát")) {
					botState = BotState.NONE;
					sendMenu(chatId);
				} else {
					String result = "";
					String exnessId = messageText.trim();
					try {
						result = validateExness(exnessId);
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						result = e.getMessage();
					} catch (ResourceAccessException e) {
						// TODO Auto-generated catch block
						result = e.getMessage();
					} catch (ConnectException e) {
						// TODO Auto-generated catch block
						result = e.getMessage();
					}

					sendMessageToChat(chatId, result);
					botState = BotState.NONE;
					sendMenu(chatId);
				}
			} else {
				sendMessageToChat(chatId, "Xin lỗi, chức năng bạn chọn không tồn tại!");
				sendMenu(chatId);
				botState = BotState.NONE;
			}
		}
	}

	public void sendMenu(String chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Vui lòng chọn chức năng trên menu:");

		// Tạo hàng cho nút thứ nhất
		KeyboardRow row1 = new KeyboardRow();
		row1.add("Xuất file thu chi");

		KeyboardRow row2 = new KeyboardRow();
		row2.add("Xuất file báo cáo IB");

		KeyboardRow row3 = new KeyboardRow();
		row3.add("Xuất file báo cáo lợi nhuận");

		KeyboardRow row4 = new KeyboardRow();
		row4.add("Danh sách bỏ qua IB");

		KeyboardRow row5 = new KeyboardRow();
		row5.add("Kiểm tra tài khoản Exness");

		KeyboardRow row6 = new KeyboardRow();
		row6.add("Kiểm tra IB");

		// Thêm cả hai hàng vào bàn phím
		List<KeyboardRow> keyboard = new ArrayList<>();
		keyboard.add(row1);
		keyboard.add(row2);
		keyboard.add(row3);
		keyboard.add(row4);
		keyboard.add(row5);
		keyboard.add(row6);

		// Thêm hàng vào bàn phím
		ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup();
		replyMarkup.setKeyboard(keyboard);
		message.setReplyMarkup(replyMarkup);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public void sendFormExness(String chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Nhập Exness ID:");

		// Tạo hàng cho nút thứ nhất
		KeyboardRow row1 = new KeyboardRow();
		row1.add("Thoát");

		// Thêm cả hai hàng vào bàn phím
		List<KeyboardRow> keyboard = new ArrayList<>();
		keyboard.add(row1);

		// Thêm hàng vào bàn phím
		ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup();
		replyMarkup.setKeyboard(keyboard);
		message.setReplyMarkup(replyMarkup);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public String getDate(int dayCount) {
		Date currentDateTime = new Date();

		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDateTime);

		// Đặt thời gian thành 00:00:01
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		// Lấy timestamp sau khi đặt thời gian
		long timestamp = calendar.getTimeInMillis() / 1000 - (86400 * dayCount);

		// Tạo đối tượng SimpleDateFormat với định dạng "yyyy-MM-dd"
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		// Chuyển đổi timestamp thành đối tượng Date
		Date date = new Date(timestamp * 1000); // *1000 để đổi về milliseconds

		// Chuyển đối tượng Date thành chuỗi với định dạng "yyyy-MM-dd"
		return dateFormat.format(date);
	}

	public void sendMenuChosseDate(String chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Vui lòng chọn ngày:");

		// Tạo hàng cho nút thứ nhất
		KeyboardRow row1 = new KeyboardRow();
		row1.add("Hôm nay");

		// Tạo hàng cho nút thứ nhất
		KeyboardRow row2 = new KeyboardRow();
		row2.add("Hôm qua");

		KeyboardRow row3 = new KeyboardRow();
		row3.add("Thoát");

		// Thêm cả hai hàng vào bàn phím
		List<KeyboardRow> keyboard = new ArrayList<>();
		keyboard.add(row1);
		keyboard.add(row2);
		keyboard.add(row3);
		// Thêm hàng vào bàn phím
		ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup();
		replyMarkup.setKeyboard(keyboard);
		message.setReplyMarkup(replyMarkup);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
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

	private static List<DataItem> getDataIB(String fromDate, String toDate)
			throws JsonMappingException, JsonProcessingException {
		List<DataItem> dataItems = new ArrayList<>();
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

		if (rootNode.has("totals")) {
			JsonNode totalsNode = rootNode.get("totals");
			if (totalsNode.has("reward")) {
				JsonNode dataNode = rootNode.get("data");
				if (dataNode.isArray()) {
					for (JsonNode itemNode : dataNode) {
						DataItem dataItem = objectMapper.treeToValue(itemNode, DataItem.class);

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

					// Add all items from the map to the result list
					dataItems.addAll(clientAccountMap.values());

					// Add the total item
					DataItem total = new DataItem();
					total.setClient_account(0);
					total.setClient_account_type("Total");
					total.setReward(totalsNode.get("reward").asText());
					dataItems.add(total);
				}
			}
		}

		return dataItems;
	}

	public String validateExness(String exnessId)
			throws JsonMappingException, JsonProcessingException, ConnectException, ResourceAccessException {

		String token = authentication();

		// Gọi API khác với token
		// Ví dụ: Gửi yêu cầu GET đến một API sử dụng token
		String apiUrl = "https://my.exnessaffiliates.com/api/reports/clients/?client_account=" + exnessId;

		HttpHeaders headersWithToken = new HttpHeaders();
		headersWithToken.set("Authorization", "JWT " + token);

		HttpEntity<String> requestWithToken = new HttpEntity<>(headersWithToken);

		ResponseEntity<String> apiResponse = new RestTemplate().exchange(apiUrl, HttpMethod.GET, requestWithToken,
				String.class);
		String json = apiResponse.getBody();

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(json); // Chuyển JSON thành một đối tượng JsonNode
		String message = "";

		JsonNode dataNode = rootNode.get("data");

		if (dataNode.isArray() && dataNode.size() > 0) {
			// Lấy phần tử đầu tiên trong mảng
			JsonNode firstDataItem = dataNode.get(0);

			message += "Ngày tạo:" + firstDataItem.get("reg_date").asText() + "\n";
			message += "Tổng IB:" + rootNode.get("totals").get("reward").asText() + "\n";
			return message;
		} else {
			return "Tài khoản Exness không nằm dưới Long_phan@ymail.com";
		}
	}

	private Map<String, String> loadExnessIds() {
		Map<String, String> exnessIdMap = new HashMap<>();

		// Thực hiện truy vấn để lấy tất cả exnessId và branchName
		List<User> userList = userRepo.findAll(); // (hoặc sử dụng phương thức tương ứng)

		for (User user : userList) {
			for (Exness exness : user.getExnessList()) {
				exnessIdMap.put(exness.getExness(), user.getBranchName());
			}
		}

		return exnessIdMap;
	}

	public String processData(String fromDate, String toDate)
			throws JsonMappingException, JsonProcessingException, ConnectException, ResourceAccessException {
		// Tải tất cả exnessId vào HashMap trước vòng lặp
		Map<String, String> exnessIdMap = loadExnessIds();
		String total = "";
		String alex = "";
		String hhl = "";
		String queen = "";
		String bh = "";
		String vu = "";

		LocalDate currentDate = LocalDate.now();

		// Chuyển chuỗi thành LocalDate
		LocalDate localDate = LocalDate.parse(fromDate);

		// Lấy giá trị ngày
		int dateToProcess = localDate.getDayOfMonth();

		// Lấy số ngày của tháng hiện tại
		int daysInMonth = YearMonth.from(currentDate).lengthOfMonth();

		Date currentDateTime = new Date();

		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDateTime);

		// Đặt thời gian thành 00:00:01
		calendar.set(Calendar.HOUR_OF_DAY, 16);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.DATE, dateToProcess);

		// Lấy timestamp sau khi đặt thời gian
		long timestamp = calendar.getTimeInMillis() / 1000;

		double totalAmountAlex = 0, totalAmountHHL = 0, totalAmountQueen = 0, totalAmountVu = 0;

		// Tiếp tục với vòng lặp của bạn
		List<DataItem> result = getDataIB(fromDate, toDate);

		if (result.size() > 0) {
			for (DataItem item : result) {
				if (item.getId() == 0 && !item.getReward().equals("null")) {
					total += item.getReward();
				}

				long exnessId = item.getClient_account();
				double amount = 0;
				if (item.getReward().equals("null")) {
					amount = 0;

				} else {
					amount = Double.parseDouble(item.getReward());
				}

				if (exnessIdMap.containsKey(String.valueOf(exnessId))) {
					String branchName = exnessIdMap.get(String.valueOf(exnessId));
					if (branchName.equalsIgnoreCase("alex")) {
						double amountAlex = amount / 2;
						totalAmountAlex += amountAlex;
						String formattedAmountAlex = String.format("%.2f", amountAlex);
						alex += "ExnessId " + exnessId + " - " + formattedAmountAlex + " branch " + branchName + "\n";
					} else if (branchName.equalsIgnoreCase("hhl")) {
						double amountHHL = amount * 0.8;
						totalAmountHHL += amountHHL;
						String formattedAmountHHL = String.format("%.2f", amountHHL);
						hhl += "ExnessId " + exnessId + " - " + formattedAmountHHL + " branch " + branchName + "\n";
					} else if (branchName.equalsIgnoreCase("lisa")
							&& !setAvoidPayIB.contains(String.valueOf(exnessId))) {
						double amountQueen = amount * 0.8;
						totalAmountQueen += amountQueen;
						String formattedAmountQueen = String.format("%.2f", amountQueen);
						queen += "ExnessId " + exnessId + " - " + formattedAmountQueen + " branch " + branchName + "\n";
					} else if (branchName.equalsIgnoreCase("PixiuGroup")) {
						double amountVu = amount * 0.8;
						totalAmountVu += amountVu;
						String formattedAmountVu = String.format("%.2f", amountVu);
						vu += "ExnessId " + exnessId + " - " + formattedAmountVu + " branch " + branchName + "\n";
					}
				}

			}

			String message = "";
			alex += "Tổng IB chia nhánh Alex " + totalAmountAlex;
			hhl += "Tổng IB chia nhánh HHL " + totalAmountHHL;
			queen += "Tổng IB chia nhánh Queen " + totalAmountQueen;
			vu += "Tổng IB chia nhánh Vũ " + totalAmountVu;

			if (total.length() == 0) {
				message += "Tổng IB từ ngày " + fromDate + " đến ngày " + toDate + " = " + 0 + "\n";
			} else {
				message += "Tổng IB từ ngày " + fromDate + " đến ngày " + toDate + " = " + total + "\n";
			}

			message += alex + "\n\n";
			message += hhl + "\n\n";
			message += queen + "\n\n";
			message += vu + "\n\n";

			return message;
		} else {
			return "Từ " + fromDate + " đến " + toDate + " không có IB!";
		}
	}

	private static boolean isValidDateFormat(String dateStr) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setLenient(false);

		try {
			// Parsing chuỗi thành đối tượng Date
			dateFormat.parse(dateStr);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	private static boolean isValidDateRange(String fromDate, String toDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			// Chuyển đổi chuỗi thành đối tượng Date
			Date fromDateObj = dateFormat.parse(fromDate);
			Date toDateObj = dateFormat.parse(toDate);

			// So sánh ngày
			if (fromDateObj.before(toDateObj) || fromDateObj.equals(toDateObj)) {
				return true;
			} else {
				return false;
			}
		} catch (ParseException e) {
			// Xử lý lỗi chuyển đổi chuỗi thành đối tượng Date
			e.printStackTrace();
			return false;
		}
	}

	private static String convertParamToValid(String date) throws ParseException {
		String inputDateFormat = "dd-MM-yyyy";
		String outputDateFormat = "yyyy-MM-dd";

		// Tạo đối tượng SimpleDateFormat cho cả hai định dạng
		SimpleDateFormat inputFormatter = new SimpleDateFormat(inputDateFormat);
		SimpleDateFormat outputFormatter = new SimpleDateFormat(outputDateFormat);

		// Chuyển đổi từ string sang Date
		Date inputDate = inputFormatter.parse(date);

		// Chuyển đổi từ Date sang string với định dạng mới
		String outputDateStr = outputFormatter.format(inputDate);

		return outputDateStr;
	}
	
	private static long[] convertDateToUnix(String date) throws ParseException {
		long[] result = new long[2];
		SimpleDateFormat inputFormatter = new SimpleDateFormat("MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(inputFormatter.parse(date));

        // Đặt giờ, phút, giây về 0 để lấy ngày đầu tiên của tháng
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Lấy unixtimestamp của ngày đầu tiên của tháng hiện tại
        result[0] = calendar.getTimeInMillis() / 1000;

        // Tăng tháng lên 1 để lấy tháng sau
        calendar.add(Calendar.MONTH, 1);

        // Lấy unixtimestamp của ngày đầu tiên của tháng sau
        result[1] = calendar.getTimeInMillis() / 1000;
		return result;
	}
}
