package com.stat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.stat.dto.TelegramBot;
import com.stat.service.StatisticService;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
@EnableScheduling
public class SecurityApplication {
	@Autowired
	StatisticService statService;
	
	private final TelegramBot tele;
	private final String chatId = String.valueOf("-1002107535161");
	private final String userId = String.valueOf("1008362877");

	public static void main(String[] args) {
		SpringApplication.run(SecurityApplication.class, args);
	}

//	@Scheduled(cron = "0 0 16 * * *", zone = "GMT+7:00")
//	public void sendMessage() throws Exception {
//		String fromDate = tele.getDate(0);
//		String toDate = tele.getDate(0);
//		String message = tele.processData(fromDate, toDate);
//		tele.sendMessageToChat(chatId, message);
//	}
}
