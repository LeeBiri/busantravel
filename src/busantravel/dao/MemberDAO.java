package busantravel.dao;

import busantravel.db.DBConnectionMgr;
import busantravel.model.Member;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MemberDAO {
	private DBConnectionMgr pool = DBConnectionMgr.getInstance();
	
	public Member getMember(String id, String pw) {
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

	public Member getMemberById(String id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT M_ID, M_NAME, M_PASSWORD, M_EMAIL FROM MEMBER WHERE M_ID = ?";
		Member member = null;
		try {
			con = pool.getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				member = new Member(
					rs.getString("M_ID"),
					rs.getString("M_NAME"),
					rs.getString("M_PASSWORD"),
					rs.getString("M_EMAIL")
				);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return member;
	}
	
	public boolean register(Member member) {
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
	
	public boolean checkID(String id) {
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
	
	public boolean deleteId(String id, String pw) {
		String checkSql = "SELECT COUNT(*) FROM member WHERE m_id = ? AND m_password = ?";
		String updateChecklistSql = "UPDATE checklist SET creator_id = '탈퇴한 사용자' WHERE creator_id = ?";
		String updateInterestSql = "UPDATE interest SET m_id = '탈퇴한 사용자' WHERE m_id = ?";
		String updateReviewCommentSql = "UPDATE review_comment SET m_id = '탈퇴한 사용자' WHERE m_id = ?";
		String updateTravelPlanSql = "UPDATE travel_plan SET creator_id = '탈퇴한 사용자' WHERE creator_id = ?";
		String updateTravelPlanMemberSql = "UPDATE travel_plan_member SET m_id = '탈퇴한 사용자' WHERE m_id = ?";
		String updateTravelReviewSql = "UPDATE travel_review SET m_id = '탈퇴한 사용자' WHERE m_id = ?";
		String anonymizeMemberSql = "UPDATE member SET " +
            "m_name = CONCAT('탈퇴한 사용자_', LPAD(FLOOR(RAND()*100000), 5, '0')), " +
            "m_email = NULL, " +
            "m_password = UUID() " + 
            "WHERE m_id = ?";
		boolean flag = false;
		try (Connection con = pool.getConnection()) {
       		 // 비밀번호 일치 여부 확인
        	try (PreparedStatement checkStmt = con.prepareStatement(checkSql)) {
				checkStmt.setString(1, id);
				checkStmt.setString(2, pw);
				ResultSet rs = checkStmt.executeQuery();
				if (!rs.next() || rs.getInt(1) == 0) {
					return false; // 비밀번호 불일치 또는 없는 계정
				}
        }

        con.setAutoCommit(false); // 트랜잭션 시작

        // 각 테이블 외래키 업데이트
        try (PreparedStatement ps1 = con.prepareStatement(updateChecklistSql);
             PreparedStatement ps2 = con.prepareStatement(updateInterestSql);
             PreparedStatement ps3 = con.prepareStatement(updateReviewCommentSql);
             PreparedStatement ps4 = con.prepareStatement(updateTravelPlanSql);
             PreparedStatement ps5 = con.prepareStatement(updateTravelPlanMemberSql);
             PreparedStatement ps6 = con.prepareStatement(updateTravelReviewSql);
			 PreparedStatement ps7 = con.prepareStatement(anonymizeMemberSql)) {

            for (PreparedStatement ps : new PreparedStatement[]{ps1, ps2, ps3, ps4, ps5, ps6}) {
                ps.setString(1, id);
                ps.executeUpdate();
            }
			ps7.setString(1, id);
            int cnt = ps7.executeUpdate();

            if (cnt == 1) {
                con.commit();
                flag = true;
            } else {
                con.rollback();
            }
        } catch (Exception ex) {
            con.rollback();
            ex.printStackTrace();
        } finally {
            con.setAutoCommit(true); // 트랜잭션 종료
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

		return flag;
	}

	public boolean updateId(Member member) {
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
