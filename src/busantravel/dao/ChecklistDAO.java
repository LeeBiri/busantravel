package busantravel.dao;

import busantravel.db.DBConnectionMgr;
import busantravel.model.Checklist;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ChecklistDAO {
    static DBConnectionMgr pool= DBConnectionMgr.getInstance();

    public static void insertChecklist(String plan_id, String creator_id, String checklist_content) {
        Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "INSERT INTO checklist(plan_id, creator_id, checklist_content, is_completed, is_public) VALUES(?, ?, ?, ?, ?)";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, plan_id);
			pstmt.setString(2, creator_id);
			pstmt.setString(3, checklist_content);
            pstmt.setBoolean(4, false);
            pstmt.setBoolean(5, false);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
    }

    public static void insertPublicChecklist(String plan_id, String checklist_content) {
        Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "INSERT INTO checklist(plan_id, creator_id, checklist_content, is_completed, is_public) VALUES(?, ?, ?, ?, ?)";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, plan_id);
			pstmt.setString(2, "공통");
			pstmt.setString(3, checklist_content);
            pstmt.setBoolean(4, false);
            pstmt.setBoolean(5, true);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
    }

    public static List<Checklist> getChecklistByid(String plan_id, String creator_id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT checklist_id, plan_id, creator_id, checklist_content, is_completed, is_public FROM checklist WHERE plan_id = ? AND creator_id = ?";
		List<Checklist> checklist = new ArrayList<>();
		try {
			con = pool.getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, plan_id);
			pstmt.setString(2, creator_id);
			rs = pstmt.executeQuery();
			while (rs.next()){
					checklist.add(new Checklist(
                    rs.getInt("checklist_id"), 
                    rs.getString("plan_id"),
                    rs.getString("creator_id"),
                    rs.getString("checklist_content"), 
                    rs.getBoolean("is_completed"), 
                    rs.getBoolean("is_public"))
                );
            }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return checklist;
	}

    public static List<Checklist> getPublicChecklist(String plan_id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT checklist_id, plan_id, creator_id, checklist_content, is_completed, is_public FROM checklist WHERE plan_id = ? AND is_public = ?";
		List<Checklist> checklist = new ArrayList<>();
		try {
			con = pool.getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, plan_id);
			pstmt.setBoolean(2, true);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				checklist.add(new Checklist(
                    rs.getInt("checklist_id"), 
                    rs.getString("plan_id"),	 
                    rs.getString("creator_id"), 
                    rs.getString("checklist_content"),
                    rs.getBoolean("is_completed"), 
                    rs.getBoolean("is_public")
				));
            }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return checklist;
    }

    public static void completeChecklist(int checklist_id, boolean flag) {
        String sql = "UPDATE checklist SET is_completed = ? WHERE checklist_id = ?";
        try (
            Connection con = pool.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setBoolean(1, flag);
			pstmt.setInt(2, checklist_id);
			pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateChecklist(int checklist_id, String checklist_content) {
        String sql = "UPDATE CHECKLIST SET CHECKLIST_CONTENT = ? WHERE CHECKLIST_ID = ?";
        try {
            Connection con = pool.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, checklist_content);
			pstmt.setInt(2, checklist_id);
			pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteChecklist(int checklist_id) {
        String sql = "DELETE FROM CHECKLIST WHERE CHECKLIST_ID = ?";
        try {
            Connection con = pool.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, checklist_id);
			pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
