package busantravel.dao;

import busantravel.db.DBConnectionMgr;
import busantravel.model.ScheduledPlace;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduledPlaceDAO {
    private final DBConnectionMgr pool;

    public ScheduledPlaceDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    public void insertSchedule(ScheduledPlace schedule) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO scheduled_place(plan_id, p_id, p_type, scheduled_date, visit_order) VALUES(?, ?, ?, ?, ?)";
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, schedule.getplan_id());
            pstmt.setString(2, schedule.getP_id());
            pstmt.setString(3, schedule.getP_type());
            pstmt.setTimestamp(4, Timestamp.valueOf(schedule.getScheduled_date()));
            pstmt.setInt(5, schedule.getVisit_order());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    public void updateSchedule(ScheduledPlace schedule) {
        String sql = "UPDATE scheduled_place SET scheduled_date = ?, visit_order = ? WHERE plan_id = ? AND p_id = ?";
        try (Connection con = pool.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(schedule.getScheduled_date()));
            pstmt.setInt(2, schedule.getVisit_order());
            pstmt.setString(3, schedule.getplan_id());
            pstmt.setString(4, schedule.getP_id());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteSchedule(ScheduledPlace schedule) {
        String sql = "DELETE FROM scheduled_place WHERE plan_id = ? AND p_id = ?";
        try (Connection con = pool.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, schedule.getplan_id());
            pstmt.setString(2, schedule.getP_id());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ScheduledPlace> findSchedulesByPlanId(String planId) {
        List<ScheduledPlace> schedules = new ArrayList<>();
        String sql = "SELECT * FROM scheduled_place WHERE plan_id = ? ORDER BY visit_order";
        try (Connection con = pool.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, planId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String pId = rs.getString("p_id");
                    String pType = rs.getString("p_type");
                    LocalDateTime scheduledDate = rs.getTimestamp("scheduled_date").toLocalDateTime();
                    int visitOrder = rs.getInt("visit_order");
                    schedules.add(new ScheduledPlace(planId, pId, pType, scheduledDate, visitOrder));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schedules;
    }

    public void deleteAllByPlanId(String planId) {
        String sql = "DELETE FROM scheduled_place WHERE plan_id = ?";
        try (Connection con = pool.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, planId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateScheduleOrder(List<ScheduledPlace> schedules) {
        String sql = "UPDATE scheduled_place SET visit_order = ? WHERE plan_id = ? AND p_id = ?";
        try (Connection con = pool.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            for (ScheduledPlace schedule : schedules) {
                pstmt.setInt(1, schedule.getVisit_order());
                pstmt.setString(2, schedule.getplan_id());
                pstmt.setString(3, schedule.getP_id());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
