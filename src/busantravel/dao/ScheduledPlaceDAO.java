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
        String sql = "INSERT INTO scheduled_place(plan_id, p_id, p_type, scheduled_date, visit_order, schedule_memo) VALUES(?, ?, ?, ?, ?, ?)";
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, schedule.getplan_id());
            pstmt.setString(2, schedule.getP_id());
            pstmt.setString(3, schedule.getP_type());
            pstmt.setTimestamp(4, Timestamp.valueOf(schedule.getScheduled_date()));
            pstmt.setInt(5, schedule.getVisit_order());
            pstmt.setString(6, schedule.getSchedule_memo());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    public void updateSchedule(ScheduledPlace schedule) {
        String sql = "UPDATE scheduled_place SET scheduled_date = ?, visit_order = ?, schedule_memo = ? WHERE plan_id = ? AND p_id = ?";
        try (Connection con = pool.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(schedule.getScheduled_date()));
            pstmt.setInt(2, schedule.getVisit_order());
            pstmt.setString(3, schedule.getSchedule_memo());
            pstmt.setString(4, schedule.getplan_id());
            pstmt.setString(5, schedule.getP_id());
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
                    String memo = rs.getString("Schedule_memo");
                    schedules.add(new ScheduledPlace(planId, pId, pType, scheduledDate, visitOrder, memo));
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
        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        String planId = schedules.get(0).getplan_id();
        
        String deleteSql = "DELETE FROM scheduled_place WHERE plan_id = ?";
        String insertSql = "INSERT INTO scheduled_place(plan_id, p_id, p_type, scheduled_date, visit_order, schedule_memo) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection con = pool.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement deletePstmt = con.prepareStatement(deleteSql)) {
                deletePstmt.setString(1, planId);
                deletePstmt.executeUpdate();
            }

            try (PreparedStatement insertPstmt = con.prepareStatement(insertSql)) {
                for (ScheduledPlace schedule : schedules) {
                    insertPstmt.setString(1, schedule.getplan_id());
                    insertPstmt.setString(2, schedule.getP_id());
                    insertPstmt.setString(3, schedule.getP_type());
                    insertPstmt.setTimestamp(4, Timestamp.valueOf(schedule.getScheduled_date()));
                    insertPstmt.setInt(5, schedule.getVisit_order());
                    insertPstmt.setString(6, schedule.getSchedule_memo());
                    insertPstmt.addBatch();
                }
                insertPstmt.executeBatch();
            }

            con.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
