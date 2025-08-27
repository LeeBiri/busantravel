package busantravel.model;

import java.util.Date;
import java.util.List;

public class Review {
    private String reviewId;
    private String memberId;
    private String title;
    private String content;
    private int visibility; // 0: public, 1: private
    private Date createdAt;
    private String planId;
    private List<String> photoUris;
    private String memberNickname;

    public Review() {
    }

    public Review(String reviewId, String memberId, String title, String content, int visibility, Date createdAt) {
        this.reviewId = reviewId;
        this.memberId = memberId;
        this.title = title;
        this.content = content;
        this.visibility = visibility;
        this.createdAt = createdAt;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }


    public List<String> getPhotoUris() {
        return photoUris;
    }

    public void setPhotoUris(List<String> photoUris) {
        this.photoUris = photoUris;
    }

    public String getMemberNickname() {
        return memberNickname;
    }

    public void setMemberNickname(String memberNickname) {
        this.memberNickname = memberNickname;
    }
}
