package com.cafe.members;

public class CardIdentityMaker {
	
	public String getCardIdentity() {
		StringBuilder s = new StringBuilder();
		long millis = System.currentTimeMillis(); //13�ڸ��� ������
		s.append(millis + String.format("%03d",(int)(Math.random()*1000)));
		return s.toString();
	}
	
}
