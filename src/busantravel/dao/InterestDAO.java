package busantravel.dao;

import busantravel.db.DBConnectionMgr;
import busantravel.model.Interest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InterestDAO {
    static DBConnectionMgr pool= DBConnectionMgr.getInstance();

    public static void insertInterest(String m_id, String p_id, String p_type) {
        Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			con = pool.getConnection();
			sql = "INSERT INTO interest(m_id, p_id, p_type) VALUES(?, ?, ?)";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, m_id);
			pstmt.setString(2, p_id);
            pstmt.setString(3, p_type);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
    }

    public static List<Interest> getInterestById(String m_id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
        String sql = "SELECT M_ID, P_ID, P_TYPE FROM INTEREST WHERE M_ID = ?";
        List<Interest> interests = new ArrayList<>();
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, m_id);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                interests.add(new Interest(
                    rs.getString("m_id"), 
                    rs.getString("p_id"), 
                    rs.getString("p_type")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return interests;
    }

    public static void deleteInterest(Interest interest) {
        String sql = "DELETE FROM INTEREST WHERE M_ID = ? AND P_ID = ? AND P_TYPE = ?";
        try (Connection con = pool.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, interest.getM_id());
            pstmt.setString(2, interest.getP_id());
            pstmt.setString(3, interest.getP_type());
			pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int countInterestsByPlace(String p_id, String p_type) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM interest WHERE p_id = ? AND p_type = ?";
        try (Connection con = pool.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, p_id);
            pstmt.setString(2, p_type);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
}
