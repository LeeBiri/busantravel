package busantravel.model;

import java.time.LocalDateTime;

public class TravelReview {
	private String review_id; // 게시글 번호
	private String m_id; //작성자 ID
	private LocalDateTime created_at; //작성 시각
	private String title; //글제목
	private String content; //글내용
	
	public TravelReview(String review_id, String m_id, LocalDateTime created_at, String title, String content) {
		this.review_id = review_id;
		this.m_id = m_id;
		this.created_at = created_at;
		this.title = title;
		this.content = content;
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
	public LocalDateTime getCreated_at() {
		return created_at;
	}
	public void setCreated_at(LocalDateTime created_at) {
		this.created_at = created_at;
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
	
	
}
