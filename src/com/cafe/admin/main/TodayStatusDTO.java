package com.cafe.admin.main;

public class TodayStatusDTO {
	private String todayMenuName; // ������ ȿ��ǰ��
	private String thumbnail;
	private int todayTotalSales;// ������ �����

	public String getTodayMenuName() {
		return todayMenuName;
	}

	public void setTodayMenuName(String todayMenuName) {
		this.todayMenuName = todayMenuName;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public int getTodayTotalSales() {
		return todayTotalSales;
	}

	public void setTodayTotalSales(int todayTotalSales) {
		this.todayTotalSales = todayTotalSales;
	}

}
