package com.cafe.members;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
			sql = "SELECT cardNum FROM cards WHERE cardNum = ? AND balance >= ? AND userNum = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, cardNum);
			pstmt.setInt(2, totalPaymentAmount);
			pstmt.setInt(3, userNum); //�Դٰ� ī�� �����ֱ��� ��ȸ�ϱ�
			rs = pstmt.executeQuery();
			if(rs.next()==false) {//�����ݾ��� �ܾ׺��� ũ�ٸ� ���� ���� ��ȯ���� ���� ����
				throw new OrderException("�ܾ��� �����մϴ�.");
				//�׷��� ������ üũ AND userNum = ? ���� �����Ƿ� ���� ī���ȣ�� ��û�� ��쿡�� �̰����� �ɸ���.
				//�ٸ� Ư���� ��Ȳ�̹Ƿ� ������ ����ȭ�Ͽ� ������ �ʾ��� .(DB��û �ּ�ȭ)
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
			sql = "INSERT INTO order_detail(detailNum, orderNum, menuNum, unitPrice, quantity, paymentAmount) "
					+ " VALUES(order_detail_seq.NEXTVAL, ?, ?, ?, ?, ?)";
			Map<Integer, MenuDTO> items = cart.getItems();
			for(int menuNum: items.keySet()) {
				MenuDTO item = items.get(menuNum);
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, orderNum);
				pstmt.setInt(2, item.getMenuNum());
				pstmt.setInt(3, item.getPrice());
				pstmt.setInt(4, item.getQuantity()); //�ϴ� 1���� ����
				pstmt.setInt(5, item.getPrice()*item.getQuantity());//�ܰ�*����(1�� ����)
				pstmt.executeUpdate();
				returnPrepResource(pstmt);
			}
			
			conn.commit();
		}catch(OrderException e) {
			try {
				if(conn!=null && !conn.isClosed()) {
					conn.rollback();
				}
			} catch (SQLException e1) {
			}
			throw new OrderException(e);
		}
		catch (Exception e) {
			try {
				if(conn!=null && !conn.isClosed()) {
					conn.rollback();
				}
			} catch (SQLException e1) {
			}
			e.printStackTrace();
		}finally {
			try {
				if(conn!=null && !conn.isClosed()) {
					conn.setAutoCommit(true);
				}
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
	
	//ī���ȣ�� ���� ��ȸ�ϱ�
	public List<OrderHistoryDTO> listOrderHistoryByCardNum(int cardNum, int userNum) {
		List<OrderHistoryDTO> list = new ArrayList<>();
		List<OrderDetailDTO> items;
		Connection conn = DBCPConn.getConnection();
		//OrderHistory �����ϴ� pstmt, rs
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//OrderDetail �����ϴ� pstmt, rs
		PreparedStatement pstmtSub = null;
		ResultSet rsSub = null;
		String sql;
		try {
			sql = "SELECT orderNum, totalPaymentAmount, storeNum, oh.statusNum, statusName, oh.userNum, userName, cardNum, "
					+ "TO_CHAR(order_date,'YYYY-MM-DD HH24:MI:SS') order_date, cancelNum "
					+ " FROM order_history oh "
					+ " JOIN  order_status os ON oh.statusNum = os.statusNum "
					+ "JOIN member m ON oh.userNum = m.userNum"
					+ "WHERE cardNum = ? AND userNum = ? "
					+ "ORDER BY orderNum DESC";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, cardNum);
			pstmt.setInt(2, userNum);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				OrderHistoryDTO historyDTO = new OrderHistoryDTO();
				int orderNum = rs.getInt("orderNum");
				historyDTO.setOrderNum(orderNum);
				historyDTO.setTotalPaymentAmount(rs.getInt("totalPaymentAmount"));
				historyDTO.setStoreNum(rs.getInt("storeNum"));
				historyDTO.setStatusNum(rs.getInt("statusNum"));
				historyDTO.setStatusName(rs.getString("statusName"));
				historyDTO.setUserNum(userNum);
				historyDTO.setNickname(rs.getString("nickname"));
				historyDTO.setCardNum(cardNum);
				historyDTO.setOrderDate(rs.getString("order_date"));
				historyDTO.setCancelNum(rs.getInt("cancelNum"));
				sql = "SELECT detailNum, orderNum, od.menuNum, menuName, unitPrice, quantity, paymentAmount "
						+ " FROM order_detail od "
						+ " JOIN menu mn ON od.menuNum = mn.menuNum "
						+ " WHERE orderNum = ?"
						+ "ORDER BY detailNum, orderNum DESC";
				pstmtSub = conn.prepareStatement(sql);
				pstmtSub.setInt(1, orderNum);
				rsSub = pstmtSub.executeQuery();
				items = new ArrayList<>();
				while(rsSub.next()) {
					OrderDetailDTO detailDTO = new OrderDetailDTO();
					detailDTO.setDetailNum(rsSub.getInt("detailNum"));
					detailDTO.setOrderNum(rsSub.getInt("orderNum"));
					detailDTO.setMenuNum(rsSub.getInt("menuNum"));
					detailDTO.setMenuName(rsSub.getString("menuName"));
					detailDTO.setUnitPrice(rsSub.getInt("unitPrice"));
					detailDTO.setQuantity(rsSub.getInt("quantity"));
					detailDTO.setPaymentAmount(rsSub.getInt("paymentAmount"));
					items.add(detailDTO);
				}
				historyDTO.setItems(items);
				list.add(historyDTO);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			returnDBResources(pstmt, rs);
			returnDBResources(pstmtSub, rsSub);
			try {
				if(!conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e2) {
			}
		}
		
		return list;
	}
	
	//ȸ����ȣ�� ���� ��ȸ�ϱ�
	public List<OrderHistoryDTO> listOrderHistoryByUserNum(int userNum) {
		List<OrderHistoryDTO> list = new ArrayList<>();
		List<OrderDetailDTO> items;
		Connection conn = DBCPConn.getConnection();
		//OrderHistory �����ϴ� pstmt, rs
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//OrderDetail �����ϴ� pstmt, rs
		PreparedStatement pstmtSub = null;
		ResultSet rsSub = null;
		String sql;
		try {
			sql = "SELECT orderNum, totalPaymentAmount, storeNum, oh.statusNum, statusName, userNum, cardNum, "
					+ "TO_CHAR(order_date,'YYYY-MM-DD HH24:MI:SS') order_date, cancelNum "
					+ " FROM order_history oh "
					+ " JOIN  order_status os ON oh.statusNum = os.statusNum "
					+ " WHERE userNum = ?"
					+ " ORDER BY orderNum DESC";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, userNum);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				OrderHistoryDTO historyDTO = new OrderHistoryDTO();
				int orderNum = rs.getInt("orderNum");
				historyDTO.setOrderNum(orderNum);
				historyDTO.setTotalPaymentAmount(rs.getInt("totalPaymentAmount"));
				historyDTO.setStoreNum(rs.getInt("storeNum"));
				historyDTO.setStatusNum(rs.getInt("statusNum"));
				historyDTO.setStatusName(rs.getString("statusName"));
				historyDTO.setUserNum(userNum);
				historyDTO.setCardNum(rs.getInt("cardNum"));
				historyDTO.setOrderDate(rs.getString("order_date"));
				historyDTO.setCancelNum(rs.getInt("cancelNum"));
				sql = "SELECT detailNum, orderNum, od.menuNum, menuName, unitPrice, quantity, paymentAmount "
						+ " FROM order_detail od "
						+ " JOIN menu mn ON od.menuNum = mn.menuNum "
						+ " WHERE orderNum = ?";
				pstmtSub = conn.prepareStatement(sql);
				pstmtSub.setInt(1, orderNum);
				rsSub = pstmtSub.executeQuery();
				items = new ArrayList<>();
				while(rsSub.next()) {
					OrderDetailDTO detailDTO = new OrderDetailDTO();
					detailDTO.setDetailNum(rsSub.getInt("detailNum"));
					detailDTO.setOrderNum(rsSub.getInt("orderNum"));
					detailDTO.setMenuNum(rsSub.getInt("menuNum"));
					detailDTO.setMenuName(rsSub.getString("menuName"));
					detailDTO.setUnitPrice(rsSub.getInt("unitPrice"));
					detailDTO.setQuantity(rsSub.getInt("quantity"));
					detailDTO.setPaymentAmount(rsSub.getInt("paymentAmount"));
					items.add(detailDTO);
				}
				historyDTO.setItems(items);
				list.add(historyDTO);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			returnDBResources(pstmt, rs);
			returnDBResources(pstmtSub, rsSub);
			try {
				if(!conn.isClosed()) {
					conn.close();
				}
			} catch (Exception e2) {
			}
		}
		
		return list;
	}
	
	//////////////////////////////////////////////�ڿ��ݳ� ����ϱ�..
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
