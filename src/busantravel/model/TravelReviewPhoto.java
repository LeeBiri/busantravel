package busantravel.model;

public class TravelReviewPhoto {
	private String photo_id; //사진 ID
	private String review_id; // 게시글 번호
	private String photo_path; //사진
	private String photo_description; //사진 설명
	private int photo_order; //사진 순서
	
	public TravelReviewPhoto(String photo_id, String review_id, String photo_path, String photo_description,
			int photo_order) {
		this.photo_id = photo_id;
		this.review_id = review_id;
		this.photo_path = photo_path;
		this.photo_description = photo_description;
		this.photo_order = photo_order;
	}
	
	public String getPhoto_id() {
		return photo_id;
	}
	public void setPhoto_id(String photo_id) {
		this.photo_id = photo_id;
	}
	public String getReview_id() {
		return review_id;
	}
	public void setReview_id(String review_id) {
		this.review_id = review_id;
	}
	public String getPhoto_path() {
		return photo_path;
	}
	public void setPhoto_path(String photo_path) {
		this.photo_path = photo_path;
	}
	public String getPhoto_description() {
		return photo_description;
	}
	public void setPhoto_description(String photo_description) {
		this.photo_description = photo_description;
	}
	public int getPhoto_order() {
		return photo_order;
	}
	public void setPhoto_order(int photo_order) {
		this.photo_order = photo_order;
	}
	
	
}
