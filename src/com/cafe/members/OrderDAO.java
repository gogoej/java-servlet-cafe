package com.cafe.members;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.cafe.menu.MenuDTO;
import com.util.DBCPConn;

public class OrderDAO {
	//OrderHistoryDTO (�ֹ�������) ���� �߰��ؾ� ��.
	//OrderDetailDTO (�ֹ����λ���)
	
	public int addOrderHistory(SessionCart cart, int userNum, int cardNum) throws OrderException{
		/*
		 ���� ����
		 1. ������ ���� ī�忡 �ܾ��� �����ݾ� �̻����� Ȯ��
		 2. ������ ���� ī�尡 ���� ������ ���� �´��� Ȯ�� (security check)
		 * */
		int result = 0;
		int totalPaymentAmount = cart.getTotalPaymentAmount();
		Connection conn = DBCPConn.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql;
		try {
			conn.setAutoCommit(false);

			//1. ����ī�� �ܾ� �����ݾ� �̻����� Ȯ���ϱ�
			sql = "SELECT cardNum FROM cards WHERE cardNum = ? AND balance >= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, cardNum);
			pstmt.setInt(2, totalPaymentAmount);
			rs = pstmt.executeQuery();
			if(rs.next()==false) {//�����ݾ��� �ܾ׺��� ũ�ٸ� ���� ���� ��ȯ���� ���� ����
				throw new OrderException("�ܾ��� �����մϴ�");
			}
			returnDBResources(pstmt, rs);
			
			//2. �ֹ�����(order_history)�� ���� ����ϱ�
			//2-1. �ֹ����� orderNum �Ϸù�ȣ �̸� ��������
			sql = "SELECT order_history_seq.NEXTVAL FROM DUAL";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next()==false) {
				throw new OrderException("������ ���� ��ȣ �������� ���� (order_history_seq.NEXTVAL)");
			}
			
			int orderNum = rs.getInt(1);
			returnDBResources(pstmt, rs);
			
			//2-2.
			//�� ����.. ���� order_status�� 1���� ���� ��ϵǾ� �־�� ��!!!
			final int statusNum = 1;//���� �Ϸ�
			
			sql = "INSERT INTO order_history(orderNum, totalPaymentAmount, statusNum, userNum, cardNum) "
					+ " VALUES(?, ?, ?, ?, ?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, orderNum);
			pstmt.setInt(2, totalPaymentAmount);
			pstmt.setInt(3, statusNum);
			pstmt.setInt(4, userNum);
			pstmt.setInt(5, cardNum);
			result = pstmt.executeUpdate();
			
			if(result==0) {
				throw new OrderException("�ֹ����� ������ �����Ͽ����ϴ�.");
			}
			//�ڿ� �ݳ�
			returnDBResources(pstmt, rs);
			
			
			//3. �ֹ� �� �����
			sql = "INSERT INTO order_detail(detailNum, orderNum, unitPrice, quantity, paymentAmount) "
					+ " VALUES(order_detail_seq.NEXTVAL, ?, ?, ?, ?)";
			List<MenuDTO> items = cart.getItems();
			for(MenuDTO item: items) {
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, orderNum);
				pstmt.setInt(2, item.getPrice());
				pstmt.setInt(3, 1); //�ϴ� 1���� ����
				pstmt.setInt(4, item.getPrice()*1);//�ܰ�*����(1�� ����)
				pstmt.executeUpdate();
				returnPrepResource(pstmt);
			}
			
			conn.commit();
		}catch(OrderException e) {
			throw new OrderException(e);
		}
		catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				conn.setAutoCommit(true);
			} catch (Exception e2) {
			}
			returnDBResources(pstmt, rs);
			//���� �ݱ�
			try {
				if(!conn.isClosed()) {
					DBCPConn.close(conn);
				}
			} catch (Exception e2) {
			}
		}
		
		return result;
	}
	
	
	//�ڿ��ݳ� ����ϱ�..
	public void returnDBResources(PreparedStatement pstmt, ResultSet rs) {
		returnResultSetResource(rs);
		returnPrepResource(pstmt);
	}
	
	public void returnPrepResource(PreparedStatement pstmt) {
		try {
			pstmt.close();
		} catch (Exception e) {
		}
		pstmt = null;
	}
	
	public void returnResultSetResource(ResultSet rs) {
		try {
			rs.close();
		} catch (Exception e) {
		}
		rs = null;
	}
}
