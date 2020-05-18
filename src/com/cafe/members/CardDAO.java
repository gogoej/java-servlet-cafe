package com.cafe.members;

import java.sql.Connection;
import java.sql.PreparedStatement;

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
			pstmt.setInt(2,  dto.getUserNum());
			pstmt.setInt(3, dto.getModelNum());
			pstmt.setString(4, new CardIdentityMaker().getCardIdentity());
			pstmt.setInt(5, dto.getBalance());//TODO: ������ ���� �ܾ��� �Է������� ���߿��� Ʈ���ŷ� ������־�� ��.
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
	

}
