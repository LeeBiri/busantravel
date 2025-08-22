package busantravel.model;

/**
 * review_photo 테이블 매핑
 *  ph_id          -> photoId
 *  review_id      -> reviewId
 *  ph_name        -> phName
 *  ph_mime        -> phMime
 *  ph_data        -> data (BLOB)
 *  ph_description -> description
 *  ph_order       -> order
 */
public class ReviewPhoto {

    private String photoId;     // ph_id
    private String reviewId;    // review_id

    private String phName;      // 파일명 (ph_name)
    private String phMime;      // MIME (ph_mime)
    private byte[] data;        // 원본 데이터 (ph_data, BLOB)

    private String description; // ph_description
    private int order;          // ph_order

    public ReviewPhoto() {}

    // --- getters / setters ---
    public String getPhotoId() { return photoId; }
    public void setPhotoId(String photoId) { this.photoId = photoId; }

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getPhName() { return phName; }
    public void setPhName(String phName) { this.phName = phName; }

    public String getPhMime() { return phMime; }
    public void setPhMime(String phMime) { this.phMime = phMime; }

    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public boolean hasData() { return data != null && data.length > 0; }

    @Override
    public String toString() {
        return "ReviewPhoto{" +
                "photoId='" + photoId + '\'' +
                ", reviewId='" + reviewId + '\'' +
                ", phName='" + phName + '\'' +
                ", phMime='" + phMime + '\'' +
                ", data=" + (data == null ? 0 : data.length) + " bytes" +
                ", description='" + description + '\'' +
                ", order=" + order +
                '}';
    }

    // --- (선택) 예전 경로 기반 코드가 남아 있을 때 컴파일만 되게 하는 더미 ---
    // private String photoPath;
    // public String getPhotoPath() { return photoPath; }
    // public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
}
