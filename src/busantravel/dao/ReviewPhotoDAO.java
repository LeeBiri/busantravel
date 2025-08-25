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
        return null;
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    } finally {
        pool.freeConnection(con, pstmt, rs);
    }
}

    public void addReviewPhoto(ReviewPhoto photo) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO review_photo " +
                "(ph_id, review_id, ph_name, ph_mime, ph_data, ph_description, ph_order) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            con = pool.getConnection();
            try { con.setAutoCommit(true); } catch (Exception ignore) {}

            pstmt = con.prepareStatement(sql);

            String phId = (photo.getPhotoId() != null && !photo.getPhotoId().isEmpty())
                    ? photo.getPhotoId()
                    : UUID.randomUUID().toString().replace("-", "").substring(0, 20);

            String name = safeTruncate(photo.getPhName(), NAME_MAX);
            String mime = safeTruncate(photo.getPhMime(), MIME_MAX);

            pstmt.setString(1, phId);
            pstmt.setString(2, photo.getReviewId());
            pstmt.setString(3, name);
            pstmt.setString(4, mime);
            if (photo.getData() != null) {
                pstmt.setBytes(5, photo.getData());
            } else {
                pstmt.setNull(5, java.sql.Types.BLOB);
            }
            pstmt.setString(6, photo.getDescription());
            pstmt.setInt(7, photo.getOrder());

            int r = pstmt.executeUpdate();
            System.out.println("[addReviewPhoto] ph_id=" + phId + ", result=" + r);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    /* =========================================================
       여러 파일 저장 (파일 원본을 ph_data(BLOB)로 저장)  → 저장 건수 반환
       ========================================================= */
    public int insertPhotos(String reviewId, List<File> files) {
        if (files == null || files.isEmpty()) return 0;
        if (reviewId == null || reviewId.isEmpty()) {
            System.err.println("[insertPhotos] reviewId가 비어있습니다.");
            return 0;
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO review_photo " +
                "(ph_id, review_id, ph_name, ph_mime, ph_data, ph_description, ph_order) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        int inserted = 0;

        try {
            con = pool.getConnection();
            try { con.setAutoCommit(true); } catch (Exception ignore) {}
            pstmt = con.prepareStatement(sql);

            int order = 1;
            for (File f : files) {
                if (f == null || !f.exists()) {
                    System.err.println("[insertPhotos] 파일 없음: " + f);
                    continue;
                }

                String phId = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
                String fileName = safeTruncate(f.getName(), NAME_MAX);
                String mime = safeTruncate(probeMimeSafe(f), MIME_MAX);

                try (InputStream in = new BufferedInputStream(new FileInputStream(f))) {
                    pstmt.setString(1, phId);
                    pstmt.setString(2, reviewId);
                    pstmt.setString(3, fileName);
                    pstmt.setString(4, mime);
                    // (InputStream, long) 시그니처 — 큰 파일도 안전
                    pstmt.setBinaryStream(5, in, f.length());
                    pstmt.setString(6, null);   // 설명 사용 시 값 설정
                    pstmt.setInt(7, order++);

                    int r = pstmt.executeUpdate();   // 파일별 즉시 실행
                    inserted += r;

                    System.out.println("[insertPhotos] ok ph_id=" + phId +
                            ", reviewId=" + reviewId +
                            ", file=" + fileName +
                            ", bytes=" + f.length() +
                            ", result=" + r);
                } catch (Exception ex) {
                    System.err.println("[insertPhotos] 실패 file=" + fileName + " : " + ex.getMessage());
                    ex.printStackTrace(); // 한 파일 실패해도 나머지는 계속
                }
            }

            System.out.println("[insertPhotos] reviewId=" + reviewId + " → saved=" + inserted);
            return inserted;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    private String probeMimeSafe(File f) {
        try {
            String mime = Files.probeContentType(f.toPath());
            return (mime != null) ? mime : "application/octet-stream";
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

    private String safeTruncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }

    /* =========================================================
       리뷰의 사진들 조회 (BLOB 포함)  → 상세 팝업에서 표시
       ========================================================= */
    public List<ReviewPhoto> getPhotosByReviewId(String reviewId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT ph_id, review_id, ph_name, ph_mime, ph_data, ph_description, ph_order " +
                     "FROM review_photo WHERE review_id = ? " +
                     "ORDER BY ph_order ASC, ph_id ASC";

        List<ReviewPhoto> photoList = new ArrayList<>();
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, reviewId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ReviewPhoto p = new ReviewPhoto();
                p.setPhotoId(rs.getString("ph_id"));
                p.setReviewId(rs.getString("review_id"));
                p.setPhName(rs.getString("ph_name"));
                p.setPhMime(rs.getString("ph_mime"));
                p.setDescription(rs.getString("ph_description"));
                p.setOrder(rs.getInt("ph_order"));

                try (InputStream bin = rs.getBinaryStream("ph_data");
                     ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
                    if (bin != null) {
                        bin.transferTo(bout);
                        p.setData(bout.toByteArray());
                    }
                }
                photoList.add(p);
            }
            System.out.println("[getPhotosByReviewId] reviewId=" + reviewId + ", rows=" + photoList.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return photoList;
    }

    /* =========================================================
       리뷰의 사진 모두 삭제 (수정 시 갈아끼울 때 사용)
       ========================================================= */
    public void deletePhotosByReviewId(String reviewId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = "DELETE FROM review_photo WHERE review_id = ?";
        try {
            con = pool.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, reviewId);
            int r = pstmt.executeUpdate();
            System.out.println("[deletePhotosByReviewId] reviewId=" + reviewId + ", deleted=" + r);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
    }

    /* =========================================================
       (디버그용) DB에 실제로 들어갔는지 개수 확인
       ========================================================= */
    public int countPhotos(String reviewId) {
        String q = "SELECT COUNT(*) FROM review_photo WHERE review_id = ?";
        try (Connection c = pool.getConnection();
             PreparedStatement p = c.prepareStatement(q)) {
            p.setString(1, reviewId);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
