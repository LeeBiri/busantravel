package busantravel.model;

public class ReviewComment {
	private String comment_id; //댓글 번호
	private String review_id; //게시글 번호
	private String m_id; //작성자 ID
	private String comment_text; //댓글 내용
	
	public ReviewComment(String comment_id, String review_id, String m_id, String comment_text) {
		this.comment_id = comment_id;
		this.review_id = review_id;
		this.m_id = m_id;
		this.comment_text = comment_text;
	}
	
	public String getComment_id() {
		return comment_id;
	}
	public void setComment_id(String comment_id) {
		this.comment_id = comment_id;
	}
	public String getReview_id() {
		return review_id;
	}
	public void setReview_id(String review_id) {
		this.review_id = review_id;
	}
	public String getM_id() {
		return m_id;
	}
	public void setM_id(String m_id) {
		this.m_id = m_id;
	}
	public String getComment_text() {
		return comment_text;
	}
	public void setComment_text(String comment_text) {
		this.comment_text = comment_text;
	}
	
	
}
