package busantravel.model;

public class PlaceComment {
	private int c_id; //댓글 ID
	private String p_num; //여행지 번호
	private String p_type; //여행지 구분
	private String m_id; //작성자 ID
	private String c_text; //댓글 내용
	private int rating; //별점
	
	
	public PlaceComment(int c_id, String p_num, String p_type, String m_id, String c_text, int rating) {
		this.c_id = c_id;
		this.p_num = p_num;
		this.p_type = p_type;
		this.m_id = m_id;
		this.c_text = c_text;
		this.rating = rating;
	}
	
	
	public int getC_id() {
		return c_id;
	}
	public void setC_id(int c_id) {
		this.c_id = c_id;
	}
	public String getP_num() {
		return p_num;
	}
	public void setP_num(String p_num) {
		this.p_num = p_num;
	}
	public String getP_type() {
		return p_type;
	}
	public void setP_type(String p_type) {
		this.p_type = p_type;
	}
	public String getM_id() {
		return m_id;
	}
	public void setM_id(String m_id) {
		this.m_id = m_id;
	}
	public String getC_text() {
		return c_text;
	}
	public void setC_text(String c_text) {
		this.c_text = c_text;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	
	
}
