package busantravel.dao;

import busantravel.db.DBConnectionMgr;
import busantravel.model.Member;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MemberDAO {
	static DBConnectionMgr pool= DBConnectionMgr.getInstance();
	
	// id와 pw를 확인하고 해당하는 Member 객체를 반환
	public static Member getMember(String id, String pw) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT M_ID, M_NAME, M_PASSWORD, M_EMAIL FROM MEMBER WHERE M_ID = ? AND M_PASSWORD = ?";
		Member member = null;
		try {
			con = pool.getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, pw);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				member = new Member(
					rs.getString("M_ID"),
					rs.getString("M_NAME"),
					rs.getString("M_PASSWORD"),
					rs.getString("M_EMAIL")
				);
			}  else {
				System.out.println("로그인 실패: 아이디 또는 비밀번호 불일치");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return member;
	}

	
	//회원가입
	public static boolean register(Member member) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "INSERT INTO MEMBER(M_ID, M_NAME, M_PASSWORD, M_EMAIL) VALUES(?, ?, ?, ?)";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, member.getM_id());
			pstmt.setString(2, member.getM_name());
			pstmt.setString(3, member.getM_password());
			pstmt.setString(4, member.getM_email());
			int cnt = pstmt.executeUpdate();
			if (cnt == 1) { flag = true; }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
		return flag;
	}
	
	//중복체크
	public static boolean checkID(String id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		boolean duplicated = false;
		try {
			con = pool.getConnection();
			sql = "SELECT COUNT(*) FROM MEMBER WHERE M_ID = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();
			if (rs.next() && rs.getInt(1) > 0) {
				duplicated = true;
			} else {
				System.out.println("중복된 아이디");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return duplicated;
	}
	
	public static boolean deleteId(String id, String pw) {
		String sql = "DELETE FROM member WHERE m_id = ? and m_password = ?";
		boolean flag = false;
        try {
            Connection con = pool.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
			pstmt.setString(2, pw);
			int cnt = pstmt.executeUpdate();
			if (cnt == 1) { flag = true; }
        } catch (Exception e) {
            e.printStackTrace();
        }
		return flag;
	}

	public static boolean updateId(Member member) {
		String sql = "UPDATE member SET m_name = ?, m_password = ?, m_email = ? WHERE m_id = ?";
		boolean flag = false;
        try {
            Connection con = pool.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getM_name());
			pstmt.setString(2, member.getM_password());
			pstmt.setString(3, member.getM_email());
			pstmt.setString(4, member.getM_id());
			int cnt = pstmt.executeUpdate();
			if (cnt == 1) { flag = true; }
			pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
		return flag;
	}
}
