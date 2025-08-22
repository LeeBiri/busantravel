package busantravel.dao;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import busantravel.db.DBConnectionMgr;
import busantravel.model.ReviewPhoto;

public class ReviewPhotoDAO {

    private DBConnectionMgr pool;

    public ReviewPhotoDAO() {
        pool = DBConnectionMgr.getInstance();
    }

    /* =========================
       INSERT (BLOB 저장)
       ========================= */

    /** 단건: 모델의 byte[] 데이터로 저장 */
    public void addReviewPhoto(ReviewPhoto photo) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO review_photo "
                   + "(ph_id, review_id, ph_name, ph_mime, ph_data, ph_description, ph_order) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);

            String phId = (photo.getPhotoId() != null && !photo.getPhotoId().isEmpty())
                    ? photo.getPhotoId()
                    : UUID.randomUUID().toString().replace("-", "").substring(0, 20);

            pstmt.setString(1, phId);
            pstmt.setString(2, photo.getReviewId());
            pstmt.setString(3, photo.getPhName());                   // 파일명
            pstmt.setString(4, photo.getPhMime());                   // MIME
            if (photo.getData() != null) {
                pstmt.setBytes(5, photo.getData());                  // BLOB 데이터
            } else {
                pstmt.setNull(5, java.sql.Types.BLOB);
            }
            pstmt.setString(6, photo.getDescription());
            pstmt.setInt(7, photo.getOrder());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    /** 여러 파일을 한 번에 저장 (ReviewPopup의 selectedPhotos 연동) */
    public void insertPhotos(String reviewId, List<File> files) {
        if (files == null || files.isEmpty()) return;

        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO review_photo "
                   + "(ph_id, review_id, ph_name, ph_mime, ph_data, ph_description, ph_order) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);

            int order = 1;
            for (File f : files) {
                String phId = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
                String fileName = f.getName();
                String mime = probeMimeSafe(f);

                try (InputStream in = new BufferedInputStream(new FileInputStream(f))) {
                    pstmt.setString(1, phId);
                    pstmt.setString(2, reviewId);
                    pstmt.setString(3, fileName);
                    pstmt.setString(4, mime);
                    pstmt.setBinaryStream(5, in, f.length());  // BLOB 본문
                    pstmt.setString(6, null);                  // 설명 사용 시 채우세요
                    pstmt.setInt(7, order++);
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    private String probeMimeSafe(File f) {
        try {
            String mime = Files.probeContentType(f.toPath());
            return mime != null ? mime : "application/octet-stream";
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

    /* =========================
       SELECT (BLOB 조회)
       ========================= */

    /** 리뷰별 사진 목록 (BLOB 포함해서 메모리로 읽음) */
    public List<ReviewPhoto> getPhotosByReviewId(String reviewId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT ph_id, review_id, ph_name, ph_mime, ph_data, ph_description, ph_order "
                   + "FROM review_photo WHERE review_id = ? "
                   + "ORDER BY ph_order ASC, ph_id ASC";
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
                photo.setPhName(rs.getString("ph_name"));
                photo.setPhMime(rs.getString("ph_mime"));
                photo.setDescription(rs.getString("ph_description"));
                photo.setOrder(rs.getInt("ph_order"));

                // BLOB → byte[]
                try (InputStream bin = rs.getBinaryStream("ph_data");
                     ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
                    if (bin != null) {
                        bin.transferTo(bout);
                        photo.setData(bout.toByteArray());
                    }
                }
                photoList.add(photo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return photoList;
    }

    /* =========================
       DELETE
       ========================= */

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
