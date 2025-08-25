package busantravel.service;

import busantravel.dao.ReviewDAO;
import busantravel.model.Review;

import java.util.List;

public class ReviewService {
    private ReviewDAO reviewDAO;

    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
    }

    public List<Review> getReviewsByMember(String memberId) {
        return reviewDAO.getReviewsByMemberId(memberId);
    }

    public int getReviewCountByMember(String memberId) {
        return reviewDAO.getReviewCountByMemberId(memberId);
    }
}
