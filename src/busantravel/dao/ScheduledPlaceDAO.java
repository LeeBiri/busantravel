package busantravel.dao;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import busantravel.db.DBConnectionMgr;
import busantravel.model.ScheduledPlace;

public class ScheduledPlaceDAO {
	static DBConnectionMgr pool= DBConnectionMgr.getInstance();
	
	public static void insertSchedule(ScheduledPlace schedule) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO scheduled_place(plan_id, p_id, p_type, scheduled_date, expected_duration, visit_order) VALUES(?, ?, ?, ?, ?, ?)";
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, schedule.getplan_id());
            pstmt.setString(2, schedule.getP_id());
            pstmt.setString(3, schedule.getP_type());
            pstmt.setDate(4, java.sql.Date.valueOf(schedule.getScheduled_date().toLocalDate()));
            pstmt.setTimestamp(5, Timestamp.valueOf(schedule.getExpected_dutarion()));
            pstmt.setInt(6, schedule.getVisit_order());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	pool.freeConnection(con, pstmt);
        }
	}
	
	public static void updateSchedule(ScheduledPlace newData, ScheduledPlace oldData) {
        Connection con = null;
        PreparedStatement pstmt = null;
	    String sql = "UPDATE scheduled_place " +
	                 "SET plan_id = ?, p_id = ?, p_type = ?, " +
	                 "    scheduled_date = ?, expected_duration = ?, visit_order = ? " +
	                 "WHERE plan_id = ? AND p_id = ? AND p_type = ?";

	    try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
	        // update할 새로운값
	        pstmt.setString(1, newData.getplan_id());
	        pstmt.setString(2, newData.getP_id());
	        pstmt.setString(3, newData.getP_type());
	        pstmt.setDate(4, java.sql.Date.valueOf(newData.getScheduled_date().toLocalDate()));
	        pstmt.setTimestamp(5, Timestamp.valueOf(newData.getExpected_dutarion()));
	        pstmt.setInt(6, newData.getVisit_order());

	        // 기존 값
	        pstmt.setString(7, oldData.getplan_id());
	        pstmt.setString(8, oldData.getP_id());
	        pstmt.setString(9, oldData.getP_type());

	        pstmt.executeUpdate();

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
        	pool.freeConnection(con, pstmt);
        }
	}
	
	public static List<ScheduledPlace> searchByPlanId(String planId) {
        Connection con = null;
        PreparedStatement pstmt = null;
		ResultSet rs = null;
        String sql = "select * from scheduled_place where plan_id = ?";
        List<ScheduledPlace> scheduleList = new ArrayList<ScheduledPlace>();
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, planId);
			rs = pstmt.executeQuery();
	        while (rs.next()) {
	        	scheduleList.add(new ScheduledPlace(
	        			planId, 
	        			rs.getString("p_id"), 
	        			rs.getString("p_type"), 
	        			//나중에 방문 예정 시간이나 그런거 업뎃하면 넣을 거
	        			rs.getTimestamp("scheduled_date").toLocalDateTime(), 
	        			null,
	        			rs.getInt("visit_order")));
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
        	pool.freeConnection(con, pstmt);
        }
        return scheduleList;
	}
	
	public static void deleteSchedule(ScheduledPlace schedule) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "delete from scheduled_place where plan_id = ? and p_id = ? and p_type = ? and scheduled_date = ?";
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, schedule.getplan_id());
            pstmt.setString(2, schedule.getP_id());
            pstmt.setString(3, schedule.getP_type());
            pstmt.setDate(4, java.sql.Date.valueOf(schedule.getScheduled_date().toLocalDate()));
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	pool.freeConnection(con, pstmt);
        }
	}
}
