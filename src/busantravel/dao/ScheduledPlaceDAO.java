package busantravel.dao;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;

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
            //pstmt.setDate(4, );
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
	    String sql = "UPDATE scheduled_place " +
	                 "SET plan_id = ?, p_id = ?, p_type = ?, " +
	                 "    scheduled_date = ?, expected_duration = ?, visit_order = ? " +
	                 "WHERE plan_id = ? AND p_id = ? AND p_type = ?";

	    try (Connection conn = DBConnectionMgr.getInstance().getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        // 새로운 값
	        pstmt.setString(1, newData.getplan_id());
	        pstmt.setString(2, newData.getP_id());
	        pstmt.setString(3, newData.getP_type());
	        //pstmt.setDate(4, Date.valueOf(newData.getScheduled_date()));
	        pstmt.setTimestamp(5, Timestamp.valueOf(newData.getExpected_dutarion()));
	        pstmt.setInt(6, newData.getVisit_order());

	        // 기존 값
	        pstmt.setString(7, oldData.getplan_id());
	        pstmt.setString(8, oldData.getP_id());
	        pstmt.setString(9, oldData.getP_type());

	        pstmt.executeUpdate();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	}
