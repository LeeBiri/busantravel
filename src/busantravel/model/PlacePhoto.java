package busantravel.model;

public class PlacePhoto {
	private String p_id; //사진 ID
	private String p_num; // 여행지 번호
	private String p_type; //여행지 구분
	private String p_path; //사진 경로
	private String p_desc; //사진 설명
	
	
	public PlacePhoto(String p_id, String p_num, String p_type, String p_path, String p_desc) {
		this.p_id = p_id;
		this.p_num = p_num;
		this.p_type = p_type;
		this.p_path = p_path;
		this.p_desc = p_desc;
	}
	
	public String getP_id() {
		return p_id;
	}
	public void setP_id(String p_id) {
		this.p_id = p_id;
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
	public String getP_path() {
		return p_path;
	}
	public void setP_path(String p_path) {
		this.p_path = p_path;
	}
	public String getP_desc() {
		return p_desc;
	}
	public void setP_desc(String p_desc) {
		this.p_desc = p_desc;
	}
	
	
}
