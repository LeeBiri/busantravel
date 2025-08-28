package busantravel.dao;

import busantravel.db.DBConnectionMgr;
import busantravel.model.ReviewPhoto;

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

public class ReviewPhotoDAO {

    private final DBConnectionMgr pool = DBConnectionMgr.getInstance();

    // 스키마 컬럼 길이에 맞춰 조정 (필요시 값 변경)
    private static final int NAME_MAX = 100;   // ph_name VARCHAR(100)
    private static final int MIME_MAX = 200;   // ph_mime VARCHAR(200)

    public ReviewPhotoDAO() {}

    /* =========================================================
       단건 저장 (모델이 byte[] 데이터를 가지고 있을 때)
       ========================================================= */

       // 첫 장의 BLOB만 빠르게 가져오기 (없으면 null)
public byte[] getFirstPhotoData(String reviewId) {
    Connection con = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    String sql = "SELECT ph_data FROM review_photo " +
                 "WHERE review_id = ? " +
                 "ORDER BY ph_order ASC, ph_id ASC " +
                 "LIMIT 1";
    try {
        con = pool.getConnection();
        pstmt = con.prepareStatement(sql);
        pstmt.setString(1, reviewId);
        rs = pstmt.executeQuery();
        if (rs.next()) {
            try (InputStream bin = rs.getBinaryStream(1);
                 ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
                if (bin != null) {
                    bin.transferTo(bout);
                    return bout.toByteArray();
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        pool.freeConnection(con, pstmt, rs);
    }
    return null;
}

    /** 여러 파일을 한 번에 저장 (ReviewPopup의 selectedPhotos 연동) */
    public void insertPhotos(String reviewId, List<File> files) {
        if (files == null || files.isEmpty()) return;

        String sql = "INSERT INTO review_photo "
                   + "(ph_id, review_id, ph_name, ph_mime, ph_data, ph_description, ph_order) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        int order = 1;
        for (File f : files) {
            Connection con = null;
            PreparedStatement pstmt = null;
            try {
                con = pool.getConnection();
                pstmt = con.prepareStatement(sql);

                String phId = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
                String fileName = f.getName();
                String mime = probeMimeSafe(f);

                try (InputStream in = new BufferedInputStream(new FileInputStream(f))) {
                    pstmt.setString(1, phId);
                    pstmt.setString(2, reviewId);
                    pstmt.setString(3, fileName);
                    pstmt.setString(4, mime);
                    pstmt.setBinaryStream(5, in, f.length());
                    pstmt.setString(6, null);
                    pstmt.setInt(7, order++);
                    pstmt.executeUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                pool.freeConnection(con, pstmt);
            }
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
