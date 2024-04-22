package com.stat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseIBToAlex {
	private String stt;
    private String ngayKhoiTao;
    private String idLevel1;
    private String nameLevel1;
    private String idLevel2;
    private String nameLevel2;
    private String id;
    private String nameLevel3;
    private String refferal;
    private String role;
    private String server;
    private String password;
    private double currentBalance;
    private String phanTram;
    private double ib;
    private double lot;
}
