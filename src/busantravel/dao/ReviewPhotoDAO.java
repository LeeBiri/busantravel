package busantravel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import busantravel.db.DBConnectionMgr;
import busantravel.model.ReviewPhoto;

public class ReviewPhotoDAO {

    private DBConnectionMgr pool;

    public ReviewPhotoDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    public void addReviewPhoto(ReviewPhoto photo) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO review_photo (ph_id, review_id, ph_path, ph_description, ph_order) VALUES (?, ?, ?, ?, ?)";
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, photo.getPhotoId());
            pstmt.setString(2, photo.getReviewId());
            pstmt.setString(3, photo.getPhotoPath());
            pstmt.setString(4, photo.getDescription());
            pstmt.setInt(5, photo.getOrder());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    public List<ReviewPhoto> getPhotosByReviewId(String reviewId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM review_photo WHERE review_id = ? ORDER BY ph_order ASC";
        List<ReviewPhoto> photoList = new ArrayList<>();
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, reviewId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ReviewPhoto photo = new ReviewPhoto();
                photo.setPhotoId(rs.getString("ph_id"));
                photo.setReviewId(rs.getString("review_id"));
                photo.setPhotoPath(rs.getString("ph_path"));
                photo.setDescription(rs.getString("ph_description"));
                photo.setOrder(rs.getInt("ph_order"));
                photoList.add(photo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return photoList;
    }

    public void deletePhotosByReviewId(String reviewId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "DELETE FROM review_photo WHERE review_id = ?";
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, reviewId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }
}
