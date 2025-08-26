package busantravel.dao;

import busantravel.db.DBConnectionMgr;
import busantravel.model.Member;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
    
    // planId의 여행 계획에 참여하고 있는 모든 멤버를 찾기
    public static List<Member> findMembersByPlanId(String planId) {
    List<Member> list = new ArrayList<>();
    String sql = """
        SELECT m.m_id, m.m_name, m.m_email, m.m_password
        FROM travel_plan_member tpm
        LEFT JOIN member m ON tpm.m_id = m.m_id
        WHERE tpm.plan_id = ?
        """;

    try (Connection con = pool.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, planId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Member(
                    rs.getString("m_id"),
                    rs.getString("m_name"),
                    rs.getString("m_password"),  
                    rs.getString("m_email")
                ));
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return list;
}

}
