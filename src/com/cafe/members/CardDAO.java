package com.cafe.members;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.util.DBCPConn;

public class CardDAO {

	// ī�� �Ϸù�ȣ ��Ģ
	// System.currentTimeMillis() => 13�ڸ� �� 10�ڸ�, 3�ڸ��� ���� => �� 16�ڸ�

	// ī�� �����
	public int insertCard(CardDTO dto) {
		int result = 0;
		Connection conn = DBCPConn.getConnection();
		PreparedStatement pstmt = null;
		String sql = "INSERT INTO cards(cardNum, cardName, userNum, modelNum, cardIdentity, balance) "
				+ "VALUES (cards_seq.NEXTVAL, ?, ?, ?, ?, ?)";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, dto.getCardName());
			pstmt.setInt(2, dto.getUserNum());
			pstmt.setInt(3, dto.getModelNum());
			pstmt.setString(4, new CardIdentityMaker().getCardIdentity());
			pstmt.setInt(5, dto.getBalance());// TODO: ������ ���� �ܾ��� �Է������� ���߿��� Ʈ���ŷ� ������־�� ��.
			result = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e2) {
				}
			}
			try {
				if (!conn.isClosed()) {
					DBCPConn.close(conn);
				}
			} catch (Exception e2) {
			}
		}

		return result;
	}

	// ī�� ��� �ҷ�����
	public List<CardDTO> listCard(int userNum) {
		List<CardDTO> list = new ArrayList<>();
		Connection conn = DBCPConn.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT cardNum, cardName, userNum, cards.modelNum, cardIdentity, balance, thumbnail "
				+ "FROM cards "
				+ "JOIN card_model ON cards.modelNum = card_model.modelNum "
				+ "WHERE userNum = ? ";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, userNum);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int cardNum = rs.getInt("cardNum");
				String cardName = rs.getString("cardName");
				int modelNum = rs.getInt("modelNum");
				String cardIdentity = rs.getString("cardIdentity");
				int balance = rs.getInt("balance");
				String thumbnail = rs.getString("thumbnail");
				list.add(new CardDTO(cardNum, cardName, userNum, modelNum, cardIdentity, balance, thumbnail));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e2) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e2) {
				}
			}
			try {
				if (!conn.isClosed()) {
					DBCPConn.close(conn);
				}
			} catch (Exception e2) {
			}
		}

		return list;
	}

}
