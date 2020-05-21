package com.util;

import java.util.ArrayList;
import java.util.List;

public class Pager {
	public int pageCount(int rows, int dataCount) {
		// �������� ������ 0�� ��� ó�� ���
		if (dataCount <= 0) {
			return 0;
		}
		// �� ������ �� = dataCount / �� ��
		// �׷��� dataCount / �� ���� �������� �����ϸ� +1�� �� �ؾ� �Ѵ�.
		// ex:97���� �����Ͱ� ������ 97/10=9�� �ƴ϶� 9+1�� �� 10�� ���;� �Ѵٴ� ���̴�.
		return dataCount / rows + (dataCount % rows > 0 ? 1 : 0);
	}

	public int[] paging(int current_page, int total_page) {
		return paging(10, current_page, total_page);
	}

	public int[] paging(int rows, int current_page, int total_page) {

//		int center = (rows / 2)!=0?rows/2:0;
//		int start = current_page - center > 0 ? current_page - center : 1;
//		int end;
//		int page = start;

		int maxShowPage = 9; // ���̴� ������ ���� ���ϴ� ����!!
		int length; // ���� �迭�� ����
		int centerPosition = 5;// ��� ��ġ (5��°)
		int start, end;// ����, �� ������
		start = current_page - centerPosition + 1 > 0 ? current_page - centerPosition + 1 : 1;
		end = current_page + centerPosition - 1 < total_page ? current_page + centerPosition - 1 : total_page;
		length = end - start + 1; //���ڸ��� ���ڶ��
		if(length<maxShowPage) {
			end += maxShowPage - length;
		}
		if(end>total_page) {
			end = total_page;
		}
//		System.out.println(String.format("start=%s, end=%s, length=%s / total_page=%s",start,end,length, total_page));

		List<Integer> pages = new ArrayList<>();
		int page = start;
		while (page <= end) {
//		while(page <= total_page && page < start+numPerBlock) {
			pages.add(page++);
		}

		return pages.stream().mapToInt(i -> i).toArray();
	}
}

/*
 * 95���������� �ִٰ� ���� 1 2 3 4 5 6 7 8 9 10 [ó��] [����] 6 7 8 9 10 11 12 13 14 15 [����]
 * [��]
 * 
 * 
 * [ó��] [����] 91 92 93 94 95
 */