package busantravel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import busantravel.db.DBConnectionMgr;
import busantravel.model.Review;

public class ReviewDAO {

    private DBConnectionMgr pool;

    public ReviewDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    public void addReview(Review review) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO travel_review (review_id, m_id, title, content, review_visibility, created_at, plan_id) VALUES (?, ?, ?, ?, ?, NOW(), ?)";
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, review.getReviewId());
            pstmt.setString(2, review.getMemberId());
            pstmt.setString(3, review.getTitle());
            pstmt.setString(4, review.getContent());
            pstmt.setInt(5, review.getVisibility());
            pstmt.setString(6, review.getPlanId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    public void updateReview(Review review) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE travel_review SET title = ?, content = ?, review_visibility = ?, plan_id = ? WHERE review_id = ?";
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, review.getTitle());
            pstmt.setString(2, review.getContent());
            pstmt.setInt(3, review.getVisibility());
            pstmt.setString(4, review.getPlanId());
            pstmt.setString(5, review.getReviewId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    public void deleteReview(String reviewId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        PreparedStatement photoPstmt = null;
        String deletePhotosSQL = "DELETE FROM review_photo WHERE review_id = ?";
        String deleteReviewSQL = "DELETE FROM travel_review WHERE review_id = ?";

        try {
            con = pool.getConnection();
            con.setAutoCommit(false); // 트랜잭션 시작

            // 1. 리뷰에 연결된 사진 삭제
            photoPstmt = con.prepareStatement(deletePhotosSQL);
            photoPstmt.setString(1, reviewId);
            photoPstmt.executeUpdate();

            // 2. 리뷰 삭제
            pstmt = con.prepareStatement(deleteReviewSQL);
            pstmt.setString(1, reviewId);
            pstmt.executeUpdate();

            con.commit(); // 트랜잭션 커밋
        } catch (Exception e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback(); // 오류 발생 시 롤백
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
        } finally {
            if (photoPstmt != null) {
                try {
                    photoPstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            pool.freeConnection(con, pstmt);
        }
    }

    public void deleteReviewsByPlanId(String planId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "DELETE FROM travel_review WHERE plan_id = ?";
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

    public List<Review> getReviewsByPlanId(String planId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM travel_review WHERE plan_id = ?";
        List<Review> reviewList = new ArrayList<>();
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, planId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Review review = new Review();
                review.setReviewId(rs.getString("review_id"));
                review.setMemberId(rs.getString("m_id"));
                review.setTitle(rs.getString("title"));
                review.setContent(rs.getString("content"));
                review.setVisibility(rs.getInt("review_visibility"));
                review.setCreatedAt(rs.getTimestamp("created_at"));
                review.setPlanId(rs.getString("plan_id"));
                reviewList.add(review);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return reviewList;
    }

    public Review getReview(String reviewId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM travel_review WHERE review_id = ?";
        Review review = null;
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, reviewId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                review = new Review();
                review.setReviewId(rs.getString("review_id"));
                review.setMemberId(rs.getString("m_id"));
                review.setTitle(rs.getString("title"));
                review.setContent(rs.getString("content"));
                review.setVisibility(rs.getInt("review_visibility"));
                review.setCreatedAt(rs.getTimestamp("created_at"));
                review.setPlanId(rs.getString("plan_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return review;
    }

    public int getReviewCountByMemberId(String memberId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT COUNT(*) FROM travel_review WHERE m_id = ?";
        int count = 0;
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return count;
    }

    public List<Review> getReviewsByMemberId(String memberId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM travel_review WHERE m_id = ? ORDER BY created_at DESC";
        List<Review> reviewList = new ArrayList<>();
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Review review = new Review();
                review.setReviewId(rs.getString("review_id"));
                review.setMemberId(rs.getString("m_id"));
                review.setTitle(rs.getString("title"));
                review.setContent(rs.getString("content"));
                review.setVisibility(rs.getInt("review_visibility"));
                review.setCreatedAt(rs.getTimestamp("created_at"));
                review.setPlanId(rs.getString("plan_id"));
                reviewList.add(review);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return reviewList;
    }

    public List<Review> getAllPublicReviews() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM travel_review WHERE review_visibility = 0 ORDER BY created_at DESC";
        List<Review> reviewList = new ArrayList<>();
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Review review = new Review();
                review.setReviewId(rs.getString("review_id"));
                review.setMemberId(rs.getString("m_id"));
                review.setTitle(rs.getString("title"));
                review.setContent(rs.getString("content"));
                review.setVisibility(rs.getInt("review_visibility"));
                review.setCreatedAt(rs.getTimestamp("created_at"));
                review.setPlanId(rs.getString("plan_id"));
                reviewList.add(review);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return reviewList;
    }
}
