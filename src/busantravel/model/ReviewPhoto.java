package busantravel.model;

public class ReviewPhoto {
    private String photoId;
    private String reviewId;
    private String photoPath;
    private String description;
    private int order;

    public ReviewPhoto() {
    }

    public ReviewPhoto(String photoId, String reviewId, String photoPath, String description, int order) {
        this.photoId = photoId;
        this.reviewId = reviewId;
        this.photoPath = photoPath;
        this.description = description;
        this.order = order;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
