package com.stat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
	private String stt;
    private String ngayKhoiTao;
    private String ten;
    private String server;
    private String password;
    private String id;
    private String refferal;
    private String soTienKhoiTao;
    private String chiSoDanh;
    private String phanTram;
    private String soTienNapThem;
    private String lenhNapGanNhat;
    private String soTienDaRut;
    private String loiNhuan;
    private String soDuTruoc;
    private String soDuSau;
    private String lot;
}
