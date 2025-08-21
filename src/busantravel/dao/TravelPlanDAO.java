package busantravel.dao;

import busantravel.db.DBConnectionMgr;
import busantravel.model.TravelPlan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TravelPlanDAO {
	static DBConnectionMgr pool= DBConnectionMgr.getInstance();

	/*여행 계획 추가*/
    public static String insert(TravelPlan plan) {
        Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			con = pool.getConnection();
			sql = "INSERT INTO travel_plan(plan_id, creator_id, plan_title, plan_period_start, plan_period_end) VALUES(?, ?, ?, ?, ?)";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, plan.getPlan_id());
			pstmt.setString(2, plan.getCreator_id());
			pstmt.setString(3, plan.getPlan_title());
			pstmt.setTimestamp(4, Timestamp.valueOf(plan.getPlan_period_start()));
			pstmt.setTimestamp(5, Timestamp.valueOf(plan.getPlan_period_end()));
            pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
        return plan.getPlan_id();
    }

    /*계획 ID로 계획 찾기*/
    public static TravelPlan findById(String planId) {
        Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM TRAVEL_PLAN WHERE PLAN_ID = ?";
        TravelPlan travelPlan = null;
		try {
			con = pool.getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, planId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				travelPlan = new TravelPlan(
                    rs.getString("plan_id"),
                    rs.getString("creator_id"),
                    rs.getString("plan_title"),
                    rs.getTimestamp("plan_period_start").toLocalDateTime(),
                    rs.getTimestamp("plan_period_end").toLocalDateTime()
                );
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		 return travelPlan;
    }

    /* 멤버가 참여중인 계획 전부 찾기 */
    public static List<TravelPlan> findAllByMember(String memberId) {
        Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
        String sql = "SELECT p.* FROM TRAVEL_PLAN p JOIN TRAVEL_PLAN_MEMBER m ON p.plan_id = m.plan_id " +
                    "WHERE m.m_id = ? ORDER BY p.plan_period_start ASC";
        List<TravelPlan> out = new ArrayList<>();
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Timestamp startTs = rs.getTimestamp("plan_period_start");
                Timestamp endTs = rs.getTimestamp("plan_period_end");
                out.add(new TravelPlan(
                    rs.getString("plan_id"), 
                    rs.getString("creator_id"),
                    rs.getString("plan_title"), 
                    startTs.toLocalDateTime(), 
                    endTs.toLocalDateTime()));
            }
        } catch (Exception e) {
            e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
        return out;
    }

    /* 멤버가 참여중인 계획 중 진행중인 or 앞으로의 계획 */
    public static List<TravelPlan> findUpcomingByMember(String memberId, LocalDateTime todayEnd) {
        Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
        String sql = "SELECT p.* FROM TRAVEL_PLAN p JOIN TRAVEL_PLAN_MEMBER m ON p.plan_id = m.plan_id " +
                    "WHERE m.m_id = ? AND p.plan_period_end >= ? ORDER BY p.plan_period_start ASC";
        List<TravelPlan> out = new ArrayList<>();
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.setTimestamp(2, Timestamp.valueOf(todayEnd));
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Timestamp startTs = rs.getTimestamp("plan_period_start");
                Timestamp endTs = rs.getTimestamp("plan_period_end");
                TravelPlan plan = new TravelPlan(
                    rs.getString("plan_id"), 
                    rs.getString("creator_id"),
                    rs.getString("plan_title"), 
                    startTs.toLocalDateTime(), 
                    endTs.toLocalDateTime());
                out.add(plan);
            }
        } catch (Exception e) {
            e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
        return out;
    }

    /* 끝난 계획 찾기*/
    public static List<TravelPlan> findPastByMember(String memberId, LocalDateTime todayEnd) {
        Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
        String sql = "SELECT p.* FROM TRAVEL_PLAN p JOIN TRAVEL_PLAN_MEMBER m ON p.plan_id = m.plan_id " +
                    "WHERE m.m_id = ? AND p.plan_period_end < ? ORDER BY p.plan_period_start ASC";
        List<TravelPlan> out = new ArrayList<>();
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.setTimestamp(2, Timestamp.valueOf(todayEnd));
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Timestamp startTs = rs.getTimestamp("plan_period_start");
                Timestamp endTs = rs.getTimestamp("plan_period_end");
                TravelPlan plan = new TravelPlan(
                    rs.getString("plan_id"), 
                    rs.getString("creator_id"),
                    rs.getString("plan_title"), 
                    startTs.toLocalDateTime(), 
                    endTs.toLocalDateTime());
                out.add(plan);
            }
        } catch (Exception e) {
            e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
        return out;
    }

    public static void deleteById(String planId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "DELETE FROM travel_plan WHERE plan_id = ?";
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
