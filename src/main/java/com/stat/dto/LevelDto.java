package com.stat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LevelDto {
	private String exnessId;
	private double amount;
	private double rate;
	private double sales;
}

