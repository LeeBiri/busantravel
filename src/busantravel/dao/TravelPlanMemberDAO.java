package busantravel.dao;

import busantravel.db.DBConnectionMgr;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class TravelPlanMemberDAO {
	
	static DBConnectionMgr pool= DBConnectionMgr.getInstance();
	
	/*여행 계획 멤버 추가 메소드*/
    public static void insert(String planId, String memberId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO travel_plan_member (plan_id, m_id) VALUES (?, ?)";
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, planId);
            pstmt.setString(2, memberId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	pool.freeConnection(con, pstmt);
        }
    }

    public static void deleteByPlanId(String planId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "DELETE FROM travel_plan_member WHERE plan_id = ?";
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, planId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }
}
